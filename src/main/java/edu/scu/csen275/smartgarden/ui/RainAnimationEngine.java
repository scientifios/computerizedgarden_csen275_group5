package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rain animation engine that shows falling rain when weather is RAINY.
 * Creates continuous rain droplets falling across the entire garden.
 */
public class RainAnimationEngine {
    private static final Random random = new Random();
    private static Canvas rainCanvas;
    private static Timeline rainTimeline;
    private static List<RainDrop> rainDrops;
    private static boolean isRaining = false;
    
    /**
     * Starts rain animation across the entire garden.
     */
    public static void startRain(Pane container) {
        if (isRaining) {
            return; // Already raining
        }
        
        isRaining = true;
        
        // Create rain canvas overlay
        rainCanvas = new Canvas();
        rainCanvas.setMouseTransparent(true);
        container.getChildren().add(rainCanvas);
        
        // Bind canvas size to container
        rainCanvas.widthProperty().bind(container.widthProperty());
        rainCanvas.heightProperty().bind(container.heightProperty());
        
        // Initialize rain drops
        rainDrops = new ArrayList<>();
        
        // Wait a bit for container to size properly, then initialize
        javafx.animation.PauseTransition initDelay = new javafx.animation.PauseTransition(Duration.millis(100));
        initDelay.setOnFinished(e -> {
            initializeRainDrops(container);
        });
        initDelay.play();
        
        // Create continuous rain animation (60 FPS)
        rainTimeline = new Timeline(
            new KeyFrame(Duration.millis(16), e -> {
                if (rainCanvas != null && container != null && rainDrops != null) {
                    updateAndDrawRain(container);
                }
            })
        );
        rainTimeline.setCycleCount(Animation.INDEFINITE);
        rainTimeline.play();
    }
    
    /**
     * Stops rain animation.
     */
    public static void stopRain(Pane container) {
        if (!isRaining) {
            return;
        }
        
        isRaining = false;
        
        if (rainTimeline != null) {
            rainTimeline.stop();
            rainTimeline = null;
        }
        
        if (rainCanvas != null && container != null) {
            container.getChildren().remove(rainCanvas);
            rainCanvas = null;
        }
        
        if (rainDrops != null) {
            rainDrops.clear();
        }
    }
    
    /**
     * Initializes rain drops across the canvas.
     */
    private static void initializeRainDrops(Pane container) {
        if (container == null || rainCanvas == null) {
            return;
        }
        
        // Wait for container to have valid size
        double width = container.getWidth();
        double height = container.getHeight();
        
        if (width <= 0 || height <= 0) {
            // Use default size if container not sized yet
            width = 800;
            height = 600;
        }
        
        // Create many rain drops (density based on container size)
        int dropCount = (int)(width * height / 2000); // Adjust density
        dropCount = Math.max(50, Math.min(300, dropCount)); // Clamp between 50-300
        
        if (rainDrops == null) {
            rainDrops = new ArrayList<>();
        }
        rainDrops.clear();
        
        for (int i = 0; i < dropCount; i++) {
            RainDrop drop = new RainDrop();
            drop.x = random.nextDouble() * width;
            drop.y = random.nextDouble() * height; // Start at random Y
            drop.speed = 3 + random.nextDouble() * 5; // 3-8 pixels per frame
            drop.length = 10 + random.nextDouble() * 15; // 10-25 pixels long
            drop.opacity = 0.4 + random.nextDouble() * 0.4; // 0.4-0.8 opacity
            rainDrops.add(drop);
        }
        
    }
    
    /**
     * Updates and draws all rain drops.
     */
    private static void updateAndDrawRain(Pane container) {
        if (rainCanvas == null || container == null || rainDrops == null) {
            return;
        }
        
        GraphicsContext gc = rainCanvas.getGraphicsContext2D();
        double width = rainCanvas.getWidth();
        double height = rainCanvas.getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // Clear canvas
        gc.clearRect(0, 0, width, height);
        
        // Update and draw each rain drop
        for (RainDrop drop : rainDrops) {
            // Move drop down
            drop.y += drop.speed;
            
            // Reset if drop goes off screen
            if (drop.y > height) {
                drop.y = -drop.length; // Start from top
                drop.x = random.nextDouble() * width; // Random X position
            }
            
            // Draw rain drop (line)
            gc.setStroke(Color.rgb(173, 216, 230, drop.opacity)); // Light blue
            gc.setLineWidth(1.5);
            gc.strokeLine(
                drop.x, drop.y,
                drop.x, drop.y + drop.length
            );
        }
        
        // Reinitialize if container size changed significantly
        if (Math.abs(width - (rainDrops.isEmpty() ? 0 : width)) > 100) {
            initializeRainDrops(container);
        }
    }
    
    /**
     * Checks if rain is currently active.
     */
    public static boolean isRaining() {
        return isRaining;
    }
    
    /**
     * Data class for a single rain drop.
     */
    private static class RainDrop {
        double x, y; // Position
        double speed; // Pixels per frame
        double length; // Length of rain drop
        double opacity; // 0.0 to 1.0
    }
}

