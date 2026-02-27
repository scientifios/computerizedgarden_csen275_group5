package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Overlay component that manages pest visuals, damage effects, and warning indicators on a tile.
 */
public class PestTileOverlay extends StackPane {
    private final Canvas damageCanvas; // For brown tint and bite marks
    private final StackPane spriteContainer; // For pest sprites
    private final List<PestSprite> pests;
    private final List<Label> pestTypeLabels; // Labels showing pest type names
    private final Random random;
    private Timeline damageVisualTimeline;
    private List<BiteMark> biteMarks;
    private boolean isUnderAttack;
    private double tileWidth = 60;
    private double tileHeight = 60;
    
    public PestTileOverlay() {
        this(60, 60);
    }
    
    public PestTileOverlay(double width, double height) {
        this.random = new Random();
        this.pests = new ArrayList<>();
        this.pestTypeLabels = new ArrayList<>();
        this.biteMarks = new ArrayList<>();
        this.isUnderAttack = false;
        
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);
        
        // Damage canvas for brown tint and bite marks
        damageCanvas = new Canvas();
        damageCanvas.widthProperty().bind(this.widthProperty());
        damageCanvas.heightProperty().bind(this.heightProperty());
        damageCanvas.setMouseTransparent(true);
        
        // Container for pest sprites
        spriteContainer = new StackPane();
        spriteContainer.setAlignment(Pos.CENTER);
        spriteContainer.setMouseTransparent(true);
        spriteContainer.setPickOnBounds(false);
        spriteContainer.setPrefSize(tileWidth, tileHeight);
        spriteContainer.setMinSize(tileWidth, tileHeight);
        spriteContainer.setMaxSize(tileWidth, tileHeight);
        
        // CRITICAL: Add children in correct z-order - sprites must be ABOVE damage canvas
        this.getChildren().addAll(damageCanvas, spriteContainer);
        
        // Ensure sprite container is always on top of damage canvas
        spriteContainer.toFront();
        
        // Make sure overlay is set up correctly
        this.setPickOnBounds(false);
        this.setMouseTransparent(true);
        
        // Make sprite container visible and on top
        spriteContainer.setPickOnBounds(false);
        spriteContainer.setMouseTransparent(true);
        
