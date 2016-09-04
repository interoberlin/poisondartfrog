package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.parser.data.SentientLightLED;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;

public class SentientLightLedComponent extends LinearLayout {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = SentientLightLedComponent.class.getSimpleName();

    private int ledCount;
    private String componentOneName;
    private String componentTwoName;
    private String componentThreeName;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public SentientLightLedComponent(Context context) {
        super(context);
    }

    public SentientLightLedComponent(final Context context, final OnCompleteListener ocListener, final BleDevice device) {
        super(context);

        // TODO retrieve this from LED device
        this.ledCount = 10;
        this.componentOneName = getContext().getResources().getString(R.string.cold);
        this.componentTwoName = getContext().getResources().getString(R.string.warm);
        this.componentThreeName = getContext().getResources().getString(R.string.amber);

        // Load layout
        inflate(context, R.layout.component_sentient_light_led, this);

        final TableLayout tl = (TableLayout) findViewById(R.id.tl);
        final TextView tvClear = (TextView) findViewById(R.id.tvClear);
        final TextView tvAddLine = (TextView) findViewById(R.id.tvAddLine);
        final TextView tvSend = (TextView) findViewById(R.id.tvSend);

        tl.addView(getTableHead(context, componentOneName, componentTwoName, componentThreeName));
        tl.addView(getTableRow(context, tl));

        // Add actions
        tvClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tl.removeAllViews();
                tl.addView(getTableHead(context, componentOneName, componentTwoName, componentThreeName));
                tl.addView(getTableRow(context, tl));
            }
        });

        tvAddLine.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tl.addView(getTableRow(context, tl));
            }
        });

        tvSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String json = getJson(tl);
                ocListener.onSendSentientLightLedValue(device, EService.SENTIENT_LIGHT_LED, ECharacteristic.SENTIENT_LIGHT_LED_TX, json);
            }
        });
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    private View getTableHead(Context context, String componentOneName, String componentTwoName, String componentThreeName) {
        View row = inflate(context, R.layout.component_sentient_light_led_header, null);

        // Load layout
        TextView tvId = (TextView) row.findViewById(R.id.tvID);
        TextView tvComponentOne = (TextView) row.findViewById(R.id.tvComponentOne);
        TextView tvComponentTwo = (TextView) row.findViewById(R.id.tvComponentTwo);
        TextView tvComponentThree = (TextView) row.findViewById(R.id.tvComponentThree);

        tvId.setText(R.string.id);
        tvComponentOne.setText(componentOneName);
        tvComponentTwo.setText(componentTwoName);
        tvComponentThree.setText(componentThreeName);

        return row;
    }

    private View getTableRow(final Context context, final TableLayout tl) {
        final View row = inflate(context, R.layout.component_sentient_light_led_row, null);

        // Load layout
        final Spinner spnnr = (Spinner) row.findViewById(R.id.spnnrLedID);
        final EditText etComponentOne = (EditText) row.findViewById(R.id.etComponentOne);
        final EditText etComponentTwo = (EditText) row.findViewById(R.id.etComponentTwo);
        final EditText etComponentThree = (EditText) row.findViewById(R.id.etComponentThree);
        final ImageView ivRemove = (ImageView) row.findViewById(R.id.ivRemove);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < ledCount; i++) {
            list.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, list);
        spnnr.setAdapter(adapter);

        // Add actions and listeners

        // <editor-fold defaultstate="collapsed" desc="Listeners">

        ivRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tl.removeView(row);
            }
        });
        etComponentOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate(etComponentOne);
            }
        });
        etComponentTwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate(etComponentTwo);
            }
        });
        etComponentThree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate(etComponentThree);
            }
        });

        // </editor-fold>

        return row;
    }

    private void validate(EditText et) {
        String text = et.getText().toString();

        if (Integer.parseInt(text) < 0 || Integer.parseInt(text) > 255) {
            et.setError(getContext().getResources().getString(R.string.value_must_be_between_0_and_255));
        }
    }

    private String getJson(TableLayout tl) {
        SentientLightLED slLED = new SentientLightLED();

        if (tl != null) {
            for (int i = 0; i < tl.getChildCount(); i++) {
                View tr = tl.getChildAt(i);

                if (tr != null && tr instanceof TableRow) {
                    if (((TableRow) tr).getChildAt(0) instanceof Spinner &&
                            ((TableRow) tr).getChildAt(1) instanceof EditText &&
                            ((TableRow) tr).getChildAt(2) instanceof EditText &&
                            ((TableRow) tr).getChildAt(2) instanceof EditText) {
                        Spinner spnnr = (Spinner) ((TableRow) tr).getChildAt(0);
                        EditText etComponentOne = (EditText) ((TableRow) tr).getChildAt(1);
                        EditText etComponentTwo = (EditText) ((TableRow) tr).getChildAt(2);
                        EditText etComponentThree = (EditText) ((TableRow) tr).getChildAt(3);

                        int index = Integer.parseInt(spnnr.getSelectedItem().toString());
                        int value1 = Integer.parseInt(etComponentOne.getText().toString());
                        int value2 = Integer.parseInt(etComponentTwo.getText().toString());
                        int value3 = Integer.parseInt(etComponentThree.getText().toString());

                        slLED.getLeds().add(new SentientLightLED().new LED(index, value1, value2, value3));

                    }
                }
            }
        }

        return new Gson().toJson(slLED);
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnCompleteListener {
        void onSendSentientLightLedValue(BleDevice device, EService service, ECharacteristic characteristic, String value);
    }

    // </editor-fold>
}