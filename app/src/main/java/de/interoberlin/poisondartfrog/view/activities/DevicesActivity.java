package de.interoberlin.poisondartfrog.view.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.model.ScanTask;
import de.interoberlin.poisondartfrog.view.dialogs.ScanResultsDialog;
import io.relayr.android.RelayrSdk;

public class DevicesActivity extends AppCompatActivity implements ScanTask.OnCompleteListener {
    public static final String TAG = DevicesActivity.class.getCanonicalName();
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;

    // View
    private RelativeLayout rlContent;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    // Controller
    private DevicesController devicesController;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        // Load layout
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        requestPermission(Manifest.permission.READ_CONTACTS, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        devicesController = DevicesController.getInstance(this);

        // Check BLE support
        if (!RelayrSdk.isBleSupported()) {
            Toast.makeText(this, getString(R.string.bt_not_supported), Toast.LENGTH_SHORT).show();
        } else if (!RelayrSdk.isBleAvailable()) {
            RelayrSdk.promptUserToActivateBluetooth(this);
        }

        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);

        // Add actions
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RelayrSdk.isBleSupported() && RelayrSdk.isBleAvailable()) {
                    Snackbar.make(rlContent, getResources().getString(R.string.scanning_for_ble_devices), Snackbar.LENGTH_LONG)
                            .show();
                    devicesController.scan(DevicesActivity.this);
                } else {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        disconnectBluetooth();

        super.onDestroy();
    }

    // --------------------
    // Methods - Callbacks
    // --------------------

    @Override
    public void onFinished() {
        ScanResultsDialog dialog = new ScanResultsDialog();
        Bundle b = new Bundle();
        b.putCharSequence(getResources().getString(R.string.bundle_dialog_title), getResources().getString(R.string.devices));
        dialog.setArguments(b);
        dialog.show(getFragmentManager(), ScanResultsDialog.TAG);
    }

    // --------------------
    // Methods
    // --------------------

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
