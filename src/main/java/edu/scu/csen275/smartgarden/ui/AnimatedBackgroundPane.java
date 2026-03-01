package edu.scu.csen275.smartgarden.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Static background pane with sky gradient, sun, and clouds.
 * No background animations.
 */
public class AnimatedBackgroundPane extends Pane {
    private final Canvas backgroundCanvas;
    private final List<Cloud> clouds;
    private final List<SunRay> sunRays;
    private final Random random;
    private boolean isSunny = true;

    public AnimatedBackgroundPane() {
        this.backgroundCanvas = new Canvas();
        this.clouds = new ArrayList<>();
        this.sunRays = new ArrayList<>();
        this.random = new Random();

        backgroundCanvas.widthProperty().bind(this.widthProperty());
        backgroundCanvas.heightProperty().bind(this.heightProperty());
        this.getChildren().add(backgroundCanvas);

        initializeClouds();
        initializeSunRays();

        // Redraw only when size changes.
        widthProperty().addListener((obs, oldV, newV) -> drawBackground());
        heightProperty().addListener((obs, oldV, newV) -> drawBackground());
        drawBackground();
    }

    private void initializeClouds() {
        for (int i = 0; i < 4; i++) {
            Cloud cloud = new Cloud();
            cloud.x = random.nextDouble() * 1200;
            cloud.y = 50 + random.nextDouble() * 150;
            cloud.size = 80 + random.nextDouble() * 60;
            cloud.opacity = 0.6 + random.nextDouble() * 0.3;
            clouds.add(cloud);
        }
    }

    private void initializeSunRays() {
        for (int i = 0; i < 3; i++) {
            SunRay ray = new SunRay();
            ray.angle = -45 + i * 15;
            ray.opacity = 0.15 + random.nextDouble() * 0.1;
            ray.width = 30 + random.nextDouble() * 20;
            sunRays.add(ray);
        }
    }

    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();
        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        gc.clearRect(0, 0, width, height);
        drawSkyGradient(gc, width, height);

        if (isSunny) {
            drawSunIcon(gc, width, height);
            drawSunRays(gc, width, height);
        }

        for (Cloud cloud : clouds) {
            if (cloud.x > -cloud.size && cloud.x < width + cloud.size) {
                drawCloud(gc, cloud);
            }
        }
    }

    private void drawSkyGradient(GraphicsContext gc, double width, double height) {
        LinearGradient skyGradient;

        if (isSunny) {
            skyGradient = new LinearGradient(
                0, 0, 0, height,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(135, 206, 250)),
                new Stop(0.3, Color.rgb(176, 224, 255)),
                new Stop(0.6, Color.rgb(200, 255, 200)),
                new Stop(1, Color.rgb(173, 255, 173))
            );
        } else {
            skyGradient = new LinearGradient(
                0, 0, 0, height,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(100, 150, 180)),
                new Stop(0.4, Color.rgb(120, 170, 200)),
                new Stop(0.7, Color.rgb(140, 200, 180)),
                new Stop(1, Color.rgb(120, 200, 150))
            );
        }

        gc.setFill(skyGradient);
        gc.fillRect(0, 0, width, height);
    }

    private void drawSunIcon(GraphicsContext gc, double width, double height) {
        double sunX = width * 0.85;
        double sunY = height * 0.08;
        double sunRadius = 50;

        RadialGradient sunGlow = new RadialGradient(
            0, 0, sunX, sunY, sunRadius * 1.5,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 255, 200, 0.6)),
            new Stop(0.5, Color.rgb(255, 255, 150, 0.3)),
            new Stop(1, Color.rgb(255, 255, 200, 0.0))
        );
        gc.setFill(sunGlow);
        gc.fillOval(sunX - sunRadius * 1.5, sunY - sunRadius * 1.5, sunRadius * 3, sunRadius * 3);

        RadialGradient sunGradient = new RadialGradient(
            0, 0, sunX, sunY, sunRadius,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 255, 100)),
            new Stop(0.7, Color.rgb(255, 220, 50)),
            new Stop(1, Color.rgb(255, 200, 0))
        );
        gc.setFill(sunGradient);
        gc.fillOval(sunX - sunRadius, sunY - sunRadius, sunRadius * 2, sunRadius * 2);

        gc.setFill(Color.rgb(255, 255, 200, 0.8));
        gc.fillOval(sunX - sunRadius * 0.3, sunY - sunRadius * 0.3, sunRadius * 0.6, sunRadius * 0.6);
    }

    private void drawSunRays(GraphicsContext gc, double width, double height) {
        double centerX = width * 0.85;
        double centerY = height * 0.08;

        for (SunRay ray : sunRays) {
            gc.save();
            gc.setGlobalAlpha(ray.opacity);
            gc.translate(centerX, centerY);
            gc.rotate(ray.angle);

            LinearGradient rayGradient = new LinearGradient(
                0, 0, ray.width, 0,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 255, 200, 0.5)),
                new Stop(1, Color.rgb(255, 255, 200, 0.0))
            );

            gc.setFill(rayGradient);
            gc.fillRect(0, -5, height * 1.5, 10);
            gc.restore();
        }
    }

    private void drawCloud(GraphicsContext gc, Cloud cloud) {
        double cloudOpacity = Math.max(0.7, cloud.opacity);
        gc.setGlobalAlpha(cloudOpacity);
        gc.setFill(Color.rgb(255, 255, 255, 0.95));

        double size = cloud.size;
        gc.fillOval(cloud.x, cloud.y, size * 0.6, size * 0.6);
        gc.fillOval(cloud.x + size * 0.3, cloud.y, size * 0.7, size * 0.7);
        gc.fillOval(cloud.x + size * 0.6, cloud.y, size * 0.6, size * 0.6);
        gc.fillOval(cloud.x + size * 0.2, cloud.y + size * 0.2, size * 0.5, size * 0.5);
        gc.fillOval(cloud.x + size * 0.5, cloud.y + size * 0.2, size * 0.5, size * 0.5);

        gc.setGlobalAlpha(1.0);
    }

    private static class Cloud {
        double x, y, size, opacity;
    }

    private static class SunRay {
        double angle, opacity, width;
    }

    /**
     * Updates background by weather and redraws once.
     */
    public void setWeather(boolean sunny) {
        this.isSunny = sunny;
        drawBackground();
    }

    /**
     * No-op: no animation timeline remains.
     */
    public void stop() {
        // Intentionally empty.
    }
}
