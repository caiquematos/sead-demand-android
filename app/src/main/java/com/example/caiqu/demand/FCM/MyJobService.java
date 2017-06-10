package com.example.caiqu.demand.FCM;


import android.os.Bundle;
import android.util.Log;

import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by caique on 29/05/2017.
 */

public class MyJobService extends JobService {
    private String TAG = getClass().getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Bundle bundle = params.getExtras();
        try {
            JSONObject senderJson = new JSONObject(bundle.get("sender").toString());
            JSONObject receiverJson = new JSONObject(bundle.get("receiver").toString());
            JSONObject demandJson = new JSONObject(bundle.get("demand").toString());

            User sender = User.build(senderJson);
            User receiver =User.build(receiverJson);
            Demand demand = Demand.build(sender, receiver, demandJson);

            Log.e(TAG, "demand:" + demand.toString()
                    + " sender:" + sender.toString()
                    + " receiver:" + receiver.toString()
            );

            MyDBManager myDBManager = new MyDBManager(this);
            Log.e(TAG, "New row inserted:" + myDBManager.addDemand(demand));

            List<Demand> demands;

            String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ?";
            String[] args = {"1"};
            demands = myDBManager.searchDemands(selection,args);

            for (int index = 0; index < demands.size(); index++)
            Log.e(TAG, "Demanda " + index + ":" + demands.get(index).toString() + "\n");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
