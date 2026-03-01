package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Static weather display component.
 */
public class WeatherDisplay extends HBox {
    private final Label weatherLabel;
    private final Label weatherIcon;
    private WeatherSystem.Weather currentWeather;
    
    public WeatherDisplay() {
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("weather-display");
        
        weatherIcon = new Label("☀");
        weatherIcon.setFont(javafx.scene.text.Font.font(40)); // Larger sun icon
        
        weatherLabel = new Label("Weather: Sunny");
        weatherLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        this.getChildren().addAll(weatherIcon, weatherLabel);
    }
    
    /**
     * Updates weather display based on current weather.
     */
    public void updateWeather(WeatherSystem.Weather weather) {
        this.currentWeather = weather;
        String icon = weather.getIcon();
        String text = weather.getDisplayName();
        
        weatherIcon.setText(icon);
        weatherLabel.setText("Weather: " + text);
        weatherIcon.setStyle("");
    }
    
    /**
     * Gets the current weather.
     */
    public WeatherSystem.Weather getCurrentWeather() {
        return currentWeather != null ? currentWeather : WeatherSystem.Weather.SUNNY;
    }
}

