package CSEN275Garden.system;

import CSEN275Garden.model.Plant;
import CSEN275Garden.model.GridPosition;

/**
 * A non-beneficial pest that attacks plants and applies damage based on its type.
 * Damage rate is derived from a predefined mapping in calculateDamageRate().
 */
public class HarmfulPest extends Pest {
    
    /**
     * Creates a harmful pest of the given type at the given position.
     * The damage rate is derived from the pest type via calculateDamageRate().
     */
    public HarmfulPest(String type, GridPosition position) {
        super(type, calculateDamageRate(type), position);
    }
    
    /**
     * Creates a harmful pest with the default type "Red Mite" at the given position.
     */
    public HarmfulPest(GridPosition position) {
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
     * Returns the damage rate for the given pest type using a predefined mapping (case-insensitive).
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

