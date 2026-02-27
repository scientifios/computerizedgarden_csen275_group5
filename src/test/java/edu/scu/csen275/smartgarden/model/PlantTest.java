package edu.scu.csen275.smartgarden.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlantTest {

    private Plant plant;
    private Position position;

    // Concrete implementation of abstract Plant class for testing
    private static class TestPlant extends Plant {
        public TestPlant(Position position) {
            super("TestPlant", position, 10, 100, 50, 10, 30, 2);
        }

        @Override
        public int getGrowthDuration() {
            return 2;
        }
    }

    @BeforeEach
    void setUp() {
        position = new Position(1, 1);
        plant = new TestPlant(position);
    }

    @Test
    void testInitialization() {
        assertEquals("TestPlant", plant.getPlantType());
        assertEquals(position, plant.getPosition());
        assertEquals(100, plant.getHealthLevel());
        assertEquals(100, plant.getWaterLevel()); // Starts with full water requirement
        assertEquals(0, plant.getDaysAlive());
        assertEquals(GrowthStage.SEED, plant.getGrowthStage());
        assertFalse(plant.isDead());
    }

    @Test
    void testWaterConsumption() {
        plant.update(); // 1 tick
        assertEquals(99, plant.getWaterLevel());

        // Simulate 50 ticks
        for (int i = 0; i < 50; i++) {
            plant.update();
        }
        assertEquals(49, plant.getWaterLevel());
    }

    @Test
    void testHealthDegradationWhenDry() {
        // Drain water
        for (int i = 0; i < 100; i++) {
            plant.update();
        }
        assertEquals(0, plant.getWaterLevel());

        int initialHealth = plant.getHealthLevel();
        plant.update(); // Should take damage due to 0 water
        assertTrue(plant.getHealthLevel() < initialHealth);
    }

    @Test
    void testWatering() {
        // Drain some water
        for (int i = 0; i < 50; i++) {
            plant.update();
        }
        int waterLevel = plant.getWaterLevel();

        plant.water(20);
        assertEquals(waterLevel + 20, plant.getWaterLevel());
    }

    @Test
    void testGrowth() {
        assertEquals(GrowthStage.SEED, plant.getGrowthStage());

        // Advance 1 day
        plant.advanceDay();
        assertEquals(GrowthStage.SEED, plant.getGrowthStage()); // Growth duration is 2

        // Advance 2nd day
        plant.advanceDay();
        assertEquals(GrowthStage.SEEDLING, plant.getGrowthStage());
    }

    @Test
    void testDeathByLifespan() {
        // Max lifespan is 10
        for (int i = 0; i < 10; i++) {
            plant.advanceDay();
            plant.update();
        }
        assertTrue(plant.isDead());
    }

    @Test
    void testDeathByHealth() {
        plant.takeDamage(100);
        assertTrue(plant.isDead());
    }
}
