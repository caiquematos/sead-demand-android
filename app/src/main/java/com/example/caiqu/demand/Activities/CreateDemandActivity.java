package com.example.caiqu.demand.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.caiqu.demand.Databases.MyDBManager;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CreateDemandActivity extends AppCompatActivity{
    private final String TAG = getClass().getSimpleName();

    private Spinner mPositionView;
    private Spinner mPriorView;
    private AutoCompleteTextView mReceiverView;
    private EditText mSubjectView;
    private EditText mDescriptionView;
    private ProgressDialog mPDDemand;
    private CreateDemandActivity mActivity;
    private SendDemandTask mDemandTask;
    private  ArrayList<String> mEmployeesEmails;
    private int mReceiverIndex;
    private String mReceiverName;
    private SharedPreferences mPrefs;
    private int mPage;
    private int mMenuType;
    private FloatingActionButton mFab;
    private List<String> mAutocompleteArray;

    public CreateDemandActivity() {
        this.mActivity = this;
        this.mPage = Constants.CREATE_PAGE;
        this.mMenuType = Constants.SHOW_NO_MENU;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPDDemand = new ProgressDialog(this);
        mReceiverIndex = -1;

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mReceiverView = (AutoCompleteTextView) findViewById(R.id.demand_receiver_act);
        mSubjectView = (EditText) findViewById(R.id.demand_subject_et);
        mDescriptionView = (EditText) findViewById(R.id.demand_description_et);

        mPositionView = (Spinner) findViewById(R.id.demand_position_spinner);
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Constants.JOB_POSITIONS);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPositionView.setAdapter(positionAdapter);

        mPriorView = (Spinner) findViewById(R.id.demand_prior_spinner);
        ArrayAdapter<String> priorAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.array_status));
        priorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPriorView.setAdapter(priorAdapter);

        mPositionView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                new PopulateReceiverTask(Constants.JOB_POSITIONS[position]).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mReceiverView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mReceiverName = mReceiverView.getText().toString();
                mReceiverIndex = mAutocompleteArray.indexOf(mReceiverName);
                Log.d(TAG, "Receiver Index:" + mReceiverIndex
                        + " Receiver name:" + mReceiverName);
            }
        });

        // TODO: Create a job for each attempt to queue it.
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSendDemand();
            }
        });

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Deseja sair sem enviar a demanda?");
        alert.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.finish();
            }
        });
        alert.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void attemptSendDemand() {
        String senderEmail = "";
        boolean cancel = false;
        View focusView = null;
        Demand demand = null;

        if (!CommonUtils.isOnline(mActivity)) {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (mDemandTask != null) {
            return;
        }

        // Reset errors.
        mSubjectView.setError(null);
        mReceiverView.setError(null);
        // Get logged in user email
        senderEmail = mPrefs.getString(Constants.LOGGED_USER_EMAIL,"");
        Log.d(TAG, "Shared Prefs:" + senderEmail);
        Log.d(TAG, "Receiver Index:" + mReceiverIndex);
        Log.d(TAG, "Receiver Email:" + mEmployeesEmails.get(mReceiverIndex));


        User sender = CommonUtils.getCurrentUserPreference(this);
        User receiver = new User(
                mReceiverIndex,
                "",
                mEmployeesEmails.get(mReceiverIndex)
        );

        if (mReceiverIndex == -1){
            mReceiverView.setError(getString(R.string.error_field_required));
            focusView = mReceiverView;
            cancel = true;
        } else {
            Log.d(TAG, "Receiver Email:" + mEmployeesEmails.get(mReceiverIndex));
            Log.e(TAG, "Prior position:" + mPriorView.getSelectedItemPosition());
            String priorTag = CommonUtils.getPriorTag(mPriorView.getSelectedItemPosition());
            Log.e(TAG, "Prior Tag:" + priorTag);

            demand = new Demand(
                    -1,
                    -1,
                    sender,
                    receiver,
                    null,
                    priorTag,
                    mSubjectView.getText().toString(),
                    mDescriptionView.getText().toString(),
                    Constants.UNDEFINE_STATUS,
                    Constants.NO,
                    null,
                    null);

            Log.d(TAG, "Demand Created:" + demand.toString());

            if (demand.getSubject().isEmpty()){
                mSubjectView.setError(getString(R.string.error_field_required));
                focusView = mSubjectView;
                cancel = true;
            }

            if (demand.getReceiver().getEmail().isEmpty()){
                mReceiverView.setError(getString(R.string.error_field_required));
                focusView = mReceiverView;
                cancel = true;
            }

            if (!mReceiverName.equals(mReceiverView.getText().toString())){
                mReceiverView.setError(getString(R.string.error_send_demand_receiver));
                focusView = mReceiverView;
                cancel = true;
            }

        }

        if (cancel) {
            // There was an error; don't attempt to send and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the send demand attempt.
            mDemandTask = new SendDemandTask(demand);
            mDemandTask.execute((Void) null);
        }

    }

    private class SendDemandTask extends  AsyncTask<Void, Void, String>{
        private final Demand demand;

        public SendDemandTask(Demand demand) {
            this.demand = demand;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Por favor aguarde");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("sender", demand.getSender().getEmail());
            values.put("receiver", demand.getReceiver().getEmail());
            values.put("prior", demand.getPrior());
            values.put("subject", demand.getSubject());
            values.put("description", demand.getDescription());
            return CommonUtils.POST("/demand/send/", values);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mDemandTask = null;

            JSONObject jsonObject;
            JSONObject senderJson;
            JSONObject receiverJson;
            JSONObject demandJson;
            Demand demandResponse = null;
            boolean success = false;

            Log.d(TAG, "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                senderJson = jsonObject.getJSONObject("sender");
                receiverJson = jsonObject.getJSONObject("receiver");
                demandJson = jsonObject.getJSONObject("demand");

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                demandResponse = Demand.build(
                        sender,
                        receiver,
                        null,
                        demandJson
                );

                Log.e(TAG,
                        "Json:" + demandResponse.toString()
                        + " sender:" + sender.toString()
                        + " receiver:" + receiver.toString()
                );
            } catch (JSONException e) {
                Snackbar.make(mFab, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

            if (success && demandResponse != null) {
                MyDBManager myDBManager = new MyDBManager(mActivity);
                long wasDemandStored = myDBManager.addDemand(demandResponse);

                if(wasDemandStored >= 0) {
                    CommonUtils.notifyDemandListView(
                            demandResponse,
                            Constants.BROADCAST_SENT_FRAG,
                            Constants.INSERT_DEMAND_SENT,
                            mActivity
                    );
                    Log.e(TAG, "Demand was stored.");
                }
                else Log.e(TAG, "Demand could not be stored!");

                Intent intent = new Intent(mActivity, ViewDemandActivity.class);
                intent.putExtra(Constants.INTENT_ACTIVITY, mActivity.getClass().getSimpleName());
                intent.putExtra(Constants.INTENT_PAGE, mPage);
                intent.putExtra(Constants.INTENT_MENU, mMenuType);
                intent.putExtra(Constants.INTENT_DEMAND, demandResponse);
                finish();
                mActivity.startActivity(intent);
            } else {
                Snackbar.make(mFab, R.string.send_demand_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    //Handle post request for superior array
    private class PopulateReceiverTask extends AsyncTask<Void, Void, String> {
        String mJobPosition;

        public PopulateReceiverTask(String jobPosition) {
            this.mJobPosition = jobPosition;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Buscando colaboradores...");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("position", this.mJobPosition);
            String response = CommonUtils.POST("/user/employee/", values);
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);

            JSONObject jsonObject;
            JSONArray jsonArray = null;
            boolean success = false;

            Log.d("ON POST EXECUTE LOGIN", "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonArray = jsonObject.getJSONArray("employees");
            } catch (JSONException e) {
                Snackbar.make(mPositionView, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if(success){
                mEmployeesEmails = new ArrayList<>();
                mAutocompleteArray =  new ArrayList<>();
                for(int i=0; i < jsonArray.length(); i++){
                    try {
                        JSONObject json = jsonArray.getJSONObject(i);
                        mAutocompleteArray.add(i,json.getString("name"));
                        mEmployeesEmails.add(i,json.getString("email"));
                        Log.d("ON DEMAND", "" + mAutocompleteArray.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                        android.R.layout.simple_dropdown_item_1line, mAutocompleteArray);
                mReceiverView.setAdapter(adapter);

                if (mAutocompleteArray.isEmpty())
                    Snackbar.make(mPositionView, R.string.position_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }else{
                Snackbar.make(mPositionView, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
        }
    }

}
