package com.sead.demand.Handlers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.sead.demand.Activities.MainActivity;
import com.sead.demand.Activities.ViewDemandActivity;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Entities.Demand;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by caiqu on 19/06/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    String TAG = getClass().getSimpleName();
    private StatusTask mStatusTask;
    private DueTimeTask mDueTimeTask;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra(Constants.INTENT_BUNDLE);
        Log.e(TAG, "(onReceive) string: " + intent.getExtras().getString("test"));
        int type = intent.getExtras().getInt("type");
        Demand demand = (Demand) bundle.getSerializable(Constants.INTENT_DEMAND);
        String title = "";
        String text = demand.getSubject();
        String bigTextTitle = "";
        String bigText = "";
        int drawable = -1;

        Log.d(TAG, "(onReceive) alarm type:" + type);
        Log.d(TAG, "(onReceive) demand:" + demand.toString());

        switch (type) {
            case Constants.WARN_DUE_TIME_ALARM_TAG:
                title = "Expira em "
                        + Constants.DUE_TIME_PREVIOUS_WARNING
                        + (Constants.DUE_TIME_PREVIOUS_WARNING > 1 ? " dias" : " dia");
                bigTextTitle = title;
                bigText = context.getString(R.string.big_text_due_time_warning);
                drawable = R.drawable.ic_alarm_black_24dp;
                attemptToSendLateWarning(demand, context);
                break;
            case Constants.DUE_TIME_ALARM_TAG:
                title = "Prazo expirou!";
                bigTextTitle = title;
                bigText = context.getString(R.string.big_text_due_time);
                drawable = R.drawable.ic_alarm_off_black_24dp;
                attemptToMarkDemandAsLate(demand, context);
                break;
            case Constants.REMIND_ME_ALARM_TAG:
                title = "Lembrete!";
                bigTextTitle = title;
                bigText = context.getString(R.string.big_text_remeind_me);
                drawable = R.drawable.ic_alarm_black_24dp;
                break;
        }

       generateNotification(demand, context, intent, drawable, title, text, bigTextTitle, bigText);
    }

    private void generateNotification(Demand demand, Context context, Intent intent,
                                      int drawable, String title, String text, String bigTextTitle, String bigText) {
        Intent targetIntent = new Intent(context, ViewDemandActivity.class);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        targetIntent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
        targetIntent.putExtra(Constants.INTENT_PAGE, intent.getExtras().getInt(Constants.INTENT_PAGE));
        targetIntent.putExtra(Constants.INTENT_MENU, intent.getExtras().getInt(Constants.INTENT_MENU));
        targetIntent.putExtra(Constants.INTENT_DEMAND, demand);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack((new Intent(context, MainActivity.class)));
        stackBuilder.addNextIntent(targetIntent);

        int notificationId = demand.getId();

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "channel_alarm")
                .setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(bigTextTitle);
        bigTextStyle.bigText(bigText);
        bigTextStyle.setSummaryText(CommonUtils.formatDate(demand.getCreatedAt()));

        if (bigTextStyle != null) notificationBuilder.setStyle(bigTextStyle);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void attemptToSendLateWarning(Demand demand, Context context) {
        if(CommonUtils.isOnline(context)) {
            if (mDueTimeTask == null){
                mDueTimeTask = new DueTimeTask(demand);
                mDueTimeTask.execute("late-warning");
            }
        } else {
            CommonUtils.handleLater(demand,Constants.UPDATE_JOB_TAG, context);
        }
    }

    private void attemptToMarkDemandAsLate(Demand demand, Context context) {
        if(CommonUtils.isOnline(context)) {
            if (mDueTimeTask == null){
                mDueTimeTask = new DueTimeTask(demand);
                mDueTimeTask.execute("mark-as-late");
            }
        } else {
            //CommonUtils.handleLater(demand,Constants.UPDATE_JOB_TAG, context);
        }
    }

    private void attemptToChangeStatus(Demand demand, Context context){
        Log.e(TAG, "Demand status:" + demand.getStatus());

        // Mark locally.
        CommonUtils.updateColumnDB(
                FeedReaderContract.DemandEntry.COLUMN_NAME_LATE,
                "" + demand.isLate(),
                demand,
                Constants.UPDATE_STATUS,
                context
        );

        // Attempt to mark on server
        if(CommonUtils.isOnline(context)) {
            if (mStatusTask == null){
                mStatusTask = new StatusTask(demand.getId(), demand.getStatus());
                mStatusTask.execute();
            }
        } else {
            CommonUtils.handleLater(demand,Constants.UPDATE_JOB_TAG, context);
        }
    }

    public class StatusTask extends AsyncTask<Void, Void, String> {
        private int id;
        private String status;

        public StatusTask(int id, String status) {
            this.id = id;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", id);
            values.put("status", status);
            return CommonUtils.POST("/demand/set-status/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e(TAG, jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Log.e(TAG, "Server problem!");
                e.printStackTrace();
            }

            if (success) {
                Log.e(TAG, "Status successfully changed.");
                // No need to broadcast change, since local changes are made before server.
            } else {
                Log.e(TAG, "Server problem!");
            }
        }
    }

    // TODO: Criar um Job services pra isso!
    public class DueTimeTask extends AsyncTask<String, Void, String> {
        private Demand demand;

        public DueTimeTask(Demand demand) {
            this.demand = demand;
        }

        @Override
        protected String doInBackground(String... strings) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demand.getId());
            String url = "";
            switch (strings[0]) {
                case "late-warning":
                    url = "late-warning";
                    break;
                case "mark-as-late":
                    url = "late";
                    break;
                default:
            }
            return CommonUtils.POST("/send/" + url, values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDueTimeTask = null;
            Log.d(TAG, "(DueTimeTask) response: " + s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    int type = jsonObject.getInt("type");
                    switch (type) {
                        case Constants.WARN_DUE_TIME_ALARM_TAG:
                            //do something.
                            Log.d(TAG, "(DueTimeTask) warn due time sent!");
                            break;
                        case Constants.DUE_TIME_ALARM_TAG:
                            Log.d(TAG, "(DueTimeTask) due time sent!");
                            //do another something;
                            break;
                        default:
                            Log.e(TAG, "(DueTimeTask) post type unexpected!!!");
                    }
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
