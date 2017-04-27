package com.example.caiqu.demand.Fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
 * Created by caiqu on 09/03/2017.
 */

public class ReceivedFragment extends Fragment{
    public static final String ARG_PAGE = "RECEIVED_DEMAND";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Demand> mDemandSet;
    private GetDemandTask mGetDemandTask;
    private SharedPreferences mPrefs;
    private SwipeRefreshLayout mSwipeRefresh;
    private String mUserEmail;
    private int mPage;

    public static ReceivedFragment newInstance(int page){
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ReceivedFragment fragment = new ReceivedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_demand, container, false);

        mPrefs = getActivity().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
        mUserEmail = mPrefs.getString(Constants.USER_EMAIL,"");

        mRecyclerView = (RecyclerView) view.findViewById(R.id.all_demand_recycler);
        mLayoutManager = new LinearLayoutManager(getContext());

        mSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.demand_swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mGetDemandTask == null){
                    mGetDemandTask = new GetDemandTask(mUserEmail, getView());
                    mGetDemandTask.execute();
                }
            }
        });

        mGetDemandTask = new GetDemandTask(mUserEmail, view);
        mGetDemandTask.execute();

        return view;
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
            return CommonUtils.POST("/demand/list-received/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mGetDemandTask = null;

            JSONObject jsonObject;
            JSONArray jsonArray = null;
            boolean success = false;

            Log.d("ON RECEI TAB POST EXEC", "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonArray = jsonObject.getJSONArray("list");
            } catch (JSONException e) {
                Snackbar.make(view, "Server Problem", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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

                mAdapter = new DemandAdapter(mDemandSet,getActivity(), mPage);
                mRecyclerView.setAdapter(mAdapter);

                mSwipeRefresh.setRefreshing(false);
            } else {
                mSwipeRefresh.setRefreshing(false);
                Snackbar.make(view, "Problema no servidor. Tente mais tarde.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

        }
    }
}
