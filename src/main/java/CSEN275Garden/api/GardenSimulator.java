package CSEN275Garden.api;

import java.util.Map;

public class GardenSimulator {

    private static void sleepOneHour() {
    try {
        Thread.sleep(60000); // 1 hour = 3600000 ms
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

    public static void main(String[] args) {

        GardenSimulationAPI gardenAPI = new GardenSimulationAPI();

        // beginning of the simulation
        gardenAPI.initializeGarden(); // this marks the beginning of the clock
        Map<String, Object> initialPlantDetails = gardenAPI.getPlants();

        gardenAPI.rain(25);
        sleepOneHour(); // end of first day

        // beginning of the second day
        gardenAPI.temperature(90);
        gardenAPI.parasite("Green Leaf Worm");
        sleepOneHour(); // end of second day

        gardenAPI.rain(10);
        sleepOneHour();
        
        gardenAPI.parasite("Green Leaf Worm");
        sleepOneHour();
        // ... after 24 days

        gardenAPI.getState();
        gardenAPI.stopHeadlessSimulation();
    }
}
