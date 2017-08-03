package com.example.caiqu.demand.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.caiqu.demand.Adapters.UserAdapter;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import java.util.List;

public class RequestActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private SharedPreferences mPrefs;
    private int mUserId;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<User> mUsers;
    private UserAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mUserId = mPrefs.getInt(Constants.LOGGED_USER_ID,-1);

        mRecyclerView = (RecyclerView) findViewById(R.id.demand_recycler);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        loadRequest(mUserId);

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra(Constants.INTENT_USER);
        if (user != null) {
            Log.e(TAG, "User not null:" + user.toString());
            int position = CommonUtils.getIndexByUserId(mUsers,user.getId());
            mAdapter.notifyItemInserted(position);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.BROADCAST_REQUEST_ACT));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void loadRequest(int userId) {
        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR + " = ?";
        String[] args = {
                "" + userId
        };

        MyDBManager myDBManager = new MyDBManager(this);
        mUsers = myDBManager.searchUsers(selection,args);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new UserAdapter(mUsers,this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User user = (User) intent.getSerializableExtra(Constants.INTENT_USER);
            int position = CommonUtils.getIndexByUserId(mUsers,user.getId());

            if(position > -1) {
                mUsers.remove(position);
                mUsers.add(position,user);
                mAdapter.notifyItemChanged(position);
            } else {
                mUsers.add(0,user);
                mAdapter.notifyDataSetChanged();
            }

        }
    };
}
