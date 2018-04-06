package com.sead.demand.FCM;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sead.demand.Activities.MainActivity;
import com.sead.demand.Activities.RequestActivity;
import com.sead.demand.Activities.ViewDemandActivity;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by caiqu on 10/04/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = getClass().getSimpleName();

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
                if (true) // dataBelongToCurrentUser(remoteMessage.getData())
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
            JSONObject receiverJson;

            if (bundle.get("type").equals(Constants.UNLOCK_USER)) {
                receiverJson = new JSONObject(bundle.get("superior").toString());
                User receiver = User.build(receiverJson);
                if (amITheReceiver(receiver)) return true;
                return true; // TODO treat this after. Missing proper verification.
            } else if (bundle.get("type").equals(Constants.UPDATE_READ)){
                receiverJson = new JSONObject(bundle.get("sender").toString());
                User receiver = User.build(receiverJson);
                if (amITheReceiver(receiver)) return true;
            } else {
                receiverJson = new JSONObject(bundle.get("receiver").toString());
                User receiver = User.build(receiverJson);
                if (amITheReceiver(receiver)) return true;
                if (amIThisReceiversSuperior(receiver)) return true;
                return true;
                // this will always return true. TODO: Check tree of superiors online.
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Check if current user is this user's superior.
    private boolean amIThisReceiversSuperior(User receiver) {
        User currentUser = CommonUtils.getCurrentUserPreference(this);
        Log.e(TAG, "Current user:" + currentUser.getId() + " User receiver:" + receiver.getId());
        return currentUser.getId() == receiver.getSuperiorId();
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
        String title;
        String text;

        if (data.get("title") != null) title = data.get("title");
        else title = "Demanda SEAD";
        if (data.get("text") != null) text = data.get("text");
        else text = "Clique para abrir";

        Log.d(TAG, "Data Type:" + type + " title:" + title + " text:" + text);

        switch(type) {
            case Constants.UNLOCK_USER:
                unlockUser(bundle, type, title, text, data);
                break;
            case Constants.AUTH:
                handleAuthority(bundle, type);
                break;
            default:
                handleDemand(bundle, type, title, text, data);
        }

        Log.d(TAG, "Short lived task is done.");
    }

    private void handleDemand(Bundle bundle, String type, String title, String text, Map<String, String> data) {
        JSONObject reasonJson;
        JSONObject demandTypeJson;
        PredefinedReason reason = null;
        DemandType demandType = null;

        try {
            if(bundle.get("reason") != null) {
                reasonJson = new JSONObject(bundle.get("reason").toString());
                reason = PredefinedReason.build(reasonJson);
                Log.e(TAG, " reason:" + reason.toString());
            }

            if(bundle.get("demand_type") != null){
                Log.d(TAG, "bundle demand_type: " + bundle.get("demand_type").toString());
                demandTypeJson = new JSONObject(bundle.get("demand_type").toString());
                demandType = DemandType.build(demandTypeJson);
                Log.e(TAG, " type:" + demandType.toString());
            }

            JSONObject senderJson = new JSONObject(bundle.get("sender").toString());
            JSONObject receiverJson = new JSONObject(bundle.get("receiver").toString());
            JSONObject demandJson = new JSONObject(bundle.get("demand").toString());

            User sender = User.build(senderJson);
            User receiver = User.build(receiverJson);
            Demand demand = Demand.build(sender, receiver, reason, demandType, demandJson);

            Log.e(TAG, "demand:" + demand.toString()
                    + " sender:" + sender.toString()
                    + " receiver:" + receiver.toString()
            );

            switch (type) {
                case Constants.INSERT_DEMAND_RECEIVED:
                    Log.d(TAG, "demand received: " + demand.toString());
                    handleDeadlineAlarm(demand);
                case Constants.INSERT_DEMAND_SENT:
                case Constants.INSERT_DEMAND_ADMIN:
                    CommonUtils.storeDemandDB(demand, type, this);
                    generateNotification(title, text, data);
                    break;
                case Constants.UPDATE_DEMAND:
                    CommonUtils.updateDemandDB(demand, type, this);
                    generateNotification(title, text, data);
                    break;
                case Constants.UPDATE_STATUS:
                    handleUpdateStatus(demand, type, title, text, data);
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

    private void handleUpdateStatus(Demand demand, String type, String title, String text, Map<String, String> data) {
        Log.d(TAG, "Handle Now - Update Status:" + demand.getStatus());
        switch (demand.getStatus()) {
            case Constants.DEADLINE_ACCEPTED_STATUS:
                if (demand.getPostponed() != 0) {
                    Log.d(TAG, "demand postponed: " + demand.toString());
                    handleDeadlineAlarm(demand);
                }
                break;
            case Constants.REOPEN_STATUS:
                if (this.amITheReceiver(demand.getReceiver())) {
                    Log.d(TAG, "I am the receiver: " + demand.toString());
                    handleDeadlineAlarm(demand);
                }
        }
        CommonUtils.updateDemandDB(type, demand, this);
        generateNotification(title, text, data);
    }

    private void handleAuthority(Bundle bundle, String type) {
        try {
            JSONObject authJson = new JSONObject(bundle.get("authority").toString());
            Authority authority = Authority.build(authJson);

            String action = bundle.getString("action");
            if (action.equals("add")) {
                // Try to add authority, but if it already exists, then it'll update it.
                CommonUtils.storeAuthDB(authority,type,this);
                //generateNotification(title, text, data);
                Log.d(TAG, "ADD AUTH. End of the line. Implement notification later: " + authority.toString());
            } else {
                MyDBManager myDBManager = new MyDBManager(this);
                int count = myDBManager.deleteAuthById(authority.getId());
                Log.d(TAG, "REMOVE AUTH. count:" + count + ". Should remove auth from db: " + authority.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void unlockUser(Bundle bundle, String type, String title, String text, Map<String, String> data) {
        try {
            JSONObject jobJson = new JSONObject(bundle.get("job").toString());
            com.sead.demand.Entities.Job job = com.sead.demand.Entities.Job.build(jobJson);
            JSONObject superiorJson = new JSONObject(bundle.get("superior").toString());
            User superior = User.build(superiorJson);
            JSONObject userJson = new JSONObject(bundle.get("user").toString());
            User user = User.build(job, superior, userJson);
            CommonUtils.storeUserDB(user,type,this);
            generateNotification(title, text, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleDeadlineAlarm(Demand demand) {
        long demandDueTimeInMillis = demand.getDueTimeInMillis();
        int warnDueTimeInDays = Constants.DUE_TIME_PREVIOUS_WARNING;
        long warnDueTimeInMillis = warnDueTimeInDays * 24 * 3600 * 1000;
        Log.d(TAG, "Warn Time in Millis: " + warnDueTimeInMillis);
        long warnDemandTimeInMillis = demandDueTimeInMillis - warnDueTimeInMillis;
        Log.d(TAG, "Warn Demand in Millis: " + warnDemandTimeInMillis);

        // first, set the due time WARNING alarm.
        CommonUtils.setAlarm(
                this,
                warnDemandTimeInMillis,
                demand,
                Constants.WARN_DUE_TIME_ALARM_TAG,
                Constants.RECEIVED_PAGE,
                Constants.RECEIVER_MENU
        );
        // finally, set the DUE TIME alarm.
        CommonUtils.setAlarm(
                this,
                demandDueTimeInMillis,
                demand,
                Constants.DUE_TIME_ALARM_TAG,
                Constants.RECEIVED_PAGE,
                Constants.RECEIVER_MENU
        );
    }

    private void generateNotification(String title, String text, Map<String, String> data) {
        Log.d(TAG, "generate notification!");

        Intent intent;
        int notificationId;
        int icon;
        NotificationCompat.BigTextStyle bigTextStyle = null;
        PendingIntent resultPendingIntent;

        switch (data.get("type")) {
            case Constants.UNLOCK_USER:
                icon = R.drawable.ic_account_box_white_24dp;
                break;
            default:
                icon = R.drawable.ic_business_center_black_24dp;
        }

        if (data != null) {
            String dataType = data.get("type");
            if (dataType.equals(Constants.UNLOCK_USER)){
                intent = new Intent(this, RequestActivity.class);
                User user = getUser(data);
                if (user != null) {
                    notificationId = user.getId();
                    intent.putExtra(Constants.INTENT_USER, user);
                } else {
                    notificationId = -1;
                }
            } else {
                title = "Demanda " + title;
                intent = new Intent(this, ViewDemandActivity.class);
                Demand demand = getDemand(data);
                if (demand != null) {
                    notificationId = demand.getId();
                    intent.putExtra(Constants.INTENT_DEMAND, demand);
                    bigTextStyle = new NotificationCompat.BigTextStyle();
                    bigTextStyle.setBigContentTitle(demand.getSubject() + " " + title);
                    bigTextStyle.bigText(demand.getSubject() + "\n\n" + demand.getDescription());
                    bigTextStyle.setSummaryText(CommonUtils.formatDate(demand.getCreatedAt()));
                } else {
                    notificationId = -1;
                }

                intent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
                intent.putExtra(Constants.INTENT_PAGE, Integer.parseInt(data.get("page")));
                intent.putExtra(Constants.INTENT_MENU, Integer.parseInt(data.get("menu")));

            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        } else {
            notificationId = -1;
            intent = null;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack((new Intent(this, MainActivity.class)));
        stackBuilder.addNextIntent(intent);

        resultPendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        if (bigTextStyle != null) notificationBuilder.setStyle(bigTextStyle);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param notification FCM notification received.
     * @param data
     */
    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        // TODO: set notifications' tag field on server and handle it here (TAG: demand or user?).

        Log.d(TAG, "send notification data:" + data.toString());

        Intent intent;
        int notificationId;
        String title;
        int icon;
        NotificationCompat.BigTextStyle bigTextStyle = null;
        PendingIntent resultPendingIntent;

        icon = R.drawable.ic_business_center_black_24dp;

        /*
        switch (notification.getIcon().toString()) {
            case "business":
                icon = R.drawable.ic_business_center_black_24dp;
                break;
            case "account":
                icon = R.drawable.ic_account_box_white_24dp;
                break;
            default:
                icon = R.drawable.ic_business_center_black_24dp;
        }
        */

        if (data != null) {
            String dataType = data.get("type");
            if (dataType.equals(Constants.UNLOCK_USER)){
                title = notification.getTitle();
                intent = new Intent(this, RequestActivity.class);
                User user = getUser(data);
                if (user != null) {
                    notificationId = user.getId();
                    intent.putExtra(Constants.INTENT_USER, user);
                } else {
                    notificationId = -1;
                }
            } else {
                title = "Demanda " + data.get("type") + " - " + notification.getTitle();
                intent = new Intent(this, ViewDemandActivity.class);
                Demand demand = getDemand(data);
                if (demand != null) {
                    notificationId = demand.getId();
                    intent.putExtra(Constants.INTENT_DEMAND, demand);
                    bigTextStyle = new NotificationCompat.BigTextStyle();
                    bigTextStyle.setBigContentTitle(demand.getSubject() + " " + notification.getTitle());
                    bigTextStyle.bigText(notification.getBody() + "\n\n" + demand.getDescription());
                    bigTextStyle.setSummaryText(CommonUtils.formatDate(demand.getCreatedAt()));
                } else {
                    notificationId = -1;
                }

                intent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
                intent.putExtra(Constants.INTENT_PAGE, Integer.parseInt(data.get("page")));
                intent.putExtra(Constants.INTENT_MENU, Integer.parseInt(data.get("menu")));

            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        } else {
            title = notification.getTitle();
            notificationId = -1;
            intent = null;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack((new Intent(this, MainActivity.class)));
        stackBuilder.addNextIntent(intent);

        resultPendingIntent = stackBuilder.getPendingIntent(
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

    private Demand getDemand(Map<String, String> data) {
        JSONObject senderJson;
        JSONObject receiverJson;
        JSONObject reasonJson;
        JSONObject demandTypeJson;
        JSONObject demandJson;
        User sender;
        User receiver;
        DemandType demandType;
        PredefinedReason reason;
        Demand demand;

        try {
            // Check if there is a reason attached to this demand.
            if(data.get("reason") != null) {
                reasonJson = new JSONObject(data.get("reason"));
                reason = PredefinedReason.build(reasonJson);
            } else {
                reason = null;
            }

            if(data.get("reason") != null) {
                demandTypeJson = new JSONObject(data.get("demand_type"));
                demandType = DemandType.build(demandTypeJson);
            } else {
                demandType = null;
            }

            senderJson = new JSONObject(data.get("sender"));
            receiverJson = new JSONObject(data.get("receiver"));
            demandJson = new JSONObject(data.get("demand"));
            sender = User.build(senderJson);
            receiver = User.build(receiverJson);
            demand = Demand.build(sender,receiver,reason,demandType,demandJson);
            Log.e(TAG, "Json (Notification):"
                    + demand.toString()
                    + " sender:" + sender.toString()
                    + " receiver:" + receiver.toString()
            );
        } catch (JSONException e) {
            e.printStackTrace();
            demand = null;
        }

        return demand;
    }

    private User getUser( Map<String, String> data) {
        JSONObject userJson;
        User user;

        try {
            userJson = new JSONObject(data.get("user"));
            user = User.build(userJson);
            Log.e(TAG, "Json (Notification):" + user.toString());
        } catch (JSONException e) {
            user = null;
            e.printStackTrace();
        }

        return user;
    }
}
