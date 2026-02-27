package edu.scu.csen275.smartgarden.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Modern toolbar with icons and animations.
 */
public class ModernToolbar extends HBox {
    private final Label statusLabel;
    private final Button startBtn;
    private final Button pauseBtn;
    private final Button stopBtn;
    private final ComboBox<String> speedBox;
    
    public ModernToolbar() {
        this.setSpacing(15);
        this.setPadding(new Insets(15));
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().add("toolbar");
        
        // Title with icon (simplified - no duplicate text)
        Label title = new Label("🌿 Smart Garden");
        title.getStyleClass().add("title-label");
        
        // Separator
        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep1.setPrefHeight(30);
        
        // Control buttons
        startBtn = createIconButton("▶️ Start", "button-start");
        pauseBtn = createIconButton("⏸ Pause", "button-pause");
        stopBtn = createIconButton("⏹ Stop", "button-stop");
        
        pauseBtn.setDisable(true);
        
        // Speed selector
        Label speedLabel = new Label("Speed:");
        speedLabel.setTextFill(Color.WHITE);
        speedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        speedBox = new ComboBox<>();
        speedBox.getItems().addAll("1x", "2x", "5x", "10x");
        speedBox.setValue("1x");
        speedBox.getStyleClass().add("modern-combo");
        
        // Status label
        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep2.setPrefHeight(30);
        
        statusLabel = new Label("Status: STOPPED");
        statusLabel.getStyleClass().addAll("status-label", "status-stopped");
        
        this.getChildren().addAll(
            title, sep1,
            startBtn, pauseBtn, stopBtn,
            speedLabel, speedBox,
            sep2, statusLabel
        );
    }
    
    /**
     * Creates a button with icon and modern styling.
     */
    private Button createIconButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("modern-button", styleClass);
        btn.setPrefWidth(100);
        return btn;
    }
    
    /**
     * Updates status label with glow effect for running state.
     */
    public void updateStatus(String status) {
        statusLabel.setText("Status: " + status);
        
        // Remove old status classes
        statusLabel.getStyleClass().removeAll("status-running", "status-stopped", "status-paused");
        
        // Add appropriate status class
        if (status.equals("RUNNING")) {
            statusLabel.getStyleClass().add("status-running");
        } else if (status.equals("PAUSED")) {
            statusLabel.getStyleClass().add("status-paused");
        } else {
            statusLabel.getStyleClass().add("status-stopped");
        }
    }
    
    // Getters for buttons and combo box
    public Button getStartButton() { return startBtn; }
    public Button getPauseButton() { return pauseBtn; }
    public Button getStopButton() { return stopBtn; }
    public ComboBox<String> getSpeedBox() { return speedBox; }
    public Label getStatusLabel() { return statusLabel; }
}

