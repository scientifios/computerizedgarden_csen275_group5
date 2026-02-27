package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Zone;

/**
 * Sensor that measures soil moisture levels in a zone.
 */
public class MoistureSensor extends Sensor {
    
    public MoistureSensor(Zone zone) {
        super("MOISTURE-" + zone.getZoneId(), zone);
    }
    
    @Override
    public int readValue() {
        updateReadingTime();
        
        try {
            // Read moisture level from zone
            return zone.getMoistureLevel();
        } catch (Exception e) {
            status = SensorStatus.ERROR;
            return -1; // Error indicator
        }
    }
    
    @Override
    public String toString() {
        return "MoistureSensor[" + sensorId + ", Zone " + zone.getZoneId() + 
               ", Status: " + status + "]";
    }
}

