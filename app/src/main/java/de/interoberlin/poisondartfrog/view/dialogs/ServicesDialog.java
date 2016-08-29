package de.interoberlin.poisondartfrog.view.dialogs;

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
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.adapters.ServicesAdapter;

public class ServicesDialog extends DialogFragment {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = ServicesDialog.class.getSimpleName();

    // View
    private ServicesAdapter servicesAdapter;
    private ListView lvServices;

    // Controller
    private DevicesController devicesController;

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

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_services, null);

        lvServices = (ListView) v.findViewById(R.id.lvServices);

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));
        final String deviceAddress = bundle.getString(res.getString(R.string.bundle_device_address));

        // Fill views with arguments
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);
        servicesAdapter = new ServicesAdapter(getActivity(), R.layout.item_characteristic, devicesController.getAttachedDeviceByAddress(deviceAddress), devicesController.getAttachedDevices().get(deviceAddress).getServices());
        lvServices.setAdapter(servicesAdapter);

        // Add negative button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        // Add neutral button
        builder.setNeutralButton(R.string.refresh_cache, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                devicesController.refreshCache(deviceAddress);
            }
        });

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
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Updates the view
     */
    public void updateView() {
        lvServices.invalidateViews();
    }

    // </editor-fold>
}