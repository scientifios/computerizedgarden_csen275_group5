package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.util.Logger;
import java.time.LocalDateTime;

/**
 * Sprinkler device that delivers water to a zone.
 */
public class Sprinkler {
    private final Zone zone;
    private final int flowRate; // liters per minute
    private boolean isActive;
    private LocalDateTime lastActivation;
    
    private static final Logger logger = Logger.getInstance();
    private static final int DEFAULT_FLOW_RATE = 5; // liters per minute per plant
    
    /**
     * Creates a new Sprinkler for a zone.
     */
    public Sprinkler(Zone zone) {
        this(zone, DEFAULT_FLOW_RATE);
    }
    
    /**
     * Creates a new Sprinkler with custom flow rate.
     */
    public Sprinkler(Zone zone, int flowRate) {
        this.zone = zone;
        this.flowRate = flowRate;
        this.isActive = false;
        this.lastActivation = null;
    }
    
    /**
     * Activates the sprinkler.
     */
    public void activate() {
        if (!isActive) {
            isActive = true;
            lastActivation = LocalDateTime.now();
            logger.info("Watering", "Sprinkler activated for Zone " + zone.getZoneId());
        }
    }
    
    /**
     * Deactivates the sprinkler.
     */
    public void deactivate() {
        if (isActive) {
            isActive = false;
            logger.info("Watering", "Sprinkler deactivated for Zone " + zone.getZoneId());
        }
    }
    
    /**
     * Distributes water to plants in the zone.
     */
    public int distributeWater(int amount) {
        if (!isActive) {
            return 0;
        }
        
        int waterUsed = 0;
        
        // Water each plant in the zone
        for (Plant plant : zone.getLivingPlants()) {
            int waterForPlant = Math.min(amount / zone.getLivingPlantCount(), flowRate);
            plant.water(waterForPlant);
            waterUsed += waterForPlant;
        }
        
        // Update zone moisture
        zone.updateMoisture(amount / 10); // Partial absorption
        
        return waterUsed;
    }
    
    // Getters
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
    
    @Override
    public String toString() {
        return "Sprinkler[Zone " + zone.getZoneId() + 
               ", Active: " + isActive + 
               ", Flow: " + flowRate + "L/min]";
    }
}

