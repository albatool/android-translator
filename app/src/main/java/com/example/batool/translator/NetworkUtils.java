package com.example.batool.translator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;


public class NetworkUtils {

    private final static String LOG_TAG = NetworkUtils.class.getName();


    // Check the network connection status
    public static boolean isConnected(Context context) {

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }


    //  Make an HTTP request to the given URL and return a string as a response
    private static String makeHttpRequest(String stringUrl) throws IOException {

        URL url = null;

        try {

            url = new URL(stringUrl);

        } catch (MalformedURLException e) {

            Log.e(LOG_TAG, "Problem in creating the URL", e);
        }


        HttpURLConnection urlConnection = null;

        InputStream inputStream = null;

        StringBuilder jsonResponse = new StringBuilder();

        try {

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");

            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);

            urlConnection.connect();

            /* If the connection was successful, retrieve the response body as an InputStream
             * and then read from that InputStream into a jsonResponse String*/
            if (urlConnection.getResponseCode() == 200) {

                inputStream = urlConnection.getInputStream();

                //  Read and convert the contents of the inputStream into a String
                if (inputStream != null) {

                    // An instance of InputStreamReader to read bytes and decode them into characters using UTF-8 charset
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

                    // An instance of BufferedReader to read text from a character-input stream
                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    String line = reader.readLine();

                    while (line != null) {

                        jsonResponse.append(line);
                        line = reader.readLine();
                    }
                }

                // Otherwise, log the error
            } else if (urlConnection.getResponseCode() == 401) {

                Log.e(LOG_TAG, "Error response code:" + " 401 invalid API key");


            } else if (urlConnection.getResponseCode() == 402) {

                Log.e(LOG_TAG, "Error response code:" + " 402 Blocked API key");

            } else if (urlConnection.getResponseCode() == 404) {

                Log.e(LOG_TAG, "Error response code:" + "404 Exceeded the daily limit on the amount of translated text");


            } else {

                Log.e(LOG_TAG, "Error response code:" + urlConnection.getResponseCode());

            }

        } catch (IOException e) {

            Log.e(LOG_TAG, "Problem in retrieving the translation result.", e);

        } finally {

            // Close Stream and disconnect HTTP connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse.toString();
    }


    // Get the request URL and perform the HTTP request then process the JSON response and fetch the translation
    public static String fetchTranslation(String requestUrl) {

        String jsonResponse = null;
        String translation = "";

        try {

            // Perform the HTTP request and get the JSON response
            jsonResponse = NetworkUtils.makeHttpRequest(requestUrl);

        } catch (IOException e) {

            Log.e(LOG_TAG, "Problem in making the HTTP request", e);
        }

        // If the returned JSON string is empty or null, return early
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Otherwise, process the JSON response and fetch the translation
        try {

            JSONObject baseJsonResponse = new JSONObject(jsonResponse);

            JSONArray textArray = baseJsonResponse.getJSONArray("text");

            translation = textArray.get(0).toString();

        } catch (JSONException e) {

            Log.e(LOG_TAG, "Problem in parsing the translation JSON result", e);
        }

        return translation;
    }
}
