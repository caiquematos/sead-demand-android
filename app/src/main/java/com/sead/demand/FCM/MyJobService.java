package com.sead.demand.FCM;


import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.sead.demand.Entities.Demand;
import com.sead.demand.Handlers.AlarmReceiver;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by caique on 29/05/2017.
 */

public class MyJobService extends JobService {
    private String TAG = getClass().getSimpleName();
    private SeenTask mSeenTask;
    private StatusTask mStatusTask;
    private DueTimeTask mDueTimeTask;
    private String mType;
    private long mDemandId;
    private String mTag;

    @Override
    public boolean onStartJob(JobParameters params) {
        Bundle bundle = params.getExtras();
        mTag = params.getTag();
        //Demand demand = (Demand) bundle.getSerializable(Constants.INTENT_DEMAND);
        mType = bundle.getString(Constants.JOB_TYPE_KEY);
        mDemandId = bundle.getInt(Constants.INTENT_DEMAND_SERVER_ID);
        Log.e(TAG, "Job Tag:" + mTag + " Job Type:" + mType + " Demand Id:" + mDemandId);

        // TODO: Send Task. Verify list of jobs!
        switch (mType) {
            case Constants.MARK_AS_READ_JOB_TAG:
                return !attemptToMarkAsSeen(mDemandId);
            case Constants.UPDATE_JOB_TAG:
                String status = bundle.getString(Constants.INTENT_DEMAND_STATUS);
                return !attemptToUpdateStatus(mDemandId, status);
            case Constants.WARN_DUE_TIME_JOB_TAG:
                return !attemptToSendLateWarning(mDemandId);
            case Constants.DUE_TIME_JOB_TAG:
                return !attemptToMarkDemandAsLate(mDemandId);
        }

        return false; // Answers the question: "Is there still work going on?"
    }

    private boolean attemptToMarkDemandAsLate(long demandId) {
            if (mDueTimeTask == null){
                mDueTimeTask = new DueTimeTask(demandId);
                mDueTimeTask.execute("mark-as-late");
                return true;
            } else return false;
    }

    private boolean attemptToSendLateWarning(long demandId) {
        if (mDueTimeTask == null){
            mDueTimeTask = new DueTimeTask(demandId);
            mDueTimeTask.execute("late-warning");
            return true;
        } else return false;
    }

    private boolean attemptToMarkAsSeen(long id){
        if (mSeenTask == null) {
            mSeenTask = new SeenTask(id);
            mSeenTask.execute();
            return true;
        } else return false;
    }

    private boolean attemptToUpdateStatus(long id, String status){
        if (mStatusTask == null) {
            mStatusTask = new StatusTask(id, status);
            mStatusTask.execute();
            return true;
        } else return false;

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "(onStop) job somehow canceled!!!");
        if (mType.equals(Constants.DUE_TIME_JOB_TAG)) {
            return true;
        }
        return false; // Answers the question: "Should this job be retried?"
    }

    public class SeenTask extends AsyncTask<Void, Void, String> {
        private long demandId;

        public SeenTask(long demandId) {
            this.demandId = demandId;
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", demandId);
            return CommonUtils.POST("/demand/mark-as-read", values);
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
        private long id;
        private String status;

        public StatusTask(long id, String status) {
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
            return CommonUtils.POST("/demand/set-status", values);
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

    public class DueTimeTask extends AsyncTask<String, Void, String> {
        private long demandId;

        public DueTimeTask(long demandId) {
            this.demandId = demandId;
        }

        @Override
        protected String doInBackground(String... strings) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            String url = "";
            switch (strings[0]) {
                case "late-warning":
                    url = "late-warning";
                    break;
                case "mark-as-late":
                    url = "late";
                    break;
                default:
            }
            return CommonUtils.POST("/send/" + url, values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDueTimeTask = null;
            Log.d(TAG, "(DueTimeTask) response: " + s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    int type = jsonObject.getInt("type");
                    switch (type) {
                        case Constants.WARN_DUE_TIME_ALARM_TAG:
                            //do something.
                            Log.d(TAG, "(DueTimeTask) warn due time sent!");
                            break;
                        case Constants.DUE_TIME_ALARM_TAG:
                            Log.d(TAG, "(DueTimeTask) due time sent!");
                            //do another something;
                            break;
                        default:
                            Log.e(TAG, "(DueTimeTask) post type unexpected!!!");
                    }
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}