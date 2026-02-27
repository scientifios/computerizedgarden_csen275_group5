package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.Random;

/**
 * Animated sprite for harmful pests (Set C: Red Mite, Green Leaf Worm, Black Beetle, Brown Caterpillar).
 * Slow crawl or jitter movement with periodic damage animations.
 * Uses Text node for proper emoji rendering support.
 */
public class PestSprite extends StackPane {
    private final String pestType;
    private final Random random;
    private final Text emojiText;
    private TranslateTransition crawlAnimation;
    private RotateTransition wiggleAnimation;
    private Timeline damageAnimation;
    private boolean isAlive;
    
    // Emoji size: 20-28px for visibility
    private static final int EMOJI_SIZE = 24;
    private static final int SPRITE_SIZE = 28;
    
    public PestSprite(String pestType) {
        this.pestType = pestType;
        this.random = new Random();
        this.isAlive = true;
        
        // Use Text node for proper emoji rendering
        String emoji = getPestEmoji(pestType);
        emojiText = new Text(emoji);
        
        // Use system font that supports emojis - smaller size (20-28px)
        try {
            Font emojiFont = Font.font("Segoe UI Emoji", EMOJI_SIZE);
            emojiText.setFont(emojiFont);
        } catch (Exception e) {
            try {
                Font emojiFont = Font.font("Apple Color Emoji", EMOJI_SIZE);
                emojiText.setFont(emojiFont);
            } catch (Exception e2) {
                emojiText.setFont(Font.font(EMOJI_SIZE));
            }
        }
        
        emojiText.setFill(javafx.scene.paint.Color.BLACK);
        emojiText.setStyle("-fx-font-weight: normal;");
        
        // Add text to StackPane
        this.getChildren().add(emojiText);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: transparent;");
        
        // Set initial random position within tile bounds
        setLayoutX(10 + random.nextDouble() * 20);
        setLayoutY(10 + random.nextDouble() * 20);
        
        // Make sure sprite is visible and on top
        setVisible(true);
        setOpacity(1.0);
        setMouseTransparent(true);
        
        // Ensure sprite is properly sized (20-28px)
        setMinSize(SPRITE_SIZE, SPRITE_SIZE);
        setMaxSize(SPRITE_SIZE, SPRITE_SIZE);
        setPrefSize(SPRITE_SIZE, SPRITE_SIZE);
        
        // Add tooltip to show pest type
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(pestType + " (Harmful Pest)");
        javafx.scene.control.Tooltip.install(this, tooltip);
        
        setupAnimations();
    }
    
    /**
     * Gets emoji for pest type (Set C) - colorful emoji-based definitions.
     */
    private String getPestEmoji(String type) {
        return switch (type.toLowerCase()) {
            case "red mite" -> "🐞";           // Just the mite emoji
            case "green leaf worm" -> "🐛";    // Just the worm emoji
            case "black beetle" -> "🪲";        // Just the beetle emoji
            case "brown caterpillar" -> "🐛";  // Just the caterpillar emoji (same as worm)
            default -> "🐞";                   // Red Mite (default)
        };
    }
    
    /**
     * Sets up slow crawl or jitter movement animation for harmful pests.
     */
    private void setupAnimations() {
        // Slow crawl animation - gentle movement pattern
        crawlAnimation = new TranslateTransition(Duration.millis(3000 + random.nextInt(2000)), this);
        double offsetX = -3 - random.nextDouble() * 4;
        double offsetY = -3 - random.nextDouble() * 4;
        crawlAnimation.setFromX(0);
        crawlAnimation.setToX(offsetX);
        crawlAnimation.setFromY(0);
        crawlAnimation.setToY(offsetY);
        crawlAnimation.setInterpolator(Interpolator.EASE_BOTH);
        crawlAnimation.setCycleCount(Animation.INDEFINITE);
        crawlAnimation.setAutoReverse(true);
        crawlAnimation.play();
        
        // Jitter animation - small random movements for some pests
        if (random.nextDouble() < 0.5) {
            Timeline jitter = new Timeline(
                new KeyFrame(Duration.millis(100 + random.nextInt(100)), e -> {
                    if (isAlive) {
                        setTranslateX(getTranslateX() + (random.nextDouble() - 0.5) * 1.5);
                        setTranslateY(getTranslateY() + (random.nextDouble() - 0.5) * 1.5);
                    }
                })
            );
            jitter.setCycleCount(Animation.INDEFINITE);
            jitter.play();
        }
        
        // Periodic wiggle animation (every 2-3 seconds)
        wiggleAnimation = new RotateTransition(Duration.millis(200), this);
        wiggleAnimation.setFromAngle(-8);
        wiggleAnimation.setToAngle(8);
        wiggleAnimation.setCycleCount(2);
        wiggleAnimation.setAutoReverse(true);
        
        // Damage tick animation - wiggle every 2-3 seconds
        damageAnimation = new Timeline(
            new KeyFrame(Duration.seconds(2 + random.nextDouble()), e -> {
                if (isAlive) {
                    wiggleAnimation.playFromStart();
                }
            })
        );
        damageAnimation.setCycleCount(Animation.INDEFINITE);
        damageAnimation.play();
    }
    
    /**
     * Animates pest death when pesticide is applied (shrink and fade animation).
     */
    public void animateDeath(Runnable onComplete) {
        if (!isAlive) return;
        isAlive = false;
        
        // Stop all animations
        crawlAnimation.stop();
        damageAnimation.stop();
        if (wiggleAnimation != null) wiggleAnimation.stop();
        
        // Shrink and fade out animation
        ScaleTransition shrink = new ScaleTransition(Duration.millis(600), this);
        shrink.setFromX(1.0);
        shrink.setFromY(1.0);
        shrink.setToX(0.3);
        shrink.setToY(0.3);
        shrink.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(Duration.millis(600), this);
        fade.setFromValue(1.0);
        fade.setToValue(0);
        
        ParallelTransition death = new ParallelTransition(shrink, fade);
        death.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        death.play();
    }
    
    public String getPestType() {
        return pestType;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    /**
     * Triggers attack animation (wiggle).
     */
    public void animateAttack() {
        if (wiggleAnimation != null) {
            wiggleAnimation.playFromStart();
        }
    }
    
    public void stopAnimations() {
        if (crawlAnimation != null) crawlAnimation.stop();
        if (damageAnimation != null) damageAnimation.stop();
        if (wiggleAnimation != null) wiggleAnimation.stop();
    }
}
