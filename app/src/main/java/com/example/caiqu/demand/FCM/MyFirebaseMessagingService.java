package com.example.caiqu.demand.FCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.caiqu.demand.Activities.MainActivity;
import com.example.caiqu.demand.Activities.ViewDemandActivity;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by caiqu on 10/04/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = getClass().getSimpleName();
    private final int PAGE = 3;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (false) {
                scheduleJob(remoteMessage.getData(),Constants.INSERT_JOB_TAG);
            } else {
                handleNow(remoteMessage.getData());
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification(), remoteMessage.getData());
        }

    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     * @param data
     */
    private void scheduleJob(Map<String, String> data, String tag) {
        Bundle myExtrasBundle = doMapToBundle(data);
        Log.e(TAG, "Bundle:" + myExtrasBundle.toString());

        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag(tag)
                .setReplaceCurrent(false)
                .setExtras(myExtrasBundle)
                .build();
        dispatcher.schedule(myJob);
    }

    private Bundle doMapToBundle(Map<String, String> data) {
        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return bundle;
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     * @param data
     */
    private void handleNow(Map<String, String> data) {
        Bundle bundle = doMapToBundle(data);
        String type = bundle.getString("type");

        Log.d(TAG, "Data Type: " + type);

        try {
            JSONObject senderJson = new JSONObject(bundle.get("sender").toString());
            JSONObject receiverJson = new JSONObject(bundle.get("receiver").toString());
            JSONObject demandJson = new JSONObject(bundle.get("demand").toString());

            User sender = User.build(senderJson);
            User receiver = User.build(receiverJson);
            Demand demand = Demand.build(sender, receiver, demandJson);

            Log.e(TAG, "demand:" + demand.toString()
                    + " sender:" + sender.toString()
                    + " receiver:" + receiver.toString()
            );

            switch (data.get("type")) {
                case Constants.INSERT_DEMAND_RECEIVED:
                    Log.d(TAG, "Create method to store user type demands.");
                    break;
                case Constants.INSERT_DEMAND_ADMIN:
                    CommonUtils.storeDemandDB(demand, type, this);
                    break;
                case Constants.UPDATE_DEMAND:
                    CommonUtils.updateDemandDB(demand, type, this);
                    break;
                case Constants.UPDATE_STATUS:
                    CommonUtils.updateColumnDB(
                            FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS,
                            demandJson.getString(FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS),
                            demand,
                            type,
                            this
                            );
                    break;
                case Constants.UPDATE_IMPORTANCE:
                    CommonUtils.updateColumnDB(
                            FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE,
                            demandJson.getString(FeedReaderContract.DemandEntry.COLUMN_NAME_IMPORTANCE),
                            demand,
                            type,
                            this
                    );
                    break;
                case Constants.UPDATE_READ:
                    CommonUtils.updateColumnDB(
                            FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN,
                            demandJson.getString(FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN),
                            demand,
                            type,
                            this
                    );
                    break;
            }

        } catch (JSONException e){
            e.printStackTrace();
        }

        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param notification FCM notification received.
     * @param data
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        Intent intent = new Intent(this, ViewDemandActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        JSONObject senderJson;
        JSONObject receiverJson;
        JSONObject demandJson;
        User sender;
        User receiver;
        Demand demand = null;
                
        try {
            senderJson = new JSONObject(data.get("sender"));
            receiverJson = new JSONObject(data.get("receiver"));
            demandJson = new JSONObject(data.get("demand"));
            sender = User.build(senderJson);
            receiver = User.build(receiverJson);
            demand = Demand.build(sender,receiver,demandJson);
            Log.e(TAG, "Json (Notification):"
                    + demand.toString()
                    + " sender:" + sender.toString()
                    + " receiver:" + receiver.toString()
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int notificationId = demand.getId();

        intent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
        intent.putExtra(Constants.INTENT_PAGE, Integer.parseInt(data.get("page")));
        intent.putExtra(Constants.INTENT_MENU, Integer.parseInt(data.get("menu")));
        intent.putExtra(Constants.INTENT_DEMAND, demand);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack((new Intent(this, MainActivity.class)));
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT
                );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_business_center_black_24dp)
                .setContentTitle("Demanda " + notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(demand.getSubject() + " " + notification.getTitle());
        bigTextStyle.bigText(notification.getBody() + "\n\n" + demand.getDescription());
        bigTextStyle.setSummaryText(CommonUtils.formatDate(demand.getCreatedAt()));

        notificationBuilder.setStyle(bigTextStyle);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

}
