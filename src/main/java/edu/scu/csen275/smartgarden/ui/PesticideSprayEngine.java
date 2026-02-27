package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates spray mist animation when pesticide is applied to a tile.
 * Shows expanding white/blue fog, pest death animations, and healing glow.
 */
public class PesticideSprayEngine {
    
    /**
     * Animates pesticide spray on a tile with all effects.
     */
    public static void animateSpray(StackPane tile, List<PestSprite> pests, 
                                   Pane particleContainer, double centerX, double centerY) {
        double tileWidth = 60;
        double tileHeight = 60;
        
        // Check if mist canvas already exists (prevent multiple animations)
        Canvas existingMist = null;
        for (var child : tile.getChildren()) {
            if (child instanceof Canvas && child.getUserData() != null && child.getUserData().equals("mist")) {
                existingMist = (Canvas) child;
                break;
            }
        }
        if (existingMist != null) {
            return; // Already animating, skip
        }
        
        // Create spray mist canvas overlay
        Canvas mistCanvas = new Canvas(tileWidth, tileHeight);
        mistCanvas.setUserData("mist"); // Mark as mist canvas
        GraphicsContext gc = mistCanvas.getGraphicsContext2D();
        mistCanvas.setMouseTransparent(true);
        
        // Add mist canvas to tile temporarily (bring to front)
        tile.getChildren().add(mistCanvas);
        mistCanvas.toFront();
        
        // List of active mist particles - centered in tile
        // Create more particles for denser, continuous mist effect
        List<MistParticle> mistParticles = new ArrayList<>();
        double tileCenterX = tileWidth / 2;
        double tileCenterY = tileHeight / 2;
        for (int i = 0; i < 40; i++) { // More particles for continuous spray effect
            MistParticle p = new MistParticle();
            p.x = tileCenterX + (Math.random() - 0.5) * 8; // Tighter initial spread
            p.y = tileCenterY + (Math.random() - 0.5) * 8;
            p.radius = 1.5 + Math.random() * 2.5; // Smaller initial size - grows over time
            p.vx = (Math.random() - 0.5) * 2.5; // Slower initial velocity for continuous spray
            p.vy = (Math.random() - 0.5) * 2.5;
            p.alpha = 0.95; // High initial opacity - fades slowly
            mistParticles.add(p);
        }
        
        // Animate continuous spray mist - longer duration for realistic spray effect
        Timeline mistAnimation = new Timeline(
            new KeyFrame(Duration.millis(16), e -> {
                gc.clearRect(0, 0, tileWidth, tileHeight);
                
                // Update and draw mist particles
                for (MistParticle p : mistParticles) {
                    p.x += p.vx;
                    p.y += p.vy;
                    p.radius += 0.15; // Much slower expansion for continuous spray
                    p.alpha *= 0.995; // Much slower fade - maintains visibility longer
                    p.vx *= 0.998; // Very slow velocity decay - particles move longer
                    p.vy *= 0.998;
                    
                    // Draw mist particle - white/light gray mist spray effect
                    // Use white with slight transparency for realistic mist
                    gc.setFill(Color.color(1.0, 1.0, 1.0, p.alpha * 0.9)); // Pure white mist
                    gc.fillOval(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
                    
                    // Add slight gray outer edge for depth
                    gc.setFill(Color.color(0.85, 0.85, 0.85, p.alpha * 0.4)); // Light gray edge
                    gc.fillOval(p.x - p.radius * 1.1, p.y - p.radius * 1.1, p.radius * 2.2, p.radius * 2.2);
                }
            })
        );
        mistAnimation.setCycleCount(400); // ~6.4 seconds at 16ms per frame - MUCH LONGER for continuous spray
        mistAnimation.setOnFinished(e -> {
            tile.getChildren().remove(mistCanvas);
        });
        mistAnimation.play();
        
        // Animate pest death (shrink animation)
        for (PestSprite pest : new ArrayList<>(pests)) {
            // Delay each pest death slightly for staggered effect
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                Duration.millis(200 + (int)(Math.random() * 300)) // 200-500ms delay
            );
            delay.setOnFinished(e -> {
                pest.animateDeath(() -> {
                    // Pest death animation completed
                });
            });
            delay.play();
        }
        
        // Show "Plant Saved!" text - FIXED: Use correct coordinates and ensure visibility
        if (particleContainer != null) {
            javafx.geometry.Bounds tileBounds = tile.localToScene(tile.getBoundsInLocal());
            javafx.geometry.Bounds containerBounds = particleContainer.sceneToLocal(tileBounds);
            double textX = containerBounds.getMinX() + containerBounds.getWidth() / 2;
            double textY = containerBounds.getMinY() + containerBounds.getHeight() / 2;
            DamageTextAnimation.createText(particleContainer, "Plant Saved!", 
                                          Color.rgb(51, 255, 51), 
                                          textX, 
                                          textY);
        } else {
            System.err.println("[PesticideSprayEngine] WARNING: particleContainer is null - cannot show 'Plant Saved!' message");
        }
        
        // Subtle white glow on tile after mist clears (no rainbow effect)
        // Use simple glow without ColorAdjust to avoid rainbow colors
        // Delay glow to appear after mist starts fading (around 5 seconds)
        PauseTransition glowDelay = new PauseTransition(Duration.millis(5000)); // Wait 5 seconds
        glowDelay.setOnFinished(e -> {
            // Only apply effect if tile is in scene and has valid bounds
            if (tile.getScene() != null && tile.getBoundsInLocal().getWidth() > 0 && tile.getBoundsInLocal().getHeight() > 0) {
                Glow healGlow = new Glow(0.3); // Subtle white glow (no color tint)
                tile.setEffect(healGlow);
                
                FadeTransition glowFade = new FadeTransition(Duration.millis(1500), tile); // 1.5 seconds
                glowFade.setFromValue(1.0);
                glowFade.setToValue(1.0); // Don't fade, just remove effect
                glowFade.setOnFinished(e2 -> {
                    tile.setEffect(null);
                });
                glowFade.play();
            }
        });
        glowDelay.play();
    }
    
    /**
     * Helper class for mist particles.
     */
    private static class MistParticle {
        double x, y, radius, vx, vy, alpha;
    }
}
