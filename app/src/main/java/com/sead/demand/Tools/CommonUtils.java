package com.sead.demand.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.Entities.User;
import com.sead.demand.FCM.MyJobService;
import com.sead.demand.Handlers.AlarmReceiver;
import com.sead.demand.R;

import org.json.JSONException;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdAt);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH); // Starts from 0 to 11.
        month++;
        int year = cal.get(Calendar.YEAR);
        String dayString;
        String monthString;
        String yearString;
        String shownTime;
        if(day < 10) dayString = "0" + day;
        else dayString = "" + day;
        if(month < 10) monthString = "0" + month;
        else monthString = "" + month;
        if(year < 10) yearString = "0" + year;
        else yearString = "" + year;
        shownTime = dayString + "/" + monthString + "/" + yearString;
        return shownTime;
    }

    public static String formatTime(Date createdAt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdAt);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String hourString;
        String minuteString;
        if (hour < 10) hourString = "0" + hour;
        else hourString = "" + hour;
        if (minute < 10) minuteString = "0" + minute;
        else minuteString = "" + minute;
        String shownTime = hourString + ":" + minuteString;
        return shownTime;
    }

    public static Date convertTimestampToDate(String timestamp) {
        try{
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date formattedDate = formatter.parse(timestamp);
            return formattedDate;
        }catch(Exception e){//this generic but you can control another types of exception
            return null;
        }
    }

    // DB methods.

    // Only for test purposes.
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

    // Only for test purposes.
    public static void listAllUsersDB(Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        List<User> users;

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] args = {"1"};
        users = myDBManager.searchUsers(selection,args);

        for (int index = 0; index < users.size(); index++)
            Log.e(TAG, "User " + index + ":" + users.get(index).toString() + "\n");
    }

    // Only for test purposes.
    public static void listAllAuthsDB(Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        List<Authority> authorities;

        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_AUTH_ID + " = ?";
        String[] args = {"1"};
        authorities = myDBManager.searchAuthorities(selection,args);

        for (int index = 0; index < authorities.size(); index++)
            Log.e(TAG, "User " + index + ":" + authorities.get(index).toString() + "\n");
    }

    // Only for test purposes.
    public static void listAllReasonsDB(Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        List<PredefinedReason> reasons;

        String selection = FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID + " = ?";
        String[] args = {"1"};
        reasons = myDBManager.searchReasons(selection,args);

        for (int index = 0; index < reasons.size(); index++)
            Log.e(TAG, "Reason " + index + ":" + reasons.get(index).toString() + "\n");
    }

    public static void notifyDemandListView(Demand demand, String fragmentTag, String type, Context context){
        Log.e(TAG, "on notify receiver. Frag:" + fragmentTag + " demand:" + demand.toString()
        + " type:" + type);

        Intent i = new Intent(fragmentTag);
        i.putExtra(Constants.INTENT_DEMAND, demand);
        i.putExtra(Constants.INTENT_STORAGE_TYPE, type);
        context.sendBroadcast(i);
    }

    public static void notifyUserListView(User user, String fragmentTag, String type, Context context){
        Intent i = new Intent(fragmentTag);
        i.putExtra(Constants.INTENT_USER, user);
        i.putExtra(Constants.INTENT_STORAGE_TYPE, type);
        context.sendBroadcast(i);
        Log.e(TAG, "on notify receiver. Frag:" + fragmentTag + " user:" + user.toString());
    }

    public static void storeDemandDB(Demand demand, String type, Context context){
        // listAllDemandsDB(context);
        // listAllUsersDB(context);

        MyDBManager myDBManager = new MyDBManager(context);
        long newRow = myDBManager.addDemand(demand);
        Log.e(TAG, "New row inserted:" + newRow);
        listAllDemandsDB(context);
        listAllReasonsDB(context);

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
            notifyDemandListView(demand,fragTag,type,context);
        }

        if (type == Constants.INSERT_DEMAND_RECEIVED) {
            Log.e(TAG, "Starting Insert setWarnDueTime");
            setWarnDueTime(demand,context);
        }

    }

    public static void updateDemandDB(String type, Demand demand, Context context) {
        MyDBManager myDBManager = new MyDBManager(context);
        long newRow = myDBManager.addDemand(demand);
        Log.e(TAG, "New row inserted:" + newRow);
        listAllDemandsDB(context);
        listAllReasonsDB(context);

        if(newRow > 0) {
            String fragTag = "";
            fragTag = Constants.BROADCAST_SENT_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_RECEIVER_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_ADMIN_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_STATUS_ACT;
            notifyDemandListView(demand,fragTag,type, context);
        }
    }

    public static void updateDemandDB(Demand demand, String type, Context context){
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID, demand.getSender().getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID, demand.getReceiver().getId());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT, demand.getSubject());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION, demand.getDescription());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS, demand.getStatus());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN, demand.getSeen());
        values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_UPDATED_AT, demand.getUpdatedAt().toString());
        if (demand.getType() != null) {
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_TYPE_ID, demand.getType().getId());
            updateTypeDB(demand.getType(), context);
        }
        if (demand.getReason() != null) {
            values.put(FeedReaderContract.DemandEntry.COLUMN_NAME_REASON_ID, demand.getReason().getServerId());
            updateReasonDB(demand.getReason(), context);
        }

        MyDBManager myDBManager = new MyDBManager(context);
        int newRow = myDBManager.updateDemand(demand.getId(), values);
        Log.e(TAG, "Row updated:" + newRow);

        if(newRow > 0) {
            String fragTag = "";
            fragTag = Constants.BROADCAST_SENT_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_RECEIVER_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_ADMIN_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_STATUS_ACT;
            notifyDemandListView(demand,fragTag,type, context);
        }

        // listAllDemandsDB(context);
    }

    private static void updateTypeDB(DemandType demandType, Context context) {
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TYPE_ID, demandType.getId());
        values.put(FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TITLE, demandType.getTitle());
        values.put(FeedReaderContract.DemandTypeEntry.COLUMN_NAME_PRIORITY, demandType.getPriority());
        values.put(FeedReaderContract.DemandTypeEntry.COLUMN_NAME_COMPLEXITY, demandType.getComplexity());
        values.put(FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TYPE_UPDATED_AT, CommonUtils.formatDate(demandType.getUpdatedAt()));
        values.put(FeedReaderContract.DemandTypeEntry.COLUMN_NAME_TYPE_CREATED_AT, CommonUtils.formatDate(demandType.getCreatedAt()));

        MyDBManager myDBManager = new MyDBManager(context);
        int newRow = myDBManager.updateType((int) demandType.getId(), values);
        Log.e(TAG, "Demand Type BD updated:" + newRow);
    }

    private static void updateReasonDB(PredefinedReason reason, Context context) {
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_REASON_ID, reason.getServerId());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_TYPE, reason.getType());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_TITLE, reason.getTitle());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_DESCRIPTION, reason.getDescription());
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_CREATED_AT, CommonUtils.formatDate(reason.getCreatedAt()));
        values.put(FeedReaderContract.ReasonEntry.COLUMN_NAME_USER_UPDATED_AT, CommonUtils.formatDate(reason.getUpdatedAt()));

        MyDBManager myDBManager = new MyDBManager(context);
        int newRow = myDBManager.updateReason((int) reason.getServerId(), values);
        Log.e(TAG, "Reason DB updated:" + newRow);
    }

    public static void updateColumnDB(String columnName, String value, Demand demand, String type, Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        int count = myDBManager.updateDemandColumn(demand,columnName,value);

        if(count > 0) {

            if (demand.getReason() != null) {
                updateReasonDB(demand.getReason(), context);
            }

            // If demand is updated to ACCEPTED then check if this user is the receiver,
            // so warn alarm should be set.
            if(demand.getStatus().equals(Constants.ACCEPT_STATUS)) {
                SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                try {
                    JSONObject userJson = new JSONObject(preferences.getString(Constants.USER_PREFERENCES,""));
                    User currentUser = User.build(userJson);
                    if (demand.getReceiver().getId() == currentUser.getId()){
                        Log.e(TAG, "setWarnDueTime called!");
                        if (demand.getType() != null) setWarnDueTime(demand,context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            String fragTag = "";

            fragTag = Constants.BROADCAST_SENT_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_RECEIVER_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_ADMIN_FRAG;
            notifyDemandListView(demand,fragTag,type,context);
            fragTag = Constants.BROADCAST_STATUS_ACT;
            notifyDemandListView(demand,fragTag,type, context);
        }

        // listAllDemandsDB(context);
    }

    public static int archiveDemand(Demand demand, Context context, Boolean state){
        String columnName = FeedReaderContract.DemandEntry.COLUMN_NAME_ARCHIVE;

        MyDBManager myDBManager = new MyDBManager(context);
        int count = myDBManager.updateDemandColumn(demand,columnName,state.toString());

        listAllDemandsDB(context);

        return count;
    }

    public static int deleteDemand(Demand demand, Context context) {
        MyDBManager myDBManager = new MyDBManager(context);
        int count = myDBManager.deleteDemandById(demand.getId());
        listAllDemandsDB(context);
        return count;
    }

    public static void storeUserDB(User user, String type, Context context){
        // listAllDemandsDB(context);
        // listAllUsersDB(context);

        MyDBManager myDBManager = new MyDBManager(context);
        long newRow = myDBManager.addUser(user);
        Log.e(TAG, "New row inserted or updated:" + newRow);

        if(newRow > 0) {
            String fragTag = "";
            switch (type){
                case Constants.UNLOCK_USER:
                    fragTag = Constants.BROADCAST_REQUEST_ACT;
                    break;
            }
            notifyUserListView(user,fragTag,type,context);
        }

        listAllUsersDB(context);
    }

    public static void updateColumnUserDB(String columnName, String value, User user, Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        int count = myDBManager.updateUserColumn(user.getId(),columnName,value);

        if(count > 0) {
            Log.e(TAG, "User column changed to " + value);
        }

        listAllUsersDB(context);

    }

    public static void storeAuthDB(Authority authority, String type, Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        long newRow = myDBManager.addAuthority(authority);
        Log.e(TAG, "New row inserted or updated AUTH:" + newRow);
        listAllAuthsDB(context);
    }

    public static void updateColumnAuthDB(String columnName, String value, Authority authority, Context context){
        MyDBManager myDBManager = new MyDBManager(context);
        int count = myDBManager.updateAuthorityColumn(authority.getId(),columnName,value);

        if(count > 0) {
            Log.e(TAG, "Auth column changed to " + value);
        }

        listAllAuthsDB(context);

    }
    // END DB methods.

    public static void setWarnDueTime(Demand demand, Context context){
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        int type = Constants.WARN_DUE_TIME_ALARM_TAG;
        receiverIntent.setType("" + type);
        receiverIntent.putExtra(Constants.INTENT_DEMAND, demand);
        receiverIntent.putExtra(Constants.INTENT_PAGE, Constants.RECEIVED_PAGE);
        receiverIntent.putExtra(Constants.INTENT_MENU, Constants.RECEIVER_MENU);
        Log.e(TAG, "(warning) Alarm Key Type:" + receiverIntent.getType());

        Calendar c = Calendar.getInstance();
        Log.e(TAG, "(warning) Now:" + c.getTime().toString());
        int postponeTime = getPriorityTime(demand.getType().getPriority());
        c.add(Calendar.DAY_OF_YEAR, postponeTime - Constants.DUE_TIME_PREVIOUS_WARNING);
        Log.e(TAG, "Warn Due time:" + c.getTime().toString());
        long timeInMillis = c.getTimeInMillis();
        Log.e(TAG, "Time in millis:" + timeInMillis);

        MyAlarmManager.addAlarm(context, receiverIntent, demand.getId(), type, timeInMillis);
    }

    public static int getPriorityTime(String priority) {
        int postponeTime;
        switch (priority){
            case Constants.VERY_HIGH_PRIOR_TAG:
                postponeTime = Constants.DUE_TIME[0];
                break;
            case Constants.HIGH_PRIOR_TAG:
                postponeTime = Constants.DUE_TIME[1];
                break;
            case Constants.MEDIUM_PRIOR_TAG:
                postponeTime = Constants.DUE_TIME[2];
                break;
            case Constants.LOW_PRIOR_TAG:
                postponeTime = Constants.DUE_TIME[3];
                break;
            default:
                postponeTime = Constants.DUE_TIME[0];
        }
        return postponeTime;
    }

    public static void cancelDueTime(Demand demand, Context context, int type){
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        receiverIntent.setType("" + type);
        Log.e(TAG, "(cancel warning) Alarm Key Type:" + receiverIntent.getType());
        receiverIntent.putExtra(Constants.INTENT_DEMAND, demand);
        receiverIntent.putExtra(Constants.INTENT_PAGE, Constants.RECEIVED_PAGE);
        receiverIntent.putExtra(Constants.INTENT_MENU, Constants.SHOW_DONE_MENU);
        Log.e(TAG, "Is there any alarm for " + demand.getId() + ":"
                + MyAlarmManager.hasAlarm(context,receiverIntent,demand.getId()));
        MyAlarmManager.cancelAlarm(context,receiverIntent,demand.getId(), type);
        Log.e(TAG, "Canceled alarm:" + demand.getSubject());
        Log.e(TAG, "Is there any alarm for " + demand.getId() + ":"
                + MyAlarmManager.hasAlarm(context,receiverIntent,demand.getId()));
    }

    public static void setAlarm(Context context, long dueTime, Demand demand, int type, int page, int menu) {
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_DEMAND, demand);
        intent.setType("" + type);
        intent.putExtra(Constants.INTENT_PAGE, page);
        intent.putExtra(Constants.INTENT_MENU, menu);
        MyAlarmManager.addAlarm(context, intent, demand.getId(), type, dueTime);
        Log.d(TAG, "Due Time (milli): " + dueTime
                + " Due Date|Time: " + demand.getDueDate() + "|" + demand.getDueTime());
    }

    // Set an alarm from now to plus (previous waring constant) days.
    public static void setDueTime(Demand demand, Context context){
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        int type = Constants.DUE_TIME_ALARM_TAG;
        receiverIntent.setType("" + type);
        receiverIntent.putExtra(Constants.INTENT_DEMAND, demand);
        receiverIntent.putExtra(Constants.INTENT_PAGE, Constants.RECEIVED_PAGE);
        receiverIntent.putExtra(Constants.INTENT_MENU, Constants.SHOW_DONE_MENU);
        Log.e(TAG, "Alarm Key Type:" + receiverIntent.getType());

        Calendar c = Calendar.getInstance();
        Log.e(TAG, "(Due time) Now:" + c.getTime().toString());
        c.add(Calendar.DAY_OF_YEAR, Constants.DUE_TIME_PREVIOUS_WARNING);
        Log.e(TAG, "Due time:" + c.getTime().toString());
        long timeInMillis = c.getTimeInMillis();
        Log.e(TAG, "Time in millis:" + timeInMillis);

        MyAlarmManager.addAlarm(context, receiverIntent, demand.getId(), type, timeInMillis);
    }

    public static void cancelAllAlarms(Demand demand, Context context) {
        CommonUtils.cancelDueTime(demand,context, Constants.WARN_DUE_TIME_ALARM_TAG);
        CommonUtils.cancelDueTime(demand,context, Constants.DUE_TIME_ALARM_TAG);
        CommonUtils.cancelDueTime(demand,context, Constants.POSTPONE_ALARM_TAG);
    }

    // For Demand Adapter List purposes
    public static int getIndexByDemandId(List<Demand> demands, int id){
        for(int i=0; i < demands.size(); i++ ) {
            if(demands.get(i).getId() == id ) return i;
        }
        return -1;
    }

    // For User Adapter List purposes
    public static int getIndexByUserId(List<User> users, int id){
        for(int i=0; i < users.size(); i++ ) {
            if(users.get(i).getId() == id ) return i;
        }
        return -1;
    }

    // Handle demand changes when there is no internet connection
    public static void handleLater(Demand demand, String type, Context context){
        Bundle bundle = new Bundle();
        //bundle.putSerializable(Constants.INTENT_DEMAND, demand);
        bundle.putInt(Constants.INTENT_DEMAND_SERVER_ID, demand.getId());
        bundle.putString(Constants.INTENT_DEMAND_STATUS, demand.getStatus());
        bundle.putString(Constants.JOB_TYPE_KEY, type);

        String jobTag = type + "-" + demand.getId();
        Log.e(TAG, "Job Tag:" + jobTag);

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag(jobTag)
                // Overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(
                        Constraint.ON_ANY_NETWORK
                )
                .setExtras(bundle)
                .build();
        dispatcher.schedule(myJob);
        //Demand demandSer = (Demand) bundle.getSerializable(Constants.INTENT_DEMAND);
        Log.e(TAG, "Handle Later Called. Demand:" + bundle.getInt(Constants.INTENT_DEMAND_SERVER_ID)
         + " Job type:" + bundle.getString(Constants.JOB_TYPE_KEY));
    }

    public static String getPriorTag(int position){
        String priorTag;
        switch (position) {
            case 0:
                priorTag = Constants.VERY_HIGH_PRIOR_TAG;
                break;
            case 1:
                priorTag = Constants.HIGH_PRIOR_TAG;
                break;
            case 2:
                priorTag = Constants.MEDIUM_PRIOR_TAG;
                break;
            case 3:
                priorTag = Constants.LOW_PRIOR_TAG;
                break;
            default:
                priorTag = Constants.MEDIUM_PRIOR_TAG;
        }
        return priorTag;
    }

    public static String getPriorName(String priorTag, Context context){
        String priorName = "Prioridade ";
        String[] priorArray = context.getResources().getStringArray(R.array.array_status);
        Log.e(TAG, "Prior Array:" + priorArray.toString());
        switch (priorTag){
            case Constants.VERY_HIGH_PRIOR_TAG:
                priorName = priorName.concat(priorArray[0]);
                break;
            case Constants.HIGH_PRIOR_TAG:
                priorName = priorName.concat(priorArray[1]);
                break;
            case Constants.MEDIUM_PRIOR_TAG:
                priorName = priorName.concat(priorArray[2]);
                break;
            case Constants.LOW_PRIOR_TAG:
                priorName = priorName.concat(priorArray[3]);
                break;
            default:
                priorName = priorName.concat(priorArray[3]);
        }
        return priorName;
    }

    // Get Preferences
    public static User getCurrentUserPreference(Context context){
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            com.sead.demand.Entities.Job job = getCurrentUserJobPreference(context);
            User superior = getCurrentUserSuperiorPreference(context);
            JSONObject userJson = new JSONObject(preferences.getString(Constants.USER_PREFERENCES,""));
            User currentUser = User.build(job, superior, userJson);
            return currentUser;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static com.sead.demand.Entities.Job getCurrentUserJobPreference(Context context){
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            JSONObject jobJson = new JSONObject(preferences.getString(Constants.JOB_PREFERENCES,""));
            com.sead.demand.Entities.Job currentUserJob = com.sead.demand.Entities.Job.build(jobJson);
            return currentUserJob;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User getCurrentUserSuperiorPreference(Context context){
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            JSONObject superiorJson = new JSONObject(preferences.getString(Constants.SUPERIOR_PREFERENCES,""));
            User currentUserSuperior = User.build(superiorJson);
            return currentUserSuperior;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
