package edu.scu.csen275.smartgarden.model;

/**
 * Represents a flowering plant in the garden.
 * Flowers have moderate growth rates and low water requirements.
 */
public class Flower extends Plant {
    private static final int DEFAULT_LIFESPAN = 90; // days
    private static final int WATER_REQ = 30;
    private static final int SUNLIGHT_REQ = 70;
    private static final int MIN_TEMP = 10;
    private static final int MAX_TEMP = 30;
    private static final int PEST_RESISTANCE = 3;
    private static final int GROWTH_DURATION = 7; // days per stage
    
    private final String bloomColor;
    
    public Flower(Position position, String bloomColor) {
        super("Flower", position, DEFAULT_LIFESPAN, WATER_REQ, SUNLIGHT_REQ,
              MIN_TEMP, MAX_TEMP, PEST_RESISTANCE);
        this.bloomColor = bloomColor;
    }
    
    public Flower(Position position) {
        this(position, "Pink");
    }
    
    @Override
    public int getGrowthDuration() {
        return GROWTH_DURATION;
    }
    
    public String getBloomColor() {
        return bloomColor;
    }
    
    @Override
    public String getPlantType() {
        return "Flower (" + bloomColor + ")";
    }
}

