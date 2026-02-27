package edu.scu.csen275.smartgarden.system;

import edu.scu.csen275.smartgarden.model.Zone;
import java.time.LocalDateTime;

/**
 * Abstract base class for all sensors in the garden.
 */
public abstract class Sensor {
    protected final String sensorId;
    protected final Zone zone;
    protected LocalDateTime lastReading;
    protected SensorStatus status;
    
    /**
     * Creates a new Sensor.
     */
    protected Sensor(String sensorId, Zone zone) {
        this.sensorId = sensorId;
        this.zone = zone;
        this.lastReading = LocalDateTime.now();
        this.status = SensorStatus.ACTIVE;
    }
    
    /**
     * Reads the current sensor value.
     * Subclasses implement specific measurement logic.
     */
    public abstract int readValue();
    
    /**
     * Calibrates the sensor.
     */
    public void calibrate() {
        status = SensorStatus.ACTIVE;
        lastReading = LocalDateTime.now();
    }
    
    /**
     * Reports the current sensor status.
     */
    public SensorStatus reportStatus() {
        return status;
    }
    
    /**
     * Updates last reading timestamp.
     */
    protected void updateReadingTime() {
        lastReading = LocalDateTime.now();
    }
    
    // Getters
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
    
    /**
     * Sensor status enumeration.
     */
    public enum SensorStatus {
        ACTIVE,
        INACTIVE,
        ERROR
    }
}

