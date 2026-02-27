package edu.scu.csen275.smartgarden.controller;

import edu.scu.csen275.smartgarden.model.*;
import edu.scu.csen275.smartgarden.simulation.SimulationEngine;
import edu.scu.csen275.smartgarden.util.Logger;

/**
 * Main controller that coordinates the garden model and simulation.
 * Mediates between UI and domain logic.
 */
public class GardenController {
    private final Garden garden;
    private final SimulationEngine simulationEngine;
    private final Logger logger;
    
    /**
     * Creates a new GardenController.
     */
    public GardenController(int rows, int columns) {
        this.garden = new Garden(rows, columns);
        this.simulationEngine = new SimulationEngine(garden);
        this.logger = Logger.getInstance();
        
        logger.info("Controller", "Garden controller initialized with " + 
                   rows + "x" + columns + " garden");
    }
    
    /**
     * Plants a new plant in the garden using PlantType enum.
     */
    public boolean plantSeed(PlantType plantType, Position position) {
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
     * Legacy method for backward compatibility - converts string to PlantType.
     */
    public boolean plantSeed(String plantType, Position position) {
        try {
            PlantType type = PlantType.valueOf(plantType.toUpperCase().replace(" ", "_"));
            return plantSeed(type, position);
        } catch (IllegalArgumentException e) {
            // Fallback to old system for compatibility
            Plant plant = createPlantLegacy(plantType, position);
            if (plant != null && garden.addPlant(plant)) {
                logger.info("Controller", "Plant " + plantType + " added at " + position);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Creates a plant based on PlantType enum.
     */
    private Plant createPlant(PlantType plantType, Position position) {
        return switch (plantType) {
            // Fruit Plants
            case STRAWBERRY -> new Fruit(position, "Strawberry");
            case GRAPEVINE -> new Fruit(position, "Grapevine");
            case APPLE -> new Fruit(position, "Apple Sapling");
            
            // Vegetable Crops
            case CARROT -> new Vegetable(position, "Carrot");
            case TOMATO -> new Vegetable(position, "Tomato");
            case ONION -> new Vegetable(position, "Onion");
            
            // Flowers
            case SUNFLOWER -> new Flower(position, "Sunflower");
            case TULIP -> new Flower(position, "Tulip");
            case ROSE -> new Flower(position, "Rose");
        };
    }
    
    /**
     * Legacy method for creating plants from string (backward compatibility).
     */
    private Plant createPlantLegacy(String plantType, Position position) {
        return switch (plantType.toLowerCase()) {
            case "flower" -> new Flower(position);
            case "vegetable", "tomato" -> new Vegetable(position, "Tomato");
            case "carrot" -> new Vegetable(position, "Carrot");
            // Tree, Herb, and Grass plant models have been removed
            // Only PlantType enum values are supported now
            default -> null;
        };
    }
    
    /**
     * Removes a plant from the garden.
     */
    public boolean removePlant(Position position) {
        return garden.removePlant(position);
    }
    
    /**
     * Starts the simulation.
     */
    public void startSimulation() {
        try {
            simulationEngine.start();
        } catch (IllegalStateException e) {
            logger.warning("Controller", "Cannot start simulation: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Pauses the simulation.
     */
    public void pauseSimulation() {
        simulationEngine.pause();
    }
    
    /**
     * Resumes the simulation.
     */
    public void resumeSimulation() {
        simulationEngine.resume();
    }
    
    /**
     * Stops the simulation.
     */
    public void stopSimulation() {
        simulationEngine.stop();
    }
    
    /**
     * Sets simulation speed.
     */
    public void setSimulationSpeed(int multiplier) {
        simulationEngine.setSpeed(multiplier);
    }
    
    /**
     * Manually waters a zone.
     */
    public void manualWaterZone(int zoneId) {
        simulationEngine.getWateringSystem().manualWater(zoneId);
    }
    
    /**
     * Manually treats a zone for pests.
     */
    public void manualTreatZone(int zoneId) {
        simulationEngine.getPestControlSystem().manualTreat(zoneId);
    }
    
    /**
     * Refills water supply.
     */
    public void refillWater() {
        simulationEngine.getWateringSystem().refillWater(5000);
    }
    
    /**
     * Refills pesticide stock.
     */
    public void refillPesticide() {
        simulationEngine.getPestControlSystem().refillPesticide(25);
    }
    
    // Getters
    public Garden getGarden() {
        return garden;
    }
    
    public SimulationEngine getSimulationEngine() {
        return simulationEngine;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    /**
     * Gets current simulation state as a readable string.
     */
    public String getSimulationStatus() {
        return simulationEngine.getState().toString();
    }
    
    /**
     * Shuts down the controller and releases resources.
     */
    public void shutdown() {
        if (simulationEngine.getState() == SimulationEngine.SimulationState.RUNNING) {
            simulationEngine.stop();
        }
        logger.close();
    }
}

