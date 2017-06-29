package com.example.caiqu.demand.FCM;


import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.example.caiqu.demand.Adapters.DemandAdapter;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;
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
    private SeenTask mSeenTask;
    private StatusTask mStatusTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        Bundle bundle = params.getExtras();
        String tag = params.getTag();
        String type = bundle.getString(Constants.JOB_TYPE_KEY);
        int demandId = bundle.getInt(Constants.INTENT_DEMAND_SERVER_ID);
        Log.e(TAG, "Job Tag:" + tag + " Job Type:" + type + " Demand Id:" + demandId);

        // TODO: Send Task. Verify list of jobs!
        switch (type) {
            case Constants.MARK_AS_READ_JOB_TAG:
                return !attemptToMarkAsSeen(demandId);
            case Constants.UPDATE_JOB_TAG:
                String status = bundle.getString(Constants.INTENT_DEMAND_STATUS);
                return !attemptToUpdateStatus(demandId, status);
        }

        return false; // Answers the question: "Is there still work going on?"

    }

    private boolean attemptToMarkAsSeen(int id){
        mSeenTask = new SeenTask(id);
        mSeenTask.execute();
        return true;
    }

    private boolean attemptToUpdateStatus(int id, String status){
        mStatusTask = new StatusTask(id, status);
        mStatusTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false; // Answers the question: "Should this job be retried?"
    }

    public class SeenTask extends AsyncTask<Void, Void, String> {
        private int demandId;

        public SeenTask(int demandId) {
            this.demandId = demandId;
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", demandId);
            return CommonUtils.POST("/demand/mark-as-read/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mSeenTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e(TAG, "Mark as seen json response: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) Log.d(TAG, "Job mark as seen successful");
        }
    }

    public class StatusTask extends AsyncTask<Void, Void, String> {
        private int id;
        private String status;

        public StatusTask(int id, String status) {
            this.id = id;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", id);
            values.put("status", status);
            return CommonUtils.POST("/demand/set-status/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e(TAG, jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Log.e(TAG, getString(R.string.server_error));
                e.printStackTrace();
            }

            if (success) {
                // No need to broadcast change, since local changes are made before server.
            } else {
                Log.e(TAG, getString(R.string.server_error));
            }
        }
    }

}
