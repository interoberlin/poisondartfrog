package de.interoberlin.poisondartfrog.view.diagrams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

public class CircleDiagram extends ImageView {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = CircleDiagram.class.getSimpleName();

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public CircleDiagram(Context context) {
        super(context);
    }

    /**
     * Generates a circle diagram by using {@code value}
     *
     * @param context          context
     * @param width            diagram width
     * @param height           diagram height
     * @param minRadius        minimum radius
     * @param maxRadius        maximum radius
     * @param minColorResource minimum color
     * @param maxColorResource maximum color
     * @param minValue         minimum value
     * @param maxValue         maximum value
     * @param value            value
     */
    public CircleDiagram(Context context, int width, int height, int minColorResource, int maxColorResource, float minRadius, float maxRadius, float minValue, float maxValue, Float value) {
        super(context);

        int minColor = ContextCompat.getColor(context, minColorResource);
        int maxColor = ContextCompat.getColor(context, maxColorResource);

        init(width, height, minColor, maxColor, minRadius, maxRadius, minValue, maxValue, value);
    }

    /**
     * Generates a circle diagram by using {@code color}
     *
     * @param context context
     * @param width   diagram width
     * @param height  diagram height
     * @param color   color
     */
    public CircleDiagram(Context context, int width, int height, int color) {
        super(context);

        init(width, height, color, color, 1.0f, 1.0f, 100.0f, 100.0f, 100.0f);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    private void init(int width, int height, int minColor, int maxColor, float minRadius, float maxRadius, float minValue, float maxValue, Float value) {
        final float MIN_VALUE = 0.05f;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        // Set fill color
        Paint paintMin = new Paint();
        paintMin.setColor(minColor);
        Paint paintMax = new Paint();
        paintMax.setColor(maxColor);

        Paint paint = getPaint(paintMin, paintMax, minValue, maxValue, value);
        paint.setStyle(Paint.Style.FILL);

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int smallerSide = (width < height) ? width : height;

        // Get radius
        float mValue = 1 / (maxValue - minValue);
        float nValue = -(1 / (maxValue - minValue)) * minValue;
        float pValue = mValue * value + nValue;

        maxRadius = (maxRadius > 1) ? 1 : (maxRadius < 0) ? 0 : maxRadius;
        minRadius = (minRadius > 1) ? 1 : (minRadius < 0) ? 0 : minRadius;

        float mRadius = maxRadius - minRadius;
        float pRadius = mRadius * ((pValue + MIN_VALUE) > 1.0f ? 1.0f : (pValue + MIN_VALUE)) + minRadius;

        int radius = (int) ((smallerSide / 2) * pRadius);

        // Draw circle
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        setImageBitmap(bmp);
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
    private Paint getPaint(Paint pMin, Paint pMax, float vMin, float vMax, float v) {
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
    private int getPaintComponent(int pMin, int pMax, float vMin, float vMax, float v) {
        float m = (pMax - pMin) / (vMax - vMin);
        float n = pMax - (m * vMax);
        return (int) ((m * v) + n);
    }

    // </editor-fold>
}
