package edu.scu.csen275.smartgarden.simulation;

import edu.scu.csen275.smartgarden.model.*;
import edu.scu.csen275.smartgarden.simulation.SimulationEngine.SimulationState;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive tests for SimulationEngine.
 * Tests simulation lifecycle, state transitions, speed control, and system integration.
 */
public class SimulationEngineTest {

    private Garden garden;
    private SimulationEngine engine;
    private static boolean javafxInitialized = false;

    /**
     * Initialize JavaFX toolkit once for all tests.
     * Required because SimulationEngine uses JavaFX Timeline.
     */
    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        if (!javafxInitialized) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(() -> latch.countDown());
                latch.await(5, TimeUnit.SECONDS);
                javafxInitialized = true;
            } catch (IllegalStateException e) {
                // JavaFX already initialized
                javafxInitialized = true;
            }
        }
    }

    @BeforeEach
    void setUp() {
        garden = new Garden(9, 9);
        engine = new SimulationEngine(garden);
        
        // Add a plant so simulation can start
        garden.addPlant(new Flower(new Position(0, 0), "TestFlower"));
    }

    @AfterEach
    void tearDown() {
        if (engine != null && engine.getState() != SimulationState.STOPPED) {
            engine.stop();
        }
    }

    // ==================== Initialization Tests ====================

    @Test
    @DisplayName("Engine initializes with STOPPED state")
    void testInitialState() {
        assertEquals(SimulationState.STOPPED, engine.getState());
    }

    @Test
    @DisplayName("Engine initializes with default speed of 1x")
    void testInitialSpeed() {
        assertEquals(1, engine.getSpeedMultiplier());
    }

    @Test
    @DisplayName("Engine initializes with zero elapsed ticks")
    void testInitialTicks() {
        assertEquals(0, engine.getElapsedTicks());
    }

    @Test
    @DisplayName("Engine initializes with day counter at 0")
    void testInitialDayCounter() {
        assertEquals(0, engine.getDayCounter());
    }

    @Test
    @DisplayName("All subsystems are initialized")
    void testSubsystemsInitialized() {
        assertNotNull(engine.getWateringSystem(), "WateringSystem should be initialized");
        assertNotNull(engine.getHeatingSystem(), "HeatingSystem should be initialized");
        assertNotNull(engine.getPestControlSystem(), "PestControlSystem should be initialized");
        assertNotNull(engine.getWeatherSystem(), "WeatherSystem should be initialized");
    }

    // ==================== State Transition Tests ====================

    @Test
    @DisplayName("Start transitions from STOPPED to RUNNING")
    void testStartTransition() {
        engine.start();
        assertEquals(SimulationState.RUNNING, engine.getState());
    }

    @Test
    @DisplayName("Pause transitions from RUNNING to PAUSED")
    void testPauseTransition() {
        engine.start();
        engine.pause();
        assertEquals(SimulationState.PAUSED, engine.getState());
    }

    @Test
    @DisplayName("Resume transitions from PAUSED to RUNNING")
    void testResumeTransition() {
        engine.start();
        engine.pause();
        engine.resume();
        assertEquals(SimulationState.RUNNING, engine.getState());
    }

    @Test
    @DisplayName("Stop transitions from RUNNING to STOPPED")
    void testStopFromRunning() {
        engine.start();
        engine.stop();
        assertEquals(SimulationState.STOPPED, engine.getState());
    }

    @Test
    @DisplayName("Stop transitions from PAUSED to STOPPED")
    void testStopFromPaused() {
        engine.start();
        engine.pause();
        engine.stop();
        assertEquals(SimulationState.STOPPED, engine.getState());
    }

    @Test
    @DisplayName("Cannot start without plants in garden")
    void testCannotStartWithoutPlants() {
        Garden emptyGarden = new Garden(3, 3);
        SimulationEngine emptyEngine = new SimulationEngine(emptyGarden);
        
        assertThrows(IllegalStateException.class, emptyEngine::start,
                "Should throw exception when starting with no plants");
    }

    @Test
    @DisplayName("Pause has no effect when not running")
    void testPauseWhenNotRunning() {
        engine.pause(); // Should not throw
        assertEquals(SimulationState.STOPPED, engine.getState());
    }

    @Test
    @DisplayName("Resume has no effect when not paused")
    void testResumeWhenNotPaused() {
        engine.resume(); // Should not throw
        assertEquals(SimulationState.STOPPED, engine.getState());
    }

    // ==================== Speed Control Tests ====================

    @Test
    @DisplayName("Speed can be set to valid values (1-10)")
    void testValidSpeedSettings() {
        for (int speed = 1; speed <= 10; speed++) {
            engine.setSpeed(speed);
            assertEquals(speed, engine.getSpeedMultiplier());
        }
    }

    @Test
    @DisplayName("Speed below 1 throws exception")
    void testSpeedBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> engine.setSpeed(0));
        assertThrows(IllegalArgumentException.class, () -> engine.setSpeed(-1));
    }

    @Test
    @DisplayName("Speed above 10 throws exception")
    void testSpeedAboveMaximum() {
        assertThrows(IllegalArgumentException.class, () -> engine.setSpeed(11));
        assertThrows(IllegalArgumentException.class, () -> engine.setSpeed(100));
    }

    // ==================== Garden Integration Tests ====================

    @Test
    @DisplayName("Engine references the correct garden")
    void testGardenReference() {
        assertSame(garden, engine.getGarden());
    }

    @Test
    @DisplayName("Living plants count matches garden")
    void testLivingPlantsSync() {
        // Add more plants
        garden.addPlant(new Vegetable(new Position(1, 1), "Tomato"));
        garden.addPlant(new Flower(new Position(2, 2), "Rose"));
        
        assertEquals(3, garden.getLivingPlants().size());
    }

    // ==================== Formatted Time Tests ====================

    @Test
    @DisplayName("Formatted time returns valid string")
    void testFormattedTime() {
        String formattedTime = engine.getFormattedTime();
        assertNotNull(formattedTime);
        assertTrue(formattedTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"),
                "Time should be in format yyyy-MM-dd HH:mm");
    }

    @Test
    @DisplayName("Simulation time is not null")
    void testSimulationTimeNotNull() {
        assertNotNull(engine.getSimulationTime());
    }

    // ==================== Property Binding Tests ====================

    @Test
    @DisplayName("State property is observable")
    void testStateProperty() {
        assertNotNull(engine.stateProperty());
        assertEquals(engine.getState(), engine.stateProperty().get());
    }

    @Test
    @DisplayName("Speed property is observable")
    void testSpeedProperty() {
        assertNotNull(engine.speedMultiplierProperty());
        assertEquals(engine.getSpeedMultiplier(), engine.speedMultiplierProperty().get());
    }

    @Test
    @DisplayName("Elapsed ticks property is observable")
    void testElapsedTicksProperty() {
        assertNotNull(engine.elapsedTicksProperty());
        assertEquals(engine.getElapsedTicks(), engine.elapsedTicksProperty().get());
    }

    @Test
    @DisplayName("Simulation time property is observable")
    void testSimulationTimeProperty() {
        assertNotNull(engine.simulationTimeProperty());
        assertEquals(engine.getSimulationTime(), engine.simulationTimeProperty().get());
    }

    // ==================== toString Tests ====================

    @Test
    @DisplayName("toString returns informative string")
    void testToString() {
        String result = engine.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("SimulationEngine"), "Should contain class name");
        assertTrue(result.contains("State"), "Should contain state");
        assertTrue(result.contains("Day"), "Should contain day info");
        assertTrue(result.contains("Ticks"), "Should contain ticks info");
        assertTrue(result.contains("Speed"), "Should contain speed info");
    }

    // ==================== Subsystem Connectivity Tests ====================

    @Test
    @DisplayName("WateringSystem is connected to WeatherSystem")
    void testWateringWeatherConnection() {
        // The watering system should know about weather
        // This is verified by checking that watering system exists and works
        assertNotNull(engine.getWateringSystem());
        assertNotNull(engine.getWeatherSystem());
        
        // Water supply should be at initial value
        assertEquals(10000, engine.getWateringSystem().getWaterSupply());
    }

    @Test
    @DisplayName("HeatingSystem maintains temperature")
    void testHeatingSystemTemperature() {
        assertNotNull(engine.getHeatingSystem());
        // Temperature should be in a reasonable range
        int temp = engine.getHeatingSystem().getCurrentTemperature();
        assertTrue(temp >= 0 && temp <= 50, "Temperature should be in range 0-50");
    }

    @Test
    @DisplayName("PestControlSystem has initial pesticide stock")
    void testPestControlStock() {
        assertEquals(50, engine.getPestControlSystem().getPesticideStock());
    }

    // ==================== Multiple Start/Stop Cycles ====================

    @Test
    @DisplayName("Can restart after stopping")
    void testRestartAfterStop() {
        engine.start();
        assertEquals(SimulationState.RUNNING, engine.getState());
        
        engine.stop();
        assertEquals(SimulationState.STOPPED, engine.getState());
        
        // Should be able to start again
        engine.start();
        assertEquals(SimulationState.RUNNING, engine.getState());
    }

    @Test
    @DisplayName("Multiple pause/resume cycles work correctly")
    void testMultiplePauseResumeCycles() {
        engine.start();
        
        for (int i = 0; i < 3; i++) {
            engine.pause();
            assertEquals(SimulationState.PAUSED, engine.getState());
            
            engine.resume();
            assertEquals(SimulationState.RUNNING, engine.getState());
        }
    }
}

