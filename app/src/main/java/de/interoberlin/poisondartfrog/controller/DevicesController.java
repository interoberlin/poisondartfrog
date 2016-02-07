package de.interoberlin.poisondartfrog.controller;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interoberlin.poisondartfrog.model.BleDeviceReading;
import de.interoberlin.poisondartfrog.model.EReadingType;
import io.relayr.android.ble.BleDevice;
import io.relayr.java.model.action.Reading;

public class DevicesController {
    public static final String TAG = DevicesController.class.getCanonicalName();

    private Activity activity;

    private Map<String, BleDevice> scannedDevices;
    private Map<String, BleDeviceReading> subscribedDevices;

    private static DevicesController instance;

    // --------------------
    // Constructors
    // --------------------

    private DevicesController(Activity activity) {
        this.activity = activity;
        this.scannedDevices = new HashMap<>();
        this.subscribedDevices = new HashMap<>();
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
     * Updates a {@code reading} value of a {@code device}
     *
     * @param address device address
     * @param reading reading
     */
    public void updateSubscribedDevice(String address, Reading reading) {
        BleDeviceReading bleDeviceReading = this.subscribedDevices.get(address);

        if (bleDeviceReading != null) {
            bleDeviceReading.getReadings().put(EReadingType.fromString(reading.meaning), reading.value.toString());
        }
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Map<String, BleDevice> getScannedDevices() {
        return scannedDevices;
    }

    public List<BleDevice> getScannedDevicesAsList() {
        return new ArrayList<>(getScannedDevices().values());
    }

    public void setScannedDevices(Map<String, BleDevice> scannedDevices) {
        this.scannedDevices = scannedDevices;
    }

    public Map<String, BleDeviceReading> getSubscribedDevices() {
        return subscribedDevices;
    }

    public List<BleDeviceReading> getSubscribedDevicesAsList() {
        return new ArrayList<>(getSubscribedDevices().values());
    }

    public void setSubscribedDevices(Map<String, BleDeviceReading> subscribedDevices) {
        this.subscribedDevices = subscribedDevices;
    }
}
