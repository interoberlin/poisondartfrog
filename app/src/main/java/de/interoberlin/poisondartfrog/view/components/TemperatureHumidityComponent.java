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
import android.widget.LinearLayout;
import android.widget.TextView;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.EReadingType;

public class TemperatureHumidityComponent extends LinearLayout {
    private static final String TAG = TemperatureHumidityComponent.class.getCanonicalName();

    private Activity activity;

    // --------------------
    // Constructors
    // --------------------

    public TemperatureHumidityComponent(Context context, Activity activity) {
        super(context);
        this.activity = activity;
    }

    public TemperatureHumidityComponent(Context context, Activity activity, String temperature, String humidity) {
        super(context);
        this.activity = activity;
        double temp = temperature != null ? Double.parseDouble(temperature) : 0.0;
        double hum = humidity != null ? Double.parseDouble(humidity) : 0.0;

        inflate(context, R.layout.component_temperature_humidity, this);

        TextView tvTemperature = (TextView) findViewById(R.id.tvTemperature);
        TextView tvHumidity = (TextView) findViewById(R.id.tvHumidity);

        ImageView ivTemperature = (ImageView) findViewById(R.id.ivTemperature);
        ImageView ivHumidity = (ImageView) findViewById(R.id.ivHumidity);

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int columnCount = 3;
        int columnWidth = (int) (displayWidth * 0.8 / columnCount);

        // Set values
        tvTemperature.setText(temperature);
        tvHumidity.setText(humidity);

        Bitmap bmpTemperature = Bitmap.createBitmap(columnWidth, columnWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasTemperature = new Canvas(bmpTemperature);
        Paint paintTemperatureMin = new Paint();
        paintTemperatureMin.setARGB(255, 0, 0, 255);
        Paint paintTemperatureMax = new Paint();
        paintTemperatureMax.setARGB(255, 255, 0, 0);
        Paint paintTemperature = getPaint(paintTemperatureMin, paintTemperatureMax, EReadingType.TEMPERATURE.getMin(), EReadingType.TEMPERATURE.getMax(), temp);
        canvasTemperature.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasTemperature.drawCircle(columnWidth / 2, columnWidth / 2, (int) (columnWidth / 2 * 0.8), paintTemperature);
        ivTemperature.setImageBitmap(bmpTemperature);

        Bitmap bmpHumidity = Bitmap.createBitmap(columnWidth, columnWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasHumidity = new Canvas(bmpHumidity);
        Paint paintHumidityMin = new Paint();
        paintHumidityMin.setARGB(0, 0, 0, 255);
        Paint paintHumidityMax = new Paint();
        paintHumidityMax.setARGB(255, 0, 0, 255);
        Paint paintHumidity = getPaint(paintHumidityMin, paintHumidityMax, EReadingType.HUMIDITY.getMin(), EReadingType.HUMIDITY.getMax(), hum);
        canvasHumidity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasHumidity.drawCircle(columnWidth / 2, columnWidth / 2, (int) (columnWidth / 2 * 0.8), paintHumidity);
        ivHumidity.setImageBitmap(bmpHumidity);
    }

    /**
     * Generates a paint based on a min and max color and a min and max value
     *
     * @param pMin minimal paint
     * @param pMax maximal paint
     * @param vMin minimal value
     * @param vMax maximal value
     * @param v    value
     * @return resulting paint
     */
    private Paint getPaint(Paint pMin, Paint pMax, int vMin, int vMax, double v) {
        if (v < vMin) return pMin;
        if (v > vMax) return pMax;

        int aMin = Color.alpha(pMin.getColor());
        int aMax = Color.alpha(pMax.getColor());
        int rMin = Color.red(pMin.getColor());
        int rMax = Color.red(pMax.getColor());
        int gMin = Color.green(pMin.getColor());
        int gMax = Color.green(pMax.getColor());
        int bMin = Color.blue(pMin.getColor());
        int bMax = Color.blue(pMax.getColor());

        int a = getPaintComponent(aMin, aMax, vMin, vMax, v);
        int r = getPaintComponent(rMin, rMax, vMin, vMax, v);
        int g = getPaintComponent(gMin, gMax, vMin, vMax, v);
        int b = getPaintComponent(bMin, bMax, vMin, vMax, v);

        Paint p = new Paint();

        p.setARGB(a, r, g, b);
        return p;
    }

    /**
     * Maps a {@code value} from {@code vMin} to {@code vMax} to a range from {@code pMin} to {@code pMax}
     *
     * @param pMin min output value
     * @param pMax max output value
     * @param vMin min input value
     * @param vMax max input value
     * @param v    input value
     * @return mapped value
     */
    private int getPaintComponent(int pMin, int pMax, int vMin, int vMax, double v) {
        int m = (pMax - pMin) / (vMax - vMin);
        int n = pMax - (m * vMax);
        return (int) (m * v) + n;
    }
}
