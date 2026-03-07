package edu.scu.csen275.smartgarden.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Side panel that displays simulation status and resource levels.
 */
public class InfoPanel extends VBox {
    private final InfoCard simulationCard;
    private final InfoCard resourcesCard;

    private final Label timeLabel;
    private final Label statsLabel;
    private final Label heatingStatusLabel;
    private final Label temperatureLabel;
    private final WeatherDisplay weatherDisplay;
    private final ProgressBar waterBar;
    private final ProgressBar pesticideBar;

    public InfoPanel() {
        this.setSpacing(25);
        this.setPadding(new Insets(20));
        this.setMinWidth(320);
        this.getStyleClass().add("info-panel");

        simulationCard = new InfoCard("Simulation Info");
        timeLabel = simulationCard.addLabel("Time: --", false);
        statsLabel = simulationCard.addLabel("Plants: 0", false);
        heatingStatusLabel = simulationCard.addLabel("Heating: Off", false);
        temperatureLabel = simulationCard.addLabel("Current: 20C", false);
        weatherDisplay = new WeatherDisplay();
        simulationCard.getChildren().add(weatherDisplay);

        resourcesCard = new InfoCard("Resources");
        resourcesCard.setTitleColor("#1E88E5");
        waterBar = resourcesCard.addProgressBar("Water Supply", 1.0);
        pesticideBar = resourcesCard.addProgressBar("Pesticide Stock", 1.0);

        this.getChildren().addAll(simulationCard, resourcesCard);
    }

    /**
     * Updates the resource progress bars to the given values.
     */
    public void updateProgressBars(double waterProgress, double pesticideProgress) {
        animateProgressBar(waterBar, waterProgress);
        animateProgressBar(pesticideBar, pesticideProgress);
    }

    /**
     * Transitions a progress bar to the target value.
     */
    private void animateProgressBar(ProgressBar bar, double targetValue) {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(300),
                e -> bar.setProgress(targetValue)
            )
        );
        timeline.play();
    }

    public Label getTimeLabel() { return timeLabel; }
    public Label getStatsLabel() { return statsLabel; }
    public Label getHeatingStatusLabel() { return heatingStatusLabel; }
    public Label getTemperatureLabel() { return temperatureLabel; }
    public WeatherDisplay getWeatherDisplay() { return weatherDisplay; }
    public ProgressBar getWaterBar() { return waterBar; }
    public ProgressBar getPesticideBar() { return pesticideBar; }
}
