package edu.scu.csen275.smartgarden.simulation;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.system.CoolingSystem;
import edu.scu.csen275.smartgarden.system.HeatingSystem;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.util.Duration;
import java.util.Random;

/**
 * Simulates dynamic weather conditions that affect plant growth.
 */
public class WeatherSystem {
    private final Garden garden;
    private final HeatingSystem heatingSystem;
    private final CoolingSystem coolingSystem; // Optional - can be null
    private final ObjectProperty<Weather> currentWeather;
    private Weather previousWeather; // Track previous weather to detect changes
    private int weatherDuration; // minutes
    private final Random random;
    private boolean rainTestMode = false; // TEST MODE: Force rain every minute
    private boolean rotateSunnyRainyMode = false; // Rotate between sunny and rainy every minute
    private boolean apiModeEnabled = false; // When enabled, automatic weather changes are disabled
    private Timeline realTimeRotationTimer; // Timer for real-world time rotation
    
    private static final Logger logger = Logger.getInstance();
    private static final int MIN_WEATHER_DURATION = 30; // minutes
    private static final int MAX_WEATHER_DURATION = 120; // minutes
    
    /**
     * Creates a new WeatherSystem for the garden.
     */
    public WeatherSystem(Garden garden, HeatingSystem heatingSystem) {
        this(garden, heatingSystem, null);
    }
    
    /**
     * Creates a new WeatherSystem for the garden with cooling system support.
     */
    public WeatherSystem(Garden garden, HeatingSystem heatingSystem, CoolingSystem coolingSystem) {
        this.garden = garden;
        this.heatingSystem = heatingSystem;
        this.coolingSystem = coolingSystem;
        this.currentWeather = new SimpleObjectProperty<>(Weather.SUNNY);
        this.previousWeather = Weather.SUNNY; // Initialize previous weather
        this.weatherDuration = 60;
        this.random = new Random();
        
        logger.info("Weather", "Weather system initialized. Current: " + Weather.SUNNY);
        
        // Set initial temperature based on initial weather
        applyTemperatureForWeather(Weather.SUNNY);
    }
    
    /**
     * Sets whether API mode is enabled.
     * When enabled, automatic weather changes are disabled (weather only via API calls).
     * Weather effects (like rain watering plants) still apply normally.
     */
    public void setApiModeEnabled(boolean enabled) {
        this.apiModeEnabled = enabled;
        
        // When enabling API mode, stop rotation timer (if running)
        // This prevents automatic weather changes from rotation timer
        if (enabled) {
            if (realTimeRotationTimer != null) {
                realTimeRotationTimer.stop();
                realTimeRotationTimer = null;
            }
            rotateSunnyRainyMode = false; // Also disable rotation flag
        } else {
            // If disabling API mode and rotation was enabled, disable rotation
            if (rotateSunnyRainyMode) {
                disableSunnyRainyRotation();
            }
        }
    }
    
    /**
     * Checks if API mode is enabled.
     */
    public boolean isApiModeEnabled() {
        return apiModeEnabled;
    }
    
    /**
     * Updates weather system each simulation tick.
     * Note: In rotation mode, weather changes are handled by real-time timer, not simulation ticks.
     */
    public void update() {
        // Skip weather duration countdown in rotation mode OR API mode
        // In API mode, weather changes only happen via API calls (rain(), temperature())
        if (!rotateSunnyRainyMode && !apiModeEnabled) {
            weatherDuration--;
            
            if (weatherDuration <= 0) {
                changeWeather();
            }
        }
        
        // Apply weather effects - rain should water plants continuously
        if (currentWeather.get() == Weather.RAINY) {
            // When raining, water plants every tick (every minute)
            applyWeatherEffects();
        } else {
            // For other weather, apply effects periodically (every 10 minutes)
            if (!rotateSunnyRainyMode && weatherDuration % 10 == 0) {
                applyWeatherEffects();
            }
        }
    }
    
    /**
     * TEST MODE: Forces rain every 1 minute for testing purposes.
     * Call this method to enable test mode.
     */
    public void enableRainTestMode() {
        rainTestMode = true;
        rotateSunnyRainyMode = false; // Disable rotation mode
        // Force rain immediately
        applyTemperatureForWeather(Weather.RAINY);
        previousWeather = currentWeather.get();
        currentWeather.set(Weather.RAINY);
        garden.setWeather(Weather.RAINY.name());
        weatherDuration = 1; // 1 minute duration
        logger.info("Weather", "TEST MODE ENABLED: Rain will occur every 1 minute for testing");
    }
    
    /**
     * Disables rain test mode and returns to normal weather behavior.
     */
    public void disableRainTestMode() {
        rainTestMode = false;
        rotateSunnyRainyMode = false;
        logger.info("Weather", "TEST MODE DISABLED: Returning to normal weather behavior");
    }
    
