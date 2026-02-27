package edu.scu.csen275.smartgarden.model;

/**
 * Represents a fruit plant in the garden.
 * Fruits have moderate growth rates and moderate water requirements.
 */
public class Fruit extends Plant {
    private static final int DEFAULT_LIFESPAN = 90; // days
    private static final int WATER_REQ = 50;
    private static final int SUNLIGHT_REQ = 75;
    private static final int MIN_TEMP = 12;
    private static final int MAX_TEMP = 30;
    private static final int PEST_RESISTANCE = 4;
    private static final int GROWTH_DURATION = 8; // days per stage
    
    private final String fruitType;
    
    public Fruit(Position position, String fruitType) {
        super("Fruit", position, DEFAULT_LIFESPAN, WATER_REQ, SUNLIGHT_REQ,
              MIN_TEMP, MAX_TEMP, PEST_RESISTANCE);
        this.fruitType = fruitType;
    }
    
    @Override
    public int getGrowthDuration() {
        return GROWTH_DURATION;
    }
    
    public String getFruitType() {
        return fruitType;
    }
    
    @Override
    public String getPlantType() {
        return fruitType;
    }
}

