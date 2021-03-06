package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableRow;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.components.colorpicker.ColorPickerPalette;
import de.interoberlin.poisondartfrog.view.components.colorpicker.ColorPickerSwatch;

public class SentientLightComponent extends LinearLayout implements ColorPickerSwatch.OnColorSelectedListener {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = SentientLightComponent.class.getSimpleName();

    private BleDevice device;

    private static final int COL_NUMBER = 5;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public SentientLightComponent(Context context) {
        super(context);
    }

    public SentientLightComponent(Context context, BleDevice device) {
        super(context);
        this.device = device;

        LayoutParams lp = new LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);
        setOrientation(VERTICAL);

        LinearLayout llData = new LinearLayout(context);
        ColorPickerPalette cpp = new ColorPickerPalette(context);

        cpp.init(COL_NUMBER, this);
        cpp.drawPalette(context.getResources().getIntArray(R.array.colorSentient), 0);

        llData.addView(cpp);
        addView(llData);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    @Override
    public void onColorSelected(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        Log.d(TAG, debugColorComponent("r",r) + ", " + debugColorComponent("g",g) + ", " + debugColorComponent("b",b) + ", ");

        device.write(EService.SENTIENT_LIGHT_LED, ECharacteristic.SENTIENT_LIGHT_LED_TX, new byte[]{intToByte(r), intToByte(g), intToByte(b)});
    }

    private byte intToByte(int value) {
        return Integer.valueOf(value).byteValue();
    }

    private String intToHex(int value) {
        return byteToHex(intToByte(value));
    }

    private String byteToHex(byte value) {
        return String.format("%02X ", value);
    }

    private String debugColorComponent(String component, int value) {
        return component + ":" + value + " (" + intToHex(value) + ")";
    }

    // </editor-fold>
}