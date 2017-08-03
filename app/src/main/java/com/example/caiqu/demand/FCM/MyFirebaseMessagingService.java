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
import com.example.caiqu.demand.Activities.RequestActivity;
import com.example.caiqu.demand.Activities.ViewDemandActivity;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.Reason;
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (false) {
                scheduleJob(remoteMessage.getData(),Constants.INSERT_JOB_TAG);
            } else {
                if (dataBelongToCurrentUser(remoteMessage.getData()))
                    handleNow(remoteMessage.getData());
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null && dataBelongToCurrentUser(remoteMessage.getData())) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification(), remoteMessage.getData());
        }
    }

    // Check if the data received from server was really sent to current user.
    private boolean dataBelongToCurrentUser(Map<String, String> data) {
        Bundle bundle = doMapToBundle(data);
        String type = bundle.getString("type");

        Log.d(TAG, "Data Type: " + type);

        try {
            JSONObject receiverJson = new JSONObject(bundle.get("receiver").toString());
            User receiver = User.build(receiverJson);

            if (amITheReceiver(receiver)) return true;
            if (amIThisReceiversSuperior(receiver)) return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Check if current user is this user's superior.
    private boolean amIThisReceiversSuperior(User receiver) {
        User currentUser = CommonUtils.getCurrentUserPreference(this);
        Log.e(TAG, "Current user:" + currentUser.getId() + " User receiver:" + receiver.getId());
        return currentUser.getId() == receiver.getSuperior();
    }

    // Check if current user is the receiver.
    private boolean amITheReceiver(User receiver) {
        User currentUser = CommonUtils.getCurrentUserPreference(this);
        Log.e(TAG, "Current user:" + currentUser.getId() + " User receiver:" + receiver.getId());
        return currentUser.getId() == receiver.getId();
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

        if( type.equals(Constants.UNLOCK_USER)){
            try {
                JSONObject userJson = new JSONObject(bundle.get("user").toString());
                User user = User.build(userJson);
                // Try to add user, but if it already exists, then it'll update it.
                CommonUtils.storeUserDB(user,type,this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

            JSONObject reasonJson;
            Reason reason;

            try {
                // Check if there is a reason attached to this demand.
                // If yes, create a reason object.
                if(bundle.get("reason") != null) {
                    reasonJson = new JSONObject(bundle.get("reason").toString());
                    reason = Reason.build(reasonJson);
                    Log.e(TAG, " reason:" + reason.toString());
                } else {
                    reason = null;
                }

                JSONObject senderJson = new JSONObject(bundle.get("sender").toString());
                JSONObject receiverJson = new JSONObject(bundle.get("receiver").toString());
                JSONObject demandJson = new JSONObject(bundle.get("demand").toString());

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                Demand demand = Demand.build(sender, receiver, reason, demandJson);

                Log.e(TAG, "demand:" + demand.toString()
                        + " sender:" + sender.toString()
                        + " receiver:" + receiver.toString()
                );

                switch (type) {
                    case Constants.INSERT_DEMAND_SENT:
                    case Constants.INSERT_DEMAND_RECEIVED:
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
                    case Constants.UPDATE_PRIOR:
                        CommonUtils.updateColumnDB(
                                FeedReaderContract.DemandEntry.COLUMN_NAME_PRIOR,
                                demandJson.getString(FeedReaderContract.DemandEntry.COLUMN_NAME_PRIOR),
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
        }

        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param notification FCM notification received.
     * @param data
     */
    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        // TODO: set notifications' tag field on server and handle it here (TAG: demand or user?).

        Intent intent;
        int notificationId;
        String title;
        int icon;
        NotificationCompat.BigTextStyle bigTextStyle = null;

        String dataType = data.get("type");
        if (dataType.equals(Constants.UNLOCK_USER)){
            intent = new Intent(this, RequestActivity.class);
            JSONObject userJson;
            User user = null;

            try {
                userJson = new JSONObject(data.get("user"));
                user = User.build(userJson);
                Log.e(TAG, "Json (Notification):" + user.toString());
                notificationId = user.getId();
            } catch (JSONException e) {
                notificationId = -1;
                e.printStackTrace();
            }
            title = notification.getTitle();
            icon = R.drawable.ic_account_box_white_24dp;

            intent.putExtra(Constants.INTENT_USER, user);
        } else {
            intent = new Intent(this, ViewDemandActivity.class);
            JSONObject senderJson;
            JSONObject receiverJson;
            JSONObject reasonJson;
            JSONObject demandJson;
            User sender;
            User receiver;
            Reason reason;
            Demand demand = null;

            try {

                // Check if there is a reason attached to this demand.
                if(data.get("reason") != null) {
                    reasonJson = new JSONObject(data.get("reason"));
                    reason = Reason.build(reasonJson);
                } else {
                    reason = null;
                }

                senderJson = new JSONObject(data.get("sender"));
                receiverJson = new JSONObject(data.get("receiver"));
                demandJson = new JSONObject(data.get("demand"));
                sender = User.build(senderJson);
                receiver = User.build(receiverJson);
                demand = Demand.build(sender,receiver,reason,demandJson);
                Log.e(TAG, "Json (Notification):"
                        + demand.toString()
                        + " sender:" + sender.toString()
                        + " receiver:" + receiver.toString()
                );

                notificationId = demand.getId();

            } catch (JSONException e) {
                notificationId = -1;
                e.printStackTrace();
            }

            intent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
            intent.putExtra(Constants.INTENT_PAGE, Integer.parseInt(data.get("page")));
            intent.putExtra(Constants.INTENT_MENU, Integer.parseInt(data.get("menu")));
            intent.putExtra(Constants.INTENT_DEMAND, demand);

            title = "Demanda " + notification.getTitle();
            icon = R.drawable.ic_business_center_black_24dp;

            bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle(demand.getSubject() + " " + notification.getTitle());
            bigTextStyle.bigText(notification.getBody() + "\n\n" + demand.getDescription());
            bigTextStyle.setSummaryText(CommonUtils.formatDate(demand.getCreatedAt()));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack((new Intent(this, MainActivity.class)));
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT
                );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        if (bigTextStyle != null) notificationBuilder.setStyle(bigTextStyle);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

}
