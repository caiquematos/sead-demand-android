package com.example.caiqu.demand.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class RejectDialogActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private TextView mReasonTitle;
    private EditText mReasonEditText;
    private Spinner mReasonSpinner;
    private TextView mReasonCancel;
    private TextView mReasonOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reject_dialog);

        mReasonTitle = (TextView) findViewById(R.id.reject_dialog_reason_title);
        mReasonEditText = (EditText) findViewById(R.id.reject_dialog_reason_edit);

        mReasonSpinner = (Spinner) findViewById(R.id.reject_dialog_reason_spinner);
        mReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 4) mReasonEditText.setVisibility(View.VISIBLE);
                else mReasonEditText.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                Intent intent = new Intent();
                intent.putExtra(Constants.INTENT_REJECT_REASON_COMMENT, mReasonEditText.getText().toString());
                intent.putExtra(Constants.INTENT_REJECT_REASON_INDEX, mReasonSpinner.getSelectedItemPosition());
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }

}