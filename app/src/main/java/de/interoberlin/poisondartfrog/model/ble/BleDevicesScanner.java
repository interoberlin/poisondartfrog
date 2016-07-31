package de.interoberlin.poisondartfrog.model.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleDevicesScanner implements Runnable, BluetoothAdapter.LeScanCallback {
    // <editor-fold defaultstate="expanded" desc="Interfaces">
    private static final String TAG = BleDevicesScanner.class.getSimpleName();

    // Constants
    public static int DEVICE_SCAN_PERIOD;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final LeScansPoster leScansPoster;

    private ScanSettings settings;
    private ScanCallback mScanCallback;
    private BluetoothLeScanner mLeScanner;
    private List<ScanFilter> filters = new ArrayList<>();

    private Thread scanThread;
    private volatile boolean isScanning = false;
    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Constructors">

    public BleDevicesScanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
        this.bluetoothAdapter = adapter;
        this.leScansPoster = new LeScansPoster(callback);

        Context context = App.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();

        DEVICE_SCAN_PERIOD = Integer.parseInt(prefs.getString(res.getString(R.string.pref_golem_temperature_send_period), "10"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    public boolean isScanning() {
        return scanThread != null && scanThread.isAlive();
    }

    public synchronized void start() {
        if (isScanning()) {
            return;
        }

        if (scanThread != null) scanThread.interrupt();

        if (isBluetoothEnabled()) {
            scanThread = new Thread(this);
            scanThread.setName(TAG);
            scanThread.start();
        }
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
        if (isBluetoothEnabled()) {
            if (Build.VERSION.SDK_INT < 21 || mLeScanner == null) {
                bluetoothAdapter.cancelDiscovery();
            } else {
                mLeScanner.stopScan(mScanCallback);
                mLeScanner.flushPendingScanResults(mScanCallback);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void run() {
        try {
            isScanning = true;
            do {
                synchronized (this) {
                    if (isBluetoothEnabled()) {
                        if (Build.VERSION.SDK_INT < 21 || mLeScanner == null) {
                            bluetoothAdapter.startDiscovery();
                        } else {
                            mLeScanner.startScan(filters, settings, mScanCallback);
                        }
                    }
                }

                Thread.sleep(DEVICE_SCAN_PERIOD * 1000);

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

    private boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
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

    // </editor-fold>
}