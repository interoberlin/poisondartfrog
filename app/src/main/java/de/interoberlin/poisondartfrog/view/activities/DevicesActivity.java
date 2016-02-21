package de.interoberlin.poisondartfrog.view.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.ExtendedBluetoothDevice;
import de.interoberlin.poisondartfrog.view.adapters.DevicesAdapter;
import de.interoberlin.poisondartfrog.view.adapters.ScanResultsAdapter;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;

public class DevicesActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback, ScanResultsAdapter.OnCompleteListener, DevicesAdapter.OnCompleteListener {
    public static final String TAG = DevicesActivity.class.getCanonicalName();
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;

    // Model
    private DevicesAdapter devicesAdapter;

    // View
    private RelativeLayout rlContent;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ListView lv;

    // Controller
    private DevicesController devicesController;

    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;

    private boolean scanning;
    private boolean connected;
    private Handler handler;

    private BluetoothLeService bluetoothLeService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "Service connected");
            if (service == null)
                Log.e(TAG, "service is null");

            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }

            devicesController = DevicesController.getInstance();
            if (!devicesController.getAttachedDevices().isEmpty())
                devicesController.getAttachedDevices().get(0).setConnected(true);
            updateListView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "Service disconnected");
            bluetoothLeService = null;

            devicesController = DevicesController.getInstance();
            if (!devicesController.getAttachedDevices().isEmpty())
                devicesController.getAttachedDevices().get(0).setConnected(false);
            updateListView();
        }
    };

    private BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String address = intent.getStringExtra(BluetoothLeService.EXTRA_ADDRESS);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "Gatt connected");
                connected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "Gatt disconnected");
                connected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                ExtendedBluetoothDevice device = devicesController.getAttachedDeviceByAdress(address);

                List<BluetoothGattService> supportedGattServices = bluetoothLeService.getSupportedGattServices();
                Log.i(TAG, "Gatt services discovered");
                for (BluetoothGattService gs : supportedGattServices) {
                    Log.d(TAG, ".. " + gs.getUuid().toString());
                    for (BluetoothGattCharacteristic gc : gs.getCharacteristics()) {
                        Log.d(TAG, ".... " + gc.getUuid().toString());
                        device.getGattCharacteristics().put(gc.getUuid(), gc);
                    }
                }

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // TODO : update bluetooth device reading
                Log.i(TAG, "Data available");
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

        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);

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
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        lv = (ListView) findViewById(R.id.lv);

        lv.setAdapter(devicesAdapter);
        setSupportActionBar(toolbar);

        // Check BLE support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.bt_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

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
            Log.d(TAG, device.toString() + " \t " + device.getName() + " \t " + rssi);
            devicesController.getScannedDevices().put(device.getAddress(), device);
        }
    }

    @Override
    public void onAttachDevice(BluetoothDevice device) {
        snack("Attached device");
        devicesController.attach(device);
        updateListView();
    }

    @Override
    public void onConnectDevice(ExtendedBluetoothDevice device) {
        if (bluetoothLeService != null) {
            bluetoothLeService.connect(device.getAddress());
            device.setConnected(true);
            updateListView();
            snack("Connected device");
        } else {
            snack("bluetooth LE service not available");
        }
    }

    @Override
    public void onDisconnectDevice(ExtendedBluetoothDevice device) {
        if (bluetoothLeService != null) {
            bluetoothLeService.disconnect();
            device.setConnected(false);
            updateListView();
        } else {
            snack("bluetooth LE service not available");
        }
    }

    @Override
    public void onDetachDevice(ExtendedBluetoothDevice device) {
        devicesController.detach(device);
        snack(R.string.detached_device);
        updateListView();
    }

    // --------------------
    // Methods
    // --------------------

    private void scanLeDevice(final boolean enable, final long scanPeriod) {
        if (enable) {
            devicesController.getScannedDevices().clear();
            handler = new Handler();

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
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

            scanning = true;
            bluetoothAdapter.startLeScan(this);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(this);
        }
    }

    /**
     * Displays a snack with a given {@code text}
     *
     * @param text text resource
     */
    public void snack(int text) {
        Snackbar.make(rlContent, getResources().getString(text), Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Displays a snack with a given {@code text}
     *
     * @param text text resource
     */
    public void snack(String text) {
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
            Log.d(TAG, "Permission not granted");

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        callBack);
            }
        } else {
            Log.d(TAG, "Permission granted");
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
}
