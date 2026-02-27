package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Advanced watering animation engine with droplets, ripples, and soil darkening.
 * Provides 60 FPS smooth animations for watering effects.
 */
public class WaterAnimationEngine {
    private static final Random random = new Random();
    private static final double ANIMATION_DURATION = 2000; // 2 seconds
    private static final double FRAME_RATE = 60.0; // 60 FPS
    private static final double FRAME_INTERVAL = 1000.0 / FRAME_RATE; // ~16.67ms
    
    // Track active animations to prevent duplicates
    private static final java.util.Set<Pane> activeAnimations = new java.util.HashSet<>();
    
    /**
     * Animates watering for all tiles with full effects.
     */
    public static void animateAllTilesWatering(List<AnimatedTile> tiles, Pane container) {
        if (tiles == null || tiles.isEmpty() || container == null) {
            return;
        }
        
        // Check if scene is ready
        if (container.getScene() == null) {
            return;
        }
        
        // Prevent duplicate animations on same container
        synchronized (activeAnimations) {
            if (activeAnimations.contains(container)) {
                return; // Animation already running
            }
            activeAnimations.add(container);
        }
        
        // Create animation canvas overlay
        Canvas animationCanvas = new Canvas();
        animationCanvas.setMouseTransparent(true);
        
        // Set initial size
        double containerWidth = container.getWidth();
        double containerHeight = container.getHeight();
        if (containerWidth > 0 && containerHeight > 0) {
            animationCanvas.setWidth(containerWidth);
            animationCanvas.setHeight(containerHeight);
        } else {
            // Use default size if container not sized yet
            animationCanvas.setWidth(800);
            animationCanvas.setHeight(600);
        }
        
        container.getChildren().add(animationCanvas);
        
        // Bind canvas size to container
        animationCanvas.widthProperty().bind(container.widthProperty());
        animationCanvas.heightProperty().bind(container.heightProperty());
        
        // Create animation data for each tile
        List<WaterAnimationData> animations = new ArrayList<>();
        for (AnimatedTile tile : tiles) {
            if (tile == null) continue;
            
            WaterAnimationData data = new WaterAnimationData(tile, null);
            animations.add(data);
            
            // Start soil darkening animation on tile
            try {
                tile.startWateringAnimation();
            } catch (Exception ex) {
                // Skip if tile is invalid
            }
        }
        
        // Create high-frequency animation timeline (60 FPS)
        Timeline animationTimeline = new Timeline(
            new KeyFrame(Duration.millis(FRAME_INTERVAL), e -> {
                if (animationCanvas == null || container == null || !container.getChildren().contains(animationCanvas)) {
                    return;
                }
                
                try {
                    GraphicsContext gc = animationCanvas.getGraphicsContext2D();
                    if (gc == null) return;
                    
                    double width = animationCanvas.getWidth();
                    double height = animationCanvas.getHeight();
                    
                    if (width <= 0 || height <= 0) {
                        return;
                    }
                    
                    gc.clearRect(0, 0, width, height);
                    
                    long currentTime = System.currentTimeMillis();
                    boolean hasActiveAnimations = false;
                    
                    for (WaterAnimationData data : animations) {
                        if (data == null || data.tile == null) continue;
                        
                        double elapsed = currentTime - data.startTime;
                        double progress = Math.min(1.0, elapsed / ANIMATION_DURATION);
                        
                        if (progress < 1.0) {
                            hasActiveAnimations = true;
                            
                            // Draw droplets
                            try {
                                drawDroplets(gc, data, progress);
                            } catch (Exception ex) {
                                // Skip if error drawing
                            }
                            
                            // Draw ripples
                            try {
                                drawRipples(gc, data, progress);
                            } catch (Exception ex) {
                                // Skip if error drawing
                            }
                        }
                    }
                    
                    // Remove canvas when animation is complete
                    if (!hasActiveAnimations) {
                        if (container.getChildren().contains(animationCanvas)) {
                            container.getChildren().remove(animationCanvas);
                        }
                        synchronized (activeAnimations) {
                            activeAnimations.remove(container);
                        }
                    }
                } catch (Exception ex) {
                    // Handle any drawing errors - clean up
                    try {
                        if (container.getChildren().contains(animationCanvas)) {
                            container.getChildren().remove(animationCanvas);
                        }
                    } catch (Exception ex2) {
                        // Ignore cleanup errors
                    }
                    synchronized (activeAnimations) {
                        activeAnimations.remove(container);
                    }
                }
            })
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        
        // Stop after animation duration
        Timeline stopTimeline = new Timeline(
            new KeyFrame(Duration.millis(ANIMATION_DURATION), e -> {
                if (animationTimeline != null) {
                    animationTimeline.stop();
                }
                try {
                    if (container != null && container.getChildren().contains(animationCanvas)) {
                        container.getChildren().remove(animationCanvas);
                    }
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
                synchronized (activeAnimations) {
                    activeAnimations.remove(container);
                }
            })
        );
        
        // Start all tiles at slightly different times for staggered effect
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).startTime = System.currentTimeMillis() + (i * 30); // 30ms stagger
        }
        
        animationTimeline.play();
        stopTimeline.play();
    }
    
    /**
     * Animates watering for a specific zone.
     */
    public static void animateZoneWatering(List<AnimatedTile> tiles, Pane container) {
        animateAllTilesWatering(tiles, container);
    }
    
