package com.sead.demand.Tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sead.demand.Handlers.AlarmReceiver;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by caiqu on 27/07/2017.
 */

public class MyAlarmManager {
    private static String TAG = "MyAlarmManager";
    private static final String sTagAlarms = ":alarms";

    public static void addAlarm(Context context, Intent intent, int notificationId, int type, long timeInMillis){
        int alarmId = generateAlarmId(notificationId, type);



        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId , intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (hasAlarm(context, intent, alarmId, type)) cancelAlarm(context, intent, notificationId, type);

        /* teste */
        Intent intentToFire = intent;
        intentToFire.putExtra("test", "teste sim");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intentToFire,0);
        /* teste */

        /* only for test purpose */
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        Log.d(TAG, "due time (test): " + CommonUtils.convertMillisToDate(c.getTimeInMillis())
                + " " + CommonUtils.convertMillisToTime(c.getTimeInMillis()));
        /* only for test purpose */

        timeInMillis = c.getTimeInMillis();
        pendingIntent = alarmIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "(addAlarm) > version M");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.d(TAG, "(addAlarm) > version K");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            Log.d(TAG, "(addAlarm) other version < K");
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        saveAlarmId(context, alarmId);

        //CHECKING IF PENDING INTENT IS ALREADY RUNNING
        Intent checkIntent = new Intent(context, AlarmReceiver.class);
        Boolean alarmUp = (PendingIntent.getBroadcast(context, 0, checkIntent, PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmUp) Log.d(TAG, "(addAlarm) alarm set!");
        else Log.e(TAG, "(addAlarm) alarm NOT set!");
    }

    public static void cancelAlarm(Context context, Intent intent, int notificationId, int type){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, generateAlarmId(notificationId,type), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        removeAlarmId(context, generateAlarmId(notificationId, type));
    }

    public static void cancelAllAlarms(Context context, Intent intent, int type){
        for (int idAlarm : getAlarmIds(context)) {
            cancelAlarm(context, intent, idAlarm, type);
        }
    }

    public static boolean hasAlarm(Context context, Intent intent, int notificationId, int type){
        return PendingIntent.getBroadcast(context, generateAlarmId(notificationId,type), intent, PendingIntent.FLAG_UPDATE_CURRENT) != null;
    }

    private static void saveAlarmId(Context context, int id){
        List<Integer> idsAlarms = getAlarmIds(context);
        Log.e(TAG, "Before save alarm ids: " + idsAlarms.toString());

        if (idsAlarms.contains(id)) {
            return;
        }

        idsAlarms.add(id);

        Log.e(TAG, "After save alarm ids: " + idsAlarms.toString());
        saveIdsInPreferences(context, idsAlarms);
    }

    private static void removeAlarmId(Context context, int id) {
        List<Integer> idsAlarms = getAlarmIds(context);
        Log.e(TAG, "Before remove alarm ids: " + idsAlarms.toString());

        for (int i = 0; i < idsAlarms.size(); i++) {
            if (idsAlarms.get(i) == id)
                idsAlarms.remove(i);
        }

        Log.e(TAG, "After remove alarm ids: " + idsAlarms.toString());
        saveIdsInPreferences(context, idsAlarms);
    }


    private static List<Integer> getAlarmIds(Context context) {
        List<Integer> ids = new ArrayList<>();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            JSONArray jsonArray2 = new JSONArray(prefs.getString(context.getPackageName() + sTagAlarms, "[]"));

            for (int i = 0; i < jsonArray2.length(); i++) {
                ids.add(jsonArray2.getInt(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    private static void saveIdsInPreferences(Context context, List<Integer> lstIds) {
        JSONArray jsonArray = new JSONArray();
        for (Integer idAlarm : lstIds) {
            jsonArray.put(idAlarm);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getPackageName() + sTagAlarms, jsonArray.toString());

        editor.apply();
    }

    public String toString(Context context) {
        String alarmIdsString = "";
        for (int alarmId: getAlarmIds(context) ){
            alarmIdsString = alarmIdsString + "[" + alarmId + "] ";
        }
        return alarmIdsString;
    }

    // Generate a unique alarm id using two parameters.
    // In this case: demand_id, type_number.
    private static int generateAlarmId(int p1, int p2) {
        String sp1 = "" + p1;
        String sp2 = "" + p2;
        String sresult = sp1 + sp2;
        int result = Integer.parseInt(sresult);
        return result;
    }


}
