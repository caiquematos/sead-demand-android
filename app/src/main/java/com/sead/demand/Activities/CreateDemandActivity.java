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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.Department;
import com.sead.demand.Entities.PredefinedReason;
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
    private SharedPreferences mPrefs;
    private int mPage;
    private int mMenuType;
    private FloatingActionButton mFab;
    private FetchDemandsTask mFetchDemandsTask;
    private FetchUsersTask mFetchUsersTask;
    private FetchDepartmentsTask mFetchDepartmentsTask;
    private ProgressDialog mPDDepartment;
    private ArrayList<Department> mDepartmentsArray;
    private ArrayList<DemandType> mDemandTypesArray;
    private ArrayList<User> mUsersArray;
    private CheckBox mCheckBox;
    private User mUser;
    private User mReceiver;
    private DemandType mDemandTypeSelected;
    private SendingPermissionTask mSendingPermissionTask;

    public CreateDemandActivity() {
        this.mActivity = this;
        this.mPage = Constants.CREATE_PAGE;
        this.mMenuType = Constants.NO_MENU;
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
        mUser = CommonUtils.getCurrentUserPreference(this);

        mSubjectView = (EditText) findViewById(R.id.demand_subject_et);
        mDescriptionView = (EditText) findViewById(R.id.demand_description_et);

        mDepartmentSpinner = (Spinner) findViewById(R.id.demand_department_spinner);
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

        mDemandTypesSpinner = (Spinner) findViewById(R.id.demand_type_spinner);
        mDemandTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDemandTypeSelected = mDemandTypesArray.get(position);
                mReceiver = null;
                mReceiverView.setText("");
                fetchUsers(position, mUser);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mReceiverView = (AutoCompleteTextView) findViewById(R.id.demand_receiver_act);
        mReceiverView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "To position:" + position + " Id:" + id);
                mReceiver = mUsersArray.get(position);
                Log.d(TAG, "Receiver Selected:" + mReceiver.toString());
            }
        });

        mCheckBox = (CheckBox) findViewById(R.id.demand_check_box);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDepartmentSpinner.setEnabled(!isChecked);
                mDemandTypesSpinner.setEnabled(!isChecked);
                mReceiverView.setEnabled(!isChecked);
            }
        });

        // TODO: Create a job service for each attempt to queue it.
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSendDemand();
            }
        });

        this.checkSendingPermission();
    }

    private void checkSendingPermission() {
        if (CommonUtils.isOnline(this)) {
            if (mSendingPermissionTask == null) {
                mSendingPermissionTask = new SendingPermissionTask(mUser);
                mSendingPermissionTask.execute();
            }
        } else {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void fetchUsers(int position, User user) {
        if (CommonUtils.isOnline(this)) {
            if (mFetchUsersTask == null) {
                mFetchUsersTask = new FetchUsersTask(position, user);
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
        if (!CommonUtils.isOnline(mActivity)) {
            Snackbar.make(mFab, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (mDemandTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;
        Demand demand;

        // Reset errors.
        mSubjectView.setError(null);
        mReceiverView.setError(null);
        // Get logged in user email

        if (mCheckBox.isChecked()) {
            mReceiver = null;
            mDemandTypeSelected = null;
        } else {
            if (mReceiver == null){
                mReceiverView.setError(getString(R.string.error_field_required));
                focusView = mReceiverView;
                cancel = true;
            } else {
                Log.d(TAG, "Receiver Email:" + mReceiver.getEmail());

                if (!mReceiver.getName().equals(mReceiverView.getText().toString())){
                    // in case user type a letter or erase it.
                    mReceiverView.setError(getString(R.string.error_send_demand_receiver));
                    focusView = mReceiverView;
                    cancel = true;
                }
            }
        }

        demand = new Demand(
                -1,
                -1,
                mUser, // this current user.
                mReceiver, // created at selection, @null.
                null,
                mDemandTypeSelected,
                mSubjectView.getText().toString(),
                mDescriptionView.getText().toString(),
                Constants.UNDEFINE_STATUS,
                Constants.NO,
                0,
                0,
                null,
                null);

        Log.d(TAG, "Demand Created:" + demand.toString());

        if (demand.getSubject().isEmpty()){
            mSubjectView.setError(getString(R.string.error_field_required));
            focusView = mSubjectView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt to send and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the send demand attempt.
            mDemandTask = new SendDemandTask(mCheckBox.isChecked(),demand);
            mDemandTask.execute((Void) null);
        }
    }

    private class SendDemandTask extends  AsyncTask<Void, Void, String>{
        private final Demand demand;
        private final boolean isChecked;

        public SendDemandTask(boolean isChecked, Demand demand) {
            this.demand = demand;
            this.isChecked = isChecked;
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
            if (!isChecked) {
                Log.d(TAG, "receiver:" + demand.getReceiver().getEmail());
                values.put("receiver", demand.getReceiver() != null ? demand.getReceiver().getEmail() : "");
                values.put("type", demand.getType().getId());
            }
            values.put("is_department_undefined", isChecked);
            values.put("sender", demand.getSender().getEmail());
            values.put("subject", demand.getSubject());
            values.put("description", demand.getDescription());
            return CommonUtils.POST("/send/send", values);
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
            JSONObject demandTypeJson;
            JSONObject reasonJson;
            Demand demandResponse = null;
            boolean success = false;

            Log.d(TAG, "string json: " + jsonResponse);

            try {
                DemandType demandType = null;
                PredefinedReason reason = null;
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                senderJson = jsonObject.getJSONObject("sender");
                receiverJson = jsonObject.getJSONObject("receiver");

                if(jsonObject.has("reason")){
                    reasonJson = jsonObject.getJSONObject("reason");
                    reason = PredefinedReason.build(reasonJson);
                    Log.e(TAG, " reason:" + reason.toString());
                }

                if (!jsonObject.isNull("demand_type")){
                    demandTypeJson = jsonObject.getJSONObject("demand_type");
                    demandType = DemandType.build(demandTypeJson);
                }

                demandJson = jsonObject.getJSONObject("demand");

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                demandResponse = Demand.build(
                        sender,
                        receiver,
                        reason,
                        demandType,
                        demandJson
                );

                Log.d(TAG,
                        "(SendDemandTask) demand:" + demandResponse.toString()
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
                CommonUtils.storeDemandDB(demandResponse, Constants.INSERT_DEMAND_SENT, mActivity);
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
        private DemandType demandType;
        private User user;

        public FetchUsersTask(int position, User user) {
            this.demandType = mDemandTypesArray.get(position);
            this.user = user;
            Log.d(TAG, "Demand Type:" + this.demandType.toString());
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("demand_type_id", demandType.getId());
            values.put("sender_id", user.getId());
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
                    mReceiverView.setAdapter(adapter);

                    if (usersNames.isEmpty()) {
                        Snackbar.make(mReceiverView, "Ainda não há colaboradores(ras) para essa demanda.", Snackbar.LENGTH_LONG).show();
                        mReceiverView.setEnabled(false);
                    } else {
                        mReceiverView.setEnabled(true);
                        mReceiverView.showDropDown();
                    }
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    public class SendingPermissionTask extends AsyncTask<String, Void, String> {
        private User user;

        public SendingPermissionTask(User user) {
            this.user = user;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage(getString(R.string.progress_dialog_wait));
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            ContentValues values = new ContentValues();
            values.put("user_id", user.getId());
            return CommonUtils.POST("/send/check-sending-permission", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mSendingPermissionTask = null;
            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
            Log.e(TAG, s);
            try {
                JSONObject jsonObject;
                boolean success;

                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    boolean isSendingAllowed = jsonObject.getBoolean("permission");
                    if (isSendingAllowed){
                        fetchDepartments();
                    } else {
                        showSendingPermissionDialog(Constants.PERMISSION_FALSE);
                    }
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFab, R.string.server_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
                showSendingPermissionDialog(Constants.PERMISSION_ERROR);
            }
        }
    }

    private void showSendingPermissionDialog(int type) {
        String message;
        switch (type) {
            case Constants.PERMISSION_FALSE:
                message = "Há 3 ou mais demandas \"Concluídas\" pendentes. Avalie-as," +
                        " antes, para ser permitido criar uma nova demanda.";
                break;
            case Constants.PERMISSION_ERROR:
                message = "Não foi possível avaliar a permissão de envio, por favor, tente mais tarde";
                break;
            default:
                message = "Erro!";
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Permissão de Envio");
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.finish();
            }
        });
        alertBuilder.create();
        alertBuilder.show();
    }
}