    /**
     * Enables rotation between SUNNY, RAINY, and SNOWY every 1 REAL minute (60 seconds).
     */
    public void enableSunnyRainyRotation() {
        rotateSunnyRainyMode = true;
        rainTestMode = false; // Disable rain-only test mode
        // Start with sunny
        applyTemperatureForWeather(Weather.SUNNY);
        previousWeather = currentWeather.get();
        currentWeather.set(Weather.SUNNY);
        garden.setWeather(Weather.SUNNY.name());
        weatherDuration = 1; // Keep for display purposes
        
        // Stop any existing timer
        if (realTimeRotationTimer != null) {
            realTimeRotationTimer.stop();
        }
        
        // Create real-time timer that rotates weather every 60 seconds (1 actual minute)
        realTimeRotationTimer = new Timeline(
            new KeyFrame(Duration.seconds(60), e -> {
                // Rotate weather every 60 seconds: SUNNY → RAINY → SNOWY → SUNNY
                Weather current = currentWeather.get();
                Weather newWeather;
                if (current == Weather.SUNNY) {
                    newWeather = Weather.RAINY;
                } else if (current == Weather.RAINY) {
                    newWeather = Weather.SNOWY;
                } else { // SNOWY
                    newWeather = Weather.SUNNY;
                }
                
                // Set temperature for the new weather
                applyTemperatureForWeather(newWeather);
                
                previousWeather = current;
                currentWeather.set(newWeather);
                garden.setWeather(newWeather.name());
                logger.info("Weather", "REAL-TIME ROTATION: Weather changed from " + current + " to " + 
                           newWeather + " (after 1 actual minute)");
            })
        );
        realTimeRotationTimer.setCycleCount(Timeline.INDEFINITE);
        realTimeRotationTimer.play();
        
        logger.info("Weather", "REAL-TIME ROTATION MODE ENABLED: Weather will rotate between SUNNY → RAINY → SNOWY every 1 actual minute (60 seconds)");
    }
    
    /**
     * Disables sunny/rainy rotation mode.
     */
    public void disableSunnyRainyRotation() {
        rotateSunnyRainyMode = false;
        if (realTimeRotationTimer != null) {
            realTimeRotationTimer.stop();
            realTimeRotationTimer = null;
        }
        logger.info("Weather", "ROTATION MODE DISABLED: Returning to normal weather behavior");
    }
    
    /**
     * Changes to a new weather condition.
     */
    private void changeWeather() {
        Weather oldWeather = currentWeather.get();
        Weather newWeather;
        
        // ROTATION MODE: Rotate between SUNNY, RAINY, and SNOWY
        if (rotateSunnyRainyMode) {
            Weather current = oldWeather;
            if (current == Weather.SUNNY) {
                newWeather = Weather.RAINY;
            } else if (current == Weather.RAINY) {
                newWeather = Weather.SNOWY;
            } else { // SNOWY
                newWeather = Weather.SUNNY;
            }
            weatherDuration = 1; // 1 minute
            logger.info("Weather", "ROTATION MODE: Weather changed from " + oldWeather + " to " + 
                       newWeather + " (Duration: 1 min)");
        }
        // TEST MODE: Force rain every minute
        else if (rainTestMode) {
            newWeather = Weather.RAINY;
            weatherDuration = 1; // 1 minute
            logger.info("Weather", "TEST MODE: Weather forced to RAINY (Duration: 1 min)");
        } else {
            newWeather = generateNextWeather(oldWeather);
            weatherDuration = MIN_WEATHER_DURATION + 
                             random.nextInt(MAX_WEATHER_DURATION - MIN_WEATHER_DURATION);
            logger.info("Weather", "Weather changed from " + oldWeather + " to " + 
                       newWeather + " (Duration: " + weatherDuration + " min)");
        }
        
        // Set temperature immediately when weather changes
        if (oldWeather != newWeather) {
            applyTemperatureForWeather(newWeather);
        }
        
        previousWeather = oldWeather; // Update previous weather
        currentWeather.set(newWeather);
        garden.setWeather(newWeather.name());
    }
    
