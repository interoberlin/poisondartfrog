package de.interoberlin.poisondartfrog.view.diagrams;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import de.interoberlin.poisondartfrog.R;


public class BatteryDiagram extends ImageView {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BatteryDiagram.class.getSimpleName();

    private int width;
    private int height;
    private int minColor;
    private int maxColor;

    private Paint paintMin;
    private Paint paintMax;

    private int value;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public BatteryDiagram(Context context) {
        super(context);

        paintMin = new Paint();
        paintMax = new Paint();
    }

    public BatteryDiagram(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.width = (int) getResources().getDimension(R.dimen.battery_icon_size);
        this.height = (int) getResources().getDimension(R.dimen.battery_icon_size);

        this.width = (int) getResources().getDimension(R.dimen.battery_icon_size);
        this.height = (int) getResources().getDimension(R.dimen.battery_icon_size);
        this.paintMin = new Paint();
        this.paintMax = new Paint();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatteryDiagram,
                0, 0);

        try {
            this.minColor = a.getColor(R.styleable.BatteryDiagram_minColor, ContextCompat.getColor(getContext(), R.color.md_black_1000));
            this.maxColor = a.getColor(R.styleable.BatteryDiagram_maxColor, ContextCompat.getColor(getContext(), R.color.md_black_1000));
            this.value = a.getInteger(R.styleable.BatteryDiagram_value, 0);
        } finally {
            a.recycle();
        }
    }

    /**
     * Generates a circle diagram by using {@code value}
     *
     * @param context          context
     * @param width            diagram width
     * @param height           diagram height
     * @param minColorResource minimum color
     * @param maxColorResource maximum color
     * @param value            value
     */
    public BatteryDiagram(Context context, int width, int height, int minColorResource, int maxColorResource, int value) {
        super(context);

        this.width = width;
        this.height = height;
        this.minColor = ContextCompat.getColor(context, minColorResource);
        this.maxColor = ContextCompat.getColor(context, maxColorResource);
        this.value = value;
    }

    // </editor-fold>

    // --------------------
    // Methods - Callbacks
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callbacks">

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float MIN_VALUE = 0.0f;
        final float MAX_VALUE = 100.0f;

        // Dimension
        int smallerSide = (width < height) ? width : height;

        // Set fill color
        paintMin.setColor(minColor);
        paintMax.setColor(maxColor);

        Paint paintStroke = getPaint(paintMin, paintMax, MIN_VALUE, MAX_VALUE, value);
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(smallerSide * 0.04f);

        Paint paintFill = getPaint(paintMin, paintMax, MIN_VALUE, MAX_VALUE, value);
        paintFill.setStyle(Paint.Style.FILL);

        float mainLeft = smallerSide * 0.28f;
        float mainTop = smallerSide * 0.16f;
        float mainRight = smallerSide * (1 - 0.28f);
        float mainBottom = smallerSide * (1 - 0.085f);
        float mainRoundedCorner = smallerSide * 0.05f;

        float smallLeft = smallerSide * 0.4f;
        float smallTop = smallerSide * 0.085f;
        float smallRight = smallerSide * (1 - 0.4f);
        float smallBottom = smallerSide * 0.16f;

        float mValue = - (1 / (MIN_VALUE - MAX_VALUE));
        float nValue = - (1 / (MIN_VALUE - MAX_VALUE)) * MIN_VALUE;
        float pValue = mValue * value + nValue;

        float mFillLevel = mainBottom - mainTop;
        float pFillLevel = mFillLevel * (1.0f - pValue) + mainTop;

        // Draw rect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(mainLeft, mainTop, mainRight, mainBottom, mainRoundedCorner, mainRoundedCorner, paintStroke);
            canvas.drawRoundRect(mainLeft, pFillLevel, mainRight, mainBottom, mainRoundedCorner, mainRoundedCorner, paintFill);
        } else {
            canvas.drawRect(mainLeft, mainTop, mainRight, mainBottom, paintStroke);
        }

        // Fill
        canvas.drawRect(smallLeft, smallTop, smallRight, smallBottom, paintFill);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

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

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public void setValue(int value) {
        this.value = value;
    }

    // </editor-fold>
}
