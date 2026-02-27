package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.*;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive tests for PestControlSystem.
 * Tests pest management, threat assessment, treatment, and pesticide stock.
 */
public class PestControlSystemTest {

    private Garden garden;
    private PestControlSystem pestControl;
    private static boolean javafxInitialized = false;

    /**
     * Initialize JavaFX toolkit once for all tests.
     * Required because PestControlSystem may use JavaFX Timeline for delayed treatment.
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
        pestControl = new PestControlSystem(garden);
        
        // Add some plants for testing
        garden.addPlant(new Flower(new Position(0, 0), "Rose"));
        garden.addPlant(new Vegetable(new Position(1, 1), "Tomato"));
        garden.addPlant(new Flower(new Position(4, 4), "Sunflower"));
    }

    // ==================== Initialization Tests ====================

    @Test
    @DisplayName("PestControlSystem initializes with correct pesticide stock")
    void testInitialPesticideStock() {
        assertEquals(50, pestControl.getPesticideStock());
    }

    @Test
    @DisplayName("PestControlSystem initializes with no pests")
    void testInitialPestCount() {
        assertEquals(0, pestControl.getHarmfulPestCount());
    }

    @Test
    @DisplayName("PestControlSystem initializes with default sensitivity")
    void testInitialSensitivity() {
        assertEquals(50, pestControl.getDetectionSensitivity());
    }

    @Test
    @DisplayName("PestControlSystem initializes with default treatment threshold")
    void testInitialThreshold() {
        assertEquals(30, pestControl.getTreatmentThreshold());
    }

    // ==================== Pesticide Stock Management Tests ====================

    @Test
    @DisplayName("Refilling pesticide increases stock")
    void testRefillPesticide() {
        int initialStock = pestControl.getPesticideStock();
        pestControl.refillPesticide(10);
        assertEquals(initialStock + 10, pestControl.getPesticideStock());
    }

    @Test
    @DisplayName("Multiple refills accumulate correctly")
    void testMultipleRefills() {
        int initialStock = pestControl.getPesticideStock();
        pestControl.refillPesticide(5);
        pestControl.refillPesticide(10);
        pestControl.refillPesticide(15);
        assertEquals(initialStock + 30, pestControl.getPesticideStock());
    }

    @Test
    @DisplayName("Refilling with zero does not change stock")
    void testRefillZero() {
        int initialStock = pestControl.getPesticideStock();
        pestControl.refillPesticide(0);
        assertEquals(initialStock, pestControl.getPesticideStock());
    }

    // ==================== Pest Creation and Management Tests ====================

    @Test
    @DisplayName("HarmfulPest is created correctly")
    void testHarmfulPestCreation() {
        Position pos = new Position(0, 0);
        HarmfulPest pest = new HarmfulPest("Red Mite", pos);
        
        assertEquals("Red Mite", pest.getPestType());
        assertEquals(pos, pest.getPosition());
        assertTrue(pest.isAlive());
        assertFalse(pest.isBeneficial());
    }

    @Test
    @DisplayName("HarmfulPest default type is Red Mite")
    void testDefaultHarmfulPest() {
        Position pos = new Position(1, 1);
        HarmfulPest pest = new HarmfulPest(pos);
        
        assertEquals("Red Mite", pest.getPestType());
    }

    @Test
    @DisplayName("Different pest types have correct damage rates")
    void testPestDamageRates() {
        Position pos = new Position(0, 0);
        
        HarmfulPest redMite = new HarmfulPest("Red Mite", pos);
        assertEquals(2, redMite.getDamageRate());
        
        HarmfulPest greenWorm = new HarmfulPest("Green Leaf Worm", pos);
        assertEquals(3, greenWorm.getDamageRate());
        
        HarmfulPest blackBeetle = new HarmfulPest("Black Beetle", pos);
        assertEquals(4, blackBeetle.getDamageRate());
        
        HarmfulPest brownCaterpillar = new HarmfulPest("Brown Caterpillar", pos);
        assertEquals(2, brownCaterpillar.getDamageRate());
    }

    @Test
    @DisplayName("Pest can be eliminated")
    void testPestElimination() {
        HarmfulPest pest = new HarmfulPest(new Position(0, 0));
        assertTrue(pest.isAlive());
        
        pest.eliminate();
        assertFalse(pest.isAlive());
    }

    @Test
    @DisplayName("Pest position can be changed")
    void testPestPositionChange() {
        Position originalPos = new Position(0, 0);
        Position newPos = new Position(1, 1);
        HarmfulPest pest = new HarmfulPest(originalPos);
        
        pest.setPosition(newPos);
        assertEquals(newPos, pest.getPosition());
    }

    // ==================== Pest Damage Tests ====================

    @Test
    @DisplayName("Pest causes damage to plant")
    void testPestCausesDamage() {
        Plant plant = garden.getPlant(new Position(0, 0));
        int initialHealth = plant.getHealthLevel();
        
        HarmfulPest pest = new HarmfulPest(new Position(0, 0));
        pest.causeDamage(plant);
        
        assertTrue(plant.getHealthLevel() < initialHealth, 
                "Plant health should decrease after pest attack");
    }

    @Test
    @DisplayName("Pest tracks attack count on plant")
    void testPestAttackCount() {
        Plant plant = garden.getPlant(new Position(0, 0));
        assertEquals(0, plant.getPestAttacks());
        
        HarmfulPest pest = new HarmfulPest(new Position(0, 0));
        pest.causeDamage(plant);
        
        assertEquals(1, plant.getPestAttacks());
    }

    @Test
    @DisplayName("Dead pest does not cause damage")
    void testDeadPestNoDamage() {
        Plant plant = garden.getPlant(new Position(0, 0));
        int initialHealth = plant.getHealthLevel();
        
        HarmfulPest pest = new HarmfulPest(new Position(0, 0));
        pest.eliminate();
        pest.causeDamage(plant);
        
        assertEquals(initialHealth, plant.getHealthLevel(),
                "Dead pest should not cause damage");
    }

    @Test
    @DisplayName("Pest does not damage dead plant")
    void testNoDamageToDeadPlant() {
        Plant plant = garden.getPlant(new Position(0, 0));
        plant.takeDamage(100); // Kill the plant
        assertTrue(plant.isDead());
        
        HarmfulPest pest = new HarmfulPest(new Position(0, 0));
        // Should not throw exception
        pest.causeDamage(plant);
    }

    // ==================== Manual Treatment Tests ====================

    @Test
    @DisplayName("Manual treatment decreases pesticide stock")
    void testManualTreatmentDecreasesStock() {
        int initialStock = pestControl.getPesticideStock();
        
        // Add a pest first to ensure treatment is applied
        Zone zone1 = garden.getZone(1);
        zone1.updatePestLevel(50); // Set high infestation level
        
        pestControl.manualTreat(1);
        
        assertEquals(initialStock - 1, pestControl.getPesticideStock());
    }

    @Test
    @DisplayName("Manual treatment on invalid zone does not crash")
    void testManualTreatInvalidZone() {
        int initialStock = pestControl.getPesticideStock();
        
        // Treating non-existent zone should not throw
        pestControl.manualTreat(999);
        
        // Stock should remain unchanged for invalid zone
        assertEquals(initialStock, pestControl.getPesticideStock());
    }

    @Test
    @DisplayName("Treatment reduces plant pest attacks")
    void testTreatmentReducesPestAttacks() {
        Plant plant = garden.getPlant(new Position(0, 0));
        
        // Simulate pest attacks
        for (int i = 0; i < 10; i++) {
            plant.pestAttack();
        }
        int attacksBeforeTreatment = plant.getPestAttacks();
        assertTrue(attacksBeforeTreatment > 0);
        
        // Apply treatment
        Zone zone1 = garden.getZone(1);
        zone1.updatePestLevel(50);
        pestControl.manualTreat(1);
        
        assertTrue(plant.getPestAttacks() < attacksBeforeTreatment,
                "Pest attacks should be reduced after treatment");
    }

    // ==================== Pest List Management Tests ====================

    @Test
    @DisplayName("getPests returns copy of list (not direct reference)")
    void testGetPestsReturnsCopy() {
        List<Pest> pests1 = pestControl.getPests();
        List<Pest> pests2 = pestControl.getPests();
        
        assertNotSame(pests1, pests2, "Should return a copy, not the same reference");
    }

    @Test
    @DisplayName("getActivePestCountAtPosition returns correct count")
    void testActivePestCountAtPosition() {
        // Initially no pests
        assertEquals(0, pestControl.getActivePestCountAtPosition(new Position(0, 0)));
        assertEquals(0, pestControl.getActivePestCountAtPosition(new Position(1, 1)));
    }

    // ==================== Property Tests ====================

    @Test
    @DisplayName("Pesticide stock property is observable")
    void testPesticideStockProperty() {
        assertNotNull(pestControl.pesticideStockProperty());
        assertEquals(pestControl.getPesticideStock(), 
                pestControl.pesticideStockProperty().get());
    }

    @Test
    @DisplayName("Detection sensitivity property is observable")
    void testDetectionSensitivityProperty() {
        assertNotNull(pestControl.detectionSensitivityProperty());
        assertEquals(pestControl.getDetectionSensitivity(), 
                pestControl.detectionSensitivityProperty().get());
    }

    @Test
    @DisplayName("Treatment threshold property is observable")
    void testTreatmentThresholdProperty() {
        assertNotNull(pestControl.treatmentThresholdProperty());
        assertEquals(pestControl.getTreatmentThreshold(), 
                pestControl.treatmentThresholdProperty().get());
    }

    // ==================== Zone Infestation Tests ====================

    @Test
    @DisplayName("Zone infestation level updates correctly")
    void testZoneInfestationLevelUpdate() {
        Zone zone = garden.getZone(1);
        assertEquals(0, zone.getPestInfestationLevel());
        
        zone.updatePestLevel(50);
        assertEquals(50, zone.getPestInfestationLevel());
    }

    @Test
    @DisplayName("Zone infestation level clamps to 0-100")
    void testZoneInfestationClamping() {
        Zone zone = garden.getZone(1);
        
        zone.updatePestLevel(-50);
        assertEquals(0, zone.getPestInfestationLevel());
        
        zone.updatePestLevel(150);
        assertEquals(100, zone.getPestInfestationLevel());
    }

    // ==================== Threat Level Tests ====================

    @Test
    @DisplayName("ThreatLevel enum has correct values")
    void testThreatLevelValues() {
        PestControlSystem.ThreatLevel[] levels = PestControlSystem.ThreatLevel.values();
        assertEquals(4, levels.length);
        
        assertEquals(PestControlSystem.ThreatLevel.LOW, levels[0]);
        assertEquals(PestControlSystem.ThreatLevel.MEDIUM, levels[1]);
        assertEquals(PestControlSystem.ThreatLevel.HIGH, levels[2]);
        assertEquals(PestControlSystem.ThreatLevel.CRITICAL, levels[3]);
    }

    // ==================== toString Tests ====================

    @Test
    @DisplayName("toString returns informative string")
    void testToString() {
        String result = pestControl.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("PestControlSystem"), "Should contain class name");
        assertTrue(result.contains("Pests"), "Should contain pests info");
        assertTrue(result.contains("Stock"), "Should contain stock info");
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Update method does not throw with empty garden")
    void testUpdateWithEmptyGarden() {
        Garden emptyGarden = new Garden(3, 3);
        PestControlSystem emptyPestControl = new PestControlSystem(emptyGarden);
        
        // Should not throw
        for (int i = 0; i < 10; i++) {
            emptyPestControl.update();
        }
    }

    @Test
    @DisplayName("Update method does not throw with plants")
    void testUpdateWithPlants() {
        // Should not throw
        for (int i = 0; i < 10; i++) {
            pestControl.update();
        }
    }

    @Test
    @DisplayName("System handles multiple zones correctly")
    void testMultipleZones() {
        // All 9 zones should exist
        for (int i = 1; i <= 9; i++) {
            Zone zone = garden.getZone(i);
            assertNotNull(zone, "Zone " + i + " should exist");
        }
    }

    @Test
    @DisplayName("Treatment works on zones with plants")
    void testTreatmentOnZoneWithPlants() {
        // Zone 1 should have plants (we added at position 0,0)
        Zone zone1 = garden.getZone(1);
        assertTrue(zone1.getLivingPlantCount() > 0, "Zone 1 should have plants");
        
        zone1.updatePestLevel(80);
        int initialStock = pestControl.getPesticideStock();
        
        pestControl.manualTreat(1);
        
        assertEquals(initialStock - 1, pestControl.getPesticideStock(),
                "Treatment should consume pesticide");
        assertTrue(zone1.getPestInfestationLevel() < 80,
                "Infestation level should decrease after treatment");
    }

    @Test
    @DisplayName("No treatment when pesticide stock is zero")
    void testNoTreatmentWithoutPesticide() {
        // Drain all pesticide by treating multiple times
        for (int i = 0; i < 60; i++) {
            Zone zone = garden.getZone(1);
            zone.updatePestLevel(100);
            pestControl.manualTreat(1);
        }
        
        assertEquals(0, pestControl.getPesticideStock());
        
        // Set high infestation
        Zone zone = garden.getZone(1);
        zone.updatePestLevel(100);
        
        // Try to treat - should have no effect since stock is 0
        pestControl.manualTreat(1);
        
        // Stock should still be 0 (no pesticide was used because none was available)
        assertEquals(0, pestControl.getPesticideStock());
    }
}

