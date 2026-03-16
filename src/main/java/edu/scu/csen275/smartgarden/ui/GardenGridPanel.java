package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.controller.GardenController;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.PlantType;
import edu.scu.csen275.smartgarden.model.Position;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel containing the interactive garden grid with animated tiles.
 */
public class GardenGridPanel extends VBox {
    private final GardenController controller;
    private final GridPane gardenGrid;
    private final AnimatedTile[][] tiles;
    private final GrassTile[][] grassTiles; // Grass tiles for empty cells
    private ComboBox<PlantType> plantSelector;
    private Pane animationContainer; // Container for watering animations
    private Pane coinFloatPane; // Pane for coin float animations
    private Position selectedPlantPosition;
    private Consumer<Position> plantSelectionHandler;
    private final int gridRows;
    private final int gridCols;
    
    /**
     * Gets the animation container.
     */
    public Pane getAnimationContainer() {
        return animationContainer;
    }
    
    /**
     * Sets the pane for coin float animations.
     */
    public void setCoinFloatPane(Pane pane) {
        this.coinFloatPane = pane;
    }

    public void setPlantSelectionHandler(Consumer<Position> plantSelectionHandler) {
        this.plantSelectionHandler = plantSelectionHandler;
    }
    
    /**
     * Safely applies an effect to a node, deferring if not in scene.
     */
    private void safeSetEffect(javafx.scene.Node node, javafx.scene.effect.Effect effect) {
        if (node.getScene() != null && node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
            // Node is in scene and has valid bounds, apply immediately
            node.setEffect(effect);
        } else {
            // Defer until node is in scene
            node.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    // Use Platform.runLater to ensure layout is complete
                    javafx.application.Platform.runLater(() -> {
                        if (node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
                            node.setEffect(effect);
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Helper to find particle system and trigger sparkles.
     */
    private void findAndTriggerSparkles(javafx.scene.Node node, double x, double y) {
        if (node instanceof ParticleSystem) {
            ((ParticleSystem) node).createSparkleBurst(x, y);
        } else if (node instanceof javafx.scene.layout.Pane) {
            javafx.scene.layout.Pane pane = (javafx.scene.layout.Pane) node;
            for (javafx.scene.Node child : pane.getChildren()) {
                findAndTriggerSparkles(child, x, y);
            }
        }
    }
    
    public GardenGridPanel(GardenController controller) {
        this.controller = controller;
        this.gridRows = controller.getGarden().getRows();
        this.gridCols = controller.getGarden().getColumns();
        this.tiles = new AnimatedTile[gridRows][gridCols];
        this.grassTiles = new GrassTile[gridRows][gridCols];
        this.gardenGrid = new GridPane();
        
        setupPanel();
        setupPlantSelector();
        setupGrid();
    }
    
    /**
     * Sets the animation container for watering effects overlay.
     */
    public void setAnimationContainer(Pane container) {
        this.animationContainer = container;
        
        // Also set animation container on all existing tiles
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                if (tiles[row][col] != null) {
                    tiles[row][col].setAnimationContainer(container);
                }
            }
        }
    }
    
    /**
     * Sets up the main panel.
     */
    private void setupPanel() {
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("garden-panel");
        // Make panel background transparent so animated background shows through
    }
    
    /**
     * Sets up plant selector dropdown with categorized PlantType enum.
     */
    private void setupPlantSelector() {
        HBox selectorBox = new HBox(10);
        selectorBox.setAlignment(Pos.CENTER);
        selectorBox.getStyleClass().add("plant-selector");
        
        Label selectLabel = new Label("Select Plant:");
        selectLabel.getStyleClass().add("plant-selector-label");
        
        plantSelector = new ComboBox<>();
        plantSelector.setEditable(false); // Make sure it's not editable
        plantSelector.setDisable(false); // Ensure it's enabled
        plantSelector.setFocusTraversable(true); // Allow focus
        
        // Add all plant types grouped by category
        // Fruit Plants
        plantSelector.getItems().add(PlantType.STRAWBERRY);
        plantSelector.getItems().add(PlantType.GRAPEVINE);
        plantSelector.getItems().add(PlantType.APPLE);
        
        // Vegetable Crops
        plantSelector.getItems().add(PlantType.CARROT);
        plantSelector.getItems().add(PlantType.TOMATO);
        plantSelector.getItems().add(PlantType.ONION);
        
        // Flowers
        plantSelector.getItems().add(PlantType.SUNFLOWER);
        plantSelector.getItems().add(PlantType.TULIP);
        plantSelector.getItems().add(PlantType.ROSE);
        
        // Set default value
        plantSelector.setValue(PlantType.STRAWBERRY);
        
        // Cell factory: show plant name only (no emoji)
        plantSelector.setCellFactory(list -> new ListCell<PlantType>() {
            @Override
            protected void updateItem(PlantType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    setGraphic(null);
                }
            }
        });

        // Button cell: show selected plant name only (no emoji)
        plantSelector.setButtonCell(new ListCell<PlantType>() {
            @Override
            protected void updateItem(PlantType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Plant");
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    setGraphic(null);
                }
            }
        });
        
        // Ensure dropdown is visible and can open
        plantSelector.setVisibleRowCount(10); // Show more items

        // Style via CSS classes.
        plantSelector.getStyleClass().add("modern-combo");
        plantSelector.setPrefWidth(200);
        
        javafx.scene.control.Button clearBtn = new javafx.scene.control.Button("Clear All");
        clearBtn.getStyleClass().add("modern-button");
        clearBtn.setOnAction(e -> clearGarden());
        
        selectorBox.getChildren().addAll(selectLabel, plantSelector, clearBtn);
        this.getChildren().add(selectorBox);
    }
    
    /**
     * Sets up the garden grid with animated tiles.
     */
    private void setupGrid() {
        gardenGrid.setHgap(3);
        gardenGrid.setVgap(3);
        gardenGrid.getStyleClass().add("garden-grid");
        gardenGrid.setAlignment(Pos.CENTER);
        
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                // Create grass tile for empty cells
                GrassTile grassTile = new GrassTile();
                grassTiles[row][col] = grassTile;
                
                // Create plant tile (hidden initially)
                AnimatedTile tile = createTile(row, col);
                int tileIndex = (row * gridCols + col);
                tile.setTileIndex(tileIndex);
                tiles[row][col] = tile;
                tile.setVisible(false); // Hidden until plant is added
                
                // Stack grass and plant tiles so plant appears on top
                StackPane tileStack = new StackPane();
                tileStack.getChildren().addAll(grassTile, tile);
                tileStack.setAlignment(javafx.geometry.Pos.CENTER);
                
                gardenGrid.add(tileStack, col, row);
            }
        }
        
