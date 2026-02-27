package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Animated weather display component.
 */
public class WeatherDisplay extends HBox {
    private final Label weatherLabel;
    private final Label weatherIcon;
    private RotateTransition rotateAnimation;
    private ScaleTransition pulseAnimation;
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
        
        setupAnimations();
    }
    
    /**
     * Sets up weather animations.
     */
    private void setupAnimations() {
        // Pulse animation for weather icon
        pulseAnimation = new ScaleTransition(Duration.seconds(2), weatherIcon);
        pulseAnimation.setFromX(1.0);
        pulseAnimation.setFromY(1.0);
        pulseAnimation.setToX(1.1);
        pulseAnimation.setToY(1.1);
        pulseAnimation.setAutoReverse(true);
        pulseAnimation.setCycleCount(Animation.INDEFINITE);
        pulseAnimation.play(); // Start default animation
        
        // Rotate animation for wind/rain
        rotateAnimation = new RotateTransition(Duration.seconds(3), weatherIcon);
        rotateAnimation.setCycleCount(Animation.INDEFINITE);
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
        
        // Adjust animations based on weather type
        switch (weather) {
            case SUNNY:
                // Bright sun with glowing pulse animation
                weatherIcon.setStyle("-fx-text-fill: #FFD700; -fx-effect: dropshadow(gaussian, #FFD700, 20, 0.8, 0, 0);");
                pulseAnimation.setDuration(Duration.seconds(1.5));
                pulseAnimation.setToX(1.15); // Slightly larger pulse for sun
                pulseAnimation.setToY(1.15);
                pulseAnimation.play();
                rotateAnimation.stop();
                break;
            case WINDY:
                weatherIcon.setStyle(""); // Reset style
                rotateAnimation.setFromAngle(0);
                rotateAnimation.setToAngle(360);
                rotateAnimation.play();
                pulseAnimation.stop();
                break;
            case RAINY:
                weatherIcon.setStyle(""); // Reset style
                pulseAnimation.setDuration(Duration.seconds(0.5));
                pulseAnimation.play();
                rotateAnimation.stop();
                break;
            case SNOWY:
                weatherIcon.setStyle(""); // Reset style
                rotateAnimation.setFromAngle(-5);
                rotateAnimation.setToAngle(5);
                rotateAnimation.setDuration(Duration.seconds(1));
                rotateAnimation.play();
                pulseAnimation.stop();
                break;
            default:
                weatherIcon.setStyle(""); // Reset style
                pulseAnimation.setDuration(Duration.seconds(2));
                pulseAnimation.play();
                rotateAnimation.stop();
                break;
        }
    }
    
    /**
     * Gets the current weather.
     */
    public WeatherSystem.Weather getCurrentWeather() {
        return currentWeather != null ? currentWeather : WeatherSystem.Weather.SUNNY;
    }
}

