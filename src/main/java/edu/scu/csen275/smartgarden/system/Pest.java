package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Position;

/**
 * Common pest model used by the simulation.
 * Tracks pest identity (type), damage intensity, location, and alive state.
 */
public abstract class Pest {
    protected final String pestType;
    protected final int damageRate;
    protected Position position;
    protected boolean isAlive;
    
    /**
     * Initializes a pest instance with its type, damage rate, and starting position.
     */
    protected Pest(String pestType, int damageRate, Position position) {
        this.pestType = pestType;
        this.damageRate = damageRate;
        this.position = position;
        this.isAlive = true;
    }
    
    /**
     * Applies this pest's attack behavior to the given plant.
     */
    public abstract void causeDamage(Plant plant);
    
    /**
     * @return true if this pest is beneficial to the garden; false if it is harmful.
     */
    public abstract boolean isBeneficial();
    
    /**
     * Marks this pest as no longer alive (e.g., removed by a treatment action).
     */
    public void eliminate() {
        isAlive = false;
    }
    
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

