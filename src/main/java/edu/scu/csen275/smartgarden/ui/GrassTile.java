package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lush green grass tile with swaying animation, bloom effects, and sparkles.
 * Gardenscapes-inspired cheerful grass tile.
 */
public class GrassTile extends StackPane {
    private final Canvas grassCanvas;
    private final StackPane baseTile;
    private final Label flowerLabel;
    private final List<Sparkle> sparkles;
    private final Random random;
    private Timeline swayAnimation;
    private Timeline sparkleTimeline;
    private boolean hasFlower;
    private double swayOffset;
    
    private static final double BASE_SIZE = 60;
    private static final int GRASS_BLADES = 15;
    
    public GrassTile() {
        this.random = new Random();
        this.sparkles = new ArrayList<>();
        this.hasFlower = false;
        this.swayOffset = 0;
        
        this.setMinSize(BASE_SIZE, BASE_SIZE);
        this.setMaxSize(BASE_SIZE, BASE_SIZE);
        this.setAlignment(Pos.CENTER);
        
        // Base tile with rounded edges and shadow
        baseTile = new StackPane();
        baseTile.setMinSize(BASE_SIZE, BASE_SIZE);
        baseTile.setMaxSize(BASE_SIZE, BASE_SIZE);
        baseTile.setStyle(getGrassTileStyle());
        // Defer effect application until node is in scene
        safeSetEffect(baseTile, createSoftShadow());
        
        // Canvas for grass texture and animation
        grassCanvas = new Canvas(BASE_SIZE, BASE_SIZE);
        grassCanvas.setMouseTransparent(true);
        
        // Flower label (hidden initially)
        flowerLabel = new Label("");
        flowerLabel.setFont(javafx.scene.text.Font.font(20));
        flowerLabel.setAlignment(Pos.CENTER);
        flowerLabel.setVisible(false);
        
        this.getChildren().addAll(baseTile, grassCanvas, flowerLabel);
        
        // Draw initial grass
        drawGrass();
        
        // Start animations
        startSwayAnimation();
        startSparkleSystem();
    }
    
    /**
     * Gets the grass tile style with rounded edges.
     */
    private String getGrassTileStyle() {
        return "-fx-background-color: linear-gradient(to bottom, " +
               "#7CB342 0%, " +  // Light green
               "#66BB6A 30%, " + // Medium green
               "#4CAF50 60%, " + // Darker green
               "#66BB6A 100%); " + // Back to medium
               "-fx-background-radius: 8; " +
               "-fx-border-color: rgba(76, 175, 80, 0.3); " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 8;";
    }
    
