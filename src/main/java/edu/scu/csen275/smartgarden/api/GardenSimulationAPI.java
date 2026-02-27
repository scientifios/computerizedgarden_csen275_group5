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
 * GardenSimulationAPI provides an interface for automated testing and monitoring 
 * of the smart garden simulation system.
 * It exposes methods to initialize the garden, retrieve plant information, 
 * and simulate environmental conditions.
 * 
 * This API wraps smartGarden's existing systems (GardenController, WateringSystem,
 * HeatingSystem, PestControlSystem) to provide a simple programmatic interface
 * similar to Group9's API, but integrated with smartGarden's architecture.
 */
public class GardenSimulationAPI {
    private final GardenController controller;
    private final Garden garden;
    private final SimulationEngine engine;
    private final HeadlessSimulationEngine headlessEngine;
    private final Logger logger;
    
    // Track day count for API compatibility (from API calls)
    private int dayCount = 0;
    
    // Track active API instances for automatic cleanup
    private static final Set<GardenSimulationAPI> activeInstances = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdownHookRegistered = false;
    private static final Object shutdownLock = new Object();
    
    // Pest vulnerabilities mapping - loaded from parasites.json config file
    // Maps plant name -> list of pest names that can attack it
    private static Map<String, List<String>> pestVulnerabilities = new HashMap<>();
    
    static {
        // Load pest vulnerabilities from parasites.json config file
        loadPestVulnerabilitiesFromConfig();
    }
    
