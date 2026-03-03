package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Zone;
import java.time.LocalDateTime;

/**
 * Base sensor model bound to a zone.
 * Tracks status and last-read timestamp; subclasses provide the measurement value.
 */
public abstract class Sensor {
    protected final String sensorId;
    protected final Zone zone;
    protected LocalDateTime lastReading;
    protected SensorStatus status;
    
    /**
     * Initializes a zone-scoped sensor with an id and default ACTIVE status.
     */
    protected Sensor(String sensorId, Zone zone) {
        this.sensorId = sensorId;
        this.zone = zone;
        this.lastReading = LocalDateTime.now();
        this.status = SensorStatus.ACTIVE;
    }
    
    /**
     * Returns the current reading for this sensor.
     */
    public abstract int readValue();
    
    /**
     * Resets the sensor to ACTIVE and refreshes the last-read timestamp.
     */
    public void calibrate() {
        status = SensorStatus.ACTIVE;
        lastReading = LocalDateTime.now();
    }
    
    /**
     * @return current sensor status
     */
    public SensorStatus reportStatus() {
        return status;
    }
    
    /**
     * Updates the last-read timestamp to now.
     */
    protected void updateReadingTime() {
        lastReading = LocalDateTime.now();
    }
    
    public String getSensorId() {
        return sensorId;
    }
    
    public Zone getZone() {
        return zone;
    }
    
    public LocalDateTime getLastReading() {
        return lastReading;
    }
    
    public SensorStatus getStatus() {
        return status;
    }
    
    public enum SensorStatus {
        ACTIVE,
        INACTIVE,
        ERROR
    }
}

