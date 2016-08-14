package de.interoberlin.poisondartfrog.view.diagrams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.golem.Vector2;

public class SpiderWebChart extends ImageView {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = SpiderWebChart.class.getSimpleName();

    private final float MIN_VALUE = 0.05f;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public SpiderWebChart(Context context) {
        super(context);
    }

    /**
     * Generates a spider diagram by using {@code values}
     *
     * @param context  context
     * @param width    diagram width
     * @param height   diagram height
     * @param color    color
     * @param offset   display value when value is zero
     * @param maxValue maximum value
     * @param values   values
     */
    public SpiderWebChart(Context context, int width, int height, int color, Float offset, Float maxValue, List<Float> values) {
        super(context);

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        // Set diagram color
        Paint paintDiagram = new Paint();
        paintDiagram.setColor(ContextCompat.getColor(context, R.color.md_grey_400));
        paintDiagram.setStyle(Paint.Style.STROKE);
        paintDiagram.setStrokeWidth(context.getResources().getInteger(R.integer.chart_outline_width));

        // Set diagram dotted line color
        Paint paintDiagramDottedLine = new Paint();
        paintDiagramDottedLine.setColor(ContextCompat.getColor(context, R.color.md_grey_400));
        paintDiagramDottedLine.setStyle(Paint.Style.STROKE);
        paintDiagramDottedLine.setStrokeWidth(context.getResources().getInteger(R.integer.chart_outline_width));
        paintDiagramDottedLine.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

        // Set fill color
        Paint paintFill = new Paint();
        paintFill.setColor(ContextCompat.getColor(context, color));
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setAlpha(100);

        // Set stroke color
        Paint paintStroke = new Paint();
        paintStroke.setColor(ContextCompat.getColor(context, color));
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(context.getResources().getInteger(R.integer.chart_outline_width));

        // Clear canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (!values.isEmpty()) {
            // Draw diagram outline
            List<Vector2> diagramOutlinePoints = getDiagramOutlinePoints(width, height, values.size());
            Path diagramOutlinePath = new Path();
            diagramOutlinePath.moveTo(diagramOutlinePoints.get(0).getX(), diagramOutlinePoints.get(0).getY());
            for (Vector2 p : diagramOutlinePoints) {
                diagramOutlinePath.lineTo(p.getX(), p.getY());
            }

            diagramOutlinePath.close();
            diagramOutlinePath.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(diagramOutlinePath, paintDiagram);

            // Draw diagram outline
            List<Vector2> diagramDottedLinePoints = getDiagramOutlinePoints(width, height, values.size());
            Path diagramDottedLinePath = new Path();
            Vector2 center = new Vector2((float) width / 2, (float) height / 2);
            diagramDottedLinePath.moveTo(center.getX(), center.getY());
            for (Vector2 p : diagramDottedLinePoints) {
                diagramDottedLinePath.lineTo(p.getX(), p.getY());
                diagramDottedLinePath.moveTo(center.getX(), center.getY());
            }

            diagramOutlinePath.close();
            diagramOutlinePath.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(diagramDottedLinePath, paintDiagramDottedLine);

            // Draw value points
            List<Vector2> valuePoints = getValuePoints(width, height, offset, maxValue, values);
            Path valuePath = new Path();
            valuePath.moveTo(valuePoints.get(0).getX(), valuePoints.get(0).getY());
            for (Vector2 p : valuePoints) {
                valuePath.lineTo(p.getX(), p.getY());
            }

            valuePath.close();
            valuePath.setFillType(Path.FillType.EVEN_ODD);
            canvas.drawPath(valuePath, paintFill);
            canvas.drawPath(valuePath, paintStroke);
        }

        setImageBitmap(bmp);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Retrieves two-dimensional points limiting the chart according to {@code valueCount}
     *
     * @param width      canvas width
     * @param height     canvas height
     * @param valueCount number of values
     * @return list of two-dimensional points
     */
    private List<Vector2> getDiagramOutlinePoints(int width, int height, int valueCount) {
        int smallerSide = (width < height) ? width : height;

        Vector2 c = new Vector2((float) width / 2, (float) height / 2);
        List<Vector2> points = new ArrayList<>();

        for (int i = 0; i < valueCount; i++) {
            int angle = 360 / valueCount * i;

            Vector2 p = new Vector2();
            p.setX((float) Math.sin(Math.toRadians(angle)));
            p.setY((float) -Math.cos(Math.toRadians(angle)));
            p.normalize();
            p.scale((float) smallerSide / 2);
            p.add(c);

            points.add(p);
        }

        return points;
    }

    /**
     * Retrieves two-dimensional points according to {@code values}
     *
     * @param width    canvas width
     * @param height   canvas height
     * @param offset   offset
     * @param maxValue maximum values
     * @param values   list of values
     * @return list of two-dimensional points
     */
    private List<Vector2> getValuePoints(int width, int height, Float offset, Float maxValue, List<Float> values) {
        int smallerSide = (width < height) ? width : height;

        Vector2 c = new Vector2((float) width / 2, (float) height / 2);
        List<Vector2> points = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            int angle = 360 / values.size() * i;
            float value = (values.get(i) + offset + (maxValue * MIN_VALUE)) / maxValue;

            Vector2 p = new Vector2();
            p.setX((float) Math.sin(Math.toRadians(angle)));
            p.setY((float) -Math.cos(Math.toRadians(angle)));
            p.normalize();
            p.scale((float) smallerSide / 2);
            p.scale(value < maxValue ? value : maxValue);
            p.add(c);

            points.add(p);
        }

        return points;
    }

    // </editor-fold>
}