    /**
     * Safely applies an effect to a node, deferring if not in scene.
     */
    private void safeSetEffect(javafx.scene.Node node, javafx.scene.effect.Effect effect) {
        if (node.getScene() != null && node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
            // Node is in scene and has valid bounds, apply immediately
            node.setEffect(effect);
        } else {
            // Defer until node is in scene
            node.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    // Use Platform.runLater to ensure layout is complete
                    javafx.application.Platform.runLater(() -> {
                        if (node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
                            node.setEffect(effect);
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Creates soft shadow for depth.
     */
    private DropShadow createSoftShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(6);
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2));
        shadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        return shadow;
    }
    
    /**
     * Draws lush grass with texture and color variation.
     */
    private void drawGrass() {
        GraphicsContext gc = grassCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, BASE_SIZE, BASE_SIZE);
        
        // Draw grass blades with variation
        for (int i = 0; i < GRASS_BLADES; i++) {
            double x = (i / (double)GRASS_BLADES) * BASE_SIZE + random.nextDouble() * 3;
            double height = 8 + random.nextDouble() * 6; // Vary blade height
            double baseY = BASE_SIZE - 2;
            
            // Color variation for natural look
            double greenVariation = 0.85 + random.nextDouble() * 0.15;
            Color bladeColor = Color.rgb(
                (int)(76 * greenVariation),
                (int)(175 * greenVariation),
                (int)(80 * greenVariation),
                0.9
            );
            
            // Add sway offset for wind effect
            double swayX = x + Math.sin(swayOffset + i * 0.5) * 2;
            
            // Draw grass blade as a curved line
            gc.setStroke(bladeColor);
            gc.setLineWidth(1.5);
            
            // Curved blade path
            double controlX = swayX + Math.sin(swayOffset + i) * 1.5;
            double controlY = baseY - height * 0.5;
            double endX = swayX + Math.sin(swayOffset + i * 0.7) * 3;
            double endY = baseY - height;
            
            // Draw blade as bezier curve approximation
            gc.beginPath();
            gc.moveTo(swayX, baseY);
            gc.quadraticCurveTo(controlX, controlY, endX, endY);
            gc.stroke();
            
            // Add highlight on some blades
            if (random.nextDouble() < 0.3) {
                gc.setStroke(Color.rgb(129, 199, 132, 0.6));
                gc.setLineWidth(0.8);
                gc.strokeLine(swayX, baseY, endX, endY);
            }
        }
        
        // Draw sparkles
        for (Sparkle sparkle : sparkles) {
            drawSparkle(gc, sparkle);
        }
    }
    
    /**
     * Draws a sparkle particle.
     */
    private void drawSparkle(GraphicsContext gc, Sparkle sparkle) {
        double alpha = 1.0 - (sparkle.age / sparkle.lifetime);
        alpha = Math.max(0, Math.min(1, alpha));
        
        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.rgb(255, 255, 200, 0.8));
        gc.setStroke(Color.rgb(255, 255, 150, 0.6));
        
        double size = sparkle.size;
        double x = sparkle.x;
        double y = sparkle.y;
        
        // Draw sparkle as a star
        gc.fillOval(x - size/2, y - size/2, size, size);
        gc.setLineWidth(0.5);
        gc.strokeLine(x - size, y, x + size, y);
        gc.strokeLine(x, y - size, x, y + size);
        
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * Starts gentle swaying animation (wind effect).
     */
    private void startSwayAnimation() {
        swayAnimation = new Timeline(
            new KeyFrame(Duration.millis(1000.0 / 60.0), e -> {
                // Update sway offset for wind effect
                swayOffset += 0.05;
                if (swayOffset > Math.PI * 2) {
                    swayOffset -= Math.PI * 2;
                }
                drawGrass(); // Redraw with new sway
            })
        );
        swayAnimation.setCycleCount(Animation.INDEFINITE);
        swayAnimation.play();
    }
    
    /**
     * Starts sparkle system for occasional floating sparkles.
     */
    private void startSparkleSystem() {
        sparkleTimeline = new Timeline(
            new KeyFrame(Duration.millis(1000.0 / 60.0), e -> {
                // Update sparkles
                sparkles.removeIf(sparkle -> {
                    sparkle.age += 0.016;
                    sparkle.y -= 0.3; // Float upward
                    sparkle.x += Math.sin(sparkle.age * 2) * 0.2; // Gentle drift
                    return sparkle.age > sparkle.lifetime;
                });
                
                // Occasionally add new sparkle
                if (random.nextDouble() < 0.01 && sparkles.size() < 3) {
                    Sparkle sparkle = new Sparkle();
                    sparkle.x = random.nextDouble() * BASE_SIZE;
                    sparkle.y = BASE_SIZE - 5;
                    sparkle.size = 2 + random.nextDouble() * 2;
                    sparkle.lifetime = 2 + random.nextDouble() * 2;
                    sparkle.age = 0;
                    sparkles.add(sparkle);
                }
                
                drawGrass(); // Redraw with updated sparkles
            })
        );
        sparkleTimeline.setCycleCount(Animation.INDEFINITE);
        sparkleTimeline.play();
    }
    
    /**
     * Makes a flower bloom on the tile.
     * Only called on user interaction, not automatically.
     */
    public void bloomFlower() {
        if (!hasFlower) {
            hasFlower = true;
            String[] flowerEmojis = {"🌸", "🌺", "🌻", "🌷", "🌼"};
            flowerLabel.setText(flowerEmojis[random.nextInt(flowerEmojis.length)]);
            flowerLabel.setVisible(true);
            
            // Bloom animation (scale up)
            ScaleTransition bloom = new ScaleTransition(Duration.millis(500), flowerLabel);
            bloom.setFromX(0.0);
            bloom.setFromY(0.0);
            bloom.setToX(1.0);
            bloom.setToY(1.0);
            bloom.setInterpolator(Interpolator.EASE_OUT);
            bloom.play();
            
            // Create sparkle burst
            for (int i = 0; i < 5; i++) {
                Sparkle sparkle = new Sparkle();
                sparkle.x = BASE_SIZE / 2 + (random.nextDouble() - 0.5) * 20;
                sparkle.y = BASE_SIZE / 2 + (random.nextDouble() - 0.5) * 20;
                sparkle.size = 3 + random.nextDouble() * 3;
                sparkle.lifetime = 1.5 + random.nextDouble() * 1;
                sparkle.age = 0;
                sparkles.add(sparkle);
            }
        }
    }
    
    /**
     * Removes flower from tile (called when plant is added).
     */
    public void removeFlower() {
        if (hasFlower) {
            hasFlower = false;
            flowerLabel.setVisible(false);
            flowerLabel.setText("");
        }
    }
    
    /**
     * Makes petals float from the flower.
     */
    public void floatPetals() {
        if (hasFlower) {
            for (int i = 0; i < 3; i++) {
                Sparkle petal = new Sparkle();
                petal.x = BASE_SIZE / 2;
                petal.y = BASE_SIZE / 2;
                petal.size = 2 + random.nextDouble() * 2;
                petal.lifetime = 2 + random.nextDouble() * 1;
                petal.age = 0;
                petal.isPetal = true;
                sparkles.add(petal);
            }
        }
    }
    
    /**
     * Checks if tile has a flower.
     */
    public boolean hasFlower() {
        return hasFlower;
    }
    
    /**
     * Stops all animations.
     */
    public void stop() {
        if (swayAnimation != null) {
            swayAnimation.stop();
        }
        if (sparkleTimeline != null) {
            sparkleTimeline.stop();
        }
    }
    
    /**
     * Sparkle/Petal data structure.
     */
    private static class Sparkle {
        double x, y, size, lifetime, age;
        boolean isPetal = false;
    }
}

