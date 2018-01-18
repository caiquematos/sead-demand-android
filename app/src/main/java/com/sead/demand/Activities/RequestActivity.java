package com.sead.demand.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.sead.demand.Adapters.UserAdapter;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private SharedPreferences mPrefs;
    private int mUserId;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<User> mUsers;
    private UserAdapter mAdapter;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mCurrentUser = CommonUtils.getCurrentUserPreference(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.demand_recycler);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        List<Authority> authorities = fetchAuthoritiesBySuperior(mCurrentUser.getId());
        loadRequest(mCurrentUser.getId(), authorities);

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

    // This is necessary, because even though current user is not a direct superior of certain
    // receivers, they can be the receiver's superior's superior.
    // So authority has a list of each user which this current user has authority over.
    private List<Authority> fetchAuthoritiesBySuperior(int superiorId) {
        List<Authority> authorities;

        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_SUPERIOR + " = ?";

        String[] args = {
                "" + superiorId,
        };

        MyDBManager myDBManager = new MyDBManager(this);
        authorities = myDBManager.searchAuthorities(selection,args);

        return authorities;
    }

    private void loadRequest(int userId, List<Authority> authorities) {
        ArrayList<String> argsArray = new ArrayList<>();

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR + " = ?";
        argsArray.add("" + userId);

        if (authorities != null) {
            Log.d(TAG, "Authorities different from null");
            for(int i = 0; i < authorities.size(); i++){
                selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?");
                argsArray.add("" + authorities.get(i).getUser());
            }
        }

        String[] args = new String[argsArray.size()];
        for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
        Log.e(TAG, "args fetch user:" + argsArray.toString());

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
