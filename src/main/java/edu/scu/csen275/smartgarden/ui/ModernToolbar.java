package edu.scu.csen275.smartgarden.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Toolbar for simulation controls and status display.
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
        
        Label title = new Label("Garden Sim");
        title.getStyleClass().add("title-label");
        
        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep1.setPrefHeight(30);
        
        startBtn = createIconButton("Start", "button-start");
        pauseBtn = createIconButton("Pause", "button-pause");
        stopBtn = createIconButton("Stop", "button-stop");
        
        pauseBtn.setDisable(true);
        
        Label speedLabel = new Label("Speed:");
        speedLabel.getStyleClass().add("toolbar-label");
        
        speedBox = new ComboBox<>();
        speedBox.getItems().addAll("1x", "2x", "5x", "10x");
        speedBox.setValue("1x");
        speedBox.getStyleClass().add("modern-combo");
        
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
     * Creates a toolbar button with the given text and style class.
     */
    private Button createIconButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("modern-button", styleClass);
        btn.setPrefWidth(100);
        return btn;
    }
    
    /**
     * Updates the status text and applies the matching status style class.
     */
    public void updateStatus(String status) {
        statusLabel.setText("Status: " + status);
        
        statusLabel.getStyleClass().removeAll("status-running", "status-stopped", "status-paused");
        
        if (status.equals("RUNNING")) {
            statusLabel.getStyleClass().add("status-running");
        } else if (status.equals("PAUSED")) {
            statusLabel.getStyleClass().add("status-paused");
        } else {
            statusLabel.getStyleClass().add("status-stopped");
        }
    }
    
    public Button getStartButton() { return startBtn; }
    public Button getPauseButton() { return pauseBtn; }
    public Button getStopButton() { return stopBtn; }
    public ComboBox<String> getSpeedBox() { return speedBox; }
    public Label getStatusLabel() { return statusLabel; }
}

