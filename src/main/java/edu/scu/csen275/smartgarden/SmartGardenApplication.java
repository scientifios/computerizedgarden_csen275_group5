package edu.scu.csen275.smartgarden;

import edu.scu.csen275.smartgarden.controller.GardenController;
import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Position;
import edu.scu.csen275.smartgarden.simulation.SimulationEngine;
import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import edu.scu.csen275.smartgarden.ui.*;
import edu.scu.csen275.smartgarden.util.Logger;
import edu.scu.csen275.smartgarden.api.GardenSimulationAPI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main JavaFX application for the Smart Garden Simulation.
 * Modern UI with animations, gradients, and garden-themed design.
 */
public class SmartGardenApplication extends Application {
    private GardenController controller;
    private GardenGridPanel gardenPanel;
    private ModernToolbar toolbar;
    private InfoPanel leftInfoPanel;
    private InfoPanel rightInfoPanel;
    private ListView<String> logListView;
    private boolean autoScrollLogs = true;
    private Pane decorativePane;
    private AnimatedBackgroundPane animatedBackground;
    private ParticleSystem particleSystem;
    private PestEventBridge pestEventBridge;
    private Scene scene;
    
    // Track displayed pests to avoid duplicates
    private java.util.Set<String> displayedPestIds;
    private java.util.Map<Position, Integer> lastPestCounts;
    
    // Track previous weather to detect changes
    private WeatherSystem.Weather previousWeather = null;
    private int previousGardenTemperature = Integer.MIN_VALUE;
    
    // API instance (optional - only created if API mode is enabled)
    private GardenSimulationAPI api = null;
    
    private static final int GRID_ROWS = 8;
    private static final int GRID_COLS = 14;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            controller = new GardenController(GRID_ROWS, GRID_COLS);
            
            // Initialize pest event bridge and connect to PestControlSystem
            pestEventBridge = new PestEventBridge();
            controller.getSimulationEngine().getPestControlSystem().setPestEventBridge(pestEventBridge);
            
            // Initialize pest tracking
            displayedPestIds = new java.util.HashSet<>();
            lastPestCounts = new java.util.HashMap<>();
            
            // Load CSS styles
            scene = createScene();
            scene.getStylesheets().add(
                getClass().getResource("/styles/garden-theme.css").toExternalForm()
            );
            
            // Setup stage
            primaryStage.setTitle("Smart Garden Simulation - CSEN 275");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double targetWidth = Math.max(900, Math.min(1400, visualBounds.getWidth() * 0.95));
            double targetHeight = Math.max(600, Math.min(900, visualBounds.getHeight() * 0.95));
            primaryStage.setWidth(targetWidth);
            primaryStage.setHeight(targetHeight);
            primaryStage.centerOnScreen();
            primaryStage.setOnCloseRequest(e -> {
                controller.shutdown();
                Platform.exit();
            });
            
            // Setup pest event handlers
            setupPestEventHandlers();
            
            // Check if API mode is enabled (via system property)
            boolean apiModeEnabled = Boolean.getBoolean("smartgarden.api.enabled") || 
                                    System.getenv("SMARTGARDEN_API_ENABLED") != null;
            
            if (apiModeEnabled) {
                // API mode enabled - create API using same controller (shared state)
                System.out.println("[SmartGardenApplication] API MODE ENABLED - API and UI will share garden state");
                controller.getLogger().info("System", "API mode enabled - UI and API sharing garden state");
                
                // Create API using the same controller
                api = new GardenSimulationAPI(controller);
                api.initializeGarden();
                
                // Schedule API calls (similar to Smart_Garden_System 3)
                scheduleAPICalls(api);
            } else {
                // Normal mode - enable weather rotation
                controller.getSimulationEngine().getWeatherSystem().enableSunnyRainyRotation();
                System.out.println("[SmartGardenApplication] ROTATION MODE: Weather rotating between SUNNY and RAINY every 1 minute");
            }
            
            // Add initial test log entry to verify log display
            controller.getLogger().info("System", "Smart Garden Simulation started");
            controller.getLogger().info("System", "Rain test mode enabled - rain will occur every 1 minute");
            
            // Add initial test log entries to list view
            if (logListView != null) {
                logListView.getItems().clear();
                logListView.getItems().add("[00:00:00] System: Smart Garden Simulation started");
                logListView.getItems().add("[00:00:00] System: Rain test mode enabled - rain will occur every 1 minute");
            }
            
