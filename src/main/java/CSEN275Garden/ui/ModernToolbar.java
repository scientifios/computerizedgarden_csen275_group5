package CSEN275Garden.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Toolbar for simulation controls and status display.
 */
public class ModernToolbar extends HBox {
    private final Label statusLabel;
    private final Button startButton;
    private final Button pauseButton;
    private final Button stopButton;
    private final ComboBox<String> speedComboBox;
    
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
        
        startButton = createIconButton("Start", "button-start");
        pauseButton = createIconButton("Pause", "button-pause");
        stopButton = createIconButton("Stop", "button-stop");
        
        pauseButton.setDisable(true);
        
        Label speedLabel = new Label("Speed:");
        speedLabel.getStyleClass().add("toolbar-label");
        
        speedComboBox = new ComboBox<>();
        speedComboBox.getItems().addAll("1x", "2x", "5x", "10x");
        speedComboBox.setValue("1x");
        speedComboBox.getStyleClass().add("modern-combo");
        
        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep2.setPrefHeight(30);
        
        statusLabel = new Label("Status: STOPPED");
        statusLabel.getStyleClass().addAll("status-label", "status-stopped");
        
        this.getChildren().addAll(
            title, sep1,
            startButton, pauseButton, stopButton,
            speedLabel, speedComboBox,
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
    
    public Button getStartButton() { return startButton; }
    public Button getPauseButton() { return pauseButton; }
    public Button getStopButton() { return stopButton; }
    public ComboBox<String> getSpeedComboBox() { return speedComboBox; }
    public Label getStatusLabel() { return statusLabel; }
}

