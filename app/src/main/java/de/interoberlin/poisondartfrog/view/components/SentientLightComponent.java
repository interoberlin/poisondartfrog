package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TableRow;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.activities.DevicesActivity;
import de.interoberlin.poisondartfrog.view.components.colorpicker.ColorPickerPalette;
import de.interoberlin.poisondartfrog.view.components.colorpicker.ColorPickerSwatch;

public class SentientLightComponent extends LinearLayout implements ColorPickerSwatch.OnColorSelectedListener {
    public static final String TAG = SentientLightComponent.class.getSimpleName();

    private Activity activity;

    // --------------------
    // Constructors
    // --------------------

    public SentientLightComponent(Context context, Activity activity) {
        super(context);
        this.activity = activity;

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
        if (activity instanceof DevicesActivity) {
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color >> 0) & 0xFF;
            ((DevicesActivity) activity).snack("r:" + r + " , g:" + g + ", b:" + b);
        }
    }
}