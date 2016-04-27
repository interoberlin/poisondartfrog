package de.interoberlin.poisondartfrog.view.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.service.BleDeviceManager;
import de.interoberlin.poisondartfrog.view.adapters.DevicesAdapter;
import de.interoberlin.poisondartfrog.view.adapters.ScanResultsAdapter;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;

public class DevicesActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback, ScanResultsAdapter.OnCompleteListener, DevicesAdapter.OnCompleteListener {
    public static final String TAG = DevicesActivity.class.getSimpleName();

    // Model
    private DevicesAdapter devicesAdapter;

    // Controller
    private DevicesController devicesController;

    private BluetoothAdapter bluetoothAdapter;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 1;
    private static final int PERMISSION_VIBRATE = 2;
    private final static int REQUEST_ENABLE_BT = 3;
    private final static int REQUEST_ENABLE_LOCATION = 4;

    // Properties
    private static int VIBRATION_DURATION;

    private Handler handler;

    private BluetoothLeService bluetoothLeService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "Service connected");
            if (service != null) {
                bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            } else {
                Log.e(TAG, "Service is null");
            }

            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }

            devicesController = DevicesController.getInstance();
            updateListView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "Service disconnected");
            bluetoothLeService = null;

            devicesController = DevicesController.getInstance();
            updateListView();
        }
    };

    private BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String deviceAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DEVICE_ADDRESS);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "Gatt connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "Gatt disconnected from " + deviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i(TAG, "Gatt services discovered");
                BleDevice device = devicesController.getAttachedDeviceByAdress(deviceAddress);
                device.setServices(bluetoothLeService.getSupportedGattServices());
                Log.i(TAG, device.toString());

                updateListView();
            }
        }
    };

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        VIBRATION_DURATION = getResources().getInteger(R.integer.vibration_duration);

        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        requestPermission(Manifest.permission.BLUETOOTH_ADMIN, PERMISSION_BLUETOOTH_ADMIN);
        requestPermission(Manifest.permission.VIBRATE, PERMISSION_VIBRATE);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        devicesController = DevicesController.getInstance();
        devicesAdapter = new DevicesAdapter(this, this, R.layout.card_device, devicesController.getAttachedDevicesAsList());

        // Load layout
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final ListView lv = (ListView) findViewById(R.id.lv);

        lv.setAdapter(devicesAdapter);
        setSupportActionBar(toolbar);

        checkBleSupport();
        initBleAdapter();

        requestEnableBluetooth();
        requestEnableLocation();

        // Add actions
        final long[] times = new long[2];

        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (devicesController.getAttachedDevices().isEmpty()) {
                    switch (arg1.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            times[0] = System.currentTimeMillis();
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            times[1] = System.currentTimeMillis();
                            long diff = times[1] - times[0];
                            final long scanPeriod = diff < 1000 ? 1000 : diff;

                            scanLeDevice(true, scanPeriod);
                            break;
                        }
                    }
                } else {
                    snack("Detach device first");
                }
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        disconnectBluetooth();
    }

    // --------------------
    // Methods - Callbacks
    // --------------------

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!devicesController.getAttachedDevices().containsKey(device.getAddress())) {
            Log.i(TAG, device.toString() + " \t " + device.getName() + " \t " + rssi);
            devicesController.getScannedDevices().put(device.getAddress(), device);
        }
    }

    @Override
    public void onAttachDevice(BluetoothDevice device) {
        vibrate(VIBRATION_DURATION);;
        if (devicesController.attach(bluetoothLeService, new BleDevice(this, device, BleDeviceManager.getInstance()))) {
            updateListView();
            snack(R.string.attached_device);
        } else {
            snack(R.string.failed_to_attach_device);
        }
    }

    @Override
    public void onDetachDevice(BleDevice device) {
        vibrate(VIBRATION_DURATION);
        if (devicesController.detach(bluetoothLeService, device)) {
            updateListView();
            snack(R.string.detached_device);
        } else {
            snack(R.string.failed_to_detach_device);
        }
    }

    // --------------------
    // Methods
    // --------------------

    private void vibrate(int duration) {
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(duration);
    }

    private void checkBleSupport() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initBleAdapter() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    private boolean isLocationEnabled() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void requestEnableBluetooth() {
        if (!isBluetoothEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }
    }

    private void requestEnableLocation() {
        if (!isLocationEnabled()) {
            startActivityForResult(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLE_LOCATION);
        }
    }

    /**
     * Disables bluetooth
     */
    private void disconnectBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
    }

    /**
     * Asks user for permission
     *
     * @param permission permission to ask for
     * @param callBack   callback
     */
    private void requestPermission(String permission, int callBack) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission not granted");

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        callBack);
            }
        } else {
            Log.i(TAG, "Permission granted");
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @SuppressWarnings("deprecation")
    private void scanLeDevice(final boolean enable, final long scanPeriod) {
        vibrate(VIBRATION_DURATION);

        if (enable) {
            devicesController.getScannedDevices().clear();
            handler = new Handler();

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(DevicesActivity.this);
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if (!devicesController.getScannedDevices().isEmpty()) {
                                ScanResultsDialog dialog = new ScanResultsDialog();
                                Bundle b = new Bundle();
                                b.putCharSequence(getResources().getString(R.string.bundle_dialog_title), getResources().getString(R.string.devices));
                                dialog.setArguments(b);
                                dialog.show(getFragmentManager(), ScanResultsDialog.TAG);
                            } else {
                                snack(R.string.no_devices_found);
                            }
                        }
                    }, 0);
                }
            }, scanPeriod);

            bluetoothAdapter.startLeScan(this);
        } else {
            bluetoothAdapter.stopLeScan(this);
        }
    }

    /**
     * Displays a snack with a given {@code text}
     *
     * @param text text resource
     */
    public void snack(int text) {
        final RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        Snackbar.make(rlContent, getResources().getString(text), Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Displays a snack with a given {@code text}
     *
     * @param text text resource
     */
    public void snack(String text) {
        final RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        Snackbar.make(rlContent, text, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Updates the list view
     */
    public void updateListView() {
        final ListView lv = (ListView) findViewById(R.id.lv);

        devicesAdapter.filter();
        lv.invalidateViews();
    }
}
