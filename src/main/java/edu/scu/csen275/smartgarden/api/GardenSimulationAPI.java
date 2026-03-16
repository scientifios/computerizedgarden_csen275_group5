package edu.scu.csen275.smartgarden.api;

import edu.scu.csen275.smartgarden.controller.GardenController;
import edu.scu.csen275.smartgarden.model.*;
import edu.scu.csen275.smartgarden.simulation.SimulationEngine;
import edu.scu.csen275.smartgarden.simulation.HeadlessSimulationEngine;
import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import edu.scu.csen275.smartgarden.system.CoolingSystem;
import edu.scu.csen275.smartgarden.system.HeatingSystem;
import edu.scu.csen275.smartgarden.system.HarmfulPest;
import edu.scu.csen275.smartgarden.system.PestControlSystem;
import edu.scu.csen275.smartgarden.system.WateringSystem;
import edu.scu.csen275.smartgarden.util.Logger;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Programmatic interface for automated testing and monitoring of the garden simulation.
 *
 * This API provides headless control over garden initialization and environmental events
 * (e.g., rain, temperature changes, pest infestations) while reusing the same underlying
 * domain model and systems as the UI-driven simulation.
 *
 * Methods are designed to be script-friendly and to produce traceable logs for grading
 * and long-running stability tests.
 */
public class GardenSimulationAPI {
    private final GardenController controller;
    private final Garden garden;
    private final SimulationEngine engine;
    private final HeadlessSimulationEngine headlessEngine;
    private final Logger logger;
    
    private int dayCount = 0;
    
    private static final Set<GardenSimulationAPI> activeInstances = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdownHookRegistered = false;
    private static final Object shutdownLock = new Object();
    private static Map<String, List<String>> pestVulnerabilities = new HashMap<>();
    
    static {
        loadPestVulnerabilitiesFromConfig();
    }
    
    /**
     * Loads pest vulnerabilities from parasites.json config file.
     * Creates a reverse mapping: plant name -> list of pests that can attack it.
     */
private static void loadPestVulnerabilitiesFromConfig() {
    Logger logger = Logger.getInstance();
    try {
        InputStream configStream = GardenSimulationAPI.class.getResourceAsStream("/parasites.json");
        if (configStream == null) {
            logger.warning("API", "parasites.json not found, using default vulnerabilities");
            loadDefaultPestVulnerabilities();
            return;
        }

        String configContent = new String(configStream.readAllBytes());
        configStream.close();

        Pattern parasitePattern = Pattern.compile(
            "\"name\"\\s*:\\s*\"([^\"]+)\".*?\"targetPlants\"\\s*:\\s*\\[([^\\]]+)\\]",
            Pattern.DOTALL
        );

        Matcher matcher = parasitePattern.matcher(configContent);

        while (matcher.find()) {
            String parasiteName = matcher.group(1);
            String targetPlantsStr = matcher.group(2);

            Pattern plantPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher plantMatcher = plantPattern.matcher(targetPlantsStr);

            while (plantMatcher.find()) {
                String plantName = plantMatcher.group(1);
                pestVulnerabilities
                    .computeIfAbsent(plantName, k -> new ArrayList<>())
                    .add(parasiteName);
            }
        }

        logger.info("API", "Loaded pest vulnerabilities from parasites.json (" +
                pestVulnerabilities.size() + " plants configured)");

    } catch (Exception e) {
        logger.error("API", "Failed to load parasites.json: " + e.getMessage());
        loadDefaultPestVulnerabilities();
    }
}
    
