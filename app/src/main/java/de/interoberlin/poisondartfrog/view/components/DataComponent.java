package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.txusballesteros.SnakeView;

import java.util.Map;
import java.util.Queue;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.service.Reading;

public class DataComponent extends LinearLayout {
    public static final String TAG = DataComponent.class.getSimpleName();

    // --------------------
    // Constructors
    // --------------------

    public DataComponent(Context context) {
        super(context);
    }

    public DataComponent(Context context, BleDevice device) {
        super(context);

        LayoutParams lp = new LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);

        if (device.getReadings() != null) {
            for (Map.Entry<String, Queue<Reading>> e : device.getReadings().entrySet()) {
                LinearLayout llData = new LinearLayout(context);
                TextView tvMeaning = new TextView(context);
                TextView tvValue = new TextView(context);
                SnakeView sv = new SnakeView(context);

                llData.setOrientation(VERTICAL);

                Queue<Reading> q = e.getValue();
                Reading latest = device.getLatestReadings().get(e.getKey());
                boolean numeric;


                sv.setMinValue(0);
                sv.setMaxValue(1024);
                sv.setMinimumHeight(200);
                sv.setMaximumNumberOfValues(10);

                try {
                    for (Reading r : q) {
                        float value = Float.parseFloat(r.value.toString());
                        sv.addValue(value);
                    }
                    numeric = true;
                } catch (NumberFormatException nfe) {
                    numeric = false;
                }

                tvMeaning.setText(e.getKey());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Display3);
                }

                if (latest != null)
                    tvValue.setText(latest.value.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Large);
                }

                llData.addView(tvMeaning);
                llData.addView(tvValue);
                if (numeric)
                    llData.addView(sv);
                addView(llData);
            }
        }
    }
}