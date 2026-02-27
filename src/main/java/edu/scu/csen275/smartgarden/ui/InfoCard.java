package edu.scu.csen275.smartgarden.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Styled card component for information display.
 */
public class InfoCard extends VBox {
    private final Label titleLabel;
    
    public InfoCard(String title) {
        this.setSpacing(18);
        this.setPadding(new Insets(22));
        this.getStyleClass().add("card");
        this.setAlignment(Pos.TOP_LEFT);
        
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        this.getChildren().add(titleLabel);
    }
    
    /**
     * Adds a labeled progress bar to the card.
     */
    public ProgressBar addProgressBar(String label, double initialValue) {
        Label barLabel = new Label(label);
        barLabel.getStyleClass().add("info-label");
        
        ProgressBar progressBar = new ProgressBar(initialValue);
        progressBar.getStyleClass().add("modern-progress-bar");
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(28);
        
        this.getChildren().addAll(barLabel, progressBar);
        return progressBar;
    }
    
    /**
     * Adds a text label to the card.
     */
    public Label addLabel(String text, boolean bold) {
        Label label = new Label(text);
        if (bold) {
            label.getStyleClass().add("info-label-bold");
        } else {
            label.getStyleClass().add("info-label");
        }
        this.getChildren().add(label);
        return label;
    }
    
    /**
     * Updates progress bar color based on value.
     */
    public static void updateProgressBarStyle(ProgressBar bar, double value) {
        bar.getStyleClass().removeAll("low", "critical");
        
        if (value < 0.3) {
            bar.getStyleClass().add("critical");
        } else if (value < 0.5) {
            bar.getStyleClass().add("low");
        }
    }
}

