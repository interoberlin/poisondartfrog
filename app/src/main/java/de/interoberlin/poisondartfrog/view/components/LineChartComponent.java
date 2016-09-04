package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableRow;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.parser.BleDataParser;
import de.interoberlin.merlot_android.model.parser.data.AccelGyroscope;
import de.interoberlin.merlot_android.model.parser.data.LightColorProx;
import de.interoberlin.merlot_android.model.service.Reading;
import de.interoberlin.poisondartfrog.R;

public class LineChartComponent extends LinearLayout {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = DataComponent.class.getSimpleName();

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public LineChartComponent(Context context) {
        super(context);
    }

    public LineChartComponent(Context context, BleDevice device) {
        super(context);

        LayoutParams lp = new LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);
        setOrientation(VERTICAL);

        for (Map.Entry<String, Queue<Reading>> reading : device.getReadings().entrySet()) {
            if (reading != null) {
                String meaning = reading.getKey();
                Queue<Reading> queue = reading.getValue();

                LinearLayout llData = new LinearLayout(context);
                ValueLineChart vlcValues = new ValueLineChart(context);
                llData.setOrientation(VERTICAL);

                List<ValueLineSeries> series = new ArrayList<>();

                // Initialize value line series
                if (meaning.equals("acceleration") || meaning.equals("angularSpeed") || meaning.equals("color")) {
                    series.add(new ValueLineSeries());
                    series.add(new ValueLineSeries());
                    series.add(new ValueLineSeries());
                } else {
                    series.add(new ValueLineSeries());
                }

                try {
                    // Iterate over readings
                    for (Reading r : queue) {
                        String value = r.value.toString();

                        switch (meaning) {
                            case "acceleration": {
                                if (value.isEmpty()) {
                                    series.get(0).addPoint(new ValueLinePoint("", 0.0f));
                                    series.get(1).addPoint(new ValueLinePoint("", 0.0f));
                                    series.get(2).addPoint(new ValueLinePoint("", 0.0f));
                                } else if (value.startsWith("{")) {
                                    AccelGyroscope.Acceleration acceleration = BleDataParser.getAcceleration(value);

                                    series.get(0).addPoint(new ValueLinePoint("", acceleration.x));
                                    series.get(1).addPoint(new ValueLinePoint("", acceleration.y));
                                    series.get(2).addPoint(new ValueLinePoint("", acceleration.z));
                                }
                                break;
                            }
                            case "angularSpeed": {
                                if (value.isEmpty()) {
                                    series.get(0).addPoint(new ValueLinePoint("", 0.0f));
                                    series.get(1).addPoint(new ValueLinePoint("", 0.0f));
                                    series.get(2).addPoint(new ValueLinePoint("", 0.0f));
                                } else if (value.startsWith("{")) {
                                    AccelGyroscope.AngularSpeed angularSpeed = BleDataParser.getAngularSpeed(value);

                                    series.get(0).addPoint(new ValueLinePoint("", angularSpeed.x));
                                    series.get(1).addPoint(new ValueLinePoint("", angularSpeed.y));
                                    series.get(2).addPoint(new ValueLinePoint("", angularSpeed.z));
                                }
                                break;
                            }
                            case "color": {
                                if (value.isEmpty()) {
                                    series.get(0).addPoint(new ValueLinePoint("", 0.0f));
                                    series.get(1).addPoint(new ValueLinePoint("", 0.0f));
                                    series.get(2).addPoint(new ValueLinePoint("", 0.0f));
                                } else if (value.startsWith("{")) {
                                    LightColorProx.Color color = BleDataParser.getColor(value);

                                    series.get(0).addPoint(new ValueLinePoint("", color.red));
                                    series.get(1).addPoint(new ValueLinePoint("", color.green));
                                    series.get(2).addPoint(new ValueLinePoint("", color.blue));
                                }
                                break;
                            }
                            default: {
                                if (value.isEmpty()) {
                                    series.get(0).addPoint(new ValueLinePoint("", 0.0f));
                                } else {
                                    float v = Float.parseFloat(value);
                                    series.get(0).addPoint(new ValueLinePoint("", v));
                                }
                            }
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, nfe.getMessage());
                }

                // Set series colors
                for (int i = 0; i < series.size(); i++) {
                    int[] colorsSeries = context.getResources().getIntArray(R.array.colorSeries);
                    series.get(i).setColor(colorsSeries[i % colorsSeries.length]);
                }

                // Build value line chart
                if (!series.isEmpty()) {
                    vlcValues.setUseDynamicScaling(true);
                    vlcValues.setMinimumHeight((int) context.getResources().getDimension(R.dimen.linechart_height));
                    vlcValues.setTop(50);
                    vlcValues.setUseCubic(true);

                    for (ValueLineSeries vls : series) {
                        vlcValues.addSeries(vls);
                    }

                    llData.addView(vlcValues);
                }

                addView(llData);
            }
        }
    }

    // </editor-fold>
}