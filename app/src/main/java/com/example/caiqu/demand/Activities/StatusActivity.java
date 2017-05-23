package com.example.caiqu.demand.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.caiqu.demand.Adapters.DemandAdapter;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Fragments.SentFragment;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private GetAcceptedTask mGetAcceptedTask;
    private String mUserEmail;
    private SharedPreferences mPrefs;
    private List<Demand> mDemandSet;
    private StatusActivity mActivity;
    private String mStatus;
    private String mType;
    private int mPage;

    public StatusActivity() {
        this.mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPage = -1;

        Intent intent = getIntent();
        mType = intent.getStringExtra("TYPE"); // U - user, A - Admin
        mStatus = intent.getStringExtra("STATUS"); // A - Accepted, C - Cancelled, X - Rejected
        Log.d("On Accepted", "Status: " + mStatus + " Type: " + mType);
        switch (mType) {
            case "U":
                switch (mStatus) {
                    case "A":
                        mPage = 5;
                        setTitle("Demandas Aceitas");
                        break;
                    case "X":
                        mPage = 6;
                        setTitle("Demandas Rejeitadas");
                        break;
                    case "C":
                        mPage = 4; // This makes menu REOPEN visible
                        setTitle("Demandas Canceladas");
                }
                break;
            case "A":
                switch (mStatus) {
                    case "A":
                        mPage = 5;
                        setTitle("(Admin) Demandas Aceitas");
                        break;
                    case "X":
                        mPage = 6;
                        setTitle("Demandas Rejeitadas");
                        break;
                    case "C":
                        mPage = 4; // This makes menu REOPEN visible
                        setTitle("(Admin) Demandas Canceladas");
                }

        }

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mUserEmail = mPrefs.getString(Constants.USER_EMAIL,"");

        mRecyclerView = (RecyclerView) findViewById(R.id.accepted_recycler);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.accepted_swipe);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mGetAcceptedTask == null && CommonUtils.isOnline(mActivity)){
                    mGetAcceptedTask = new GetAcceptedTask(mUserEmail, mStatus);
                    mGetAcceptedTask.execute();
                } else {
                    mSwipeRefresh.setRefreshing(false);
                    Snackbar.make(mSwipeRefresh, R.string.internet_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        if(mGetAcceptedTask == null && CommonUtils.isOnline(mActivity)){
            mGetAcceptedTask = new GetAcceptedTask(mUserEmail, mStatus);
            mGetAcceptedTask.execute();
        } else {
            Snackbar.make(mSwipeRefresh, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private class GetAcceptedTask extends AsyncTask<Void, Void, String>{
        private String userEmail;
        private String status;

        public GetAcceptedTask(String userEmail, String status) {
            this.userEmail = userEmail;
            this.status = status;
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("email", userEmail);
            values.put("status", status);
            switch (mType) {
                case "U":
                    return CommonUtils.POST("/demand/list-demand-by-status/", values);
                case "A":
                    return CommonUtils.POST("/demand/list-admin-demand-by-status/", values);
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mGetAcceptedTask = null;

            JSONObject jsonObject;
            JSONArray jsonArray = null;
            boolean success = false;

            Log.d("ON ACCEPT EXEC", "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonArray = jsonObject.getJSONArray("list");
            } catch (JSONException e) {
                Snackbar.make(mSwipeRefresh, "Server Problem", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                e.printStackTrace();
            }

            if (success) {
                mDemandSet =  new ArrayList<>();
                for(int i=0; i < jsonArray.length(); i++){
                    try {
                        JSONObject json = jsonArray.getJSONObject(i);
                        mDemandSet.add(new Demand(json));
                        Log.d("ON DEMAND", "" + json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new DemandAdapter(mDemandSet,mActivity,mPage);
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefresh.setRefreshing(false);

            } else {
                Snackbar.make(mSwipeRefresh, "Problema no servidor. Tente mais tarde.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }
}
