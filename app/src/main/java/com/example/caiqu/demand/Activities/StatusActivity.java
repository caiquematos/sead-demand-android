package com.example.caiqu.demand.Activities;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

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
    private int mUserId;

    public StatusActivity() {
        this.mActivity = this;
        this.mPage = Constants.STATUS_PAGE;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.BROADCAST_STATUS_ACT));
        if (mUserId != -1 && !mStatus.isEmpty()) loadStatusList(mUserId, mStatus);
        else Log.e(TAG, "Logged User id  or status not found!");
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mType = intent.getStringExtra("TYPE"); // U - user, A - Admin
        mStatus = intent.getStringExtra("STATUS"); // A - Accepted, C - Cancelled, X - Rejected, P - Postponed, D - Done.
        Log.d(TAG, "Status: " + mStatus + " Type: " + mType);

        // Handle Screen Title.
        String title = "Demandas";
        switch (mType) {
            case Constants.INTENT_USER_TYPE:
                switch (mStatus) {
                    case Constants.DONE_STATUS:
                        title = "Demandas Cocluídas";
                        break;
                }
                break;
            case Constants.INTENT_ADMIN_TYPE:
                switch (mStatus) {
                    case Constants.ACCEPT_STATUS:
                        title = "Demandas Aceitas";
                        break;
                    case Constants.REJECT_STATUS:
                        title = "Demandas Rejeitadas";
                        break;
                    case Constants.CANCEL_STATUS:
                        title = "Demandas Canceladas";
                        break;
                    case Constants.POSTPONE_STATUS:
                        title = "Demandas Adiadas";
                        break;
                    case Constants.DONE_STATUS:
                        title = "Demandas Cocluídas";
                        break;
                }
        }
        setTitle(title);

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mUserEmail = mPrefs.getString(Constants.LOGGED_USER_EMAIL,"");
        mUserId = mPrefs.getInt(Constants.LOGGED_USER_ID,-1);

        mRecyclerView = (RecyclerView) findViewById(R.id.accepted_recycler);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        if (mUserId != -1 && !mStatus.isEmpty()) loadStatusList(mUserId, mStatus);
        else Log.e(TAG, "Logged User id or status not found!");

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.accepted_swipe);
        mSwipeRefresh.setEnabled(false);

        /*
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

        */
    }

    private void loadStatusList(int adminId, String status){
        String selection = "(( " + FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ? AND "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " = ? ) OR ( "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " != ? AND "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " != ?)) AND ( "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? )";

        String[] args = {
                "" + adminId,
                "" + adminId,
                "" + adminId,
                "" + adminId,
                status
        };

        MyDBManager myDBManager = new MyDBManager(mActivity);
        mDemandSet = myDBManager.searchDemands(selection,args);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DemandAdapter(mDemandSet,mActivity, mPage);
        mRecyclerView.setAdapter(mAdapter);
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
                case Constants.INTENT_USER_TYPE:
                    return CommonUtils.POST("/demand/list-demand-by-status/", values);
                case Constants.INTENT_ADMIN_TYPE:
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

            Log.d(TAG, "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonArray = jsonObject.getJSONArray("list");
            } catch (JSONException e) {
                Snackbar.make(mSwipeRefresh, R.string.server_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                e.printStackTrace();
            }

            if (success) {
                mDemandSet =  new ArrayList<>();
                for(int i=0; i < jsonArray.length(); i++){
                    try {
                        JSONObject json = jsonArray.getJSONObject(i);
                        mDemandSet.add(Demand.build(json));
                        Log.d(TAG, json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new DemandAdapter(mDemandSet,mActivity,mPage);
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefresh.setRefreshing(false);

            } else {
                Snackbar.make(mSwipeRefresh, R.string.server_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Demand demand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
            String storageType = intent.getExtras().getString(Constants.INTENT_STORAGE_TYPE);
            int position = CommonUtils.getIndexByDemandId(mDemandSet,demand.getId());

            Log.e(TAG, "on broadcast receiver. Type:" +storageType+ " position: " + position+ " demand:" + demand.toString());

            switch (storageType) {
                case Constants.INSERT_DEMAND_RECEIVED:
                    mDemandSet.add(0, demand);
                    break;
                case Constants.UPDATE_DEMAND:
                    if(position >= 0) mDemandSet.remove(position);
                    mDemandSet.add(0,demand);
                    break;
                case Constants.UPDATE_IMPORTANCE:
                    if (position >= 0) {
                        mDemandSet.remove(position);
                        mDemandSet.add(0,demand);
                        //mDemandSet.get(position).setImportance(demand.getImportance());
                    }
                    break;
                case Constants.UPDATE_READ:
                    if (position >= 0) {
                        mDemandSet.remove(position);
                        mDemandSet.add(0,demand);
                        //mDemandSet.get(position).setSeen(demand.getSeen());
                    }
                    break;
                case Constants.UPDATE_STATUS:
                    if (position >= 0 && !demand.getStatus().equals(mStatus)) {
                        mDemandSet.remove(position);
                    }
                    break;
            }

            mAdapter.notifyDataSetChanged();
        }
    };
}
