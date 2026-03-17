package edu.scu.csen275.smartgarden.model;

import javafx.beans.property.*;
import edu.scu.csen275.smartgarden.util.Logger;

/**
 * Abstract base class for all plants in the garden simulation.
 *
 * A Plant maintains observable state (JavaFX properties) for UI binding and
 * implements common lifecycle logic such as water consumption, health updates,
 * growth-stage progression, and death conditions.
 *
 * Subclasses specialize behavior by providing a growth duration via
 * {@link #getGrowthDuration()} and configuring environmental parameters
 * (e.g., water requirement, temperature range, pest resistance).
 */
public abstract class Plant {

    private final ObjectProperty<Position> position;
    private final ObjectProperty<GrowthStage> growthStage;
    private final IntegerProperty healthLevel;
    private final IntegerProperty waterLevel;
    private final IntegerProperty daysAlive;
    private final BooleanProperty isDead;
    
    private final int maxLifespan;
    private final int waterRequirement;
    private final int sunlightRequirement;
    private final int minTemperature;
    private final int maxTemperature;
    private final int pestResistance;

    private int daysSinceGrowth;
    private int pestAttacks; // Current attack count (can be reduced by treatment)
    private int totalPestAttacks; // Lifetime total (never decreases)
    private final String plantType;
    
    protected static final Logger logger = Logger.getInstance();
    
    /**
     * Creates a new Plant with specified parameters.
     */
    protected Plant(String plantType, Position position, int maxLifespan, 
                   int waterRequirement, int sunlightRequirement,
                   int minTemperature, int maxTemperature, int pestResistance) {
        this.plantType = plantType;
        this.position = new SimpleObjectProperty<>(position);
        this.growthStage = new SimpleObjectProperty<>(GrowthStage.SEED);
        this.healthLevel = new SimpleIntegerProperty(100);
        this.waterLevel = new SimpleIntegerProperty(waterRequirement);
        this.daysAlive = new SimpleIntegerProperty(0);
        this.isDead = new SimpleBooleanProperty(false);
        
        this.maxLifespan = maxLifespan;
        this.waterRequirement = waterRequirement;
        this.sunlightRequirement = sunlightRequirement;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.pestResistance = pestResistance;
        
        this.daysSinceGrowth = 0;
        this.pestAttacks = 0;
        this.totalPestAttacks = 0;
    }
    
    private int waterConsumptionTicks = 0;
    private static final int TICKS_PER_WATER_CONSUMPTION = 5; // 1 water unit per 5 ticks (minutes)
    
    /**
    * Advances the plant by one simulation tick (1 minute).
    * Handles water consumption, health recalculation, and death checks.
    */
    public void update() {
        if (isDead.get()) {
            return;
        }
        
        // Decrease water level over time (1 per 5 ticks for moderate consumption)
        waterConsumptionTicks++;
        if (waterConsumptionTicks >= TICKS_PER_WATER_CONSUMPTION) {
            waterConsumptionTicks = 0;
            if (waterLevel.get() > 0) {
                waterLevel.set(waterLevel.get() - 1);
            }
        }
        
        // Update health based on conditions
        updateHealth();
        
        // Check for death
        if (healthLevel.get() <= 0 || daysAlive.get() >= maxLifespan) {
            die();
        }
    }
    
    public void advanceDay() {
        if (isDead.get()) {
            return;
        }
        
        daysAlive.set(daysAlive.get() + 1);
        daysSinceGrowth++;
        
        // Check for growth stage advancement
        if (daysSinceGrowth >= getGrowthDuration() && !growthStage.get().isFinalStage()) {
            GrowthStage nextStage = growthStage.get().next();
            growthStage.set(nextStage);
            daysSinceGrowth = 0;
            logger.info("Plant", plantType + " at " + position.get() + 
                       " advanced to " + nextStage.getDisplayName());
        }
    }
    
    public void water(int amount) {
        if (isDead.get()) {
            return;
        }
        
        waterLevel.set(Math.min(100, waterLevel.get() + amount));
        
        // Bonus health if water needs are met
        if (waterLevel.get() >= waterRequirement) {
            heal(2);
        }
    }
    
    public void takeDamage(int amount) {
        if (isDead.get()) {
            return;
        }
        
        healthLevel.set(Math.max(0, healthLevel.get() - amount));
        
        if (healthLevel.get() == 0) {
            die();
        }
    }
    
