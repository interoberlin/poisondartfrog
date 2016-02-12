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

public class ReadingComponent extends LinearLayout {
    private static final String TAG = ReadingComponent.class.getCanonicalName();

    // --------------------
    // Constructors
    // --------------------

    public ReadingComponent(Context context, Activity activity, EReadingType readingType, double value, double radiusFactorMin, double radiusFactorMax) {
        this(context, activity, readingType, value, null, null, radiusFactorMin, radiusFactorMax);
    }

    public ReadingComponent(Context context, Activity activity, EReadingType readingType, double value, Paint paintMin, Paint paintMax) {
        this(context, activity, readingType, value, paintMin, paintMax, 1, 1);
    }

    private ReadingComponent(Context context, Activity activity, EReadingType readingType, double value, Paint paintMin, Paint paintMax, double radiusFactorMin, double radiusFactorMax) {
        super(context);

        inflate(context, R.layout.component_reading, this);

        TextView tvMeaning = (TextView) findViewById(R.id.tvMeaning);
        TextView tvValue = (TextView) findViewById(R.id.tvValue);
        ImageView ivVisualization = (ImageView) findViewById(R.id.ivVisualization);

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int dim = (displayWidth / 4);

        // Set values
        tvMeaning.setText(readingType.getMeaning());
        tvValue.setText(String.valueOf(value));

        // Use default value
        Paint defaultPaint = new Paint();
        defaultPaint.setARGB(255, 200, 200, 200);

        if (paintMin == null)
            paintMin = defaultPaint;

        if (paintMax == null)
            paintMax = defaultPaint;


        // Dynamic values
        Paint paint = getPaint(paintMin, paintMax, readingType.getMin(), readingType.getMax(), value);
        double radiusFactor = getMappedValue(radiusFactorMin, radiusFactorMax, readingType.getMin(), readingType.getMax(), value);

        Bitmap bmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(dim / 2, dim / 2, (int) ((dim / 2) * 0.8 * radiusFactor), paint);
        ivVisualization.setImageBitmap(bmp);
    }

    public ReadingComponent(Context context, Activity activity, EReadingType readingType, String value, Paint paint) {
        super(context);

        inflate(context, R.layout.component_reading, this);

        TextView tvMeaning = (TextView) findViewById(R.id.tvMeaning);
        TextView tvValue = (TextView) findViewById(R.id.tvValue);
        ImageView ivVisualization = (ImageView) findViewById(R.id.ivVisualization);

        // Get display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        int dim = (displayWidth / 5);

        // Set values
        tvMeaning.setText(readingType.getMeaning());
        tvValue.setText(value);

        Bitmap bmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawCircle(dim / 2, dim / 2, (dim / 2) * 0.8f, paint);
        ivVisualization.setImageBitmap(bmp);
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

        int a = (int) getMappedValue(aMin, aMax, vMin, vMax, v);
        int r = (int) getMappedValue(rMin, rMax, vMin, vMax, v);
        int g = (int) getMappedValue(gMin, gMax, vMin, vMax, v);
        int b = (int) getMappedValue(bMin, bMax, vMin, vMax, v);

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
    private double getMappedValue(double pMin, double pMax, int vMin, int vMax, double v) {
        double m = (pMax - pMin) / (vMax - vMin);
        double n = pMax - (m * vMax);
        return (m * v) + n;
    }
}
