package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Zone;
import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import edu.scu.csen275.smartgarden.model.Plant;

/**
 * Automated watering system that manages irrigation across all zones.
 */
public class WateringSystem {
    private final Garden garden;
    private final Map<Integer, Sprinkler> sprinklers;
    private final Map<Integer, MoistureSensor> sensors;
    private final IntegerProperty waterSupply;
    private final IntegerProperty moistureThreshold;
    private WeatherSystem weatherSystem; // Reference to weather system
    
    private static final Logger logger = Logger.getInstance();
    private static final int INITIAL_WATER_SUPPLY = 10000; // liters
    private static final int DEFAULT_MOISTURE_THRESHOLD = 40; // percentage
    private static final int WATER_PER_CYCLE = 10; // amount to water per cycle
    
    /**
     * Creates a new WateringSystem for the garden.
     */
    public WateringSystem(Garden garden) {
        this.garden = garden;
        this.sprinklers = new HashMap<>();
        this.sensors = new HashMap<>();
        this.waterSupply = new SimpleIntegerProperty(INITIAL_WATER_SUPPLY);
        this.moistureThreshold = new SimpleIntegerProperty(DEFAULT_MOISTURE_THRESHOLD);
        
        initializeSprinklersAndSensors();
        logger.info("Watering", "Watering system initialized with " + 
                   sprinklers.size() + " zones");
    }
    
    /**
     * Sets the weather system reference to check for rain.
     * Also adds a listener to stop sprinklers when rain starts.
     */
    public void setWeatherSystem(WeatherSystem weatherSystem) {
        this.weatherSystem = weatherSystem;
        logger.info("Watering", "Weather system connected - will skip watering when raining");
        
        // Add listener to stop sprinklers when weather changes to RAINY
        if (weatherSystem != null) {
            weatherSystem.currentWeatherProperty().addListener((obs, oldWeather, newWeather) -> {
                if (newWeather == WeatherSystem.Weather.RAINY && oldWeather != WeatherSystem.Weather.RAINY) {
                    // Rain just started - stop all active sprinklers
                    stopAllSprinklers();
                    logger.info("Watering", "Rain detected - stopped all active sprinklers");
                }
            });
        }
    }
    
    /**
     * Initializes sprinklers and sensors for each zone.
     */
    private void initializeSprinklersAndSensors() {
        for (Zone zone : garden.getZones()) {
            sprinklers.put(zone.getZoneId(), new Sprinkler(zone));
            sensors.put(zone.getZoneId(), new MoistureSensor(zone));
        }
    }
    
    /**
     * Checks moisture levels in all zones and waters if needed.
     * Now checks individual plant water levels for automatic watering.
     * SKIPS watering if it's currently raining.
     */
    public void checkAndWater() {
        // Check if it's raining - don't water if it is
        if (weatherSystem != null && weatherSystem.getCurrentWeather() == WeatherSystem.Weather.RAINY) {
            logger.info("Watering", "Skipping watering - it's currently raining");
            return;
        }
        
        if (waterSupply.get() < 10) {
            logger.warning("Watering", "Water supply critically low: " + waterSupply.get() + "L");
            return;
        }
        
        // Check individual plants that need water
        for (Zone zone : garden.getZones()) {
            MoistureSensor sensor = sensors.get(zone.getZoneId());
            
            if (sensor.getStatus() == Sensor.SensorStatus.ERROR) {
                logger.error("Watering", "Sensor error in Zone " + zone.getZoneId());
                continue;
            }
            
            // Check if any plants in this zone need water
            List<Plant> plantsNeedingWater = zone.getPlantsNeedingWater();
            
            if (!plantsNeedingWater.isEmpty() && zone.getLivingPlantCount() > 0) {
                // Water the zone to hydrate plants
                waterZone(zone.getZoneId(), WATER_PER_CYCLE);
                logger.info("Watering", "Auto-watered Zone " + zone.getZoneId() + 
                           " - " + plantsNeedingWater.size() + " plants needed water");
            }
        }
    }
    
