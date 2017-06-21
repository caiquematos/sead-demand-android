package com.example.caiqu.demand.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
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
    private PagerAdapter mPagerAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

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
            attemptLogout();
            return true;
        }

        /*
        if (id == R.id.main_accepted){
            intent.putExtra("TYPE", "U"); // Demands I accepted as User
            intent.putExtra("STATUS", "A");
            startActivity(intent);
        }
        */
        String status = "";

        switch(id){
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
    }

    @Override
    public void onClick(View v) {

    }
}
