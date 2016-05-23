package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TableLayout;

import com.google.gson.Gson;

import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.parser.LightColorProx;
import de.interoberlin.poisondartfrog.model.service.Reading;

public class LightProximityComponent extends TableLayout {
    // private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final int MAX_LUMINOSITY = 150; // 4096
    private static final int MAX_PROXIMITY = 150; // 2047
    // private static final int MAX_RGB = 4096;

    // --------------------
    // Constructors
    // --------------------

    public LightProximityComponent(Context context) {
        super(context);
    }

    public LightProximityComponent(Context context, Activity activity, BleDevice device) {
        super(context);
        inflate(activity, R.layout.component_light_proximity, this);

        ImageView ivLuminosity = (ImageView) findViewById(R.id.ivLuminosity);
        ImageView ivProximity = (ImageView) findViewById(R.id.ivProximity);
        ImageView ivColor = (ImageView) findViewById(R.id.ivColor);

        Map<String, Reading> readings = device.getLatestReadings();
        String luminosity = readings.containsKey("luminosity") ? String.valueOf(readings.get("luminosity").value) : null;
        String proximity = readings.containsKey("proximity") ? String.valueOf(readings.get("proximity").value) : null;
        String color = readings.containsKey("color") ? String.valueOf(readings.get("color").value) : null;

        // String luminosity, String proximity, String color
        double lum = luminosity != null ? Double.parseDouble(luminosity) : 0.0;
        double prox = proximity != null ? Double.parseDouble(proximity) : 0.0;
        LightColorProx.Color col = color != null ? new Gson().fromJson(color, LightColorProx.Color.class) : new LightColorProx.Color(0,0,0);

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int columnCount = 3;
        int columnWidth = (int) (displayWidth * 0.8 / columnCount);
        int columnHeight = (int) (displayWidth * 0.8 / columnCount);

        Bitmap bmpLuminosity = Bitmap.createBitmap(columnWidth, columnHeight, Bitmap.Config.ARGB_8888);
        Canvas canvasLuminosity = new Canvas(bmpLuminosity);
        Paint paintLuminosity = new Paint();
        int rgbLuminosity = (int) (lum / MAX_LUMINOSITY * 255);
        paintLuminosity.setARGB(255, rgbLuminosity, rgbLuminosity, rgbLuminosity);
        canvasLuminosity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasLuminosity.drawCircle(columnWidth / 2, columnWidth / 2, (int) (columnWidth / 2 * 0.8), paintLuminosity);
        ivLuminosity.setImageBitmap(bmpLuminosity);

        Bitmap bmpProximity = Bitmap.createBitmap(columnWidth, columnHeight, Bitmap.Config.ARGB_8888);
        Canvas canvasProximity = new Canvas(bmpProximity);
        Paint paintProximity = new Paint();
        int rgbProximity = 200;
        int radiusProximity = (int) ((columnWidth / 2) * (1 -(prox / MAX_PROXIMITY)));
        paintProximity.setARGB(255, rgbProximity, rgbProximity, rgbProximity);
        canvasProximity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasProximity.drawCircle(columnWidth / 2, columnWidth / 2, radiusProximity, paintProximity);
        ivProximity.setImageBitmap(bmpProximity);

        Bitmap bmpColor = Bitmap.createBitmap(columnWidth, columnHeight, Bitmap.Config.ARGB_8888);
        Canvas canvasColor = new Canvas(bmpColor);
        Paint paintColor = new Paint();
        int radiusColor = (int) (columnWidth / 2 * 0.8);
        paintColor.setARGB(255, col.red, col.green, col.blue);
        canvasColor.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasColor.drawCircle(columnWidth / 2, columnWidth / 2, radiusColor, paintColor);
        ivColor.setImageBitmap(bmpColor);
    }
}