package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Automated cooling system that maintains optimal temperature for plants.
 * Activates when temperature exceeds the maximum temperature of any plant.
 */
public class CoolingSystem {
    private final Garden garden;
    private final Map<Integer, TemperatureSensor> sensors;
    private final IntegerProperty currentTemperature;
    private final ObjectProperty<CoolingMode> coolingMode;
    private final IntegerProperty energyConsumption;
    private boolean apiModeEnabled = false; // When enabled, temperature change logs are suppressed
    
    private static final Logger logger = Logger.getInstance();
    private static final int DEFAULT_AMBIENT_TEMP = 20;
    
    /**
     * Creates a new CoolingSystem for the garden.
     */
    public CoolingSystem(Garden garden) {
        this.garden = garden;
        this.sensors = new HashMap<>();
        this.currentTemperature = new SimpleIntegerProperty(DEFAULT_AMBIENT_TEMP);
        this.coolingMode = new SimpleObjectProperty<>(CoolingMode.OFF);
        this.energyConsumption = new SimpleIntegerProperty(0);
        
        initializeSensors();
        logger.info("Cooling", "Cooling system initialized");
    }
    
    /**
     * Initializes temperature sensors for each zone.
     */
    private void initializeSensors() {
        for (Zone zone : garden.getZones()) {
            sensors.put(zone.getZoneId(), new TemperatureSensor(zone));
        }
    }
    
    /**
     * Monitors temperature and adjusts cooling as needed.
     * Activates cooling if temperature exceeds maxTemperature of any plant.
     */
    public void update() {
        int avgTemp = calculateAverageTemperature();
        currentTemperature.set(avgTemp);
        
        // Find the highest maxTemperature among all living plants
        int maxPlantTemp = getMaxTemperatureThreshold();
        
        // Determine cooling mode based on temperature
        if (maxPlantTemp > 0 && avgTemp > maxPlantTemp) {
            activateCooling(maxPlantTemp);
        } else if (avgTemp <= maxPlantTemp - 2) {
            // Slight hysteresis to avoid rapid cycling
            deactivateCooling();
        } else {
            deactivateCooling();
        }
        
        // Apply temperature effects to plants
        applyTemperatureEffects();
    }
    
    /**
     * Gets the maximum temperature threshold from all living plants.
     * Returns the highest maxTemperature value, or 0 if no plants exist.
     */
    private int getMaxTemperatureThreshold() {
        int maxThreshold = 0;
        for (Plant plant : garden.getLivingPlants()) {
            if (plant.getMaxTemperature() > maxThreshold) {
                maxThreshold = plant.getMaxTemperature();
            }
        }
        return maxThreshold;
    }
    
    /**
     * Activates cooling system.
     */
    private void activateCooling(int maxPlantTemp) {
        int excess = currentTemperature.get() - maxPlantTemp;
        
        if (coolingMode.get() == CoolingMode.OFF) {
            logger.info("Cooling", "Cooling activated. Current temp: " + 
                       currentTemperature.get() + "°C (max threshold: " + maxPlantTemp + "°C)");
        }
        
        // Set cooling mode based on temperature excess
        if (excess > 10) {
            coolingMode.set(CoolingMode.HIGH);
        } else if (excess > 5) {
            coolingMode.set(CoolingMode.MEDIUM);
        } else {
            coolingMode.set(CoolingMode.LOW);
        }
        
        // Decrease temperature based on mode
        int decrease = switch (coolingMode.get()) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
            default -> 0;
        };
        
        decreaseTemperature(decrease);
        energyConsumption.set(energyConsumption.get() + decrease);
    }
    
    /**
     * Deactivates cooling system.
     */
    private void deactivateCooling() {
        if (coolingMode.get() != CoolingMode.OFF) {
            coolingMode.set(CoolingMode.OFF);
            logger.info("Cooling", "Cooling deactivated. Current temp: " + 
                       currentTemperature.get() + "°C");
        }
    }
    
    /**
     * Decreases temperature in all zones.
     */
    private void decreaseTemperature(int amount) {
        int oldTemp = currentTemperature.get();
        for (Zone zone : garden.getZones()) {
            int newTemp = Math.max(0, zone.getTemperature() - amount);
            zone.setTemperature(newTemp);
        }
        // Update current temperature
        int newTemp = Math.max(0, oldTemp - amount);
        logger.info("Cooling", "Temperature decreasing: " + oldTemp + "°C → " + newTemp + "°C (decreased by " + amount + "°C)");
    }
    
    /**
     * Calculates average temperature across all zones.
     */
    private int calculateAverageTemperature() {
        if (sensors.isEmpty()) {
            return DEFAULT_AMBIENT_TEMP;
        }
        
        int sum = 0;
        int count = 0;
        
        for (TemperatureSensor sensor : sensors.values()) {
            int reading = sensor.readValue();
            if (reading > -999) { // Valid reading
                sum += reading;
                count++;
            }
        }
        
        return count > 0 ? sum / count : DEFAULT_AMBIENT_TEMP;
    }
    
    /**
     * Applies temperature effects to all plants.
     */
    private void applyTemperatureEffects() {
        for (Plant plant : garden.getLivingPlants()) {
            Zone zone = garden.getZoneForPosition(plant.getPosition());
            if (zone != null) {
                plant.applyTemperatureEffect(zone.getTemperature());
            }
        }
    }
    
    /**
     * Manually sets ambient temperature (for weather simulation).
     */
    public void setAmbientTemperature(int temperature) {
        int oldTemp = currentTemperature.get();
        currentTemperature.set(temperature);
        for (Zone zone : garden.getZones()) {
            zone.setTemperature(temperature);
        }
        if (!apiModeEnabled) {
            logger.info("Cooling", "Ambient temperature set to " + temperature + "°C (was " + oldTemp + "°C)");
        }
    }
    
    /**
     * Sets whether API mode is enabled.
     * When enabled, temperature change logs are suppressed.
     */
    public void setApiModeEnabled(boolean enabled) {
        this.apiModeEnabled = enabled;
    }
    
    /**
     * Gets current cooling status.
     */
    public String getStatus() {
        return "Temperature: " + currentTemperature.get() + "°C, Mode: " + 
               coolingMode.get() + ", Energy: " + energyConsumption.get() + " units";
    }
    
    // Property getters
    public IntegerProperty currentTemperatureProperty() {
        return currentTemperature;
    }
    
    public ObjectProperty<CoolingMode> coolingModeProperty() {
        return coolingMode;
    }
    
    public IntegerProperty energyConsumptionProperty() {
        return energyConsumption;
    }
    
    // Value getters
    public int getCurrentTemperature() {
        return currentTemperature.get();
    }
    
    public CoolingMode getCoolingMode() {
        return coolingMode.get();
    }
    
    public int getEnergyConsumption() {
        return energyConsumption.get();
    }
    
    /**
     * Cooling mode enumeration.
     */
    public enum CoolingMode {
        OFF,
        LOW,
        MEDIUM,
        HIGH
    }
    
    @Override
    public String toString() {
        return "CoolingSystem[Temp: " + currentTemperature.get() + "°C, Mode: " + 
               coolingMode.get() + "]";
    }
}

