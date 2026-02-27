package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Floating text animation for damage and healing indicators.
 * Shows "-2 HP", "+3 health", "Plant Saved!" etc.
 */
public class DamageTextAnimation {
    private static final Duration ANIMATION_DURATION = Duration.millis(2500); // Increased from 1500ms to 2500ms for better visibility
    
    /**
     * Creates floating damage text (e.g., "-2 HP").
     */
    public static void createDamageText(Pane container, double x, double y, int damage) {
        createText(container, "-" + damage + " HP", Color.rgb(255, 51, 51), x, y);
    }
    
    /**
     * Creates floating healing text (e.g., "+3 health").
     */
    public static void createHealingText(Pane container, double x, double y, int healing) {
        createText(container, "+" + healing + " health", Color.rgb(51, 255, 51), x, y);
    }
    
    /**
     * Creates custom floating text (e.g., "Plant Saved!").
     */
    public static void createText(Pane container, String text, Color color, double x, double y) {
        Label textLabel = new Label(text);
        textLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + toHexString(color) + "; " +
            "-fx-background-color: transparent;"
        );
        
        // Drop shadow for better visibility
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.8));
        shadow.setRadius(3);
        shadow.setOffsetX(1);
        shadow.setOffsetY(1);
        textLabel.setEffect(shadow);
        
        // Position at center of tile
        textLabel.setLayoutX(x - 30);
        textLabel.setLayoutY(y - 10);
        textLabel.setAlignment(Pos.CENTER);
        
        container.getChildren().add(textLabel);
        
        // Animate floating up and fading out
        TranslateTransition floatUp = new TranslateTransition(ANIMATION_DURATION, textLabel);
        floatUp.setFromY(0);
        floatUp.setToY(-40); // Float up
        
        FadeTransition fadeOut = new FadeTransition(ANIMATION_DURATION, textLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0);
        
        ScaleTransition scaleOut = new ScaleTransition(ANIMATION_DURATION, textLabel);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(1.3);
        scaleOut.setToY(1.3);
        
        ParallelTransition animation = new ParallelTransition(floatUp, fadeOut, scaleOut);
        animation.setOnFinished(e -> container.getChildren().remove(textLabel));
        animation.play();
    }
    
    /**
     * Converts Color to hex string for CSS.
     */
    private static String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
