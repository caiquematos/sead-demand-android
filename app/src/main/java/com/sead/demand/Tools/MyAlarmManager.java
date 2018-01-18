package com.sead.demand.Tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caiqu on 27/07/2017.
 */

public class MyAlarmManager {
    private static String TAG = "MyAlarmManager";
    private static final String sTagAlarms = ":alarms";

    public static void addAlarm(Context context, Intent intent, int notificationId, long timeInMillis){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        saveAlarmId(context, notificationId);
    }

    public static void cancelAlarm(Context context, Intent intent, int notificationId){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        removeAlarmId(context, notificationId);
    }

    public static void cancelAllAlarms(Context context, Intent intent){
        for (int idAlarm : getAlarmIds(context)) {
            cancelAlarm(context, intent, idAlarm);
        }
    }

    public static boolean hasAlarm(Context context, Intent intent, int notificationId){
        return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT) != null;
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


}
