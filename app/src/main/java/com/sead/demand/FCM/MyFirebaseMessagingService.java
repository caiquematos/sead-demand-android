package com.sead.demand.FCM;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.Calendar;
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
            try {
                sendNotification(remoteMessage.getNotification(), doMapToBundle(remoteMessage.getData()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        Bundle bundleData = doMapToBundle(data);
        String type = bundleData.getString("type");
        String title;
        String text;
        if (data.get("title") != null) title = data.get("title");
        else title = "Demanda SEAD";
        if (data.get("text") != null && !data.get("text").isEmpty()) text = data.get("text");
        else text = "Clique para abrir";

        Log.d(TAG, "(handleNow) DataType:" + type + " title:" + title + " text:" + text);

        switch(type) {
            case Constants.UNLOCK_USER:
                unlockUser(bundleData, type, title, text);
                break;
            case Constants.AUTH:
                handleAuthority(bundleData, type);
                break;
            default:
                handleDemand(bundleData, type, title, text);
        }

        Log.d(TAG, "(handleNow) done!");
    }

    private void handleDemand(Bundle data, String type, String title,
                              String text) {
        try {
            Demand demand = generateDemand(data);
            String bigTextTitle = demand.getSubject();
            String bigText =  title + "\nDe: " + demand.getSender().getName() + "\n\n" + demand.getDescription();

            Log.d(TAG, "(handleDemand) demand:" + demand.toString()
                    + "\nsender:" + demand.getSender().toString()
                    + "\nreceiver:" + demand.getReceiver().toString()
            );

            switch (type) {
                case Constants.INSERT_DEMAND_RECEIVED:
                    handleDeadlineAlarm(demand);
                    Log.d(TAG, "handleDemand: on " + Constants.INSERT_DEMAND_RECEIVED);
                case Constants.INSERT_DEMAND_SENT:
                    Log.d(TAG, "handleDemand: on " + Constants.INSERT_DEMAND_SENT);
                case Constants.INSERT_DEMAND_ADMIN:
                    Log.d(TAG, "handleDemand: on " + Constants.INSERT_DEMAND_ADMIN);
                    CommonUtils.storeDemandDB(demand, type, this);
                    handleNotificationType(title, text, bigTextTitle, bigText, data);
                    break;
                case Constants.UPDATE_DEMAND:
                    CommonUtils.updateDemandDB(demand, type, this);
                    handleNotificationType(title, text, bigTextTitle, bigText, data);
                    break;
                case Constants.UPDATE_STATUS:
                    handleUpdateStatus(demand, type, title, text, bigTextTitle, bigText, data);
                    break;
                case Constants.UPDATE_READ:
                    CommonUtils.updateColumnDB(
                            FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN,
                            demand.getSeen(),
                            demand,
                            type,
                            this
                    );
                    break;
                case Constants.LATE_WARNING:
                    bigTextTitle = title;
                    bigText = text;
                    handleNotificationType(title, text, bigTextTitle, bigText, data);
                    break;
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private Demand generateDemand(Bundle bundle) throws JSONException {
        JSONObject reasonJson;
        JSONObject demandTypeJson;
        PredefinedReason reason = null;
        DemandType demandType = null;

        if(bundle.get("reason") != null) {
            reasonJson = new JSONObject(bundle.get("reason").toString());
            reason = PredefinedReason.build(reasonJson);
            Log.d(TAG, "(handleDemand) reason:" + reason.toString());
        } else {
            Log.e(TAG, "(handleDemand) reason is null");
        }

        if(bundle.get("demand_type") != null){
            demandTypeJson = new JSONObject(bundle.get("demand_type").toString());
            demandType = DemandType.build(demandTypeJson);
            Log.d(TAG, "(handleDemand) demand type:" + demandType.toString());
        } else {
            Log.e(TAG, "(handleDemand) demand type is null");
        }

        JSONObject senderJson = new JSONObject(bundle.get("sender").toString());
        JSONObject receiverJson = new JSONObject(bundle.get("receiver").toString());
        JSONObject demandJson = new JSONObject(bundle.get("demand").toString());

        User sender = User.build(senderJson);
        User receiver = User.build(receiverJson);
        return Demand.build(sender, receiver, reason, demandType, demandJson);
    }

    private void handleUpdateStatus(Demand demand, String type, String title,
                                    String text, String bigTextTitle, String bigText, Bundle data) throws JSONException {
        Log.d(TAG, "(handleUpdateStatus) demand status:" + demand.getStatus());
        switch (demand.getStatus()) {
            case Constants.DEADLINE_ACCEPTED_STATUS:
                if (demand.getPostponed() != 0) {
                    handleDeadlineAlarm(demand);
                }
                break;
            case Constants.REOPEN_STATUS:
                if (this.amITheReceiver(demand.getReceiver())) {
                    handleDeadlineAlarm(demand);
                }
        }
        CommonUtils.updateDemandDB(type, demand, this);
        handleNotificationType(title, text, bigTextTitle, bigText, data);
    }

    private void handleAuthority(Bundle bundle, String type) {
        try {
            JSONObject authJson = new JSONObject(bundle.get("authority").toString());
            Authority authority = Authority.build(authJson);

            String action = bundle.getString("action");
            if (action.equals("add")) {
                // Try to add authority, but if it already exists, then it'll update it.
                CommonUtils.storeAuthDB(authority,type,this);
                //handleNotificationType(title, text, data);
                Log.d(TAG, "(handleAuthority) added authority: " + authority.toString());
            } else {
                MyDBManager myDBManager = new MyDBManager(this);
                int count = myDBManager.deleteAuthById(authority.getId());
                Log.d(TAG, "(handleAuthority) removed authority. count:" + count + ". authority: " + authority.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void unlockUser(Bundle data, String type, String title, String text) {
        Log.d(TAG, "(unlockUser)");
        try {
            User user = generateUser(data);
            CommonUtils.storeUserDB(user,type,this);
            handleNotificationType(title, text, null, null, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private User generateUser(Bundle data) throws JSONException {
        com.sead.demand.Entities.Job job = null;
        if (data.containsKey("job")) {
             JSONObject jobJson = new JSONObject(data.get("job").toString());
            job = com.sead.demand.Entities.Job.build(jobJson);
            Log.e(TAG, "(generateUser) job: " + job.toString());
        } else {
            Log.e(TAG, "(generateUser) job is null");
        }

        User superior = null;
        if (data.containsKey("superior")) {
            JSONObject superiorJson = new JSONObject(data.get("superior").toString());
            superior = User.build(superiorJson);
            Log.e(TAG, "(generateUser) superior: " + superior.toString());
        } else {
            Log.e(TAG, "(generateUser) superior is null");
        }

        JSONObject userJson = new JSONObject(data.get("user").toString());
        return User.build(job, superior, userJson);
    }

    private void handleDeadlineAlarm(Demand demand) {
        Log.d(TAG, "(handleDeadlineAlarm)");
        long demandDueTimeInMillis = demand.getDueTimeInMillis();
        int warnDueTimeInDays = Constants.DUE_TIME_PREVIOUS_WARNING;
        long warnDueTimeInMillis = warnDueTimeInDays * 24 * 3600 * 1000;
        Log.d(TAG, "(handleDeadlineAlarm) Warn Time in Millis: " + warnDueTimeInMillis);
        long warnDemandTimeInMillis = demandDueTimeInMillis - warnDueTimeInMillis;
        Log.d(TAG, "(handleDeadlineAlarm) Warn Demand in Millis: " + warnDemandTimeInMillis);

        /* only for test purpose */
        //Calendar c = Calendar.getInstance();
        //c.add(Calendar.MINUTE, 1);
        //Log.d(TAG, "(handleDeadlineAlarm) due time warn (test): " + CommonUtils.convertMillisToDate(c.getTimeInMillis())
        //        + " " + CommonUtils.convertMillisToTime(c.getTimeInMillis()));
        /* only for test purpose */

        // first, set the due time WARNING alarm.
        CommonUtils.setAlarm(
                this,
                warnDemandTimeInMillis,
                demand,
                Constants.WARN_DUE_TIME_ALARM_TAG,
                Constants.RECEIVED_PAGE,
                Constants.RECEIVER_MENU
        );
        /* only for test purpose */
        //c.add(Calendar.MINUTE, 1);
        //Log.d(TAG, "(handleDeadlineAlarm) due time (test): " + CommonUtils.convertMillisToDate(c.getTimeInMillis())
        //        + " " + CommonUtils.convertMillisToTime(c.getTimeInMillis()));
        /* only for test purpose */
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

    private void handleNotificationType(String title, String text, String bigTextTitle,
                                        String bigText, Bundle data) throws JSONException {
        Log.d(TAG, "(handleNotificationType)");
        int notificationId = -1;

        if (data != null) {
            String dataType = data.getString("type");

            switch (dataType) {
                case Constants.UNLOCK_USER:
                    setUserNotification(
                            data,
                            notificationId,
                            title,
                            text
                    );
                    break;
                default:
                    setDemandNotification(
                            data,
                            title,
                            text,
                            bigTextTitle,
                            bigText
                    );
            }
        } else {
           Log.e(TAG, "(handleNotificationType) data is null!");
        }
    }

    private void setDemandNotification(Bundle data, String title, String text,
                                       String bigTextTitle, String bigText) throws JSONException {
        int icon = R.drawable.ic_business_center_black_24dp;
        title = "Demanda " + title;
        NotificationCompat.BigTextStyle bigTextStyle = null;
        Intent intent = new Intent(this, ViewDemandActivity.class);
        Demand demand = generateDemand(data);
        int notificationId;
        if (demand != null) {
            notificationId = demand.getId();
            intent.putExtra(Constants.INTENT_DEMAND, demand);
            bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle(bigTextTitle);
            bigTextStyle.bigText(bigText);
            bigTextStyle.setSummaryText(CommonUtils.formatDate(demand.getCreatedAt()));
        } else {
            notificationId = -1;
        }

        intent.putExtra(Constants.INTENT_ACTIVITY, getClass().getSimpleName());
        intent.putExtra(Constants.INTENT_PAGE, Integer.valueOf(data.getString("page")));
        intent.putExtra(Constants.INTENT_MENU, Integer.valueOf(data.getString("menu")));

        startNotification(intent, icon, notificationId, title, text, bigTextStyle);
    }

    private void setUserNotification(Bundle data, int notificationId,
                                     String title, String text) throws JSONException {
        int icon = R.drawable.ic_account_box_white_24dp;
        Intent intent = new Intent(this, RequestActivity.class);
        User user = generateUser(data);
        if (user != null) {
            notificationId = user.getId();
            intent.putExtra(Constants.INTENT_USER, user);
        }
        startNotification(intent, icon, notificationId, title, text, null);
    }

    private void startNotification(Intent intent, int icon, int notificationId, String title,
                                   String text, NotificationCompat.BigTextStyle bigTextStyle) {
        PendingIntent resultPendingIntent;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack((new Intent(this, MainActivity.class)));
        stackBuilder.addNextIntent(intent);

        resultPendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = getString(R.string.general_channel_id);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.general_channel_name);
            String Description = getString(R.string.general_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        if (bigTextStyle != null) notificationBuilder.setStyle(bigTextStyle);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     * @param notification FCM notification received.
     * @param data
     */
    private void sendNotification(RemoteMessage.Notification notification, Bundle data) throws JSONException {
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
            String dataType = data.getString("type");
            if (dataType.equals(Constants.UNLOCK_USER)){
                title = notification.getTitle();
                intent = new Intent(this, RequestActivity.class);
                User user = generateUser(data);
                if (user != null) {
                    notificationId = user.getId();
                    intent.putExtra(Constants.INTENT_USER, user);
                } else {
                    notificationId = -1;
                }
            } else {
                title = "Demanda " + data.get("type") + " - " + notification.getTitle();
                intent = new Intent(this, ViewDemandActivity.class);
                Demand demand = generateDemand(data);
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
                intent.putExtra(Constants.INTENT_PAGE, Integer.valueOf(data.getInt("page")));
                intent.putExtra(Constants.INTENT_MENU, Integer.valueOf(data.getInt("menu")));

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

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = getString(R.string.general_channel_id);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.general_channel_name);
            String Description = getString(R.string.general_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        if (bigTextStyle != null) notificationBuilder.setStyle(bigTextStyle);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
