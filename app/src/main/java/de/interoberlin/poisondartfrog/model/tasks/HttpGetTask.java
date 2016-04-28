package de.interoberlin.poisondartfrog.model.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpGetTask extends AsyncTask<Map<EHttpParameter, String>, Void, String> {
    public static final String TAG = HttpGetTask.class.toString();

    private static final String HTTP_METHOD = "GET";
    private static final int RESPONSE_CODE_OKAY = 200;

    private static OnCompleteListener ocListener;
    private static String url;

    // --------------------
    // Constructors
    // --------------------

    public HttpGetTask(OnCompleteListener ocListener, String url) {
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
    protected String doInBackground(Map<EHttpParameter, String>... params) {
        Map<EHttpParameter, String> values = params[0];

        if (values != null && !values.isEmpty()) {
            try {
                return callGolemTemperatureURL(values);
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
    private static String callGolemTemperatureURL(Map<EHttpParameter, String> values) throws Exception {
        // projekte/ot/temp.php?dbg=1&token=a54a54bad9a51bd059ba4746157eef21&city=Berlin&zip=133&
        // country=DE&lat=0.0&long=0.0&type=other&temp=

        // Connection
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

        // Request header
        con.setRequestMethod(HTTP_METHOD);
        con.setDoOutput(true);

        // Execute request
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(getParamString(values));
        wr.flush();
        wr.close();

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
                return response.toString();
            }
        } finally {
            con.disconnect();
        }
    }

    public static String getParamString(Map<EHttpParameter, String> values) {
        StringBuilder sb = new StringBuilder();

        // Append params
        for (Map.Entry<EHttpParameter, String> v : values.entrySet()) {
            sb.append(v.getKey().getParam()).append("=").append(v.getValue()).append("&");
        }

        // Remove trailing ampersand
        String paramString = "";
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '&') {
            paramString = sb.substring(0, sb.length() - 1);
        }

        return paramString;
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onHttpGetExecuted(String response);
    }
}
