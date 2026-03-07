package edu.scu.csen275.smartgarden.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * Container used to group related UI elements with a title.
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
     * Updates the text color of the title label.
     */
    public void setTitleColor(String colorHex) {
        titleLabel.setStyle("-fx-text-fill: " + colorHex + ";");
    }
    
    /**
     * Creates a labeled progress bar and attaches it to the card layout.
     */
    public ProgressBar addProgressBar(String label, double initialValue) {
        Label barLabel = new Label(label);
        barLabel.getStyleClass().add("info-label");
        barLabel.getStyleClass().add("resource-label");
        
        ProgressBar progressBar = new ProgressBar(initialValue);
        progressBar.getStyleClass().add("modern-progress-bar");
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(28);
        
        this.getChildren().addAll(barLabel, progressBar);
        return progressBar;
    }
    
    /**
     * Creates a text label and adds it to the card content.
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
     * Adjusts the style classes of the progress bar according to its value.
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
