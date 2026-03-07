package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.model.Position;
import javafx.application.Platform;

/**
 * Dispatches pest-related events to a UI animation handler.
 * Ensures callbacks are executed on the JavaFX UI thread.
 */
public class PestEventBridge {
    private PestAnimationHandler handler;
    
    /**
     * Callback interface used by the UI layer to react to pest events.
     */
    public interface PestAnimationHandler {
        void onPestSpawned(Position position, String pestType, boolean isHarmful);
        void onPestAttack(Position position, int damage);
        void onPesticideApplied(Position position);
        void onPestRemoved(Position position, String pestType);
    }
    
    /**
     * Registers the UI handler that will receive pest event callbacks.
     */
    public void setHandler(PestAnimationHandler handler) {
        this.handler = handler;
    }
    
    /**
     * Dispatches a pest spawn event to the UI handler.
     */
    public void notifyPestSpawned(Position position, String pestType, boolean isHarmful) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestSpawned(position, pestType, isHarmful));
        }
    }
    
    /**
     * Dispatches a pest attack event.
     */
    public void notifyPestAttack(Position position, int damage) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestAttack(position, damage));
        }
    }
    
    /**
     * Dispatches a pesticide application event.
     */
    public void notifyPesticideApplied(Position position) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPesticideApplied(position));
        }
    }
    
    /**
     * Dispatches a pest removal event.
     */
    public void notifyPestRemoved(Position position, String pestType) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestRemoved(position, pestType));
        }
    }
}
