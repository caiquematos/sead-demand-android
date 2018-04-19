package com.sead.demand.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

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

public class TransferActivity extends AppCompatActivity {
    public String TAG = getClass().getSimpleName();
    private CheckBox mReceiverCB;
    private Spinner mDepartmentS;
    private Spinner mDemandTypeS;
    private AutoCompleteTextView mReceiverAC;
    private  FloatingActionButton mTransferFab;
    private FetchDepartmentsTask mFetchDepartmentsTask;
    private ProgressDialog mPDDemand;
    private ProgressDialog mPDDepartment;
    private ArrayList<Department> mDepartmentsArray;
    private ArrayList<DemandType> mDemandTypesArray;
    private ArrayList<User> mUsersArray;
    private Activity mActivity;
    private FetchDemandsTask mFetchDemandsTask;
    private User mReceiver;
    private DemandType mDemandTypeSelected;
    private FetchUsersTask mFetchUsersTask;
    private FetchAllUsersTask mFetchAllUsersTask;
    private Demand mDemand;

    public TransferActivity() {
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(Constants.INTENT_BUNDLE);
        if (bundle != null) {
            mDemand = (Demand) bundle.getSerializable(Constants.INTENT_DEMAND);
            if (mDemand == null) return;
        } else return;

        mTransferFab = findViewById(R.id.transfer_fab_transfer_activity);
        mTransferFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               attemptToTransfer();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPDDemand = new ProgressDialog(this);
        mPDDepartment = new ProgressDialog(this);

        mReceiverCB = findViewById(R.id.transfer_receiver_check_box);
        mDepartmentS = findViewById(R.id.transfer_department_spinner);
        mDemandTypeS = findViewById(R.id.transfer_type_spinner);
        mReceiverAC = findViewById(R.id.transfer_receiver_ac);
        mReceiverCB = findViewById(R.id.transfer_receiver_check_box);

        mDepartmentS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "department selected:" + position);
                fetchDemands(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mDemandTypeS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDemandTypeSelected = mDemandTypesArray.get(position);
                mReceiver = null;
                mReceiverAC.setText("");
                fetchUsers(mDemand, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mReceiverAC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "To position:" + position + " Id:" + id);
                mReceiver = mUsersArray.get(position);
                Log.d(TAG, "Receiver Selected:" + mReceiver.toString());
            }
        });
        mReceiverCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDepartmentS.setEnabled(!isChecked);
                mDemandTypeS.setEnabled(!isChecked);
                if(isChecked) fetchAllUsers();
                else fetchDepartments();
            }
        });

        fetchDepartments();
    }

    private void fetchAllUsers() {
        if (CommonUtils.isOnline(this)) {
            if (mFetchAllUsersTask == null) {
                mFetchAllUsersTask = new FetchAllUsersTask(mDemand);
                mFetchAllUsersTask.execute();
            }
        } else {
            Snackbar.make(mReceiverAC, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void fetchUsers(Demand demand, int position) {
        if (CommonUtils.isOnline(this)) {
            if (mFetchUsersTask == null) {
                mFetchUsersTask = new FetchUsersTask(position, demand);
                mFetchUsersTask.execute();
            }
        } else {
            Snackbar.make(mReceiverAC, R.string.internet_error, Snackbar.LENGTH_LONG)
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
            Snackbar.make(mDemandTypeS, R.string.internet_error, Snackbar.LENGTH_LONG)
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
            Snackbar.make(mTransferFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void attemptToTransfer() {
        boolean cancel = false;
        View focusView = null;

        mReceiverAC.setError(null);

        if (mReceiver == null){
            mReceiverAC.setError(getString(R.string.error_field_required));
            focusView = mReceiverAC;
            cancel = true;
        } else {
            Log.d(TAG, "Receiver Email:" + mReceiver.getEmail());

            if (!mReceiver.getName().equals(mReceiverAC.getText().toString())){
                // in case user type a letter or erase it.
                mReceiverAC.setError(getString(R.string.error_send_demand_receiver));
                focusView = mReceiverAC;
                cancel = true;
            }
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            returnResult(mReceiver);
        }
    }

    private void returnResult(User user) {
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_USER, user);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED,intent);
        finish();
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
                        mDepartmentS.setAdapter(adapter);
                        if (!mDepartmentS.isEnabled()) mDepartmentS.setEnabled(true);
                    } else {
                        Snackbar.make(mDepartmentS,"Não há setores disponíveis!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        if (mDepartmentS.isEnabled()) mDepartmentS.setEnabled(false);
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

    private class FetchDemandsTask extends AsyncTask<Void, Void, String> {
        private long departmentId;

        public FetchDemandsTask(long position) {
            this.departmentId = position;
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("department_id", departmentId);
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
                        mDemandTypeS.setAdapter(adapter);
                        if (!mDemandTypeS.isEnabled()) mDemandTypeS.setEnabled(true);
                    } else {
                        Snackbar.make(mDemandTypeS,"Não há demandas disponíveis nesse setor!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        if (mDemandTypeS.isEnabled())  mDemandTypeS.setEnabled(false);
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
        private DemandType demandType;
        private Demand demand;

        public FetchUsersTask(int position, Demand demand) {
            this.demandType = mDemandTypesArray.get(position);
            this.demand = demand;
            Log.d(TAG, "Demand Type:" + this.demandType.toString());
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("demand_type_id", demandType.getId());
            values.put("sender_id", demand.getSender().getId());
            return CommonUtils.POST("/user/get-by-demand-type", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mFetchUsersTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    JSONArray usersJson = jsonObject.getJSONArray("users");
                    List<String> usersNames = new ArrayList<>();
                    mUsersArray = new ArrayList<>();
                    for (int i = 0; i < usersJson.length(); i++) {
                        JSONObject userJson = usersJson.getJSONObject(i);
                        User user = User.build(userJson);
                        mUsersArray.add(user);
                        usersNames.add(user.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                            android.R.layout.simple_dropdown_item_1line, usersNames);
                    mReceiverAC.setAdapter(adapter);

                    if (usersNames.isEmpty()) {
                        Snackbar.make(mReceiverAC, "Ainda não há colaboradores(ras) para essa demanda.", Snackbar.LENGTH_LONG).show();
                        mReceiverAC.setEnabled(false);
                    } else {
                        mReceiverAC.setEnabled(true);
                        mReceiverAC.showDropDown();
                    }
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchAllUsersTask extends AsyncTask<Void, Void, String> {
        private Demand demand;

        public FetchAllUsersTask(Demand demand) {
            this.demand = demand;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Por favor aguarde...");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("sender_id", demand.getSender().getId());
            return CommonUtils.POST("/user/transfer-all-internal", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mFetchAllUsersTask = null;

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    JSONArray usersJson = jsonObject.getJSONArray("users");
                    List<String> usersNames = new ArrayList<>();
                    mUsersArray = new ArrayList<>();
                    for (int i = 0; i < usersJson.length(); i++) {
                        JSONObject userJson = usersJson.getJSONObject(i);
                        User user = User.build(userJson);
                        mUsersArray.add(user);
                        usersNames.add(user.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                            android.R.layout.simple_dropdown_item_1line, usersNames);
                    mReceiverAC.setAdapter(adapter);

                    if (usersNames.isEmpty()) {
                        Snackbar.make(mReceiverAC, "Ainda não há colaboradores cadastrados", Snackbar.LENGTH_LONG).show();
                        mReceiverAC.setEnabled(false);
                    } else {
                        mReceiverAC.setEnabled(true);
                        mReceiverAC.showDropDown();
                    }
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}