package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;

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
            for (Map.Entry<String, String> r : device.getReadings().entrySet()) {
                // LayoutInflater inflater = LayoutInflater.from(context);
                // LinearLayout llData = (LinearLayout) inflater.inflate(R.layout.component_data, this);
                // TextView tvMeaning = (TextView) llData.findViewById(R.id.tvMeaning);
                // TextView tvValue = (TextView) llData.findViewById(R.id.tvValue);
                LinearLayout llData = new LinearLayout(context);
                TextView tvMeaning = new TextView(context);
                TextView tvValue= new TextView(context);

                llData.setOrientation(VERTICAL);

                tvMeaning.setText(r.getKey());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Display3);
                }

                tvValue.setText(r.getValue());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Large);
                }

                llData.addView(tvMeaning);
                llData.addView(tvValue);

                addView(llData);
            }
        }
    }
}