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
 * Snow animation engine that shows falling snowflakes when weather is SNOWY.
 * Creates continuous snowflakes falling across the entire garden.
 */
public class SnowAnimationEngine {
    private static final Random random = new Random();
    private static Canvas snowCanvas;
    private static Timeline snowTimeline;
    private static List<Snowflake> snowflakes;
    private static boolean isSnowing = false;
    
    /**
     * Starts snow animation across the entire garden.
     */
    public static void startSnow(Pane container) {
        if (isSnowing) {
            return; // Already snowing
        }
        
        isSnowing = true;
        
        // Create snow canvas overlay
        snowCanvas = new Canvas();
        snowCanvas.setMouseTransparent(true);
        container.getChildren().add(snowCanvas);
        
        // Bind canvas size to container
        snowCanvas.widthProperty().bind(container.widthProperty());
        snowCanvas.heightProperty().bind(container.heightProperty());
        
        // Initialize snowflakes
        snowflakes = new ArrayList<>();
        
        // Wait a bit for container to size properly, then initialize
        javafx.animation.PauseTransition initDelay = new javafx.animation.PauseTransition(Duration.millis(100));
        initDelay.setOnFinished(e -> {
            initializeSnowflakes(container);
        });
        initDelay.play();
        
        // Create continuous snow animation (60 FPS)
        snowTimeline = new Timeline(
            new KeyFrame(Duration.millis(16), e -> {
                if (snowCanvas != null && container != null && snowflakes != null) {
                    updateAndDrawSnow(container);
                }
            })
        );
        snowTimeline.setCycleCount(Animation.INDEFINITE);
        snowTimeline.play();
    }
    
    /**
     * Stops snow animation.
     */
    public static void stopSnow(Pane container) {
        if (!isSnowing) {
            return;
        }
        
        isSnowing = false;
        
        if (snowTimeline != null) {
            snowTimeline.stop();
            snowTimeline = null;
        }
        
        if (snowCanvas != null && container != null) {
            container.getChildren().remove(snowCanvas);
            snowCanvas = null;
        }
        
        if (snowflakes != null) {
            snowflakes.clear();
        }
    }
    
