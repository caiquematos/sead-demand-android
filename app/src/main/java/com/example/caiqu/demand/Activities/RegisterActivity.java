package com.example.caiqu.demand.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private final String TAG = getClass().getSimpleName();
    private RegisterActivity mActivity;
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
    private EditText mConfirmPasswordView;
    private Spinner mPositionView;
    private Spinner mSuperiorView;
    private EditText mNameView;
    private Button mEmailSignInButton;
    private ProgressDialog mPDRegister;
    private SharedPreferences mPrefs;
    private String mToken;

    public RegisterActivity() {
        this.mActivity = this;
    }

    private ArrayList<String> mSuperiorsEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar();

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mPDRegister = new ProgressDialog(mActivity);

        //Generate FCM Token and save it on Shared Preferences
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "My Token:" + token);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(Constants.IS_LOGGED,true);
        editor.putString(Constants.GCM_TOKEN, token);
        if (editor.commit()) {
            mToken = mPrefs.getString(Constants.GCM_TOKEN, "");
            Log.d(TAG, "SHAREDPREF Token:" + mToken);
        } else {
            Log.d(TAG, "SHAREDPREF FALSE");
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.register_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);
        mNameView = (EditText) findViewById(R.id.register_name);
        mPositionView = (Spinner) findViewById(R.id.register_position_spinner);
        mSuperiorView = (Spinner) findViewById(R.id.register_superior_spinner);

        //Populate Position Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Constants.JOB_POSITIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPositionView.setAdapter(adapter);

        mPositionView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int superior = position + 1; //This will make it retrieve the superior position

                if(superior != Constants.JOB_POSITIONS.length)
                    new FetchSuperiorTask(Constants.JOB_POSITIONS[superior]).execute();
                else
                    Snackbar.make(mSuperiorView, "Esta posição ainda não possui superior", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

    }

    //Handle post request for superior array
    private class FetchSuperiorTask extends AsyncTask<Void, Void, String>{
        String mJobPosition;

        public FetchSuperiorTask(String jobPosition) {
            this.mJobPosition = jobPosition;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDRegister.setMessage("Buscando superiores...");
            mPDRegister.setCancelable(false);
            mPDRegister.show();
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

            JSONObject jsonObject = null;
            JSONArray jsonArray = null;
            boolean success = false;

            Log.d("ON POST EXECUTE LOGIN", "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonArray = jsonObject.getJSONArray("employees");
            } catch (JSONException e) {
                Snackbar.make(mPositionView, "Server Problem", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if(success){
                mSuperiorsEmails = new ArrayList<>();
                List<String> spinnerArray =  new ArrayList<>();
                for(int i=0; i < jsonArray.length(); i++){
                    try {
                        JSONObject json = jsonArray.getJSONObject(i);
                        spinnerArray.add(json.getString("name"));
                        mSuperiorsEmails.add(json.getString("email"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                        android.R.layout.simple_spinner_item, spinnerArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSuperiorView.setAdapter(adapter);

                if (spinnerArray.isEmpty())
                    Snackbar.make(mPositionView, "Esta ainda posição não possui superior", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }else{
                Snackbar.make(mPositionView, "Server Problem", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            if (mPDRegister.isShowing()){
                mPDRegister.dismiss();
            }
        }
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
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String superiorEmail;
        boolean cancel = false;
        View focusView = null;


        if( mSuperiorsEmails == null){
            Snackbar.make(mPositionView, "Escolha uma posição que possua superiores", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            focusView = mSuperiorView;
            cancel = true;
            superiorEmail = "";
        }else if( mSuperiorsEmails.isEmpty()){
            superiorEmail = "";
            ((TextView)mSuperiorView.getChildAt(0)).setError("This field is required");
        } else {
            superiorEmail = mSuperiorsEmails.get(mSuperiorView.getSelectedItemPosition());
        }

        // Store values at the time of the login attempt.
        User user = new User(
                mEmailView.getText().toString(),
                mPasswordView.getText().toString(),
                mNameView.getText().toString(),
                superiorEmail,
                mPositionView.getSelectedItem().toString(),
                mToken
                );

        if (!isNameValid(user.getName())){
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (!isPasswordValid(user.getPassword())) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!confirmPasswordMatch(user.getPassword(),mConfirmPasswordView.getText().toString())) {
            mConfirmPasswordView.setError("Password doesn't match");
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(user.getEmail())) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(user.getEmail())) {
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
            mAuthTask = new UserLoginTask(user);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isSuperiorValid(String superior) {
        return !superior.isEmpty();
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
                new ArrayAdapter<>(RegisterActivity.this,
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

        private final User mUser;

        UserLoginTask(User user) {
           mUser = user;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDRegister.setMessage("Por favor aguarde");
            mPDRegister.setCancelable(false);
            mPDRegister.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("email", mUser.getEmail());
            values.put("password", mUser.getPassword());
            values.put("name", mUser.getName());
            values.put("superior", mUser.getSuperior());
            values.put("position", mUser.getPosition());
            values.put("gcm", mUser.getGcm());
            return CommonUtils.POST("/user/register/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            mAuthTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e("ON POST EXECUTE REG", jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Snackbar.make(mPositionView, "Server Problem", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (success) {
                if (mPDRegister.isShowing()){
                    mPDRegister.dismiss();
                }
                Snackbar.make(mEmailSignInButton, "Registro realizado com sucesso", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(getApplication(), LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (mPDRegister.isShowing()){
                    mPDRegister.dismiss();
                }
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            if (mPDRegister.isShowing()){
                mPDRegister.dismiss();
            }
        }
    }
}