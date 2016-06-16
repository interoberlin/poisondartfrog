package de.interoberlin.poisondartfrog.controller;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleScannerFilter implements BluetoothAdapter.LeScanCallback {

    private final BleDeviceManager deviceManager;
    private final BleFilteredScanCallback callback;

    BleScannerFilter(BleDeviceManager deviceManager, BleFilteredScanCallback callback) {
        this.deviceManager = deviceManager;
        this.callback = callback;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        BleDevice bleDevice = new BleDevice(null, device, null, deviceManager);

        // TODO do filter stuff

        if (callback != null)
            callback.onLeScan(bleDevice, rssi);
    }

    public interface BleFilteredScanCallback {
        void onLeScan(BleDevice device, int rssi);
    }
}
