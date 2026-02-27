package edu.scu.csen275.smartgarden.model;

import javafx.beans.property.*;
import edu.scu.csen275.smartgarden.util.Logger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the entire garden with its grid layout and zones.
 * Central domain model that coordinates all plants and zones.
 */
public class Garden {
    private final int rows;
    private final int columns;
    private final Map<Position, Plant> plantMap;
    private final List<Zone> zones;
    private final LocalDateTime creationTime;
    
    private final ObjectProperty<String> currentWeather;
    private final IntegerProperty totalPlants;
    private final IntegerProperty livingPlants;
    
    private static final Logger logger = Logger.getInstance();
    
    /**
     * Creates a new Garden with specified dimensions.
     */
    public Garden(int rows, int columns) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Garden dimensions must be positive");
        }
        
        this.rows = rows;
        this.columns = columns;
        this.plantMap = new HashMap<>();
        this.zones = new ArrayList<>();
        this.creationTime = LocalDateTime.now();
        
        this.currentWeather = new SimpleObjectProperty<>("SUNNY");
        this.totalPlants = new SimpleIntegerProperty(0);
        this.livingPlants = new SimpleIntegerProperty(0);
        
        initializeZones();
        
        logger.info("Garden", "Created " + rows + "x" + columns + " garden with " + 
                   zones.size() + " zones");
    }
    
    /**
     * Initializes zones for the garden (3x3 grid of zones).
     */
    private void initializeZones() {
        int zoneRows = rows / 3;
        int zoneCols = columns / 3;
        int zoneId = 1;
        
        for (int zr = 0; zr < 3; zr++) {
            for (int zc = 0; zc < 3; zc++) {
                List<Position> boundaries = new ArrayList<>();
                
                int startRow = zr * zoneRows;
                int endRow = (zr == 2) ? rows : (zr + 1) * zoneRows;
                int startCol = zc * zoneCols;
                int endCol = (zc == 2) ? columns : (zc + 1) * zoneCols;
                
                for (int r = startRow; r < endRow; r++) {
                    for (int c = startCol; c < endCol; c++) {
                        boundaries.add(new Position(r, c));
                    }
                }
                
                zones.add(new Zone(zoneId++, boundaries));
            }
        }
    }
    
    /**
     * Adds a plant to the garden.
     */
    public boolean addPlant(Plant plant) {
        Position pos = plant.getPosition();
        
        if (!isValidPosition(pos)) {
            logger.warning("Garden", "Invalid position: " + pos);
            return false;
        }
        
        if (isPositionOccupied(pos)) {
            logger.warning("Garden", "Position already occupied: " + pos);
            return false;
        }
        
        plantMap.put(pos, plant);
        totalPlants.set(totalPlants.get() + 1);
        livingPlants.set(livingPlants.get() + 1);
        
        // Add to appropriate zone
        for (Zone zone : zones) {
            if (zone.containsPosition(pos)) {
                zone.addPlant(plant);
                break;
            }
        }
        
        // Plant already starts with waterLevel = waterRequirement in constructor
        // No need for additional watering here
        
        logger.info("Garden", "Planted " + plant.getPlantType() + " at " + pos + 
                   " with initial water: " + plant.getWaterRequirement());
        return true;
    }
    
    /**
     * Removes a plant from the garden.
     */
    public boolean removePlant(Position position) {
        Plant plant = plantMap.remove(position);
        
        if (plant != null) {
            totalPlants.set(totalPlants.get() - 1);
            if (!plant.isDead()) {
                livingPlants.set(livingPlants.get() - 1);
            }
            
            // Remove from zone
            for (Zone zone : zones) {
                if (zone.containsPosition(position)) {
                    zone.removePlant(plant);
                    break;
                }
            }
            
            logger.info("Garden", "Removed plant from " + position);
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets the plant at a specific position.
     */
    public Plant getPlant(Position position) {
        return plantMap.get(position);
    }
    
    /**
     * Gets all plants in the garden.
     */
    public List<Plant> getAllPlants() {
        return new ArrayList<>(plantMap.values());
    }
    
    /**
     * Gets all living plants.
     */
    public List<Plant> getLivingPlants() {
        return plantMap.values().stream()
            .filter(p -> !p.isDead())
            .toList();
    }
    
    /**
     * Gets all dead plants.
     */
    public List<Plant> getDeadPlants() {
        return plantMap.values().stream()
            .filter(Plant::isDead)
            .toList();
    }
    
    /**
     * Updates living plant count.
     */
    public void updateLivingCount() {
        long count = plantMap.values().stream().filter(p -> !p.isDead()).count();
        livingPlants.set((int) count);
    }
    
    /**
     * Checks if a position is valid within the garden bounds.
     */
    public boolean isValidPosition(Position position) {
        return position.row() >= 0 && position.row() < rows &&
               position.column() >= 0 && position.column() < columns;
    }
    
    /**
     * Checks if a position is occupied by a plant.
     */
    public boolean isPositionOccupied(Position position) {
        return plantMap.containsKey(position);
    }
    
    /**
     * Gets the zone containing a specific position.
     */
    public Zone getZoneForPosition(Position position) {
        for (Zone zone : zones) {
            if (zone.containsPosition(position)) {
                return zone;
            }
        }
        return null;
    }
    
    /**
     * Gets zone by ID.
     */
    public Zone getZone(int zoneId) {
        return zones.stream()
            .filter(z -> z.getZoneId() == zoneId)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Sets the current weather.
     */
    public void setWeather(String weather) {
        currentWeather.set(weather);
    }
    
    /**
     * Gets garden statistics.
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalPlants", totalPlants.get());
        stats.put("livingPlants", livingPlants.get());
        stats.put("deadPlants", totalPlants.get() - livingPlants.get());
        stats.put("zones", zones.size());
        
        // Plant type counts
        Map<String, Integer> typeCounts = new HashMap<>();
        for (Plant plant : plantMap.values()) {
            String type = plant.getClass().getSimpleName();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        stats.putAll(typeCounts);
        
        return stats;
    }
    
    // Getters
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public List<Zone> getZones() { return new ArrayList<>(zones); }
    public LocalDateTime getCreationTime() { return creationTime; }
    
    // Property getters
    public ObjectProperty<String> currentWeatherProperty() { return currentWeather; }
    public IntegerProperty totalPlantsProperty() { return totalPlants; }
    public IntegerProperty livingPlantsProperty() { return livingPlants; }
    
    public String getCurrentWeather() { return currentWeather.get(); }
    public int getTotalPlants() { return totalPlants.get(); }
    public int getLivingPlantCount() { return livingPlants.get(); }
    
    @Override
    public String toString() {
        return "Garden[" + rows + "x" + columns + ", Plants: " + 
               livingPlants.get() + "/" + totalPlants.get() + " alive]";
    }
}

