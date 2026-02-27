package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Automated heating system that maintains optimal temperature for plants.
 */
public class HeatingSystem {
    private final Garden garden;
    private final Map<Integer, TemperatureSensor> sensors;
    private final IntegerProperty currentTemperature;
    private final IntegerProperty targetMinTemperature;
    private final IntegerProperty targetMaxTemperature;
    private final ObjectProperty<HeatingMode> heatingMode;
    private final IntegerProperty energyConsumption;
    private boolean apiModeEnabled = false; // When enabled, temperature change logs are suppressed
    
    private static final Logger logger = Logger.getInstance();
    private static final int DEFAULT_MIN_TEMP = 15; // Celsius
    private static final int DEFAULT_MAX_TEMP = 28; // Celsius
    private static final int DEFAULT_AMBIENT_TEMP = 20;
    
    /**
     * Creates a new HeatingSystem for the garden.
     */
    public HeatingSystem(Garden garden) {
        this.garden = garden;
        this.sensors = new HashMap<>();
        this.currentTemperature = new SimpleIntegerProperty(DEFAULT_AMBIENT_TEMP);
        this.targetMinTemperature = new SimpleIntegerProperty(DEFAULT_MIN_TEMP);
        this.targetMaxTemperature = new SimpleIntegerProperty(DEFAULT_MAX_TEMP);
        this.heatingMode = new SimpleObjectProperty<>(HeatingMode.OFF);
        this.energyConsumption = new SimpleIntegerProperty(0);
        
        initializeSensors();
        logger.info("Heating", "Heating system initialized. Target range: " + 
                   DEFAULT_MIN_TEMP + "-" + DEFAULT_MAX_TEMP + "°C");
    }
    
    /**
     * Initializes temperature sensors for each zone.
     */
    private void initializeSensors() {
        for (Zone zone : garden.getZones()) {
            sensors.put(zone.getZoneId(), new TemperatureSensor(zone));
            zone.setTemperature(DEFAULT_AMBIENT_TEMP);
        }
    }
    
    /**
     * Monitors temperature and adjusts heating as needed.
     */
    public void update() {
        int avgTemp = calculateAverageTemperature();
        currentTemperature.set(avgTemp);
        
        // Determine heating mode based on temperature
        if (avgTemp < targetMinTemperature.get()) {
            activateHeating();
        } else if (avgTemp > targetMaxTemperature.get()) {
            deactivateHeating();
        } else if (avgTemp >= targetMinTemperature.get() + 2) {
            // Slight hysteresis to avoid rapid cycling
            deactivateHeating();
        }
        
        // Apply temperature effects to plants
        applyTemperatureEffects();
    }
    
    /**
     * Activates heating system.
     */
    private void activateHeating() {
        if (heatingMode.get() == HeatingMode.OFF) {
            logger.info("Heating", "Heating activated. Current temp: " + 
                       currentTemperature.get() + "°C");
        }
        
        // Set heating mode based on temperature deficit
        int deficit = targetMinTemperature.get() - currentTemperature.get();
        if (deficit > 10) {
            heatingMode.set(HeatingMode.HIGH);
        } else if (deficit > 5) {
            heatingMode.set(HeatingMode.MEDIUM);
        } else {
            heatingMode.set(HeatingMode.LOW);
        }
        
        // Increase temperature based on mode
        int increase = switch (heatingMode.get()) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
            default -> 0;
        };
        
        increaseTemperature(increase);
        energyConsumption.set(energyConsumption.get() + increase);
    }
    
    /**
     * Deactivates heating system.
     */
    private void deactivateHeating() {
        if (heatingMode.get() != HeatingMode.OFF) {
            heatingMode.set(HeatingMode.OFF);
            logger.info("Heating", "Heating deactivated. Current temp: " + 
                       currentTemperature.get() + "°C");
        }
        
        // Don't apply natural cooling - weather system controls temperature
        // Natural cooling removed to prevent interference with weather-set temperatures
    }
    
    /**
     * Increases temperature in all zones.
     */
    private void increaseTemperature(int amount) {
        int oldTemp = currentTemperature.get();
        for (Zone zone : garden.getZones()) {
            int newTemp = zone.getTemperature() + amount;
            zone.setTemperature(newTemp);
        }
        // Update current temperature (it's calculated from zones in monitor(), but log here)
        int newTemp = oldTemp + amount;
        logger.info("Heating", "Temperature increasing: " + oldTemp + "°C → " + newTemp + "°C (increased by " + amount + "°C)");
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
        // Update current temperature (it's calculated from zones in monitor(), but log here)
        int newTemp = Math.max(0, oldTemp - amount);
        if (oldTemp != newTemp) {
            logger.info("Heating", "Temperature decreasing: " + oldTemp + "°C → " + newTemp + "°C (decreased by " + amount + "°C)");
        }
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
            logger.info("Heating", "Ambient temperature set to " + temperature + "°C (was " + oldTemp + "°C)");
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
     * Sets target temperature range.
     */
    public void setTargetRange(int minTemp, int maxTemp) {
        if (minTemp >= maxTemp) {
            throw new IllegalArgumentException("Min temperature must be less than max");
        }
        targetMinTemperature.set(minTemp);
        targetMaxTemperature.set(maxTemp);
        logger.info("Heating", "Target temperature range updated: " + 
                   minTemp + "-" + maxTemp + "°C");
    }
    
    /**
     * Gets current heating status.
     */
    public String getStatus() {
        return "Temperature: " + currentTemperature.get() + "°C, Mode: " + 
               heatingMode.get() + ", Energy: " + energyConsumption.get() + " units";
    }
    
    // Property getters
    public IntegerProperty currentTemperatureProperty() {
        return currentTemperature;
    }
    
    public IntegerProperty targetMinTemperatureProperty() {
        return targetMinTemperature;
    }
    
    public IntegerProperty targetMaxTemperatureProperty() {
        return targetMaxTemperature;
    }
    
    public ObjectProperty<HeatingMode> heatingModeProperty() {
        return heatingMode;
    }
    
    public IntegerProperty energyConsumptionProperty() {
        return energyConsumption;
    }
    
    // Value getters
    public int getCurrentTemperature() {
        return currentTemperature.get();
    }
    
    public int getTargetMinTemperature() {
        return targetMinTemperature.get();
    }
    
    public int getTargetMaxTemperature() {
        return targetMaxTemperature.get();
    }
    
    public HeatingMode getHeatingMode() {
        return heatingMode.get();
    }
    
    public int getEnergyConsumption() {
        return energyConsumption.get();
    }
    
    /**
     * Heating mode enumeration.
     */
    public enum HeatingMode {
        OFF,
        LOW,
        MEDIUM,
        HIGH
    }
    
    @Override
    public String toString() {
        return "HeatingSystem[Temp: " + currentTemperature.get() + "°C, Mode: " + 
               heatingMode.get() + ", Target: " + targetMinTemperature.get() + 
               "-" + targetMaxTemperature.get() + "°C]";
    }
}

