package edu.scu.csen275.smartgarden.model;

/**
 * Enumerates the lifecycle stages of a plant within the garden simulation.
 *
 * Each stage is associated with a display label and an ordinal stage number
 * used for progression tracking.
 */
public enum GrowthStage {
    SEED("Seed", 0),
    SEEDLING("Seedling", 1),
    MATURE("Mature", 2),
    FLOWERING("Flowering", 3),
    FRUITING("Fruiting", 4);
    
    private final String displayName;
    private final int stageNumber;
    
    GrowthStage(String displayName, int stageNumber) {
        this.displayName = displayName;
        this.stageNumber = stageNumber;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getStageNumber() {
        return stageNumber;
    }
    
    /**
    * Returns the next lifecycle stage.
    * If this stage is already the final stage, the current stage is returned.
    */
    public GrowthStage next() {
        GrowthStage[] stages = values();
        int nextIndex = this.ordinal() + 1;
        return nextIndex < stages.length ? stages[nextIndex] : this;
    }
    
    /**
    * Indicates whether this stage represents terminal plant development.
    */
    public boolean isFinalStage() {
        return this == FRUITING;
    }
}

