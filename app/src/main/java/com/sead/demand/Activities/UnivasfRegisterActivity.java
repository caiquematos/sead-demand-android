package com.sead.demand.Activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sead.demand.Entities.Department;
import com.sead.demand.Entities.Job;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UnivasfRegisterActivity extends AppCompatActivity {
    private UnivasfRegisterActivity mActivity;
    private final String TAG = getClass().getSimpleName();

    private EditText mName;
    private EditText mEmail;
    private AutoCompleteTextView mDepartmentAC;
    private AutoCompleteTextView mJobAC;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private DepartmentTask mDepartmentTask;
    private JobTask mJobTask;
    private Button mRegisterBT;
    private RegisterTask mRegisterTask;
    private Department mDepartment;
    private Job mJob;
    private ArrayList<Department> mDepartmentsArray;
    private ArrayList<Job> mJobsArray;

    public UnivasfRegisterActivity() {
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_univasf_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDepartmentsArray = new ArrayList<>();
        mJobsArray = new ArrayList<>();
        mDepartment = null;
        mJob = null;

        mName = (EditText) findViewById(R.id.register_univasf_name);
        mEmail = (EditText) findViewById(R.id.register_univasf_email);
        mDepartmentAC = (AutoCompleteTextView) findViewById(R.id.register_univasf_department);
        mJobAC = (AutoCompleteTextView) findViewById(R.id.register_univasf_job_title);
        mPassword = (EditText) findViewById(R.id.register_univasf_password);
        mConfirmPassword = (EditText) findViewById(R.id.register_univasf_confirm_password);
        mRegisterBT = (Button) findViewById(R.id.register_univasf_sign_up);

        mDepartmentAC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Department id: " + id);
                Log.d(TAG, mDepartmentsArray.toString());
                mDepartment = mDepartmentsArray.get((int) id);
            }
        });

        mJobAC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Job id: " + id);
                Log.d(TAG, mJobsArray.toString());
                mJob =  mJobsArray.get((int) id);
            }
        });

        mRegisterBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToRegister();
            }
        });

        if (CommonUtils.isOnline(this)) {
            if (mDepartmentTask == null) {
                mDepartmentTask = new DepartmentTask();
                mDepartmentTask.execute();
            }
            if (mJobTask == null) {
                mJobTask = new JobTask();
                mJobTask.execute();
            }
        } else {
            Toast.makeText(this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void attemptToRegister() {
        if (mRegisterTask != null) return;

        if (!CommonUtils.isOnline(mActivity)) {
            Snackbar.make(mRegisterBT, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Reset errors.
        mName.setError(null);
        mEmail.setError(null);
        mDepartmentAC.setError(null);
        mJobAC.setError(null);
        mPassword.setError(null);
        mConfirmPassword.setError(null);

        boolean cancel = false;
        View focusView = null;

        User user = new User(
                mEmail.getText().toString(),
                mName.getText().toString(),
                mPassword.getText().toString(),
                mDepartment,
                mJob,
                FirebaseInstanceId.getInstance().getToken(),
                Constants.UNIVASF_USER
        );

        Log.d(TAG, "user: " + user.toString());

        if (!isPasswordAMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
            mConfirmPassword.setError("Password doesn't match");
            focusView = mConfirmPassword;
            cancel = true;
        }

        if (!isPasswordValid(mPassword.getText().toString())) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        if (mJobAC.getText().toString().isEmpty()) {
            mJobAC.setError(getString(R.string.error_field_required));
            focusView = mJobAC;
            cancel = true;
        }

        if(!isJobValid(mJobAC.getText().toString())) {
            mJobAC.setError("Escolha um/uma cargo/função que esteja na lista");
            focusView = mJobAC;
            cancel = true;
        }

        if (mDepartmentAC.getText().toString().isEmpty()) {
            mDepartmentAC.setError(getString(R.string.error_field_required));
            focusView = mDepartmentAC;
            cancel = true;
        }

        if(!isDepartmentValid(mDepartmentAC.getText().toString())) {
            mDepartmentAC.setError("Escolha um setor que esteja na lista");
            focusView = mDepartmentAC;
            cancel = true;
        }

        if (!isEmailValid(mEmail.getText().toString())) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        if (mName.getText().toString().isEmpty()) {
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mRegisterTask = new RegisterTask(user);
            mRegisterTask.execute((Void) null);
        }

    }

    private boolean isJobValid(String s) {
        boolean tag = false;
        for (int i=0; i < mJobsArray.size(); i++) {
           if (mJobsArray.get(i).getTitle().equals(s)) {
               tag = true;
               break;
           }
        }
        return tag;
    }

    private boolean isDepartmentValid(String s){
        boolean tag = false;
        for (int i=0; i < mDepartmentsArray.size(); i++) {
            if (mDepartmentsArray.get(i).getTitle().equals(s)) {
                tag = true;
                break;
            }
        }
        return tag;
    }

    private boolean isPasswordValid(String s) {
        return s.length() > 3;
    }

    private boolean isPasswordAMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    private boolean isEmailValid(String s) {
        return s.contains("@");
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Deseja sair antes de se registrar?");
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



    private void populateDepartment(JSONArray jsonArray) throws JSONException {
        List<String> departmentsTitles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Department department = Department.build(jsonArray.getJSONObject(i));
            mDepartmentsArray.add(department);
            departmentsTitles.add(department.getTitle());
        }

        if (!departmentsTitles.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, departmentsTitles);
            mDepartmentAC.setAdapter(adapter);
            if (!mDepartmentAC.isEnabled()) mDepartmentAC.setEnabled(true);
            mDepartmentAC.showDropDown();
        } else {
            Snackbar.make(mDepartmentAC,"Não há setores disponíveis!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (mDepartmentAC.isEnabled()) mDepartmentAC.setEnabled(false);
        }
    }

    private void populateJob(JSONArray jsonArray) throws JSONException {
        List<String> jobsTitles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Job job = Job.build(jsonArray.getJSONObject(i));
            mJobsArray.add(job);
            jobsTitles.add(job.getTitle());
        }

        if (!jobsTitles.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, jobsTitles);
            mJobAC.setAdapter(adapter);
            if (!mJobAC.isEnabled()) mJobAC.setEnabled(true);
        } else {
            Snackbar.make(mJobAC,"Não há cargos disponíveis!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (mJobAC.isEnabled()) mJobAC.setEnabled(false);
        }
    }

    private class DepartmentTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            return CommonUtils.POST("/univasf/departments", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDepartmentTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    JSONArray jsonArray = jsonObject.getJSONArray("departments");
                    populateDepartment(jsonArray);
                } else {
                    throw new JSONException("json hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar.make(mDepartmentAC, "Erro ao carregar setores!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class JobTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            return CommonUtils.POST("/univasf/jobs", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mJobTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    JSONArray jsonArray = jsonObject.getJSONArray("jobs");
                    populateJob(jsonArray);
                } else {
                    throw new JSONException("json hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar.make(mJobAC, "Erro ao carregar funções e cargos!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class RegisterTask extends AsyncTask<Void, Void, String> {
        User user;

        public RegisterTask(User user) {
            this.user = user;
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("email", user.getEmail());
            values.put("name", user.getName());
            values.put("password", user.getPassword());
            values.put("gcm", user.getGcm());
            values.put("type", user.getType());
            values.put("department_id", user.getDepartment().getId());
            values.put("job_id", user.getJob().getId());
            return CommonUtils.POST("/user/register", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mRegisterTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    Snackbar.make(mRegisterBT, "Usuário cadastrado com sucesso.", Snackbar.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    throw new JSONException("json hit false");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar.make(mRegisterBT, getString(R.string.server_error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
