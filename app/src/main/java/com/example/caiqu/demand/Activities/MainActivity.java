package com.example.caiqu.demand.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.caiqu.demand.Adapters.FixedTabsPageAdapter;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = getClass().getSimpleName();
    private ProgressDialog mPDLogout;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;
    private FixedTabsPageAdapter mPagerAdapter;
    private SharedPreferences mPrefs;
    private String mUserJobPosition;
    private Activity mActivity;
    private LogoutTask mLogoutTask;
    private User mCurrentUser;
    private AlertDialog.Builder mLogoffAlert;

    public MainActivity() {
        this.mActivity = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        mFab = (FloatingActionButton) findViewById(R.id.main_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, CreateDemandActivity.class);
                startActivity(mainIntent);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.mainPager);
        mPagerAdapter= new FixedTabsPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        tabLayout.setTabTextColors(ContextCompat.getColor(this,R.color.transwhite), Color.WHITE);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_call_received_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_call_made_white_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_supervisor_account_white_24dp);

        // If this user belongs to "ponta" job position, hide admin tab.
        mUserJobPosition = mPrefs.getString(Constants.LOGGED_USER_JOB_POSITION,"");
        Log.e(TAG, "on create " + mUserJobPosition);
        if (mUserJobPosition.equals(Constants.JOB_POSITIONS[0])) tabLayout.removeTabAt(2);

        // Fetch info about current user logged
        try {
            JSONObject userJson = new JSONObject(mPrefs.getString(Constants.USER_PREFERENCES, ""));
            mCurrentUser = User.build(userJson);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get user from preferences!!!");
        }
    }

    public android.support.v4.app.Fragment getFragment(int pos){
        return mPagerAdapter.getItem(pos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, "on create options " + mUserJobPosition);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if( mUserJobPosition.equals(Constants.JOB_POSITIONS[0]))
            menu.setGroupVisible(R.id.main_admin_group,false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logoff) {
            mLogoffAlert = new AlertDialog.Builder(this);
            mLogoffAlert.setTitle(getString(R.string.logoff_alert_title));
            mLogoffAlert.setMessage(getString(R.string.logoff_warning));
            mLogoffAlert.setPositiveButton("sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    attemptLogout();
                }
            });
            mLogoffAlert.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            mLogoffAlert.create();
            mLogoffAlert.show();
            return true;
        }

        if (id == R.id.main_admin_requests) {
            intent = new Intent(getApplicationContext(), RequestActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.main_archived_demands) {
            intent = new Intent(getApplicationContext(), ArchiveActivity.class);
            startActivity(intent);
            return true;
        }

        String status = "";
        intent = new Intent(getApplicationContext(), StatusActivity.class);

        switch(id){
            case R.id.main_admin_done:
                status = Constants.DONE_STATUS;
                break;
            case R.id.main_admin_accepted:
                status = Constants.ACCEPT_STATUS;
                break;
            case R.id.main_admin_rejected:
                status = Constants.REJECT_STATUS;
                break;
            case R.id.main_admin_cancelled:
                status = Constants.CANCEL_STATUS;
                break;
            case R.id.main_admin_postponed:
                status = Constants.POSTPONE_STATUS;
                break;
        }

        intent.putExtra("TYPE", Constants.INTENT_ADMIN_TYPE); // Demands viewed as Admin
        intent.putExtra("STATUS", status);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    private void attemptLogout() {
        if (!CommonUtils.isOnline(mActivity)) {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        if (mLogoutTask == null){
            mLogoutTask = new LogoutTask(mCurrentUser.getEmail());
            mLogoutTask.execute();
        } else {
            return;
        }
    }


    public class LogoutTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;

        LogoutTask(String email) {
            mEmail = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDLogout = new ProgressDialog(mActivity);
            mPDLogout.setMessage("Por favor aguarde...");
            mPDLogout.setCancelable(false);
            mPDLogout.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("email", mEmail);
            String response = CommonUtils.POST("/user/logout/", values);
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            mLogoutTask = null;
            JSONObject jsonObject;
            JSONObject userJson;
            User user = null;
            boolean success = false;

            Log.d(TAG, "string json response: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                userJson = jsonObject.getJSONObject("user");

                user = User.build(userJson);
                Log.d(TAG, "user response email:" + user.toString());
            } catch (JSONException e) {
                Snackbar.make(mFab, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (success && user != null) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(Constants.IS_LOGGED,false);
                if (editor.commit()){
                    Log.d(TAG,"User logged off prefs:" + mPrefs.getBoolean(Constants.IS_LOGGED,false));
                } else {
                    Log.d(TAG,"Could not save prefs!");
                }
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                if (mPDLogout.isShowing()){
                    mPDLogout.dismiss();
                }
                resetAppData();
                startActivity(intent);
                finish();
            } else {
                if (mPDLogout.isShowing()){
                    mPDLogout.dismiss();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mLogoutTask = null;
            if (mPDLogout.isShowing()){
                mPDLogout.dismiss();
            }
        }
    }

    private void resetAppData() {
        Log.e(TAG, "Reset App data.");
        //TODO: Reset all data.
    }

    @Override
    public void onClick(View v) {

    }
}
