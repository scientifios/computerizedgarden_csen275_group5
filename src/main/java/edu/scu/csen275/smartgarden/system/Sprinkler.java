package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.util.Logger;
import java.time.LocalDateTime;

/**
 * Zone-scoped sprinkler that applies watering events to living plants and updates zone moisture.
 */
public class Sprinkler {
    private final Zone zone;
    private final int flowRate; // L/min
    private boolean isActive;
    private LocalDateTime lastActivation;
    private int activationCount;
    
    private static final Logger logger = Logger.getInstance();
    private static final int DEFAULT_FLOW_RATE = 5; // L/min per plant
    
    /**
     * Creates a sprinkler for the given zone using the default flow rate.
     */
    public Sprinkler(Zone zone) {
        this(zone, DEFAULT_FLOW_RATE);
    }
    
    /**
     * Creates a sprinkler for the given zone with a fixed flow rate limit.
     */
    public Sprinkler(Zone zone, int flowRate) {
        this.zone = zone;
        this.flowRate = flowRate;
        this.isActive = false;
        this.lastActivation = null;
        this.activationCount = 0;
    }
    
    /**
     * Enables watering and records the activation time.
     */
    public void activate() {
        if (!isActive) {
            isActive = true;
            lastActivation = LocalDateTime.now();
            activationCount++;
            logger.info("Watering", "Sprinkler activated for Zone " + zone.getZoneId());
        }
    }
    
    /**
     * Disables watering.
     */
    public void deactivate() {
        if (isActive) {
            isActive = false;
            logger.info("Watering", "Sprinkler deactivated for Zone " + zone.getZoneId());
        }
    }
    
    /**
     * Applies a watering amount to living plants in the zone and updates zone moisture.
     *
     * @return total water actually applied to plants (0 if inactive)
     */
    public int distributeWater(int amount) {
        if (!isActive) {
            return 0;
        }
        
        int waterUsed = 0;
        
        for (Plant plant : zone.getLivingPlants()) {
            int waterForPlant = Math.min(amount / zone.getLivingPlantCount(), flowRate);
            plant.water(waterForPlant);
            waterUsed += waterForPlant;
        }
        
        zone.updateMoisture(amount / 10); // Simplified absorption model
        
        return waterUsed;
    }
    
    public Zone getZone() {
        return zone;
    }
    
    public int getFlowRate() {
        return flowRate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public LocalDateTime getLastActivation() {
        return lastActivation;
    }

    public int getActivationCount() {
        return activationCount;
    }
    
    @Override
    public String toString() {
        return "Sprinkler[Zone " + zone.getZoneId() + 
               ", Active: " + isActive + 
               ", Activations: " + activationCount +
               ", Flow: " + flowRate + "L/min]";
    }
}
