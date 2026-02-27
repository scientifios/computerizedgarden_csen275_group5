package edu.scu.csen275.smartgarden.simulation;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.system.CoolingSystem;
import edu.scu.csen275.smartgarden.system.HeatingSystem;
import edu.scu.csen275.smartgarden.system.PestControlSystem;
import edu.scu.csen275.smartgarden.system.WateringSystem;
import edu.scu.csen275.smartgarden.util.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Central simulation engine that coordinates all garden systems.
 * Manages time progression and system updates.
 */
public class SimulationEngine {
    private final Garden garden;
    private final WateringSystem wateringSystem;
    private final HeatingSystem heatingSystem;
    private final CoolingSystem coolingSystem;
    private final PestControlSystem pestControlSystem;
    private final WeatherSystem weatherSystem;
    
    private final Timeline timeline;
    private final ObjectProperty<SimulationState> state;
    private final IntegerProperty speedMultiplier;
    private final LongProperty elapsedTicks;
    private final ObjectProperty<LocalDateTime> simulationTime;
    
    private int ticksPerDay;
    private int dayCounter;
    
    private static final Logger logger = Logger.getInstance();
    private static final int BASE_TICK_INTERVAL_MS = 1000; // 1 second real time = 1 minute sim time at 1x
    private static final int TICKS_PER_SIM_DAY = 1440; // 1440 minutes in a day
    
