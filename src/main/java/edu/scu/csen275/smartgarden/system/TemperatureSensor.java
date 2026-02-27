package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Zone;

/**
 * Sensor that measures temperature in a zone.
 */
public class TemperatureSensor extends Sensor {
    
    public TemperatureSensor(Zone zone) {
        super("TEMP-" + zone.getZoneId(), zone);
    }
    
    @Override
    public int readValue() {
        updateReadingTime();
        
        try {
            // Read temperature from zone
            return zone.getTemperature();
        } catch (Exception e) {
            status = SensorStatus.ERROR;
            return -999; // Error indicator
        }
    }
    
    @Override
    public String toString() {
        return "TemperatureSensor[" + sensorId + ", Zone " + zone.getZoneId() + 
               ", Status: " + status + "]";
    }
}