    /**
     * Initializes snowflakes across the canvas.
     */
    private static void initializeSnowflakes(Pane container) {
        if (container == null || snowCanvas == null) {
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
        
        // Create snowflakes - MINIMUM count to avoid covering plants
        // Much lower density, especially avoiding center area where garden grid is
        int flakeCount = (int)(width * height / 15000); // Very low density - minimal snowflakes
        flakeCount = Math.max(8, Math.min(25, flakeCount)); // Clamp between 8-25 (very minimal)
        
        if (snowflakes == null) {
            snowflakes = new ArrayList<>();
        }
        snowflakes.clear();
        
        // Garden grid is typically centered, so avoid center area
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double gardenAreaWidth = width * 0.4; // Assume garden takes ~40% of width
        double gardenAreaHeight = height * 0.5; // Assume garden takes ~50% of height
        
        int attempts = 0;
        for (int i = 0; i < flakeCount; i++) {
            Snowflake flake = new Snowflake();
            
            // Try to place snowflakes away from center (garden grid area)
            // Prefer edges and corners, avoid center
            do {
                if (random.nextDouble() < 0.7) {
                    // 70% chance: place in edge areas (avoid center)
                    if (random.nextBoolean()) {
                        // Left or right edge
                        flake.x = random.nextDouble() < 0.5 
                            ? random.nextDouble() * width * 0.2  // Left edge
                            : width - random.nextDouble() * width * 0.2; // Right edge
                        flake.y = random.nextDouble() * height;
                    } else {
                        // Top or bottom edge
                        flake.x = random.nextDouble() * width;
                        flake.y = random.nextDouble() < 0.5
                            ? random.nextDouble() * height * 0.2  // Top edge
                            : height - random.nextDouble() * height * 0.2; // Bottom edge
                    }
                } else {
                    // 30% chance: random placement (but still avoid center)
                    flake.x = random.nextDouble() * width;
                    flake.y = random.nextDouble() * height;
                }
                attempts++;
            } while (attempts < 50 && 
                     Math.abs(flake.x - centerX) < gardenAreaWidth / 2 &&
                     Math.abs(flake.y - centerY) < gardenAreaHeight / 2);
            
            flake.speed = 0.5 + random.nextDouble() * 1.5; // 0.5-2 pixels per frame (slower than rain)
            flake.size = 10 + random.nextDouble() * 12; // 10-22 pixels size (slightly bigger)
            flake.opacity = 0.7 + random.nextDouble() * 0.3; // 0.7-1.0 opacity (brighter for glow)
            flake.wobble = random.nextDouble() * Math.PI * 2; // Random starting wobble phase
            flake.wobbleSpeed = 0.02 + random.nextDouble() * 0.03; // Slow horizontal drift
            flake.wobbleAmount = 0.5 + random.nextDouble() * 1.5; // Amount of horizontal movement
            flake.rotation = random.nextDouble() * Math.PI * 2; // Random starting rotation
            flake.rotationSpeed = 0.01 + random.nextDouble() * 0.02; // Slow rotation speed
            snowflakes.add(flake);
        }
    }
    
    /**
     * Updates and draws all snowflakes.
     */
    private static void updateAndDrawSnow(Pane container) {
        if (snowCanvas == null || container == null || snowflakes == null) {
            return;
        }
        
        GraphicsContext gc = snowCanvas.getGraphicsContext2D();
        double width = snowCanvas.getWidth();
        double height = snowCanvas.getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // Clear canvas
        gc.clearRect(0, 0, width, height);
        
        // Update and draw each snowflake
        for (Snowflake flake : snowflakes) {
            // Update wobble (horizontal drift)
            flake.wobble += flake.wobbleSpeed;
            
            // Update rotation
            flake.rotation += flake.rotationSpeed;
            if (flake.rotation > Math.PI * 2) {
                flake.rotation -= Math.PI * 2;
            }
            
            // Move flake down and add horizontal drift
            flake.y += flake.speed;
            flake.x += Math.sin(flake.wobble) * flake.wobbleAmount;
            
            // Reset if flake goes off screen
            if (flake.y > height) {
                flake.y = -flake.size * 2; // Start from top
                flake.x = random.nextDouble() * width; // Random X position
            }
            
            // Wrap around horizontally
            if (flake.x < 0) {
                flake.x = width;
            } else if (flake.x > width) {
                flake.x = 0;
            }
            
            // Draw snowflake (6-pointed star pattern)
            drawSnowflake(gc, flake.x, flake.y, flake.size, flake.rotation, flake.opacity);
        }
        
        // Reinitialize if container size changed significantly
        if (Math.abs(width - (snowflakes.isEmpty() ? 0 : width)) > 100) {
            initializeSnowflakes(container);
        }
    }
    
    /**
     * Draws a highly detailed, branchy snowflake with multiple levels of branches at the given position.
     * Includes glowing effect for visibility.
     */
    private static void drawSnowflake(GraphicsContext gc, double x, double y, double size, double rotation, double opacity) {
        gc.save(); // Save current graphics state
        
        // Set more blue color (200, 230, 255 is a more blue-tinted white, like ice)
        // Clamp opacity to valid range (0.0-1.0)
        double clampedOpacity = Math.max(0.0, Math.min(1.0, opacity));
        double brightOpacity = Math.max(0.0, Math.min(1.0, opacity * 1.1)); // Slightly brighter but clamped
        double glowOpacity = Math.max(0.0, Math.min(1.0, opacity * 0.6)); // Outer glow layer
        
        // Create glowing colors - brighter blue for glow effect
        Color snowColor = Color.rgb(200, 230, 255, clampedOpacity);
        Color brightBlue = Color.rgb(180, 220, 255, brightOpacity); // Brighter blue for glow
        Color glowColor = Color.rgb(150, 200, 255, glowOpacity); // Outer glow layer
        
        // Translate to snowflake center and rotate
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(rotation));
        
        // Draw outer glow layer first (larger, more transparent) for glowing effect
        gc.setStroke(glowColor);
        gc.setLineWidth(2.0); // Thicker for glow
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI) / 3.0;
            double armLength = size * 1.1; // Slightly larger for glow
            double endX = Math.cos(angle) * armLength;
            double endY = Math.sin(angle) * armLength;
            gc.strokeLine(0, 0, endX, endY);
        }
        
        // Draw 6-pointed snowflake pattern with extensive branching
        // Main 6 arms (radiating from center)
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI) / 3.0; // 60 degrees apart
            double armLength = size;
            
            // Main arm
            double endX = Math.cos(angle) * armLength;
            double endY = Math.sin(angle) * armLength;
            gc.setStroke(snowColor);
            gc.setLineWidth(1.5); // Slightly thicker for visibility
            gc.strokeLine(0, 0, endX, endY);
            
            // First set of side branches (at 33% of arm)
            drawBranchSet(gc, angle, armLength * 0.33, size * 0.5, snowColor, 1.1);
            
            // Second set of side branches (at 50% of arm)
            drawBranchSet(gc, angle, armLength * 0.5, size * 0.45, snowColor, 1.0);
            
            // Third set of side branches (at 67% of arm)
            drawBranchSet(gc, angle, armLength * 0.67, size * 0.35, snowColor, 0.9);
            
            // Fourth set of side branches (at 80% of arm)
            drawBranchSet(gc, angle, armLength * 0.8, size * 0.25, snowColor, 0.8);
            
            // Tiny branches at the tip of each main arm
            double tipBranchLength = size * 0.25;
            double tipBranchAngle1 = angle + Math.PI / 4.5; // ~40 degrees
            double tipBranchAngle2 = angle - Math.PI / 4.5;
            
            gc.setLineWidth(0.9);
            double tipBranch1EndX = endX + Math.cos(tipBranchAngle1) * tipBranchLength;
            double tipBranch1EndY = endY + Math.sin(tipBranchAngle1) * tipBranchLength;
            gc.strokeLine(endX, endY, tipBranch1EndX, tipBranch1EndY);
            
            double tipBranch2EndX = endX + Math.cos(tipBranchAngle2) * tipBranchLength;
            double tipBranch2EndY = endY + Math.sin(tipBranchAngle2) * tipBranchLength;
            gc.strokeLine(endX, endY, tipBranch2EndX, tipBranch2EndY);
            
            // Sub-branches on the tip branches
            double subTipLength = size * 0.15;
            double subTip1MidX = endX + Math.cos(tipBranchAngle1) * tipBranchLength * 0.5;
            double subTip1MidY = endY + Math.sin(tipBranchAngle1) * tipBranchLength * 0.5;
            double subTip1EndX = subTip1MidX + Math.cos(tipBranchAngle1 + Math.PI / 3.0) * subTipLength;
            double subTip1EndY = subTip1MidY + Math.sin(tipBranchAngle1 + Math.PI / 3.0) * subTipLength;
            gc.strokeLine(subTip1MidX, subTip1MidY, subTip1EndX, subTip1EndY);
            
            double subTip2MidX = endX + Math.cos(tipBranchAngle2) * tipBranchLength * 0.5;
            double subTip2MidY = endY + Math.sin(tipBranchAngle2) * tipBranchLength * 0.5;
            double subTip2EndX = subTip2MidX + Math.cos(tipBranchAngle2 - Math.PI / 3.0) * subTipLength;
            double subTip2EndY = subTip2MidY + Math.sin(tipBranchAngle2 - Math.PI / 3.0) * subTipLength;
            gc.strokeLine(subTip2MidX, subTip2MidY, subTip2EndX, subTip2EndY);
        }
        
        // Draw glowing center hexagon with blue tint (with outer glow)
        // Outer glow ring first
        gc.setFill(glowColor);
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI) / 3.0;
            double radius = size * 0.35; // Larger for glow
            double px = Math.cos(angle) * radius;
            double py = Math.sin(angle) * radius;
            if (i == 0) {
                gc.beginPath();
                gc.moveTo(px, py);
            } else {
                gc.lineTo(px, py);
            }
        }
        gc.closePath();
        gc.fill();
        
        // Inner bright center
        gc.setFill(brightBlue);
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI) / 3.0;
            double radius = size * 0.25;
            double px = Math.cos(angle) * radius;
            double py = Math.sin(angle) * radius;
            if (i == 0) {
                gc.beginPath();
                gc.moveTo(px, py);
            } else {
                gc.lineTo(px, py);
            }
        }
        gc.closePath();
        gc.fill();
        
        // Bright center dot for extra glow
        gc.setFill(Color.rgb(200, 240, 255, brightOpacity));
        gc.fillOval(-size * 0.1, -size * 0.1, size * 0.2, size * 0.2);
        
        // Additional outer glow circles for enhanced glow effect
        gc.setStroke(glowColor);
        gc.setLineWidth(1.5);
        gc.strokeOval(-size * 0.4, -size * 0.4, size * 0.8, size * 0.8);
        gc.setStroke(Color.rgb(180, 220, 255, glowOpacity * 0.5));
        gc.setLineWidth(1.0);
        gc.strokeOval(-size * 0.5, -size * 0.5, size * 1.0, size * 1.0);
        
        gc.restore(); // Restore graphics state
    }
    
    /**
     * Helper method to draw a set of branches at a specific position on an arm.
     */
    private static void drawBranchSet(GraphicsContext gc, double mainAngle, double distance, double branchLength, Color color, double lineWidth) {
        double branchAngle1 = mainAngle + Math.PI / 3.0; // 60 degrees from main arm
        double branchAngle2 = mainAngle - Math.PI / 3.0;
        
        double branchStartX = Math.cos(mainAngle) * distance;
        double branchStartY = Math.sin(mainAngle) * distance;
        
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);
        
        // Main branch pair
        double branch1EndX = branchStartX + Math.cos(branchAngle1) * branchLength;
        double branch1EndY = branchStartY + Math.sin(branchAngle1) * branchLength;
        gc.strokeLine(branchStartX, branchStartY, branch1EndX, branch1EndY);
        
        double branch2EndX = branchStartX + Math.cos(branchAngle2) * branchLength;
        double branch2EndY = branchStartY + Math.sin(branchAngle2) * branchLength;
        gc.strokeLine(branchStartX, branchStartY, branch2EndX, branch2EndY);
        
        // Sub-branches on each branch (making it more dendritic)
        double subBranchLength = branchLength * 0.4;
        double subBranch1MidX = branchStartX + Math.cos(branchAngle1) * branchLength * 0.6;
        double subBranch1MidY = branchStartY + Math.sin(branchAngle1) * branchLength * 0.6;
        
        double subBranch1EndX = subBranch1MidX + Math.cos(branchAngle1 + Math.PI / 4.0) * subBranchLength;
        double subBranch1EndY = subBranch1MidY + Math.sin(branchAngle1 + Math.PI / 4.0) * subBranchLength;
        gc.setLineWidth(lineWidth * 0.7);
        gc.strokeLine(subBranch1MidX, subBranch1MidY, subBranch1EndX, subBranch1EndY);
        
        double subBranch2MidX = branchStartX + Math.cos(branchAngle2) * branchLength * 0.6;
        double subBranch2MidY = branchStartY + Math.sin(branchAngle2) * branchLength * 0.6;
        
        double subBranch2EndX = subBranch2MidX + Math.cos(branchAngle2 - Math.PI / 4.0) * subBranchLength;
        double subBranch2EndY = subBranch2MidY + Math.sin(branchAngle2 - Math.PI / 4.0) * subBranchLength;
        gc.strokeLine(subBranch2MidX, subBranch2MidY, subBranch2EndX, subBranch2EndY);
    }
    
    /**
     * Checks if snow is currently active.
     */
    public static boolean isSnowing() {
        return isSnowing;
    }
    
    /**
     * Data class for a single snowflake.
     */
    private static class Snowflake {
        double x, y; // Position
        double speed; // Pixels per frame (slower than rain)
        double size; // Size of snowflake
        double opacity; // 0.0 to 1.0
        double wobble; // Phase for horizontal wobble
        double wobbleSpeed; // Speed of wobble
        double wobbleAmount; // Amount of horizontal movement
        double rotation; // Current rotation angle
        double rotationSpeed; // Speed of rotation
    }
}

