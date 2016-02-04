package de.interoberlin.poisondartfrog.model;

import java.util.HashMap;
import java.util.Map;

import io.relayr.android.ble.BleDevice;

public class BleDeviceReading  {
    private BleDevice device;
    private Map<EMeaning, String> readings;

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

    public Map<EMeaning, String> getReadings() {
        return readings;
    }

    public void setReadings(Map<EMeaning, String> readings) {
        this.readings = readings;
    }
}
