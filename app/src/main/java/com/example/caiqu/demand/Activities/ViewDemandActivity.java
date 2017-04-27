package com.example.caiqu.demand.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewDemandActivity extends AppCompatActivity {
    private String mDemandId;
    private String mSeen;
    private TextView mSubjectTV;
    private TextView mImportanceTV;
    private TextView mStatusTV;
    private TextView mSenderTV;
    private TextView mReceiverTV;
    private TextView mTimeTV;
    private TextView mDescriptionTV;
    private View mScrollView;
    private StatusTask mStatusTask;
    private SeenTask mSeenTask;
    private ProgressDialog mPDDemand;
    private FloatingActionButton mFabYes;
    private FloatingActionButton mFabLater;
    private FloatingActionButton mFabNo;
    private FloatingActionButton mFabMenu;
    private TextView mYesTV;
    private TextView mNoTV;
    private TextView mLaterTV;
    private String mDemandStatus;
    private int mPage; //identifies which tab called this
    private boolean mTurned;
    ViewDemandActivity mActivity;

    public ViewDemandActivity() {
        mActivity = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubjectTV = (TextView) findViewById(R.id.view_demand_subject);
        mImportanceTV = (TextView) findViewById(R.id.view_demand_importance);
        mStatusTV = (TextView) findViewById(R.id.view_demand_status);
        mSenderTV = (TextView) findViewById(R.id.view_demand_sender);
        mReceiverTV = (TextView) findViewById(R.id.view_demand_receiver);
        mTimeTV = (TextView) findViewById(R.id.view_demand_time);
        mDescriptionTV = (TextView) findViewById(R.id.view_demand_description);
        mPDDemand = new ProgressDialog(mActivity);

        Intent intent = getIntent();
        String activity = intent.getStringExtra("ACTIVITY");
        String importance = intent.getStringExtra("IMPORTANCE");
        setImportanceColor(importance);
        mPage = intent.getIntExtra("PAGE", 0);
        mSeen = intent.getStringExtra("SEEN");
        if (mSeen.equals("N") && mPage == 1) markAsSeen();

        mDemandId = intent.getStringExtra("DEMAND");
        mSubjectTV.setText(intent.getStringExtra("SUBJECT").toUpperCase());
        mImportanceTV.setText(importance);
        mSenderTV.setText("De: " + intent.getStringExtra("SENDER"));
        mReceiverTV.setText("Para: " + intent.getStringExtra("RECEIVER"));
        mTimeTV.setText(intent.getStringExtra("TIME"));
        mDescriptionTV.setText(intent.getStringExtra("DESCRIPTION"));

        mDemandStatus = intent.getStringExtra("STATUS");
        showDemandStatus(mDemandStatus);

        mYesTV = (TextView) findViewById(R.id.tv_yes);
        mFabYes = (FloatingActionButton) findViewById(R.id.fab_yes);
        mFabYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus("A");
            }
        }); //Accepted

        mLaterTV = (TextView) findViewById(R.id.tv_later);
        mFabLater = (FloatingActionButton) findViewById(R.id.fab_later);
        mFabLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus("P");
            }
        }); //Postponed

        mNoTV = (TextView) findViewById(R.id.tv_no);
        mFabNo = (FloatingActionButton) findViewById(R.id.fab_no);
        mFabNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus("C");
            }
        }); //Cancelled

        mFabMenu = (FloatingActionButton) findViewById(R.id.fab_menu);
        mFabMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTurned) {
                    ViewCompat.animate(mFabMenu).
                            rotation(0f).
                            withLayer().
                            setDuration(1000).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                    mTurned = false;
                    mFabNo.hide();
                    mFabLater.hide();
                    mFabYes.hide();
                    mNoTV.setVisibility(View.INVISIBLE);
                    mLaterTV.setVisibility(View.INVISIBLE);
                    mYesTV.setVisibility(View.INVISIBLE);
                } else {
                    ViewCompat.animate(mFabMenu).
                            rotation(135f).
                            withLayer().
                            setDuration(1000).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                    mTurned = true;
                    mFabYes.show();
                    mFabLater.show();
                    mFabNo.show();
                    mNoTV.setVisibility(View.VISIBLE);
                    mLaterTV.setVisibility(View.VISIBLE);
                    mYesTV.setVisibility(View.VISIBLE);
                }
            }
        });

        Log.e("On ViewDemand", "Activity out if: " + activity);
        if (activity.equals("StatusActivity") || mPage == 2){
            Log.e("On ViewDemand", "Activity in if: " + activity);
            mFabMenu.hide();
            mFabNo.hide();
            mFabLater.hide();
            mFabYes.hide();
        }else{
            mScrollView = findViewById(R.id.view_demand_scroll_view);
            mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(scrollY > 0){
                        if(mTurned){
                            ViewCompat.animate(mFabMenu).
                                    rotation(0f).
                                    withLayer().
                                    setDuration(800).
                                    setInterpolator(new OvershootInterpolator()).
                                    setListener(new ViewPropertyAnimatorListener() {
                                        @Override
                                        public void onAnimationStart(View view) {
                                            mFabNo.hide();
                                            mFabLater.hide();
                                            mFabYes.hide();
                                            mNoTV.setVisibility(View.INVISIBLE);
                                            mLaterTV.setVisibility(View.INVISIBLE);
                                            mYesTV.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onAnimationEnd(View view) {
                                            mTurned = false;
                                            mFabMenu.hide();
                                        }

                                        @Override
                                        public void onAnimationCancel(View view) {
                                            mTurned = true;
                                        }
                                    }).
                                    start();
                        }else {
                            mFabMenu.hide();
                        }
                    } else{
                        mFabMenu.show();
                    }
                }
            });
        }
    }

    private void showDemandStatus(String status) {
        if (status.equals("A"))
            mStatusTV.setText("Essa demanda foi Aceita");
        else if (status.equals("C"))
            mStatusTV.setText("Essa demanda foi Cancelada");
        else if (status.equals("P"))
            mStatusTV.setText("Essa demanda foi Adiada");
        else if (status.equals("R"))
            mStatusTV.setText("Essa demanda foi Reaberta");
        else
            mStatusTV.setText("Essa demanda ainda não foi avaliada");
    }

    private void markAsSeen(){
        if (mSeenTask == null){
            mSeenTask = new SeenTask();
            mSeenTask.execute();
        }
    }

    private void setDemandStatus(String status){
        if (mStatusTask == null){
            mStatusTask = new StatusTask(mDemandId, status);
            mStatusTask.execute();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setImportanceColor(String importance) {
        int color = ContextCompat.getColor(this,R.color.dGreen);
        if (importance.equals("Urgente")) color = ContextCompat.getColor(this,R.color.darkred);
        if (importance.equals("Importante")) color = ContextCompat.getColor(this,R.color.dyellow);
        mImportanceTV.getCompoundDrawables()[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public class SeenTask extends  AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", mDemandId);
            return CommonUtils.POST("/demand/mark-as-read/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mSeenTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e("ON SEEN DEMAND", jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) mSeen = "Y"; //TODO: Make this persistent for a while
        }
    }

    public class StatusTask extends AsyncTask<Void, Void, String> {
        private String id;
        private String status;

        public StatusTask(String id, String status) {
            this.id = id;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Por favor aguarde");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", id);
            values.put("status", status);
            return CommonUtils.POST("/demand/set-status/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e("ON POST EX VIEW DEMAND", jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Snackbar.make(mFabYes, "Server Problem", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (success) {
                Snackbar.make(mFabYes, "SUCCESS", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showDemandStatus(status);
            } else {
                Snackbar.make(mFabYes, "FAILED", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
        }
    }
}
