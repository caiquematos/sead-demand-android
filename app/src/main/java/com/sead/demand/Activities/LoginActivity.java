package com.sead.demand.Activities;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Job;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    public String TAG = getClass().getSimpleName();
    private LoginActivity mActivity;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private ProgressDialog mPDLogin;
    private SharedPreferences mPrefs;
    private Button mEmailSignInButton;

    public LoginActivity() {
        this.mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        Intent intent = getIntent();
        if (intent.hasExtra("isRegistered"))
            if (intent.getBooleanExtra("isRegistered", false))
                Snackbar.make(mEmailView, "Usuário registrado com sucesso!",Snackbar.LENGTH_LONG).show();

        populateAutoComplete();

        mPasswordView = findViewById(R.id.password);

        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CommonUtils.isOnline(mActivity)){
                    attemptLogin();
                }
                else {
                    Snackbar.make(mEmailSignInButton, R.string.internet_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        TextView goToInternSignUp = (TextView) findViewById(R.id.login_register_btn);
        goToInternSignUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView goToUnivasfSignUp = (TextView) findViewById(R.id.login_univasf_register_btn);
        goToUnivasfSignUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, UnivasfRegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView goToExternSignUp = (TextView) findViewById(R.id.login_external_register_btn);
        goToExternSignUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ExternalRegisterActivity.class);
                startActivity(intent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);

    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if (mAuthTask != null) {
            return;
        }

        if (!CommonUtils.isOnline(mActivity)) {
            Snackbar.make(mEmailSignInButton, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        Log.d(TAG, "(ON LOGIN ATTEMPT) password:" + password + " email:" + email);

        // Check for a valid password.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (email.isEmpty()) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
    }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private String mEmail = "";
        private String mPassword = "";
        private String mFcmToken = "";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            mFcmToken = FirebaseInstanceId.getInstance().getToken();
            /*
            * Replaced by the code above, because when user do logout, all data is wiped up,
            * so as GCM_TOKEN. TODO: Review its usability then. maybe not necessary.
            * mFcmToken = mPrefs.getString(Constants.GCM_TOKEN, "");
            */
            if (mFcmToken != null) {
                if (mFcmToken.isEmpty()) {
                    mFcmToken = mPrefs.getString(Constants.GCM_TOKEN, "");
                    if (mFcmToken.isEmpty()) Log.e(TAG, "(UserLoginTask) FCM Token empty!!!");
                }
            } else {
                Log.e(TAG, "(UserLoginTask) FCM Token NULL!!!");
                mFcmToken = mPrefs.getString(Constants.GCM_TOKEN, "");
                if (mFcmToken != null)
                    if (mFcmToken.isEmpty()) Log.e(TAG, "(UserLoginTask) FCM Token empty!!!");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDLogin = new ProgressDialog(mActivity);
            mPDLogin.setMessage("Por favor aguarde");
            mPDLogin.setCancelable(false);
            mPDLogin.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("email", mEmail);
            values.put("password", mPassword);
            values.put("fcm", mFcmToken);
            String response = CommonUtils.POST("/user/login", values);
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            mAuthTask = null;
            JSONObject jsonObject;
            JSONObject userJson;
            JSONObject jobJson = null;
            JSONObject superiorJson = null;
            User user;
            Job job = null;
            User superior = null;
            boolean success;

            Log.d(TAG, "(UserLoginTask) string json response: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");

                if (success) {
                    userJson = jsonObject.getJSONObject("user");

                    if(!jsonObject.isNull("job")) {
                        jobJson = jsonObject.getJSONObject("job");
                        job = Job.build(jobJson);
                        Log.d(TAG, " job:" + job.toString());
                    }

                    if (!jsonObject.isNull("superior")) {
                        superiorJson = jsonObject.getJSONObject("superior");
                        superior = User.build(superiorJson);
                        Log.d(TAG, " superior:" + superior.toString());
                    }

                    user = User.build(job, superior, userJson);
                    Log.d(TAG, "user:" + user.toString());

                    // Try to store user
                    MyDBManager myDBManager = new MyDBManager(mActivity);
                    long isUserStored = myDBManager.addUser(user);
                    if (isUserStored >= 0) Log.e(TAG, "User stored");

                    saveUserPrefs(userJson, jobJson, superiorJson, user);

                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    throw new JSONException("success hit false!");
                }

            } catch (JSONException e) {
                Snackbar.make(findViewById(R.id.email_sign_in_button), R.string.login_locked_message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDLogin.isShowing()){
                mPDLogin.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            if (mPDLogin.isShowing()){
                mPDLogin.dismiss();
            }
        }
    }

    private void saveUserPrefs(JSONObject userJson, JSONObject jobJson, JSONObject superiorJson, User user) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Constants.USER_PREFERENCES, userJson.toString());
        if (jobJson != null) editor.putString(Constants.JOB_PREFERENCES, jobJson.toString());
        if (superiorJson != null) editor.putString(Constants.SUPERIOR_PREFERENCES, superiorJson.toString());
        editor.putBoolean(Constants.IS_LOGGED,true);
        editor.putInt(Constants.LOGGED_USER_ID, user.getId());
        editor.putString(Constants.LOGGED_USER_EMAIL, user.getEmail());
        editor.putString(Constants.LOGGED_USER_JOB_POSITION, user.getPosition());
        if (editor.commit()){
            Log.d(TAG,"User json in prefs:" + mPrefs.getString(Constants.USER_PREFERENCES, "NOT FOUND"));
            Log.d(TAG,"Job json in prefs:" + mPrefs.getString(Constants.JOB_PREFERENCES, "NOT FOUND"));
            Log.d(TAG,"Superior json in prefs:" + mPrefs.getString(Constants.SUPERIOR_PREFERENCES, "NOT FOUND"));
            Log.d(TAG,"User logged in prefs:" + mPrefs.getBoolean(Constants.IS_LOGGED,false));
            Log.d(TAG,"User email prefs:" + mPrefs.getString(Constants.LOGGED_USER_EMAIL,""));
            Log.d(TAG,"User job position prefs:" + mPrefs.getString(Constants.LOGGED_USER_JOB_POSITION,""));
            Log.d(TAG,"User id prefs:" + mPrefs.getInt(Constants.LOGGED_USER_ID,-1));
        } else {
            Log.d(TAG,"Could not save prefs!");
        }
    }
}

