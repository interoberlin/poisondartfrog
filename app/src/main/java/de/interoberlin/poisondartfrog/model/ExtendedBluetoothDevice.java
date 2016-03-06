package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.model.devices.PropertyMapper;
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
        setReading(true);

        Log.i(TAG, lastReadCharacteristic + 1 + "/" + getCharacteristics().size());

        readCharacteristicTask = new ReadCharacteristicTask(service);
        readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));

        lastReadCharacteristic++;
        if (lastReadCharacteristic >= getCharacteristics().size())
            lastReadCharacteristic = 0;
    }

    public void stopReading() {
        setReading(false);

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
     * Updates the {@code value} of a characteristic defined by a given {@code id}
     *
     * @param id    characteristic id
     * @param value new value
     */
    public void updateCharacteristicValue(String id, String value) {
        for (BluetoothGattService service : getGattServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if (characteristic.getUuid().toString().equals(id)) {
                    characteristic.setValue(value);
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(this.getName()).append(", \n");
        sb.append("type=").append(this.getDevice().getType()).append("(").append(getDeviceType(this.getDevice().getType())).append("), \n");
        sb.append("address=").append(this.getAddress()).append(", \n");
        sb.append("services=\n");

        for (BluetoothGattService service : getGattServices()) {
            sb.append("  service ").append(service.getUuid()).append("\n");
            for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                sb.append("  characteristic " + chara.getUuid() + (getFormat(chara) != null ? "[" + getFormat(chara) + "]" : "") + ((chara.getValue() != null) ? " / " + parseValue(chara) : "") + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * Determines the value format if the given {@code characteristic} is known
     *
     * @param characteristic characteristic
     * @return value format string
     */
    public static String getFormat(BluetoothGattCharacteristic characteristic) {
        String id = characteristic.getUuid().toString();

        if (PropertyMapper.getInstance().isKnownCharacteristic(id) && PropertyMapper.getInstance().getCharacteristicById(id).getFormat() != null)
            return PropertyMapper.getInstance().getCharacteristicById(id).getFormat().toString();
        else
            return null;

    }

    /**
     * Retrieves a value of a given {@code characteristic}
     *
     * @param characteristic characteristic
     * @return value string
     */
    public static String parseValue(BluetoothGattCharacteristic characteristic) {
        String id = characteristic.getUuid().toString();

        if (PropertyMapper.getInstance().isKnownCharacteristic(id) && PropertyMapper.getInstance().getCharacteristicById(id).getFormat() != null) {
            switch (PropertyMapper.getInstance().getCharacteristicById(id).getFormat()) {
                case ASCII:
                    return characteristic.getStringValue(0);
                case FORMAT_UINT8:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                case FORMAT_UINT16:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
                case FORMAT_UINT32:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
                case FORMAT_SINT8:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0));
                case FORMAT_SINT16:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                case FORMAT_SINT32:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0));
                case FORMAT_SFLOAT:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 0));
                case FORMAT_FLOAT:
                    return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0));
            }
        }

        /*
        Log.i(TAG, "INT FORMAT_UINT8 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        Log.i(TAG, "INT FORMAT_UINT16 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
        Log.i(TAG, "INT FORMAT_UINT32 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
        Log.i(TAG, "INT FORMAT_SINT8 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0));
        Log.i(TAG, "INT FORMAT_SINT16 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
        Log.i(TAG, "INT FORMAT_SINT32 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0));
        Log.i(TAG, "INT FORMAT_SFLOAT " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 0));
        Log.i(TAG, "INT FORMAT_FLOAT " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0));
        Log.i(TAG, " ");
        */

        return String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0));
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
                Log.i(TAG, "Appended chara #" + (characteristics.size()+1) + " " + characteristic.getUuid());
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
