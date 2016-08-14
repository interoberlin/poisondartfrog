package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;

import java.util.Map;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.parser.LightColorProx;
import de.interoberlin.merlot_android.model.service.Reading;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.diagrams.CircleDiagram;

public class LightProximityComponent extends TableLayout {
    // <editor-fold defaultstate="collapsed" desc="Members">

    // private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final String CHARA_LUMINOSITY = "luminosity";
    private static final String CHARA_PROXIMITY = "proximity";
    private static final String CHARA_COLOR = "color";

    private static final float MIN_LUMINOSITY = 0.0f;
    private static final float MAX_LUMINOSITY = 150.0f; // 4096
    private static final float MIN_PROXIMITY = 0.0f;
    private static final float MAX_PROXIMITY = 150.0f; // 2047

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public LightProximityComponent(Context context) {
        super(context);
    }

    public LightProximityComponent(Context context, BleDevice device) {
        super(context);
        inflate(context, R.layout.component_table, this);

        TableRow tr = (TableRow) findViewById(R.id.tr);

        Map<String, Reading> readings = device.getLatestReadings();
        String luminosity = readings.containsKey(CHARA_LUMINOSITY) ? String.valueOf(readings.get(CHARA_LUMINOSITY).value) : null;
        String proximity = readings.containsKey(CHARA_PROXIMITY) ? String.valueOf(readings.get(CHARA_PROXIMITY).value) : null;
        String color = readings.containsKey(CHARA_COLOR) ? String.valueOf(readings.get(CHARA_COLOR).value) : null;

        float lum = luminosity != null ? Float.valueOf(luminosity) : MIN_LUMINOSITY;
        float pro = proximity != null ? Float.valueOf(proximity) : MIN_PROXIMITY;
        LightColorProx.Color col = color != null ? new Gson().fromJson(color, LightColorProx.Color.class) : new LightColorProx.Color(0, 0, 0);

        float minScreenWidth = context.getResources().getDimension(R.dimen.min_screen_width);
        float cardMargin = context.getResources().getDimension(R.dimen.card_margin);
        int itemsPerRow = context.getResources().getInteger(R.integer.items_per_row);
        int colCount = 3;

        int diagramDimen = (int) ((minScreenWidth / itemsPerRow) - (2*cardMargin)) / colCount ;

        tr.addView(new CircleDiagram(context, diagramDimen, diagramDimen, R.color.md_grey_200, R.color.md_grey_900, 1.0f, 1.0f, MIN_LUMINOSITY, MAX_LUMINOSITY, lum));
        tr.addView(new CircleDiagram(context, diagramDimen, diagramDimen, R.color.md_grey_400, R.color.md_grey_400, 1.0f, 0.0f, MIN_PROXIMITY, MAX_PROXIMITY, pro));
        tr.addView(new CircleDiagram(context, diagramDimen, diagramDimen, col.toRgb()));
    }

    // </editor-fold>
}