            // Start UI update timer
            startUIUpdateTimer();
            
            primaryStage.show();
            
            // Bind decorative pane size after scene is shown
            Platform.runLater(() -> {
                Scene currentScene = primaryStage.getScene();
                if (currentScene != null && decorativePane != null) {
                    decorativePane.prefWidthProperty().bind(currentScene.widthProperty());
                    decorativePane.prefHeightProperty().bind(currentScene.heightProperty().subtract(200));
                }
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logException("Application", "Failed to start", e);
        }
    }
    
    /**
     * Creates the main scene with all UI components.
     */
    private Scene createScene() {
        // Create animated background with clouds and sunlight rays - fills entire scene
        animatedBackground = new AnimatedBackgroundPane();
        
        // Create decorative overlay pane
        decorativePane = new Pane();
        decorativePane.setPickOnBounds(false);
        decorativePane.setMouseTransparent(true);
        
        // Create particle system for sparkles and pollen
        particleSystem = new ParticleSystem();
        particleSystem.setPickOnBounds(false);
        particleSystem.setMouseTransparent(true);
        
        // Create UI components
        toolbar = new ModernToolbar();
        gardenPanel = new GardenGridPanel(controller);
        leftInfoPanel = new InfoPanel(InfoPanel.Mode.LEFT_SYSTEMS);
        rightInfoPanel = new InfoPanel(InfoPanel.Mode.RIGHT_GARDEN);
        
        // Container for center (garden + decorations + particles)
        StackPane centerContainer = new StackPane();
        centerContainer.setStyle("-fx-background-color: transparent;");
        centerContainer.getChildren().addAll(
            gardenPanel,
            decorativePane,
            particleSystem
        );
        
        // Set animation container for garden panel (for watering animations)
        gardenPanel.setAnimationContainer(centerContainer);
        
        // Set coin float pane
        gardenPanel.setCoinFloatPane(decorativePane);
        gardenPanel.setPlantSelectionHandler(position -> rightInfoPanel.setSelectedPlant(position));
        rightInfoPanel.setPlantSelectionHandler(position -> {
            gardenPanel.setSelectedPlant(position);
            rightInfoPanel.setSelectedPlant(position);
        });
        
        // Setup toolbar actions
        setupToolbarActions();
        
        // Create log panel
        VBox logPanel = createLogPanel();
        
        // Center panel: log on top, garden below (integrated)
        VBox centerPanel = new VBox();
        centerPanel.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(centerContainer, Priority.ALWAYS);
        centerPanel.getChildren().addAll(logPanel, centerContainer);
        
        // Root BorderPane with transparent background
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");
        
        // Make toolbar semi-transparent so clouds show through
        toolbar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        
        root.setBottom(toolbar);
        root.setCenter(centerPanel);
        root.setLeft(leftInfoPanel);
        root.setRight(rightInfoPanel);
        BorderPane.setMargin(centerPanel, new Insets(10));
        BorderPane.setMargin(leftInfoPanel, new Insets(10));
        BorderPane.setMargin(rightInfoPanel, new Insets(10));

        ScrollPane mainScrollPane = new ScrollPane(root);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setPannable(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setStyle("-fx-background-color: transparent;");

        StackPane rootStack = new StackPane();
        rootStack.getChildren().addAll(animatedBackground, mainScrollPane);
        
        // Make background fill entire stack and be visible
        StackPane.setAlignment(animatedBackground, javafx.geometry.Pos.TOP_LEFT);
        animatedBackground.prefWidthProperty().bind(rootStack.widthProperty());
        animatedBackground.prefHeightProperty().bind(rootStack.heightProperty());
        animatedBackground.minWidthProperty().bind(rootStack.widthProperty());
        animatedBackground.minHeightProperty().bind(rootStack.heightProperty());
        animatedBackground.maxWidthProperty().bind(rootStack.widthProperty());
        animatedBackground.maxHeightProperty().bind(rootStack.heightProperty());
        
        // Ensure background is behind everything and visible
        animatedBackground.toBack();
        
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double sceneWidth = Math.max(900, Math.min(1400, visualBounds.getWidth() * 0.95));
        double sceneHeight = Math.max(600, Math.min(900, visualBounds.getHeight() * 0.95));
        Scene scene = new Scene(rootStack, sceneWidth, sceneHeight);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Transparent scene fill so background shows
        
        return scene;
    }
    
    /**
     * Sets up toolbar button actions.
     */
    private void setupToolbarActions() {
        toolbar.getStartButton().setOnAction(e -> {
            try {
                controller.startSimulation();
                toolbar.getStartButton().setDisable(true);
                toolbar.getPauseButton().setDisable(false);
            } catch (IllegalStateException ex) {
                showAlert("Error", ex.getMessage());
            }
        });
        
        toolbar.getPauseButton().setOnAction(e -> {
            if (toolbar.getPauseButton().getText().contains("Pause")) {
                controller.pauseSimulation();
                toolbar.getPauseButton().setText("Resume");
            } else {
                controller.resumeSimulation();
                toolbar.getPauseButton().setText("Pause");
            }
        });
        
        toolbar.getStopButton().setOnAction(e -> {
            controller.stopSimulation();
            toolbar.getStartButton().setDisable(false);
            toolbar.getPauseButton().setDisable(true);
            toolbar.getPauseButton().setText("Pause");
        });
        
        toolbar.getSpeedBox().setOnAction(e -> {
            String speed = toolbar.getSpeedBox().getValue().replace("x", "");
            controller.setSimulationSpeed(Integer.parseInt(speed));
        });
    }
    
    
    /**
     * Creates the log panel.
     */
    private VBox createLogPanel() {
        VBox logPanel = new VBox(8);
        logPanel.setPadding(new Insets(10));
        logPanel.setPrefHeight(200); // Increased from maxHeight
        logPanel.setMinHeight(150);
        logPanel.setMaxHeight(250);
        logPanel.getStyleClass().add("log-panel");
        logPanel.setVisible(true); // Ensure visible
        logPanel.setManaged(true); // Ensure managed
        
        Label logTitle = new Label("Events");
        logTitle.getStyleClass().add("log-title");

        javafx.scene.control.Button clearButton = new javafx.scene.control.Button("Clear");
        clearButton.getStyleClass().add("modern-button");
        clearButton.setOnAction(e -> {
            if (logListView != null) {
                logListView.getItems().clear();
            }
        });

        javafx.scene.control.Button pauseButton = new javafx.scene.control.Button("Pause Auto-Scroll");
        pauseButton.getStyleClass().add("modern-button");
        pauseButton.setOnAction(e -> {
            autoScrollLogs = !autoScrollLogs;
            pauseButton.setText(autoScrollLogs ? "Pause Auto-Scroll" : "Resume Auto-Scroll");
        });

        HBox titleBar = new HBox(10, logTitle, clearButton, pauseButton);
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        logListView = new ListView<>();
        logListView.setPrefHeight(150); // Increased height
        logListView.setMinHeight(100);
        logListView.getStyleClass().add("list-view");
        logListView.setVisible(true); // Ensure visible
        logListView.setManaged(true); // Ensure managed
        logListView.setEditable(false);
        logListView.setFocusTraversable(false);
        
        // Add initial test message to verify list view works
        logListView.getItems().add("Log panel initialized - waiting for events...");
        
        logPanel.getChildren().addAll(titleBar, logListView);
        return logPanel;
    }
    
    /**
     * Animates button click.
     */
    private void animateButtonClick(javafx.scene.control.Button button) {
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
            javafx.util.Duration.millis(100), button
        );
        scale.setToX(0.9);
        scale.setToY(0.9);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }
    
