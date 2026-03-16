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
 * Utility for displaying short-lived floating text overlays on a pane.
 * Common uses include status deltas and brief notifications.
 */
public class DamageTextAnimation {
    private static final Duration ANIMATION_DURATION = Duration.millis(2500); 
    
    /**
     * Shows a floating negative HP label at the given position.
     */
    public static void createDamageText(Pane container, double x, double y, int damage) {
        createText(container, "-" + damage + " HP", Color.rgb(255, 51, 51), x, y);
    }
    
    /**
     * Shows a floating positive heal label at the given position.
     */
    public static void createHealingText(Pane container, double x, double y, int healing) {
        createText(container, "+" + healing + " health", Color.rgb(51, 255, 51), x, y);
    }
    
    /**
     * Displays a floating text label with the given styling and animation.
     */
    public static void createText(Pane container, String text, Color color, double x, double y) {
        Label textLabel = new Label(text);
        textLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + toHexString(color) + "; " +
            "-fx-background-color: transparent;"
        );
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.8));
        shadow.setRadius(3);
        shadow.setOffsetX(1);
        shadow.setOffsetY(1);
        textLabel.setEffect(shadow);
        
        textLabel.setLayoutX(x - 30);
        textLabel.setLayoutY(y - 10);
        textLabel.setAlignment(Pos.CENTER);
        
        container.getChildren().add(textLabel);
        
        TranslateTransition floatUp = new TranslateTransition(ANIMATION_DURATION, textLabel);
        floatUp.setFromY(0);
        floatUp.setToY(-40);
        
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
     * Formats a JavaFX Color as a CSS hex string.
     */
    private static String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
