package com.example.caiqu.demand.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.caiqu.demand.Entities.PredefinedReason;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RejectDialogActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private EditText mReasonDescriptionET;
    private EditText mReasonTitleET;
    private Spinner mReasonSpinner;
    private ArrayList<String> mReasonsArray;
    private List<PredefinedReason> mPredefinedReasonList;
    private FetchPredefinedReasonTask mFetchPredefinedReason;
    private TextView mReasonCancel;
    private TextView mReasonOk;
    private ProgressBar mRejectPB;
    private  ArrayAdapter<String> mReasonSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reject_dialog);

        mReasonDescriptionET = (EditText) findViewById(R.id.reject_dialog_reason_edit);
        mReasonTitleET = (EditText) findViewById(R.id.reject_reason_title);
        mRejectPB = (ProgressBar) findViewById(R.id.reject_progress_bar);
        mReasonSpinner = (Spinner) findViewById(R.id.reject_dialog_reason_spinner);
        mReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    mReasonTitleET.setVisibility(View.VISIBLE);
                    mReasonDescriptionET.setVisibility(View.VISIBLE);
                }
                else {
                    mReasonTitleET.setVisibility(View.GONE);
                    mReasonDescriptionET.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        if (CommonUtils.isOnline(this)) fetchPredefinedReasons();
        else {
            Snackbar.make(mReasonSpinner,R.string.internet_error,Snackbar.LENGTH_SHORT).show();
            mRejectPB.setVisibility(View.GONE);
            mReasonSpinner.setVisibility(View.VISIBLE);
            mReasonSpinner.setEnabled(false);
        }

        mReasonCancel = (TextView) findViewById(R.id.reject_dialog_reason_cancel);
        mReasonCancel.setClickable(true);
        mReasonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "button cancel clicked");
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mReasonOk = (TextView) findViewById(R.id.reject_dialog_reason_ok);
        mReasonOk.setClickable(true);
        mReasonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "button ok clicked");
                int position = mReasonSpinner.getSelectedItemPosition();
                if (position == 0) {
                    if (setCustomReason()) sendResult(position);
                    else return;
                } else {
                    sendResult(position);
                }
            }
        });

    }

    private void sendResult(int position) {
        Intent intent = new Intent();
        intent.putExtra(
                Constants.INTENT_REJECT_PREDEFINED_REASON,
                mPredefinedReasonList.get(position)
        );
        setResult(RESULT_OK,intent);
        finish();
    }

    // Setting custom reason (other).
    private boolean setCustomReason() {
        String reasonTitle = mReasonTitleET.getText().toString();
        if (reasonTitle.isEmpty()){
            mReasonTitleET.setError(getString(R.string.error_field_required));
            mReasonTitleET.requestFocus();
            return false;
        } else {
            String reasonDescription = mReasonDescriptionET.getText().toString();
            PredefinedReason predefinedReason = new PredefinedReason(
                    -1,
                    -1,
                    Constants.REASON_OTHER_TYPE,
                    reasonTitle,
                    reasonDescription,
                    "",
                    ""
            );
            mPredefinedReasonList.add(Constants.REASON_OTHER_POSITION,predefinedReason);
            return true;
        }
    }

    private void fetchPredefinedReasons() {
        if (mFetchPredefinedReason == null) {
            mFetchPredefinedReason = new FetchPredefinedReasonTask();
            mFetchPredefinedReason.execute();
        } else {
            return;
        }
    }

    private class FetchPredefinedReasonTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            mFetchPredefinedReason = null;

            JSONObject jsonObject;
            boolean success;

            try {
                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    mPredefinedReasonList = new ArrayList<>();
                    mPredefinedReasonList.add(new PredefinedReason(-1, -1, "", "", "", "", ""));
                    mReasonsArray = new ArrayList<>();
                    mReasonsArray.add(getString(R.string.reason_default_item)); // This is default reason in position 0.
                    JSONArray predefinedReasonJsonArray = jsonObject.getJSONArray("reasons");

                    for (int i = 0; i < predefinedReasonJsonArray.length(); i++) {
                        PredefinedReason predefinedReason;
                        predefinedReason = PredefinedReason.build((JSONObject) predefinedReasonJsonArray.get(i));
                        mPredefinedReasonList.add(predefinedReason);
                        if (predefinedReason != null)
                            mReasonsArray.add("Ref.: "
                                + predefinedReason.getServerId()
                                + " - "
                                + predefinedReason.getTitle()
                            );
                    }

                    mReasonSpinnerAdapter = new ArrayAdapter<>(
                            getApplicationContext(), R.layout.custom_spinner_item, mReasonsArray);
                    mReasonSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mReasonSpinner.setAdapter(mReasonSpinnerAdapter);
                    mRejectPB.setVisibility(View.GONE);
                    mReasonSpinner.setVisibility(View.VISIBLE);
                } else {
                    Snackbar.make(mReasonSpinner, R.string.internet_error,Snackbar.LENGTH_SHORT).show();
                }
            } catch(JSONException e) {
                mRejectPB.setVisibility(View.GONE);
                mReasonSpinner.setEnabled(false);
                mReasonOk.setEnabled(false);
                mReasonSpinner.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

            // mRejectPD.cancel();
        }
    }

}