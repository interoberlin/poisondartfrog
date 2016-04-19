package de.interoberlin.poisondartfrog.model.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public abstract class Utils {

    public static BluetoothGattService getServiceForUuid(List<BluetoothGattService> services,
                                                  String shortUuid) {
        for (BluetoothGattService service: services) {
            String serviceUuid = BleUtils.getShortUUID(service.getUuid());
            if (shortUuid.equals(serviceUuid)) return service;
        }
        return null;
    }

    public static BluetoothGattCharacteristic getCharacteristicForUuid(
            List<BluetoothGattCharacteristic> characteristics,
            String uuid) {
        for (BluetoothGattCharacteristic characteristic: characteristics) {
            String shortUuid = BleUtils.getShortUUID(characteristic.getUuid());
            String longUuid = BleUtils.getLongUUID(characteristic.getUuid());
            if (uuid.equals(shortUuid) || uuid.equals(longUuid)) return characteristic;
        }
        return null;
    }

    public static BluetoothGattCharacteristic getCharacteristicInServices(
            List<BluetoothGattService> services,
            String serviceUuid,
            String characteristicUuid) {
        BluetoothGattService service = getServiceForUuid(services, serviceUuid);
        if (service == null) return null;

        return getCharacteristicForUuid(service.getCharacteristics(), characteristicUuid);
    }

    public static BluetoothGattDescriptor getDescriptorInCharacteristic(
            BluetoothGattCharacteristic characteristic,
            String shortUuid) {
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor: descriptors) {
            String descriptorUuid = BleUtils.getShortUUID(descriptor.getUuid());
            if (shortUuid.equals(descriptorUuid)) return  descriptor;
        }
        return null;
    }

    public static String getCharacteristicInServicesAsString(List<BluetoothGattService> services,
                                                      String serviceUuid,
                                                      String characteristicUuid) {

        BluetoothGattCharacteristic characteristic = getCharacteristicInServices(
                services, serviceUuid, characteristicUuid);
        if (characteristic == null) return "";
        return characteristic.getStringValue(0);
    }
}
