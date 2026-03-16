package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.PlantType;
import edu.scu.csen275.smartgarden.model.Position;
import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ProgressBar;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sidebar panel used for either the left system overview or the right garden info panel.
 */
public class InfoPanel extends VBox {
    public enum Mode {
        LEFT_SYSTEMS,
        RIGHT_GARDEN
    }

    private static final int WATER_CAPACITY = 10000;
    private static final int PESTICIDE_CAPACITY = 50;

    private final Mode mode;
    private final Map<String, Image> imageCache = new HashMap<>();

    private Consumer<Position> plantSelectionHandler;
    private Position selectedPlantPosition;

    private Label weatherLabel;
    private Label ambientTemperatureLabel;
    private Label legacyTimeLabel;
    private Label legacyStatsLabel;
    private Label legacyHeatingStatusLabel;
    private Label legacyTemperatureLabel;
    private WeatherDisplay legacyWeatherDisplay;
    private ProgressBar legacyWaterBar;
    private ProgressBar legacyPesticideBar;

    private Label dryPlantsLabel;
    private Label sprinklerStatusLabel;
    private Label sensorStatusLabel;
    private Label waterStockLabel;

    private Label heatingStatusLabel;
    private Label coolingStatusLabel;
    private Label gardenTemperatureLabel;
    private Label temperatureTrendLabel;

    private Label pestCountLabel;
    private Label criticalThreatLabel;
    private Label pesticideStockLabel;

    private Label dayLabel;
    private Label timeLabel;
    private Label inventoryLabel;
    private VBox plantListBox;
    private ScrollPane plantScrollPane;
    private Label emptyGardenLabel;

    public InfoPanel(Mode mode) {
        this.mode = mode;
        this.setSpacing(18);
        this.setPadding(new Insets(20));
        this.getStyleClass().add("info-panel");

        if (mode == Mode.LEFT_SYSTEMS) {
            this.setPrefWidth(360);
            buildLeftSystemsPanel();
        } else {
            this.setPrefWidth(420);
            buildRightGardenPanel();
        }
    }

    private void buildLeftSystemsPanel() {
        InfoCard weatherCard = new InfoCard("Weather");
        legacyTimeLabel = weatherCard.addLabel("", false);
        legacyTimeLabel.setManaged(false);
        legacyTimeLabel.setVisible(false);
        legacyStatsLabel = weatherCard.addLabel("", false);
        legacyStatsLabel.setManaged(false);
        legacyStatsLabel.setVisible(false);
        weatherLabel = weatherCard.addLabel("Weather: --", false);
        ambientTemperatureLabel = weatherCard.addLabel("Ambient Temperature: --", false);
        legacyWeatherDisplay = new WeatherDisplay();
        legacyWeatherDisplay.setManaged(false);
        legacyWeatherDisplay.setVisible(false);
        weatherCard.getChildren().add(legacyWeatherDisplay);

        InfoCard wateringCard = new InfoCard("Watering System");
        dryPlantsLabel = wateringCard.addLabel("Plants needing water: 0", false);
        sprinklerStatusLabel = wateringCard.addLabel("Sprinkler activations: 0", false);
        sensorStatusLabel = wateringCard.addLabel("Sensors: --", false);
        waterStockLabel = wateringCard.addLabel("Water stock: 0 / " + WATER_CAPACITY + "L", false);

        InfoCard temperatureCard = new InfoCard("Temperature System");
        heatingStatusLabel = temperatureCard.addLabel("Heating system: Off", false);
        coolingStatusLabel = temperatureCard.addLabel("Cooling system: Off", false);
        gardenTemperatureLabel = temperatureCard.addLabel("Garden temperature: --", false);
        temperatureTrendLabel = temperatureCard.addLabel("Temperature trend: --", false);
        legacyHeatingStatusLabel = heatingStatusLabel;
        legacyTemperatureLabel = gardenTemperatureLabel;

        InfoCard pestCard = new InfoCard("Pesting System");
        pestCountLabel = pestCard.addLabel("Current pests: 0", false);
        criticalThreatLabel = pestCard.addLabel("Critical threat tiles: None", false);
        pesticideStockLabel = pestCard.addLabel("Pesticide stock: 0 / " + PESTICIDE_CAPACITY, false);
        legacyWaterBar = new ProgressBar(1.0);
        legacyPesticideBar = new ProgressBar(1.0);

        this.getChildren().addAll(weatherCard, wateringCard, temperatureCard, pestCard);
    }

