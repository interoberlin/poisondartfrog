package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.config.ECharacteristic;

public class DataComponent extends LinearLayout {
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

        if (device.getCharacteristics().contains(ECharacteristic.DATA.getId())) {
            // String value = device.getCharacteristics().get(ECharacteristic.DATA.getId()).getValue();

            TextView tvData = new TextView(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvData.setTextAppearance(android.R.style.TextAppearance_Large);
            } else {
                tvData.setTextAppearance(context, android.R.style.TextAppearance_Large);
            }

            addView(tvData);
        }
    }
}