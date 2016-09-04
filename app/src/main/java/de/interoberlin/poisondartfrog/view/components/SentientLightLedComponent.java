package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;

public class SentientLightLedComponent extends LinearLayout {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = SentientLightLedComponent.class.getSimpleName();

    private BleDevice device;
    private int ledCount;

    private OnCompleteListener ocListener;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public SentientLightLedComponent(Context context) {
        super(context);
    }

    public SentientLightLedComponent(Context context, final OnCompleteListener ocListener, final BleDevice device) {
        super(context);
        this.ocListener = ocListener;
        this.device = device;
        this.ledCount = 10;

        // Load layout
        inflate(context, R.layout.component_sentient_light_led, this);

        TableLayout tl = (TableLayout) findViewById(R.id.tl);
        TextView tvSend = (TextView) findViewById(R.id.tvSend);

        tl.addView(getTableHead(context));
        tl.addView(getTableRow(context));

        // Add actions
        tvSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ocListener.onSendSentientLightLedValue(device, EService.SENTIENT_LIGHT_LED, ECharacteristic.SENTIENT_LIGHT_LED_TX, "100");
            }
        });
    }



    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    private View getTableHead(Context context) {
        View row = inflate(context, R.layout.component_sentient_light_led_header, null);

        // Load layout
        TextView tvId = (TextView) row.findViewById(R.id.tvID);
        TextView tvComponentOne = (TextView) row.findViewById(R.id.tvComponentOne);
        TextView tvComponentTwo = (TextView) row.findViewById(R.id.tvComponentTwo);
        TextView tvComponentThree = (TextView) row.findViewById(R.id.tvComponentThree);

        tvId.setText(R.string.id);
        tvComponentOne.setText(R.string.cold);
        tvComponentTwo.setText(R.string.warm);
        tvComponentThree.setText(R.string.amber);

        return row;
    }

    private View getTableRow(Context context) {
        View row = inflate(context, R.layout.component_sentient_light_led_row, null);

        // Load layout
        Spinner spnnr = (Spinner) row.findViewById(R.id.spnnrLedID);
        EditText etComponentOne = (EditText) row.findViewById(R.id.etComponentOne);
        EditText etComponentTwo = (EditText) row.findViewById(R.id.etComponentOne);
        EditText etComponentThree = (EditText) row.findViewById(R.id.etComponentOne);


        List<String> list = new ArrayList<>();
        for (int i = 0; i < ledCount; i++) {
            list.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, list);
        spnnr.setAdapter(adapter);

        return row;
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