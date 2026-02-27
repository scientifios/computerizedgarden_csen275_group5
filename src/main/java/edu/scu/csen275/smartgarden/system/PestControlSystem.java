package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Position;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.ui.PestEventBridge;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Automated pest control system that detects and treats infestations.
 */
public class PestControlSystem {
    private final Garden garden;
    private final List<Pest> pests;
    private final IntegerProperty pesticideStock;
    private final IntegerProperty detectionSensitivity;
    private final IntegerProperty treatmentThreshold;
    private final Random random;
    private PestEventBridge pestEventBridge;
    private boolean apiModeEnabled = false; // When enabled, automatic pest spawning is disabled
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    private static final Logger logger = Logger.getInstance();
    private static final int INITIAL_PESTICIDE_STOCK = 50;
    private static final int DEFAULT_SENSITIVITY = 50;
    private static final int DEFAULT_THRESHOLD = 30; // Infestation percentage - lowered for earlier treatment
    private static final double PEST_SPAWN_PROBABILITY = 0.05; // 5% per check
    
    /**
     * Creates a new PestControlSystem for the garden.
     */
    public PestControlSystem(Garden garden) {
        this.garden = garden;
        this.pests = new ArrayList<>();
        this.pesticideStock = new SimpleIntegerProperty(INITIAL_PESTICIDE_STOCK);
        this.detectionSensitivity = new SimpleIntegerProperty(DEFAULT_SENSITIVITY);
        this.treatmentThreshold = new SimpleIntegerProperty(DEFAULT_THRESHOLD);
        this.random = new Random();
        
        logger.info("PestControl", "Pest control system initialized. Stock: " + 
                   INITIAL_PESTICIDE_STOCK + ", Threshold: " + DEFAULT_THRESHOLD + "%");
    }
    
    /**
     * Sets whether API mode is enabled.
     * When enabled, automatic pest spawning is disabled (pests only via API calls).
     * Automatic pesticide application still works when pests are detected.
     */
    public void setApiModeEnabled(boolean enabled) {
        this.apiModeEnabled = enabled;
    }
    
    /**
     * Checks if API mode is enabled.
     */
    public boolean isApiModeEnabled() {
        return apiModeEnabled;
    }
    
    /**
     * Updates pest system - spawns pests, applies damage, checks for treatment.
     */
    public void update() {
        // Clean up null pests first (defensive programming for concurrent access)
        pests.removeIf(p -> p == null);
        
        // Randomly spawn pests (skip if API mode enabled)
        // In API mode, pests are only spawned via API calls (api.parasite())
        if (!apiModeEnabled && random.nextDouble() < PEST_SPAWN_PROBABILITY && garden.getLivingPlants().size() > 0) {
            spawnPest();
        }
        
        // Apply pest damage
        applyPestDamage();
        
        // Check each zone for treatment needs
        for (Zone zone : garden.getZones()) {
            if (zone.getLivingPlantCount() > 0) {
                assessAndTreat(zone);
            }
        }
        
        // Update zone infestation levels
        updateInfestationLevels();
    }
    
    /**
     * Spawns a new pest at a random location.
     */
    private void spawnPest() {
        List<Plant> livingPlants = garden.getLivingPlants();
        if (livingPlants.isEmpty()) {
            return;
        }
        
        // Select random plant to attack
        Plant targetPlant = livingPlants.get(random.nextInt(livingPlants.size()));
        Position position = targetPlant.getPosition();
        
        // All pests are harmful
        String[] harmfulTypes = {"Red Mite", "Green Leaf Worm", "Black Beetle", "Brown Caterpillar"};
        String type = harmfulTypes[random.nextInt(harmfulTypes.length)];
        Pest newPest = new HarmfulPest(type, position);
        logger.warning("PestControl", type + " appeared at " + position);
        
        pests.add(newPest);
        
        // NOTIFY UI IMMEDIATELY when pest spawns
        if (pestEventBridge != null) {
            pestEventBridge.notifyPestSpawned(position, newPest.getPestType(), true);
        }
    }
    
