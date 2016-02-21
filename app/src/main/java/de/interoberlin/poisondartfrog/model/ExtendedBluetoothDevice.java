package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a tuple of a device and a set of characteristics
 */
public class ExtendedBluetoothDevice {
    public static final String TAG = ExtendedBluetoothDevice.class.getSimpleName();

    private BluetoothDevice device;
    private Map<UUID, BluetoothGattCharacteristic> gattCharacteristics;
    private boolean scanning;
    private boolean connected;

    // --------------------
    // Constructors
    // --------------------

    public ExtendedBluetoothDevice(BluetoothDevice device) {
        this.device = device;
        this.gattCharacteristics = new HashMap<>();
        this.scanning = false;
        this.connected = false;
    }

    // --------------------
    // Methods
    // --------------------

    public String getAddress() {
        return (device != null) ? device.getAddress() : null;
    }

    public String getName() {
        return (device != null) ? device.getName() : null;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public Map<UUID, BluetoothGattCharacteristic> getGattCharacteristics() {
        return gattCharacteristics;
    }

    public void setGattCharacteristics(Map<UUID, BluetoothGattCharacteristic> gattCharacteristics) {
        this.gattCharacteristics = gattCharacteristics;
    }

    public boolean isScanning() {
        return scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
