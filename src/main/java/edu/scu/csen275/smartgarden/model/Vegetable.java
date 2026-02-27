package edu.scu.csen275.smartgarden.model;

/**
 * Represents a vegetable crop in the garden.
 * Vegetables grow quickly but require high water and care.
 */
public class Vegetable extends Plant {
    private static final int DEFAULT_LIFESPAN = 90; // days
    private static final int WATER_REQ = 60;
    private static final int SUNLIGHT_REQ = 80;
    private static final int MIN_TEMP = 15;
    private static final int MAX_TEMP = 28;
    private static final int PEST_RESISTANCE = 2;
    private static final int GROWTH_DURATION = 5; // days per stage
    
    private final String vegetableType;
    
    public Vegetable(Position position, String vegetableType) {
        super("Vegetable", position, DEFAULT_LIFESPAN, WATER_REQ, SUNLIGHT_REQ,
              MIN_TEMP, MAX_TEMP, PEST_RESISTANCE);
        this.vegetableType = vegetableType;
    }
    
    public Vegetable(Position position) {
        this(position, "Tomato");
    }
    
    @Override
    public int getGrowthDuration() {
        return GROWTH_DURATION;
    }
    
    public String getVegetableType() {
        return vegetableType;
    }
    
    @Override
    public String getPlantType() {
        return vegetableType;
    }
}

