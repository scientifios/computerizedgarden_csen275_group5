package CSEN275Garden.controller;

import CSEN275Garden.model.*;
import CSEN275Garden.simulation.SimulationEngine;
import CSEN275Garden.util.Logger;

/**
 * Bridge between UI actions and the garden/simulation domain layer.
 */
public class GardenController {
    private final GardenPlot garden;
    private final SimulationEngine simulationEngine;
    private final Logger logger;
    
    public GardenController(int rows, int columns) {
        this.garden = new GardenPlot(rows, columns);
        this.simulationEngine = new SimulationEngine(garden);
        this.logger = Logger.getInstance();
        
        logger.info("Controller", "Garden controller initialized with " + 
                   rows + "x" + columns + " garden");
    }
   
    public boolean plantSeed(PlantType plantType, GridPosition position) {
        try {
            Plant plant = createPlant(plantType, position);
            if (plant != null && garden.addPlant(plant)) {
                logger.info("Controller", "Plant " + plantType.getDisplayName() + " added at " + position);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.logException("Controller", "Failed to plant seed", e);
            return false;
        }
    }
    
    /**
    * String-based API kept for backward compatibility.
    * Tries to map to PlantType first; falls back to legacy mapping if needed.
    */
    public boolean plantSeed(String plantType, GridPosition position) {
        try {
            PlantType type = PlantType.valueOf(plantType.toUpperCase().replace(" ", "_"));
            return plantSeed(type, position);
        } catch (IllegalArgumentException e) {
            // Legacy fallback for older string inputs.
            Plant plant = createPlantLegacy(plantType, position);
            if (plant != null && garden.addPlant(plant)) {
                logger.info("Controller", "Plant " + plantType + " added at " + position);
                return true;
            }
            return false;
        }
    }
    
    /** Factory for PlantType -> Plant instance. */
    private Plant createPlant(PlantType plantType, GridPosition position) {
        return switch (plantType) {
            // Fruit Plants
            case STRAWBERRY -> new Fruit(position, "Strawberry");
            case CHERRY -> new Fruit(position, "Cherry");
            case APPLE -> new Fruit(position, "Apple Sapling");
            
            // Vegetable Crops
            case CABBAGE -> new Vegetable(position, "Cabbage");
            case TOMATO -> new Vegetable(position, "Tomato");
            case SCALLION -> new Vegetable(position, "Scallion");
            
            // Flowers
            case DAISY -> new Flower(position, "Daisy");
            case LILY -> new Flower(position, "Lily");
            case PEONY -> new Flower(position, "peony");
        };
    }
    
    /**
     * Legacy string-to-plant mapping.
     * Only kept to support older UI/input paths.
     */
    private Plant createPlantLegacy(String plantType, GridPosition position) {
        return switch (plantType.toLowerCase()) {
            case "flower" -> new Flower(position);
            case "vegetable", "tomato" -> new Vegetable(position, "Tomato");
            case "carrot" -> new Vegetable(position, "Carrot");
            // Legacy support is limited: removed plant categories are intentionally not supported.
            default -> null;
        };
    }
    
    public boolean removePlant(GridPosition position) {
        return garden.removePlant(position);
    }
    
    public void startSimulation() {
        try {
            simulationEngine.start();
        } catch (IllegalStateException e) {
            logger.warning("Controller", "Cannot start simulation: " + e.getMessage());
            throw e;
        }
    }

    public void pauseSimulation() {
        simulationEngine.pause();
    }
    
    public void resumeSimulation() {
        simulationEngine.resume();
    }
    
    public void stopSimulation() {
        simulationEngine.stop();
    }
    
    public void setSimulationSpeed(int multiplier) {
        simulationEngine.setSpeed(multiplier);
    }
    
    public void manualWaterZone(int zoneId) {
        simulationEngine.getWateringSystem().manualWater(zoneId);
    }
    
    public void manualTreatZone(int zoneId) {
        simulationEngine.getPestControlSystem().manualTreat(zoneId);
    }
    
    public void refillWater() {
        simulationEngine.getWateringSystem().refillWater(5000);
    }
    
    public void refillPesticide() {
        simulationEngine.getPestControlSystem().refillPesticide(25);
    }
    
    public GardenPlot getGarden() {
        return garden;
    }
    
    public SimulationEngine getSimulationEngine() {
        return simulationEngine;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public String getSimulationStatus() {
        return simulationEngine.getState().toString();
    }
    
    /** Stops simulation if running and closes logger. */
    public void shutdown() {
        if (simulationEngine.getState() == SimulationEngine.SimulationState.RUNNING) {
            simulationEngine.stop();
        }
        logger.close();
    }
}