    /**
     * Waters a specific zone with given amount.
     * Checks weather before and during watering - stops if it starts raining.
     */
    public void waterZone(int zoneId, int amount) {
        Sprinkler sprinkler = sprinklers.get(zoneId);
        Zone zone = garden.getZone(zoneId);
        
        if (sprinkler == null || zone == null) {
            logger.error("Watering", "Invalid zone ID: " + zoneId);
            return;
        }
        
        // Check if it's raining before starting
        if (weatherSystem != null && weatherSystem.getCurrentWeather() == WeatherSystem.Weather.RAINY) {
            logger.info("Watering", "Skipping watering Zone " + zoneId + " - it's currently raining");
            return;
        }
        
        if (waterSupply.get() < amount) {
            amount = waterSupply.get();
            logger.warning("Watering", "Limited water available for Zone " + zoneId);
        }
        
        if (amount <= 0) {
            return;
        }
        
        // Activate sprinkler
        sprinkler.activate();
        
        // Check weather again before distributing water (in case it started raining)
        if (weatherSystem != null && weatherSystem.getCurrentWeather() == WeatherSystem.Weather.RAINY) {
            logger.info("Watering", "Stopping watering Zone " + zoneId + " - rain detected");
            sprinkler.deactivate();
            return;
        }
        
        // Distribute water
        int waterUsed = sprinkler.distributeWater(amount);
        
        // Update supply
        waterSupply.set(waterSupply.get() - waterUsed);
        
        // Deactivate sprinkler
        sprinkler.deactivate();
        
        logger.info("Watering", "Zone " + zoneId + " watered with " + waterUsed + 
                   "L. Supply remaining: " + waterSupply.get() + "L");
    }
    
    /**
     * Stops all active sprinklers (called when rain starts).
     */
    public void stopAllSprinklers() {
        for (Sprinkler sprinkler : sprinklers.values()) {
            if (sprinkler != null && sprinkler.isActive()) {
                sprinkler.deactivate();
                logger.info("Watering", "Stopped active sprinkler for Zone " + sprinkler.getZone().getZoneId() + " due to rain");
            }
        }
    }
    
    /**
     * Manually waters a zone (user override).
     */
    public void manualWater(int zoneId) {
        logger.info("Watering", "Manual watering triggered for Zone " + zoneId);
        waterZone(zoneId, WATER_PER_CYCLE);
    }
    
    /**
     * Updates moisture threshold.
     */
    public void setMoistureThreshold(int threshold) {
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be 0-100");
        }
        moistureThreshold.set(threshold);
        logger.info("Watering", "Moisture threshold updated to " + threshold + "%");
    }
    
    /**
     * Refills water supply.
     */
    public void refillWater(int amount) {
        waterSupply.set(waterSupply.get() + amount);
        logger.info("Watering", "Water supply refilled by " + amount + "L. Total: " + 
                   waterSupply.get() + "L");
    }
    
    /**
     * Checks if water is available.
     */
    public boolean isWaterAvailable() {
        return waterSupply.get() > 0;
    }
    
    /**
     * Gets sensor for a zone.
     */
    public MoistureSensor getSensor(int zoneId) {
        return sensors.get(zoneId);
    }
    
    /**
     * Gets sprinkler for a zone.
     */
    public Sprinkler getSprinkler(int zoneId) {
        return sprinklers.get(zoneId);
    }
    
    // Property getters
    public IntegerProperty waterSupplyProperty() {
        return waterSupply;
    }
    
    public IntegerProperty moistureThresholdProperty() {
        return moistureThreshold;
    }
    
    // Value getters
    public int getWaterSupply() {
        return waterSupply.get();
    }
    
    public int getMoistureThreshold() {
        return moistureThreshold.get();
    }
    
    @Override
    public String toString() {
        return "WateringSystem[Zones: " + sprinklers.size() + 
               ", Water: " + waterSupply.get() + "L, Threshold: " + 
               moistureThreshold.get() + "%]";
    }
}

