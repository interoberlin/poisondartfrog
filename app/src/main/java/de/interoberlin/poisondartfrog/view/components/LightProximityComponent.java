package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.reading.LightProximity;

public class LightProximityComponent extends LinearLayout {
    private static final String TAG = LightProximityComponent.class.getCanonicalName();

    private static final int MAX_LUMINOSITY = 150; // 4096
    private static final int MAX_PROXIMITY = 150; // 2047
    private static final int MAX_RGB = 4096;

    private Activity activity;

    // --------------------
    // Constructors
    // --------------------

    public LightProximityComponent(Context context, Activity activity) {
        super(context);
        this.activity = activity;
    }

    public LightProximityComponent(Context context, Activity activity, String luminosity, String proximity, String color) {
        super(context);
        this.activity = activity;
        double prox = luminosity != null ? Double.parseDouble(luminosity) : 0.0;
        double lum = luminosity != null ? Double.parseDouble(luminosity) : 0.0;
        LightProximity.Color col = color != null ? new Gson().fromJson(color, LightProximity.Color.class) : new LightProximity.Color();

        inflate(context, R.layout.component_light_promity, this);

        TextView tvLuminosity = (TextView) findViewById(R.id.tvLuminosity);
        TextView tvProximity = (TextView) findViewById(R.id.tvProximity);
        TextView tvColor = (TextView) findViewById(R.id.tvColor);

        ImageView ivLuminosity = (ImageView) findViewById(R.id.ivLuminosity);
        ImageView ivProximity = (ImageView) findViewById(R.id.ivProximity);
        ImageView ivColor = (ImageView) findViewById(R.id.ivColor);

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int columnCount = 3;
        int columnWidth = (int) (displayWidth * 0.8 / columnCount);

        // Set values
        tvLuminosity.setText(luminosity);
        tvProximity.setText(proximity);
        tvColor.setText(col.getRed() + " " + col.getGreen() + " " + col.getBlue());

        Bitmap bmpLuminosity = Bitmap.createBitmap(columnWidth, columnWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasLuminosity = new Canvas(bmpLuminosity);
        Paint paintLuminosity = new Paint();
        int rgbLuminosity = (int) (lum / MAX_LUMINOSITY * 255);
        paintLuminosity.setARGB(255, rgbLuminosity, rgbLuminosity, rgbLuminosity);
        canvasLuminosity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasLuminosity.drawCircle(columnWidth / 2, columnWidth / 2, (int) (columnWidth / 2 * 0.8), paintLuminosity);
        ivLuminosity.setImageBitmap(bmpLuminosity);

        Bitmap bmpProximity = Bitmap.createBitmap(columnWidth, columnWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasProximity = new Canvas(bmpProximity);
        Paint paintProximity = new Paint();
        int rgbProximity = 200;
        int radiusProximity = (int) ((columnWidth / 2) * (1 -(prox / MAX_PROXIMITY)));
        paintProximity.setARGB(255, rgbProximity, rgbProximity, rgbProximity);
        canvasProximity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasProximity.drawCircle(columnWidth / 2, columnWidth / 2, radiusProximity, paintProximity);
        ivProximity.setImageBitmap(bmpProximity);

        Bitmap bmpColor = Bitmap.createBitmap(columnWidth, columnWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasColor = new Canvas(bmpColor);
        Paint paintColor = new Paint();
        int radiusColor = (int) (columnWidth / 2 * 0.8);
        paintColor.setARGB(255, col.getRed(), col.getGreen(), col.getBlue());
        canvasColor.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasColor.drawCircle(columnWidth / 2, columnWidth / 2, radiusColor, paintColor);
        ivColor.setImageBitmap(bmpColor);
    }
}