    /**
     * Registers a pest from external source (e.g., API).
     * This allows API-created pests to be tracked and automatically treated.
     * 
     * @param pest The pest to register
     */
    public void registerPest(Pest pest) {
        if (pest != null && pest.isAlive()) {
            pests.add(pest);
            logger.info("PestControl", "Registered external pest: " + pest.getPestType() + 
                       " at " + pest.getPosition());
            
            // Notify UI
            if (pestEventBridge != null) {
                pestEventBridge.notifyPestSpawned(pest.getPosition(), pest.getPestType(), true);
            }
            
            // Immediately check for treatment (don't wait for next tick)
            Zone zone = garden.getZoneForPosition(pest.getPosition());
            if (zone != null) {
                assessAndTreat(zone);
            }
        }
    }
    
    /**
     * Applies damage from all pests to their target plants.
     */
    private void applyPestDamage() {
        for (Pest pest : new ArrayList<>(pests)) {
            // Skip null pests
            if (pest == null) {
                pests.remove(pest);
                continue;
            }
            
            if (!pest.isAlive()) {
                pests.remove(pest);
                continue;
            }
            
            Plant plant = garden.getPlant(pest.getPosition());
            if (plant != null && !plant.isDead()) {
                pest.causeDamage(plant);
            } else {
                // Plant is gone, remove pest
                pests.remove(pest);
            }
        }
    }
    