    private void buildRightGardenPanel() {
        this.getStyleClass().add("garden-info-panel");

        InfoCard gardenCard = new InfoCard("Garden Info");
        dayLabel = gardenCard.addLabel("Day --", true);
        timeLabel = gardenCard.addLabel("Time: --", false);
        inventoryLabel = gardenCard.addLabel("Stored plants: 0 | Planted plants: 0", false);

        plantListBox = new VBox(12);
        plantListBox.getStyleClass().add("plant-list-box");
        emptyGardenLabel = new Label("No plants in the garden.");
        emptyGardenLabel.getStyleClass().add("plant-card-empty");
        plantListBox.getChildren().add(emptyGardenLabel);

        plantScrollPane = new ScrollPane(plantListBox);
        plantScrollPane.setFitToWidth(true);
        plantScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        plantScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        plantScrollPane.setPannable(true);
        plantScrollPane.getStyleClass().add("garden-info-scroll");

        VBox.setVgrow(plantScrollPane, Priority.ALWAYS);
        gardenCard.getChildren().add(plantScrollPane);
        VBox.setVgrow(gardenCard, Priority.ALWAYS);

        this.getChildren().add(gardenCard);
    }

    public void setPlantSelectionHandler(Consumer<Position> plantSelectionHandler) {
        this.plantSelectionHandler = plantSelectionHandler;
    }

    public void updateLeftPanel(
        WeatherSystem.Weather weather,
        int ambientTemperature,
        int plantsNeedingWater,
        String sprinklerSummary,
        String sensorSummary,
        int waterStock,
        String heatingSummary,
        String coolingSummary,
        int gardenTemperature,
        String temperatureTrend,
        int pestCount,
        String criticalThreatSummary,
        int pesticideStock
    ) {
        if (mode != Mode.LEFT_SYSTEMS) {
            return;
        }

        weatherLabel.setText("Weather: " + weather.getDisplayName());
        ambientTemperatureLabel.setText("Ambient Temperature: " + ambientTemperature + "C");

        dryPlantsLabel.setText("Plants needing water: " + plantsNeedingWater);
        sprinklerStatusLabel.setText("Sprinkler activations: " + sprinklerSummary);
        sensorStatusLabel.setText("Sensors: " + sensorSummary);
        waterStockLabel.setText("Water stock: " + waterStock + " / " + WATER_CAPACITY + "L");

        heatingStatusLabel.setText("Heating system: " + heatingSummary);
        coolingStatusLabel.setText("Cooling system: " + coolingSummary);
        gardenTemperatureLabel.setText("Garden temperature: " + gardenTemperature + "C");
        temperatureTrendLabel.setText("Temperature trend: " + temperatureTrend);

        pestCountLabel.setText("Current pests: " + pestCount);
        criticalThreatLabel.setText("Critical threat tiles: " + criticalThreatSummary);
        pesticideStockLabel.setText("Pesticide stock: " + pesticideStock + " / " + PESTICIDE_CAPACITY);
    }

    public void updateGardenInfo(
        int dayCount,
        String formattedTime,
        int storedPlants,
        List<Plant> plants,
        Map<Position, Integer> activePestsByPosition
    ) {
        if (mode != Mode.RIGHT_GARDEN) {
            return;
        }

        dayLabel.setText("Day " + dayCount);
        timeLabel.setText("Time: " + formattedTime);
        inventoryLabel.setText("Stored plants: " + storedPlants + " | Planted plants: " + plants.size());

        List<Plant> sortedPlants = plants.stream()
            .sorted(Comparator
                .comparingInt((Plant p) -> p.getPosition().row())
                .thenComparingInt(p -> p.getPosition().column()))
            .toList();

        plantListBox.getChildren().clear();
        if (sortedPlants.isEmpty()) {
            plantListBox.getChildren().add(emptyGardenLabel);
            return;
        }

        for (Plant plant : sortedPlants) {
            plantListBox.getChildren().add(createPlantCard(plant, activePestsByPosition.getOrDefault(plant.getPosition(), 0)));
        }
    }

