package CSEN275Garden.simulation;

import CSEN275Garden.model.GardenPlot;
import CSEN275Garden.model.Plant;
import CSEN275Garden.system.CoolingSystem;
import CSEN275Garden.system.HeatingSystem;
import CSEN275Garden.system.PestControlSystem;
import CSEN275Garden.system.WateringSystem;
import CSEN275Garden.util.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX-driven simulation loop for the garden.
 * Coordinates per-tick updates across plants + systems.
 */
public class SimulationEngine {
    private final GardenPlot garden;
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
    private static final int BASE_TICK_INTERVAL_MS = 1000; // 1s real = 1 min sim (1x)
    private static final int TICKS_PER_SIM_DAY = 1440;     // minutes/day
    
    public SimulationEngine(GardenPlot garden) {
        this.garden = garden;
        this.wateringSystem = new WateringSystem(garden);
        this.heatingSystem = new HeatingSystem(garden);
        this.coolingSystem = new CoolingSystem(garden);
        this.pestControlSystem = new PestControlSystem(garden);
        this.weatherSystem = new WeatherSystem(garden, this.heatingSystem, this.coolingSystem);
        
        // Weather-aware watering (skip when raining)
        this.wateringSystem.setWeatherSystem(this.weatherSystem);
        
        this.state = new SimpleObjectProperty<>(SimulationState.STOPPED);
        this.speedMultiplier = new SimpleIntegerProperty(1);
        this.elapsedTicks = new SimpleLongProperty(0);
        this.simulationTime = new SimpleObjectProperty<>(LocalDateTime.now());
        
        this.ticksPerDay = 0;
        this.dayCounter = 0;
        
        this.timeline = new Timeline(
            new KeyFrame(Duration.millis(BASE_TICK_INTERVAL_MS), e -> tick())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        logger.info("Simulation", "Simulation engine created and ready");
    }
    
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
    
    public void pause() {
        if (state.get() != SimulationState.RUNNING) {
            return;
        }
        
        state.set(SimulationState.PAUSED);
        timeline.pause();
        logger.info("Simulation", "Simulation paused at tick " + elapsedTicks.get());
    }
    
    public void resume() {
        if (state.get() != SimulationState.PAUSED) {
            return;
        }
        
        state.set(SimulationState.RUNNING);
        timeline.play();
        logger.info("Simulation", "Simulation resumed");
    }
    
    public void stop() {
        state.set(SimulationState.STOPPED);
        timeline.stop();
        logger.info("Simulation", "Simulation stopped. Total ticks: " + elapsedTicks.get() + 
                   ", Days: " + dayCounter);
        
        // Log final statistics
        logStatistics();
    }
    
    public void setSpeed(int multiplier) {
        if (multiplier < 1 || multiplier > 10) {
            throw new IllegalArgumentException("Speed multiplier must be 1-10");
        }
        
        speedMultiplier.set(multiplier);
        timeline.setRate(multiplier);
        logger.info("Simulation", "Speed set to " + multiplier + "x");
    }
    
    private void tick() {
        try {
            elapsedTicks.set(elapsedTicks.get() + 1);
            ticksPerDay++;
            
            // +1 min sim time per tick
            simulationTime.set(simulationTime.get().plusMinutes(1));
            
            updatePlants();
            
            wateringSystem.checkAndWater();
            heatingSystem.update();
            coolingSystem.update();
            pestControlSystem.update();
            weatherSystem.update();
            
            autoRefillSupplies();
            
            // Check for new day
            if (ticksPerDay >= TICKS_PER_SIM_DAY) {
                advanceDay();
                ticksPerDay = 0;
            }
            
            garden.updateLivingCount();
            
            if (elapsedTicks.get() % 100 == 0) {
                logger.debug("Simulation", "Tick " + elapsedTicks.get() + 
                            " | Day " + dayCounter + 
                            " | Living plants: " + garden.getLivingPlants().size());
            }
            
        } catch (Exception e) {
            logger.logException("Simulation", "Error during tick " + elapsedTicks.get(), e);
        }
    }
    
    /** Auto-refill policy to keep long runs from stalling due to empty supplies. */
    private void autoRefillSupplies() {
        // Water: refill to INITIAL when below threshold (20%).
        final int WATER_THRESHOLD = 2000;
        final int INITIAL_WATER = 10000;
        
        int currentWater = wateringSystem.getWaterSupply();
        if (currentWater < WATER_THRESHOLD) {
            int refillAmount = INITIAL_WATER - currentWater;
            wateringSystem.refillWater(refillAmount);
            logger.info("Simulation", "Auto-refilled water supply: " + currentWater + "L -> " + 
                       INITIAL_WATER + "L");
        }
        
        // Pesticide: refill to INITIAL when below threshold (20%).
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
    
    private void updatePlants() {
        for (Plant plant : garden.getAllPlants()) {
            plant.update();
        }
    }
    
    private void advanceDay() {
        dayCounter++;
        logger.info("Simulation", "Day " + dayCounter + " complete. Living plants: " + 
                   garden.getLivingPlants().size() + "/" + garden.getTotalPlants());
        
        for (Plant plant : garden.getAllPlants()) {
            plant.advanceDay();
        }
    }
    
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
    
    // System
    public GardenPlot getGarden() {
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
    
    // Properties
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
    
    public String getFormattedTime() {
        return simulationTime.get().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
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

