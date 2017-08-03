package com.example.caiqu.demand.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.caiqu.demand.Adapters.DemandAdapter;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.Interfaces.RecyclerClickListener;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.RecycerSupport.RecyclerTouchListener;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import java.util.ArrayList;
import java.util.List;

public class StatusActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private DemandAdapter mAdapter;
    private List<Demand> mDemandSet;
    private StatusActivity mActivity;
    private String mStatus;
    private String mType;
    private int mPage;
    private User mCurrentUser;

    public StatusActivity() {
        this.mActivity = this;
        this.mPage = Constants.STATUS_PAGE;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.BROADCAST_STATUS_ACT));
        if (mCurrentUser.getId() != -1){
            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUser.getId());
            if (usersUnderMySupervision != null)
                if (usersUnderMySupervision.size() > 0)
                    loadAdminListByStatus(usersUnderMySupervision, mStatus);
        } else Log.e(TAG, "Logged User id not found!");
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mType = intent.getStringExtra("TYPE"); // U - user, A - Admin
        mStatus = intent.getStringExtra("STATUS"); // A - Accepted, C - Cancelled, X - Rejected, P - Postponed, D - Done.
        Log.d(TAG, "Status: " + mStatus + " Type: " + mType);

        // Setting activity title.
        String activityTitle;
        switch (mType) {
            case Constants.INTENT_USER_TYPE:
                switch (mStatus) {
                    case Constants.DONE_STATUS:
                        activityTitle = "Demandas Concluídas";
                        break;
                    default:
                        activityTitle = "Demandas";
                }
                break;
            case Constants.INTENT_ADMIN_TYPE:
                switch (mStatus) {
                    case Constants.ACCEPT_STATUS:
                        activityTitle = "Demandas Deferidas";
                        break;
                    case Constants.REJECT_STATUS:
                        activityTitle = "Demandas Indeferidas";
                        break;
                    case Constants.CANCEL_STATUS:
                        activityTitle = "Demandas Canceladas";
                        break;
                    case Constants.POSTPONE_STATUS:
                        activityTitle = "Demandas Adiadas";
                        break;
                    case Constants.DONE_STATUS:
                        activityTitle = "Demandas Concluídas";
                        break;
                    default:
                        activityTitle = "Demandas";
                }
                break;
            default:
                activityTitle = "Demandas";
        }
        setTitle(activityTitle);

        mCurrentUser = CommonUtils.getCurrentUserPreference(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.demand_recycler);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        if (mCurrentUser.getId() != -1){
            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUser.getId());
            if (usersUnderMySupervision != null)
                if (usersUnderMySupervision.size() > 0)
                    loadAdminListByStatus(usersUnderMySupervision, mStatus);
        } else Log.e(TAG, "Logged User id not found!");

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mActivity, mRecyclerView,
                new RecyclerClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                       mAdapter.showDemand(view,position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {}
                }));

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private List<User> fetchUsersUnderMySupervision(int superiorId) {
        List<User> users;

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR + " = ?";

        String[] args = {
                "" + superiorId,
        };

        MyDBManager myDBManager = new MyDBManager(this);
        users = myDBManager.searchUsers(selection,args);

        return users;
    }

    private void loadAdminListByStatus(List<User> usersUnderMySupervision, String status){

        String selection = "";
        ArrayList<String> argsArray = new ArrayList<>();

        selection = selection.concat("(");
        for(int i = 0; i < usersUnderMySupervision.size(); i++){
            if(i > 0)  selection = selection.concat(" OR ");
            selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " = ?");
            argsArray.add("" + usersUnderMySupervision.get(i).getId());
        }
        selection = selection.concat(")");
        selection = selection.concat( " AND ");
        selection = selection.concat("(");
        selection = selection.concat( FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ?");
        selection = selection.concat(")");

        Log.e(TAG, "selection:" + selection.toString());

        argsArray.add(status);

        String[] args = new String[argsArray.size()];
        for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
        Log.e(TAG, "args:" + argsArray.toString());

        MyDBManager myDBManager = new MyDBManager(this);
        mDemandSet = myDBManager.searchDemands(selection,args);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DemandAdapter(mDemandSet,this, mPage);
        mRecyclerView.setAdapter(mAdapter);
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
                case Constants.UPDATE_PRIOR:
                    if (position >= 0) {
                        mDemandSet.remove(position);
                        mDemandSet.add(0,demand);
                        //mDemandSet.get(position).setPrior(demand.getPrior());
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
