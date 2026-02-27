package edu.scu.csen275.smartgarden.model;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a zone in the garden.
 * A zone is a logical grouping of grid cells for management purposes.
 */
public class Zone {
    private final int zoneId;
    private final List<Position> boundaries;
    private final List<Plant> plantsInZone;
    
    private final IntegerProperty moistureLevel;
    private final IntegerProperty temperature;
    private final IntegerProperty pestInfestationLevel;
    
    /**
     * Creates a new Zone.
     */
    public Zone(int zoneId, List<Position> boundaries) {
        this.zoneId = zoneId;
        this.boundaries = new ArrayList<>(boundaries);
        this.plantsInZone = new ArrayList<>();
        this.moistureLevel = new SimpleIntegerProperty(50);
        this.temperature = new SimpleIntegerProperty(20);
        this.pestInfestationLevel = new SimpleIntegerProperty(0);
    }
    
    /**
     * Checks if a position is within this zone.
     */
    public boolean containsPosition(Position position) {
        return boundaries.contains(position);
    }
    
    /**
     * Adds a plant to this zone.
     */
    public void addPlant(Plant plant) {
        if (!plantsInZone.contains(plant)) {
            plantsInZone.add(plant);
        }
    }
    
    /**
     * Removes a plant from this zone.
     */
    public void removePlant(Plant plant) {
        plantsInZone.remove(plant);
    }
    
    /**
     * Gets plants that need water.
     */
    public List<Plant> getPlantsNeedingWater() {
        return plantsInZone.stream()
            .filter(p -> !p.isDead())
            .filter(p -> p.getWaterLevel() < p.getWaterRequirement())
            .toList();
    }
    
    /**
     * Gets all living plants in this zone.
     */
    public List<Plant> getLivingPlants() {
        return plantsInZone.stream()
            .filter(p -> !p.isDead())
            .toList();
    }
    
    /**
     * Updates zone moisture level.
     */
    public void updateMoisture(int amount) {
        moistureLevel.set(Math.max(0, Math.min(100, moistureLevel.get() + amount)));
    }
    
    /**
     * Updates zone temperature.
     */
    public void setTemperature(int newTemp) {
        temperature.set(newTemp);
    }
    
    /**
     * Updates pest infestation level.
     */
    public void updatePestLevel(int level) {
        pestInfestationLevel.set(Math.max(0, Math.min(100, level)));
    }
    
    /**
     * Decreases moisture naturally over time.
     */
    public void evaporate(int amount) {
        updateMoisture(-amount);
    }
    
    // Getters
    public int getZoneId() {
        return zoneId;
    }
    
    public List<Position> getBoundaries() {
        return new ArrayList<>(boundaries);
    }
    
    public List<Plant> getPlants() {
        return new ArrayList<>(plantsInZone);
    }
    
    public int getPlantCount() {
        return plantsInZone.size();
    }
    
    public int getLivingPlantCount() {
        return (int) plantsInZone.stream().filter(p -> !p.isDead()).count();
    }
    
    // Property getters
    public IntegerProperty moistureLevelProperty() {
        return moistureLevel;
    }
    
    public IntegerProperty temperatureProperty() {
        return temperature;
    }
    
    public IntegerProperty pestInfestationLevelProperty() {
        return pestInfestationLevel;
    }
    
    public int getMoistureLevel() {
        return moistureLevel.get();
    }
    
    public int getTemperature() {
        return temperature.get();
    }
    
    public int getPestInfestationLevel() {
        return pestInfestationLevel.get();
    }
    
    @Override
    public String toString() {
        return "Zone " + zoneId + " [Plants: " + getPlantCount() + 
               ", Moisture: " + moistureLevel.get() + "%, Temp: " + 
               temperature.get() + "°C]";
    }
}

