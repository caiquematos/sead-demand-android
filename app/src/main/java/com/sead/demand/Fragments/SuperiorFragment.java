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
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caiqu on 04/05/2017.
 */

public class SuperiorFragment extends DemandFragment {
    public static final String ARG_PAGE = "SuperiorFragment";
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences mPrefs;
    private SwipeRefreshLayout mSwipeRefresh;
    private User mCurrentUser;
    private int mCurrentUserId;

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
        if (mCurrentUserId != -1){
            List<Authority> authorities = fetchAuthoritiesBySuperior(mCurrentUserId);
            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUserId, authorities);
            if (usersUnderMySupervision != null)
                if (usersUnderMySupervision.size() > 0)
                    loadAdminList(usersUnderMySupervision);
        }
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

        // Fetch info about current user logged
        try {
            JSONObject userJson = new JSONObject(mPrefs.getString(Constants.USER_PREFERENCES, ""));
            mCurrentUser = User.build(userJson);
            mCurrentUserId = mCurrentUser.getId();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get user from preferences!!!");
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.all_demand_recycler);
        mLayoutManager = new LinearLayoutManager(getContext());

        if (mCurrentUserId != -1){
            List<Authority> authorities = fetchAuthoritiesBySuperior(mCurrentUserId);
            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUserId, authorities);
            if (usersUnderMySupervision != null)
                if (usersUnderMySupervision.size() > 0)
                    loadAdminList(usersUnderMySupervision);
        } else Log.e(TAG, "Logged User id not found!");

        implementRecyclerViewClickListener();

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.demand_swiperefresh);
        mSwipeRefresh.setEnabled(false);

        return view;
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

        MyDBManager myDBManager = new MyDBManager(getContext());
        authorities = myDBManager.searchAuthorities(selection,args);

        return authorities;
    }

    private List<User> fetchUsersUnderMySupervision(int superiorId, List<Authority> authorities) {
        List<User> users;
        ArrayList<String> argsArray = new ArrayList<>();

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR + " = ?";
        argsArray.add("" + superiorId);

        if (authorities != null) {
            Log.d(TAG, "Authorities different from null");
            for(int i = 0; i < authorities.size(); i++){
                selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?");
                argsArray.add("" + authorities.get(i).getUser());
                Log.d("fetchUsersUnderMySup", "auth upon: " + authorities.get(i).getUser());
            }
        }

        String[] args = new String[argsArray.size()];
        for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
        Log.e(TAG, "args fetch user:" + argsArray.toString());

        MyDBManager myDBManager = new MyDBManager(getContext());
        users = myDBManager.searchUsers(selection,args);

        for (User user : users ) {
            Log.d("fetchUsersUnderMySup", "user:" + user.getId());
        }

        CommonUtils.listAllUsersDB(getContext());

        return users;
    }

    private void loadAdminList(List<User> usersUnderMySupervision){
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
        selection = selection.concat( FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ? OR "
                        + FeedReaderContract.DemandEntry.COLUMN_NAME_STATUS + " = ?" );
        selection = selection.concat(")");

        argsArray.add(Constants.REOPEN_STATUS);
        argsArray.add(Constants.UNDEFINE_STATUS);
        argsArray.add(Constants.LATE_STATUS);
        argsArray.add(Constants.TRANSFER_STATUS);
        argsArray.add(Constants.DEADLINE_REQUESTED_STATUS);
        argsArray.add(Constants.CANCEL_REQUESTED_STATUS);
        argsArray.add(Constants.UNFINISH_STATUS);
        argsArray.add(Constants.RESEND_STATUS);

        selection = selection.concat(" AND ");
        selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_ARCHIVE + " = ?");

        argsArray.add("" + 0);

        String[] args = new String[argsArray.size()];
        for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);

        MyDBManager myDBManager = new MyDBManager(getContext());
        mDemandSet = myDBManager.searchDemands(selection,args);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DemandAdapter(mDemandSet,getActivity(), mPage);
        mRecyclerView.setAdapter(mAdapter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Demand demand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
            String storageType = intent.getExtras().getString(Constants.INTENT_STORAGE_TYPE);

            // This list can be null, in case user is a 'ponta' type, superior tab wont exist.
            if (mDemandSet != null) {
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
                    case Constants.UPDATE_PRIOR:
                        if (position >= 0) {
                            mDemandSet.remove(position);
                            mDemandSet.add(0,demand);
                            //mDemandSet.get(position).setPrior(demand.getPrior());
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
                        } else {
                            List<Authority> authorities = fetchAuthoritiesBySuperior(mCurrentUserId);
                            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUserId, authorities);
                            if (usersUnderMySupervision != null)
                                if (usersUnderMySupervision.size() > 0)
                                    loadAdminList(usersUnderMySupervision);
                            // TODO: Maybe check status value instead in order to make less work.
                        }
                        break;
                }

                mAdapter.notifyDataSetChanged();
            }

        }
    };
}
