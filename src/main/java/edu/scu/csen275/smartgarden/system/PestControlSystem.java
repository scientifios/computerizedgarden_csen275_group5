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
 * Pest management logic for the garden simulation.
 * Tracks active pests, applies damage, and triggers zone treatments when threat thresholds are met.
 */
public class PestControlSystem {
    private final Garden garden;
    private final List<Pest> pests;
    private final IntegerProperty pesticideStock;
    private final IntegerProperty detectionSensitivity;
    private final IntegerProperty treatmentThreshold;
    private final Random random;
    private PestEventBridge pestEventBridge;
    private boolean apiModeEnabled = false; // Disables random spawning; pests must be registered externally
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    private static final Logger logger = Logger.getInstance();
    private static final int INITIAL_PESTICIDE_STOCK = 50;
    private static final int DEFAULT_SENSITIVITY = 50;
    private static final int DEFAULT_THRESHOLD = 30; // Infestation percentage threshold used to trigger treatment
    private static final double PEST_SPAWN_PROBABILITY = 0.05; // 5% per check
    
    /**
     * Initializes pest tracking and treatment configuration for the given garden.
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
     * Initializes pest tracking and treatment configuration for the given garden.
     */
    public void setApiModeEnabled(boolean enabled) {
        this.apiModeEnabled = enabled;
    }
    
    /**
     * @return whether API mode is enabled
     */
    public boolean isApiModeEnabled() {
        return apiModeEnabled;
    }
    
    /**
     * Advances pest simulation by one tick: optionally spawns pests, applies damage, and triggers treatments.
     */
    public void update() {
        // Drop null entries (safety for cross-thread modifications)
        pests.removeIf(p -> p == null);
        
        // Random spawning is disabled in API mode; pests must be registered externally.
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
     * Spawns a random harmful pest near a randomly selected living plant.
     */
    private void spawnPest() {
        List<Plant> livingPlants = garden.getLivingPlants();
        if (livingPlants.isEmpty()) {
            return;
        }
        
        Plant targetPlant = livingPlants.get(random.nextInt(livingPlants.size()));
        Position position = targetPlant.getPosition();
        
        String[] harmfulTypes = {"Red Mite", "Green Leaf Worm", "Black Beetle", "Brown Caterpillar"};
        String type = harmfulTypes[random.nextInt(harmfulTypes.length)];
        Pest newPest = new HarmfulPest(type, position);
        logger.warning("PestControl", type + " appeared at " + position);
        
        pests.add(newPest);
        
        // Notify UI listeners of the spawn event.
        if (pestEventBridge != null) {
            pestEventBridge.notifyPestSpawned(position, newPest.getPestType(), true);
        }
    }
    
    /**
     * Registers an externally created pest so it participates in damage and treatment logic.
     *
     * @param pest pest instance to register
     */
    public void registerPest(Pest pest) {
        if (pest != null && pest.isAlive()) {
            pests.add(pest);
            logger.info("PestControl", "Registered external pest: " + pest.getPestType() + 
                       " at " + pest.getPosition());
            
            // Notify UI listeners.
            if (pestEventBridge != null) {
                pestEventBridge.notifyPestSpawned(pest.getPosition(), pest.getPestType(), true);
            }
            
            // Trigger an immediate assessment for the affected zone.
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
                // Remove pests that no longer have a valid target plant.
                pests.remove(pest);
            }
        }
    }
    
    /**
     * Assesses zone threat level and schedules treatment when threat is high.
     * Treatment is delayed briefly so the UI can display pest activity; falls back to the scheduler in headless mode.
     */
    private void assessAndTreat(Zone zone) {
        ThreatLevel threat = assessThreat(zone);
        
        if (threat == ThreatLevel.HIGH || threat == ThreatLevel.CRITICAL) {
            logger.info("PestControl", "Threat detected in Zone " + zone.getZoneId() + " (" + threat + ")");
            
            // Delay treatment briefly to keep pest activity visible in the UI.
            // Use Timeline when available; otherwise fall back to the scheduler.
            Timeline delayTreatment = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    applyTreatment(zone);
                })
            );
            
            try {
                delayTreatment.play();
            } catch (RuntimeException e) {
                // Headless fallback: schedule treatment without JavaFX.
                scheduler.schedule(() -> {
                    applyTreatment(zone);
                }, 3, TimeUnit.SECONDS);
            }
        }
    }
    
    /**
     * Computes the zone threat level from infestation metrics and active pest presence.
     */
    private ThreatLevel assessThreat(Zone zone) {
        int infestationLevel = zone.getPestInfestationLevel();
        
        long harmfulPestCount = pests.stream()
            .filter(p -> p != null && p.isAlive())
            .filter(p -> zone.containsPosition(p.getPosition()))
            .count();
        
        // If there are harmful pests present, treat immediately to prevent plant damage
        if (harmfulPestCount > 0) {
            if (infestationLevel >= 80 || harmfulPestCount >= 2) {
                return ThreatLevel.CRITICAL;
            } else {
                return ThreatLevel.HIGH;
            }
        }
        
        // If no active pests are found, fall back to infestation-only thresholds.
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
        
        // Notify UI before mutation so animations can reference current pests/plants.
        // Emit pesticide-applied events for living plants in the zone.
        for (Plant plant : zone.getPlants()) {
            if (!plant.isDead() && pestEventBridge != null) {
                pestEventBridge.notifyPesticideApplied(plant.getPosition());
            }
        }
        
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
        // Drop null entries before computing zone metrics.
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
     * Triggers an immediate treatment for the given zone id.
     */
    public void manualTreat(int zoneId) {
        Zone zone = garden.getZone(zoneId);
        if (zone != null) {
            logger.info("PestControl", "Manual treatment triggered for Zone " + zoneId);
            applyTreatment(zone);
        }
    }
    
    /**
     * Adds pesticide units to the current stock.
     */
    public void refillPesticide(int amount) {
        pesticideStock.set(pesticideStock.get() + amount);
        logger.info("PestControl", "Pesticide stock refilled by " + amount + 
                   ". Total: " + pesticideStock.get());
    }
    
    /**
     * @return number of currently alive pests being tracked
     */
    public int getHarmfulPestCount() {
        // Remove nulls first (defensive)
        pests.removeIf(p -> p == null);
        return (int) pests.stream()
            .filter(p -> p != null && p.isAlive())
            .count();
    }
    
    /**
     * Returns the number of alive pests currently located at the given position.
     */
    public int getActivePestCountAtPosition(edu.scu.csen275.smartgarden.model.Position position) {
        return (int) pests.stream()
            .filter(p -> p != null && p.isAlive())
            .filter(p -> p.getPosition().equals(position))
            .count();
    }
    
    public IntegerProperty pesticideStockProperty() {
        return pesticideStock;
    }
    
    public IntegerProperty detectionSensitivityProperty() {
        return detectionSensitivity;
    }
    
    public IntegerProperty treatmentThresholdProperty() {
        return treatmentThreshold;
    }
    
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
     * Sets the UI event bridge used to emit pest-related events.
     */
    public void setPestEventBridge(PestEventBridge bridge) {
        this.pestEventBridge = bridge;
    }
    
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