    /**
     * Sets temperature based on weather type and activates/deactivates heating accordingly.
     */
    private void applyTemperatureForWeather(Weather weather) {
        if (heatingSystem == null) {
            return;
        }
        
        // In API mode, don't automatically change temperature with weather
        // Temperature should only be controlled via api.temperature() calls
        if (apiModeEnabled) {
            return; // Skip temperature changes in API mode
        }
        
        int targetTemp;
        if (weather == Weather.SUNNY) {
            targetTemp = 20;
            heatingSystem.setAmbientTemperature(targetTemp);
            if (coolingSystem != null) {
                coolingSystem.setAmbientTemperature(targetTemp);
            }
            if (!apiModeEnabled) {
                logger.info("Weather", "SUNNY weather: Temperature set to " + targetTemp + "°C");
            }
            // Turn off heating when sunny (temp >= 17°C threshold)
            heatingSystem.update(); // This will deactivate heating
            if (coolingSystem != null) {
                coolingSystem.update(); // Check if cooling needed
            }
        } else if (weather == Weather.RAINY) {
            targetTemp = 10;
            heatingSystem.setAmbientTemperature(targetTemp);
            if (coolingSystem != null) {
                coolingSystem.setAmbientTemperature(targetTemp);
            }
            if (!apiModeEnabled) {
                logger.info("Weather", "RAINY weather: Temperature set to " + targetTemp + "°C");
            }
            // Heating will activate (temp < 15°C, deficit = 5 = LOW mode)
            heatingSystem.update();
            if (coolingSystem != null) {
                coolingSystem.update(); // Cooling should be off at 10°C
            }
        } else if (weather == Weather.SNOWY) {
            targetTemp = 5;
            heatingSystem.setAmbientTemperature(targetTemp);
            if (coolingSystem != null) {
                coolingSystem.setAmbientTemperature(targetTemp);
            }
            if (!apiModeEnabled) {
                logger.info("Weather", "SNOWY weather: Temperature set to " + targetTemp + "°C");
            }
            // Heating will activate (temp < 15°C, deficit > 10 = HIGH mode)
            heatingSystem.update();
            if (coolingSystem != null) {
                coolingSystem.update(); // Cooling should be off at 5°C
            }
        }
    }
    
    /**
     * Generates the next weather based on current conditions.
     */
    private Weather generateNextWeather(Weather current) {
        double rand = random.nextDouble();
        
        // Weather transitions with realistic probabilities
        return switch (current) {
            case SUNNY -> {
                if (rand < 0.6) yield Weather.SUNNY;      // Stay sunny
                else if (rand < 0.8) yield Weather.CLOUDY; // Clouds forming
                else if (rand < 0.95) yield Weather.WINDY; // Wind
                else yield Weather.RAINY;                  // Sudden rain
            }
            case CLOUDY -> {
                if (rand < 0.4) yield Weather.CLOUDY;     // Stay cloudy
                else if (rand < 0.6) yield Weather.RAINY; // Rain likely
                else if (rand < 0.85) yield Weather.SUNNY; // Clear up
                else yield Weather.WINDY;                  // Windy
            }
            case RAINY -> {
                if (rand < 0.5) yield Weather.RAINY;      // Continue raining
                else if (rand < 0.8) yield Weather.CLOUDY; // Stop raining
                else yield Weather.SUNNY;                  // Clear after rain
            }
            case WINDY -> {
                if (rand < 0.5) yield Weather.SUNNY;      // Calm down
                else if (rand < 0.8) yield Weather.CLOUDY; // Clouds
                else yield Weather.WINDY;                  // Stay windy
            }
            case SNOWY -> {
                if (rand < 0.6) yield Weather.CLOUDY;     // Warm up
                else if (rand < 0.9) yield Weather.SNOWY; // Keep snowing
                else yield Weather.SUNNY;                  // Clear and cold
            }
        };
    }
    
    /**
     * Applies current weather effects to all plants.
     */
    private void applyWeatherEffects() {
        for (Plant plant : garden.getLivingPlants()) {
            plant.applyWeatherEffect(currentWeather.get().name());
        }
        
        // Weather affects zone moisture
        if (currentWeather.get() == Weather.RAINY) {
            garden.getZones().forEach(zone -> zone.updateMoisture(5));
        } else if (currentWeather.get() == Weather.SUNNY) {
            garden.getZones().forEach(zone -> zone.evaporate(2));
        }
        
        // Temperature is set immediately on weather change in changeWeather()
        // Heating system will gradually increase temperature from the set point to 15°C
        // No continuous temperature changes here - only moisture effects
    }
    
    /**
     * Manually sets weather (for testing).
     */
    public void setWeather(Weather weather) {
        if (currentWeather.get() != weather) {
            applyTemperatureForWeather(weather);
            previousWeather = currentWeather.get();
        }
        currentWeather.set(weather);
        garden.setWeather(weather.name());
        weatherDuration = 60;
        logger.info("Weather", "Weather manually set to " + weather);
    }
    
    /**
     * Gets weather forecast (next expected weather).
     */
    public Weather getForecast() {
        return generateNextWeather(currentWeather.get());
    }
    
    // Property getter
    public ObjectProperty<Weather> currentWeatherProperty() {
        return currentWeather;
    }
    
    // Value getter
    public Weather getCurrentWeather() {
        return currentWeather.get();
    }
    
    public int getWeatherDuration() {
        return weatherDuration;
    }
    
    /**
     * Weather enumeration.
     */
    public enum Weather {
        SUNNY("☀", "Sunny"),
        CLOUDY("☁", "Cloudy"),
        RAINY("🌧", "Rainy"),
        WINDY("💨", "Windy"),
        SNOWY("❄", "Snowy");
        
        private final String icon;
        private final String displayName;
        
        Weather(String icon, String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Override
    public String toString() {
        return "WeatherSystem[Current: " + currentWeather.get() + 
               ", Duration left: " + weatherDuration + " min]";
    }
}

