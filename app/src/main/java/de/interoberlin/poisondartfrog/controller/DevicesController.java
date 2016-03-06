package de.interoberlin.poisondartfrog.controller;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interoberlin.poisondartfrog.model.ExtendedBluetoothDevice;

public class DevicesController {
    public static final String TAG = DevicesController.class.getSimpleName();

    private Map<String, BluetoothDevice> scannedDevices;
    private Map<String, ExtendedBluetoothDevice> attachedDevices;

    private static DevicesController instance;

    // --------------------
    // Constructors
    // --------------------

    private DevicesController() {
        this.scannedDevices = new HashMap<>();
        this.attachedDevices = new HashMap<>();
    }

    public static DevicesController getInstance() {
        if (instance == null) {
            instance = new DevicesController();
        }

        return instance;
    }

    // --------------------
    // Methods
    // --------------------

    /**
     * Attaches a device
     *
     * @param device device
     */
    public void attach(BluetoothDevice device) {
        Log.i(TAG, "Attach " + device.getName());
        if (scannedDevices.containsKey(device.getAddress()))
            scannedDevices.remove(device.getAddress());
        attachedDevices.put(device.getAddress(), new ExtendedBluetoothDevice(device));
    }

    /**
     * Detaches a device
     *
     * @param device device
     */
    public void detach(ExtendedBluetoothDevice device) {
        if (attachedDevices.containsKey(device.getAddress()))
            attachedDevices.remove(device.getAddress());
    }

    public ExtendedBluetoothDevice getAttachedDeviceByAdress(String address) {
        return getAttachedDevices().get(address);
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Map<String, BluetoothDevice> getScannedDevices() {
        return scannedDevices;
    }

    public List<BluetoothDevice> getScannedDevicesAsList() {
        return new ArrayList<>(getScannedDevices().values());
    }

    public Map<String, ExtendedBluetoothDevice> getAttachedDevices() {
        return attachedDevices;
    }

    public List<ExtendedBluetoothDevice> getAttachedDevicesAsList() {
        return new ArrayList<>(getAttachedDevices().values());
    }
}
