package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Zone;

/**
 * Zone-scoped sensor that reports the zone's current temperature.
 * On read failure, marks the sensor as ERROR and returns a sentinel value.
 */
public class TemperatureSensor extends Sensor {
    
    public TemperatureSensor(Zone zone) {
        super("TEMP-" + zone.getZoneId(), zone);
    }
    
    @Override
    public int readValue() {
        updateReadingTime();
        
        try {
            return zone.getTemperature();
        } catch (Exception e) {
            status = SensorStatus.ERROR;
            return -999; // Sensor read failure sentinel
        }
    }
    
    @Override
    public String toString() {
        return "TemperatureSensor[" + sensorId + ", Zone " + zone.getZoneId() + 
               ", Status: " + status + "]";
    }
}

