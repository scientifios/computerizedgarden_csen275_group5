package edu.scu.csen275.smartgarden.simulation;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.system.*;
import edu.scu.csen275.smartgarden.util.Logger;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Headless simulation engine for API/monitoring use.
 * Uses ScheduledExecutorService instead of JavaFX Timeline.
 * Shares the same systems as SimulationEngine for consistency.
 * 
 * This allows the API to run continuous simulation in headless mode,
 * enabling true 24-hour survival testing without requiring JavaFX.
 */
public class HeadlessSimulationEngine {
    private final Garden garden;
    private final WateringSystem wateringSystem;
    private final HeatingSystem heatingSystem;
    private final CoolingSystem coolingSystem;
    private final PestControlSystem pestControlSystem;
    private final WeatherSystem weatherSystem;
    
    private ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;
    private final AtomicLong elapsedTicks = new AtomicLong(0);
    private final AtomicInteger dayCounter = new AtomicInteger(0);
    private volatile int ticksPerDay = 0;
    private volatile LocalDateTime simulationTime;
    
    private static final Logger logger = Logger.getInstance();
    private static final int BASE_TICK_INTERVAL_MS = 1000; // 1 second real time = 1 minute sim time
    private static final int TICKS_PER_SIM_DAY = 1440; // 1440 minutes in a day
    
    // Track active instances for automatic cleanup
    private static final Set<HeadlessSimulationEngine> activeInstances = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdownHookRegistered = false;
    private static final Object shutdownLock = new Object();
    
