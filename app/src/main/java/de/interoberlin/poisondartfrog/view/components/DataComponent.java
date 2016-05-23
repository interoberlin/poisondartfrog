package de.interoberlin.poisondartfrog.view.components;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Map;

import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.BleDevice;
import de.interoberlin.poisondartfrog.model.parser.AccelGyroscope;
import de.interoberlin.poisondartfrog.model.parser.BleDataParser;
import de.interoberlin.poisondartfrog.model.parser.LightColorProx;
import de.interoberlin.poisondartfrog.model.service.Reading;

public class DataComponent extends TableLayout {
    public static final String TAG = DataComponent.class.getSimpleName();

    // --------------------
    // Constructors
    // --------------------

    public DataComponent(Context context) {
        super(context);
    }

    public DataComponent(Context context, Activity activity, BleDevice device) {
        super(context);

        LayoutParams lp = new LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) context.getResources().getDimension(R.dimen.card_margin), 0, 0);
        setLayoutParams(lp);

        Map<String, Reading> readings = device.getLatestReadings();

        if (readings != null) {
            for (Map.Entry<String, Reading> r : readings.entrySet()) {
                LinearLayout llDataReading = new LinearLayout(context);
                TextView tvMeaning = new TextView(context);
                TextView tvValue = new TextView(context);

                String meaning = r.getKey();
                Reading reading = r.getValue();

                tvMeaning.setText(meaning);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Display3);
                }

                if (reading != null)
                    if (meaning.equals("acceleration")) {
                        if (reading.value.toString().startsWith("{")) {
                            AccelGyroscope.Acceleration acceleration = BleDataParser.getAcceleration(reading.value.toString());
                            tvValue.setText(acceleration.toString());
                        }
                    } else if (meaning.equals("angularSpeed")) {
                        if (reading.value.toString().startsWith("{")) {
                            AccelGyroscope.AngularSpeed angularSpeed = BleDataParser.getAngularSpeed(reading.value.toString());
                            tvValue.setText(angularSpeed.toString());
                        }
                    } else if (meaning.equals("color")) {
                        if (reading.value.toString().startsWith("{")) {
                            LightColorProx.Color color = BleDataParser.getColor(reading.value.toString());
                            tvValue.setText(color.toString());
                        }
                    } else {
                        tvValue.setText(reading.value.toString());
                    }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tvValue.setTextAppearance(android.R.style.TextAppearance_Material_Large);
                }

                llDataReading.setOrientation(VERTICAL);
                llDataReading.addView(tvMeaning);
                llDataReading.addView(tvValue);
                addView(llDataReading);
            }

        }

        setStretchAllColumns(true);
    }
}