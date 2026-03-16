package edu.scu.csen275.smartgarden.model;

/**
 * Concrete implementation of {@link Plant} representing a flowering plant.
 *
 * A Flower has moderate growth rate, low water requirements,
 * and a defined temperature tolerance range. Each flower
 * is characterized by its bloom color and a fixed growth duration.
 *
 * This class encapsulates flower-specific properties while
 * leveraging the common lifecycle behavior defined in Plant.
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