    /**
     * Creates a new HeadlessSimulationEngine with new systems.
     * Use this when you want independent systems from SimulationEngine.
     */
    public HeadlessSimulationEngine(Garden garden) {
        this.garden = garden;
        this.wateringSystem = new WateringSystem(garden);
        this.heatingSystem = new HeatingSystem(garden);
        this.coolingSystem = new CoolingSystem(garden);
        this.pestControlSystem = new PestControlSystem(garden);
        this.weatherSystem = new WeatherSystem(garden, this.heatingSystem, this.coolingSystem);
        
        // Connect weather system to watering system
        this.wateringSystem.setWeatherSystem(this.weatherSystem);
        
        this.simulationTime = LocalDateTime.now();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeadlessSimulationEngine");
            t.setDaemon(true);
            return t;
        });
        
        // Register this instance for automatic cleanup
        activeInstances.add(this);
        registerShutdownHook();
        
        logger.info("Simulation", "Headless simulation engine created");
    }
    
    /**
     * Creates a new HeadlessSimulationEngine reusing systems from existing SimulationEngine.
     * This allows both engines to share the same systems for consistency.
     * 
     * @param garden The garden model
     * @param wateringSystem Shared watering system
     * @param heatingSystem Shared heating system
     * @param coolingSystem Shared cooling system
     * @param pestControlSystem Shared pest control system
     * @param weatherSystem Shared weather system
     */
    public HeadlessSimulationEngine(Garden garden, 
                                    WateringSystem wateringSystem,
                                    HeatingSystem heatingSystem,
                                    CoolingSystem coolingSystem,
                                    PestControlSystem pestControlSystem,
                                    WeatherSystem weatherSystem) {
        this.garden = garden;
        this.wateringSystem = wateringSystem;
        this.heatingSystem = heatingSystem;
        this.coolingSystem = coolingSystem;
        this.pestControlSystem = pestControlSystem;
        this.weatherSystem = weatherSystem;
        
        this.simulationTime = LocalDateTime.now();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeadlessSimulationEngine");
            t.setDaemon(true);
            return t;
        });
        
        // Register this instance for automatic cleanup
        activeInstances.add(this);
        registerShutdownHook();
        
        logger.info("Simulation", "Headless simulation engine created (reusing systems)");
    }
    
    /**
     * Registers a JVM shutdown hook to automatically stop all active simulation engines.
     * This ensures cleanup happens even if stop() is not explicitly called.
     * Only registers once for all instances.
     */
    private static void registerShutdownHook() {
        synchronized (shutdownLock) {
            if (shutdownHookRegistered) {
                return;
            }
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                synchronized (activeInstances) {
                    for (HeadlessSimulationEngine engine : activeInstances) {
                        if (engine.isRunning) {
                            engine.isRunning = false;
                            engine.scheduler.shutdown();
                            try {
                                if (!engine.scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                                    engine.scheduler.shutdownNow();
                                }
                            } catch (InterruptedException e) {
                                engine.scheduler.shutdownNow();
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            }, "HeadlessSimulationEngine-ShutdownHook"));
            
            shutdownHookRegistered = true;
        }
    }
    
    /**
     * Starts the headless simulation loop.
     * Plants will update continuously, water will decrease, systems will respond automatically.
     */
    public void start() {
        if (isRunning) {
            logger.warning("Simulation", "Headless simulation already running");
            return;
        }
        
        if (garden.getLivingPlants().isEmpty()) {
            logger.warning("Simulation", "Cannot start - no plants in garden");
            throw new IllegalStateException("Garden must have at least one plant");
        }
        
        isRunning = true;
        scheduler.scheduleAtFixedRate(this::tick, 0, BASE_TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stops the headless simulation loop.
     * Note: This is optional - shutdown hook will handle cleanup automatically when JVM exits.
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Simulation", "Headless simulation stopped at tick " + elapsedTicks.get());
    }
    
    /**
     * Main simulation tick - same logic as SimulationEngine.tick()
     * Called every BASE_TICK_INTERVAL_MS milliseconds.
     */
    private void tick() {
        if (!isRunning) {
            return;
        }
        
        try {
            elapsedTicks.incrementAndGet();
            ticksPerDay++;
            
            // Advance simulation time by 1 minute
            simulationTime = simulationTime.plusMinutes(1);
            
            // Update all plants (water decreases, health updates)
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
            if (ticksPerDay >= TICKS_PER_SIM_DAY) {
                advanceDay();
                ticksPerDay = 0;
            }
            
            // Update garden living count
            garden.updateLivingCount();
            
            // Log every 100 ticks
            if (elapsedTicks.get() % 100 == 0) {
                logger.debug("Simulation", "Headless Tick " + elapsedTicks.get() + 
                            " | Day " + dayCounter.get() + 
                            " | Living plants: " + garden.getLivingPlants().size());
            }
            
        } catch (Exception e) {
            logger.logException("Simulation", "Error during headless tick " + elapsedTicks.get(), e);
            // Continue simulation despite errors
        }
    }
    
    /**
     * Updates all plants in the garden.
     * This causes water to decrease, health to update based on conditions.
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
        int day = dayCounter.incrementAndGet();
        logger.info("Simulation", "Headless Day " + day + " complete. Living plants: " + 
                   garden.getLivingPlants().size() + "/" + garden.getTotalPlants());
        
        // Advance all plants by one day
        for (Plant plant : garden.getAllPlants()) {
            plant.advanceDay();
        }
    }
    
    /**
     * Automatically refills water and pesticide supplies when they drop below threshold.
     * Same logic as SimulationEngine.
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
    
    // Getters for systems (same interface as SimulationEngine)
    public WateringSystem getWateringSystem() { return wateringSystem; }
    public HeatingSystem getHeatingSystem() { return heatingSystem; }
    public CoolingSystem getCoolingSystem() { return coolingSystem; }
    public PestControlSystem getPestControlSystem() { return pestControlSystem; }
    public WeatherSystem getWeatherSystem() { return weatherSystem; }
    public Garden getGarden() { return garden; }
    
    public boolean isRunning() { return isRunning; }
    public long getElapsedTicks() { return elapsedTicks.get(); }
    public int getDayCounter() { return dayCounter.get(); }
    public LocalDateTime getSimulationTime() { return simulationTime; }
}

