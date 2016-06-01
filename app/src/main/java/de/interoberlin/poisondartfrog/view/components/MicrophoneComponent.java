package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.service.Reading;
import de.interoberlin.poisondartfrog.view.diagrams.CircleDiagram;

public class MicrophoneComponent extends TableLayout {
    // private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final String CHARA_NOISE = "noiseLevel";
    private static final float MIN_NOISE = 0;
    private static final float MAX_NOISE = 1023;

    // --------------------
    // Constructors
    // --------------------

    public MicrophoneComponent(Context context) {
        super(context);
    }

    public MicrophoneComponent(Context context, Activity activity, BleDevice device) {
        super(context);
        inflate(activity, R.layout.component_accelerometer_gyroscope, this);

        TableRow tr = (TableRow) findViewById(R.id.tr);

        Map<String, Reading> readings = device.getLatestReadings();
        String noise = readings.containsKey(CHARA_NOISE) ? String.valueOf(readings.get(CHARA_NOISE).value) : null;

        float noi = noise != null ? Float.valueOf(noise) : MIN_NOISE;

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int colCount = 3;
        int colWidth = (int) (displayWidth * 0.8 / colCount);
        int colHeight = (int) (displayWidth * 0.8 / colCount);

        tr.addView(new CircleDiagram(context, colWidth, colHeight, 0, 100, R.color.colorPrimary, R.color.colorPrimaryDark, MIN_NOISE, MAX_NOISE, noi));
    }
}