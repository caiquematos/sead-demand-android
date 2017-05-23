package com.example.caiqu.demand.FCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.caiqu.demand.Activities.FirstActivity;
import com.example.caiqu.demand.Activities.MainActivity;
import com.example.caiqu.demand.Activities.ViewDemandActivity;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.google.android.gms.gcm.Task;
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

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            //if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            //    scheduleJob();
            //} else {
                // Handle message within 10 seconds
            //    handleNow();
            //}
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification(), remoteMessage.getData());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
// message, here is where that should be initiated. See sendNotification method below.
    }

    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        /*// [START dispatch_job]
        Firebase dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job] */
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
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

        JSONObject jsonObject = null;
        Demand demand = null;
                
        try {
            jsonObject = new JSONObject(data.get("demand"));
            Log.e(TAG, "Json Noti:" + jsonObject.toString());
            demand = new Demand(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("ACTIVITY", getClass().getSimpleName());
        intent.putExtra("PAGE", Integer.parseInt(data.get("page")));
        intent.putExtra("DEMAND", demand.getId());
        intent.putExtra("SUBJECT", demand.getSubject());
        intent.putExtra("STATUS", demand.getStatus());
        intent.putExtra("SENDERNAME", demand.getFrom());
        intent.putExtra("SENDEREMAIL", demand.getFromEmail());
        intent.putExtra("SEEN", demand.getSeen());
        intent.putExtra("DESCRIPTION", demand.getDescription());
        intent.putExtra("TIME", CommonUtils.formatDate(demand.getCreatedAt()));
        intent.putExtra("IMPORTANCE", demand.getImportance());
        intent.putExtra("RECEIVERNAME", demand.getTo());
        intent.putExtra("RECEIVEREMAIL", demand.getToEmail());
        Log.d(TAG, demand.getSubject() + " Importance:" + demand.getImportance()
                + " PACKAGE:"  + getClass().getSimpleName() + " Page:" + PAGE
                + " Seen:" + demand.getSeen());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack((new Intent(this, MainActivity.class)));
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
                );

      //  PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_business_center_black_24dp)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Integer.parseInt(data.get("noteId")) /* ID of notification */, notificationBuilder.build());
    }

}
