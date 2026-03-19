package CSEN275Garden.system;

import CSEN275Garden.model.Plant;
import CSEN275Garden.model.GridPosition;

/**
 * Common pest model used by the simulation.
 * Tracks pest identity (type), damage intensity, location, and alive state.
 */
public abstract class Pest {
    protected final String pestType;
    protected final int damageRate;
    protected GridPosition position;
    protected boolean isAlive;
    
    /**
     * Initializes a pest instance with its type, damage rate, and starting position.
     */
    protected Pest(String pestType, int damageRate, GridPosition position) {
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
    
    public GridPosition getPosition() {
        return position;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public void setPosition(GridPosition position) {
        this.position = position;
    }
    
    @Override
    public String toString() {
        return pestType + " at " + position + " [" + (isAlive ? "Alive" : "Dead") + "]";
    }
}

