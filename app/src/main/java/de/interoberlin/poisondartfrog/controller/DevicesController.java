package de.interoberlin.poisondartfrog.controller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interoberlin.poisondartfrog.model.BluetoothDeviceReading;

public class DevicesController {
    public static final String TAG = DevicesController.class.getCanonicalName();

    private Activity activity;

    private Map<String, BluetoothDevice> scannedDevices;
    private Map<String, BluetoothDeviceReading> attachedDevices;

    private static DevicesController instance;

    // --------------------
    // Constructors
    // --------------------

    private DevicesController(Activity activity) {
        this.activity = activity;
        this.scannedDevices = new HashMap<>();
        this.attachedDevices = new HashMap<>();
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
     * Attaches a device
     *
     * @param device device
     */
    public void attach(BluetoothDevice device) {
        if (scannedDevices.containsKey(device.getAddress()))
            scannedDevices.remove(device.getAddress());
        attachedDevices.put(device.getAddress(), new BluetoothDeviceReading(device));
    }

    /**
     * Detaches a device
     *
     * @param device device
     */
    public void detach(BluetoothDevice device) {
        if (attachedDevices.containsKey(device.getAddress()))
            attachedDevices.remove(device.getAddress());
    }

    /**
     * Updates a {@code characteristic} value of a device with a given {@code address}
     *
     * @param address        device address
     * @param characteristic characteristic
     */
    public void updateSubscribedDevice(String address, BluetoothGattCharacteristic characteristic) {
        BluetoothDeviceReading bluetoothDeviceReading = this.attachedDevices.get(address);

        if (bluetoothDeviceReading != null) {
            bluetoothDeviceReading.getGattCharacteristics().put(characteristic.getUuid(), characteristic);
        }
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

    public Map<String, BluetoothDeviceReading> getAttachedDevices() {
        return attachedDevices;
    }

    public List<BluetoothDeviceReading> getAttachedDevicesAsList() {
        return new ArrayList<>(getAttachedDevices().values());
    }
}
