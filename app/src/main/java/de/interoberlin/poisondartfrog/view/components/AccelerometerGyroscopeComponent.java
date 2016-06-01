package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.parser.AccelGyroscope;
import de.interoberlin.poisondartfrog.model.service.Reading;
import de.interoberlin.poisondartfrog.view.diagrams.SpiderWebChart;

public class AccelerometerGyroscopeComponent extends TableLayout {
    private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final float MIN_ACCELERATION = 0;
    private static final float MAX_ACCELERATION = 655;
    private static final float MIN_ANGULAR_SPEED = -250;
    private static final float MAX_ANGULAR_SPEED = 250;

    // --------------------
    // Constructors
    // --------------------

    public AccelerometerGyroscopeComponent(Context context) {
        super(context);
    }

    public AccelerometerGyroscopeComponent(Context context, Activity activity, BleDevice device) {
        super(context);
        inflate(activity, R.layout.component_accelerometer_gyroscope, this);

        TableRow tr = (TableRow) findViewById(R.id.tr);

        Map<String, Reading> readings = device.getLatestReadings();
        String acceleration = readings.containsKey("acceleration") ? String.valueOf(readings.get("acceleration").value) : null;
        String gyroscope = readings.containsKey("angularSpeed") ? String.valueOf(readings.get("angularSpeed").value) : null;

        AccelGyroscope.Acceleration acc = acceleration != null ? new Gson().fromJson(acceleration, AccelGyroscope.Acceleration.class) : new AccelGyroscope.Acceleration();
        AccelGyroscope.AngularSpeed ang = gyroscope != null ? new Gson().fromJson(gyroscope, AccelGyroscope.AngularSpeed.class) : new AccelGyroscope.AngularSpeed();

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int colCount = 2;
        int colWidth = (int) (displayWidth * 0.8 / colCount);
        int colHeight = (int) (displayWidth * 0.8 / colCount);

        tr.addView(new SpiderWebChart(context, colWidth, colHeight, R.color.colorAccent, -MIN_ACCELERATION, MAX_ACCELERATION - MIN_ACCELERATION, Arrays.asList(acc.x, acc.y, acc.z)));
        tr.addView(new SpiderWebChart(context, colWidth, colHeight, R.color.colorAccent, -MIN_ANGULAR_SPEED, MAX_ANGULAR_SPEED - MIN_ANGULAR_SPEED, Arrays.asList(ang.x, ang.y, ang.z)));
    }

    // --------------------
    // Methods
    // --------------------


}