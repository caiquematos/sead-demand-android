package com.example.caiqu.demand.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.caiqu.demand.Adapters.FixedTabsPageAdapter;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressDialog mPDLogout;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFab = (FloatingActionButton) findViewById(R.id.main_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(MainActivity.this, DemandActivity.class);
                startActivity(mainIntent);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.mainPager);
        PagerAdapter pagerAdapter= new FixedTabsPageAdapter(getSupportFragmentManager(),this);
        mViewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String LOGIN_STATUS = "";

        if (resultCode == RESULT_OK){
            if (resultCode == Constants.REQUEST_LOGIN){
                if (data.getBooleanExtra(Constants.IS_LOGGED, false)){
                    LOGIN_STATUS = "USER LOGGED";
                }else{
                    LOGIN_STATUS = "NOT LOGGED";
                }
            }
        }

        Snackbar.make(findViewById(R.id.main_activity_view),LOGIN_STATUS, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = new Intent(getApplicationContext(), StatusActivity.class);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mPDLogout = new ProgressDialog(this);
            mPDLogout.setMessage("Por favor aguarde");
            mPDLogout.setCancelable(false);
            mPDLogout.show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    prefs.edit().putBoolean(Constants.IS_LOGGED, false).apply();
                    if (mPDLogout.isShowing()){
                        mPDLogout.dismiss();
                    }
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
            return true;
        }

        /*
        if (id == R.id.main_accepted){
            intent.putExtra("TYPE", "U"); // Demands I accepted as User
            intent.putExtra("STATUS", "A");
            startActivity(intent);
        }
        if (id == R.id.main_cancelled){
            intent.putExtra("TYPE", "U"); // Demands I canceled as User
            intent.putExtra("STATUS", "C");
            startActivity(intent);
        }
        */

        if (id == R.id.main_admin_accepted){
            intent.putExtra("TYPE", "A"); // Demands I accepted as Admin
            intent.putExtra("STATUS", "A");
            startActivity(intent);
        }
        if (id == R.id.main_admin_rejected){
            intent.putExtra("TYPE", "A"); // Demands I rejected as Admin
            intent.putExtra("STATUS", "X");
            startActivity(intent);
        }
        if (id == R.id.main_admin_cancelled){
            intent.putExtra("TYPE", "A"); // Demands I canceled as Admin
            intent.putExtra("STATUS", "C");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
