package com.sead.demand.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class ExternalRegisterActivity extends AppCompatActivity {
    private ExternalRegisterActivity mActivity;

    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private Button mRegisterBtn;
    private RegistrationTask mRegistrationTask;
    private ProgressDialog mPDRegister;
    private CheckBox mInstitutional;
    private CheckBox mNotInstitutional;
    private EditText mInstitutionName;
    private Spinner mInstitutionalType;

    public ExternalRegisterActivity() {
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPDRegister = new ProgressDialog(mActivity);

        mName = (EditText) findViewById(R.id.register_external_name);
        mEmail = (EditText) findViewById(R.id.register_external_email);
        mPassword = (EditText) findViewById(R.id.register_external_password);
        mConfirmPassword = (EditText) findViewById(R.id.register_external_confirm_password);
        mInstitutional = (CheckBox) findViewById(R.id.register_external_institutional_check_box);
        mNotInstitutional = (CheckBox) findViewById(R.id.register_external_not_institutional_check_box);
        mInstitutionName = (EditText) findViewById(R.id.register_external_institution_name);
        mInstitutionalType = (Spinner) findViewById(R.id.register_external_institution_type);
        mRegisterBtn = (Button) findViewById(R.id.register_external_sign_up_button);

        mInstitutional.setChecked(true);

        mInstitutional.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNotInstitutional.setChecked(!isChecked);
                mInstitutionName.setEnabled(isChecked);
                mInstitutionalType.setEnabled(isChecked);
            }
        });

        mNotInstitutional.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mInstitutional.setChecked(!isChecked);
                mInstitutionName.setEnabled(!isChecked);
                mInstitutionalType.setEnabled(!isChecked);
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isOnline(mActivity)) {
                    attemptToRegister();
                } else {
                    Snackbar.make(mRegisterBtn, R.string.internet_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void attemptToRegister() {
        if (mRegistrationTask != null) return;
        mName.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);
        mInstitutionName.setError(null);
        boolean cancel = false;
        View focusView = null;
        String institution = "";
        String institutionType = "";

        if (mInstitutional.isChecked()) {
            institution = mInstitutionName.getText().toString();
            institutionType = mInstitutionalType.getSelectedItem().toString();
        }

        User user = new User(
                mEmail.getText().toString(),
                mName.getText().toString(),
                mPassword.getText().toString(),
                FirebaseInstanceId.getInstance().getToken(),
                Constants.EXTERNAL_USER,
                institution,
                institutionType
        );

        if (!confirmPasswordMatch(user.getPassword(),mConfirmPassword.getText().toString())) {
            mConfirmPassword.setError("As senhas não combinam");
            focusView = mConfirmPassword;
            cancel = true;
        }

        if (!isPasswordValid(user.getPassword())) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        if (!isInstitutionValid(mInstitutionName.getText().toString())) {
            mInstitutionName.setError(getString(R.string.error_field_required));
            focusView = mInstitutionName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(user.getEmail())) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(user.getEmail())) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        if (!isNameValid(user.getName())){
            mName.setError(getString(R.string.error_field_required));
            focusView = mName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mRegistrationTask = new RegistrationTask(user);
            mRegistrationTask.execute();
        }

    }

    private boolean isInstitutionValid(String institutionName) {
        if (mInstitutional.isChecked()) {
            if (institutionName.isEmpty()) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private boolean isNameValid(String name) {
        return !name.isEmpty();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    private boolean confirmPasswordMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
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

    private class RegistrationTask extends AsyncTask<Void, Void, String> {
        User user;

        public RegistrationTask(User user) {
            this.user = user;
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("email", user.getEmail());
            values.put("password", user.getPassword());
            values.put("name", user.getName());
            values.put("gcm", user.getGcm());
            values.put("type", user.getType());
            values.put("institution", user.getInstitution());
            values.put("institution_type", user.getInstitutionType());
            return CommonUtils.POST("/user/register", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mRegistrationTask = null;

            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    if (mPDRegister.isShowing()) mPDRegister.dismiss();
                    Snackbar.make(mRegisterBtn, "Registro realizado com sucesso", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    intent.putExtra("isRegistered", true);
                    startActivity(intent);
                    finish();
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                Snackbar.make(mRegisterBtn, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }
        }
    }

}
