package de.interoberlin.poisondartfrog.model.parser;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import de.interoberlin.poisondartfrog.model.BleDevice;

public class ValueParser {

    /**
     * Retrieves a value of a given {@code characteristic}
     *
     * @param characteristic characteristic
     * @param device         device
     * @return value string
     */
    public static String parseValue(BleDevice device, BluetoothGattCharacteristic characteristic) {
        UUID id = characteristic.getUuid();
/*
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
                case RELAYR:
                    value = RelayrDataParser.getFormattedValue(EDevice.fromString(device.getName()), characteristic.getValue());
                    value = value.replaceAll(",", ",\n");
                    break;
            }



            return value;
        } else {
            return new String(characteristic.getValue()).replaceAll(" ", "");
        }
        */
        return null;
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
}
