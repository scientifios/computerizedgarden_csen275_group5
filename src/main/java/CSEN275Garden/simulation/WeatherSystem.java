package CSEN275Garden.simulation;

import CSEN275Garden.model.GardenPlot;
import CSEN275Garden.model.Plant;
import CSEN275Garden.system.CoolingSystem;
import CSEN275Garden.system.HeatingSystem;
import CSEN275Garden.util.Logger;
import javafx.animation.*;
import javafx.beans.property.*;
import javafx.util.Duration;
import java.util.Random;

/**
 * Weather state machine + effects (moisture/temperature).
 * Supports API override and optional real-time rotation mode.
 */
public class WeatherSystem {
    private final GardenPlot garden;
    private final HeatingSystem heatingSystem;
    private final CoolingSystem coolingSystem; // optional (may be null)
    private final ObjectProperty<Weather> currentWeather;
    private Weather previousWeather;
    private int weatherDuration;
    private final Random random;
    private boolean rainTestMode = false; 
    private boolean rotateSunnyRainyMode = false; 
    private boolean apiModeEnabled = false; // disables automatic weather changes
    private Timeline realTimeRotationTimer; // real-time rotation (not sim ticks)
    
    private static final Logger logger = Logger.getInstance();
    private static final int MIN_WEATHER_DURATION = 30; // minutes
    private static final int MAX_WEATHER_DURATION = 120; // minutes
    
    public WeatherSystem(GardenPlot garden, HeatingSystem heatingSystem) {
        this(garden, heatingSystem, null);
    }
    
    public WeatherSystem(GardenPlot garden, HeatingSystem heatingSystem, CoolingSystem coolingSystem) {
        this.garden = garden;
        this.heatingSystem = heatingSystem;
        this.coolingSystem = coolingSystem;
        this.currentWeather = new SimpleObjectProperty<>(Weather.SUNNY);
        this.previousWeather = Weather.SUNNY;
        this.weatherDuration = 60;
        this.random = new Random();
        
        logger.info("Weather", "Weather system initialized. Current: " + Weather.SUNNY);
        
        applyTemperatureForWeather(Weather.SUNNY);
    }
    
    /** Enables API override: disables automatic weather changes (effects still apply). */
    public void setApiModeEnabled(boolean enabled) {
        this.apiModeEnabled = enabled;
        
        // API mode disables real-time rotation.
        if (enabled) {
            if (realTimeRotationTimer != null) {
                realTimeRotationTimer.stop();
                realTimeRotationTimer = null;
            }
            rotateSunnyRainyMode = false;
        } else {
            // Leaving API mode: ensure rotation mode is off.
            if (rotateSunnyRainyMode) {
                disableSunnyRainyRotation();
            }
        }
    }
    
    public boolean isApiModeEnabled() {
        return apiModeEnabled;
    }

    /**
     * Advances one API day for weather state handling.
     * In API mode, weather is daily. Every new day resets to SUNNY baseline.
     */
    public void apiAdvanceDay() {
        if (!apiModeEnabled) {
            return;
        }
        
        // New API day starts from default SUNNY weather unless explicitly set again.
        if (currentWeather.get() != Weather.SUNNY) {
            previousWeather = currentWeather.get();
            currentWeather.set(Weather.SUNNY);
            garden.setWeather(Weather.SUNNY.name());
            logger.info("Weather", "API day advanced. Weather reset to SUNNY");
        }
        
        // Also reset ambient temperature to SUNNY baseline (20C).
        heatingSystem.setAmbientTemperature(20);
        if (coolingSystem != null) {
            coolingSystem.setAmbientTemperature(20);
        }
        heatingSystem.update();
        if (coolingSystem != null) {
            coolingSystem.update();
        }
    }
    
