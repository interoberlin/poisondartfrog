package de.interoberlin.poisondartfrog.controller;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.interoberlin.poisondartfrog.model.ScanTask;
import io.relayr.android.ble.BleDevice;
import io.relayr.java.ble.BleDeviceType;

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
    // Methods
    // --------------------

    /**
     * Scans for BLE scannedDevices
     * @param ocListener callback interface
     */
    public void scan(ScanTask.OnCompleteListener ocListener) {
        try {
            new ScanTask(ocListener).execute(BleDeviceType.WunderbarLIGHT, BleDeviceType.WunderbarGYRO, BleDeviceType.WunderbarHTU, BleDeviceType.WunderbarMIC).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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
