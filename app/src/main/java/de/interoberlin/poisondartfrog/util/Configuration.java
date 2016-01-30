package de.interoberlin.poisondartfrog.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private static final String TAG = Configuration.class.getCanonicalName();
    private static final String GRADLE_PROPERTIES_FILE = "frog.properties";

    public static String getProperty(Context c, String property) {
        try {
            InputStream inputStream = c.getAssets().open(GRADLE_PROPERTIES_FILE);
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(property);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return null;
    }

    public static int getIntProperty(Context c, String property) {
        try {
            String propertyValue = getProperty(c, property);
            return Integer.parseInt(propertyValue != null ? propertyValue : "0");
        } catch (NumberFormatException nfe) {
            Log.e(TAG, nfe.getMessage());
            return 0;
        }
    }
}
