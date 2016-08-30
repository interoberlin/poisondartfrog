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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.interoberlin.merlot_android.controller.MappingController;
import de.interoberlin.merlot_android.model.mapping.Mapping;
import de.interoberlin.merlot_android.model.mapping.actions.EActionType;
import de.interoberlin.merlot_android.model.mapping.functions.EFunctionType;
import de.interoberlin.poisondartfrog.R;

public class AddMappingDialog extends DialogFragment {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = AddMappingDialog.class.getSimpleName();

    // Model
    private String selectedFunctionTypeName;
    private EFunctionType selectedFunctionType;
    private String selectedActionTypeName;
    private EActionType selectedActionType;

    // View
    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.spnnrFunctionTypes) Spinner spnnrFunctionTypes;
    @BindView(R.id.llFunctionParametersThreshold) LinearLayout llFunctionParametersThreshold;
    @BindView(R.id.etParameterThresholdMinValue) EditText etParameterThresholdMinValue;
    @BindView(R.id.etParameterThresholdMaxValue) EditText etParameterThresholdMaxValue;

    @BindView(R.id.spnnrActionTypes) Spinner spnnrActionTypes;
    @BindView(R.id.llActionParametersWriteCharacteristic) LinearLayout llActionParametersWriteCharacteristic;
    @BindView(R.id.etParameterWriteCharacteristicValue) EditText etParameterWriteCharacteristicValue;
    
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
        final View v = View.inflate(getActivity(), R.layout.dialog_mapping, null);
        ButterKnife.bind(this, v);

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));

        // Fill views with arguments
        ArrayAdapter<String> spnnrFunctionTypesAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, EFunctionType.getNamesList());
        spnnrFunctionTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrFunctionTypes.setAdapter(spnnrFunctionTypesAdapter);

        ArrayAdapter<String> spnnrActionTypesAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, EActionType.getNamesList());
        spnnrActionTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnnrActionTypes.setAdapter(spnnrActionTypesAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);

        // Add actions
        spnnrFunctionTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedFunctionTypeName = parent.getItemAtPosition(pos).toString();
                selectedFunctionType = EFunctionType.fromString(selectedFunctionTypeName);

                if (selectedFunctionType != null) {
                    switch (selectedFunctionType) {
                        case THRESHOLD: {
                            llFunctionParametersThreshold.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                selectedFunctionTypeName = null;
            }
        });

        spnnrFunctionTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedFunctionTypeName = parent.getItemAtPosition(pos).toString();
                selectedFunctionType = EFunctionType.fromString(selectedFunctionTypeName);

                if (selectedFunctionType != null) {
                    switch (selectedFunctionType) {
                        case THRESHOLD: {
                            llActionParametersWriteCharacteristic.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                selectedFunctionTypeName = null;
            }
        });

        spnnrActionTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedActionTypeName = parent.getItemAtPosition(pos).toString();
                selectedActionType = EActionType.fromString(selectedActionTypeName);

                if (selectedActionType != null) {
                    switch (selectedActionType) {
                        case WRITE_CHARACTERISTIC: {
                            llActionParametersWriteCharacteristic.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                selectedFunctionTypeName = null;
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