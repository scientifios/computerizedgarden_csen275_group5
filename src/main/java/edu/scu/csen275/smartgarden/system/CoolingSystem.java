package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Automated cooling system that maintains garden temperatures within plant-safe limits.
 * Uses the average sensor temperature across zones and compares it against the highest
 * maxTemperature among living plants, with hysteresis to avoid rapid on/off cycling.
 */
public class CoolingSystem {
    private final Garden garden;
    private final Map<Integer, TemperatureSensor> sensors;
    private final IntegerProperty currentTemperature;
    private final ObjectProperty<CoolingMode> coolingMode;
    private final IntegerProperty energyConsumption;
    private boolean apiModeEnabled = false; // Suppresses API-driven ambient temperature set logs (setAmbientTemperature)
    
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
     * Updates cooling once per simulation tick.
     * Computes the average temperature across zone sensors, compares it against the highest
     * maxTemperature among living plants (with hysteresis), and then applies temperature effects
     * to each plant based on its zone temperature.
     */
    public void update() {
        int avgTemp = calculateAverageTemperature();
        currentTemperature.set(avgTemp);
        
        int maxPlantTemp = getMaxTemperatureThreshold();
        
        if (maxPlantTemp > 0 && avgTemp > maxPlantTemp) {
            activateCooling(maxPlantTemp);
        } else if (avgTemp <= maxPlantTemp - 2) {
            deactivateCooling();
        } else {
            deactivateCooling();
        }
        
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
     * Activates cooling when the current temperature exceeds the plant threshold.
     * Selects a cooling mode based on the excess and applies a temperature decrease.
     */
    private void activateCooling(int maxPlantTemp) {
        int excess = currentTemperature.get() - maxPlantTemp;
        
        if (coolingMode.get() == CoolingMode.OFF) {
            logger.info("Cooling", "Cooling activated. Current temp: " + 
                       currentTemperature.get() + "°C (max threshold: " + maxPlantTemp + "°C)");
        }
        
        if (excess > 10) {
            coolingMode.set(CoolingMode.HIGH);
        } else if (excess > 5) {
            coolingMode.set(CoolingMode.MEDIUM);
        } else {
            coolingMode.set(CoolingMode.LOW);
        }
        
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
     * Turns cooling off (mode = OFF) and logs the transition once.
     */
    private void deactivateCooling() {
        if (coolingMode.get() != CoolingMode.OFF) {
            coolingMode.set(CoolingMode.OFF);
            logger.info("Cooling", "Cooling deactivated. Current temp: " + 
                       currentTemperature.get() + "°C");
        }
    }
    
    /**
     * Decreases the temperature of all zones by the given amount (clamped to 0) and logs the change.
     */
    private void decreaseTemperature(int amount) {
        int oldTemp = currentTemperature.get();
        for (Zone zone : garden.getZones()) {
            int newTemp = Math.max(0, zone.getTemperature() - amount);
            zone.setTemperature(newTemp);
        }
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
            if (reading > -999) { // Ignore invalid readings
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
     * Enables/disables API mode logging behavior.
     * When enabled, suppresses the log message produced by setAmbientTemperature().
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
    
    public IntegerProperty currentTemperatureProperty() {
        return currentTemperature;
    }
    
    public ObjectProperty<CoolingMode> coolingModeProperty() {
        return coolingMode;
    }
    
    public IntegerProperty energyConsumptionProperty() {
        return energyConsumption;
    }
    
    public int getCurrentTemperature() {
        return currentTemperature.get();
    }
    
    public CoolingMode getCoolingMode() {
        return coolingMode.get();
    }
    
    public int getEnergyConsumption() {
        return energyConsumption.get();
    }
    
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

