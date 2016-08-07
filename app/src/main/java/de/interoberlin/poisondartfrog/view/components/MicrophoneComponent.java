package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.ble.BleDevice;
import de.interoberlin.poisondartfrog.model.service.Reading;
import de.interoberlin.poisondartfrog.view.diagrams.CircleDiagram;

public class MicrophoneComponent extends TableLayout {
    // <editor-fold defaultstate="collapsed" desc="Members">

    // private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final String CHARA_NOISE = "noiseLevel";
    private static final float MIN_NOISE = 0.0f;
    private static final float MAX_NOISE = 1023.0f;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public MicrophoneComponent(Context context) {
        super(context);
    }

    public MicrophoneComponent(Context context, BleDevice device) {
        super(context);
        inflate(context, R.layout.component_table, this);

        TableRow tr = (TableRow) findViewById(R.id.tr);

        Map<String, Reading> readings = device.getLatestReadings();
        String noise = readings.containsKey(CHARA_NOISE) ? String.valueOf(readings.get(CHARA_NOISE).value) : null;

        float noi = noise != null ? Float.valueOf(noise) : MIN_NOISE;

        float minScreenWidth = context.getResources().getDimension(R.dimen.min_screen_width);
        float cardMargin = context.getResources().getDimension(R.dimen.card_margin);
        int itemsPerRow = context.getResources().getInteger(R.integer.items_per_row);
        int colCount = 2;

        int diagramDimen = (int) ((minScreenWidth / itemsPerRow) - (2*cardMargin)) / colCount ;

        tr.addView(new CircleDiagram(context, diagramDimen, diagramDimen, R.color.colorPrimary, R.color.colorPrimaryDark, 0.0f, 1.0f, MIN_NOISE, MAX_NOISE, noi));
    }

    // </editor-fold>
}