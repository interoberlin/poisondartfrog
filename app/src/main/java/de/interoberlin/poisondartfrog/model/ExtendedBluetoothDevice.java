package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.model.tasks.ReadCharacteristicTask;

/**
 * Represents a tuple of a device and a set of characteristics
 */
public class ExtendedBluetoothDevice {
    public static final String TAG = ExtendedBluetoothDevice.class.getSimpleName();

    private BluetoothDevice device;
    private List<BluetoothGattService> gattServices;
    private List<BluetoothGattCharacteristic> characteristics;

    private ReadCharacteristicTask readCharacteristicTask;
    private int lastReadCharacteristic;

    private boolean reading;
    private boolean connected;

    // --------------------
    // Constructors
    // --------------------

    public ExtendedBluetoothDevice(BluetoothDevice device) {
        this.device = device;
        this.gattServices = new ArrayList<>();
        this.reading = false;
        this.connected = false;
    }

    // --------------------
    // Methods
    // --------------------

    public void readNextCharacteristic(BluetoothLeService service) {
        Log.d(TAG, lastReadCharacteristic+1 + "/" + lastReadCharacteristic);

        reading = true;

        readCharacteristicTask = new ReadCharacteristicTask(service);
        readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));

        lastReadCharacteristic++;
        if (lastReadCharacteristic >= getCharacteristics().size())
            lastReadCharacteristic = 0;
    }

    public void stopReading() {
        reading = false;

        readCharacteristicTask.cancel(true);
    }

    public String getAddress() {
        return (device != null) ? device.getAddress() : null;
    }

    public String getName() {
        return (device != null) ? device.getName() : null;
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        return characteristics;
    }

    /**
     * Updates the {@code value} of a characteristic defined by a given {@coe id}
     *
     * @param id    characteristic id
     * @param value new value
     */
    public void updateCharacteristicValue(String id, String value) {
        for (BluetoothGattService service : getGattServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.equals(id)) {
                    characteristic.setValue(value);
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=" + this.getName() + ", \n");
        sb.append("type=" + this.getDevice().getType() + "(" + getDeviceType(this.getDevice().getType()) + "), \n");
        sb.append("address=" + this.getAddress() + ", \n");
        sb.append("services=\n");

        for (BluetoothGattService service : getGattServices()) {
            sb.append("  service " + service.getUuid() + "\n");
            for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                sb.append("  characteristic " + chara.getUuid() + ((chara.getValue() != null) ? " / " + parseValue(chara.getValue()) : "") + "\n");
            }
        }
        return sb.toString();
    }

    public static String parseValue(byte[] values) {
        return new String(values).replaceAll(" ", "");
    }

    /**
     * Retrieves the device type string by its integer
     *
     * @param type type integer
     * @return type string
     */
    private String getDeviceType(int type) {
        switch (type) {
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                return "unknown";
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                return "classic";
            case BluetoothDevice.DEVICE_TYPE_LE:
                return "le";
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                return "dual";
        }

        return "unknown";
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
        this.characteristics = new ArrayList<>();

        for (BluetoothGattService service : gattServices) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                characteristics.add(characteristic);
            }
        }
    }

    public boolean isReading() {
        return reading;
    }

    public void setReading(boolean reading) {
        this.reading = reading;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
