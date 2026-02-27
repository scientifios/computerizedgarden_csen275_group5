package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.model.Position;
import javafx.application.Platform;

/**
 * Bridge class to connect PestControlSystem events to UI animations.
 * Acts as an observer/listener interface for pest-related events.
 */
public class PestEventBridge {
    private PestAnimationHandler handler;
    
    /**
     * Interface for handling pest animation events.
     */
    public interface PestAnimationHandler {
        void onPestSpawned(Position position, String pestType, boolean isHarmful);
        void onPestAttack(Position position, int damage);
        void onPesticideApplied(Position position);
        void onPestRemoved(Position position, String pestType);
    }
    
    /**
     * Sets the animation handler.
     */
    public void setHandler(PestAnimationHandler handler) {
        this.handler = handler;
    }
    
    /**
     * Notifies that a pest has spawned.
     */
    public void notifyPestSpawned(Position position, String pestType, boolean isHarmful) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestSpawned(position, pestType, isHarmful));
        }
    }
    
    /**
     * Notifies that a pest has attacked.
     */
    public void notifyPestAttack(Position position, int damage) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestAttack(position, damage));
        }
    }
    
    /**
     * Notifies that pesticide has been applied.
     */
    public void notifyPesticideApplied(Position position) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPesticideApplied(position));
        }
    }
    
    /**
     * Notifies that a pest has been removed.
     */
    public void notifyPestRemoved(Position position, String pestType) {
        if (handler != null) {
            Platform.runLater(() -> handler.onPestRemoved(position, pestType));
        }
    }
}
