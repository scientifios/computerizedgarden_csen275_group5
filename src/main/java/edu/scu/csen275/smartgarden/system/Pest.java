package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Position;

/**
 * Abstract base class for pests and insects in the garden.
 */
public abstract class Pest {
    protected final String pestType;
    protected final int damageRate;
    protected Position position;
    protected boolean isAlive;
    
    /**
     * Creates a new Pest.
     */
    protected Pest(String pestType, int damageRate, Position position) {
        this.pestType = pestType;
        this.damageRate = damageRate;
        this.position = position;
        this.isAlive = true;
    }
    
    /**
     * Causes damage to a plant.
     */
    public abstract void causeDamage(Plant plant);
    
    /**
     * Checks if this pest is beneficial or harmful.
     */
    public abstract boolean isBeneficial();
    
    /**
     * Removes the pest (killed by treatment).
     */
    public void eliminate() {
        isAlive = false;
    }
    
    // Getters
    public String getPestType() {
        return pestType;
    }
    
    public int getDamageRate() {
        return damageRate;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public void setPosition(Position position) {
        this.position = position;
    }
    
    @Override
    public String toString() {
        return pestType + " at " + position + " [" + (isAlive ? "Alive" : "Dead") + "]";
    }
}

