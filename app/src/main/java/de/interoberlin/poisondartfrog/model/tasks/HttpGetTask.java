package de.interoberlin.poisondartfrog.model.tasks;

import android.os.AsyncTask;
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
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = HttpGetTask.class.getSimpleName();

    private static final String HTTP_METHOD = "GET";
    private static final String ENCODING = "UTF-8";
    private static final String CONTENT_TYPE = "text/plain";
    private static final int RESPONSE_CODE_OKAY = 200;

    private static OnCompleteListener ocListener;
    private static String url;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public HttpGetTask(OnCompleteListener ocListener, String url) {
        HttpGetTask.ocListener = ocListener;
        HttpGetTask.url = url;
    }

    // </editor-fold>

    // --------------------
    // Methods - Lifecycle
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Lifecycle">

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Map<EHttpParameter, String>... parameters) {
        Map<EHttpParameter, String> values = parameters[0];

        if (url != null && values != null && !values.isEmpty()) {
            try {
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

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

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

        Log.d(TAG, url + getParamString(values));

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

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnCompleteListener {
        void onHttpGetExecuted(String response);
    }

    // </editor-fold>
}