package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;

import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.parser.LightColorProx;
import de.interoberlin.poisondartfrog.model.service.Reading;
import de.interoberlin.poisondartfrog.view.diagrams.CircleDiagram;

public class LightProximityComponent extends TableLayout {
    // private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final String CHARA_LUMINOSITY = "luminosity";
    private static final String CHARA_PROXIMITY = "proximity";
    private static final String CHARA_COLOR = "color";

    private static final float MIN_LUMINOSITY = 0.0f;
    private static final float MAX_LUMINOSITY = 150.0f; // 4096
    private static final float MIN_PROXIMITY = 0.0f;
    private static final float MAX_PROXIMITY = 150.0f; // 2047

    // --------------------
    // Constructors
    // --------------------

    public LightProximityComponent(Context context) {
        super(context);
    }

    public LightProximityComponent(Context context, Activity activity, BleDevice device) {
        super(context);
        inflate(activity, R.layout.component_table, this);

        TableRow tr = (TableRow) findViewById(R.id.tr);

        Map<String, Reading> readings = device.getLatestReadings();
        String luminosity = readings.containsKey(CHARA_LUMINOSITY) ? String.valueOf(readings.get(CHARA_LUMINOSITY).value) : null;
        String proximity = readings.containsKey(CHARA_PROXIMITY) ? String.valueOf(readings.get(CHARA_PROXIMITY).value) : null;
        String color = readings.containsKey(CHARA_COLOR) ? String.valueOf(readings.get(CHARA_COLOR).value) : null;

        float lum = luminosity != null ? Float.valueOf(luminosity) : MIN_LUMINOSITY;
        float pro = proximity != null ? Float.valueOf(proximity) : MIN_PROXIMITY;
        LightColorProx.Color col = color != null ? new Gson().fromJson(color, LightColorProx.Color.class) : new LightColorProx.Color(0, 0, 0);

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int colCount = 3;
        int colWidth = (int) (displayWidth * 0.8 / colCount);
        int colHeight = (int) (displayWidth * 0.8 / colCount);

        tr.addView(new CircleDiagram(context, colWidth, colHeight, R.color.md_grey_200, R.color.md_grey_900, 1.0f, 1.0f, MIN_LUMINOSITY, MAX_LUMINOSITY, lum));
        tr.addView(new CircleDiagram(context, colWidth, colHeight, R.color.md_grey_400, R.color.md_grey_400, 1.0f, 0.0f, MIN_PROXIMITY, MAX_PROXIMITY, pro));
        tr.addView(new CircleDiagram(context, colWidth, colHeight, col.toRgb()));
    }
}