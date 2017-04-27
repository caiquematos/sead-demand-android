package com.example.caiqu.demand.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirstActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private GoogleApiAvailability mGoogleClient;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_first);

        //TODO: GET FCM TOKEN AND CHECK IF IT SYNCHRONOUS OR ASSYNCHRONOUS
        if (isPlayServiceAvailable()) {
            try {
                isUserLoggedIn();
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

    private void isUserLoggedIn() throws InterruptedException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isLogged = prefs.getBoolean(Constants.IS_LOGGED,false);

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
