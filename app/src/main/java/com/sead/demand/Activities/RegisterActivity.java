package com.sead.demand.Activities;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
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
    private RegistrationTask mAuthTask = null;

    private FetchSuperiorTask mFetchSuperiorTask = null;
    private FetchJobTask mFetchJobTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private Spinner mPositionView;
    private Spinner mSuperiorView;
    private Spinner mJobView;
    private EditText mNameView;
    private Button mEmailSignInButton;
    private ProgressDialog mPDRegister;
    private SharedPreferences mPrefs;
    private String mToken;
    private boolean mIsTopPosition;
    private int mSaveInternetJobPosition; // saves the Job Position when internet connected
    private int mCurrentJobPosition;

    public RegisterActivity() {
        this.mActivity = this;
    }

    private ArrayList<String> mSuperiorsEmails;
    private ArrayList<Job> mJobArray;
    private boolean hasJobRetrieverFinished = false;
    private boolean hasSuperiorRetrieverFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar();

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        mPDRegister = new ProgressDialog(mActivity);

        /*
        mToken = mPrefs.getString(Constants.GCM_TOKEN, "");
        Log.d(TAG, "SHAREDPREF Token:" + mToken);
        */

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);
        mNameView = (EditText) findViewById(R.id.register_name);
        mPositionView = (Spinner) findViewById(R.id.register_position_spinner);
        mSuperiorView = (Spinner) findViewById(R.id.register_superior_spinner);
        mJobView = (Spinner) findViewById(R.id.register_job_spinner);

        //Populate Position Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Constants.JOB_POSITIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPositionView.setAdapter(adapter);

        mPositionView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPDRegister.setMessage("Buscando superiores e cargos...");
                mPDRegister.setCancelable(false);
                mPDRegister.show();
                attemptToGetSuperiors(position);
                attemptToGetJobs(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CommonUtils.isOnline(mActivity)){
                    attemptRegister();
                }
                else {
                    Snackbar.make(mEmailSignInButton, R.string.internet_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

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

    private void attemptToGetSuperiors(int position){
        mCurrentJobPosition = position;

        int superior = position + 1; //This will make it retrieve the superior position

        if(CommonUtils.isOnline(mActivity)) mSaveInternetJobPosition = position;

        Log.e(TAG,"PositionInt:" + mSaveInternetJobPosition + " PositionNot:" + mCurrentJobPosition);

        if(superior != Constants.JOB_POSITIONS.length){
            mIsTopPosition = false;
            mSuperiorView.setEnabled(true);
            Log.e(TAG, Constants.JOB_POSITIONS[superior]);
            if(CommonUtils.isOnline(mActivity)){
                if(mFetchSuperiorTask == null)
                    new FetchSuperiorTask(Constants.JOB_POSITIONS[superior]).execute();
                else return;
            }
            else {
                Snackbar.make(mSuperiorView, R.string.internet_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        else {
            mIsTopPosition = true;
            mSuperiorView.setEnabled(false);
            Snackbar.make(mSuperiorView, R.string.position_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void attemptToGetJobs(int position) {
        if (CommonUtils.isOnline(this)) {
            if (mFetchJobTask == null) {
                new FetchJobTask(Constants.JOB_POSITIONS[position]).execute();
            } else {
                return;
            }
        } else {
            Snackbar.make(mJobView, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    //Handle post request for superior array
    private class FetchSuperiorTask extends AsyncTask<Void, Void, String>{
        String mJobPosition;

        public FetchSuperiorTask(String jobPosition) {
            this.mJobPosition = jobPosition;
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("position", this.mJobPosition);
            String response = CommonUtils.POST("/user/retrieve-superiors-by-position", values);
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);

            mFetchSuperiorTask = null;

            JSONObject jsonObject;
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

            if (mPDRegister.isShowing() && mFetchJobTask == null){
                mPDRegister.dismiss();
            }
        }
    }

    private class FetchJobTask extends AsyncTask<Void, Void, String>{
        private String level; // ponta, coordenador...

        public FetchJobTask(String level) {
            this.level = level;
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("position", this.level);
            String response = CommonUtils.POST("/user/retrieve-jobs-by-position", values);
            return response;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);

            mFetchJobTask = null;

            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                boolean success = jsonObject.getBoolean("success");
                JSONArray jsonArray = jsonObject.getJSONArray("jobs");
                if (success) {
                   fillJobSpinner(jsonArray);
                } else {
                    throw new JSONException("Success hit false");
                }
            } catch (JSONException e) {
                Snackbar.make(mPositionView,R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDRegister.isShowing() && mFetchSuperiorTask == null){
                mPDRegister.dismiss();
            }
        }
    }

    private void fillJobSpinner(JSONArray jsonArray) throws JSONException {
        mJobArray = new ArrayList<>();
        List<String> spinnerArray =  new ArrayList<>();
        for(int i=0; i < jsonArray.length(); i++){
            JSONObject json = jsonArray.getJSONObject(i);
            Job job = Job.build(json);
            spinnerArray.add(job.getTitle());
            mJobArray.add(job);
        }

        if (spinnerArray.isEmpty()) {
            Snackbar.make(mJobView, "Esta posição ainda não possui cargos", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                    android.R.layout.simple_spinner_item, spinnerArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mJobView.setAdapter(adapter);
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

        if (!CommonUtils.isOnline(mActivity)) {
            Snackbar.make(mEmailSignInButton, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        if (mAuthTask != null) {
            return;
        }

        if (mCurrentJobPosition != mSaveInternetJobPosition) {
            attemptToGetSuperiors(mSaveInternetJobPosition);
            Snackbar.make(mPositionView, "Recarregando superiores... Tente registrar agora.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String superiorEmail;
        boolean cancel = false;
        View focusView = null;

        // set superior's email address.
        if (mIsTopPosition) {
            superiorEmail = mEmailView.getText().toString();
        } else {
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
        }

        Job job = mJobArray.get(mJobView.getSelectedItemPosition());
        Log.d(TAG, "job: " + job.toString());

        // Store values at the time of the login attempt.
        User user = new User(
                mEmailView.getText().toString(),
                mPasswordView.getText().toString(),
                mNameView.getText().toString(),
                superiorEmail,
                mPositionView.getSelectedItem().toString(),
                FirebaseInstanceId.getInstance().getToken(),
                job.getId(),
                Constants.INTERNAL_USER
                );

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

        if (!isNameValid(user.getName())){
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new RegistrationTask(user);
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
    public class RegistrationTask extends AsyncTask<Void, Void, String> {

        private final User mUser;

        RegistrationTask(User user) {
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
            values.put("superior", mUser.getSuperiorEmail());
            values.put("position", mUser.getPosition());
            values.put("gcm", mUser.getGcm());
            values.put("job", mUser.getJobId());
            values.put("type", mUser.getType());
            return CommonUtils.POST("/user/register", values);
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
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            } catch (JSONException e) {
                Snackbar.make(mPositionView, "Server Problem", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDRegister.isShowing()){
                mPDRegister.dismiss();
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