package de.interoberlin.poisondartfrog.view.components;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.interoberlin.poisondartfrog.R;

public class LightProximityComponent extends LinearLayout {

    // --------------------
    // Constructors
    // --------------------

    public LightProximityComponent(Context context) {
        super(context);
    }

    public LightProximityComponent(Context context, String luminosity, String proximity, String color) {
        super(context);
        inflate(context, R.layout.component_light_promity, this);

        TextView tvLuminosity = (TextView) findViewById(R.id.tvLuminosity);
        TextView tvProximity = (TextView) findViewById(R.id.tvProximity);
        TextView tvColor = (TextView) findViewById(R.id.tvColor);

        // Set value
        tvLuminosity.setText(luminosity);
        tvProximity.setText(proximity);
        tvColor.setText(color);
    }
}
