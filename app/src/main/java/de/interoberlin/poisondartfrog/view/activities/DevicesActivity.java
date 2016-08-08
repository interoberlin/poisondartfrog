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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.interoberlin.mate.lib.view.AboutActivity;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.controller.MappingController;
import de.interoberlin.poisondartfrog.model.ble.BleDevice;
import de.interoberlin.poisondartfrog.model.ble.BleScannerFilter;
import de.interoberlin.poisondartfrog.model.ble.BluetoothLeService;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;
import de.interoberlin.poisondartfrog.model.mapping.Mapping;
import de.interoberlin.poisondartfrog.model.tasks.HttpGetTask;
import de.interoberlin.poisondartfrog.view.adapters.DevicesAdapter;
import de.interoberlin.poisondartfrog.view.dialogs.CharacteristicsDialog;
import de.interoberlin.poisondartfrog.view.dialogs.MappingDialog;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;

public class DevicesActivity extends AppCompatActivity implements
        // <editor-fold defaultstate="collapsed" desc="Interfaces">
        BleScannerFilter.BleFilteredScanCallback,
        DevicesAdapter.OnCompleteListener,
        BleDevice.OnChangeListener,
        Mapping.OnChangeListener,
        ScanResultsDialog.OnCompleteListener,
        MappingDialog.OnCompleteListener,
        HttpGetTask.OnCompleteListener,
        LocationListener {
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = DevicesActivity.class.getSimpleName();

    // Context
    private SharedPreferences prefs;
    private Resources res;

    // Model
    private DevicesAdapter devicesAdapter;

    // View
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fam)
    FloatingActionsMenu fam;
    @BindView(R.id.fabScan)
    FloatingActionButton fabScan;
    @BindView(R.id.fabAddMapping)
    FloatingActionButton fabAddMapping;
    @BindView(R.id.sgv)
    StaggeredGridView sgv;

    // Controller
    private DevicesController devicesController;
    private MappingController mappingController;

    // Constants
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 1;
    private static final int PERMISSION_VIBRATE = 2;
    // private static final int REQUEST_ENABLE_BT = 100;
    // private static final int REQUEST_ENABLE_LOCATION = 101;

    // Properties
    private static int VIBRATION_DURATION;
    private static int DEVICE_SCAN_PERIOD;
    private static int DEVICE_SCAN_DELAY;
    private static int GOLEM_SEND_PERIOD;

    // Async
    private static Handler scanHandler;
    private static Runnable scanRunnable;

    private static Handler sendLocationHandler;
    private static Runnable sendLocationRunnable;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeService bluetoothLeService;
    private ServiceConnection serviceConnection;
    private BroadcastReceiver gattUpdateReceiver;

    // Location
    private LocationManager locationManager;
    private Location currentLocation;

    // </editor-fold>

    // --------------------
    // Methods - Lifecycle
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Lifecycle">

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.res = getResources();

        requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        requestPermission(this, Manifest.permission.BLUETOOTH_ADMIN, PERMISSION_BLUETOOTH_ADMIN);
        requestPermission(this, Manifest.permission.VIBRATE, PERMISSION_VIBRATE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        serviceConnection = new ServiceConnection() {
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
                mappingController = MappingController.getInstance();
                updateView();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Service disconnected");
                bluetoothLeService = null;

                devicesController = DevicesController.getInstance();
                mappingController = MappingController.getInstance();
                updateView();
            }
        };
        gattUpdateReceiver = new BroadcastReceiver() {
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
                    BleDevice device = devicesController.getAttachedDeviceByAddress(deviceAddress);
                    device.setServices(bluetoothLeService.getSupportedGattServices());
                    Log.d(TAG, device.toString());

                    device.init();
                    device.read(ECharacteristic.BATTERY_LEVEL);
                    devicesController.disconnect(bluetoothLeService, device);

                    updateView();
                }
            }
        };

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());

        // Instatiate controller
        devicesController = DevicesController.getInstance();
        // devicesAdapter = new DevicesAdapter(this, this, R.layout.card_device, devicesController.getAttachedDevicesAsList());
        devicesAdapter = new DevicesAdapter(this, this, 0, null);

        // Load preferences
        VIBRATION_DURATION = getResources().getInteger(R.integer.vibration_duration);
        DEVICE_SCAN_PERIOD = Integer.parseInt(prefs.getString(res.getString(R.string.pref_scan_timer_period), "10"));
        DEVICE_SCAN_DELAY = Integer.parseInt(prefs.getString(res.getString(R.string.pref_scan_timer_delay), "60"));
        GOLEM_SEND_PERIOD = Integer.parseInt(prefs.getString(res.getString(R.string.pref_golem_temperature_send_period), "5"));

        ButterKnife.bind(this);

        setRequestedOrientation(isXLargeTablet(this) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (sgv != null)
            sgv.setAdapter(devicesAdapter);

        setSupportActionBar(toolbar);

        checkBleSupport();
        initBleAdapter();

        // requestEnableBluetooth();
        // requestEnableLocation();

        // Add actions
        if (fabScan != null) {
            fabScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fam != null)
                        fam.collapse();

                    if (!isBluetoothEnabled()) {
                        snack(R.string.enable_bluetooth_before_scan);
                    } else if (!isLocationEnabled()) {
                        snack(R.string.enable_location_before_scan);
                    } else {
                        vibrate();
                        ScanResultsDialog dialog = new ScanResultsDialog();
                        Bundle b = new Bundle();
                        b.putCharSequence(getResources().getString(R.string.bundle_dialog_title), getResources().getString(R.string.devices));
                        dialog.setArguments(b);
                        dialog.show(getFragmentManager(), ScanResultsDialog.TAG);
                    }
                }
            });
        }

        if (fabAddMapping != null) {
            fabAddMapping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fam != null)
                        fam.collapse();

                    vibrate();
                    MappingDialog dialog = new MappingDialog();
                    Bundle b = new Bundle();
                    b.putCharSequence(getResources().getString(R.string.bundle_dialog_title), getResources().getString(R.string.mapping));
                    dialog.setArguments(b);
                    dialog.show(getFragmentManager(), ScanResultsDialog.TAG);
                }
            });
        }

        // Run scan
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                devicesController.startScan(DevicesActivity.this);
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        devicesController.stopScan();
                    }
                }, DEVICE_SCAN_PERIOD * 1000);

                // Re-run
                scanHandler.postDelayed(this, DEVICE_SCAN_DELAY * 1000);
            }
        };
        scanHandler = new Handler();
        scanHandler.postDelayed(scanRunnable, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanHandler != null)
            scanHandler.removeCallbacks(scanRunnable);
        if (sendLocationHandler != null)
            sendLocationHandler.removeCallbacks(sendLocationRunnable);

        devicesController.stopScan();
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

    // </editor-fold>

    // --------------------
    // Methods - Callbacks
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callbacks">

    // Callbacks from BleFilteredScanCallback

    @Override
    public void onLeScan(BleDevice device, int rssi) {
        boolean attached = devicesController.getAttachedDevices().containsKey(device.getAddress());
        boolean autoCorrectEnabled = devicesController.isAutoConnectEnabled(device);

        if (!attached && autoCorrectEnabled) {
            device.setOnChangeListener(this);
            device.setAutoConnectEnabled(true);
            devicesController.attach(this, bluetoothLeService, device);
            updateView();
            snack("Auto " + device.getAddress());
        }
    }

    // Callbacks from ScanResulstDialog

    @Override
    public void onAttachDevice(BleDevice device) {
        vibrate();

        // getSingleLocation();

        if (devicesController.attach(this, bluetoothLeService, device)) {
            updateView();
            snack(R.string.attached_device);
        } else {
            snack(R.string.failed_to_attach_device);
        }
    }

    // Callbacks from DevicesAdapter

    @Override
    public void onChange(BleDevice device, int text) {
        snack(text, Snackbar.LENGTH_SHORT);
        updateView();
    }

    @Override
    public void onDetachDevice(BleDevice device) {
        Log.d(TAG, "onDetachDevice " + device.getName());

        vibrate();

        if (devicesController.detach(bluetoothLeService, device)) {
            updateView();
            snack(R.string.detached_device);
        } else {
            snack(R.string.failed_to_detach_device);
        }
    }

    @Override
    public void onSendLocation(final BleDevice device) {
        getSingleLocation();

        sendLocationRunnable = new Runnable() {
            @Override
            public void run() {
                devicesController.sendLocation(DevicesActivity.this, DevicesActivity.this, device, currentLocation);

                // Re-run
                scanHandler.postDelayed(this, GOLEM_SEND_PERIOD * 60 * 1000);
            }
        };
        sendLocationHandler = new Handler();
        sendLocationHandler.postDelayed(sendLocationRunnable, 100);
    }

    @Override
    public void onOpenCharacteristicsDialog(BleDevice device) {
        vibrate();
        CharacteristicsDialog dialog = new CharacteristicsDialog();
        Bundle b = new Bundle();
        b.putCharSequence(getResources().getString(R.string.bundle_dialog_title), getResources().getString(R.string.characteristics));
        b.putCharSequence(getResources().getString(R.string.bundle_device_address), device.getAddress());
        dialog.setArguments(b);
        dialog.show(getFragmentManager(), ScanResultsDialog.TAG);
    }

    @Override
    public void onRead() {
        vibrate();
    }

    @Override
    public void onSend() {
        vibrate();
    }

    @Override
    public void onSubscribe() {
        vibrate();
    }

    @Override
    public void onToggleAutoConnect() {
        vibrate();
    }

    @Override
    public void onDetachMapping(Mapping mapping) {
        vibrate();

        if (mappingController.deactivateMapping(mapping)) {
            updateView();
            snack(R.string.detached_mapping);
        } else {
            snack(R.string.failed_to_detach_mapping);
        }
    }

    // Callbacks from BleDevice

    @Override
    public void onChange(BleDevice device) {
        updateView();
    }

    @Override
    public void onCacheCleared(boolean success) {
        snack(success ? R.string.cached_refreshed : R.string.cached_refresh_failed);
    }

    // Callbacks from Mapping

    @Override
    public void onChange(Mapping mapping) {
        updateView();
    }

    // Callbacks from MappingDialog

    @Override
    public void onMappingSelected(Mapping mapping) {
        Log.d(TAG, "onMappingSelected " + mapping.toString());

        vibrate();
        mappingController.activateMapping(this, mapping);
        updateView();
        snack(R.string.attached_mapping);
    }

    // Callbacks from HttpGetTask

    @Override
    public void onHttpGetExecuted(String response) {
        toast("Webservice response " + response);
    }

    // Callbacks from LocationListener

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

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public void getSingleLocation() {
        String provider = locationManager.getBestProvider(new Criteria(), false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(provider, this, null);
        }
    }

    private void vibrate() {
        vibrate(VIBRATION_DURATION);
    }

    private void vibrate(int VIBRATION_DURATION) {
        ((Vibrator) getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION);
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

    /*
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
    */

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
        snack(getResources().getString(text));
    }

    /**
     * Displays a snack with a given {@code text resource}
     *
     * @param text     text resource
     * @param duration duration
     */
    public void snack(int text, int duration) {
        snack(getResources().getString(text, duration));
    }

    /**
     * Displays a snack with a given {@code text}
     *
     * @param text text
     */
    public void snack(String text) {
        final RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        if (rlContent != null)
            Snackbar.make(rlContent, text, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Displays a snack with a given {@code text}
     *
     * @param text     text
     * @param duration duration
     */
    @SuppressWarnings("unused")
    public void snack(String text, int duration) {
        final RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        if (rlContent != null)
            Snackbar.make(rlContent, text, duration).show();
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
        Log.v(TAG, "Update view");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devicesAdapter.filter();
                if (isXLargeTablet(DevicesActivity.this)) {
                    final StaggeredGridView sgv = (StaggeredGridView) findViewById(R.id.sgv);
                    if (sgv != null) {
                        sgv.invalidate();
                    }
                } else {
                    final ListView lv = (ListView) findViewById(R.id.lv);
                    if (lv != null) {
                        lv.invalidateViews();
                    }
                }
            }
        });
    }

    // </editor-fold>
}