    /**
     * Creates a new SimulationEngine.
     */
    public SimulationEngine(Garden garden) {
        this.garden = garden;
        this.wateringSystem = new WateringSystem(garden);
        this.heatingSystem = new HeatingSystem(garden);
        this.coolingSystem = new CoolingSystem(garden);
        this.pestControlSystem = new PestControlSystem(garden);
        this.weatherSystem = new WeatherSystem(garden, this.heatingSystem, this.coolingSystem);
        
        // Connect weather system to watering system (so watering skips when raining)
        this.wateringSystem.setWeatherSystem(this.weatherSystem);
        
        this.state = new SimpleObjectProperty<>(SimulationState.STOPPED);
        this.speedMultiplier = new SimpleIntegerProperty(1);
        this.elapsedTicks = new SimpleLongProperty(0);
        this.simulationTime = new SimpleObjectProperty<>(LocalDateTime.now());
        
        this.ticksPerDay = 0;
        this.dayCounter = 0;
        
        // Create timeline for simulation ticks
        this.timeline = new Timeline(
            new KeyFrame(Duration.millis(BASE_TICK_INTERVAL_MS), e -> tick())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        logger.info("Simulation", "Simulation engine created and ready");
    }
    
    /**
     * Starts the simulation.
     */
    public void start() {
        if (state.get() == SimulationState.RUNNING) {
            logger.warning("Simulation", "Simulation already running");
            return;
        }
        
        if (garden.getLivingPlants().isEmpty()) {
            logger.warning("Simulation", "Cannot start - no plants in garden");
            throw new IllegalStateException("Garden must have at least one plant");
        }
        
        state.set(SimulationState.RUNNING);
        timeline.play();
        logger.info("Simulation", "Simulation started at speed " + speedMultiplier.get() + "x");
    }
    
    /**
     * Pauses the simulation.
     */
    public void pause() {
        if (state.get() != SimulationState.RUNNING) {
            return;
        }
        
        state.set(SimulationState.PAUSED);
        timeline.pause();
        logger.info("Simulation", "Simulation paused at tick " + elapsedTicks.get());
    }
    
    /**
     * Resumes the simulation after pause.
     */
    public void resume() {
        if (state.get() != SimulationState.PAUSED) {
            return;
        }
        
        state.set(SimulationState.RUNNING);
        timeline.play();
        logger.info("Simulation", "Simulation resumed");
    }
    
    /**
     * Stops the simulation.
     */
    public void stop() {
        state.set(SimulationState.STOPPED);
        timeline.stop();
        logger.info("Simulation", "Simulation stopped. Total ticks: " + elapsedTicks.get() + 
                   ", Days: " + dayCounter);
        
        // Log final statistics
        logStatistics();
    }
    
    /**
     * Sets simulation speed multiplier.
     */
    public void setSpeed(int multiplier) {
        if (multiplier < 1 || multiplier > 10) {
            throw new IllegalArgumentException("Speed multiplier must be 1-10");
        }
        
        speedMultiplier.set(multiplier);
        timeline.setRate(multiplier);
        logger.info("Simulation", "Speed set to " + multiplier + "x");
    }
    
    /**
     * Main simulation tick - called every interval.
     */
    private void tick() {
        try {
            elapsedTicks.set(elapsedTicks.get() + 1);
            ticksPerDay++;
            
            // Advance simulation time by 1 minute
            simulationTime.set(simulationTime.get().plusMinutes(1));
            
            // Update all plants
            updatePlants();
            
            // Update all systems
            wateringSystem.checkAndWater();
            heatingSystem.update();
            coolingSystem.update();
            pestControlSystem.update();
            weatherSystem.update();
            
            // Auto-refill water and pesticide if below threshold
            autoRefillSupplies();
            
            // Check for new day
            if (ticksPerDay >= TICKS_PER_SIM_DAY / speedMultiplier.get()) {
                advanceDay();
                ticksPerDay = 0;
            }
            
            // Update garden living count
            garden.updateLivingCount();
            
            // Log every 100 ticks
            if (elapsedTicks.get() % 100 == 0) {
                logger.debug("Simulation", "Tick " + elapsedTicks.get() + 
                            " | Day " + dayCounter + 
                            " | Living plants: " + garden.getLivingPlants().size());
            }
            
        } catch (Exception e) {
            logger.logException("Simulation", "Error during tick " + elapsedTicks.get(), e);
            // Continue simulation despite errors
        }
    }
    
    /**
     * Automatically refills water and pesticide supplies when they drop below threshold.
     */
    private void autoRefillSupplies() {
        // Water supply threshold: 20% of initial (2000L out of 10000L)
        final int WATER_THRESHOLD = 2000;
        final int INITIAL_WATER = 10000;
        
        int currentWater = wateringSystem.getWaterSupply();
        if (currentWater < WATER_THRESHOLD) {
            int refillAmount = INITIAL_WATER - currentWater;
            wateringSystem.refillWater(refillAmount);
            logger.info("Simulation", "Auto-refilled water supply: " + currentWater + "L -> " + 
                       INITIAL_WATER + "L");
        }
        
        // Pesticide stock threshold: 20% of initial (10 out of 50)
        final int PESTICIDE_THRESHOLD = 10;
        final int INITIAL_PESTICIDE = 50;
        
        int currentPesticide = pestControlSystem.getPesticideStock();
        if (currentPesticide < PESTICIDE_THRESHOLD) {
            int refillAmount = INITIAL_PESTICIDE - currentPesticide;
            pestControlSystem.refillPesticide(refillAmount);
            logger.info("Simulation", "Auto-refilled pesticide stock: " + currentPesticide + " -> " + 
                       INITIAL_PESTICIDE);
        }
    }
    
    /**
     * Updates all plants in the garden.
     */
    private void updatePlants() {
        for (Plant plant : garden.getAllPlants()) {
            plant.update();
        }
    }
    
    /**
     * Advances to a new simulation day.
     */
    private void advanceDay() {
        dayCounter++;
        logger.info("Simulation", "Day " + dayCounter + " complete. Living plants: " + 
                   garden.getLivingPlants().size() + "/" + garden.getTotalPlants());
        
        // Advance all plants by one day
        for (Plant plant : garden.getAllPlants()) {
            plant.advanceDay();
        }
    }
    
    /**
     * Logs simulation statistics.
     */
    private void logStatistics() {
        logger.info("Statistics", "=== Simulation Summary ===");
        logger.info("Statistics", "Total ticks: " + elapsedTicks.get());
        logger.info("Statistics", "Days elapsed: " + dayCounter);
        logger.info("Statistics", "Total plants: " + garden.getTotalPlants());
        logger.info("Statistics", "Living plants: " + garden.getLivingPlants().size());
        logger.info("Statistics", "Dead plants: " + garden.getDeadPlants().size());
        logger.info("Statistics", "Water used: " + 
                   (10000 - wateringSystem.getWaterSupply()) + "L");
        logger.info("Statistics", "Heating energy used: " + 
                   heatingSystem.getEnergyConsumption() + " units");
        logger.info("Statistics", "Cooling energy used: " + 
                   coolingSystem.getEnergyConsumption() + " units");
        logger.info("Statistics", "Pesticide used: " + 
                   (50 - pestControlSystem.getPesticideStock()) + " applications");
        logger.info("Statistics", "=========================");
    }
    
    // System getters
    public Garden getGarden() {
        return garden;
    }
    
    public WateringSystem getWateringSystem() {
        return wateringSystem;
    }
    
    public HeatingSystem getHeatingSystem() {
        return heatingSystem;
    }
    
    public CoolingSystem getCoolingSystem() {
        return coolingSystem;
    }
    
    public PestControlSystem getPestControlSystem() {
        return pestControlSystem;
    }
    
    public WeatherSystem getWeatherSystem() {
        return weatherSystem;
    }
    
    // Property getters
    public ObjectProperty<SimulationState> stateProperty() {
        return state;
    }
    
    public IntegerProperty speedMultiplierProperty() {
        return speedMultiplier;
    }
    
    public LongProperty elapsedTicksProperty() {
        return elapsedTicks;
    }
    
    public ObjectProperty<LocalDateTime> simulationTimeProperty() {
        return simulationTime;
    }
    
    // Value getters
    public SimulationState getState() {
        return state.get();
    }
    
    public int getSpeedMultiplier() {
        return speedMultiplier.get();
    }
    
    public long getElapsedTicks() {
        return elapsedTicks.get();
    }
    
    public LocalDateTime getSimulationTime() {
        return simulationTime.get();
    }
    
    public int getDayCounter() {
        return dayCounter;
    }
    
    /**
     * Gets formatted simulation time string.
     */
    public String getFormattedTime() {
        return simulationTime.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    /**
     * Simulation state enumeration.
     */
    public enum SimulationState {
        STOPPED,
        RUNNING,
        PAUSED
    }
    
    @Override
    public String toString() {
        return "SimulationEngine[State: " + state.get() + 
               ", Day: " + dayCounter + 
               ", Ticks: " + elapsedTicks.get() + 
               ", Speed: " + speedMultiplier.get() + "x]";
    }
}

