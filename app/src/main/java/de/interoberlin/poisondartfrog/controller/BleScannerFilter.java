package de.interoberlin.poisondartfrog.controller;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleScannerFilter implements BluetoothAdapter.LeScanCallback {
    // <editor-fold defaultstate="expanded" desc="Members">

    private final BleDeviceManager deviceManager;
    private final BleFilteredScanCallback callback;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Constructors">

    BleScannerFilter(BleDeviceManager deviceManager, BleFilteredScanCallback callback) {
        this.deviceManager = deviceManager;
        this.callback = callback;
    }

    // </editor-fold>

    // --------------------
    // Methods - Callbacks
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Callbacks">

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        BleDevice bleDevice = new BleDevice(device, deviceManager);

        // TODO do filter stuff

        if (callback != null)
            callback.onLeScan(bleDevice, rssi);
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    public interface BleFilteredScanCallback {
        void onLeScan(BleDevice device, int rssi);
    }

    // </editor-fold>
}
