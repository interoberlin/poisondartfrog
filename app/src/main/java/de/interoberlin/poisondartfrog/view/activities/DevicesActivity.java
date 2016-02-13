package de.interoberlin.poisondartfrog.view.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.view.adapters.DevicesAdapter;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;

public class DevicesActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    public static final String TAG = DevicesActivity.class.getCanonicalName();
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;

    // Model
    private DevicesAdapter devicesAdapter;

    // View
    private RelativeLayout rlContent;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ListView lv;
    private ProgressBar pb;

    // Controller
    private DevicesController devicesController;

    private BluetoothAdapter bluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;

    private boolean scanning;
    private Handler handler;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        devicesController = DevicesController.getInstance(this);
        devicesAdapter = new DevicesAdapter(this, this, R.layout.card_device, devicesController.getSubscribedDevicesAsList());

        // Load layout
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        lv = (ListView) findViewById(R.id.lv);
        pb = (ProgressBar) findViewById(R.id.pb);

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
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBluetooth();
    }

    // --------------------
    // Methods - Callbacks
    // --------------------

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG, device.toString() + " \t " + device.getName() + " \t " + rssi);
        devicesController.getScannedDevices().put(device.getAddress(), device);
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
                                Snackbar.make(rlContent, getResources().getString(R.string.no_devices_found), Snackbar.LENGTH_LONG)
                                        .show();
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
     * Updates the list view
     */
    private void updateListView() {
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
}
