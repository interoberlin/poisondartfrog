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

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.controller.DevicesController;
import de.interoberlin.poisondartfrog.view.adapters.ScanResultsAdapter;

public class ScanResultsDialog extends DialogFragment {
    public static final String TAG = ScanResultsDialog.class.getCanonicalName();

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DevicesController devicesController = DevicesController.getInstance();
        final Resources res = getActivity().getResources();

        ScanResultsAdapter wunderbarScanResultsAdapter = new ScanResultsAdapter(getActivity(), getActivity(), this, R.layout.item_scan_result, devicesController.getScannedDevicesAsList());

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_scan_results, null);

        ListView lvScanResults = (ListView) v.findViewById(R.id.lvScanResults);
        lvScanResults.setAdapter(wunderbarScanResultsAdapter);

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
}