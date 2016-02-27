package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.interoberlin.poisondartfrog.R;

public class ServicesComponent extends LinearLayout {
    private static final String TAG = ServicesComponent.class.getSimpleName();

    private Context context;
    private Activity activity;

    private List<BluetoothGattService> services;

    // --------------------
    // Constructors
    // --------------------

    public ServicesComponent(Context context, Activity activity) {
        super(context);
        this.context = context;
        this.activity = activity;
    }

    public ServicesComponent(Context context, Activity activity, List<BluetoothGattService> services) {
        super(context);
        this.context = context;
        this.activity = activity;

        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        for (BluetoothGattService service : services) {
            LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);

            TextView s = new TextView(activity);
            s.setText(service.getUuid().toString());
            s.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
            s.setTextAppearance(activity, android.R.style.TextAppearance_Small);
            s.setLayoutParams(lp);
            s.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            addView(s);

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                TextView c = new TextView(activity);
                c.setText("  " + characteristic.getUuid().toString());
                c.setTextAppearance(activity, android.R.style.TextAppearance_Small);
                c.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                c.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                addView(c);
            }
        }
    }
}