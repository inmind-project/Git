package com.yahoo.inmind.comm.accelerometer.model;

import com.yahoo.inmind.comm.generic.model.BaseEvent;

/**
 * Created by oscarr on 8/13/15.
 */
public class AccelerometerEvent extends BaseEvent {
    /**
     * From rowData you can extract:
     * "device_id"
     * "timestamp"
     * "double_sensor_maximum_range"
     * "double_sensor_minimum_delay"
     * "sensor_name"
     * "double_sensor_power_ma"
     * "double_sensor_resolution"
     * "sensor_type"
     * "sensor_vendor"
     * "sensor_version"
     */
    private float accelerationX;
    private float accelerationY;
    private float accelerationZ;
    private int accuracy;
    private long timestamp;

    private AccelerometerEvent(){ super(); }
    public static AccelerometerEvent build(){
        return new AccelerometerEvent();
    }

    public float getAccelerationX() {
        return accelerationX;
    }

    public AccelerometerEvent setAccelerationX(float accelerationX) {
        this.accelerationX = accelerationX;
        return this;
    }

    public float getAccelerationY() {
        return accelerationY;
    }

    public AccelerometerEvent setAccelerationY(float accelerationY) {
        this.accelerationY = accelerationY;
        return this;
    }

    public float getAccelerationZ() {
        return accelerationZ;
    }

    public AccelerometerEvent setAccelerationZ(float accelerationZ) {
        this.accelerationZ = accelerationZ;
        return this;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public AccelerometerEvent setAccuracy(int accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public AccelerometerEvent setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
