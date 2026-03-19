package CSEN275Garden.ui;

import CSEN275Garden.model.GridPosition;
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
        void onPestSpawned(GridPosition position, String pestType, boolean isHarmful);
        void onPesticideApplied(GridPosition position);
        void onPestRemoved(GridPosition position, String pestType);
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
    public void notifyPestSpawned(GridPosition position, String pestType, boolean isHarmful) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestSpawned(position, pestType, isHarmful));
        }
    }
    
    /**
     * Dispatches a pesticide application event.
     */
    public void notifyPesticideApplied(GridPosition position) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPesticideApplied(position));
        }
    }
    
    /**
     * Dispatches a pest removal event.
     */
    public void notifyPestRemoved(GridPosition position, String pestType) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestRemoved(position, pestType));
        }
    }
}
