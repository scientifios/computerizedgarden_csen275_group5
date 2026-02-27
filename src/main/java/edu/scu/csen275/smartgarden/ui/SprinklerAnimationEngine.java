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
 * Sprinkler animation engine that shows water being sprinkled when watering zones.
 * Creates water droplets spraying from sprinkler positions.
 */
public class SprinklerAnimationEngine {
    private static final Random random = new Random();
    private static final double ANIMATION_DURATION = 5000; // 5 seconds (increased for better visibility)
    private static final double FRAME_RATE = 60.0; // 60 FPS
    private static final double FRAME_INTERVAL = 1000.0 / FRAME_RATE; // ~16.67ms
    
    // Track active animations to prevent duplicates and allow stopping
    private static final java.util.Set<Pane> activeAnimations = new java.util.HashSet<>();
    private static final java.util.Map<Pane, Timeline> activeTimelines = new java.util.HashMap<>();
    
    /**
     * Stops all active sprinkler animations (called when rain starts).
     */
    public static void stopAllAnimations() {
        synchronized (activeAnimations) {
            for (Timeline timeline : new ArrayList<>(activeTimelines.values())) {
                if (timeline != null) {
                    timeline.stop();
                }
            }
            activeTimelines.clear();
            activeAnimations.clear();
        }
    }
    
    /**
     * Animates sprinkler water for a specific zone.
     */
    public static void animateSprinkler(int zoneId, List<AnimatedTile> tiles, Pane container) {
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
        
        // Bind canvas size to container for dynamic resizing
        animationCanvas.widthProperty().bind(container.widthProperty());
        animationCanvas.heightProperty().bind(container.heightProperty());
        
        container.getChildren().add(animationCanvas);
        
        // Create sprinkler water particles for each tile
        List<SprinklerParticle> particles = new ArrayList<>();
        for (AnimatedTile tile : tiles) {
            if (tile == null) continue;
            
            // Check if tile is in scene
            if (tile.getScene() == null) continue;
            
            try {
                javafx.geometry.Bounds localBounds = tile.getBoundsInLocal();
                if (localBounds == null || localBounds.isEmpty()) continue;
                
                Bounds tileBounds = tile.localToScene(localBounds);
                if (tileBounds == null || tileBounds.isEmpty()) continue;
                
                javafx.geometry.Bounds containerLocalBounds = container.getBoundsInLocal();
                if (containerLocalBounds == null || containerLocalBounds.isEmpty()) continue;
                
                Bounds containerBounds = container.localToScene(containerLocalBounds);
                if (containerBounds == null || containerBounds.isEmpty()) continue;
                
                // Convert to canvas coordinates
                double centerX = (tileBounds.getMinX() - containerBounds.getMinX()) + tileBounds.getWidth() / 2;
                double centerY = (tileBounds.getMinY() - containerBounds.getMinY()) + tileBounds.getHeight() / 2;
                
                // Create multiple water particles per tile (sprinkler effect)
                int particleCount = 15 + random.nextInt(10); // 15-25 particles per tile (increased for visibility)
                for (int i = 0; i < particleCount; i++) {
                    SprinklerParticle particle = new SprinklerParticle();
                    particle.x = centerX;
                    particle.y = centerY;
                    
                    // Random angle for sprinkler spray (arc pattern)
                    double angle = (Math.PI / 4) + (random.nextDouble() * Math.PI / 2); // 45-135 degrees
                    double velocity = 4 + random.nextDouble() * 4; // 4-8 pixels per frame (increased for more dramatic effect)
                    particle.vx = Math.cos(angle) * velocity;
                    particle.vy = Math.sin(angle) * velocity;
                    
                    particle.size = 5 + random.nextDouble() * 4; // 5-9 pixels (increased size for visibility)
                    particle.opacity = 0.9 + random.nextDouble() * 0.1; // 0.9-1.0 (increased opacity)
                    particle.lifetime = 0;
                    particle.maxLifetime = 40 + random.nextInt(30); // 40-70 frames (longer lifetime)
                    
                    particles.add(particle);
                }
            } catch (Exception ex) {
                // Skip this tile if there's an error getting bounds
                continue;
            }
        }
        
        // If no particles created, clean up and return
        if (particles.isEmpty()) {
            container.getChildren().remove(animationCanvas);
            synchronized (activeAnimations) {
                activeAnimations.remove(container);
            }
            return;
        }
        
        // Create animation timeline (60 FPS)
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
                    
                    // Update and draw particles
                    boolean hasActiveParticles = false;
                    for (SprinklerParticle particle : new ArrayList<>(particles)) {
                        if (particle == null) continue;
                        
                        particle.lifetime++;
                        
                        if (particle.lifetime < particle.maxLifetime) {
                            hasActiveParticles = true;
                            
                            // Update position
                            particle.x += particle.vx;
                            particle.y += particle.vy;
                            
                            // Apply gravity
                            particle.vy += 0.3;
                            
                            // Fade out over time
                            double progress = (double)particle.lifetime / particle.maxLifetime;
                            double currentOpacity = particle.opacity * (1.0 - progress);
                            
                            // Draw water droplet (brighter blue for better visibility)
                            gc.setFill(Color.rgb(100, 200, 255, currentOpacity)); // Brighter, more vibrant blue
                            gc.fillOval(
                                particle.x - particle.size / 2,
                                particle.y - particle.size / 2,
                                particle.size,
                                particle.size
                            );
                        } else {
                            particles.remove(particle);
                        }
                    }
                    
                        // Remove canvas when all particles are gone
                        if (!hasActiveParticles) {
                            if (container.getChildren().contains(animationCanvas)) {
                                container.getChildren().remove(animationCanvas);
                            }
                            synchronized (activeAnimations) {
                                activeAnimations.remove(container);
                                activeTimelines.remove(container);
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
                            activeTimelines.remove(container);
                        }
                    }
            })
        );
            animationTimeline.setCycleCount(Animation.INDEFINITE);
            
            // Store timeline for potential stopping
            synchronized (activeAnimations) {
                activeTimelines.put(container, animationTimeline);
            }
            
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
                        activeTimelines.remove(container);
                    }
                })
            );
            
            animationTimeline.play();
            stopTimeline.play();
    }
    
    /**
     * Data class for a sprinkler water particle.
     */
    private static class SprinklerParticle {
        double startX, startY; // Starting position
        double x, y; // Current position
        double vx, vy; // Velocity
        double size; // Particle size
        double opacity; // Initial opacity
        int lifetime; // Current lifetime in frames
        int maxLifetime; // Maximum lifetime in frames
    }
}

