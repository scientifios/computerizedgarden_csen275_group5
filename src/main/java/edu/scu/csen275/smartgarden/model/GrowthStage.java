package edu.scu.csen275.smartgarden.model;

/**
 * Represents the growth stages of a plant.
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
     * Gets the next growth stage, or returns current if at max.
     */
    public GrowthStage next() {
        GrowthStage[] stages = values();
        int nextIndex = this.ordinal() + 1;
        return nextIndex < stages.length ? stages[nextIndex] : this;
    }
    
    /**
     * Checks if this is the final stage.
     */
    public boolean isFinalStage() {
        return this == FRUITING;
    }
}

