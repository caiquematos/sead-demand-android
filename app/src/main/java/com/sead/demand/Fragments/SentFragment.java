package com.sead.demand.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sead.demand.Adapters.DemandAdapter;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Demand;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

/**
 * Created by caiqu on 09/03/2017.
 */

public class SentFragment extends DemandFragment {
    public static final String ARG_PAGE = "SENT_DEMAND";
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences mPrefs;
    private SwipeRefreshLayout mSwipeRefresh;
    private int mUserId;

    public static SentFragment newInstance(int page){
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SentFragment fragment = new SentFragment();
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
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Constants.BROADCAST_SENT_FRAG));
        if (mUserId != -1) loadSenderList(mUserId);
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
        mUserId = mPrefs.getInt(Constants.LOGGED_USER_ID,-1);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.all_demand_recycler);
        mLayoutManager = new LinearLayoutManager(getContext());

        if (mUserId != -1) loadSenderList(mUserId);
        else Log.e(TAG, "Logged User id not found!");

        implementRecyclerViewClickListener();

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.demand_swiperefresh);
        mSwipeRefresh.setEnabled(false);

        return view;
    }

    private void loadSenderList(int senderId){
        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ?";
        selection = selection.concat(" AND ");
        selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_ARCHIVE + " = ?");

        String[] args = {
                "" + senderId,
                "" + 0
        };

        MyDBManager myDBManager = new MyDBManager(getContext());
        mDemandSet = myDBManager.searchDemands(selection,args);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DemandAdapter(mDemandSet,getActivity(), mPage);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Demand demand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
            String storageType = intent.getExtras().getString(Constants.INTENT_STORAGE_TYPE);
            int position = CommonUtils.getIndexByDemandId(mDemandSet,demand.getId());

            Log.e(TAG, "on broadcast receiver. Type:" +storageType+ " position: " + position+ " demand:" + demand.toString());

            switch (storageType) {
                case Constants.INSERT_DEMAND_SENT:
                    if(position >= 0) mDemandSet.remove(position);
                    mDemandSet.add(0,demand);
                    break;
                case Constants.UPDATE_DEMAND:
                    if(position >= 0) mDemandSet.remove(position);
                    mDemandSet.add(0,demand);
                    break;
                case Constants.UPDATE_PRIOR:
                    if (position >= 0) {
                        mDemandSet.remove(position);
                        //mDemandSet.get(position).setPrior(demand.getPrior());
                    }
                    mDemandSet.add(0,demand);
                    break;
                case Constants.UPDATE_READ:
                    if (position >= 0) {
                        mDemandSet.get(position).setSeen(demand.getSeen());
                    }
                    break;
                case Constants.UPDATE_STATUS:
                    if (position >= 0) {
                        mDemandSet.remove(position);
                        //mDemandSet.get(position).setStatus(demand.getStatus());
                    }
                    mDemandSet.add(0,demand);
                    break;
            }

            mAdapter.notifyDataSetChanged();
        }
    };
}
