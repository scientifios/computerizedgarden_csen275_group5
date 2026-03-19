package CSEN275Garden.system;

import CSEN275Garden.model.GardenPlot;
import CSEN275Garden.model.GardenZone;
import CSEN275Garden.simulation.WeatherSystem;
import CSEN275Garden.util.Logger;
import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import CSEN275Garden.model.Plant;

/**
 * Irrigation controller for the garden simulation.
 * Tracks per-zone sprinklers and moisture sensors, and coordinates watering while respecting rain events.
 */
public class WateringSystem {
    private final GardenPlot garden;
    private final Map<Integer, Sprinkler> sprinklers;
    private final Map<Integer, MoistureSensor> sensors;
    private final IntegerProperty waterSupply;
    private final IntegerProperty moistureThreshold;
    private WeatherSystem weatherSystem; // used to skip/stop watering during rain
    
    private static final Logger logger = Logger.getInstance();
    private static final int INITIAL_WATER_SUPPLY = 10000; // L
    private static final int DEFAULT_MOISTURE_THRESHOLD = 40; // %
    private static final int WATER_PER_CYCLE = 10; // L per watering action

    /**
     * Initializes irrigation components (sprinklers + moisture sensors) for each zone.
     */
    public WateringSystem(GardenPlot garden) {
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
     * Connects a WeatherSystem to suppress watering during rain and stop active sprinklers when rain begins.
     */
    public void setWeatherSystem(WeatherSystem weatherSystem) {
        this.weatherSystem = weatherSystem;
        logger.info("Watering", "Weather system connected - will skip watering when raining");
        
        // Stop active sprinklers when rain begins.
        if (weatherSystem != null) {
            weatherSystem.currentWeatherProperty().addListener((obs, oldWeather, newWeather) -> {
                if (newWeather == WeatherSystem.Weather.RAINY && oldWeather != WeatherSystem.Weather.RAINY) {
                    stopAllSprinklers();
                    logger.info("Watering", "Rain detected - stopped all active sprinklers");
                }
            });
        }
    }
    
    /**
     * Creates one sprinkler and one moisture sensor per zone.
     */
    private void initializeSprinklersAndSensors() {
        for (GardenZone zone : garden.getZones()) {
            sprinklers.put(zone.getZoneId(), new Sprinkler(zone));
            sensors.put(zone.getZoneId(), new MoistureSensor(zone));
        }
    }
    
    /**
     * Runs an automatic watering pass for all zones.
     * Skips watering during rain and waters zones that contain plants needing water.
     */
    public void checkAndWater() {
        if (weatherSystem != null && weatherSystem.getCurrentWeather() == WeatherSystem.Weather.RAINY) {
            logger.info("Watering", "Skipping watering - it's currently raining");
            return;
        }
        
        if (waterSupply.get() < 10) {
            logger.warning("Watering", "Water supply critically low: " + waterSupply.get() + "L");
            return;
        }
        
        for (GardenZone zone : garden.getZones()) {
            MoistureSensor sensor = sensors.get(zone.getZoneId());
            
            if (sensor.getStatus() == Sensor.SensorStatus.ERROR) {
                logger.error("Watering", "Sensor error in Zone " + zone.getZoneId());
                continue;
            }
            
            List<Plant> plantsNeedingWater = zone.getPlantsNeedingWater();
            
            if (!plantsNeedingWater.isEmpty() && zone.getLivingPlantCount() > 0) {
                waterZone(zone.getZoneId(), WATER_PER_CYCLE);
                logger.info("Watering", "Auto-watered Zone " + zone.getZoneId() + 
                           " - " + plantsNeedingWater.size() + " plants needed water");
            }
        }
    }
    
    /**
     * Waters a zone using its sprinkler, subject to available supply and rain conditions.
     */
    public void waterZone(int zoneId, int amount) {
        Sprinkler sprinkler = sprinklers.get(zoneId);
        GardenZone zone = garden.getZone(zoneId);
        
        if (sprinkler == null || zone == null) {
            logger.error("Watering", "Invalid zone ID: " + zoneId);
            return;
        }
        
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
        
        sprinkler.activate();
        
        if (weatherSystem != null && weatherSystem.getCurrentWeather() == WeatherSystem.Weather.RAINY) {
            logger.info("Watering", "Stopping watering Zone " + zoneId + " - rain detected");
            sprinkler.deactivate();
            return;
        }
        
        int waterUsed = sprinkler.distributeWater(amount);
        
        waterSupply.set(waterSupply.get() - waterUsed);
        
        sprinkler.deactivate();
        
        logger.info("Watering", "Zone " + zoneId + " watered with " + waterUsed + 
                   "L. Supply remaining: " + waterSupply.get() + "L");
    }
    
    /**
     * Stops any currently active sprinklers.
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
     * Triggers a watering action for the given zone id.
     */
    public void manualWater(int zoneId) {
        logger.info("Watering", "Manual watering triggered for Zone " + zoneId);
        waterZone(zoneId, WATER_PER_CYCLE);
    }
    
    /**
     * Updates the configured moisture threshold (%).
     */
    public void setMoistureThreshold(int threshold) {
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be 0-100");
        }
        moistureThreshold.set(threshold);
        logger.info("Watering", "Moisture threshold updated to " + threshold + "%");
    }
    
    /**
     * Adds water to the system supply.
     */
    public void refillWater(int amount) {
        waterSupply.set(waterSupply.get() + amount);
        logger.info("Watering", "Water supply refilled by " + amount + "L. Total: " + 
                   waterSupply.get() + "L");
    }
    
    /**
     * @return true if the system has remaining water supply
     */
    public boolean isWaterAvailable() {
        return waterSupply.get() > 0;
    }
    
    public MoistureSensor getSensor(int zoneId) {
        return sensors.get(zoneId);
    }
    
    public Sprinkler getSprinkler(int zoneId) {
        return sprinklers.get(zoneId);
    }
    
    public IntegerProperty waterSupplyProperty() {
        return waterSupply;
    }
    
    public IntegerProperty moistureThresholdProperty() {
        return moistureThreshold;
    }
    
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

