package com.example.caiqu.demand.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;

public class CommonUtils {

    public static String POST(String url, ContentValues values){
        InputStream is = null;
        OutputStream os = null;

        try{
            URL urlObject = new URL(Constants.BASE_URL + url);

            Log.e("ON POST", urlObject.toString());

            HttpURLConnection urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");

            os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            Log.d("ON POST", " Values are: " + values.toString());

            writer.write(buildPostString(values));
            writer.flush();
            writer.close();

            os.close();

            urlConnection.connect();

            int statusCode = urlConnection.getResponseCode();
            Log.d("ON POST", " The status code is " + statusCode);

            if (statusCode == 200) {
                is = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(is);
                Log.d("ON POST", "The response is " + response);
                urlConnection.disconnect();
                return response;

            } else {
                Log.d("ON POST", "On Else");
                return "";
            }

        } catch (Exception e) {
            Log.d("ON POST", "On CATCH");
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("ON POST", "On Finally");
        }
    }

    private static String buildPostString(ContentValues params) {
        int i = 0;
        StringBuilder sbParams = new StringBuilder();
        String charset = "UTF-8";

        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(String.valueOf(params.get(key)), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        Log.d("HTTP Request", "params: " + sbParams.toString());
        return sbParams.toString();
    }

    private static String convertInputStreamToString(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public static boolean isOnline(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return  netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String formatDate(Date createdAt) {
        Calendar cal =  Calendar.getInstance();
        cal.setTime(createdAt);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return "Day " + day;
    }
}
