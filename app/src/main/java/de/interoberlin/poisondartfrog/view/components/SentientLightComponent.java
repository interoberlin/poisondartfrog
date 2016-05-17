package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TableRow;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.components.colorpicker.ColorPickerPalette;
import de.interoberlin.poisondartfrog.view.components.colorpicker.ColorPickerSwatch;

public class SentientLightComponent extends LinearLayout implements ColorPickerSwatch.OnColorSelectedListener {
    public static final String TAG = SentientLightComponent.class.getSimpleName();

    // --------------------
    // Constructors
    // --------------------

    public SentientLightComponent(Context context) {
        super(context);

        LayoutParams lp = new LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);
        setOrientation(VERTICAL);

        LinearLayout llData = new LinearLayout(context);
        ColorPickerPalette cpp = new ColorPickerPalette(context);
        cpp.init(4, this);
        cpp.drawPalette(context.getResources().getIntArray(R.array.colorSentient), 0);

        llData.addView(cpp);
        addView(llData);
    }

    @Override
    public void onColorSelected(int color) {

    }
}