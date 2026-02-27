package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Particle system for sparkles and pollen effects.
 * Creates floating, glowing particles throughout the garden.
 */
public class ParticleSystem extends Pane {
    private final Canvas particleCanvas;
    private final List<Particle> particles;
    private final Random random;
    private Timeline particleTimeline;
    private boolean isActive = true;
    
    public ParticleSystem() {
        this.particleCanvas = new Canvas();
        this.particles = new ArrayList<>();
        this.random = new Random();
        
        // Make canvas fill the pane
        particleCanvas.widthProperty().bind(this.widthProperty());
        particleCanvas.heightProperty().bind(this.heightProperty());
        particleCanvas.setMouseTransparent(true);
        
        this.getChildren().add(particleCanvas);
        
        // Initialize particles
        initializeParticles();
        
        // Start animation
        startParticleSystem();
    }
    
    /**
     * Initializes sparkle and pollen particles.
     */
    private void initializeParticles() {
        for (int i = 0; i < 30; i++) {
            addRandomParticle();
        }
    }
    
    /**
     * Adds a random particle at random position.
     */
    private void addRandomParticle() {
        Particle p = new Particle();
        p.x = random.nextDouble() * (particleCanvas.getWidth() > 0 ? particleCanvas.getWidth() : 800);
        p.y = random.nextDouble() * (particleCanvas.getHeight() > 0 ? particleCanvas.getHeight() : 600);
        p.vx = (random.nextDouble() - 0.5) * 0.5;
        p.vy = -random.nextDouble() * 0.8 - 0.2; // Float upward
        p.size = 2 + random.nextDouble() * 3;
        p.lifetime = 5 + random.nextDouble() * 10;
        p.age = 0;
        
        // Random type: sparkle or pollen
        if (random.nextDouble() < 0.5) {
            p.type = ParticleType.SPARKLE;
            p.color = Color.rgb(255, 255, 150, 0.8); // Yellow sparkle
        } else {
            p.type = ParticleType.POLLEN;
            p.color = Color.rgb(255, 215, 0, 0.6); // Gold pollen
        }
        
        particles.add(p);
    }
    
    /**
     * Starts the particle animation system.
     */
    private void startParticleSystem() {
        particleTimeline = new Timeline(
            new KeyFrame(Duration.millis(1000.0 / 60.0), e -> {
                if (isActive) {
                    updateParticles();
                }
            })
        );
        particleTimeline.setCycleCount(Animation.INDEFINITE);
        particleTimeline.play();
        
        // Periodically add new particles
        Timeline spawnTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> {
                if (isActive && particles.size() < 50) {
                    addRandomParticle();
                }
            })
        );
        spawnTimeline.setCycleCount(Animation.INDEFINITE);
        spawnTimeline.play();
    }
    
    /**
     * Updates all particles.
     */
    private void updateParticles() {
        GraphicsContext gc = particleCanvas.getGraphicsContext2D();
        double width = particleCanvas.getWidth();
        double height = particleCanvas.getHeight();
        
        // Clear canvas with transparent background
        gc.clearRect(0, 0, width, height);
        
        // Update and draw particles
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            
            // Update position
            p.x += p.vx;
            p.y += p.vy;
            
            // Update age
            p.age += 0.016; // ~60 FPS
            
            // Remove if expired
            if (p.age > p.lifetime || p.y < -10 || p.y > height + 10) {
                it.remove();
                continue;
            }
            
            // Draw particle
            drawParticle(gc, p);
        }
    }
    
    /**
     * Draws a particle (sparkle or pollen).
     */
    private void drawParticle(GraphicsContext gc, Particle p) {
        double alpha = 1.0 - (p.age / p.lifetime);
        alpha = Math.max(0, Math.min(1, alpha));
        
        gc.setGlobalAlpha(alpha);
        
        if (p.type == ParticleType.SPARKLE) {
            // Draw sparkle as a star/cross
            gc.setFill(p.color.deriveColor(0, 1, 1, alpha));
            gc.setStroke(p.color.deriveColor(0, 1, 1.5, alpha));
            
            double size = p.size;
            gc.fillOval(p.x - size/2, p.y - size/2, size, size);
            
            // Add cross lines for sparkle effect
            gc.setLineWidth(1);
            gc.strokeLine(p.x - size, p.y, p.x + size, p.y);
            gc.strokeLine(p.x, p.y - size, p.x, p.y + size);
        } else {
            // Draw pollen as a small circle
            gc.setFill(p.color.deriveColor(0, 1, 1, alpha));
            gc.fillOval(p.x - p.size/2, p.y - p.size/2, p.size, p.size);
        }
        
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * Creates a burst of sparkles at a specific location.
     */
    public void createSparkleBurst(double x, double y) {
        for (int i = 0; i < 10; i++) {
            Particle p = new Particle();
            p.x = x;
            p.y = y;
            double angle = (i / 10.0) * Math.PI * 2;
            double speed = 1 + random.nextDouble() * 2;
            p.vx = Math.cos(angle) * speed;
            p.vy = Math.sin(angle) * speed;
            p.size = 3 + random.nextDouble() * 4;
            p.lifetime = 1 + random.nextDouble() * 1.5;
            p.age = 0;
            p.type = ParticleType.SPARKLE;
            
            // Colorful sparkles
            double hue = random.nextDouble() * 60; // Yellow to orange range
            p.color = Color.hsb(hue, 0.8, 1.0, 1.0);
            
            particles.add(p);
        }
    }
    
    /**
     * Stops the particle system.
     */
    public void stop() {
        isActive = false;
        if (particleTimeline != null) {
            particleTimeline.stop();
        }
    }
    
    /**
     * Particle data structure.
     */
    private static class Particle {
        double x, y, vx, vy, size, lifetime, age;
        Color color;
        ParticleType type;
    }
    
    /**
     * Particle types.
     */
    private enum ParticleType {
        SPARKLE, POLLEN
    }
}

