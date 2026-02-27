package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.Random;

/**
 * Decorative animated elements (colorful butterflies, bees, leaves).
 * Provides game-like cheerful animations.
 */
public class DecorativeElements {
    private static final Random random = new Random();
    
    // Butterfly color palette
    private static final String[] BUTTERFLY_COLORS = {
        "#FF93EC", // Pink
        "#7DCBFF", // Blue
        "#FFE866", // Yellow
        "#CFA6FF"  // Purple
    };
    
    /**
     * Creates a colorful animated butterfly with sinusoidal flight.
     */
    public static Label createButterfly(Pane container) {
        // Choose random color
        String colorHex = BUTTERFLY_COLORS[random.nextInt(BUTTERFLY_COLORS.length)];
        
        Label butterfly = new Label("🦋");
        butterfly.setFont(javafx.scene.text.Font.font(24));
        butterfly.setStyle("-fx-text-fill: " + colorHex + ";");
        
        // Get container dimensions
        double containerWidth = container.getWidth() > 0 ? container.getWidth() : 800;
        double containerHeight = container.getHeight() > 0 ? container.getHeight() : 600;
        
        // Random starting position (off screen left or random)
        double startX = -30;
        double startY = random.nextDouble() * containerHeight * 0.6 + containerHeight * 0.2;
        butterfly.setLayoutX(startX);
        butterfly.setLayoutY(startY);
        
        // Wing flapping animation (faster, more realistic)
        ScaleTransition wingFlap = new ScaleTransition(Duration.millis(250), butterfly);
        wingFlap.setFromX(1.0);
        wingFlap.setFromY(1.0);
        wingFlap.setToX(1.15);
        wingFlap.setToY(0.85);
        wingFlap.setAutoReverse(true);
        wingFlap.setCycleCount(Animation.INDEFINITE);
        wingFlap.setInterpolator(Interpolator.EASE_BOTH);
        wingFlap.play();
        
        // Sinusoidal flight path (smooth wave pattern)
        double baseY = startY;
        double speed = 0.5 + random.nextDouble() * 0.5; // 0.5-1.0 speed
        double amplitude = 30 + random.nextDouble() * 30; // 30-60px amplitude
        
        Timeline flightPath = new Timeline();
        flightPath.setCycleCount(Animation.INDEFINITE);
        
        // Create smooth sinusoidal movement over time
        KeyValue[] keyValues = new KeyValue[20];
        KeyFrame[] keyFrames = new KeyFrame[20];
        
        for (int i = 0; i < 20; i++) {
            double progress = i / 20.0;
            double x = startX + (progress * (containerWidth + 60)); // Move across screen
            double y = baseY + Math.sin(progress * Math.PI * 4) * amplitude; // Sinusoidal Y movement
            double time = progress * (10.0 / speed); // Time based on speed
            
            keyFrames[i] = new KeyFrame(
                Duration.seconds(time),
                new KeyValue(butterfly.layoutXProperty(), x),
                new KeyValue(butterfly.layoutYProperty(), y)
            );
        }
        
        flightPath.getKeyFrames().addAll(keyFrames);
        flightPath.setOnFinished(e -> {
            // Loop back to start
            butterfly.setLayoutX(-30);
            butterfly.setLayoutY(baseY);
            flightPath.play();
        });
        flightPath.play();
        
        container.getChildren().add(butterfly);
        return butterfly;
    }
    
    /**
     * Creates an animated bee with fast buzzing movement.
     */
    public static Label createBee(Pane container) {
        Label bee = new Label("🐝");
        bee.setFont(javafx.scene.text.Font.font(20));
        
        // Get container dimensions
        double containerWidth = container.getWidth() > 0 ? container.getWidth() : 800;
        double containerHeight = container.getHeight() > 0 ? container.getHeight() : 600;
        
        // Random starting position
        double startX = random.nextDouble() * containerWidth;
        double startY = random.nextDouble() * containerHeight * 0.7 + containerHeight * 0.1;
        bee.setLayoutX(startX);
        bee.setLayoutY(startY);
        
        // Fast buzzing jitter (x ±3px, y ±3px at ~40ms interval)
        Timeline buzzJitter = new Timeline();
        buzzJitter.setCycleCount(Animation.INDEFINITE);
        
        // Create rapid jittering motion
        for (int i = 0; i < 10; i++) {
            double jitterX = (random.nextDouble() - 0.5) * 6; // ±3px
            double jitterY = (random.nextDouble() - 0.5) * 6; // ±3px
            double time = i * 0.04; // 40ms intervals
            
            buzzJitter.getKeyFrames().add(
                new KeyFrame(
                    Duration.seconds(time),
                    new KeyValue(bee.translateXProperty(), jitterX),
                    new KeyValue(bee.translateYProperty(), jitterY)
                )
            );
        }
        
        // Loop the jitter pattern
        buzzJitter.setAutoReverse(true);
        buzzJitter.play();
        
        // Overall movement path (zigzag pattern)
        TranslateTransition movement = new TranslateTransition(Duration.seconds(3), bee);
        movement.setByX(100 + random.nextDouble() * 100);
        movement.setByY((random.nextDouble() - 0.5) * 80);
        movement.setAutoReverse(true);
        movement.setCycleCount(Animation.INDEFINITE);
        movement.setInterpolator(Interpolator.EASE_BOTH);
        movement.play();
        
        // Small scale pulsing for wing movement
        ScaleTransition wingPulse = new ScaleTransition(Duration.millis(100), bee);
        wingPulse.setFromX(1.0);
        wingPulse.setFromY(1.0);
        wingPulse.setToX(1.05);
        wingPulse.setToY(0.95);
        wingPulse.setAutoReverse(true);
        wingPulse.setCycleCount(Animation.INDEFINITE);
        wingPulse.play();
        
        container.getChildren().add(bee);
        return bee;
    }
    
