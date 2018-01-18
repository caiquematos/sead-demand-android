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

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "On AlarmReceiver");
        String type = intent.getType();
        Log.e(TAG, "Alarm Type:" + type);
        Demand demand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
        Log.e(TAG, "Demand intent:" + demand.toString());
        String title = "";
        int drawable = -1;
        String status = Constants.UNDEFINE_STATUS;
        boolean shouldStatusChange = true;

        switch (type) {
            case Constants.POSTPONE_ALARM_TAG:
                title = "Avaliar Demanda!";
                drawable = R.drawable.ic_alarm_black_24dp;
                status = Constants.UNDEFINE_STATUS;
                break;
            case Constants.WARN_DUE_TIME_ALARM_TAG:
                title = "Expira Amanhã!";
                drawable = R.drawable.ic_alarm_black_24dp;
                shouldStatusChange = false;
                break;
            case Constants.DUE_TIME_ALARM_TAG:
                title = "Fim do Prazo! (atrasada)";
                drawable = R.drawable.ic_alarm_off_black_24dp;
                status = Constants.LATE_STATUS;
                break;
        }

        if(shouldStatusChange) {
            // Change demand status.
            demand.setStatus(status);
            attemptToChangeStatus(demand, context);
        } else {
            Log.e(TAG, "status change false");
            CommonUtils.cancelDueTime(demand,context,Constants.WARN_DUE_TIME_ALARM_TAG);
            CommonUtils.setDueTime(demand,context);
        }

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
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(drawable)
                .setContentTitle(title)
                .setContentText(demand.getSubject())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void attemptToChangeStatus(Demand demand, Context context){
        Log.e(TAG, "Demand status:" + demand.getStatus());

        // Mark locally.
        CommonUtils.updateColumnDB(
                FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS,
                demand.getStatus(),
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

}
