package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.Position;

/**
 * Represents a harmful pest that damages plants.
 */
public class HarmfulPest extends Pest {
    
    /**
     * Creates a new HarmfulPest.
     */
    public HarmfulPest(String type, Position position) {
        super(type, calculateDamageRate(type), position);
    }
    
    /**
     * Creates a default harmful pest (Red Mite).
     */
    public HarmfulPest(Position position) {
        this("Red Mite", position);
    }
    
    @Override
    public void causeDamage(Plant plant) {
        if (isAlive && !plant.isDead()) {
            plant.pestAttack();
        }
    }
    
    @Override
    public boolean isBeneficial() {
        return false;
    }
    
    /**
     * Calculates damage rate based on pest type (Set C).
     */
    private static int calculateDamageRate(String type) {
        return switch (type.toLowerCase()) {
            case "red mite" -> 2;
            case "green leaf worm" -> 3;
            case "black beetle" -> 4;
            case "brown caterpillar" -> 2;
            default -> 2;
        };
    }
}

