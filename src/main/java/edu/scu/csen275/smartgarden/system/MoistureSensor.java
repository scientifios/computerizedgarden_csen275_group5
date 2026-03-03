package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Zone;

/**
 * Zone-scoped sensor that exposes the zone's current moisture level.
 */
public class MoistureSensor extends Sensor {
    
    public MoistureSensor(Zone zone) {
        super("MOISTURE-" + zone.getZoneId(), zone);
    }
    
    @Override
    public int readValue() {
        updateReadingTime();
        
        try {
            return zone.getMoistureLevel();
        } catch (Exception e) {
            status = SensorStatus.ERROR;
            return -1; // Sentinel value for a failed sensor read
        }
    }
    
    @Override
    public String toString() {
        return "MoistureSensor[" + sensorId + ", Zone " + zone.getZoneId() + 
               ", Status: " + status + "]";
    }
}

