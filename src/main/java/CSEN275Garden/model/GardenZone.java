package CSEN275Garden.model;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a management zone within the garden grid.
 *
 * A GardenZone groups a set of grid cells (stored as an explicit list of {@link GridPosition}s)
 * and tracks zone-level conditions such as moisture, temperature, and pest pressure.
 * It also maintains the set of plants currently located within the zone for queries
 * used by the simulation and UI.
 */
public class GardenZone {
    private final int zoneId;
    private final List<GridPosition> boundaries;
    private final List<Plant> plantsInZone;

    private final IntegerProperty moistureLevel;
    private final IntegerProperty temperature;
    private final IntegerProperty pestInfestationLevel;

    public GardenZone(int zoneId, List<GridPosition> boundaries) {
        this.zoneId = zoneId;
        this.boundaries = new ArrayList<>(boundaries);
        this.plantsInZone = new ArrayList<>();
        this.moistureLevel = new SimpleIntegerProperty(50);
        this.temperature = new SimpleIntegerProperty(20);
        this.pestInfestationLevel = new SimpleIntegerProperty(0);
    }

    public boolean containsPosition(GridPosition position) {
        return boundaries.contains(position);
    }

    public void addPlant(Plant plant) {
        if (!plantsInZone.contains(plant)) {
            plantsInZone.add(plant);
        }
    }

    public void removePlant(Plant plant) {
        plantsInZone.remove(plant);
    }

    /**
    * Returns living plants whose water level is below their water requirement.
    */
    public List<Plant> getPlantsNeedingWater() {
        return plantsInZone.stream()
            .filter(p -> !p.isDead())
            .filter(p -> p.getWaterLevel() < p.getWaterRequirement())
            .toList();
    }

    public List<Plant> getLivingPlants() {
        return plantsInZone.stream()
            .filter(p -> !p.isDead())
            .toList();
    }

    public void updateMoisture(int amount) {
        moistureLevel.set(Math.max(0, Math.min(100, moistureLevel.get() + amount)));
    }

    public void setTemperature(int newTemp) {
        temperature.set(newTemp);
    }

    public void updatePestLevel(int level) {
        pestInfestationLevel.set(Math.max(0, Math.min(100, level)));
    }

    public void evaporate(int amount) {
        updateMoisture(-amount);
    }

    public int getZoneId() {
        return zoneId;
    }

    public List<GridPosition> getBoundaries() {
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
