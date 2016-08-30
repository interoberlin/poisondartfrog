package de.interoberlin.poisondartfrog.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import de.interoberlin.merlot_android.controller.MappingController;
import de.interoberlin.merlot_android.model.mapping.Mapping;
import de.interoberlin.poisondartfrog.R;

public class SelectMappingDialog extends DialogFragment {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = SelectMappingDialog.class.getSimpleName();

    // Model
    private String selectedMapping;

    // Controller
    private MappingController mappingController;

    private OnCompleteListener ocListener;

    // </editor-fold>

    // --------------------
    // Methods - Lifecycle
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Lifecycle">

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getActivity().getResources();

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_select_mapping, null);
        Spinner spnnrMappings = (Spinner) v.findViewById(R.id.spnnrMappings);

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));

        // Fill views with arguments
        ArrayAdapter<String> spnnrAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mappingController.getInstance(getActivity()).getExistingMappingNames());
        spnnrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrMappings.setAdapter(spnnrAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);

        // Add actions
        spnnrMappings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedMapping = parent.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                selectedMapping = null;
            }
        });

        // Add buttons
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
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

        dialog.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedMapping != null) {
                    mappingController = MappingController.getInstance(getActivity());
                    Mapping mapping = mappingController.getExistingMappingByName(selectedMapping);
                    ocListener.onMappingSelected(mapping);
                }
                dismiss();
            }
        });
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnCompleteListener {
        void onMappingSelected(Mapping mapping);
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