        this.getChildren().add(gardenGrid);
    }
    
    /**
     * Creates an animated tile for a grid cell.
     */
    private AnimatedTile createTile(int row, int col) {
        AnimatedTile tile = new AnimatedTile();
        Position position = new Position(row, col);
        GrassTile grassTile = grassTiles[row][col];
        
        // Set animation container on tile if available
        if (animationContainer != null) {
            tile.setAnimationContainer(animationContainer);
        }
        
        // Click handler - attach to grass tile (which is always visible for empty cells)
        grassTile.setOnMouseClicked(e -> {
            Plant existing = controller.getGarden().getPlant(position);
            
            // Create sparkle burst at click location
            javafx.geometry.Bounds bounds = grassTile.localToScene(grassTile.getBoundsInLocal());
            if (bounds != null && getAnimationContainer() != null) {
                Pane container = getAnimationContainer();
                if (container instanceof javafx.scene.layout.StackPane) {
                    javafx.geometry.Bounds containerBounds = container.localToScene(
                        container.getBoundsInLocal()
                    );
                    if (containerBounds != null) {
                        double x = bounds.getCenterX() - containerBounds.getMinX();
                        double y = bounds.getCenterY() - containerBounds.getMinY();
                        javafx.scene.Node node = container.getScene().getRoot();
                        findAndTriggerSparkles(node, x, y);
                    }
                }
            }
            
            if (existing == null) {
                PlantType selectedType = plantSelector.getValue();
                if (selectedType != null && controller.plantSeed(selectedType, position)) {
                    // Plant appears at full size immediately (no growth animation)
                    updateTile(row, col);
                    
                    // Float petals from grass when planting (only if there was a flower)
                    if (grassTile.hasFlower()) {
                        grassTile.floatPetals();
                    }
                }
                // No auto-bloom - keep empty tiles icon-free
            } else {
                showPlantTooltip(tile, existing);
            }
        });
        
        // Also attach click handler to plant tile when visible
        tile.setOnMouseClicked(e -> {
            Plant existing = controller.getGarden().getPlant(position);
            if (existing != null) {
                selectPlant(position);
                showPlantTooltip(tile, existing);
            }
        });
        
        // Hover effects on grass tile
        grassTile.setOnMouseEntered(e -> {
            if (tile.isVisible()) {
                tile.applyHoverEffect();
                Plant plant = controller.getGarden().getPlant(position);
                if (plant != null) {
                    showHoverTooltip(tile, plant);
                }
            } else {
                // Hover effect on grass - make it glow slightly
                safeSetEffect(grassTile, new javafx.scene.effect.Glow(0.2));
            }
        });
        
        grassTile.setOnMouseExited(e -> {
            if (tile.isVisible()) {
                tile.removeHoverEffect();
            } else {
                grassTile.setEffect(null);
            }
        });
        
        // Hover effects on plant tile
        tile.setOnMouseEntered(e -> {
            if (tile.isVisible()) {
                tile.applyHoverEffect();
                Plant plant = controller.getGarden().getPlant(position);
                if (plant != null) {
                    showHoverTooltip(tile, plant);
                }
            }
        });
        
        tile.setOnMouseExited(e -> {
            if (tile.isVisible()) {
                tile.removeHoverEffect();
            }
        });
        
        return tile;
    }
    
    /**
     * Shows tooltip on hover with plant information.
     */
    private void showHoverTooltip(AnimatedTile tile, Plant plant) {
        String tooltipText = String.format(
            "Plant: %s\nHealth: %d%%\nWater: %d%%\nAge: %d days\nStage: %s",
            plant.getPlantType(),
            plant.getHealthLevel(),
            plant.getWaterLevel(),
            plant.getDaysAlive(),
            plant.getGrowthStage().getDisplayName()
        );
        
        Tooltip tooltip = new Tooltip(tooltipText);
        Tooltip.install(tile, tooltip);
    }
    
    /**
     * Shows detailed plant information dialog.
     */
    private void showPlantTooltip(AnimatedTile tile, Plant plant) {
        // Get active pest count at this plant's position
        int activePestCount = controller.getSimulationEngine()
            .getPestControlSystem()
            .getActivePestCountAtPosition(plant.getPosition());
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("Plant Information");
        alert.setHeaderText(plant.getPlantType() + " at " + plant.getPosition());
        alert.setContentText(String.format(
            "Growth Stage: %s\nHealth: %d%% (%s)\nWater Level: %d%% (Requires: %d%%)\n" +
            "Days Alive: %d / %d\n" +
            "Total Pest Attacks: %d (Lifetime)\n" +
            "Active Pests: %d (Currently Attacking)\n" +
            "Status: %s",
            plant.getGrowthStage().getDisplayName(),
            plant.getHealthLevel(),
            plant.getHealthStatus(),
            plant.getWaterLevel(),
            plant.getWaterRequirement(),
            plant.getDaysAlive(),
            plant.getMaxLifespan(),
            plant.getTotalPestAttacks(),
            activePestCount,
            plant.getHealthStatus()
        ));
        alert.showAndWait();
    }
    
    /**
     * Updates a specific tile.
     */
    public void updateTile(int row, int col) {
        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            Position position = new Position(row, col);
            Plant plant = controller.getGarden().getPlant(position);
            
            if (plant == null) {
                // Show grass tile, hide plant tile
                grassTiles[row][col].setVisible(true);
                tiles[row][col].setVisible(false);
                // No icons on empty grass - just plain grass
            } else {
                // Show plant tile, hide grass tile
                grassTiles[row][col].setVisible(false);
                // Remove any flower icon from grass when plant is shown
                grassTiles[row][col].removeFlower();
                tiles[row][col].setVisible(true);
                tiles[row][col].update(plant);
                tiles[row][col].setSelected(position.equals(selectedPlantPosition));
            }
        }
    }
    
    /**
     * Updates all tiles in the grid.
     */
    public void updateAllTiles() {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                updateTile(row, col);
            }
        }
    }
    
    /**
     * Refreshes tile state after watering in a zone.
     */
    public void animateWatering(int zoneId) {
        int zoneRow = (zoneId - 1) / 3;
        int zoneCol = (zoneId - 1) % 3;
        int tilesPerZone = 3;
        
        int startRow = zoneRow * tilesPerZone;
        int endRow = startRow + tilesPerZone;
        int startCol = zoneCol * tilesPerZone;
        int endCol = startCol + tilesPerZone;
        
        for (int row = startRow; row < endRow && row < gridRows; row++) {
            for (int col = startCol; col < endCol && col < gridCols; col++) {
                Position position = new Position(row, col);
                Plant plant = controller.getGarden().getPlant(position);
                if (tiles[row][col] != null && plant != null) {
                    tiles[row][col].showTemporaryWaterHint();
                    tiles[row][col].update(plant);
                }
            }
        }
    }
    
    /**
     * Refreshes tile state after global watering.
     */
    public void animateAllTilesWatering() {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                Position position = new Position(row, col);
                Plant plant = controller.getGarden().getPlant(position);
                if (tiles[row][col] != null && plant != null) {
                    tiles[row][col].showTemporaryWaterHint();
                    tiles[row][col].update(plant);
                }
            }
        }
    }
    
    /**
     * Animates pesticide effect on a zone.
     */
    public void animatePesticide(int zoneId) {
        int zoneRow = (zoneId - 1) / 3;
        int zoneCol = (zoneId - 1) % 3;
        int tilesPerZone = 3;
        
        int startRow = zoneRow * tilesPerZone;
        int endRow = startRow + tilesPerZone;
        int startCol = zoneCol * tilesPerZone;
        int endCol = startCol + tilesPerZone;
        
        for (int row = startRow; row < endRow && row < gridRows; row++) {
            for (int col = startCol; col < endCol && col < gridCols; col++) {
                if (tiles[row][col] != null) {
                    tiles[row][col].animatePesticide();
                }
            }
        }
    }
    
    /**
     * Clears all plants from the garden.
     */
    private void clearGarden() {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                controller.removePlant(new Position(row, col));
            }
        }
        updateAllTiles();
    }
    
    public GridPane getGardenGrid() {
        return gardenGrid;
    }
    
    // ============ PEST EVENT HANDLERS ============
    
    /**
     * Handles pest spawn event - spawns pest sprite on tile.
     */
    public void onPestSpawned(Position position, String pestType, boolean isHarmful) {
        if (position.row() >= 0 && position.row() < gridRows && 
            position.column() >= 0 && position.column() < gridCols) {
            
            AnimatedTile tile = tiles[position.row()][position.column()];
            
            if (tile != null && tile.isVisible()) {
                if (isHarmful) {
                    tile.spawnPest(pestType);
                }
                // Beneficial insects removed - only harmful pests spawn
            } else {
                System.err.println("[GardenGridPanel] ERROR: Cannot spawn pest - tile is null or not visible");
            }
        } else {
            System.err.println("[GardenGridPanel] ERROR: Invalid position: (" + position.row() + ", " + position.column() + ")");
        }
    }
    
    /**
     * Handles pest attack event - shows damage visual.
     */
    public void onPestAttack(Position position, int damage) {
        if (position.row() >= 0 && position.row() < gridRows && 
            position.column() >= 0 && position.column() < gridCols) {
            
            AnimatedTile tile = tiles[position.row()][position.column()];
            if (tile != null && tile.isVisible()) {
                tile.showDamageVisual(damage);
            }
        }
    }
    
    /**
     * Handles pesticide application - animates spray effect.
     */
    public void onPesticideApplied(edu.scu.csen275.smartgarden.model.Zone zone) {
        // Apply to all plants in the zone
        for (var plant : zone.getPlants()) {
            if (!plant.isDead()) {
                Position pos = plant.getPosition();
                if (pos.row() >= 0 && pos.row() < gridRows && 
                    pos.column() >= 0 && pos.column() < gridCols) {
                    
                    AnimatedTile tile = tiles[pos.row()][pos.column()];
                    if (tile != null && tile.isVisible() && tile.hasPests()) {
                        tile.applyPesticide();
                    }
                }
            }
        }
    }
    
    /**
     * Handles pesticide application at a specific position.
     */
    public void onPesticideApplied(Position position) {
        if (position.row() >= 0 && position.row() < gridRows && 
            position.column() >= 0 && position.column() < gridCols) {
            
            AnimatedTile tile = tiles[position.row()][position.column()];
            if (tile != null && tile.isVisible() && tile.hasPests()) {
                tile.applyPesticide();
            }
        }
    }
    
    /**
     * Gets the tile at a specific position.
     */
    public AnimatedTile getTileAt(int row, int col) {
        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            return tiles[row][col];
        }
        return null;
    }

    public void setSelectedPlant(Position position) {
        selectedPlantPosition = position;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                if (tiles[row][col] != null) {
                    tiles[row][col].setSelected(position != null && position.row() == row && position.column() == col);
                }
            }
        }
    }

    private void selectPlant(Position position) {
        setSelectedPlant(position);
        if (plantSelectionHandler != null) {
            plantSelectionHandler.accept(position);
        }
    }
}
