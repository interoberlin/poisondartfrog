package de.interoberlin.poisondartfrog.controller;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import io.relayr.android.ble.BleDevice;

public class DevicesController {
    public static final String TAG = DevicesController.class.getCanonicalName();

    private Activity activity;

    private List<BleDevice> scannedDevices;

    private static DevicesController instance;

    // --------------------
    // Constructors
    // --------------------

    private DevicesController(Activity activity) {
        this.activity = activity;
        this.scannedDevices = new ArrayList<>();
    }

    public static DevicesController getInstance(Activity activity) {
        if (instance == null) {
            instance = new DevicesController(activity);
        }

        return instance;
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public List<BleDevice> getScannedDevices() {
        return scannedDevices;
    }

    public void setScannedDevices(List<BleDevice> scannedDevices) {
        this.scannedDevices = scannedDevices;
    }
}
