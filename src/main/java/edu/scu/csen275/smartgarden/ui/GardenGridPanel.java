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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.List;
import java.util.function.Consumer;

/**
 * UI panel that renders the garden grid and routes user interactions to the controller.
 */
public class GardenGridPanel extends VBox {
    private final GardenController controller;
    private final GridPane gardenGrid;
    private final AnimatedTile[][] tiles;
    private final GrassTile[][] grassTiles;
    private ComboBox<PlantType> plantSelector;
    private Pane animationContainer; // Container for watering animations
    private Pane coinFloatPane; // Pane for coin float animations
    private Position selectedPlantPosition;
    private Consumer<Position> plantSelectionHandler;
    private static final int GRID_SIZE = 9;
    
    /**
     * Returns the overlay pane used for transient UI effects.
     */
    public Pane getAnimationContainer() {
        return animationContainer;
    }
    
    /**
     * Sets the pane used for coin/score overlay effects.
     */
    public void setCoinFloatPane(Pane pane) {
        this.coinFloatPane = pane;
    }

    public void setPlantSelectionHandler(Consumer<Position> plantSelectionHandler) {
        this.plantSelectionHandler = plantSelectionHandler;
    }
    
    /**
     * Applies an effect when the node is ready (scene attached and bounds available).
     */
    private void safeSetEffect(javafx.scene.Node node, javafx.scene.effect.Effect effect) {
        if (node.getScene() != null && node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
            node.setEffect(effect);
        } else {
            node.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
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
     * Walks the scene graph to locate a ParticleSystem and emit a burst at (x, y).
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
        this.tiles = new AnimatedTile[GRID_SIZE][GRID_SIZE];
        this.grassTiles = new GrassTile[GRID_SIZE][GRID_SIZE];
        this.gardenGrid = new GridPane();
        
        setupPanel();
        setupPlantSelector();
        setupGrid();
    }
    
    /**
     * Sets the overlay pane used for tile-level visual hints and effects.
     */
    public void setAnimationContainer(Pane container) {
        this.animationContainer = container;
        
        // Also set animation container on all existing tiles
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (tiles[row][col] != null) {
                    tiles[row][col].setAnimationContainer(container);
                }
            }
        }
    }
    
    /**
     * Initializes layout and style classes for the panel.
     */
    private void setupPanel() {
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("garden-panel");
    }
    
    /**
     * Builds the plant selection controls and configures the ComboBox rendering.
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
        
        // Fruits
        plantSelector.getItems().add(PlantType.STRAWBERRY);
        plantSelector.getItems().add(PlantType.GRAPEVINE);
        plantSelector.getItems().add(PlantType.APPLE);
        
        // Vegetable
        plantSelector.getItems().add(PlantType.CARROT);
        plantSelector.getItems().add(PlantType.TOMATO);
        plantSelector.getItems().add(PlantType.ONION);
        
        // Flowers
        plantSelector.getItems().add(PlantType.SUNFLOWER);
        plantSelector.getItems().add(PlantType.TULIP);
        plantSelector.getItems().add(PlantType.ROSE);
        
        // Set default value
        plantSelector.setValue(PlantType.STRAWBERRY);
        
        plantSelector.setCellFactory(list -> new ListCell<PlantType>() {
            @Override
            protected void updateItem(PlantType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    System.out.println("DEBUG: Rendering dropdown item: " + item.getDisplayName() + " emoji: " + item.getEmoji());
                    System.out.println("DEBUG: Emoji character code: " + (int)item.getEmoji().charAt(0));
                    
                    HBox cellContent = new HBox(5);
                    cellContent.setAlignment(Pos.CENTER_LEFT);
                    
                    Text emojiText = new Text(item.getEmoji());
                    System.out.println("DEBUG: Emoji string length: " + item.getEmoji().length() + ", chars: " + item.getEmoji().codePointCount(0, item.getEmoji().length()));
                    System.out.println("DEBUG: Full emoji string: " + java.util.Arrays.toString(item.getEmoji().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                    
                    try {
                        Font emojiFont = Font.font("Segoe UI Emoji", 20);
                        emojiText.setFont(emojiFont);
                        System.out.println("DEBUG: Successfully loaded Segoe UI Emoji font");
                    } catch (Exception e) {
                        try {
                            Font emojiFont = Font.font("Apple Color Emoji", 20);
                            emojiText.setFont(emojiFont);
                            System.out.println("DEBUG: Successfully loaded Apple Color Emoji font");
                        } catch (Exception e2) {
                            emojiText.setFont(Font.font(20));
                            System.out.println("DEBUG: Using fallback font");
                        }
                    }
                    
                    Label nameLabel = new Label(item.getDisplayName());
                    nameLabel.getStyleClass().add("plant-name-label");
                    
                    cellContent.getChildren().add(nameLabel);
                    setGraphic(cellContent);
                    setText(null);
                    if (!getStyleClass().contains("plant-selector-cell")) getStyleClass().add("plant-selector-cell");
                }
            }
        });
        
        plantSelector.setButtonCell(new ListCell<PlantType>() {
            @Override
            protected void updateItem(PlantType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Plant");
                    setGraphic(null);
                } else {
                    System.out.println("DEBUG: Rendering button cell: " + item.getDisplayName() + " emoji: " + item.getEmoji());
                    System.out.println("DEBUG: Button cell - Emoji character code: " + (int)item.getEmoji().charAt(0));
                    
                    HBox buttonContent = new HBox(5);
                    buttonContent.setAlignment(Pos.CENTER_LEFT);
                    
                    Text emojiText = new Text(item.getEmoji());
                    System.out.println("DEBUG: Button cell - Emoji string length: " + item.getEmoji().length() + ", chars: " + item.getEmoji().codePointCount(0, item.getEmoji().length()));
                    
                    try {
                        Font emojiFont = Font.font("Segoe UI Emoji", 18);
                        emojiText.setFont(emojiFont);
                        System.out.println("DEBUG: Button cell - Successfully loaded Segoe UI Emoji font");
                    } catch (Exception e) {
                        try {
                            Font emojiFont = Font.font("Apple Color Emoji", 18);
                            emojiText.setFont(emojiFont);
                            System.out.println("DEBUG: Button cell - Successfully loaded Apple Color Emoji font");
                        } catch (Exception e2) {
                            emojiText.setFont(Font.font(18));
                            System.out.println("DEBUG: Button cell - Using fallback font");
                        }
                    }
                    
                    StackPane emojiWrapper = new StackPane(emojiText);
                    emojiWrapper.setAlignment(Pos.CENTER);
                    emojiWrapper.getStyleClass().add("transparent-bg");
                    
                    Label nameLabel = new Label(item.getDisplayName());
                    nameLabel.getStyleClass().add("plant-name-label");
                    
                    buttonContent.getChildren().addAll(emojiWrapper, nameLabel);
                    setGraphic(buttonContent);
                    setText(null);
                    if (!getStyleClass().contains("combo-cell-transparent-text")) getStyleClass().add("combo-cell-transparent-text");
                }
            }
        });
        
        plantSelector.setVisibleRowCount(10);

        plantSelector.getStyleClass().add("modern-combo");
        plantSelector.setPrefWidth(200);
        
        javafx.scene.control.Button clearBtn = new javafx.scene.control.Button("Clear All");
        clearBtn.getStyleClass().add("modern-button");
        clearBtn.setOnAction(e -> clearGarden());
        
        selectorBox.getChildren().addAll(selectLabel, plantSelector, clearBtn);
        this.getChildren().add(selectorBox);
    }
    
    /**
     * Creates the grid layout and initializes per-cell UI nodes.
     */
    private void setupGrid() {
        gardenGrid.setHgap(3);
        gardenGrid.setVgap(3);
        gardenGrid.getStyleClass().add("garden-grid");
        gardenGrid.setAlignment(Pos.CENTER);
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                GrassTile grassTile = new GrassTile();
                grassTiles[row][col] = grassTile;
                
                AnimatedTile tile = createTile(row, col);
                int tileIndex = (row * GRID_SIZE + col);
                tile.setTileIndex(tileIndex);
                tiles[row][col] = tile;
                tile.setVisible(false);
                
                StackPane tileStack = new StackPane();
                tileStack.getChildren().addAll(grassTile, tile);
                tileStack.setAlignment(javafx.geometry.Pos.CENTER);
                
                gardenGrid.add(tileStack, col, row);
            }
        }
        
        this.getChildren().add(gardenGrid);
    }
    
    /**
     * Creates and wires UI behavior for a single grid cell.
     */
    private AnimatedTile createTile(int row, int col) {
        AnimatedTile tile = new AnimatedTile();
        Position position = new Position(row, col);
        GrassTile grassTile = grassTiles[row][col];
        
        if (animationContainer != null) {
            tile.setAnimationContainer(animationContainer);
        }
        
        grassTile.setOnMouseClicked(e -> {
            Plant existing = controller.getGarden().getPlant(position);
            
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
                    updateTile(row, col);
                    
                    if (grassTile.hasFlower()) {
                        grassTile.floatPetals();
                    }
                }
            } else {
                showPlantTooltip(tile, existing);
            }
        });
        
        tile.setOnMouseClicked(e -> {
            Plant existing = controller.getGarden().getPlant(position);
            if (existing != null) {
                selectPlant(position);
                showPlantTooltip(tile, existing);
            }
        });
        
        grassTile.setOnMouseEntered(e -> {
            if (tile.isVisible()) {
                tile.applyHoverEffect();
                Plant plant = controller.getGarden().getPlant(position);
                if (plant != null) {
                    showHoverTooltip(tile, plant);
                }
            } else {
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
     * Installs a hover tooltip describing the plant state.
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
     * Opens a detail dialog for the selected plant.
     */
    private void showPlantTooltip(AnimatedTile tile, Plant plant) {
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
     * Refreshes UI for a single grid cell based on current model state.
     */
    public void updateTile(int row, int col) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            Position position = new Position(row, col);
            Plant plant = controller.getGarden().getPlant(position);
            
            if (plant == null) {
                grassTiles[row][col].setVisible(true);
                tiles[row][col].setVisible(false);
            } else {
                grassTiles[row][col].setVisible(false);
                grassTiles[row][col].removeFlower();
                tiles[row][col].setVisible(true);
                tiles[row][col].update(plant);
                tiles[row][col].setSelected(position.equals(selectedPlantPosition));
            }
        }
    }
    
    /**
     * Refreshes the entire grid UI.
     */
    public void updateAllTiles() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                updateTile(row, col);
            }
        }
    }
    
    /**
     * Applies a brief water hint to plants within the given zone and refreshes tiles.
     */
    public void animateWatering(int zoneId) {
        int zoneRow = (zoneId - 1) / 3;
        int zoneCol = (zoneId - 1) % 3;
        int tilesPerZone = 3;
        
        int startRow = zoneRow * tilesPerZone;
        int endRow = startRow + tilesPerZone;
        int startCol = zoneCol * tilesPerZone;
        int endCol = startCol + tilesPerZone;
        
        for (int row = startRow; row < endRow && row < GRID_SIZE; row++) {
            for (int col = startCol; col < endCol && col < GRID_SIZE; col++) {
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
     * Applies a brief water hint to all plants and refreshes tiles.
     */
    public void animateAllTilesWatering() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
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
     * Triggers pesticide-related visual updates for tiles in the given zone.
     */
    public void animatePesticide(int zoneId) {
        int zoneRow = (zoneId - 1) / 3;
        int zoneCol = (zoneId - 1) % 3;
        int tilesPerZone = 3;
        
        int startRow = zoneRow * tilesPerZone;
        int endRow = startRow + tilesPerZone;
        int startCol = zoneCol * tilesPerZone;
        int endCol = startCol + tilesPerZone;
        
        for (int row = startRow; row < endRow && row < GRID_SIZE; row++) {
            for (int col = startCol; col < endCol && col < GRID_SIZE; col++) {
                if (tiles[row][col] != null) {
                    tiles[row][col].animatePesticide();
                }
            }
        }
    }
    
    /**
     * Removes all plants from the model and refreshes the grid.
     */
    private void clearGarden() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                controller.removePlant(new Position(row, col));
            }
        }
        updateAllTiles();
    }
    
    public GridPane getGardenGrid() {
        return gardenGrid;
    }
        
    /**
     * Updates the tile state in response to a pest spawn event.
     */
    public void onPestSpawned(Position position, String pestType, boolean isHarmful) {
        if (position.row() >= 0 && position.row() < GRID_SIZE && 
            position.column() >= 0 && position.column() < GRID_SIZE) {
            
            AnimatedTile tile = tiles[position.row()][position.column()];
            
            if (tile != null && tile.isVisible()) {
                if (isHarmful) {
                    tile.spawnPest(pestType);
                }
            } else {
                System.err.println("[GardenGridPanel] ERROR: Cannot spawn pest - tile is null or not visible");
            }
        } else {
            System.err.println("[GardenGridPanel] ERROR: Invalid position: (" + position.row() + ", " + position.column() + ")");
        }
    }
    
    /**
     * Updates the affected tile in response to a pest attack event.
     */
    public void onPestAttack(Position position, int damage) {
        if (position.row() >= 0 && position.row() < GRID_SIZE && 
            position.column() >= 0 && position.column() < GRID_SIZE) {
            
            AnimatedTile tile = tiles[position.row()][position.column()];
            if (tile != null && tile.isVisible()) {
                tile.showDamageVisual(damage);
            }
        }
    }
    
    /**
     * Clears pest state for plants in the given zone and refreshes affected tiles.
     */
    public void onPesticideApplied(edu.scu.csen275.smartgarden.model.Zone zone) {
        for (var plant : zone.getPlants()) {
            if (!plant.isDead()) {
                Position pos = plant.getPosition();
                if (pos.row() >= 0 && pos.row() < GRID_SIZE && 
                    pos.column() >= 0 && pos.column() < GRID_SIZE) {
                    
                    AnimatedTile tile = tiles[pos.row()][pos.column()];
                    if (tile != null && tile.isVisible() && tile.hasPests()) {
                        tile.applyPesticide();
                    }
                }
            }
        }
    }
    
    /**
     * Clears pest state for the tile at the given position, if applicable.
     */
    public void onPesticideApplied(Position position) {
        if (position.row() >= 0 && position.row() < GRID_SIZE && 
            position.column() >= 0 && position.column() < GRID_SIZE) {
            
            AnimatedTile tile = tiles[position.row()][position.column()];
            if (tile != null && tile.isVisible() && tile.hasPests()) {
                tile.applyPesticide();
            }
        }
    }
    
    /**
     * Returns the tile instance for the given grid coordinates, or null if out of range.
     */
    public AnimatedTile getTileAt(int row, int col) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            return tiles[row][col];
        }
        return null;
    }

    public void setSelectedPlant(Position position) {
        selectedPlantPosition = position;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
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