    /**
     * Sets up pest event handlers to connect PestControlSystem to UI animations.
     */
    private void setupPestEventHandlers() {
        pestEventBridge.setHandler(new PestEventBridge.PestAnimationHandler() {
            @Override
            public void onPestSpawned(Position position, String pestType, boolean isHarmful) {
                gardenPanel.onPestSpawned(position, pestType, isHarmful);
            }

            @Override
            public void onPestAttack(Position position, int damage) {
                gardenPanel.onPestAttack(position, damage);
            }

            @Override
            public void onPesticideApplied(Position position) {
                gardenPanel.onPesticideApplied(position);
            }

            @Override
            public void onPestRemoved(Position position, String pestType) {
                // Not explicitly handled in UI yet, but can be added
            }
        });
    }
    
    /**
     * Starts the UI update timer.
     */
    private void startUIUpdateTimer() {
        javafx.animation.Timeline uiTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.5), e -> updateUI())
        );
        uiTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        uiTimeline.play();
    }
    
    /**
     * Updates the entire UI.
     */
    private void updateUI() {
        if (controller == null) {
            System.err.println("[SmartGardenApplication] ERROR: Controller is null in updateUI()!");
            return;
        }
        
        try {
            SimulationEngine engine = controller.getSimulationEngine();
            if (engine == null) {
                System.err.println("[SmartGardenApplication] ERROR: SimulationEngine is null!");
                return;
            }
            Garden garden = controller.getGarden();
            if (garden == null) {
                System.err.println("[SmartGardenApplication] ERROR: Garden is null!");
                return;
            }
            
            // Update garden tiles
            gardenPanel.updateAllTiles();
            
            // Update toolbar status
            String state = engine.getState().toString();
            toolbar.updateStatus(state);
            
            // Update simulation info
            leftInfoPanel.getTimeLabel().setText("Day " + engine.getDayCounter() + " | " + engine.getFormattedTime());
            leftInfoPanel.getStatsLabel().setText(
                String.format("Plants: %d alive / %d total",
                    garden.getLivingPlants().size(), garden.getTotalPlants())
            );
            
            // Update weather display and rain animation
            WeatherSystem.Weather weather = engine.getWeatherSystem().getCurrentWeather();
            leftInfoPanel.getWeatherDisplay().updateWeather(weather);
            
            // Update heating status
            edu.scu.csen275.smartgarden.system.HeatingSystem.HeatingMode heatingMode = 
                engine.getHeatingSystem().getHeatingMode();
            String heatingText;
            if (heatingMode == edu.scu.csen275.smartgarden.system.HeatingSystem.HeatingMode.OFF) {
                heatingText = "Heating: Off";
            } else {
                heatingText = "Heating: " + heatingMode.name() + " (" + 
                              engine.getHeatingSystem().getCurrentTemperature() + "°C)";
            }
            leftInfoPanel.getHeatingStatusLabel().setText(heatingText);
            
            // Update temperature label
            int currentTemp = engine.getHeatingSystem().getCurrentTemperature();
            leftInfoPanel.getTemperatureLabel().setText("Current: " + currentTemp + "C");
            
            // Update background brightness based on weather
            if (animatedBackground != null) {
                animatedBackground.setWeather(weather == WeatherSystem.Weather.SUNNY);
            }
            
            // Keep weather system logic, without weather visual animations.
            if (previousWeather != weather) {
                if (weather == WeatherSystem.Weather.RAINY || weather == WeatherSystem.Weather.SNOWY) {
                    if (engine != null && engine.getWateringSystem() != null) {
                        engine.getWateringSystem().stopAllSprinklers();
                    }
                    edu.scu.csen275.smartgarden.ui.SprinklerAnimationEngine.stopAllAnimations();
                }
                previousWeather = weather;
            }
            
            // Update resource bars with animation
            double waterProgress = Math.max(0, Math.min(1, 
                engine.getWateringSystem().getWaterSupply() / 10000.0));
            double pesticideProgress = Math.max(0, Math.min(1,
                engine.getPestControlSystem().getPesticideStock() / 50.0));
            
            leftInfoPanel.updateProgressBars(waterProgress, pesticideProgress);

            Map<Position, Integer> activePestsByPosition = buildActivePestMap(engine);
            List<Position> criticalThreatPositions = activePestsByPosition.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .sorted((a, b) -> {
                    int rowCompare = Integer.compare(a.row(), b.row());
                    return rowCompare != 0 ? rowCompare : Integer.compare(a.column(), b.column());
                })
                .toList();

            leftInfoPanel.updateLeftPanel(
                weather,
                currentTemp,
                countPlantsNeedingWater(garden),
                summarizeSprinklerActivations(engine),
                summarizeSensors(engine),
                engine.getWateringSystem().getWaterSupply(),
                formatHeatingStatus(engine),
                formatCoolingStatus(engine),
                currentTemp,
                formatTemperatureTrend(currentTemp),
                engine.getPestControlSystem().getHarmfulPestCount(),
                formatCriticalThreats(criticalThreatPositions),
                engine.getPestControlSystem().getPesticideStock()
            );
            rightInfoPanel.updateGardenInfo(
                engine.getDayCounter(),
                engine.getFormattedTime(),
                garden.getLivingPlants().size(),
                garden.getAllPlants(),
                activePestsByPosition
            );
            
            // Update logs and detect automatic watering
            List<String> recentLogs = new ArrayList<>();
            
            try {
                // Get logs - try both controller logger and singleton instance
                edu.scu.csen275.smartgarden.util.Logger logger = null;
                
                if (controller != null) {
                    logger = controller.getLogger();
                }
                
                // Fallback to singleton if controller logger is null
                if (logger == null) {
                    logger = edu.scu.csen275.smartgarden.util.Logger.getInstance();
                }
                
                if (logger != null) {
                    List<edu.scu.csen275.smartgarden.util.Logger.LogEntry> logEntries = 
                        logger.getRecentLogs(20);
                    
                    // Get current weather once - check if it's raining
                    WeatherSystem.Weather currentWeather = engine.getWeatherSystem().getCurrentWeather();
                    boolean isRaining = (currentWeather == WeatherSystem.Weather.RAINY);
                    
                    // Track processed log entries to avoid duplicate animations
                    java.util.Set<String> processedWateringLogs = new java.util.HashSet<>();
                    
                    for (edu.scu.csen275.smartgarden.util.Logger.LogEntry entry : logEntries) {
                        try {
                            String logEntry = entry.toDisplayFormat();
                            if (logEntry != null && !logEntry.trim().isEmpty()) {
                                recentLogs.add(logEntry);
                                
                                // Detect automatic watering from logs
                                if (logEntry.contains("Auto-watered Zone")) {
                                    // Always skip if it's currently raining
                                    if (isRaining) {
                                        continue;
                                    }
                                    
                                    // Create unique key from timestamp and message to prevent duplicates
                                    String logKey = entry.timestamp().toString() + ":" + logEntry;
                                    if (processedWateringLogs.contains(logKey)) {
                                        continue; // Already processed this log entry
                                    }
                                    processedWateringLogs.add(logKey);
                                    
                                    // Extract zone ID from log message
                                    try {
                                        int zoneId = Integer.parseInt(logEntry.substring(
                                            logEntry.indexOf("Zone ") + 5,
                                            logEntry.indexOf(" ", logEntry.indexOf("Zone ") + 5)
                                        ));
                                        System.out.println("[SmartGardenApplication] Detected automatic watering for Zone " + zoneId);
                                        gardenPanel.animateWatering(zoneId);
                                    } catch (Exception e) {
                                        // If parsing fails, just animate all tiles
                                        System.out.println("[SmartGardenApplication] Detected automatic watering - animating all tiles");
                                        gardenPanel.animateAllTilesWatering();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[SmartGardenApplication] Error formatting log entry: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("[SmartGardenApplication] ERROR: Could not get logger instance!");
                }
            } catch (Exception e) {
                System.err.println("[SmartGardenApplication] ERROR getting logs: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Update list view (already on JavaFX thread from Timeline)
            try {
                if (logListView != null) {
                    // Clear and update on JavaFX thread (we're already on it, but be safe)
                    javafx.application.Platform.runLater(() -> {
                        try {
                            logListView.getItems().clear();

                            if (!recentLogs.isEmpty()) {
                                logListView.getItems().addAll(recentLogs);

                                // Auto-scroll to bottom of log if enabled
                                if (autoScrollLogs && !logListView.getItems().isEmpty()) {
                                    logListView.scrollTo(logListView.getItems().size() - 1);
                                }
                            } else {
                                // Keep the initial message if no logs yet
                                if (logListView.getItems().isEmpty()) {
                                    logListView.getItems().add("No events yet...");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[SmartGardenApplication] ERROR updating list view items: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.err.println("[SmartGardenApplication] ERROR: logListView is null! Cannot display logs.");
                }
            } catch (Exception e) {
                System.err.println("[SmartGardenApplication] ERROR updating list view: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Monitor pest state and update UI animations
            updatePestAnimations(engine);
            
        } catch (Exception e) {
            // Log UI update errors instead of silently ignoring
            System.err.println("[SmartGardenApplication] ERROR in updateUI(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int countPlantsNeedingWater(Garden garden) {
        return (int) garden.getLivingPlants().stream()
            .filter(plant -> plant.getWaterLevel() < plant.getWaterRequirement())
            .count();
    }

    private String summarizeSprinklerActivations(SimulationEngine engine) {
        int totalActivations = engine.getGarden().getZones().stream()
            .map(zone -> zone.getZoneId())
            .filter(zoneId -> engine.getWateringSystem().getSprinkler(zoneId) != null
                && engine.getWateringSystem().getSprinkler(zoneId) != null)
            .mapToInt(zoneId -> engine.getWateringSystem().getSprinkler(zoneId).getActivationCount())
            .sum();
        return String.valueOf(totalActivations);
    }

    private String summarizeSensors(SimulationEngine engine) {
        long activeCount = engine.getGarden().getZones().stream()
            .map(zone -> engine.getWateringSystem().getSensor(zone.getZoneId()))
            .filter(sensor -> sensor != null && sensor.getStatus() == edu.scu.csen275.smartgarden.system.Sensor.SensorStatus.ACTIVE)
            .count();
        return activeCount + " / " + engine.getGarden().getZones().size() + " active";
    }

    private String formatHeatingStatus(SimulationEngine engine) {
        var mode = engine.getHeatingSystem().getHeatingMode();
        if (mode == edu.scu.csen275.smartgarden.system.HeatingSystem.HeatingMode.OFF) {
            return "Off";
        }
        return mode.name() + " at " + engine.getHeatingSystem().getCurrentTemperature() + "C";
    }

    private String formatCoolingStatus(SimulationEngine engine) {
        var mode = engine.getCoolingSystem().getCoolingMode();
        if (mode == edu.scu.csen275.smartgarden.system.CoolingSystem.CoolingMode.OFF) {
            return "Off";
        }
        return mode.name() + " at " + engine.getCoolingSystem().getCurrentTemperature() + "C";
    }

    private String formatTemperatureTrend(int currentTemp) {
        if (previousGardenTemperature == Integer.MIN_VALUE) {
            previousGardenTemperature = currentTemp;
            return "Stable";
        }

        int delta = currentTemp - previousGardenTemperature;
        previousGardenTemperature = currentTemp;
        if (delta > 0) {
            return "Rising (+" + delta + "C)";
        }
        if (delta < 0) {
            return "Cooling (" + delta + "C)";
        }
        return "Stable";
    }

    private String formatCriticalThreats(List<Position> criticalThreatPositions) {
        if (criticalThreatPositions.isEmpty()) {
            return "None";
        }
        return criticalThreatPositions.stream()
            .map(position -> "(" + (position.row() + 1) + "," + (position.column() + 1) + ")")
            .collect(Collectors.joining(", "));
    }

    private Map<Position, Integer> buildActivePestMap(SimulationEngine engine) {
        Map<Position, Integer> pestsByPosition = new HashMap<>();
        for (var pest : engine.getPestControlSystem().getPests()) {
            if (pest != null && pest.isAlive()) {
                pestsByPosition.merge(pest.getPosition(), 1, Integer::sum);
            }
        }
        return pestsByPosition;
    }

    /**
     * Monitors pest state and updates UI animations.
     */
    private void updatePestAnimations(SimulationEngine engine) {
        try {
            var pestSystem = engine.getPestControlSystem();
            var garden = controller.getGarden();
            
            // Get all current pests from PestControlSystem
            var allPests = pestSystem.getPests();
            
            // Track current pest state per position
            java.util.Map<Position, java.util.List<edu.scu.csen275.smartgarden.system.Pest>> currentPests = new java.util.HashMap<>();
            
            // Group pests by position
            for (var pest : allPests) {
                if (pest.isAlive()) {
                    Position pos = pest.getPosition();
                    currentPests.computeIfAbsent(pos, k -> new java.util.ArrayList<>()).add(pest);
                }
            }
            
            // Check ALL plant positions (not just ones with pests) to detect removal
            for (var plant : garden.getLivingPlants()) {
                Position pos = plant.getPosition();
                var tile = gardenPanel.getTileAt(pos.row(), pos.column());
                
                if (tile != null && tile.isVisible()) {
                    // Get pest counts
                    var pestsAtPos = currentPests.getOrDefault(pos, new java.util.ArrayList<>());
                    int previousCount = lastPestCounts.getOrDefault(pos, 0);
                    int currentCount = pestsAtPos.size();
                    int uiPestCount = tile.getActivePestCount();
                    
                    // If PestControlSystem has pests but UI doesn't, spawn them
                    if (currentCount > 0 && uiPestCount == 0) {
                        // Spawn all pests at this position
                        for (var pest : pestsAtPos) {
                            String pestType = pest.getPestType();
                            
                            // Spawn in UI (all pests are harmful now)
                            gardenPanel.onPestSpawned(pos, pestType, true);
                        }
                    } else if (currentCount > uiPestCount && uiPestCount > 0) {
                        // More pests than UI shows - spawn additional ones
                        int toSpawn = currentCount - uiPestCount;
                        int spawned = 0;
                        for (var pest : pestsAtPos) {
                            if (spawned >= toSpawn) break;
                            String pestType = pest.getPestType();
                            // All pests are harmful now
                            gardenPanel.onPestSpawned(pos, pestType, true);
                            spawned++;
                        }
                    }
                    
                    // If PestControlSystem has no pests but UI does, remove them (pesticide applied)
                    if (currentCount == 0 && uiPestCount > 0) {
                        // Pests were removed - trigger pesticide animation
                        tile.applyPesticide();
                        // Clean up displayed pests for this position
                        displayedPestIds.removeIf(id -> id.contains("," + pos.row() + "," + pos.column()));
                    }
                    
                    // Update animations
                    tile.updatePestAnimations();
                    
                    // Update last count
                    lastPestCounts.put(pos, currentCount);
                }
            }
            
            // Clean up positions that no longer have plants
            lastPestCounts.keySet().removeIf(pos -> {
                var plant = garden.getPlant(pos);
                return plant == null || plant.isDead();
            });
            
            // Check for pest attacks and show damage
            for (var plant : garden.getLivingPlants()) {
                if (plant.getTotalPestAttacks() > 0) {
                    Position pos = plant.getPosition();
                    var tile = gardenPanel.getTileAt(pos.row(), pos.column());
                    
                    if (tile != null && tile.isVisible() && tile.hasPests()) {
                        // Show damage visual periodically when under attack
                        // (We'll show damage when pest attacks happen)
                    }
                }
            }
            
            // Check for pesticide application (when treatment happens)
            for (var zone : garden.getZones()) {
                if (pestSystem.getActivePestCountAtPosition(zone.getBoundaries().get(0)) == 0) {
                    // Check if there were pests before (simplified check)
                    // Better approach: track when treatment is applied
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Log error but continue
        }
    }
    
    /**
     * Shows an alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Schedules API calls using JavaFX Timeline (similar to Smart_Garden_System 3).
     * Only called when API mode is enabled.
     * 
     * Note: In a real scenario, the professor's script would call API methods externally.
     * This method can optionally schedule API calls for testing/demonstration.
     * By default, it's disabled - API calls should come from external scripts.
     */
    private void scheduleAPICalls(GardenSimulationAPI api) {
        // API mode is enabled - UI and API share the same controller
                   // UI will update automatically via polling (updateUI() every 0.5s)
                   System.out.println("[SmartGardenApplication] API mode active - external API calls will be visible in UI via polling");
                   controller.getLogger().info("System", "API mode active - API calls can be made externally, UI will update via polling");
        
        // Optional: Enable scheduled API calls for testing/demonstration
        // Uncomment below to enable automatic API calls (like Smart_Garden_System 3):
        /*
        Random rand = new Random();
        
        // Schedule rain every 60 seconds
        Timeline rainTimeline = new Timeline(new KeyFrame(Duration.seconds(60), ev -> {
            api.rain(rand.nextInt(40));
        }));
        rainTimeline.setCycleCount(Timeline.INDEFINITE);
        rainTimeline.play();
        
        // Schedule temperature every 40 seconds
        Timeline tempTimeline = new Timeline(new KeyFrame(Duration.seconds(40), ev -> {
            api.temperature(40 + rand.nextInt(80)); // 40-120°F
        }));
        tempTimeline.setCycleCount(Timeline.INDEFINITE);
        tempTimeline.play();
        
        // Schedule parasite every 30 seconds
        Timeline parasiteTimeline = new Timeline(new KeyFrame(Duration.seconds(30), ev -> {
            String[] pests = {"Red Mite", "Green Leaf Worm", "Black Beetle", "Brown Caterpillar"};
            api.parasite(pests[rand.nextInt(pests.length)]);
        }));
        parasiteTimeline.setCycleCount(Timeline.INDEFINITE);
        parasiteTimeline.play();
        */
    }
    
    public static void main(String[] args) {
        // Check command-line arguments for API mode
        for (String arg : args) {
            if (arg.equals("--api") || arg.equals("-api")) {
                System.setProperty("smartgarden.api.enabled", "true");
                break;
            }
        }
        launch(args);
    }
}

