package de.interoberlin.poisondartfrog.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.interoberlin.poisondartfrog.model.devices.Characteristic;
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

        if (getCharacteristics() != null && !getCharacteristics().isEmpty()) {
            BluetoothGattCharacteristic characteristic = getCharacteristics().get(lastReadCharacteristic);
            Characteristic c = PropertyMapper.getInstance().getCharacteristicById(characteristic.getUuid());

            if (c != null && c.getRead() != null) {
                switch (c.getRead()) {
                    case NEVER: {
                        int index = getLastReadCharacteristic() + 1;
                        int total = getCharacteristics().size();
                        Log.d(TAG, "Skip [" + ((index < 10) ? " " : "") + index + "/" + total + "]");

                        incrementLastReadCharacteristic();
                        readNextCharacteristic(service);
                        break;
                    }
                    case ONCE: {
                        if (characteristic.getValue() == null || characteristic.getValue().length == 0) {
                            readCharacteristicTask = new ReadCharacteristicTask(service);
                            readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));
                        } else {
                            int index = getLastReadCharacteristic() + 1;
                            int total = getCharacteristics().size();
                            Log.d(TAG, "Skip [" + ((index < 10) ? " " : "") + index + "/" + total + "]");

                            incrementLastReadCharacteristic();
                            readNextCharacteristic(service);
                        }
                        break;
                    }
                    case CYCLIC: {
                        readCharacteristicTask = new ReadCharacteristicTask(service);
                        readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));
                        break;
                    }
                }
            } else {
                readCharacteristicTask = new ReadCharacteristicTask(service);
                readCharacteristicTask.execute(getCharacteristics().get(lastReadCharacteristic));
            }
        }
    }

    public void incrementLastReadCharacteristic() {
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
                sb.append("  characteristic ").append(chara.getUuid()).append(getFormat(chara) != null ? "[" + getFormat(chara) + "]" : "").append((chara.getValue() != null) ? " / " + parseValue(chara) : "").append("\n");
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
        UUID id = characteristic.getUuid();

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
        UUID id = characteristic.getUuid();

        if (PropertyMapper.getInstance().isKnownCharacteristic(id) && PropertyMapper.getInstance().getCharacteristicById(id).getFormat() != null) {
            Characteristic.EFormat format = PropertyMapper.getInstance().getCharacteristicById(id).getFormat();
            String value = "";

            switch (format) {
                case STRING:
                    value = getStringValue(characteristic.getValue());
                    break;
                case UINT8:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                    break;
                case UINT16:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
                    break;
                case UINT32:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
                    break;
                case SINT8:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0));
                    break;
                case SINT16:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
                    break;
                case SINT32:
                    value = String.valueOf(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0));
                    break;
                case SFLOAT:
                    value = String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 0));
                    break;
                case FLOAT:
                    value = String.valueOf(characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0));
                    break;
            }

            return value;
        } else {

        /*
        Log.i(TAG, characteristic.getUuid().toString());
        Log.i(TAG, "INT UINT8 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        Log.i(TAG, "INT UINT16 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0));
        Log.i(TAG, "INT UINT32 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0));
        Log.i(TAG, "INT FORMAT_SINT8 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0));
        Log.i(TAG, "INT FORMAT_SINT16 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0));
        Log.i(TAG, "INT FORMAT_SINT32 " + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0));
        Log.i(TAG, "INT FORMAT_SFLOAT " + characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 0));
        Log.i(TAG, "INT FORMAT_FLOAT " + characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 0));
        Log.i(TAG, " ");
        */

            return new String(characteristic.getValue()).replaceAll(" ", "");
        }
    }

    /**
     * Turns a byte array into a corresponding ASCII string
     *
     * @param value byte array
     * @return ASCII string
     */
    private static String getStringValue(byte[] value) {
        StringBuilder sb = new StringBuilder();
        String v = new String(value).replaceAll(" ", "");

        try {
            for (int i = 0; i < v.length(); i += 2) {
                String s = v.substring(i, i + 2);
                sb.append((char) Integer.parseInt(s, 16));
            }
            return sb.toString();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return v;
        }
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
                Characteristic c = PropertyMapper.getInstance().getCharacteristicById(characteristic.getUuid());
                characteristics.add(characteristic);
            }
        }
    }

    public int getLastReadCharacteristic() {
        return lastReadCharacteristic;
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