    public void heal(int amount) {
        if (isDead.get()) {
            return;
        }
        
        healthLevel.set(Math.min(100, healthLevel.get() + amount));
    }
    
    /**
     * Records a pest attack on this plant.
     * Increments both the current attack counter and the lifetime total.
     */
    public void pestAttack() {
        if (isDead.get()) {
            return;
        }
        
        pestAttacks++;
        totalPestAttacks++; // Always increment total (never decreases)
        int damage = Math.max(1, 4 - pestResistance);
        takeDamage(damage);
        
        if (pestAttacks % 5 == 0) {
            logger.warning("Plant", plantType + " at " + position.get() + 
                          " has suffered " + pestAttacks + " pest attacks (Total: " + totalPestAttacks + ")");
        }
    }
    
    /**
     * Reduces pest attack count (after treatment).
     * The lifetime total is not affected.
     */
    public void reducePestAttacks(int amount) {
        pestAttacks = Math.max(0, pestAttacks - amount);
        heal(amount * 2); // Healing from treatment
    }
    
    public void applyTemperatureEffect(int currentTemp) {
        if (isDead.get()) {
            return;
        }
        
        if (currentTemp < minTemperature) {
            takeDamage(2);
        } else if (currentTemp > maxTemperature) {
            takeDamage(1);
        } else {
            heal(1); // Optimal temperature
        }
    }
    
    public void applyWeatherEffect(String weather) {
        if (isDead.get()) {
            return;
        }
        
        switch (weather) {
            case "SUNNY" -> heal(2);
            case "RAINY" -> {
                heal(1);
                water(3); // Rain adds water every tick to keep plants hydrated
            }
            case "CLOUDY" -> heal(1);
            case "WINDY" -> takeDamage(1);
            case "SNOWY" -> takeDamage(3);
        }
    }
    
    private void die() {
        if (!isDead.get()) {
            isDead.set(true);
            healthLevel.set(0);
            logger.warning("Plant", plantType + " at " + position.get() + " has died. " +
                          "Days alive: " + daysAlive.get() + ", Total pest attacks: " + totalPestAttacks);
        }
    }
    
    private void updateHealth() {
        // Water stress
        if (waterLevel.get() < waterRequirement / 2) {
            takeDamage(1);
        }
        
        // Severe dehydration
        if (waterLevel.get() == 0) {
            takeDamage(2);
        }
    }
    
    /**
     * Gets the number of days required for each growth stage.
     * Subclasses must implement this to define growth rate.
     */
    public abstract int getGrowthDuration();
    
    public String getPlantType() {
        return plantType;
    }
    
    public double getHealthPercentage() {
        return healthLevel.get();
    }
    
    public String getHealthStatus() {
        int health = healthLevel.get();
        if (health >= 80) return "Healthy";
        if (health >= 50) return "Fair";
        if (health >= 20) return "Poor";
        return "Critical";
    }

    public String getHealthColor() {
        int health = healthLevel.get();
        if (health >= 70) return "GREEN";
        if (health >= 40) return "YELLOW";
        if (health >= 20) return "ORANGE";
        return "RED";
    }
    
    public ObjectProperty<Position> positionProperty() { return position; }
    public ObjectProperty<GrowthStage> growthStageProperty() { return growthStage; }
    public IntegerProperty healthLevelProperty() { return healthLevel; }
    public IntegerProperty waterLevelProperty() { return waterLevel; }
    public IntegerProperty daysAliveProperty() { return daysAlive; }
    public BooleanProperty isDeadProperty() { return isDead; }
    
    public Position getPosition() { return position.get(); }
    public GrowthStage getGrowthStage() { return growthStage.get(); }
    public int getHealthLevel() { return healthLevel.get(); }
    public int getWaterLevel() { return waterLevel.get(); }
    public int getDaysAlive() { return daysAlive.get(); }
    public boolean isDead() { return isDead.get(); }
    public int getWaterRequirement() { return waterRequirement; }
    public int getSunlightRequirement() { return sunlightRequirement; }
    public int getPestAttacks() { return pestAttacks; }
    public int getTotalPestAttacks() { return totalPestAttacks; }
    public int getMaxLifespan() { return maxLifespan; }
    public int getMinTemperature() { return minTemperature; }
    public int getMaxTemperature() { return maxTemperature; }
    
    @Override
    public String toString() {
        return plantType + " at " + position.get() + 
               " [" + growthStage.get().getDisplayName() + ", Health: " + healthLevel.get() + "%]";
    }
}

