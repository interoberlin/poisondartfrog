package de.interoberlin.poisondartfrog.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import de.interoberlin.merlot_android.controller.DevicesController;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.ble.BleScannerFilter;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.adapters.ScanResultsAdapter;

public class ScanResultsDialog extends DialogFragment implements
        BleScannerFilter.BleFilteredScanCallback,
        ScanResultsAdapter.OnCompleteListener{
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = ScanResultsDialog.class.getSimpleName();

    // View
    private ScanResultsAdapter scanResultsAdapter;
    private ListView lvScanResults;

    // Controller
    private DevicesController devicesController;

    private OnCompleteListener ocListener;

    // </editor-fold>

    // --------------------
    // Methods - Lifecycle
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Lifecycle">

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devicesController = DevicesController.getInstance();
        devicesController.getScannedDevices().clear();

        final Resources res = getActivity().getResources();

        scanResultsAdapter = new ScanResultsAdapter(getActivity(), this, R.layout.item_scan_result, devicesController.getScannedDevicesAsList());

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_scan_results, null);

        lvScanResults = (ListView) v.findViewById(R.id.lvScanResults);
        lvScanResults.setAdapter(scanResultsAdapter);

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));

        // Fill views with arguments
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);

        // Add negative button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        devicesController.startScan(getActivity(), this);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();

        Button negativeButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        devicesController.stopScan();
    }

    // </editor-fold>

    // --------------------
    // Methods - Callbacks
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callbacks">

    @Override
    public void onLeScan(BleDevice device, int rssi) {
        if (!devicesController.getScannedDevices().containsKey(device.getAddress()) &&
                !devicesController.getAttachedDevices().containsKey(device.getAddress())) {
            devicesController.getScannedDevices().put(device.getAddress(), device);
            updateView();
        }
    }

    @Override
    public void onAttachDevice(BleDevice device) {
        ocListener.onAttachDevice(device);
        dismiss();
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Updates the list view
     */
    public void updateView() {
        scanResultsAdapter.filter();
        lvScanResults.invalidateViews();
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnCompleteListener {
        void onAttachDevice(BleDevice bleDevice);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.ocListener = (OnCompleteListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    // </editor-fold>
}