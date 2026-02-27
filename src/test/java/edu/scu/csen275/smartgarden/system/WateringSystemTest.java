package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Garden;
import edu.scu.csen275.smartgarden.model.PlantType;
import edu.scu.csen275.smartgarden.model.Position;
import edu.scu.csen275.smartgarden.simulation.WeatherSystem;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WateringSystemTest {

    private Garden garden;
    private WateringSystem wateringSystem;
    private HeatingSystem heatingSystem;
    private WeatherSystem weatherSystem;

    @BeforeEach
    void setUp() {
        garden = new Garden(3, 3); // Small 3x3 garden
        wateringSystem = new WateringSystem(garden);
        heatingSystem = new HeatingSystem(garden);
        weatherSystem = new WeatherSystem(garden, heatingSystem);
        wateringSystem.setWeatherSystem(weatherSystem);

        // Add a plant to Zone 1 (Position 0,0) so sprinklers have something to water
        // We use a concrete Plant implementation or a mock if possible, but here we use
        // a real one
        // Since Plant is abstract, we need a concrete subclass. Let's use a simple one
        // if available or anonymous class.
        // Looking at the codebase, we have Flower, Vegetable, etc. Let's use a Flower.
        garden.addPlant(new edu.scu.csen275.smartgarden.model.Flower(new Position(0, 0), "TestFlower"));
    }

    @Test
    void testInitialization() {
        assertEquals(10000, wateringSystem.getWaterSupply());
        assertEquals(40, wateringSystem.getMoistureThreshold());
        assertNotNull(wateringSystem.getSprinkler(1));
        assertNotNull(wateringSystem.getSensor(1));
    }

    @Test
    void testRefillWater() {
        wateringSystem.refillWater(500);
        assertEquals(10500, wateringSystem.getWaterSupply());
    }

    @Test
    void testWateringDepletesSupply() {
        int initialSupply = wateringSystem.getWaterSupply();
        wateringSystem.waterZone(1, 100);
        
        // The Sprinkler distributes water at flowRate (10L/min) per plant
        // With 1 plant, only 10L is actually used regardless of requested amount
        int expectedUsed = 10; // flowRate per plant
        assertEquals(initialSupply - expectedUsed, wateringSystem.getWaterSupply());
    }

    @Test
    void testRainStopsWatering() {
        // Test that watering is skipped when it's raining
        // First, set weather to rainy using the setWeather method
        weatherSystem.setWeather(WeatherSystem.Weather.RAINY);
        
        int initialSupply = wateringSystem.getWaterSupply();
        
        // Try to water - should be skipped because it's raining
        wateringSystem.waterZone(1, 100);
        
        // Supply should remain unchanged because watering was skipped
        assertEquals(initialSupply, wateringSystem.getWaterSupply());
    }

    @Test
    void testMoistureThresholdUpdate() {
        wateringSystem.setMoistureThreshold(60);
        assertEquals(60, wateringSystem.getMoistureThreshold());
    }
}
