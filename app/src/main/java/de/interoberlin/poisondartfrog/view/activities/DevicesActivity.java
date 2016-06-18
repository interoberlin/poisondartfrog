package de.interoberlin.poisondartfrog.view.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.interoberlin.mate.lib.view.AboutActivity;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.tasks.HttpGetTask;
import de.interoberlin.poisondartfrog.view.adapters.DevicesAdapter;
import de.interoberlin.poisondartfrog.view.adapters.ScanResultsAdapter;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;

public class DevicesActivity extends AppCompatActivity implements ScanResultsAdapter.OnCompleteListener, DevicesAdapter.OnCompleteListener, HttpGetTask.OnCompleteListener, BleDevice.OnChangeListener, LocationListener {
    public static final String TAG = DevicesActivity.class.getSimpleName();

    // Model
    private DevicesAdapter devicesAdapter;

    // Controller
    private DevicesController devicesController;

    private BluetoothAdapter bluetoothAdapter;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 1;
    private static final int PERMISSION_VIBRATE = 2;

    private final static int REQUEST_ENABLE_BT = 100;
    private final static int REQUEST_ENABLE_LOCATION = 101;

    // Properties
    private static int VIBRATION_DURATION;

    private BluetoothLeService bluetoothLeService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(TAG, "Service connected");
            if (service != null) {
                bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            } else {
                Log.e(TAG, "Service is null");
            }

            if (!bluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }

            devicesController = DevicesController.getInstance();
            updateView();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Service disconnected");
            bluetoothLeService = null;

            devicesController = DevicesController.getInstance();
            updateView();
        }
    };

    private BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String deviceAddress = intent.getStringExtra(BluetoothLeService.EXTRA_DEVICE_ADDRESS);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, "Gatt connected to " + deviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Gatt disconnected from " + deviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "Gatt services discovered");
                BleDevice device = devicesController.getAttachedDeviceByAdress(deviceAddress);
                device.setServices(bluetoothLeService.getSupportedGattServices());
                Log.d(TAG, device.toString());

                devicesController.disconnect(bluetoothLeService, device);

                updateView();
            }
        }
    };

    private LocationManager locationManager;
    private Location currentLocation;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        VIBRATION_DURATION = getResources().getInteger(R.integer.vibration_duration);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        requestPermission(this, Manifest.permission.BLUETOOTH_ADMIN, PERMISSION_BLUETOOTH_ADMIN);
        requestPermission(this, Manifest.permission.VIBRATE, PERMISSION_VIBRATE);

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
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                vibrate(VIBRATION_DURATION);
                ScanResultsDialog dialog = new ScanResultsDialog();
                Bundle b = new Bundle();
                b.putCharSequence(getResources().getString(R.string.bundle_dialog_title), getResources().getString(R.string.devices));
                dialog.setArguments(b);
                dialog.show(getFragmentManager(), ScanResultsDialog.TAG);
                return false;
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

        // disableBluetooth();
        // disableLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings: {
                startActivity(new Intent(DevicesActivity.this, SettingsActivity.class));
                break;
            }
            case R.id.menu_about: {
                startActivity(new Intent(DevicesActivity.this, AboutActivity.class));
                break;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }

    // --------------------
    // Methods - Callbacks
    // --------------------

    @Override
    public void onAttachDevice(BleDevice device) {
        vibrate(VIBRATION_DURATION);

        // getSingleLocation();

        device.registerOnChangeListener(this);



        if (devicesController.attach(bluetoothLeService, device)) {
            updateView();
            snack(R.string.attached_device);
        } else {
            snack(R.string.failed_to_attach_device);
        }
    }

    @Override
    public void onDetachDevice(BleDevice device) {
        Log.d(TAG, "onDetachDevice " + device.getName());

        vibrate(VIBRATION_DURATION);

        if (devicesController.detach(bluetoothLeService, device)) {
            updateView();
            snack(R.string.detached_device);
        } else {
            snack(R.string.failed_to_detach_device);
        }
    }

    @Override
    public void onChange(BleDevice device) {
        updateView();
    }

    @Override
    public void onCacheCleared(boolean success) {
        snack(success ? R.string.cached_refreshed : R.string.cached_refresh_failed);
    }

    @Override
    public void onHttpGetExecuted(String response) {
        toast("Webservice response " + response);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
        Log.d(TAG, "Location " + location.getLongitude() + " / " + location.getLatitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    // --------------------
    // Methods
    // --------------------

    public void getSingleLocation() {
        String provider = locationManager.getBestProvider(new Criteria(), false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(provider, this, null);
        }
    }

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

    private void disableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
    }

    private void disableLocation() {
        if (isLocationEnabled()) {
            startActivityForResult(new Intent(
                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLE_LOCATION);
        }
    }

    /**
     * Asks user for permission
     *
     * @param activity   activity
     * @param permission permission to ask for
     * @param callBack   callback
     */
    public static void requestPermission(Activity activity, String permission, int callBack) {
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission not granted");

            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                ActivityCompat.requestPermissions(activity,
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

    /**
     * Displays a snack with a given {@code text resource}
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
     * Displays a toast with a give {@code text resource}
     *
     * @param text text to display
     */
    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    /**
     * Updates view
     */
    public void updateView() {
        final ListView lv = (ListView) findViewById(R.id.lv);

        devicesAdapter.filter();
        lv.invalidateViews();
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Location getCurrentLocation() {
        return currentLocation;
    }
}
