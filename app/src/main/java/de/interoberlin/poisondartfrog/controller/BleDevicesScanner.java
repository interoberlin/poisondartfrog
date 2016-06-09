package de.interoberlin.poisondartfrog.controller;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleDevicesScanner implements Runnable, BluetoothAdapter.LeScanCallback {
    private static final String TAG = BleDevicesScanner.class.getSimpleName();

    public static final long DEFAULT_SCAN_PERIOD = 5;//seconds

    private BluetoothAdapter bluetoothAdapter;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final LeScansPoster leScansPoster;

    private ScanSettings settings;
    private ScanCallback mScanCallback;
    private BluetoothLeScanner mLeScanner;
    private List<ScanFilter> filters = new ArrayList<>();

    private long scanPeriod = DEFAULT_SCAN_PERIOD;
    private Thread scanThread;
    private volatile boolean isScanning = false;

    // --------------------
    // Constructors
    // --------------------

    public BleDevicesScanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter = adapter;
        leScansPoster = new LeScansPoster(callback);

        if (Build.VERSION.SDK_INT >= 21) {
            mLeScanner = adapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            mScanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (result == null || result.getScanRecord() == null) return;
                    onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult result : results) {
                        if (result == null || result.getScanRecord() == null) return;
                        onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                }
            };
        }
    }

    // --------------------
    // Methods
    // --------------------

    public synchronized void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod < 0 ? DEFAULT_SCAN_PERIOD : scanPeriod;
    }

    public boolean isScanning() {
        return scanThread != null && scanThread.isAlive();
    }

    public synchronized void start() {
        if (isScanning()) {
            return;
        }

        if (scanThread != null) scanThread.interrupt();

        scanThread = new Thread(this);
        scanThread.setName(TAG);
        scanThread.start();
    }

    public synchronized void stop() {
        if (!isScanning()) return;

        isScanning = false;
        stopScan();

        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private synchronized void stopScan() {
        if (Build.VERSION.SDK_INT < 21 || mLeScanner == null) {
            if (bluetoothAdapter != null) bluetoothAdapter.stopLeScan(this);
        } else {
            mLeScanner.flushPendingScanResults(mScanCallback);
            mLeScanner.stopScan(mScanCallback);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void run() {
        try {
            isScanning = true;
            do {
                synchronized (this) {
                    if (Build.VERSION.SDK_INT < 21 || mLeScanner == null) {
                        bluetoothAdapter.startLeScan(this);
                    } else {
                        mLeScanner.startScan(filters, settings, mScanCallback);
                    }
                }

                Thread.sleep(scanPeriod * 1000);

                synchronized (this) {
                    stopScan();
                }
            } while (isScanning);
        } catch (InterruptedException ignore) {
        } finally {
            synchronized (this) {
                stopScan();
            }
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        synchronized (leScansPoster) {
            leScansPoster.set(device, rssi, scanRecord);
            mainThreadHandler.post(leScansPoster);
        }
    }

    private static class LeScansPoster implements Runnable {
        private final BluetoothAdapter.LeScanCallback leScanCallback;

        private BluetoothDevice device;
        private int rssi;
        private byte[] scanRecord;

        private LeScansPoster(BluetoothAdapter.LeScanCallback leScanCallback) {
            this.leScanCallback = leScanCallback;
        }

        public void set(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }

        public void run() {
            leScanCallback.onLeScan(device, rssi, scanRecord);
        }
    }
}