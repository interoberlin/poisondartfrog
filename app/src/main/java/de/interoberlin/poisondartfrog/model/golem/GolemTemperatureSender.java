package de.interoberlin.poisondartfrog.model.golem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.poisondartfrog.R;
import de.interoberlin.poisondartfrog.model.tasks.EHttpParameter;
import de.interoberlin.poisondartfrog.model.tasks.HttpGetTask;

public class GolemTemperatureSender {

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static void sendLocation(Context context, HttpGetTask.OnCompleteListener ocListener, BleDevice device, Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();

        String url = prefs.getString(res.getString(R.string.pref_golem_temperature_url), null);

        if (device.getLatestReadings() != null && device.getLatestReadings().containsKey("temperature")) {
            String temperature = device.getLatestReadings().get("temperature").value.toString();

            // Add parameters
            Map<EHttpParameter, String> parameters = new LinkedHashMap<>();

            parameters.put(EHttpParameter.DBG, String.valueOf(prefs.getBoolean(res.getString(R.string.pref_golem_temperature_dbg), true) ? "1" : "0"));
            parameters.put(EHttpParameter.TOKEN, prefs.getString(res.getString(R.string.pref_golem_temperature_token), null));
            parameters.put(EHttpParameter.TYPE, prefs.getString(res.getString(R.string.pref_golem_temperature_type), res.getString(R.string.pref_default_golem_temperature_type)));
            parameters.put(EHttpParameter.COUNTRY, prefs.getString(res.getString(R.string.pref_golem_temperature_country), null));
            parameters.put(EHttpParameter.CITY, prefs.getString(res.getString(R.string.pref_golem_temperature_city), null));
            parameters.put(EHttpParameter.ZIP, prefs.getString(res.getString(R.string.pref_golem_temperature_zip), null));
            parameters.put(EHttpParameter.TEMP, temperature);

            // Add location parameters
            if (location != null) {
                parameters.put(EHttpParameter.LAT, String.valueOf(round(location.getLatitude(), 2)));
                parameters.put(EHttpParameter.LONG, String.valueOf(round(location.getLongitude(), 2)));
            }

            // Call webservice
            try {
                HttpGetTask httpGetTask = new HttpGetTask(ocListener, url);
                httpGetTask.execute(parameters).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // </editor-fold>
}