    /** Per-tick update. Auto-change is skipped in API mode and real-time rotation mode. */
    public void update() {
        if (!rotateSunnyRainyMode && !apiModeEnabled) {
            weatherDuration--;
            
            if (weatherDuration <= 0) {
                changeWeather();
            }
        }
        // if(apiModeEnabled){
        //     changeWeather();
        // }
        
        if (currentWeather.get() == Weather.RAINY) {
            applyWeatherEffects();
        } else {
            if (!rotateSunnyRainyMode && weatherDuration % 10 == 0) {
                applyWeatherEffects();
            }
        }
    }
    
    /** Test helper: forces RAINY weather each sim minute. */
    public void enableRainTestMode() {
        rainTestMode = true;
        rotateSunnyRainyMode = false;
        applyTemperatureForWeather(Weather.RAINY);
        previousWeather = currentWeather.get();
        currentWeather.set(Weather.RAINY);
        garden.setWeather(Weather.RAINY.name());
        weatherDuration = 1; // 1 minute duration
        logger.info("Weather", "Rain test mode enabled");
    }
    
    /** Exits rain test mode. */
    public void disableRainTestMode() {
        rainTestMode = false;
        rotateSunnyRainyMode = false;
        logger.info("Weather", "Rain test mode disabled");
    }
    
    /** Real-time rotation: SUNNY �?RAINY �?SNOWY every 60 seconds. */
    public void enableSunnyRainyRotation() {
        rotateSunnyRainyMode = true;
        rainTestMode = false; // Disable rain-only test mode
        applyTemperatureForWeather(Weather.SUNNY);
        previousWeather = currentWeather.get();
        currentWeather.set(Weather.SUNNY);
        garden.setWeather(Weather.SUNNY.name());
        weatherDuration = 1; // Keep for display purposes
        
        if (realTimeRotationTimer != null) {
            realTimeRotationTimer.stop();
        }
        
        realTimeRotationTimer = new Timeline(
            new KeyFrame(Duration.seconds(60), e -> {
                Weather current = currentWeather.get();
                Weather newWeather;
                if (current == Weather.SUNNY) {
                    newWeather = Weather.RAINY;
                } else if (current == Weather.RAINY) {
                    newWeather = Weather.SNOWY;
                } else { // SNOWY
                    newWeather = Weather.SUNNY;
                }
                
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
        
        logger.info("Weather", "Real-time rotation enabled (SUNNY→RAINY→SNOWY)");
    }
    
    /** Disables real-time rotation mode. */
    public void disableSunnyRainyRotation() {
        rotateSunnyRainyMode = false;
        if (realTimeRotationTimer != null) {
            realTimeRotationTimer.stop();
            realTimeRotationTimer = null;
        }
        logger.info("Weather", "Real-time rotation disabled");
    }
    
    // Select next weather based on mode (rotation/test/random)
    private void changeWeather() {
        Weather oldWeather = currentWeather.get();
        Weather newWeather;
        
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
    
    /** Applies ambient temperature for weather unless API override is enabled. */
    private void applyTemperatureForWeather(Weather weather) {
        if (heatingSystem == null) {
            return;
        }
        if (apiModeEnabled) {
            return; // API override: temperature controlled externally
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
            heatingSystem.update();
            if (coolingSystem != null) {
                coolingSystem.update();
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
            heatingSystem.update();
            if (coolingSystem != null) {
                coolingSystem.update();
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
            heatingSystem.update();
            if (coolingSystem != null) {
                coolingSystem.update();
            }
        }
    }
    
    private Weather generateNextWeather(Weather current) {
        double rand = random.nextDouble();
        
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
    }
    
    /** Forces weather immediately (used by UI/API/test paths). */
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

    // // reset machanism in api mode
    // public void restoreWeather() {
    //     if(apiModeEnabled){
    //         currentWeather.set(Weather.SUNNY);
    //         applyTemperatureForWeather(Weather.SUNNY);
    //         garden.setWeather(Weather.SUNNY.name());
    //         logger.info("Weather", "Weather reset to" + Weather.SUNNY + "in API Mode");
    //     }
    // }
    
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
