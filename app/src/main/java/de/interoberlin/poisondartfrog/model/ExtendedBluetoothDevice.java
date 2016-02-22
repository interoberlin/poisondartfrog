package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a tuple of a device and a set of characteristics
 */
public class ExtendedBluetoothDevice {
    public static final String TAG = ExtendedBluetoothDevice.class.getSimpleName();

    private BluetoothDevice device;
    private List<BluetoothGattService> gattServices;
    private boolean scanning;
    private boolean connected;

    // --------------------
    // Constructors
    // --------------------

    public ExtendedBluetoothDevice(BluetoothDevice device) {
        this.device = device;
        this.gattServices = new ArrayList<>();
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=" + this.getName() + ", \n");
        sb.append("address=" + this.getAddress() + ", \n");
        sb.append("services=\n");

        for (BluetoothGattService service : getGattServices()) {
            sb.append("  service " + service.getUuid() + "\n");
            for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                sb.append("  characteristic " + chara.getUuid() + " / " + chara.getValue() + "\n");
            }
        }
        return sb.toString();
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public List<BluetoothGattService> getGattServices() {
        return gattServices;
    }

    public void setGattServices(List<BluetoothGattService> gattServices) {
        this.gattServices = gattServices;
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
