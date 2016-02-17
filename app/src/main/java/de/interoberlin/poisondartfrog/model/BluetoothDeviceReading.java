package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a tuple of a device and a set of readings where the latest readings are stored per reading type
 */
public class BluetoothDeviceReading {
    private BluetoothDevice device;
    private Map<UUID, BluetoothGattCharacteristic> gattCharacteristics;

    private boolean scanning;

    // --------------------
    // Constructors
    // --------------------

    public BluetoothDeviceReading(BluetoothDevice device) {
        this.device = device;
        this.gattCharacteristics = new HashMap<>();
        this.scanning = false;
    }

    // --------------------
    // Methods
    // --------------------

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
}