    /**
     * Loads pest vulnerabilities from parasites.json config file.
     * Creates a reverse mapping: plant name -> list of pests that can attack it.
     */
    private static void loadPestVulnerabilitiesFromConfig() {
        try {
            InputStream configStream = GardenSimulationAPI.class.getResourceAsStream("/parasites.json");
            if (configStream == null) {
                System.err.println("[GardenSimulationAPI] WARNING: parasites.json not found, using default vulnerabilities");
                // Fallback to hardcoded values if config not found
                loadDefaultPestVulnerabilities();
                return;
            }
            
            String configContent = new String(configStream.readAllBytes());
            configStream.close();
            
            // Parse parasites.json: extract "name" and "targetPlants" for each parasite
            // Pattern: "name": "PestName", ... "targetPlants": ["Plant1", "Plant2", ...]
            Pattern parasitePattern = Pattern.compile(
                "\"name\"\\s*:\\s*\"([^\"]+)\".*?\"targetPlants\"\\s*:\\s*\\[([^\\]]+)\\]",
                Pattern.DOTALL
            );
            
            Matcher matcher = parasitePattern.matcher(configContent);
            
            while (matcher.find()) {
                String parasiteName = matcher.group(1);
                String targetPlantsStr = matcher.group(2);
                
                // Extract all plant names from the array
                Pattern plantPattern = Pattern.compile("\"([^\"]+)\"");
                Matcher plantMatcher = plantPattern.matcher(targetPlantsStr);
                
                while (plantMatcher.find()) {
                    String plantName = plantMatcher.group(1);
                    // Add reverse mapping: plant -> list of pests
                    pestVulnerabilities.computeIfAbsent(plantName, k -> new ArrayList<>()).add(parasiteName);
                }
            }
            
            System.out.println("[GardenSimulationAPI] Loaded pest vulnerabilities from parasites.json: " + 
                             pestVulnerabilities.size() + " plants configured");
        } catch (Exception e) {
            System.err.println("[GardenSimulationAPI] ERROR loading parasites.json: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default values
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
        
        // Create headless engine that shares the same systems as SimulationEngine
        // This allows both UI and API to work with the same garden state
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
     * Initializes the garden with a predefined set of plants from config file.
     * This method marks the beginning of the simulation clock.
     * Reads plant configuration from garden-config.json in resources.
     */
    public void initializeGarden() {
        // Enable dual logging - all logs will go to both logs/garden_*.log AND log.txt
        Logger.enableApiLogging(Paths.get("log.txt"));
        
        // Register shutdown hook for automatic cleanup (only once)
        registerShutdownHook();
        
        logger.info("API", "Initializing garden - Day 0 begins");
        dayCount = 0;
        
        // Load plants from config file
        try {
            InputStream configStream = getClass().getResourceAsStream("/garden-config.json");
            if (configStream == null) {
                logger.warning("API", "Config file not found, using default plants");
                // Fallback to default plants if config not found
                addPlant(PlantType.STRAWBERRY, new Position(1, 1));
                addPlant(PlantType.CARROT, new Position(2, 2));
                addPlant(PlantType.TOMATO, new Position(3, 3));
                addPlant(PlantType.SUNFLOWER, new Position(4, 4));
            } else {
                String configContent = new String(configStream.readAllBytes());
                configStream.close();
                loadPlantsFromConfig(configContent);
            }
        } catch (Exception e) {
            logger.error("API", "Error loading config file: " + e.getMessage());
            // Fallback to default plants
            addPlant(PlantType.STRAWBERRY, new Position(1, 1));
            addPlant(PlantType.CARROT, new Position(2, 2));
            addPlant(PlantType.TOMATO, new Position(3, 3));
            addPlant(PlantType.SUNFLOWER, new Position(4, 4));
        }
        
        logger.info("API", "Garden initialized with " + garden.getTotalPlants() + " plants.");
        
        // Enable API mode - disable automatic pest spawning and weather changes
        // Pests and weather will only be triggered via API calls
        // All other automatic systems (pesticide, heating, cooling, sprinklers, water decrease) continue to work
        engine.getPestControlSystem().setApiModeEnabled(true);
        engine.getWeatherSystem().setApiModeEnabled(true);
        engine.getHeatingSystem().setApiModeEnabled(true);
        engine.getCoolingSystem().setApiModeEnabled(true);
        
        // Start headless simulation for continuous plant updates
        // This enables true 24-hour survival testing
        startHeadlessSimulation();
    }
    
    /**
     * Helper method to add a plant to the garden.
     */
    private void addPlant(PlantType plantType, Position position) {
        if (controller.plantSeed(plantType, position)) {
            logger.info("API", "Added plant: " + plantType.getDisplayName() + " at " + position);
        } else {
            logger.warning("API", "Failed to add plant: " + plantType.getDisplayName() + " at " + position);
        }
    }
    
    /**
     * Retrieves plant information including names, water requirements, and pest vulnerabilities.
     * 
     * @return Map containing:
     *         - "plants": List of plant names
     *         - "waterRequirement": List of water requirement amounts (not current levels)
     *         - "parasites": List of lists of pest types that can attack each plant
     */
    public Map<String, Object> getPlants() {
        Map<String, Object> plantInfo = new HashMap<>();
        List<String> plantNames = new ArrayList<>();
        List<Integer> waterRequirements = new ArrayList<>();
        List<List<String>> parasiteList = new ArrayList<>();
        
        for (Plant plant : garden.getAllPlants()) {
            String plantType = plant.getPlantType();
            plantNames.add(plantType);
            waterRequirements.add(plant.getWaterRequirement()); // Fixed: return requirement, not current level
            
            // Handle Flower type format "Flower (Sunflower)" -> extract "Sunflower"
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
     * Simulates rainfall in the garden by adjusting water levels for all plants.
     * This uses smartGarden's WateringSystem and WeatherSystem.
     * 
     * @param amount Amount of water units to add
     */
    public void rain(int amount) {
        logger.info("API", "Rainfall event: " + amount + " units");
        
        // Set weather to rainy
        WeatherSystem weatherSystem = engine.getWeatherSystem();
        weatherSystem.setWeather(WeatherSystem.Weather.RAINY);
        
        // Add water to all plants and apply weather effects
        for (Plant plant : garden.getAllPlants()) {
            if (!plant.isDead()) {
                plant.water(amount);
                plant.applyWeatherEffect("RAINY");
                logger.info("API", "Rain added water to " + plant.getPlantType() + 
                           " at " + plant.getPosition() + ". Current water level: " + plant.getWaterLevel());
            }
        }
        
        // Trigger system updates (sprinklers will stop due to rain listener)
        triggerSystemUpdates();
        
        dayCount++;
    }
    
    /**
     * Simulates temperature changes in the garden.
     * This uses smartGarden's HeatingSystem.
     * 
     * @param temp Temperature in Fahrenheit (specification requirement: 40-120 F)
     */
    public void temperature(int temp) {
        // Validate temperature range (40-120 F as per specification)
        if (temp < 40 || temp > 120) {
            logger.warning("API", "Temperature " + temp + "°F is outside valid range (40-120°F). Clamping to valid range.");
            temp = Math.max(40, Math.min(120, temp));
        }
        
        // Convert Fahrenheit to Celsius (spec expects Fahrenheit input)
        double tempCelsius = (temp - 32) * 5.0 / 9.0;
        int tempCelsiusInt = (int) Math.round(tempCelsius);
        
        logger.info("API", "Temperature changed to " + temp + "°F (" + tempCelsiusInt + "°C)");
        
        HeatingSystem heatingSystem = engine.getHeatingSystem();
        CoolingSystem coolingSystem = engine.getCoolingSystem();
        
        // Set ambient temperature - this updates all zones
        heatingSystem.setAmbientTemperature(tempCelsiusInt);
        coolingSystem.setAmbientTemperature(tempCelsiusInt);
        
        // Apply temperature effects to all plants (using Celsius internally)
        // NOTE: Weather is NOT changed by temperature - weather only changes via api.rain()
        // In API mode, weather changes must be explicit API calls, not automatic based on temperature
        for (Plant plant : garden.getAllPlants()) {
            if (!plant.isDead()) {
                plant.applyTemperatureEffect(tempCelsiusInt);
                // Don't apply weather effects - weather only changes via explicit api.rain() call
                logger.info("API", plant.getPlantType() + " temperature adjusted to " + temp + "°F (" + tempCelsiusInt + "°C)");
            }
        }
        
        // Trigger system updates (heating will activate if temp is low, cooling if temp is high)
        triggerSystemUpdates();
        
        dayCount++;
    }
    
    /**
     * Triggers a parasite (pest) infestation based on plant vulnerabilities.
     * This uses smartGarden's PestControlSystem.
     * 
     * @param parasiteType Type of pest (e.g., "Red Mite", "Green Leaf Worm", etc.) - case-insensitive
     */
    public void parasite(String parasiteType) {
        logger.info("API", "Parasite infestation: " + parasiteType);
        
        // Normalize input for case-insensitive matching
        String normalizedParasiteType = parasiteType.toLowerCase();
        
        // Find all plants that are vulnerable to this pest type
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
            
            // Case-insensitive matching: find matching parasite name from vulnerabilities list
            String matchedParasiteName = null;
            for (String vulnerability : vulnerabilities) {
                if (vulnerability.toLowerCase().equals(normalizedParasiteType)) {
                    matchedParasiteName = vulnerability; // Use original name from config
                    break;
                }
            }
            
            if (matchedParasiteName != null) {
                // Create a pest at the plant's position and attack it
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
        
        // Trigger system updates (pesticide will be applied automatically)
        triggerSystemUpdates();
        
        dayCount++;
    }
    
    /**
     * Logs details about the garden's current state, including plant health and status.
     * Uses smartGarden's Garden.getStatistics() method.
     */
    public void getState() {
        logger.info("API", "Garden State Report - Day " + dayCount);
        
        Map<String, Integer> stats = garden.getStatistics();
        int alive = stats.getOrDefault("livingPlants", 0);
        int dead = stats.getOrDefault("deadPlants", 0);
        
        logger.info("API", "Alive: " + alive + ", Dead: " + dead);
        logger.info("API", "Total Plants: " + stats.getOrDefault("totalPlants", 0));
        logger.info("API", "Zones: " + stats.getOrDefault("zones", 0));
        
        // Log individual plant status
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
     * Starts the headless simulation loop for continuous plant updates.
     * This enables true 24-hour survival testing where plants update automatically,
     * water decreases over time, and systems respond continuously.
     * 
     * Called automatically by initializeGarden(), but can be called manually if needed.
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
     * Registers a JVM shutdown hook to automatically clean up resources.
     * This ensures cleanup happens even if the script doesn't explicitly call cleanup methods.
     * Only registers once for all API instances.
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
     * Triggers all system updates to respond to API state changes.
     * This ensures automatic systems (watering, heating, pest control) work after API calls.
     * Systems are idempotent and safe to call multiple times.
     * All system responses are logged automatically by the systems themselves.
     */
    private void triggerSystemUpdates() {
        HeatingSystem heatingSystem = engine.getHeatingSystem();
        CoolingSystem coolingSystem = engine.getCoolingSystem();
        WateringSystem wateringSystem = engine.getWateringSystem();
        PestControlSystem pestSystem = engine.getPestControlSystem();
        
        // Systems automatically log all their actions - just call update()
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
    
    /**
     * Parses the JSON config file and loads plants into the garden.
     * Simple JSON parser for the specific config format.
     * 
     * @param configContent The JSON content as a string
     */
    private void loadPlantsFromConfig(String configContent) {
        // Simple regex-based parser for the specific JSON structure
        // Pattern: "type": "PlantName", "position": {"row": X, "column": Y}
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
                addPlant(plantType, new Position(row, column));
                plantCount++;
            } else {
                logger.warning("API", "Unknown plant type in config: " + plantTypeName);
            }
        }
        
        if (plantCount == 0) {
            logger.warning("API", "No plants loaded from config, using defaults");
            addPlant(PlantType.STRAWBERRY, new Position(1, 1));
            addPlant(PlantType.CARROT, new Position(2, 2));
            addPlant(PlantType.TOMATO, new Position(3, 3));
            addPlant(PlantType.SUNFLOWER, new Position(4, 4));
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

