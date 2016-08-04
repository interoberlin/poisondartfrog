package de.interoberlin.poisondartfrog.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.interoberlin.poisondartfrog.App;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.ble.BleDevice;
import de.interoberlin.poisondartfrog.model.ble.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.ble.BleDevicesScanner;
import de.interoberlin.poisondartfrog.model.ble.BleScannerFilter;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;
import de.interoberlin.poisondartfrog.model.tasks.EHttpParameter;
import de.interoberlin.poisondartfrog.model.tasks.HttpGetTask;
import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

public class DevicesController {
    // <editor-fold defaultstate="expanded" desc="Members">

    public static final String TAG = DevicesController.class.getSimpleName();

    // Model
    private Map<String, BleDevice> scannedDevices;
    private Map<String, BleDevice> attachedDevices;
    private BleDeviceManager bluetoothDeviceManager;
    private BleDevicesScanner bleDeviceScanner;

    // Controller
    private MappingController mappingController;

    private static DevicesController instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Constructors">

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

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Methods">

    /**
     * Starts a scan for BLE devices
     *
     * @param callback callback
     */
    public void startScan(BleScannerFilter.BleFilteredScanCallback callback) {
        Log.d(TAG, "Start scan");

        BluetoothManager bluetoothManager = (BluetoothManager) App.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

       if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        } else {
            scan(bluetoothManager, bluetoothAdapter, callback)
                    .filter(new Func1<List<BleDevice>, Boolean>() {
                        @Override
                        public Boolean call(List<BleDevice> bleDevices) {
                            return false;
                        }
                    })
                    .map(new Func1<List<BleDevice>, BleDevice>() {
                        @Override
                        public BleDevice call(List<BleDevice> bleDevices) {
                            for (BleDevice device : bleDevices) {
                                return device;
                            }

                            return null; // will never happen since it's been filtered out before
                        }
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<BleDevice>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(BleDevice device) {
                        }
                    });
        }
    }

    /**
     * Stops a scan for BLE devices
     */
    public void stopScan() {
        Log.d(TAG, "Stop scan");

        if (bleDeviceScanner != null && bleDeviceScanner.isScanning())
            bleDeviceScanner.stop();
    }

    /**
     * Performs a scan for BLE devices
     *
     * @param callback callback
     * @param bluetoothManager bluetooth manager
     * @param bluetoothAdapter bluetooth adapter
     */
    private Observable<List<BleDevice>> scan(BluetoothManager bluetoothManager, BluetoothAdapter bluetoothAdapter, BleScannerFilter.BleFilteredScanCallback callback) {
        for (BluetoothDevice d : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER)) {
            Log.i(TAG, "Already connected " + d.getName());
        }

        this.bluetoothDeviceManager = BleDeviceManager.getInstance();
        BleScannerFilter scannerFilter = new BleScannerFilter(bluetoothDeviceManager, callback);
        this.bleDeviceScanner = new BleDevicesScanner(bluetoothAdapter, scannerFilter);

        final long key = System.currentTimeMillis();
        return Observable.create(new Observable.OnSubscribe<List<BleDevice>>() {
            @Override
            public void call(Subscriber<? super List<BleDevice>> subscriber) {
                bluetoothDeviceManager.addSubscriber(key, subscriber);
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
     * @param ocListener on change listener
     * @param service    BLE service
     * @param device     device
     * @return true, if it worked
     */
    public boolean attach(BleDevice.OnChangeListener ocListener, BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Attach " + device.getName());

        if (service != null) {
            service.connect(device.getAddress());
            if (scannedDevices.containsKey(device.getAddress()))
                scannedDevices.remove(device.getAddress());
            attachedDevices.put(device.getAddress(), device);

            device.registerOnChangeListener(ocListener);

            MappingController.getInstance().checkSourceAndSink();

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

            MappingController.getInstance().checkSourceAndSink();

            return true;
        }

        return false;
    }

    /**
     * Disconnects from a {@code device}
     *
     * @param service service
     * @param device  device
     * @return true if it worked
     */
    public boolean disconnect(BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Disconnect " + device.getName());

        if (service != null) {
            device.disconnect();
            service.disconnect();
            return true;
        }

        return false;
    }

    /**
     * Toggles a auto-connect attribute of a device identified by a given {@code deviceAddress}
     *
     * @param deviceAddress device address
     */
    public void refreshCache(String deviceAddress) {
        refreshCache(getAttachedDeviceByAddress(deviceAddress));
    }

    /**
     * Refreshes cache of a given {@code device}
     *
     * @param device device
     */
    public void refreshCache(BleDevice device) {
        device.refreshCache();
    }

    /**
     * Toggles a auto-connect attribute of a given {@code device}
     *
     * @param device device
     */
    public void toggleAutoConnect(BleDevice device) {
        device.setAutoConnectEnabled(!device.isAutoConnectEnabled());
        device.save();
    }

    /**
     * Determines whether a {@code device}'s attribute {@code autoConnectEnabled} is set to true
     *
     * @param device device
     * @return true is auto-connect is enabled
     */
    public boolean isAutoConnectEnabled(BleDevice device) {
        Realm realm = Realm.getDefaultInstance();
        boolean autoConnectEnabled = realm.where(BleDevice.class)
                .equalTo("address", device.getAddress())
                .equalTo("autoConnectEnabled", true).
                        count() > 0;
        if (!realm.isClosed()) realm.close();

        return autoConnectEnabled;
    }

    public void sendLocation(Context context, HttpGetTask.OnCompleteListener ocListener, BleDevice device, Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();

        String url = prefs.getString(res.getString(R.string.pref_golem_temperature_url), null);

        if (device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
            String temperature = device.getLatestReadings().get("temperature").value.toString();

            // Add parameters
            Map<EHttpParameter, String> parameters = new LinkedHashMap<>();

            parameters.put(EHttpParameter.DBG, String.valueOf(prefs.getBoolean(res.getString(R.string.pref_golem_temperature_dbg), true) ? "1" : "0"));
            parameters.put(EHttpParameter.TOKEN, prefs.getString(res.getString(R.string.pref_golem_temperature_token), null));
            parameters.put(EHttpParameter.TYPE, prefs.getString(res.getString(R.string.pref_golem_temperature_type), res.getString(R.string.pref_default_golem_temperature_type)));
            parameters.put(EHttpParameter.COUNTRY, prefs.getString(res.getString(R.string.pref_golem_temperature_country), null));
            parameters.put(EHttpParameter.CITY, prefs.getString(res.getString(R.string.pref_golem_temperature_city), null));
            parameters.put(EHttpParameter.ZIP, prefs.getString(res.getString(R.string.pref_golem_temperature_zip), null));
            parameters.put(EHttpParameter.TEMP, temperature);

            // Add location parameters
            if (location != null) {
                parameters.put(EHttpParameter.LAT, String.valueOf(round(location.getLatitude(), 2)));
                parameters.put(EHttpParameter.LONG, String.valueOf(round(location.getLongitude(), 2)));
            }

            // Call webservice
            try {
                HttpGetTask  httpGetTask = new HttpGetTask(ocListener, url);
                httpGetTask.execute(parameters).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="expanded" desc="Getters / Setters">

    public Map<String, BleDevice> getScannedDevices() {
        return scannedDevices;
    }

    public List<BleDevice> getScannedDevicesAsList() {
        return new ArrayList<>(getScannedDevices().values());
    }

    public Map<String, BleDevice> getAttachedDevices() {
        return attachedDevices;
    }

    public BleDevice getAttachedDeviceByAddress(String address) {
        return getAttachedDevices().get(address);
    }

    public List<BleDevice> getAttachedDevicesAsList() {
        return new ArrayList<>(getAttachedDevices().values());
    }

    // </editor-fold>
}
