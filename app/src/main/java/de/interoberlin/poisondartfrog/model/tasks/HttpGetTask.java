package de.interoberlin.poisondartfrog.model.tasks;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class HttpGetTask extends AsyncTask<Map<EHttpParameter, String>, Void, String> {
    public static final String TAG = HttpGetTask.class.toString();

    private static final int PERMISSION_REQUEST_INTERNET = 3;

    private static final String HTTP_METHOD = "GET";
    private static final String ENCODING = "UTF-8";
    private static final String CONTENT_TYPE = "text/plain";
    private static final int RESPONSE_CODE_OKAY = 200;

    private static Activity activity;
    private static OnCompleteListener ocListener;
    private static String url;

    // --------------------
    // Constructors
    // --------------------

    public HttpGetTask(Activity activity, OnCompleteListener ocListener, String url) {
        HttpGetTask.activity = activity;
        HttpGetTask.ocListener = ocListener;
        HttpGetTask.url = url;
    }

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Map<EHttpParameter, String>... parameters) {
        Map<EHttpParameter, String> values = parameters[0];

        if (values != null && !values.isEmpty()) {
            try {
                requestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_INTERNET);
                return callURL(values);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result != null) {
            Log.d(TAG, result);

            if (ocListener != null)
                ocListener.onHttpGetExecuted(result);
        }
    }

    // --------------------
    // Methods
    // --------------------

    /**
     *
     */
    private static String callURL(Map<EHttpParameter, String> values) throws Exception {
        // Connection
        HttpURLConnection con = (HttpURLConnection) new URL(url + getParamString(values)).openConnection();

        // Request header
        con.setRequestMethod(HTTP_METHOD);
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", CONTENT_TYPE + "; charset=" + ENCODING);
        con.setRequestProperty("Accept-Charset", ENCODING);

        Log.i(TAG, url + getParamString(values));

        // Execute request
        try {
            if (con.getResponseCode() != RESPONSE_CODE_OKAY) {
                Log.e(TAG, "Error after executing http call");
                Log.e(TAG, "ResponseCode : " + con.getResponseCode());
                Log.e(TAG, "ResponseMethod : " + con.getRequestMethod());

                for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
                    Log.e(TAG, entry.getKey() + " : " + entry.getValue());
                }
                throw new Exception("Error after executing http call");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;

            // Evaluate response
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (response.toString().startsWith("ArgumentException")) {
                Log.e(TAG, response.toString());
                return null;
            } else {
                return String.valueOf(con.getResponseCode());
            }
        } finally {
            con.disconnect();
        }
    }

    public static String getParamString(Map<EHttpParameter, String> values) {
        StringBuilder sb = new StringBuilder();

        sb.append("?");

        // Append params
        for (Map.Entry<EHttpParameter, String> v : values.entrySet()) {
            try {
                if (v.getValue() != null)
                    sb.append(v.getKey().getParam()).append("=").append(URLEncoder.encode(v.getValue(), ENCODING)).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // Remove trailing ampersand
        String paramString = "";
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
            paramString = sb.substring(0, sb.length() - 1);
        }

        return paramString;
    }

    /**
     * Asks user for permission
     *
     * @param activity   activity
     * @param permission permission to ask for
     * @param callBack   callback
     */
    public static void requestPermission(Activity activity, String permission, int callBack) {
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permission not granted");

            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        callBack);
            }
        } else {
            Log.i(TAG, "Permission granted");
        }
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onHttpGetExecuted(String response);
    }
}
