package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Map;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.parser.AccelGyroscope;
import de.interoberlin.merlot_android.model.service.Reading;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.view.diagrams.SpiderWebChart;

public class AccelerometerGyroscopeComponent extends TableLayout {
    // <editor-fold defaultstate="collapsed" desc="Members">

    // private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final float MIN_ACCELERATION = 0;
    private static final float MAX_ACCELERATION = 655;
    private static final float MIN_ANGULAR_SPEED = -250;
    private static final float MAX_ANGULAR_SPEED = 250;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public AccelerometerGyroscopeComponent(Context context) {
        super(context);
    }

    public AccelerometerGyroscopeComponent(Context context, BleDevice device) {
        super(context);
        inflate(context, R.layout.component_table, this);

        TableRow tr = (TableRow) findViewById(R.id.tr);

        Map<String, Reading> readings = device.getLatestReadings();
        String acceleration = readings.containsKey("acceleration") ? String.valueOf(readings.get("acceleration").value) : null;
        String gyroscope = readings.containsKey("angularSpeed") ? String.valueOf(readings.get("angularSpeed").value) : null;

        AccelGyroscope.Acceleration acc = acceleration != null ? new Gson().fromJson(acceleration, AccelGyroscope.Acceleration.class) : new AccelGyroscope.Acceleration();
        AccelGyroscope.AngularSpeed ang = gyroscope != null ? new Gson().fromJson(gyroscope, AccelGyroscope.AngularSpeed.class) : new AccelGyroscope.AngularSpeed();

        float minScreenWidth = context.getResources().getDimension(R.dimen.min_screen_width);
        float cardMargin = context.getResources().getDimension(R.dimen.card_margin);
        int itemsPerRow = context.getResources().getInteger(R.integer.items_per_row);
        int colCount = 2;

        int diagramDimen = (int) ((minScreenWidth / itemsPerRow) - (2*cardMargin)) / colCount ;

        tr.addView(new SpiderWebChart(context, diagramDimen, diagramDimen, R.color.colorAccent, -MIN_ACCELERATION, MAX_ACCELERATION - MIN_ACCELERATION, Arrays.asList(acc.x, acc.y, acc.z)));
        tr.addView(new SpiderWebChart(context, diagramDimen, diagramDimen, R.color.colorAccent, -MIN_ANGULAR_SPEED, MAX_ANGULAR_SPEED - MIN_ANGULAR_SPEED, Arrays.asList(ang.x, ang.y, ang.z)));
    }

    // </editor-fold>
}