    private VBox createPlantCard(Plant plant, int activePests) {
        VBox card = new VBox(8);
        card.getStyleClass().add("plant-card");
        if (plant.getPosition().equals(selectedPlantPosition)) {
            card.getStyleClass().add("plant-card-selected");
        }

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView plantImage = new ImageView(resolvePlantImage(plant));
        plantImage.setFitWidth(28);
        plantImage.setFitHeight(28);
        plantImage.setPreserveRatio(true);

        VBox titleBox = new VBox(2);
        Label nameLabel = new Label(plant.getPlantType());
        nameLabel.getStyleClass().add("plant-card-title");
        Label positionLabel = new Label("Tile " + formatPosition(plant.getPosition()));
        positionLabel.getStyleClass().add("plant-card-subtitle");
        titleBox.getChildren().addAll(nameLabel, positionLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label statusLabel = new Label(plant.isDead() ? "Dead" : plant.getHealthStatus());
        statusLabel.getStyleClass().add("plant-status-chip");

        header.getChildren().addAll(plantImage, titleBox, spacer, statusLabel);

        VBox metrics = new VBox(4);
        metrics.getChildren().addAll(
            createMetricLabel("Growth Stage", plant.getGrowthStage().getDisplayName()),
            createMetricLabel("Water Level", plant.getWaterLevel() + "%"),
            createMetricLabel("Days Alive", String.valueOf(plant.getDaysAlive())),
            createMetricLabel("Total Pest Attacks", String.valueOf(plant.getTotalPestAttacks())),
            createMetricLabel("Active Pests", String.valueOf(activePests)),
            createMetricLabel("Status", plant.getHealthStatus())
        );

        card.getChildren().addAll(header, metrics);
        card.setOnMouseClicked(e -> {
            selectedPlantPosition = plant.getPosition();
            if (plantSelectionHandler != null) {
                plantSelectionHandler.accept(plant.getPosition());
            }
        });
        return card;
    }

    private Label createMetricLabel(String label, String value) {
        Label metric = new Label(label + ": " + value);
        metric.getStyleClass().add("plant-card-metric");
        return metric;
    }

    public void setSelectedPlant(Position position) {
        this.selectedPlantPosition = position;
    }

    private String formatPosition(Position position) {
        return "(" + (position.row() + 1) + "," + (position.column() + 1) + ")";
    }

    private Image resolvePlantImage(Plant plant) {
        String resourcePath = resolvePlantImagePath(plant);
        return imageCache.computeIfAbsent(resourcePath, path ->
            new Image(getClass().getResourceAsStream(path), 28, 28, true, true)
        );
    }

    private String resolvePlantImagePath(Plant plant) {
        String plantTypeName = plant.getPlantType().toLowerCase();
        if (plantTypeName.contains("strawberry")) return "/images/strawberry.png";
        if (plantTypeName.contains("grape")) return "/images/grape.png";
        if (plantTypeName.contains("apple")) return "/images/apple.png";
        if (plantTypeName.contains("carrot")) return "/images/carrot.png";
        if (plantTypeName.contains("tomato")) return "/images/tomato.png";
        if (plantTypeName.contains("onion")) return "/images/onion.png";
        if (plantTypeName.contains("sunflower")) return "/images/sunflower.png";
        if (plantTypeName.contains("tulip")) return "/images/tulip.png";
        if (plantTypeName.contains("rose")) return "/images/rose.png";

        for (PlantType type : PlantType.values()) {
            if (plant.getPlantType().equalsIgnoreCase(type.getDisplayName())) {
                return switch (type) {
                    case STRAWBERRY -> "/images/strawberry.png";
                    case GRAPEVINE -> "/images/grape.png";
                    case APPLE -> "/images/apple.png";
                    case CARROT -> "/images/carrot.png";
                    case TOMATO -> "/images/tomato.png";
                    case ONION -> "/images/onion.png";
                    case SUNFLOWER -> "/images/sunflower.png";
                    case TULIP -> "/images/tulip.png";
                    case ROSE -> "/images/rose.png";
                };
            }
        }

        return "/images/strawberry.png";
    }

    // Legacy API kept so existing application update code can continue compiling.
    public Label getTimeLabel() { return legacyTimeLabel; }
    public Label getStatsLabel() { return legacyStatsLabel; }
    public Label getHeatingStatusLabel() { return legacyHeatingStatusLabel; }
    public Label getTemperatureLabel() { return legacyTemperatureLabel; }
    public WeatherDisplay getWeatherDisplay() { return legacyWeatherDisplay; }
    public ProgressBar getWaterBar() { return legacyWaterBar; }
    public ProgressBar getPesticideBar() { return legacyPesticideBar; }
    public void updateProgressBars(double waterProgress, double pesticideProgress) {}
}
