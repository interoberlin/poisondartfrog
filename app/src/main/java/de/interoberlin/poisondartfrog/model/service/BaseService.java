package de.interoberlin.poisondartfrog.model.service;

import android.bluetooth.BluetoothGatt;

import de.interoberlin.poisondartfrog.model.BleDevice;
import rx.Observable;
import rx.functions.Func1;

/**
 * A class representing the basic characteristics of the BLE service a Device should have
 */
public class BaseService extends Service {

    protected final BleDevice device;

    protected BaseService(BleDevice device, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
        super(gatt, receiver);
        this.device = device;
    }

    public BleDevice getBleDevice() {
        return device;
    }

    /**
     * Disconnects and closes the gatt. It should not be called directly use
     * {@link BleDevice#disconnect()} instead.
     *
     * @return an observable of the device that was connected.
     */
    public Observable<BleDevice> disconnect() {
        return mBluetoothGattReceiver
                .disconnect(mBluetoothGatt)
                .map(new Func1<BluetoothGatt, BleDevice>() {
                    @Override
                    public BleDevice call(BluetoothGatt gatt) {
                        return device;
                    }
                });
    }

    /*
    public Observable<String> readCharacteristic(String serviceUuid,
                                                 String characteristicUuid,
                                                 final String what) {
        Observable<BluetoothGattCharacteristic> characteristic = super.readCharacteristic(serviceUuid, characteristicUuid, what);

            UUID id = characteristic.;

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
                        value = RelayrDataParser.getFormattedValue(EBluetoothDeviceType.fromString(device.getName()), characteristic.getValue());
                        value = value.replaceAll(",", ",\n");
                        break;
                }

                return new Observable.(new Observable.OnSubscribe<>(value));
            } else {
                return new String(characteristic.getValue()).replaceAll(" ", "");
            }
        }
    }
    */

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
