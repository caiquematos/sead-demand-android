package com.example.caiqu.demand.Fragments;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * Created by caiqu on 04/05/2017.
 */

public class SuperiorFragment extends Fragment {
    public String TAG = getClass().getSimpleName();
    public static final String ARG_PAGE = "SuperiorFragment";
    int mPage;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Demand> mDemandSet;
    private GetDemandTask mGetDemandTask;
    private SharedPreferences mPrefs;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mUserEmail;
    private int mUserId;

    public static SuperiorFragment newInstance( int page ) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SuperiorFragment fragment = new SuperiorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.BROADCAST_ADMIN_FRAG));
        if (mUserId != -1) loadAdminList(mUserId);
        else Log.e(TAG, "Logged User id not found!");
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_demand, container, false);
        mPrefs = getActivity().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
        mUserEmail = mPrefs.getString(Constants.LOGGED_USER_EMAIL,"");
        mUserId = mPrefs.getInt(Constants.LOGGED_USER_ID,-1);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.all_demand_recycler);
        mLayoutManager = new LinearLayoutManager(getContext());

        if (mUserId != -1) loadAdminList(mUserId);
        else Log.e(TAG, "Logged User id not found!");

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.demand_swiperefresh);
        mSwipeRefresh.setEnabled(false);

        /*
        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.demand_swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mGetDemandTask == null && CommonUtils.isOnline(getContext())){
                    mGetDemandTask = new GetDemandTask(mUserEmail, getView());
                    mGetDemandTask.execute();
                } else {
                    mSwipeRefresh.setRefreshing(false);
                    Snackbar.make(mSwipeRefresh, R.string.internet_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        if (CommonUtils.isOnline(getContext())) {
            mGetDemandTask = new GetDemandTask(mUserEmail, view);
            mGetDemandTask.execute();
        } else {
            Snackbar.make(view, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        */

        return view;
    }

    private void loadAdminList(int adminId){
        String selection = "(( " + FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ? AND "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " = ? ) OR ( "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " != ? AND "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " != ?)) AND ( "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? )";

        String[] args = {
                "" + adminId,
                "" + adminId,
                "" + adminId,
                "" + adminId,
                Constants.REOPEN_STATUS,
                Constants.UNDEFINE_STATUS,
                Constants.RESEND_STATUS
        };

        MyDBManager myDBManager = new MyDBManager(getContext());
        mDemandSet = myDBManager.searchDemands(selection,args);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DemandAdapter(mDemandSet,getActivity(), mPage);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class GetDemandTask extends AsyncTask<Void, Void, String> {
        private final String userEmail;
        private final View view;

        public GetDemandTask(String userEmail, View view) {
            this.userEmail = userEmail;
            this.view = view;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefresh.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("email", userEmail);
            return CommonUtils.POST("/demand/list-admin-received/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mGetDemandTask = null;

            JSONObject jsonObject;
            JSONArray jsonArray = null;
            boolean success = false;

            Log.d("ON SENT TAB POST EXEC", "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonArray = jsonObject.getJSONArray("list");
            } catch (JSONException e) {
                Snackbar.make(view, R.string.server_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                e.printStackTrace();
            }

            if (success) {
                mDemandSet =  new ArrayList<>();
                for(int i=0; i < jsonArray.length(); i++){
                    try {
                        JSONObject json = jsonArray.getJSONObject(i);
                        mDemandSet.add(Demand.build(json));
                        Log.d("ON DEMAND", "" + json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mRecyclerView.setLayoutManager(mLayoutManager);

                mAdapter = new DemandAdapter(mDemandSet,getActivity(), mPage);
                mRecyclerView.setAdapter(mAdapter);

                mSwipeRefresh.setRefreshing(false);
            } else {
                mSwipeRefresh.setRefreshing(false);
                Snackbar.make(view, R.string.server_error, Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
                case Constants.INSERT_DEMAND_ADMIN:
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
                        mDemandSet.get(position).setSeen(demand.getSeen());
                    }
                    break;
                case Constants.UPDATE_STATUS:
                    if (position >= 0) {
                        mDemandSet.remove(position);
                        mDemandSet.add(0,demand);
                        //mDemandSet.get(position).setStatus(demand.getStatus());
                    }
                    break;
            }

            mAdapter.notifyDataSetChanged();
        }
    };
}
