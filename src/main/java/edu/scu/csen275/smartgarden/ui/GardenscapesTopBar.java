package edu.scu.csen275.smartgarden.ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Gardenscapes-inspired top bar with coins, stars, boosters, and progress.
 * Bright, cheerful UI with playful fonts and animations.
 */
public class GardenscapesTopBar extends HBox {
    private Label coinsLabel;
    private Label starsLabel;
    private Label boostersLabel;
    private Label progressLabel;
    
    private int coins = 0;
    private int stars = 0;
    private int boosters = 3;
    private int progress = 0;
    
    public GardenscapesTopBar() {
        this.setSpacing(20);
        this.setPadding(new Insets(15, 25, 15, 25));
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle(
            "-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 100%); " +
            "-fx-background-radius: 0 0 20 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);"
        );
        
        // Title/Logo
        Label title = new Label("🌿 Smart Garden");
        title.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 24));
        title.setTextFill(Color.rgb(46, 125, 50));
        title.setEffect(new DropShadow(3, Color.rgb(0, 0, 0, 0.2)));
        
        // Separator
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(40);
        sep.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
        
        // Coins display
        HBox coinsBox = createResourceBox("💰", "Coins", coins);
        coinsLabel = (Label) coinsBox.getChildren().get(1);
        
        // Stars display
        HBox starsBox = createResourceBox("⭐", "Stars", stars);
        starsLabel = (Label) starsBox.getChildren().get(1);
        
        // Boosters display
        HBox boostersBox = createResourceBox("🚀", "Boosters", boosters);
        boostersLabel = (Label) boostersBox.getChildren().get(1);
        
        // Progress bar
        progressLabel = new Label("📊 Garden Progress: 0%");
        progressLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
        progressLabel.setTextFill(Color.rgb(156, 39, 176));
        progressLabel.setPadding(new Insets(5, 15, 5, 15));
        progressLabel.setStyle(
            "-fx-background-color: linear-gradient(to right, #E1BEE7 0%, #CE93D8 100%); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #9C27B0; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 15;"
        );
        
        // Add everything
        this.getChildren().addAll(
            title, sep,
            coinsBox, starsBox, boostersBox,
            progressLabel
        );
    }
    
    /**
     * Creates a resource display box with icon and value.
     */
    private HBox createResourceBox(String icon, String label, int value) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 15, 5, 15));
        box.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #FFF9C4 0%, #FFF59D 100%); " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: #FBC02D; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        
        Label valueLabel = new Label(label + ": " + value);
        valueLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.rgb(139, 69, 19));
        
        // Add glow effect on hover
        box.setOnMouseEntered(e -> {
            box.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FFFDE7 0%, #FFF9C4 100%); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #FFC107; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,193,7,0.5), 10, 0, 0, 3);"
            );
            iconLabel.setEffect(new Glow(0.5));
        });
        
        box.setOnMouseExited(e -> {
            box.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #FFF9C4 0%, #FFF59D 100%); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: #FBC02D; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
            iconLabel.setEffect(null);
        });
        
        box.getChildren().addAll(iconLabel, valueLabel);
        return box;
    }
    
    /**
     * Updates coins with bounce animation.
     */
    public void updateCoins(int newCoins) {
        if (newCoins != coins) {
            coins = newCoins;
            coinsLabel.setText("Coins: " + coins);
            animateUpdate(coinsLabel);
        }
    }
    
    /**
     * Updates stars with bounce animation.
     */
    public void updateStars(int newStars) {
        if (newStars != stars) {
            stars = newStars;
            starsLabel.setText("Stars: " + stars);
            animateUpdate(starsLabel);
        }
    }
    
    /**
     * Updates boosters with bounce animation.
     */
    public void updateBoosters(int newBoosters) {
        if (newBoosters != boosters) {
            boosters = newBoosters;
            boostersLabel.setText("Boosters: " + boosters);
            animateUpdate(boostersLabel);
        }
    }
    
    /**
     * Updates progress percentage.
     */
    public void updateProgress(int percentage) {
        if (percentage != progress) {
            progress = percentage;
            progressLabel.setText("📊 Garden Progress: " + percentage + "%");
            
            // Color code progress
            if (percentage < 25) {
                progressLabel.setStyle(
                    "-fx-background-color: linear-gradient(to right, #FFCDD2 0%, #EF9A9A 100%); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: #E57373; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 15;"
                );
            } else if (percentage < 50) {
                progressLabel.setStyle(
                    "-fx-background-color: linear-gradient(to right, #FFF9C4 0%, #FFF59D 100%); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: #FBC02D; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 15;"
                );
            } else if (percentage < 75) {
                progressLabel.setStyle(
                    "-fx-background-color: linear-gradient(to right, #C5E1A5 0%, #AED581 100%); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: #9CCC65; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 15;"
                );
            } else {
                progressLabel.setStyle(
                    "-fx-background-color: linear-gradient(to right, #A5D6A7 0%, #81C784 100%); " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: #66BB6A; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 15;"
                );
            }
            
            animateUpdate(progressLabel);
        }
    }
    
    /**
     * Bounce animation for value updates.
     */
    private void animateUpdate(Label label) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.setInterpolator(Interpolator.EASE_OUT);
        scale.play();
    }
    
    // Getters
    public int getCoins() { return coins; }
    public int getStars() { return stars; }
    public int getBoosters() { return boosters; }
    public int getProgress() { return progress; }
}