    /**
     * Assesses threat level and applies treatment if needed.
     * ADDED: Delay before treatment so pests are visible for a few seconds.
     * Works in both UI mode (JavaFX Timeline) and headless mode (thread-based delay).
     */
    private void assessAndTreat(Zone zone) {
        ThreatLevel threat = assessThreat(zone);
        
        if (threat == ThreatLevel.HIGH || threat == ThreatLevel.CRITICAL) {
            logger.info("PestControl", "Threat detected in Zone " + zone.getZoneId() + " (" + threat + ")");
            
            // DELAY treatment by 3 seconds so user can see pests attacking
            // Try to use JavaFX Timeline if available (UI mode)
            // If JavaFX toolkit not initialized, fall back to thread-based delay (headless/API mode)
            Timeline delayTreatment = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    applyTreatment(zone);
                })
            );
            
            try {
                delayTreatment.play();
            } catch (RuntimeException e) {
                // JavaFX toolkit not initialized - use thread-based delay (headless/API mode)
                scheduler.schedule(() -> {
                    applyTreatment(zone);
                }, 3, TimeUnit.SECONDS);
            }
        }
    }
    
    /**
     * Assesses the threat level for a zone.
     * Now also checks for actual harmful pests in the zone - treats immediately when pests are present.
     */
    private ThreatLevel assessThreat(Zone zone) {
        int infestationLevel = zone.getPestInfestationLevel();
        
        // Check for actual pests in this zone
        long harmfulPestCount = pests.stream()
            .filter(p -> p != null && p.isAlive())
            .filter(p -> zone.containsPosition(p.getPosition()))
            .count();
        
        // If there are harmful pests present, treat immediately to prevent plant damage
        if (harmfulPestCount > 0) {
            if (infestationLevel >= 80 || harmfulPestCount >= 2) {
                return ThreatLevel.CRITICAL;
            } else {
                // Any harmful pests = HIGH threat, trigger treatment
                return ThreatLevel.HIGH;
            }
        }
        
        // If no pests but high infestation (might be residual), still assess
        if (infestationLevel >= 80) {
            return ThreatLevel.CRITICAL;
        } else if (infestationLevel >= treatmentThreshold.get()) {
            return ThreatLevel.HIGH;
        } else if (infestationLevel >= 40) {
            return ThreatLevel.MEDIUM;
        } else {
            return ThreatLevel.LOW;
        }
    }
    
    /**
     * Applies pesticide treatment to a zone.
     */
    private void applyTreatment(Zone zone) {
        if (pesticideStock.get() <= 0) {
            logger.error("PestControl", "Cannot treat Zone " + zone.getZoneId() + 
                        " - no pesticide stock");
            return;
        }
        
        logger.info("PestControl", "Applying treatment to Zone " + zone.getZoneId() + 
                   " - Infestation: " + zone.getPestInfestationLevel() + "%");
        
        // NOTIFY UI FIRST - before removing pests (so animation can show them)
        // Notify for all plants in the zone
        for (Plant plant : zone.getPlants()) {
            if (!plant.isDead() && pestEventBridge != null) {
                pestEventBridge.notifyPesticideApplied(plant.getPosition());
            }
        }
        
        // Remove pests in the zone
        int pestsEliminated = 0;
        
        for (Pest pest : new ArrayList<>(pests)) {
            if (pest == null) {
                pests.remove(pest);
                continue;
            }
            
            if (zone.containsPosition(pest.getPosition())) {
                pest.eliminate();
                pests.remove(pest);
                pestsEliminated++;
            }
        }
        
        // Reduce pest attacks on plants
        for (Plant plant : zone.getPlants()) {
            if (!plant.isDead()) {
                plant.reducePestAttacks(5);
            }
        }
        
        // Update zone infestation
        int newLevel = Math.max(0, zone.getPestInfestationLevel() - 50);
        zone.updatePestLevel(newLevel);
        
        // Consume pesticide
        pesticideStock.set(pesticideStock.get() - 1);
        
        logger.info("PestControl", "Treatment complete for Zone " + zone.getZoneId() + 
                   ". Eliminated: " + pestsEliminated + ", Stock remaining: " + pesticideStock.get());
    }
    
    /**
     * Updates infestation levels for all zones.
     */
    private void updateInfestationLevels() {
        // First, remove any null pests (defensive programming)
        pests.removeIf(p -> p == null);
        
        for (Zone zone : garden.getZones()) {
            int pestCount = (int) pests.stream()
                .filter(p -> p != null && p.isAlive())
                .filter(p -> zone.containsPosition(p.getPosition()))
                .count();
            
            int plantCount = zone.getLivingPlantCount();
            if (plantCount > 0) {
                int infestationLevel = Math.min(100, (pestCount * 100) / (plantCount * 2));
                zone.updatePestLevel(infestationLevel);
            } else {
                zone.updatePestLevel(0);
            }
        }
    }
    
    /**
     * Manually treats a zone (user override).
     */
    public void manualTreat(int zoneId) {
        Zone zone = garden.getZone(zoneId);
        if (zone != null) {
            logger.info("PestControl", "Manual treatment triggered for Zone " + zoneId);
            applyTreatment(zone);
        }
    }
    
    /**
     * Refills pesticide stock.
     */
    public void refillPesticide(int amount) {
        pesticideStock.set(pesticideStock.get() + amount);
        logger.info("PestControl", "Pesticide stock refilled by " + amount + 
                   ". Total: " + pesticideStock.get());
    }
    
    /**
     * Gets count of pests.
     */
    public int getHarmfulPestCount() {
        // Remove nulls first (defensive)
        pests.removeIf(p -> p == null);
        return (int) pests.stream()
            .filter(p -> p != null && p.isAlive())
            .count();
    }
    
    /**
     * Gets the number of active pests at a specific position.
     */
    public int getActivePestCountAtPosition(edu.scu.csen275.smartgarden.model.Position position) {
        return (int) pests.stream()
            .filter(p -> p != null && p.isAlive())
            .filter(p -> p.getPosition().equals(position))
            .count();
    }
    
    // Property getters
    public IntegerProperty pesticideStockProperty() {
        return pesticideStock;
    }
    
    public IntegerProperty detectionSensitivityProperty() {
        return detectionSensitivity;
    }
    
    public IntegerProperty treatmentThresholdProperty() {
        return treatmentThreshold;
    }
    
    // Value getters
    public int getPesticideStock() {
        return pesticideStock.get();
    }
    
    public int getDetectionSensitivity() {
        return detectionSensitivity.get();
    }
    
    public int getTreatmentThreshold() {
        return treatmentThreshold.get();
    }
    
    public List<Pest> getPests() {
        return new ArrayList<>(pests);
    }
    
    /**
     * Sets the pest event bridge for UI notifications.
     */
    public void setPestEventBridge(PestEventBridge bridge) {
        this.pestEventBridge = bridge;
    }
    
    /**
     * Threat level enumeration.
     */
    public enum ThreatLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    @Override
    public String toString() {
        return "PestControlSystem[Pests: " + getHarmfulPestCount() + 
               ", Stock: " + pesticideStock.get() + "]";
    }
}

