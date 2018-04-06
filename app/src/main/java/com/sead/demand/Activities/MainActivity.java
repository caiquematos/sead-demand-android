package com.sead.demand.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sead.demand.Adapters.FixedTabsPageAdapter;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        User user = CommonUtils.getCurrentUserPreference(this);

        /** Set navigation drawer **/
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.nav_title);
        name.setText(user.getName());
        TextView email = (TextView) header.findViewById(R.id.nav_email);
        email.setText(user.getEmail());
        ImageView image = (ImageView) header.findViewById(R.id.nav_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
                intent.putExtra("mode", "me");
                startActivity(intent);
            }
        });
        // TODO: Try to retrieve pic from db, to resize check "taking photos simply" android dev and Loading Large Bitmaps Efficiently.
        /** Set navigation drawer **/

        mFab = (FloatingActionButton) findViewById(R.id.main_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CommonUtils.isOnline(mActivity)) {
                    Intent mainIntent = new Intent(MainActivity.this, CreateDemandActivity.class);
                    startActivity(mainIntent);
                } else {
                    Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG).show();
                }
            }
        });

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
        if (mUserJobPosition.equals(Constants.JOB_POSITIONS[0])) {
            navigationView.getMenu().findItem(R.id.nav_admin_items).setVisible(false);
            tabLayout.removeTabAt(2);
        }

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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Make admin's menu hide if user job position is 'ponta'.
        if( mUserJobPosition.equals(Constants.JOB_POSITIONS[0])) {
            menu.setGroupVisible(R.id.main_admin_group,false);
        }

        // Associate searchable configuration with the SearchView.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        // handle visibility of other items.
        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                setItemsVisibility(menu, false);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                setItemsVisibility(menu, true);
            }
        });

        return true;
    }

    private void setItemsVisibility(Menu menu, boolean visibility) {
        if( !mUserJobPosition.equals(Constants.JOB_POSITIONS[0]))
            menu.setGroupVisible(R.id.main_admin_group,visibility);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.search) {
            return true;
        }

        if (id == R.id.main_admin_requests) {
            intent = new Intent(getApplicationContext(), RequestActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.main_admin_reasons) {
            intent = new Intent(getApplicationContext(), CreateReasonActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
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

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_settings) {
            // Do settings here
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_archived) {
            intent = new Intent(getApplicationContext(), ArchiveActivity.class);
            startActivity(intent);
        } else {
            String status = "";
            intent = new Intent(getApplicationContext(), StatusActivity.class);

            if (id == R.id.nav_admin_accepted) {
                status = Constants.ACCEPT_STATUS;
            } else if (id == R.id.nav_admin_rejected) {
                status = Constants.REJECT_STATUS;
            } else if (id == R.id.nav_admin_canceled) {
                status = Constants.CANCEL_ACCEPTED_STATUS;
            } else if (id == R.id.nav_admin_postponed) {
                status = Constants.DEADLINE_ACCEPTED_STATUS;
            } else if (id == R.id.nav_admin_finished) {
                status = Constants.FINISH_STATUS;
            } else if (id == R.id.nav_admin_done) {
                status = Constants.DONE_STATUS;
            }

            intent.putExtra("TYPE", Constants.INTENT_ADMIN_TYPE); // Demands viewed as Admin
            intent.putExtra("STATUS", status);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
            String response = CommonUtils.POST("/user/logout", values);
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
        MyDBManager myDBManager = new MyDBManager(this);
        myDBManager.deleteAllTables();
        mPrefs.edit().clear().apply();
    }
}
