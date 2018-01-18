package com.sead.demand.FCM;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by caiqu on 10/04/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private final String TAG = getClass().getSimpleName();
    private UpdateFcmTokenTask mUpdateFcmToken;
    private SharedPreferences mPrefs;

    /*
    * The app deletes Instance ID
    * The app is restored on a new device
    * The user uninstalls/reinstall the app
    * The user clears app data.
    * */

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        String userEmail = mPrefs.getString(Constants.LOGGED_USER_EMAIL, "");
        Log.d(TAG, "Shared Prefs User Email:" + userEmail);

        // In case there is no email address yet (save it anyway)
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Constants.GCM_TOKEN, refreshedToken);
        if (editor.commit()) {
            Log.d(TAG, "Shared Prefs Fcm token updated:" + mPrefs.getString(Constants.GCM_TOKEN, ""));
        } else {
            Log.d(TAG, "Shared Prefs Fcm token not updated!");
        }

        if(mUpdateFcmToken == null && !userEmail.isEmpty()) {
            mUpdateFcmToken = new UpdateFcmTokenTask(userEmail, refreshedToken);
            mUpdateFcmToken.execute();
        } else {
            return;
        }
    }

    class UpdateFcmTokenTask extends AsyncTask<Void,Void,String>{
        private String token;
        private String email;

        public UpdateFcmTokenTask(String email, String token){
            this.token = token;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "Trying to update gcm token...");
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("email", this.token);
            values.put("fcm", this.email);
            String response = CommonUtils.POST("/user/update-fcm", values);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mUpdateFcmToken = null;

            JSONObject jsonObject = null;
            boolean success = false;
            String fcmToken;

            Log.d(TAG, "string json: " + s);

            try {
                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(success){
                try {
                    fcmToken = jsonObject.getString("fcm");
                    Log.e(TAG, "Fcm token updated to:" + fcmToken);

                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(Constants.GCM_TOKEN, fcmToken);
                    if (editor.commit()) {
                        Log.d(TAG, "Shared Prefs Fcm token up:" + mPrefs.getString(Constants.GCM_TOKEN, ""));
                    } else {
                        Log.d(TAG, "Shared Prefs Fcm token not up!");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Fcm token could not update!");
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "User not found!");
            }

        }
    }

}