    /**
     * Creates multiple colorful butterflies (3-5).
     */
    public static void createButterflies(Pane container, int count) {
        for (int i = 0; i < count; i++) {
            // Stagger creation slightly for variety
            Duration delay = Duration.millis(i * 500);
            Timeline delayTimeline = new Timeline(
                new KeyFrame(delay, e -> createButterfly(container))
            );
            delayTimeline.play();
        }
    }
    
    /**
     * Creates multiple bees (2-3).
     */
    public static void createBees(Pane container, int count) {
        for (int i = 0; i < count; i++) {
            Duration delay = Duration.millis(i * 800);
            Timeline delayTimeline = new Timeline(
                new KeyFrame(delay, e -> createBee(container))
            );
            delayTimeline.play();
        }
    }
    
    /**
     * Creates a floating leaf.
     */
    public static Label createLeaf(Pane container) {
        Label leaf = new Label("🍃");
        leaf.setFont(javafx.scene.text.Font.font(16));
        leaf.setOpacity(0.7);
        
        double containerWidth = container.getWidth() > 0 ? container.getWidth() : 800;
        double containerHeight = container.getHeight() > 0 ? container.getHeight() : 600;
        
        double startX = random.nextDouble() * containerWidth;
        double startY = containerHeight;
        leaf.setLayoutX(startX);
        leaf.setLayoutY(startY);
        
        // Falling and floating animation
        TranslateTransition fall = new TranslateTransition(Duration.seconds(10), leaf);
        fall.setFromY(0);
        fall.setToY(-containerHeight - 50);
        fall.setInterpolator(Interpolator.LINEAR);
        
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), leaf);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        
        FadeTransition fade = new FadeTransition(Duration.seconds(2), leaf);
        fade.setFromValue(0.7);
        fade.setToValue(0.3);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);
        
        fall.setOnFinished(e -> {
            container.getChildren().remove(leaf);
        });
        
        rotate.play();
        fade.play();
        fall.play();
        
        container.getChildren().add(leaf);
        return leaf;
    }
    
    /**
     * Creates an animated small bird flying across the screen.
     */
    public static Label createBird(Pane container) {
        Label bird = new Label("🐦");
        bird.setFont(javafx.scene.text.Font.font(18));
        
        double containerWidth = container.getWidth() > 0 ? container.getWidth() : 800;
        double containerHeight = container.getHeight() > 0 ? container.getHeight() : 600;
        
        // Start off-screen left
        double startX = -30;
        double startY = 80 + random.nextDouble() * (containerHeight * 0.3);
        bird.setLayoutX(startX);
        bird.setLayoutY(startY);
        
        // Flapping animation
        ScaleTransition flap = new ScaleTransition(Duration.millis(200), bird);
        flap.setFromX(1.0);
        flap.setFromY(1.0);
        flap.setToX(1.15);
        flap.setToY(0.9);
        flap.setAutoReverse(true);
        flap.setCycleCount(Animation.INDEFINITE);
        flap.play();
        
        // Flying across screen with slight wave
        double endX = containerWidth + 30;
        double speed = 0.8 + random.nextDouble() * 0.4;
        
        Timeline flightPath = new Timeline();
        flightPath.setCycleCount(Animation.INDEFINITE);
        
        KeyFrame[] keyFrames = new KeyFrame[15];
        for (int i = 0; i < 15; i++) {
            double progress = i / 14.0;
            double x = startX + (progress * (endX - startX));
            double y = startY + Math.sin(progress * Math.PI * 2) * 15; // Gentle wave
            double time = progress * (8.0 / speed);
            
            keyFrames[i] = new KeyFrame(
                Duration.seconds(time),
                new KeyValue(bird.layoutXProperty(), x),
                new KeyValue(bird.layoutYProperty(), y)
            );
        }
        
        flightPath.getKeyFrames().addAll(keyFrames);
        flightPath.setOnFinished(e -> {
            bird.setLayoutX(-30);
            bird.setLayoutY(startY);
            flightPath.play();
        });
        flightPath.play();
        
        container.getChildren().add(bird);
        return bird;
    }
    
    /**
     * Creates multiple birds (1-2).
     */
    public static void createBirds(Pane container, int count) {
        for (int i = 0; i < count; i++) {
            Duration delay = Duration.millis(i * 3000);
            Timeline delayTimeline = new Timeline(
                new KeyFrame(delay, e -> createBird(container))
            );
            delayTimeline.play();
        }
    }
    
}

