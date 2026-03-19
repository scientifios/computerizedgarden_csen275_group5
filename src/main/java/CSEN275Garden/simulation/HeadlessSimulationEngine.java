package CSEN275Garden.simulation;

import CSEN275Garden.model.GardenPlot;
import CSEN275Garden.model.Plant;
import CSEN275Garden.system.*;
import CSEN275Garden.util.Logger;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Headless simulation loop (no JavaFX).
 * Runs on ScheduledExecutorService and can reuse the same systems as SimulationEngine.
 */
public class HeadlessSimulationEngine {
    private final GardenPlot garden;
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
    private static final int TICKS_PER_SIM_DAY = 60; // 1440 minutes in a day
    
    // Active instances for JVM shutdown cleanup
    private static final Set<HeadlessSimulationEngine> activeInstances = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdownHookRegistered = false;
    private static final Object shutdownLock = new Object();
    
    /** Creates an engine with its own system instances (independent from SimulationEngine). */
    public HeadlessSimulationEngine(GardenPlot garden) {
        this.garden = garden;
        this.wateringSystem = new WateringSystem(garden);
        this.heatingSystem = new HeatingSystem(garden);
        this.coolingSystem = new CoolingSystem(garden);
        this.pestControlSystem = new PestControlSystem(garden);
        this.weatherSystem = new WeatherSystem(garden, this.heatingSystem, this.coolingSystem);
        
        // Connect weather -> watering
        this.wateringSystem.setWeatherSystem(this.weatherSystem);
        
        this.simulationTime = LocalDateTime.now();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeadlessSimulationEngine");
            t.setDaemon(true);
            return t;
        });
        
        activeInstances.add(this);
        registerShutdownHook();
        
        logger.info("Simulation", "Headless simulation engine created");
    }
    
    /** Creates an engine reusing existing system instances (shared state with SimulationEngine). */
    public HeadlessSimulationEngine(GardenPlot garden, 
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
        
        activeInstances.add(this);
        registerShutdownHook();
        
        logger.info("Simulation", "Headless simulation engine created (reusing systems)");
    }
    
    /** Registers one JVM shutdown hook to stop all active instances. */
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
    
    private void tick() {
        if (!isRunning) {
            return;
        }
        
        try {
            elapsedTicks.incrementAndGet();
            ticksPerDay++;
            
            simulationTime = simulationTime.plusMinutes(1);
            
            updatePlants();
            
            wateringSystem.checkAndWater();
            heatingSystem.update();
            coolingSystem.update();
            pestControlSystem.update();
            weatherSystem.update();
            
            autoRefillSupplies();
            
            if (ticksPerDay >= TICKS_PER_SIM_DAY) {
                advanceDay();
                ticksPerDay = 0;
            }
            
            garden.updateLivingCount();
            
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
    
    private void updatePlants() {
        for (Plant plant : garden.getAllPlants()) {
            plant.update();
        }
    }
    
    private void advanceDay() {
        int day = dayCounter.incrementAndGet();
        logger.info("Simulation", "Headless Day " + day + " complete. Living plants: " + 
                   garden.getLivingPlants().size() + "/" + garden.getTotalPlants());
        weatherSystem.apiAdvanceDay();
        // weatherSystem.update();
        // Advance all plants by one day
        for (Plant plant : garden.getAllPlants()) {
            plant.advanceDay();
        }
    }
    
    /** Auto-refill policy (kept consistent with SimulationEngine). */
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
    
    public WateringSystem getWateringSystem() { return wateringSystem; }
    public HeatingSystem getHeatingSystem() { return heatingSystem; }
    public CoolingSystem getCoolingSystem() { return coolingSystem; }
    public PestControlSystem getPestControlSystem() { return pestControlSystem; }
    public WeatherSystem getWeatherSystem() { return weatherSystem; }
    public GardenPlot getGarden() { return garden; }
    
    public boolean isRunning() { return isRunning; }
    public long getElapsedTicks() { return elapsedTicks.get(); }
    public int getDayCounter() { return dayCounter.get(); }
    public LocalDateTime getSimulationTime() { return simulationTime; }
}
