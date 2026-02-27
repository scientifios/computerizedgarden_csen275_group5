package edu.scu.csen275.smartgarden.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Modern info panel with card-based layout.
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
        
        // Simulation Info Card
        simulationCard = new InfoCard("📊 Simulation Info");
        timeLabel = simulationCard.addLabel("Time: --", false);
        statsLabel = simulationCard.addLabel("Plants: 0", false);
        heatingStatusLabel = simulationCard.addLabel("🔥 Heating: Off", false);
        temperatureLabel = simulationCard.addLabel("🌡️ Current: 20°C", false);
        weatherDisplay = new WeatherDisplay();
        simulationCard.getChildren().add(weatherDisplay);
        
        // Resources Card
        resourcesCard = new InfoCard("💧 Resources");
        waterBar = resourcesCard.addProgressBar("Water Supply", 1.0);
        pesticideBar = resourcesCard.addProgressBar("Pesticide Stock", 1.0);
        
        // Add all cards
        this.getChildren().addAll(simulationCard, resourcesCard);
    }
    
    /**
     * Updates progress bars with animation.
     */
    public void updateProgressBars(double waterProgress, double pesticideProgress) {
        animateProgressBar(waterBar, waterProgress);
        animateProgressBar(pesticideBar, pesticideProgress);
        
        // Update styles based on values
        InfoCard.updateProgressBarStyle(waterBar, waterProgress);
        InfoCard.updateProgressBarStyle(pesticideBar, pesticideProgress);
    }
    
    /**
     * Animates progress bar to new value smoothly.
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
    
    // Getters
    public Label getTimeLabel() { return timeLabel; }
    public Label getStatsLabel() { return statsLabel; }
    public Label getHeatingStatusLabel() { return heatingStatusLabel; }
    public Label getTemperatureLabel() { return temperatureLabel; }
    public WeatherDisplay getWeatherDisplay() { return weatherDisplay; }
    public ProgressBar getWaterBar() { return waterBar; }
    public ProgressBar getPesticideBar() { return pesticideBar; }
}

