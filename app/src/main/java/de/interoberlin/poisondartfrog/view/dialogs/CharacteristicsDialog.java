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
import de.interoberlin.poisondartfrog.view.adapters.CharacteristicsAdapter;

public class CharacteristicsDialog extends DialogFragment {
    public static final String TAG = CharacteristicsDialog.class.getSimpleName();

    // View
    private CharacteristicsAdapter characteristcsAdapter;
    private ListView lvCharacteristics;

    // Controller
    private DevicesController devicesController;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devicesController = DevicesController.getInstance();
        devicesController.getScannedDevices().clear();

        final Resources res = getActivity().getResources();

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_characteristics, null);

        lvCharacteristics = (ListView) v.findViewById(R.id.lvCharacteristics);

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));
        final String deviceAddress = bundle.getString(res.getString(R.string.bundle_device_address));

        // Fill views with arguments
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);
        characteristcsAdapter = new CharacteristicsAdapter(getActivity(), getActivity(), this, R.layout.item_characteristic, devicesController.getAttachedDevices().get(deviceAddress).getCharacteristics());
        lvCharacteristics.setAdapter(characteristcsAdapter);

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    // --------------------
    // Methods
    // --------------------

    /**
     * Updates the list view
     */
    public void updateListView() {
        lvCharacteristics.invalidateViews();
    }
}