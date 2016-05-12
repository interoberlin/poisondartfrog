package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.parser.AccelGyroscope;
import de.interoberlin.poisondartfrog.model.parser.BleDataParser;
import de.interoberlin.poisondartfrog.model.service.Reading;

public class DataComponent extends LinearLayout {
    public static final String TAG = DataComponent.class.getSimpleName();

    // --------------------
    // Constructors
    // --------------------

    public DataComponent(Context context) {
        super(context);
    }

    public DataComponent(Context context, BleDevice device, String meaning, Queue<Reading> readings) {
        super(context);

        LayoutParams lp = new LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);
        setOrientation(VERTICAL);

        if (readings != null) {
            LinearLayout llData = new LinearLayout(context);
            TextView tvMeaning = new TextView(context);
            TextView tvValue = new TextView(context);
            ValueLineChart vlcValues = new ValueLineChart(context);

            llData.setOrientation(VERTICAL);

            Reading latest = device.getLatestReadings().get(meaning);
            List<ValueLineSeries> series = new ArrayList<>();

            // Initialize value line series
            if (meaning.equals("acceleration") || meaning.equals("angularSpeed")) {
                series.add(new ValueLineSeries());
                series.add(new ValueLineSeries());
                series.add(new ValueLineSeries());
            } else {
                series.add(new ValueLineSeries());
            }

            try {
                // Iterate over readings
                for (Reading r : readings) {
                    String value = r.value.toString();

                    if (meaning.equals("acceleration")) {
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
                    } else if (meaning.equals("angularSpeed")) {
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
                    } else {
                        if (value.isEmpty()) {
                            series.get(0).addPoint(new ValueLinePoint("", 0.0f));
                        } else {
                            float v = Float.parseFloat(value);
                            series.get(0).addPoint(new ValueLinePoint("", v));
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, nfe.getMessage());
            }

            tvMeaning.setText(meaning);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Display3);
            }

            if (latest != null)
                if (meaning.equals("acceleration")) {
                    if (latest.value.toString().startsWith("{")) {
                        AccelGyroscope.Acceleration acceleration = BleDataParser.getAcceleration(latest.value.toString());
                        tvValue.setText(acceleration.toString());
                    }
                } else if (meaning.equals("angularSpeed")) {
                    if (latest.value.toString().startsWith("{")) {
                        AccelGyroscope.AngularSpeed angularSpeed = BleDataParser.getAngularSpeed(latest.value.toString());
                        tvValue.setText(angularSpeed.toString());
                    }
                } else {
                    tvValue.setText(latest.value.toString());
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Large);
            }

            llData.addView(tvMeaning);
            llData.addView(tvValue);

            // Set series colors
            for (int i = 0; i < series.size(); i++) {
                int[] colorsSeries = context.getResources().getIntArray(R.array.series_color);
                series.get(i).setColor(colorsSeries[i % colorsSeries.length]);
            }

            // Build value line chart
            if (!series.isEmpty()) {
                vlcValues.setUseDynamicScaling(true);
                vlcValues.setMinimumHeight(200);
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