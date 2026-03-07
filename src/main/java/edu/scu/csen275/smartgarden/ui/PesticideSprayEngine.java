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
 * Handles the visual effect sequence used when pesticide is applied to a tile.
 * Includes mist rendering, pest removal timing, and a short recovery highlight.
 */
public class PesticideSprayEngine {
    
    /**
     * Runs the pesticide effect sequence for the given tile and related UI elements.
     */
    public static void animateSpray(StackPane tile, List<PestSprite> pests, 
                                   Pane particleContainer, double centerX, double centerY) {
        double tileWidth = 60;
        double tileHeight = 60;
        
        Canvas existingMist = null;
        for (var child : tile.getChildren()) {
            if (child instanceof Canvas && child.getUserData() != null && child.getUserData().equals("mist")) {
                existingMist = (Canvas) child;
                break;
            }
        }
        if (existingMist != null) {
            return;
        }
        
        Canvas mistCanvas = new Canvas(tileWidth, tileHeight);
        mistCanvas.setUserData("mist");
        GraphicsContext gc = mistCanvas.getGraphicsContext2D();
        mistCanvas.setMouseTransparent(true);
        
        tile.getChildren().add(mistCanvas);
        mistCanvas.toFront();
        
        List<MistParticle> mistParticles = new ArrayList<>();
        double tileCenterX = tileWidth / 2;
        double tileCenterY = tileHeight / 2;
        for (int i = 0; i < 40; i++) {
            MistParticle p = new MistParticle();
            p.x = tileCenterX + (Math.random() - 0.5) * 8;
            p.y = tileCenterY + (Math.random() - 0.5) * 8;
            p.radius = 1.5 + Math.random() * 2.5;
            p.vx = (Math.random() - 0.5) * 2.5;
            p.vy = (Math.random() - 0.5) * 2.5;
            p.alpha = 0.95;
            mistParticles.add(p);
        }
        
        Timeline mistAnimation = new Timeline(
            new KeyFrame(Duration.millis(16), e -> {
                gc.clearRect(0, 0, tileWidth, tileHeight);
                
                for (MistParticle p : mistParticles) {
                    p.x += p.vx;
                    p.y += p.vy;
                    p.radius += 0.15;
                    p.alpha *= 0.995;
                    p.vx *= 0.998;
                    p.vy *= 0.998;
                    
                    gc.setFill(Color.color(1.0, 1.0, 1.0, p.alpha * 0.9));
                    gc.fillOval(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
                    
                    gc.setFill(Color.color(0.85, 0.85, 0.85, p.alpha * 0.4));
                    gc.fillOval(p.x - p.radius * 1.1, p.y - p.radius * 1.1, p.radius * 2.2, p.radius * 2.2);
                }
            })
        );
        mistAnimation.setCycleCount(400);
        mistAnimation.setOnFinished(e -> {
            tile.getChildren().remove(mistCanvas);
        });
        mistAnimation.play();
        
        for (PestSprite pest : new ArrayList<>(pests)) {
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(
                Duration.millis(200 + (int)(Math.random() * 300))
            );
            delay.setOnFinished(e -> {
                pest.animateDeath(() -> {
                });
            });
            delay.play();
        }
        
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
        
        PauseTransition glowDelay = new PauseTransition(Duration.millis(5000));
        glowDelay.setOnFinished(e -> {
            if (tile.getScene() != null && tile.getBoundsInLocal().getWidth() > 0 && tile.getBoundsInLocal().getHeight() > 0) {
                Glow healGlow = new Glow(0.3);
                tile.setEffect(healGlow);
                
                FadeTransition glowFade = new FadeTransition(Duration.millis(1500), tile);
                glowFade.setFromValue(1.0);
                glowFade.setToValue(1.0);
                glowFade.setOnFinished(e2 -> {
                    tile.setEffect(null);
                });
                glowFade.play();
            }
        });
        glowDelay.play();
    }
    
    private static class MistParticle {
        double x, y, radius, vx, vy, alpha;
    }
}
