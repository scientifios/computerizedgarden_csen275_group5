package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.util.Duration;

/**
 * Enhanced button with bounce and glow effects.
 * Gardenscapes-style playful button interactions.
 */
public class BounceButton extends Button {
    
    public BounceButton(String text) {
        super(text);
        setupButton();
    }
    
    /**
     * Sets up button with bounce and glow effects.
     */
    private void setupButton() {
        // Initial style
        this.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #81C784 0%, #66BB6A 100%); " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Comic Sans MS'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
        
        // Hover effect with bounce
        this.setOnMouseEntered(e -> {
            // Glow effect
            this.setEffect(new Glow(0.5));
            this.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #A5D6A7 0%, #81C784 100%); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Comic Sans MS'; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.5), 8, 0.8, 0, 0);"
            );
            
            // Bounce up
            TranslateTransition bounce = new TranslateTransition(Duration.millis(150), this);
            bounce.setFromY(0);
            bounce.setToY(-3);
            bounce.setAutoReverse(true);
            bounce.setCycleCount(2);
            bounce.setInterpolator(Interpolator.EASE_OUT);
            bounce.play();
        });
        
        // Exit hover
        this.setOnMouseExited(e -> {
            this.setEffect(null);
            this.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #81C784 0%, #66BB6A 100%); " +
                "-fx-text-fill: white; " +
                "-fx-font-family: 'Comic Sans MS'; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
            );
            
            TranslateTransition returnTo = new TranslateTransition(Duration.millis(150), this);
            returnTo.setFromY(this.getTranslateY());
            returnTo.setToY(0);
            returnTo.play();
        });
        
        // Click effect - bigger bounce
        this.setOnMousePressed(e -> {
            ScaleTransition press = new ScaleTransition(Duration.millis(100), this);
            press.setFromX(1.0);
            press.setFromY(1.0);
            press.setToX(0.95);
            press.setToY(0.95);
            press.play();
        });
        
        this.setOnMouseReleased(e -> {
            ScaleTransition release = new ScaleTransition(Duration.millis(150), this);
            release.setFromX(this.getScaleX());
            release.setFromY(this.getScaleY());
            release.setToX(1.0);
            release.setToY(1.0);
            release.setInterpolator(Interpolator.EASE_OUT);
            release.play();
        });
    }
    
    /**
     * Triggers a success bounce animation.
     */
    public void animateSuccess() {
        ScaleTransition success = new ScaleTransition(Duration.millis(200), this);
        success.setFromX(1.0);
        success.setFromY(1.0);
        success.setToX(1.2);
        success.setToY(1.2);
        success.setAutoReverse(true);
        success.setCycleCount(2);
        success.setInterpolator(Interpolator.EASE_OUT);
        success.play();
    }
}

