package com.sead.demand.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sead.demand.Adapters.ReasonAdapter;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateReasonActivity extends AppCompatActivity {
    public String TAG = getClass().getSimpleName();
    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private TextView mListWarning;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<PredefinedReason> mReasonsList;
    private ReasonAdapter mAdapter;
    private FetchPredefinedReasonTask mFetchPredefinedReasonTask;
    private ProgressBar mListProgressBar;
    private ProgressDialog mCreateReasonPD;
    private CreatePredefinedReasonTask mCreatePredefinedReasonTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reason);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleEditText = (EditText) findViewById(R.id.reason_title);
        mDescriptionEditText = (EditText) findViewById(R.id.reason_description);
        mListWarning = (TextView) findViewById(R.id.reason_list_warning);
        mListProgressBar = (ProgressBar) findViewById(R.id.reason_list_progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.reasons_recycler_view);
        mCreateReasonPD = new ProgressDialog(this);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.reason_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CommonUtils.isOnline(getApplicationContext())) createPredefinedReason();
                else Snackbar.make(view, R.string.internet_error,Snackbar.LENGTH_SHORT).show();
            }
        });
        
        if (CommonUtils.isOnline(this)) fetchPredefinedReasons();
        else Snackbar.make(mRecyclerView, R.string.internet_error,Snackbar.LENGTH_SHORT).show();
        
    }

    private void createPredefinedReason() {
        boolean cancel = false;
        View view = null;

        if (mCreatePredefinedReasonTask != null) return;

        if (mDescriptionEditText.getText().toString().isEmpty()) {
            mDescriptionEditText.setError(getString(R.string.error_field_required));
            view = mDescriptionEditText;
            cancel = true;
        }

        if (mTitleEditText.getText().toString().isEmpty()) {
            mTitleEditText.setError(getString(R.string.error_field_required));
            view = mTitleEditText;
            cancel = true;
        }

        if (cancel) {
            view.requestFocus();
        } else {
            mCreatePredefinedReasonTask = new CreatePredefinedReasonTask(
                    Constants.REASON_PREDEFINED_TYPE,
                    mTitleEditText.getText().toString(),
                    mDescriptionEditText.getText().toString()
            );
            mCreatePredefinedReasonTask.execute();
        }

    }

    private void fetchPredefinedReasons() {
        if (mFetchPredefinedReasonTask == null) {
            mFetchPredefinedReasonTask = new FetchPredefinedReasonTask();
            mFetchPredefinedReasonTask.execute();
        } else {
            return;
        }
    }

    private class FetchPredefinedReasonTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListWarning.setVisibility(View.GONE);
            mListProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            String response = CommonUtils.POST("/predefined-reason/fetch-all/", values);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mFetchPredefinedReasonTask = null;

            JSONObject jsonObject;
            boolean success;

            try {
                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    if (mReasonsList == null) {
                        List<PredefinedReason> predefinedReasonList = new ArrayList<>();
                        mReasonsList = predefinedReasonList;
                    }

                    JSONArray predefinedReasonJsonArray = jsonObject.getJSONArray("reasons");

                    for (int i = 0; i < predefinedReasonJsonArray.length(); i++) {
                        PredefinedReason predefinedReason;
                        predefinedReason = PredefinedReason.build((JSONObject) predefinedReasonJsonArray.get(i));
                        if (predefinedReason != null) mReasonsList.add(predefinedReason);
                    }

                    if (mReasonsList.isEmpty()) {
                        mListWarning.setText(R.string.predefined_reason_empty);
                        mListProgressBar.setVisibility(View.GONE);
                        mListWarning.setVisibility(View.VISIBLE);
                    }

                    if (mAdapter == null) {
                        mAdapter = new ReasonAdapter(mReasonsList, getApplicationContext());
                    }

                    mRecyclerView.setAdapter(mAdapter);

                } else {
                    mListProgressBar.setVisibility(View.GONE);
                    mListWarning.setVisibility(View.VISIBLE);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            mListProgressBar.setVisibility(View.GONE);
        }
    }

    private class CreatePredefinedReasonTask extends AsyncTask<Void, Void, String> {
        private String type;
        private String title;
        private String description;

        public CreatePredefinedReasonTask(String type, String title, String description) {
            this.type = type;
            this.title = title;
            this.description = description;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCreateReasonPD.setMessage(getString(R.string.progress_dialog_wait));
            mCreateReasonPD.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("type", this.type);
            values.put("title", this.title);
            values.put("description", this.description);
            String response = CommonUtils.POST("/predefined-reason/add", values);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mCreatePredefinedReasonTask = null;

            JSONObject jsonObject;
            boolean success;

            try {
                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    JSONObject reasonJson = jsonObject.getJSONObject("reason");
                    PredefinedReason predefinedReason = PredefinedReason.build(reasonJson);

                    if (predefinedReason != null) {
                        if (mAdapter != null) mAdapter.addItem(predefinedReason);
                        else {
                            mReasonsList.add(0,predefinedReason);
                            mAdapter = new ReasonAdapter(mReasonsList, getApplicationContext());
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    } else {
                        Log.e(TAG, "Error to build predefined reason");
                    }

                    mTitleEditText.setText("");
                    mDescriptionEditText.setText("");

                } else {
                    Snackbar.make(mRecyclerView,R.string.internet_error,Snackbar.LENGTH_SHORT).show();
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }

            mCreateReasonPD.cancel();
        }
    }
}
