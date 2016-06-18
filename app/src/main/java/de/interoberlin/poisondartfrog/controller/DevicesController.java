package de.interoberlin.poisondartfrog.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

public class DevicesController {
    public static final String TAG = DevicesController.class.getSimpleName();

    private Map<String, BleDevice> scannedDevices;
    private Map<String, BleDevice> attachedDevices;

    private BleDeviceManager bluetoothDeviceManager;
    private BleDevicesScanner bleDeviceScanner;

    private boolean startedScanning = false;

    private static DevicesController instance;

    // --------------------
    // Constructors
    // --------------------

    private DevicesController() {
        this.scannedDevices = new HashMap<>();
        this.attachedDevices = new HashMap<>();
    }

    public static DevicesController getInstance() {
        if (instance == null) {
            instance = new DevicesController();
        }

        return instance;
    }

    // --------------------
    // Methods
    // --------------------

    public void startScan(BleScannerFilter.BleFilteredScanCallback callback) {
        startScan(callback, -1);
    }

    public void startScan(BleScannerFilter.BleFilteredScanCallback callback, final long scanPeriod) {
        Log.d(TAG, "Start scan");

        scan(callback, scanPeriod)
                .filter(new Func1<List<BleDevice>, Boolean>() {
                    @Override
                    public Boolean call(List<BleDevice> bleDevices) {
                        startedScanning = false;
                        return false;
                    }
                })
                .map(new Func1<List<BleDevice>, BleDevice>() {
                    @Override
                    public BleDevice call(List<BleDevice> bleDevices) {
                        for (BleDevice device : bleDevices) {
                            return device;
                        }

                        startedScanning = false;
                        return null; // will never happen since it's been filtered out before
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BleDevice>() {
                    @Override
                    public void onCompleted() {
                        startedScanning = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        startedScanning = false;
                    }

                    @Override
                    public void onNext(BleDevice device) {
                    }
                });
    }

    public void stopScan() {
        Log.d(TAG, "Stop scan");

        if (bleDeviceScanner != null)
            bleDeviceScanner.stop();
    }

    private Observable<List<BleDevice>> scan(BleScannerFilter.BleFilteredScanCallback callback, final long scanPeriod) {
        BluetoothManager bluetoothManager = (BluetoothManager) App.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
            return null;
        }

        for (BluetoothDevice d : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER)) {
            Log.i(TAG, "Already connected " + d.getName());
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return null;
        }

        this.bluetoothDeviceManager = BleDeviceManager.getInstance();
        BleScannerFilter scannerFilter = new BleScannerFilter(bluetoothDeviceManager, callback);
        this.bleDeviceScanner = new BleDevicesScanner(bluetoothAdapter, scannerFilter);

        final long key = System.currentTimeMillis();
        return Observable.create(new Observable.OnSubscribe<List<BleDevice>>() {
            @Override
            public void call(Subscriber<? super List<BleDevice>> subscriber) {
                bluetoothDeviceManager.addSubscriber(key, subscriber);
                bleDeviceScanner.setScanPeriod(scanPeriod);
                bleDeviceScanner.start();
            }
        }).map(new Func1<List<BleDevice>, List<BleDevice>>() {
            @Override
            public List<BleDevice> call(List<BleDevice> bleDevices) {
                List<BleDevice> devices = new ArrayList<>();
                for (BleDevice device : bleDevices)
                    devices.add(device);
                return devices;
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                bluetoothDeviceManager.removeSubscriber(key);
                if (!bluetoothDeviceManager.isThereAnySubscriber()) bleDeviceScanner.stop();
            }
        });
    }

    /**
     * Attaches a device
     *
     * @param service BLE service
     * @param device  device
     * @return true, if it worked
     */
    public boolean attach(BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Attach " + device.getName());

        if (service != null) {
            service.connect(device.getAddress());
            if (scannedDevices.containsKey(device.getAddress()))
                scannedDevices.remove(device.getAddress());
            attachedDevices.put(device.getAddress(), device);

            return true;
        }

        return false;
    }

    /**
     * Detaches a device
     *
     * @param service BLE service
     * @param device  device
     * @return true, if it worked
     */
    public boolean detach(BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Detach" + device.getName());

        if (service != null) {
            device.disconnect();
            device.close();
            service.disconnect();
            service.close();

            if (attachedDevices.containsKey(device.getAddress()))
                attachedDevices.remove(device.getAddress());

            return true;
        }

        return false;
    }

    public boolean disconnect(BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Disconnect " + device.getName());

        if (service != null) {
            device.disconnect();
            service.disconnect();
            return true;
        }

        return false;
    }

    public void refreshCache(String deviceAddress){
        refreshCache(getAttachedDeviceByAdress(deviceAddress));
    }

    public void refreshCache(BleDevice device){
        device.refreshCache();
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Map<String, BleDevice> getScannedDevices() {
        return scannedDevices;
    }

    public List<BleDevice> getScannedDevicesAsList() {
        return new ArrayList<>(getScannedDevices().values());
    }

    public Map<String, BleDevice> getAttachedDevices() {
        return attachedDevices;
    }

    public BleDevice getAttachedDeviceByAdress(String address) {
        return getAttachedDevices().get(address);
    }

    public List<BleDevice> getAttachedDevicesAsList() {
        return new ArrayList<>(getAttachedDevices().values());
    }
}
