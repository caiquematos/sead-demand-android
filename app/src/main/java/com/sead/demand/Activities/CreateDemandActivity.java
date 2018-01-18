package com.sead.demand.Activities;

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

import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.Department;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateDemandActivity extends AppCompatActivity{
    private final String TAG = getClass().getSimpleName();

    private Spinner mDemandTypesSpinner;
    private Spinner mDepartmentSpinner;
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
    private User mCurrentUser;
    private FetchDemandsTask mFetchDemandsTask;
    private FetchUsersTask mFetchUsersTask;
    private FetchDepartmentsTask mFetchDepartmentsTask;
    private ProgressDialog mPDDepartment;
    private ArrayList<Department> mDepartmentsArray;
    private ArrayList<DemandType> mDemandTypesArray;

    public CreateDemandActivity() {
        this.mActivity = this;
        this.mPage = Constants.CREATE_PAGE;
        this.mMenuType = Constants.SHOW_NO_MENU;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPDDemand = new ProgressDialog(this);
        mPDDepartment = new ProgressDialog(this);
        mReceiverIndex = -1;

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mReceiverView = (AutoCompleteTextView) findViewById(R.id.demand_receiver_act);
        mSubjectView = (EditText) findViewById(R.id.demand_subject_et);
        mDescriptionView = (EditText) findViewById(R.id.demand_description_et);

        mDepartmentSpinner = (Spinner) findViewById(R.id.demand_department_spinner);
        mDemandTypesSpinner = (Spinner) findViewById(R.id.demand_demand_spinner);

        mDepartmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "department selected:" + position);
                fetchDemands(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDemandTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchUsers(position);
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

        fetchDepartments();
    }

    private void fetchUsers(int position) {
        if (CommonUtils.isOnline(this)) {
            if (mFetchUsersTask == null) {
                mFetchUsersTask = new FetchUsersTask(position);
                mFetchUsersTask.execute();
            }
        } else {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void fetchDemands(int position) {
        long departmentId = mDepartmentsArray.get(position).getId();
        Log.d(TAG, "Department id selected: " + departmentId);

        if (CommonUtils.isOnline(this)) {
            if (mFetchDemandsTask == null) {
                mFetchDemandsTask = new FetchDemandsTask(departmentId);
                mFetchDemandsTask.execute();
            }
        } else {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void fetchDepartments() {
        if (CommonUtils.isOnline(this)) {
            if (mFetchDepartmentsTask == null) {
                mFetchDepartmentsTask = new FetchDepartmentsTask();
                mFetchDepartmentsTask.execute();
            }
        } else {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
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

            demand = new Demand(
                    -1,
                    -1,
                    sender,
                    receiver,
                    null,
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
            return CommonUtils.POST("/demand/send", values);
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

    private class FetchDemandsTask extends AsyncTask<Void, Void, String> {
        private long departmentId;

        public FetchDemandsTask(long position) {
            this.departmentId = position;
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("department", departmentId);
            return CommonUtils.POST("/demandtype/get-by-department", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mFetchDemandsTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    JSONArray demandTypes = jsonObject.getJSONArray("demandtypes");
                    List<String> demandTypesTitles = new ArrayList<>();
                    mDemandTypesArray = new ArrayList<>();
                    for (int i = 0; i < demandTypes.length(); i++) {
                        DemandType demandType = DemandType.build(demandTypes.getJSONObject(i));
                        mDemandTypesArray.add(demandType);
                        demandTypesTitles.add(demandType.getTitle());
                    }

                    if (!demandTypesTitles.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, demandTypesTitles);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mDemandTypesSpinner.setAdapter(adapter);
                        if (!mDemandTypesSpinner.isEnabled()) mDemandTypesSpinner.setEnabled(true);
                    } else {
                        Snackbar.make(mFab,"Não há demandas disponíveis nesse setor!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        if (mDemandTypesSpinner.isEnabled())  mDemandTypesSpinner.setEnabled(false);
                    }
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchUsersTask extends AsyncTask<Void, Void, String> {
        private int position;

        public FetchUsersTask(int position) {
            this.position = position;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private class FetchDepartmentsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDepartment.setMessage("Carregando setores...");
            mPDDepartment.setCancelable(false);
            mPDDepartment.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            return CommonUtils.POST("/department/all", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mFetchDepartmentsTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");

                if (success) {
                    mDepartmentsArray = new ArrayList<>();
                    List<String> departmentsTitles = new ArrayList<>();
                    JSONArray jsonDepartments = jsonObject.getJSONArray("departments");
                    for (int i = 0; i < jsonDepartments.length(); i++) {
                        Department department = Department.build(jsonDepartments.getJSONObject(i));
                        mDepartmentsArray.add(department);
                        departmentsTitles.add(department.getTitle());
                    }

                    if (!departmentsTitles.isEmpty()) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, departmentsTitles);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mDepartmentSpinner.setAdapter(adapter);
                        if (!mDepartmentSpinner.isEnabled()) mDepartmentSpinner.setEnabled(true);
                    } else {
                        Snackbar.make(mFab,"Não há setores disponíveis!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        if (mDepartmentSpinner.isEnabled()) mDepartmentSpinner.setEnabled(false);
                    }
                } else {
                    throw new JSONException("Success hit false");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (mPDDepartment.isShowing()) {
                mPDDepartment.dismiss();
            }
        }
    }
}