    /**
     * Draws falling water droplets on a tile.
     */
    private static void drawDroplets(GraphicsContext gc, WaterAnimationData data, double progress) {
        if (data.tile == null || data.tile.getScene() == null) {
            return;
        }
        
        // Get tile bounds in scene coordinates
        Bounds sceneBounds = data.tile.localToScene(data.tile.getBoundsInLocal());
        if (sceneBounds == null || sceneBounds.isEmpty()) {
            return;
        }
        
        // Get container bounds (the canvas is bound to container size)
        Pane container = (Pane) gc.getCanvas().getParent();
        if (container == null || container.getScene() == null) {
            return;
        }
        
        Bounds containerSceneBounds = container.localToScene(container.getBoundsInLocal());
        if (containerSceneBounds == null || containerSceneBounds.isEmpty()) {
            return;
        }
        
        // Convert tile scene coordinates to canvas coordinates
        double canvasX = sceneBounds.getMinX() - containerSceneBounds.getMinX();
        double canvasY = sceneBounds.getMinY() - containerSceneBounds.getMinY();
        double tileWidth = sceneBounds.getWidth();
        double tileHeight = sceneBounds.getHeight();
        
        double centerX = canvasX + tileWidth / 2;
        double tileTop = canvasY;
        
        // Draw multiple droplets per tile
        int dropletCount = 3 + random.nextInt(3); // 3-5 droplets
        
        for (int i = 0; i < dropletCount; i++) {
            double dropletProgress = (progress * 1.5) - (i * 0.2); // Stagger droplets
            dropletProgress = Math.max(0, Math.min(1.0, dropletProgress));
            
            if (dropletProgress <= 0 || dropletProgress >= 1.0) {
                continue;
            }
            
            // Random X offset
            double offsetX = (random.nextDouble() - 0.5) * tileWidth * 0.6;
            double x = centerX + offsetX;
            
            // Falling animation
            double dropDistance = tileHeight;
            double y = tileTop + (dropletProgress * dropDistance);
            
            // Droplet size (smaller as it falls)
            double size = 4 + (2 * (1 - dropletProgress));
            
            // Alpha fade
            double alpha = 1.0 - (dropletProgress * 0.5);
            
            // Draw droplet (teardrop shape)
            gc.setFill(Color.rgb(135, 206, 250, alpha * 0.8)); // Light blue
            gc.setStroke(Color.rgb(100, 149, 237, alpha * 0.6)); // Cornflower blue
            
            // Draw circular droplet with slight elongation
            gc.fillOval(x - size/2, y - size, size, size * 1.3);
            gc.strokeOval(x - size/2, y - size, size, size * 1.3);
        }
    }
    
    /**
     * Draws expanding ripple effects on a tile.
     */
    private static void drawRipples(GraphicsContext gc, WaterAnimationData data, double progress) {
        if (data.tile == null || data.tile.getScene() == null) {
            return;
        }
        
        // Get tile bounds in scene coordinates
        Bounds sceneBounds = data.tile.localToScene(data.tile.getBoundsInLocal());
        if (sceneBounds == null || sceneBounds.isEmpty()) {
            return;
        }
        
        // Get container bounds (the canvas is bound to container size)
        Pane container = (Pane) gc.getCanvas().getParent();
        if (container == null || container.getScene() == null) {
            return;
        }
        
        Bounds containerSceneBounds = container.localToScene(container.getBoundsInLocal());
        if (containerSceneBounds == null || containerSceneBounds.isEmpty()) {
            return;
        }
        
        // Convert tile scene coordinates to canvas coordinates
        double canvasX = sceneBounds.getMinX() - containerSceneBounds.getMinX();
        double canvasY = sceneBounds.getMinY() - containerSceneBounds.getMinY();
        double tileWidth = sceneBounds.getWidth();
        double tileHeight = sceneBounds.getHeight();
        
        double centerX = canvasX + tileWidth / 2;
        double centerY = canvasY + tileHeight / 2;
        double maxRadius = Math.max(tileWidth, tileHeight) * 0.8;
        
        // Single ripple with reduced intensity
        for (int i = 0; i < 1; i++) {
            double rippleProgress = progress - (i * 0.3); // Stagger ripples
            rippleProgress = Math.max(0, Math.min(1.0, rippleProgress));
            
            if (rippleProgress <= 0) {
                continue;
            }
            
            double radius = rippleProgress * maxRadius;
            
            // Reduced alpha - much more subtle
            double alpha = (1.0 - rippleProgress) * 0.25; // Reduced from 0.6 to 0.25
            
            // Draw ripple circle with thinner line
            gc.setStroke(Color.rgb(100, 149, 237, alpha)); // Cornflower blue
            gc.setLineWidth(1.0); // Reduced from 2.0 to 1.0
            gc.strokeOval(
                centerX - radius,
                centerY - radius,
                radius * 2,
                radius * 2
            );
            
            // Removed inner ripple for less intensity
        }
    }
    
    /**
     * Calculates darkened soil color based on watering progress.
     */
    public static Color getDarkenedSoilColor(Color originalColor, double progress) {
        // Darken by 30% when fully watered, fade back
        double darknessFactor = 1.0 - (0.3 * (1.0 - progress));
        
        double red = originalColor.getRed() * darknessFactor;
        double green = originalColor.getGreen() * darknessFactor;
        double blue = originalColor.getBlue() * darknessFactor;
        
        return Color.color(
            Math.max(0, Math.min(1, red)),
            Math.max(0, Math.min(1, green)),
            Math.max(0, Math.min(1, blue))
        );
    }
    
    /**
     * Data class for tracking watering animation state per tile.
     */
    private static class WaterAnimationData {
        final AnimatedTile tile;
        Bounds sceneBounds;
        long startTime;
        
        WaterAnimationData(AnimatedTile tile, Bounds sceneBounds) {
            this.tile = tile;
            this.sceneBounds = sceneBounds;
            this.startTime = System.currentTimeMillis();
        }
    }
}
