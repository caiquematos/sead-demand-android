package com.example.caiqu.demand.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.FCM.MyJobService;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;

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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommonUtils {
    public static String TAG = "CommonUtils";

    public static String POST(String url, ContentValues values){
        InputStream is = null;
        OutputStream os = null;

        try{
            URL urlObject = new URL(Constants.BASE_URL + url);

            Log.e(TAG, urlObject.toString());

            HttpURLConnection urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");

            os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            Log.d(TAG, " Values are: " + values.toString());

            writer.write(buildPostString(values));
            writer.flush();
            writer.close();

            os.close();

            urlConnection.connect();

            int statusCode = urlConnection.getResponseCode();
            Log.d(TAG, " The status code is " + statusCode);

            if (statusCode == 200) {
                is = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(is);
                Log.d(TAG, "The response is " + response);
                urlConnection.disconnect();
                return response;

            } else {
                Log.d(TAG, "On Else");
                return "";
            }

        } catch (Exception e) {
            Log.d(TAG, "On CATCH");
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

            Log.d(TAG, "On Finally");
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

    public static String formatDate(Date createdAt) {
        Calendar cal =  Calendar.getInstance();
        cal.setTime(createdAt);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return "Day " + day;
    }

    public static Date datify(String timestamp) {
        try{
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US);
            return formatter.parse(timestamp);
        }catch(Exception e){//this generic but you can control another types of exception
            return null;
        }
    }

    // DB methods.

    // Just for text purposes.
    public static void listAllDemandsDB(Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        // Listing all demands stored
        List<Demand> demands;

        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ?";
        String[] args = {"1"};
        demands = myDBManager.searchDemands(selection,args);

        for (int index = 0; index < demands.size(); index++)
            Log.e(TAG, "Demanda " + index + ":" + demands.get(index).toString() + "\n");
    }

    // Listing all users stored. Just for text purposes.
    public static void listAllUsersDB(Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        List<User> users;

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] args = {"1"};
        users = myDBManager.searchUsers(selection,args);

        for (int index = 0; index < users.size(); index++)
            Log.e(TAG, "User " + index + ":" + users.get(index).toString() + "\n");
    }

    public static void notifyListView(Demand demand, String fragmentTag, String type, Context context){
        Intent i = new Intent(fragmentTag);
        i.putExtra(Constants.INTENT_DEMAND, demand);
        i.putExtra(Constants.INTENT_STORAGE_TYPE, type);
        context.sendBroadcast(i);
        Log.e(TAG, "on notify receiver. Frag:" + fragmentTag + " demand:" + demand.toString());
    }

    public static void storeDemandDB(Demand demand, String type, Context context){
        listAllDemandsDB(context);
        listAllUsersDB(context);

        MyDBManager myDBManager = new MyDBManager(context);
        long newRow = myDBManager.addDemand(demand);
        Log.e(TAG, "New row inserted:" + newRow);

        if(newRow > 0) {
            String fragTag = "";
            switch (type){
                case Constants.INSERT_DEMAND_RECEIVED:
                    fragTag = Constants.BROADCAST_RECEIVER_FRAG;
                    break;
                case Constants.INSERT_DEMAND_ADMIN:
                    fragTag = Constants.BROADCAST_ADMIN_FRAG;
                    break;
                case Constants.INSERT_DEMAND_SENT:
                    fragTag = Constants.BROADCAST_SENT_FRAG;
                    break;
            }
            notifyListView(demand,fragTag,type,context);
        }

        listAllDemandsDB(context);
    }

    public static void updateDemandDB(Demand demand, String type, Context context){
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID, demand.getSender().getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID, demand.getReceiver().getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT, demand.getSubject());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION, demand.getDescription());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS, demand.getStatus());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE, demand.getImportance());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN, demand.getSeen());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT, demand.getUpdatedAt().toString());

        MyDBManager myDBManager = new MyDBManager(context);
        int newRow = myDBManager.updateDemand(demand.getId(), values);
        Log.e(TAG, "Row updated:" + newRow);

        if(newRow > 0) {
            String fragTag = "";
            fragTag = Constants.BROADCAST_SENT_FRAG;
            notifyListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_RECEIVER_FRAG;
            notifyListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_ADMIN_FRAG;
            notifyListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_STATUS_ACT;
            notifyListView(demand,fragTag,type, context);
        }

        listAllDemandsDB(context);
    }

    public static void updateColumnDB(String columnName, String value, Demand demand, String type, Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        int count = myDBManager.updateDemandColumn(demand.getId(),columnName,value);

        if(count > 0) {
            String fragTag = "";

            fragTag = Constants.BROADCAST_SENT_FRAG;
            notifyListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_RECEIVER_FRAG;
            notifyListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_ADMIN_FRAG;
            notifyListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_STATUS_ACT;
            notifyListView(demand,fragTag,type, context);
        }

        listAllDemandsDB(context);
    }

    // For Demand Adapter List purposes
    public static int getIndexByDemandId(List<Demand> demands, int id){
        for(int i=0; i < demands.size(); i++ ) {
            if(demands.get(i).getId() == id ) return i;
        }
        return -1;
    }

    // Handle demand changes when there is no internet connection
    public static void handleLater(Demand demand, String tag, Context context){
        Bundle bundle = new Bundle();
        // TODO: Make Demand parcelable at some point.
        //bundle.putSerializable(Constants.INTENT_DEMAND, demand);
        bundle.putInt(Constants.INTENT_DEMAND_SERVER_ID, demand.getId());
        bundle.putString(Constants.INTENT_DEMAND_STATUS, demand.getStatus());
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag(tag)
                .setReplaceCurrent(false)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .setExtras(bundle)
                .build();
        dispatcher.schedule(myJob);
        //Demand demandSer = (Demand) bundle.getSerializable(Constants.INTENT_DEMAND);
        Log.e(TAG, "Handle Later Called. Demand:" + bundle.getInt(Constants.INTENT_DEMAND_SERVER_ID));
    }

}
