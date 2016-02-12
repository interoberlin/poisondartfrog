package de.interoberlin.poisondartfrog.model;

import java.util.HashMap;
import java.util.Map;

import io.relayr.android.ble.BleDevice;
import io.relayr.java.ble.BleDeviceType;

/**
 * Represents a tuple of a device and a set of readings where the latest readings are stored per reading type
 */
public class BleDeviceReading {

    new BleDeviceType;

    private BleDevice device;
    private Map<EReadingType, String> readings;

    // --------------------
    // Constructors
    // --------------------

    public BleDeviceReading(BleDevice device) {
        this.device = device;
        this.readings = new HashMap<>();
    }

    // --------------------
    // Methods
    // --------------------

    public BleDevice getDevice() {
        return device;
    }

    public void setDevice(BleDevice device) {
        this.device = device;
    }

    public Map<EReadingType, String> getReadings() {
        return readings;
    }

    public void setReadings(Map<EReadingType, String> readings) {
        this.readings = readings;
    }
}
