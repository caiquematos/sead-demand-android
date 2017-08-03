package com.example.caiqu.demand.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

public class FirstActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private GoogleApiAvailability mGoogleClient;
    private SharedPreferences mPrefs;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_first);

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        // Generate FCM Token and save it on Shared Preferences.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Token generated:" + token);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Constants.GCM_TOKEN, token);
        if (editor.commit()) {
            mToken = mPrefs.getString(Constants.GCM_TOKEN, "");
            Log.d(TAG, "Shared Prefs Fcm token saved:" + mToken);
        } else {
            Log.d(TAG, "Shared Prefs Fcm token not saved!");
        }

        if (isPlayServiceAvailable()) {
            try {
                checkLogin();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isPlayServiceAvailable(){
        mGoogleClient = GoogleApiAvailability.getInstance();
        int resultCode = mGoogleClient.isGooglePlayServicesAvailable(getApplicationContext());
        if ( resultCode == ConnectionResult.SUCCESS){
            return true;
        }else{
            if ( mGoogleClient.isUserResolvableError(resultCode)){
                mGoogleClient.getErrorDialog(this,resultCode,2404).show();
            }
            return false;
        }
    }

    private void checkLogin() throws InterruptedException {
        boolean isLogged = mPrefs.getBoolean(Constants.IS_LOGGED,false);
        Log.e(TAG, "User logged:" + isLogged);

        if (!isLogged){
            //this handler simulates an app first presentation
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }, 2000);

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

}
