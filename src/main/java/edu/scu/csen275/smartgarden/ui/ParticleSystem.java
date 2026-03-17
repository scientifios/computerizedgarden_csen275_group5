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
 * Lightweight particle overlay rendered on a canvas.
 * Only transient burst effects are emitted; ambient background sparkles are disabled.
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

        particleCanvas.widthProperty().bind(this.widthProperty());
        particleCanvas.heightProperty().bind(this.heightProperty());
        particleCanvas.setMouseTransparent(true);

        this.getChildren().add(particleCanvas);

        startParticleSystem();
    }

    /**
     * Starts the animation timer for particle updates.
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
    }

    /**
     * Advances particle state and redraws the canvas.
     */
    private void updateParticles() {
        GraphicsContext gc = particleCanvas.getGraphicsContext2D();
        double width = particleCanvas.getWidth();
        double height = particleCanvas.getHeight();

        gc.clearRect(0, 0, width, height);

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();

            p.x += p.vx;
            p.y += p.vy;

            p.age += 0.016;

            if (p.age > p.lifetime || p.y < -10 || p.y > height + 10) {
                it.remove();
                continue;
            }

            drawParticle(gc, p);
        }
    }

    /**
     * Renders a particle with fade-out based on its lifetime.
     */
    private void drawParticle(GraphicsContext gc, Particle p) {
        double alpha = 1.0 - (p.age / p.lifetime);
        alpha = Math.max(0, Math.min(1, alpha));

        gc.setGlobalAlpha(alpha);

        if (p.type == ParticleType.SPARKLE) {
            gc.setFill(p.color.deriveColor(0, 1, 1, alpha));
            gc.setStroke(p.color.deriveColor(0, 1, 1.5, alpha));

            double size = p.size;
            gc.fillOval(p.x - size / 2, p.y - size / 2, size, size);

            gc.setLineWidth(1);
            gc.strokeLine(p.x - size, p.y, p.x + size, p.y);
            gc.strokeLine(p.x, p.y - size, p.x, p.y + size);
        } else {
            gc.setFill(p.color.deriveColor(0, 1, 1, alpha));
            gc.fillOval(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }

        gc.setGlobalAlpha(1.0);
    }

    /**
     * Emits a short-lived burst effect centered at the given coordinates.
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

            double hue = random.nextDouble() * 60;
            p.color = Color.hsb(hue, 0.8, 1.0, 1.0);

            particles.add(p);
        }
    }

    /**
     * Disables updates and stops running timelines.
     */
    public void stop() {
        isActive = false;
        if (particleTimeline != null) {
            particleTimeline.stop();
        }
    }

    private static class Particle {
        double x, y, vx, vy, size, lifetime, age;
        Color color;
        ParticleType type;
    }

    private enum ParticleType {
        SPARKLE, POLLEN
    }
}