        // Bind tile size
        this.widthProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                tileWidth = newVal.doubleValue();
                updateDamageVisuals();
            }
        });
        this.heightProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                tileHeight = newVal.doubleValue();
                updateDamageVisuals();
            }
        });
    }
    
    /**
     * Adds a harmful pest to this tile.
     * FIXED: Ensure pest sprite is always visible and properly positioned.
     */
    public void addPest(PestSprite pest) {
        pests.add(pest);
        spriteContainer.getChildren().add(pest);
        isUnderAttack = true;
        
        // Position pest within tile bounds (no label, no warning indicator)
        double pestX = (tileWidth - 40) / 2 + (random.nextDouble() - 0.5) * 15;
        double pestY = (tileHeight - 40) / 2 + (random.nextDouble() - 0.5) * 15;
        pest.setLayoutX(pestX);
        pest.setLayoutY(pestY);
        
        // CRITICAL: Make sure everything is visible and on top
        this.setVisible(true);
        this.setOpacity(1.0);
        this.setPickOnBounds(false);
        this.setMouseTransparent(true);
        
        pest.setVisible(true);
        pest.setOpacity(1.0);
        pest.setMouseTransparent(true);
        
        // Make sure sprite container is sized correctly and fills the tile
        spriteContainer.setPrefSize(tileWidth, tileHeight);
        spriteContainer.setMinSize(tileWidth, tileHeight);
        spriteContainer.setMaxSize(tileWidth, tileHeight);
        spriteContainer.setAlignment(Pos.CENTER);
        spriteContainer.setVisible(true);
        spriteContainer.setOpacity(1.0);
        spriteContainer.setPickOnBounds(false);
        spriteContainer.setMouseTransparent(true);
        
        // Bring everything to front
        pest.toFront();
        spriteContainer.toFront();
        this.toFront();
        
        // Force layout update
        this.requestLayout();
        spriteContainer.requestLayout();
        pest.requestLayout();
        
        // Verify pest is in container
        if (!spriteContainer.getChildren().contains(pest)) {
            System.err.println("[PestTileOverlay] ERROR: Pest not in spriteContainer!");
        }
        
        updateDamageVisuals();
        startDamageVisualAnimation();
    }
    
    /**
     * Removes a pest from this tile.
     */
    public void removePest(PestSprite pest) {
        pests.remove(pest);
        spriteContainer.getChildren().remove(pest);
        
        // Remove associated label
        Label labelToRemove = null;
        for (Label label : pestTypeLabels) {
            if (label.getText().equals(pest.getPestType())) {
                labelToRemove = label;
                break;
            }
        }
        if (labelToRemove != null) {
            pestTypeLabels.remove(labelToRemove);
            spriteContainer.getChildren().remove(labelToRemove);
        }
        
        if (pests.isEmpty()) {
            isUnderAttack = false;
            stopDamageVisualAnimation();
            clearDamageVisuals();
        }
    }
    
    /**
     * Clears all pests from this tile.
     */
    public void clearAllPests() {
        // Remove all pests
        for (PestSprite pest : new ArrayList<>(pests)) {
            spriteContainer.getChildren().remove(pest);
        }
        pests.clear();
        
        // Remove all pest type labels
        for (Label label : new ArrayList<>(pestTypeLabels)) {
            spriteContainer.getChildren().remove(label);
        }
        pestTypeLabels.clear();
        
        // Reset state
        isUnderAttack = false;
        stopDamageVisualAnimation();
        clearDamageVisuals();
    }
    
    /**
     * Applies damage visual effects (brown tint, bite marks).
     */
    public void applyDamageVisual(int intensity) {
        // Add random bite marks
        for (int i = 0; i < intensity; i++) {
            BiteMark bite = new BiteMark();
            bite.x = 10 + random.nextDouble() * (tileWidth - 20);
            bite.y = 10 + random.nextDouble() * (tileHeight - 20);
            bite.size = 2 + random.nextDouble() * 2;
            biteMarks.add(bite);
        }
        updateDamageVisuals();
    }
    
    /**
     * Clears all damage visuals.
     */
    public void clearDamageVisuals() {
        biteMarks.clear();
        updateDamageVisuals();
    }
    
    /**
     * Updates damage canvas with brown tint and bite marks.
     */
    private void updateDamageVisuals() {
        GraphicsContext gc = damageCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, tileWidth, tileHeight);
        
        if (isUnderAttack && !pests.isEmpty()) {
            // Brown tint overlay
            double opacity = 0.3 * Math.min(1.0, pests.size() * 0.3);
            gc.setFill(Color.color(139.0/255.0, 90.0/255.0, 43.0/255.0, opacity)); // More intense with more pests
            gc.fillRect(0, 0, tileWidth, tileHeight);
            
            // Draw bite marks
            gc.setFill(Color.rgb(101, 67, 33));
            for (BiteMark bite : biteMarks) {
                gc.fillOval(bite.x, bite.y, bite.size, bite.size);
            }
        }
    }
    
    /**
     * Starts continuous damage visual animation.
     */
    private void startDamageVisualAnimation() {
        if (damageVisualTimeline != null) {
            damageVisualTimeline.stop();
        }
        
        damageVisualTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2 + random.nextDouble()), e -> {
                if (isUnderAttack && !pests.isEmpty()) {
                    applyDamageVisual(1); // Add one bite mark every 2-3 seconds
                    updateDamageVisuals();
                }
            })
        );
        damageVisualTimeline.setCycleCount(Animation.INDEFINITE);
        damageVisualTimeline.play();
    }
    
    /**
     * Stops damage visual animation.
     */
    private void stopDamageVisualAnimation() {
        if (damageVisualTimeline != null) {
            damageVisualTimeline.stop();
            damageVisualTimeline = null;
        }
    }
    
    public List<PestSprite> getPests() {
        return new ArrayList<>(pests);
    }
    
    public boolean isUnderAttack() {
        return isUnderAttack;
    }
    
    /**
     * Triggers leaf shake animation (called from AnimatedTile).
     */
    public void triggerLeafShake() {
        // Leaf shake is handled by AnimatedTile, but this can be used for additional effects
    }
    
    /**
     * Helper class for bite mark positions.
     */
    private static class BiteMark {
        double x, y, size;
    }
}