    /**
     * Fallback method to load default pest vulnerabilities if config file fails.
     */
    private static void loadDefaultPestVulnerabilities() {
        pestVulnerabilities.put("Strawberry", Arrays.asList("Red Mite", "Green Leaf Worm"));
        pestVulnerabilities.put("Grapevine", Arrays.asList("Black Beetle", "Red Mite"));
        pestVulnerabilities.put("Apple Sapling", Arrays.asList("Brown Caterpillar", "Green Leaf Worm"));
        pestVulnerabilities.put("Carrot", Arrays.asList("Red Mite", "Brown Caterpillar"));
        pestVulnerabilities.put("Tomato", Arrays.asList("Black Beetle", "Red Mite"));
        pestVulnerabilities.put("Onion", Arrays.asList("Green Leaf Worm"));
        pestVulnerabilities.put("Sunflower", Arrays.asList("Red Mite", "Brown Caterpillar"));
        pestVulnerabilities.put("Tulip", Arrays.asList("Green Leaf Worm"));
        pestVulnerabilities.put("Rose", Arrays.asList("Black Beetle", "Red Mite"));
    }
    
    /**
     * Creates a new GardenSimulationAPI with default settings.
     * This constructor creates its own GardenController internally.
     * Use this constructor for simple API usage as shown in the specification.
     */
    public GardenSimulationAPI() {
        // Create default GardenController (9x9 grid)
        this(new GardenController(9, 9));
    }
    
    /**
     * Creates a new GardenSimulationAPI with a GardenController.
     * The API will use this controller to interact with the garden systems.
     * 
     * @param controller The GardenController instance to use
     */
    public GardenSimulationAPI(GardenController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("GardenController cannot be null");
        }
        this.controller = controller;
        this.garden = controller.getGarden();
        this.engine = controller.getSimulationEngine();
        this.logger = Logger.getInstance();
        this.headlessEngine = new HeadlessSimulationEngine(
            garden,
            engine.getWateringSystem(),
            engine.getHeatingSystem(),
            engine.getCoolingSystem(),
            engine.getPestControlSystem(),
            engine.getWeatherSystem()
        );
        
        // Register this instance for automatic cleanup
        activeInstances.add(this);
        
