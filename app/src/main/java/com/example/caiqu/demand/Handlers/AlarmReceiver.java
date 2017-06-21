package com.example.caiqu.demand.Handlers;

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

import com.example.caiqu.demand.Activities.MainActivity;
import com.example.caiqu.demand.Activities.ViewDemandActivity;
import com.example.caiqu.demand.Adapters.DemandAdapter;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

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
        Demand demand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
        Log.e(TAG, "Demand intent:" + demand.toString());

        Intent targetIntent = new Intent(context, ViewDemandActivity.class);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        targetIntent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
        targetIntent.putExtra(Constants.INTENT_PAGE, intent.getExtras().getInt(Constants.INTENT_PAGE));
        targetIntent.putExtra(Constants.INTENT_DEMAND, demand);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack((new Intent(context, MainActivity.class)));
        stackBuilder.addNextIntent(targetIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        int notificationId = demand.getId();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_alarm_add_black_24dp)
                .setContentTitle("Avaliar Demanda!")
                .setContentText(demand.getSubject())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        // Change demand back to Undefined.
        attemptToChangeStatus(demand, Constants.UNDEFINE_STATUS, context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void attemptToChangeStatus(Demand demand, String status, Context context){
        // Mark locally.
        CommonUtils.updateColumnDB(
                FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS,
                status,
                demand,
                Constants.UPDATE_STATUS,
                context
        );

        // Attempt to mark on server
        if(CommonUtils.isOnline(context)) {
            if (mStatusTask == null){
                mStatusTask = new StatusTask(demand.getId(), status);
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
                // No need to broadcast change, since local changes are made before server.
            } else {
                Log.e(TAG, "Server problem!");
            }
        }
    }

}
