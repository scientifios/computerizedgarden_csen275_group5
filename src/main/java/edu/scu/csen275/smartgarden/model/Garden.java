package edu.scu.csen275.smartgarden.model;

import javafx.beans.property.*;
import edu.scu.csen275.smartgarden.util.Logger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central domain model representing the garden as a bounded grid with zones.
 *
 * The Garden acts as the primary coordinator for plant placement and retrieval.
 * Plants are indexed by {@link Position} and partitioned into a fixed 3x3 set
 * of {@link Zone}s for localized operations.
 *
 * This class also exposes JavaFX properties (e.g., weather and plant counts)
 * to support UI data binding, and provides basic statistics for monitoring
 * simulation state.
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
    * Partitions the garden grid into a fixed 3x3 set of zones.
    * The last zone in each row/column absorbs any remainder cells.
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
        
        for (Zone zone : zones) {
            if (zone.containsPosition(pos)) {
                zone.addPlant(plant);
                break;
            }
        }
        
        logger.info("Garden", "Planted " + plant.getPlantType() + " at " + pos + 
                   " with initial water: " + plant.getWaterRequirement());
        return true;
    }
    
    public boolean removePlant(Position position) {
        Plant plant = plantMap.remove(position);
        
        if (plant != null) {
            totalPlants.set(totalPlants.get() - 1);
            if (!plant.isDead()) {
                livingPlants.set(livingPlants.get() - 1);
            }
            
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
    
    public Plant getPlant(Position position) {
        return plantMap.get(position);
    }

    public List<Plant> getAllPlants() {
        return new ArrayList<>(plantMap.values());
    }

    public List<Plant> getLivingPlants() {
        return plantMap.values().stream()
            .filter(p -> !p.isDead())
            .toList();
    }
    
    public List<Plant> getDeadPlants() {
        return plantMap.values().stream()
            .filter(Plant::isDead)
            .toList();
    }
    
    /**
    * Recomputes the number of living plants and synchronizes the JavaFX property.
    */
    public void updateLivingCount() {
        long count = plantMap.values().stream().filter(p -> !p.isDead()).count();
        livingPlants.set((int) count);
    }
    
    public boolean isValidPosition(Position position) {
        return position.row() >= 0 && position.row() < rows &&
               position.column() >= 0 && position.column() < columns;
    }
    
    public boolean isPositionOccupied(Position position) {
        return plantMap.containsKey(position);
    }
    
    /**
    * Returns the zone that contains the given position, or {@code null} if none matches.
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
    * Returns the zone with the specified id, or {@code null} if not found.
    */
    public Zone getZone(int zoneId) {
        return zones.stream()
            .filter(z -> z.getZoneId() == zoneId)
            .findFirst()
            .orElse(null);
    }
    
    public void setWeather(String weather) {
        currentWeather.set(weather);
    }
    
    /**
    * Computes a snapshot of garden statistics.
    *
    * The returned map includes: totalPlants, livingPlants, deadPlants, zones,
    * plus per-plant-type counts keyed by the concrete class simple name
    * (e.g., "Flower", "Fruit").
    */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalPlants", totalPlants.get());
        stats.put("livingPlants", livingPlants.get());
        stats.put("deadPlants", totalPlants.get() - livingPlants.get());
        stats.put("zones", zones.size());
        
        Map<String, Integer> typeCounts = new HashMap<>();
        for (Plant plant : plantMap.values()) {
            String type = plant.getClass().getSimpleName();
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        stats.putAll(typeCounts);
        
        return stats;
    }
    
    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public List<Zone> getZones() { return new ArrayList<>(zones); }
    public LocalDateTime getCreationTime() { return creationTime; }
    
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