        logger.info("API", "GardenSimulationAPI initialized with headless simulation engine");
    }
    
    /**
    * Initializes the garden state and enables API-driven simulation mode.
    * Loads initial plants from a configuration resource (if available),
    * enables API mode for relevant subsystems, and starts the headless
    * simulation loop.
    */
    public void initializeGarden() {
        Logger.enableApiLogging(Paths.get("log.txt"));
        
        registerShutdownHook();
        
        logger.info("API", "Initializing garden - Day 0 begins");
        dayCount = 0;
        
        try {
            InputStream configStream = getClass().getResourceAsStream("/garden-config.json");
            if (configStream == null) {
                logger.warning("API", "Config file not found, using default plants");
                // Fallback to default plants if config not found
                addPlants(PlantType.STRAWBERRY, new Position(1, 1));
                addPlants(PlantType.CARROT, new Position(2, 2));
                addPlants(PlantType.TOMATO, new Position(3, 3));
                addPlants(PlantType.SUNFLOWER, new Position(4, 4));
            } else {
                String configContent = new String(configStream.readAllBytes());
                configStream.close();
                loadPlantsFromConfig(configContent);
            }
        } catch (Exception e) {
            logger.error("API", "Error loading config file: " + e.getMessage());
            // Fallback to default plants
            addPlants(PlantType.STRAWBERRY, new Position(1, 1));
            addPlants(PlantType.CARROT, new Position(2, 2));
            addPlants(PlantType.TOMATO, new Position(3, 3));
            addPlants(PlantType.SUNFLOWER, new Position(4, 4));
        }
        
        logger.info("API", "Garden initialized with " + garden.getTotalPlants() + " plants.");
        engine.getPestControlSystem().setApiModeEnabled(true);
        engine.getWeatherSystem().setApiModeEnabled(true);
        engine.getHeatingSystem().setApiModeEnabled(true);
        engine.getCoolingSystem().setApiModeEnabled(true);
        
        startHeadlessSimulation();
    }
    
    /**
     * Helper method to add a plant to the garden.
     */
    private void addPlants(PlantType plantType, Position position) {
        if (controller.plantSeed(plantType, position)) {
            logger.info("API", "Added plant: " + plantType.getDisplayName() + " at " + position);
        } else {
            logger.warning("API", "Failed to add plant: " + plantType.getDisplayName() + " at " + position);
        }
    }
    
    /**
    * Returns plant metadata for external scripts.
     *
    * @return a map containing:
    *         "plants"           -> List<String> plant display types,
    *         "waterRequirement" -> List<Integer> required water amounts,
    *         "parasites"        -> List<List<String>> pests that can target each plant
    */
    public Map<String, Object> getPlants() {
        Map<String, Object> plantInfo = new HashMap<>();
        List<String> plantNames = new ArrayList<>();
        List<Integer> waterRequirements = new ArrayList<>();
        List<List<String>> parasiteList = new ArrayList<>();
        // change from get all plants to get all living plants
        for (Plant plant : garden.getLivingPlants()) {
            String plantType = plant.getPlantType();
            plantNames.add(plantType);
            waterRequirements.add(plant.getWaterRequirement());
            
            String lookupKey = plantType;
            if (plantType.startsWith("Flower (")) {
                lookupKey = plantType.substring(8, plantType.length() - 1); // Extract "Sunflower" from "Flower (Sunflower)"
            }
            parasiteList.add(pestVulnerabilities.getOrDefault(lookupKey, new ArrayList<>()));
        }
        
        plantInfo.put("plants", plantNames);
        plantInfo.put("waterRequirement", waterRequirements);
        plantInfo.put("parasites", parasiteList);
        
        logger.info("API", "Retrieved plant information for " + plantNames.size() + " plants.");
        return plantInfo;
    }
    
    /**
    * Simulates a rainfall event.
     *
    * @param amount water units added to each living plant
    */
    public void rain(int amount) {
        logger.info("API", "Rainfall event: " + amount + " units");
        
        WeatherSystem weatherSystem = engine.getWeatherSystem();
        weatherSystem.setWeather(WeatherSystem.Weather.RAINY);
        
        for (Plant plant : garden.getAllPlants()) {
            if (!plant.isDead()) {
                plant.water(amount);
                plant.applyWeatherEffect("RAINY");
                logger.info("API", "Rain added water to " + plant.getPlantType() + 
                           " at " + plant.getPosition() + ". Current water level: " + plant.getWaterLevel());
            }
        }
        
        triggerSystemUpdates();
        
        dayCount++;
    }
    
    /**
    * Simulates an ambient temperature change.
    *
    * @param temp temperature in Fahrenheit (clamped to the valid range if necessary)
    *             Weather is not modified by this method in API mode.
    */
    public void temperature(int temp) {
        if (temp < 40 || temp > 120) {
            logger.warning("API", "Temperature " + temp + "°F is outside valid range (40-120°F). Clamping to valid range.");
            temp = Math.max(40, Math.min(120, temp));
        }
        
        double tempCelsius = (temp - 32) * 5.0 / 9.0;
        int tempCelsiusInt = (int) Math.round(tempCelsius);
        
        logger.info("API", "Temperature changed to " + temp + "°F (" + tempCelsiusInt + "°C)");
        
        HeatingSystem heatingSystem = engine.getHeatingSystem();
        CoolingSystem coolingSystem = engine.getCoolingSystem();
        
        heatingSystem.setAmbientTemperature(tempCelsiusInt);
        coolingSystem.setAmbientTemperature(tempCelsiusInt);
        
        // In API mode, weather changes must be explicit API calls, not automatic based on temperature
        for (Plant plant : garden.getAllPlants()) {
            if (!plant.isDead()) {
                plant.applyTemperatureEffect(tempCelsiusInt);
                logger.info("API", plant.getPlantType() + " temperature adjusted to " + temp + "°F (" + tempCelsiusInt + "°C)");
            }
        }
        
        triggerSystemUpdates();
        
        dayCount++;
    }
    
    /**
    * Simulates a pest infestation event.
     *
    * @param parasiteType pest type name (case-insensitive)
    */
    public void parasite(String parasiteType) {
        logger.info("API", "Parasite infestation: " + parasiteType);
        
        String normalizedParasiteType = parasiteType.toLowerCase();
        
        for (Plant plant : garden.getAllPlants()) {
            if (plant.isDead()) {
                continue;
            }
            
            String plantType = plant.getPlantType();
            // Handle Flower type format "Flower (Sunflower)" -> extract "Sunflower"
            String lookupKey = plantType;
            if (plantType.startsWith("Flower (")) {
                lookupKey = plantType.substring(8, plantType.length() - 1); // Extract "Sunflower" from "Flower (Sunflower)"
            }
            List<String> vulnerabilities = pestVulnerabilities.getOrDefault(lookupKey, new ArrayList<>());
            
            String matchedParasiteName = null;
            for (String vulnerability : vulnerabilities) {
                if (vulnerability.toLowerCase().equals(normalizedParasiteType)) {
                    matchedParasiteName = vulnerability; // Use original name from config
                    break;
                }
            }
            
            if (matchedParasiteName != null) {
                // Use the original name from config to ensure consistency
                HarmfulPest pest = new HarmfulPest(matchedParasiteName, plant.getPosition());
                pest.causeDamage(plant);
                
                // Register pest with PestControlSystem so it gets automatically treated
                PestControlSystem pestSystem = engine.getPestControlSystem();
                pestSystem.registerPest(pest);
                
                logger.info("API", plantType + " at " + plant.getPosition() + 
                           " attacked by " + matchedParasiteName);
            }
        }
        
        triggerSystemUpdates();
        
        dayCount++;
    }
    
    /**
    * Logs a snapshot report of the current garden state.
    */
    public void getState() {
        logger.info("API", "Garden State Report - Day " + dayCount);
        
        Map<String, Integer> stats = garden.getStatistics();
        int alive = stats.getOrDefault("livingPlants", 0);
        int dead = stats.getOrDefault("deadPlants", 0);
        
        logger.info("API", "Alive: " + alive + ", Dead: " + dead);
        logger.info("API", "Total Plants: " + stats.getOrDefault("totalPlants", 0));
        logger.info("API", "Zones: " + stats.getOrDefault("zones", 0));
        
        for (Plant plant : garden.getAllPlants()) {
            String status = plant.isDead() ? "DEAD" : "ALIVE";
            String plantStatus = "  - " + plant.getPlantType() + " at " + plant.getPosition() + 
                       ": " + status + " (Health: " + plant.getHealthLevel() + "%, Water: " + 
                       plant.getWaterLevel() + "%)";
            logger.info("API", plantStatus);
        }
    }
    
    /**
     * Gets the GardenController instance (for advanced use).
     */
    public GardenController getController() {
        return controller;
    }
    
    /**
     * Gets the Garden instance (for advanced use).
     */
    public Garden getGarden() {
        return garden;
    }
    
    /**
     * Gets the current day count (from API calls).
     */
    public int getDayCount() {
        return dayCount;
    }
    
    /**
    * Starts the headless simulation loop if not already running.
    */
    public void startHeadlessSimulation() {
        if (headlessEngine.isRunning()) {
            logger.warning("API", "Headless simulation already running");
            return;
        }
        
        if (garden.getLivingPlants().isEmpty()) {
            logger.warning("API", "Cannot start headless simulation - no plants in garden");
            return;
        }
        
        headlessEngine.start();
    }
    
    /**
    * Registers a JVM shutdown hook to stop active headless simulations and close API logging.
    */
    private static void registerShutdownHook() {
        synchronized (shutdownLock) {
            if (shutdownHookRegistered) {
                return;
            }
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Stop all headless simulations
                for (GardenSimulationAPI api : activeInstances) {
                    if (api.headlessEngine.isRunning()) {
                        api.headlessEngine.stop();
                    }
                }
                
                // Close API log
                try {
                    Logger.disableApiLogging();
                } catch (Exception e) {
                    // Ignore - JVM is shutting down anyway
                }
            }, "GardenSimulationAPI-ShutdownHook"));
            
            shutdownHookRegistered = true;
        }
    }
    
    /**
     * Stops the headless simulation loop.
     * Useful for cleanup or when switching to UI mode.
     * Note: This is optional - shutdown hook will handle cleanup automatically when JVM exits.
     */
    public void stopHeadlessSimulation() {
        if (!headlessEngine.isRunning()) {
            return;
        }
        
        headlessEngine.stop();
        logger.info("API", "Headless simulation loop stopped");
    }
    
    /**
     * Checks if headless simulation is currently running.
     */
    public boolean isHeadlessSimulationRunning() {
        return headlessEngine.isRunning();
    }
    
    /**
     * Gets the headless simulation engine's day counter.
     * This tracks days from continuous simulation (different from API dayCount).
     */
    public int getHeadlessDayCount() {
        return headlessEngine.getDayCounter();
    }
    
    /**
    * Invokes all automatic subsystems after an API-triggered state change.
    */
    private void triggerSystemUpdates() {
        HeatingSystem heatingSystem = engine.getHeatingSystem();
        CoolingSystem coolingSystem = engine.getCoolingSystem();
        WateringSystem wateringSystem = engine.getWateringSystem();
        PestControlSystem pestSystem = engine.getPestControlSystem();
        
        heatingSystem.update();
        coolingSystem.update();
        wateringSystem.checkAndWater();
        pestSystem.update();
    }
    
    /**
     * Closes the API log file writer. Should be called when done using the API.
     * Note: This is optional - shutdown hook will handle cleanup automatically when JVM exits.
     */
    public static void closeApiLog() {
        Logger.disableApiLogging();
    }
    
    private void loadPlantsFromConfig(String configContent) {
        // Regex-based loader for the expected garden-config.json structure.
        Pattern plantPattern = Pattern.compile(
            "\"type\"\\s*:\\s*\"([^\"]+)\".*?\"row\"\\s*:\\s*(\\d+).*?\"column\"\\s*:\\s*(\\d+)",
            Pattern.DOTALL
        );
        
        Matcher matcher = plantPattern.matcher(configContent);
        int plantCount = 0;
        
        while (matcher.find()) {
            String plantTypeName = matcher.group(1);
            int row = Integer.parseInt(matcher.group(2));
            int column = Integer.parseInt(matcher.group(3));
            
            // Map plant type name to PlantType enum
            PlantType plantType = mapPlantTypeName(plantTypeName);
            if (plantType != null) {
                addPlants(plantType, new Position(row, column));
                plantCount++;
            } else {
                logger.warning("API", "Unknown plant type in config: " + plantTypeName);
            }
        }
        
        if (plantCount == 0) {
            logger.warning("API", "No plants loaded from config, using defaults");
            addPlants(PlantType.STRAWBERRY, new Position(1, 1));
            addPlants(PlantType.CARROT, new Position(2, 2));
            addPlants(PlantType.TOMATO, new Position(3, 3));
            addPlants(PlantType.SUNFLOWER, new Position(4, 4));
        }
    }
    
    /**
     * Maps plant type name from config to PlantType enum.
     * 
     * @param plantTypeName The plant type name from config
     * @return The corresponding PlantType enum, or null if not found
     */
    private PlantType mapPlantTypeName(String plantTypeName) {
        return switch (plantTypeName) {
            case "Strawberry" -> PlantType.STRAWBERRY;
            case "Grapevine" -> PlantType.GRAPEVINE;
            case "Apple Sapling", "Apple" -> PlantType.APPLE;
            case "Carrot" -> PlantType.CARROT;
            case "Tomato" -> PlantType.TOMATO;
            case "Onion" -> PlantType.ONION;
            case "Sunflower" -> PlantType.SUNFLOWER;
            case "Tulip" -> PlantType.TULIP;
            case "Rose" -> PlantType.ROSE;
            default -> null;
        };
    }
}
