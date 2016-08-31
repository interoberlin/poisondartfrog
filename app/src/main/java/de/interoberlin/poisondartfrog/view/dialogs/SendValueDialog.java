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
import android.widget.EditText;
import android.widget.TextView;

import de.interoberlin.merlot_android.controller.DevicesController;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;

public class SendValueDialog extends DialogFragment {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = SendValueDialog.class.getSimpleName();

    // Controllers
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
        this.devicesController = DevicesController.getInstance();

        final Resources res = getActivity().getResources();

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_send_value, null);
        final TextView tvCharacteristic = (TextView) v.findViewById(R.id.tvCharacteristic);

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));
        final String characteristicId = bundle.getString(res.getString(R.string.bundle_characteristic_id));

        // Fill views with arguments
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);

        ECharacteristic c = ECharacteristic.fromId(characteristicId);

        tvCharacteristic.setText((c != null) ? c.getName() : characteristicId);

        // Add negative button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        // Add positive button
        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get arguments
        Bundle bundle = this.getArguments();
        final String deviceAddress = bundle.getString(getActivity().getString(R.string.bundle_device_address));
        final String serviceId = bundle.getString(getActivity().getString(R.string.bundle_service_id));
        final String characteristicId = bundle.getString(getActivity().getString(R.string.bundle_characteristic_id));

        AlertDialog dialog = (AlertDialog) getDialog();
        final EditText etValue = (EditText) dialog.findViewById(R.id.etValue);


        Button negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleDevice d = devicesController.getAttachedDeviceByAddress(deviceAddress);
                EService s = EService.fromId(serviceId);
                ECharacteristic c = ECharacteristic.fromId(characteristicId);

                ocListener.onSendValue(d, s, c, etValue.getText().toString());
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
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnCompleteListener {
        void onSendValue(BleDevice device, EService service, ECharacteristic characteristic, String value);
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