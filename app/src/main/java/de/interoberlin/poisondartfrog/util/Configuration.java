package de.interoberlin.poisondartfrog.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private static final String TAG = Configuration.class.getSimpleName();
    private static final String GRADLE_PROPERTIES_FILE = "frog.properties";

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static String getProperty(Context c, String property) {
        try {
            InputStream inputStream = c.getAssets().open(GRADLE_PROPERTIES_FILE);
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(property);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("unused")
    public static int getIntProperty(Context c, String property) {
        try {
            String propertyValue = getProperty(c, property);
            return Integer.parseInt(propertyValue != null ? propertyValue : "0");
        } catch (NumberFormatException nfe) {
            Log.e(TAG, nfe.getMessage());
            return 0;
        }
    }

    // </editor-fold>
}
