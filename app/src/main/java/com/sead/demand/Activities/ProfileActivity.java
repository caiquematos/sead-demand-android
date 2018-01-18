package com.sead.demand.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;

    private String TAG = getClass().getSimpleName();
    private SharedPreferences mPrefs;
    private ImageView mProfileIV;
    private ImageView mLockIV;
    private ImageView mAddActivityIV;
    private TextView mActivitiesTV;
    private TextView mJobTV;
    private String mCurrentPhotoPath;
    private ProgressDialog mProgressDialogUser;
    private StatusTask mStatusTask;
    private User me;
    private User mUser;

    private ProfileActivity mActivity;

    public ProfileActivity() {
        this.mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPrefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        me = CommonUtils.getCurrentUserPreference(this);
        if (me == null) finish();
        // TODO: Make this user above be the one who was selected.

        this.mProgressDialogUser = new ProgressDialog(this);

        this.setTitle(me.getName());

        TextView email = (TextView) findViewById(R.id.profile_email);
        email.setText(me.getEmail());

        TextView position = (TextView) findViewById(R.id.profile_position);
        position.setText(me.getPosition());

        mProfileIV = (ImageView) findViewById(R.id.profile_image);
        loadImage(me.getId());

        loadSuperior(me.getSuperior());

        mJobTV = (TextView) findViewById(R.id.profile_job);

        mActivitiesTV = (TextView) findViewById(R.id.profile_demand_type);

        mLockIV = (ImageView) findViewById(R.id.profile_lock);

        mAddActivityIV = (ImageView) findViewById(R.id.profile_add_demand_types);

        mProfileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               buildDialog();
            }
        });

        mLockIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus();
            }
        });

        mAddActivityIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DemandTypeActivity.class);
                intent.putExtra("user", me);
                startActivity(intent);
            }
        });

        showUserStatus(me.getStatus());
    }

    private void changeStatus() {
        String buttonName;
        String message;
        final String status;
        if(me.getStatus().equals(Constants.YES)) {
            buttonName = "Bloquear";
            message = "Bloqueando esse usuário, ele perderá o acesso ao aplicativo";
            status = Constants.NO;
        } else{
            buttonName = "Desbloquear";
            message = "Desbloqueando esse usuário, ele terá acesso ao aplicativo";
            status = Constants.YES;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptToSetUserStatus(me, me, status);
            }
        });

        alert.setTitle(me.getName());
        alert.setMessage(message);
        alert.show();
    }

    private void dialogResult(int which) {
        switch (which) {
            case 0:
                pickPicture();
                break;
            case 1:
                takePicture();
                break;
        }
    }

    private void buildDialog() {
        AlertDialog.Builder mPriorDialog = new AlertDialog.Builder(mActivity);
        mPriorDialog.setTitle("Selecionar foto");
        mPriorDialog.setItems(getResources().getStringArray(R.array.array_pic_selection), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogResult(which);
            }
        });
        mPriorDialog.create();
        mPriorDialog.show();
    }

    private void loadImage(int id) {
    }

    private void loadSuperior(int id) {
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void pickPicture() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        getIntent.putExtra("crop", "true");
        getIntent.putExtra("scale", true);
        getIntent.putExtra("outputX", 30);
        getIntent.putExtra("outputY", 30);
        getIntent.putExtra("aspectX", 1);
        getIntent.putExtra("aspectY", 1);
        getIntent.putExtra("return-data", true);

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        pickIntent.putExtra("crop", "true");
        pickIntent.putExtra("scale", true);
        pickIntent.putExtra("outputX", 30);
        pickIntent.putExtra("outputY", 30);
        pickIntent.putExtra("aspectX", 1);
        pickIntent.putExtra("aspectY", 1);
        pickIntent.putExtra("return-data", true);

        Intent chooserIntent = Intent.createChooser(getIntent, "Selecionar Imagem");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mProfileIV.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                mProfileIV.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Something wrong!!!");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "path:" + mCurrentPhotoPath);
        //saveImagePath(mCurrentPhotoPath);
        return image;
    }

    private void saveImagePath(String mCurrentPhotoPath) {
        // TODO: Save path in preferences.
    }

    private void attemptToSetUserStatus(User me, User user, String status) {
        if(mStatusTask == null && CommonUtils.isOnline(this)){
            mStatusTask = new StatusTask(me, user, status);
            mStatusTask.execute();
        } else {
            Snackbar.make(mLockIV, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }


    public class StatusTask extends AsyncTask<Void, Void, String> {
        private User user;
        private User me;
        private String status;

        public StatusTask(User me, User user, String status) {
            this.user = user;
            this.me = me;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialogUser.setMessage("Por favor aguarde...");
            mProgressDialogUser.setCancelable(false);
            mProgressDialogUser.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("superior", me.getId());
            values.put("user", user.getId());
            values.put("status", status);
            return CommonUtils.POST("/user/set-status", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;
            JSONObject jsonObject;
            JSONObject jsonUser;
            User user;
            boolean success;

            Log.e(TAG, jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                jsonUser = jsonObject.getJSONObject("user");
                user = User.build(jsonUser);

                String message;

                if (success) {
                    switch(user.getStatus()){
                        case Constants.YES:
                            message = "Usuário desbloqueado com sucesso.";
                            break;
                        case Constants.NO:
                            message = "Usuário bloqueado com sucesso.";
                            break;
                        default:
                            message = mActivity.getString(R.string.server_error);
                    }
                    Snackbar.make(mLockIV, message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.e(TAG, "status after server:" + user.getStatus());
                    showUserStatus(user.getStatus());
                    // Saving new status in db.
                    CommonUtils.updateColumnUserDB(
                            FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS,
                            user.getStatus(),
                            user,
                            mActivity
                    );
                    // (Only for testing) Saving new status in SharedPreferences TODO: Eliminate this.
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(Constants.USER_PREFERENCES, jsonUser.toString());
                    if (editor.commit()){
                        Log.d(TAG,"User json in prefs:" + mPrefs.getString(Constants.USER_PREFERENCES, "NOT FOUND"));
                    } else {
                        Log.d(TAG,"Could not save prefs!");
                    }
                } else {
                    throw new JSONException("success hit false");
                }

            } catch (JSONException e) {
                Snackbar.make(mLockIV, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }


            if (mProgressDialogUser.isShowing()){
                mProgressDialogUser.dismiss();
            }
        }
    }

    private void showUserStatus(String status) {
        int drawable = -1;
        int color = -1;
        switch (status) {
            case Constants.YES:
                drawable = R.drawable.ic_lock_open_black_24dp;
                color = ContextCompat.getColor(mActivity,R.color.secondary_text);
                break;
            case Constants.NO:
                drawable = R.drawable.ic_lock_outline_black_24dp;
                color = ContextCompat.getColor(mActivity,R.color.transred);
                break;
        }
        mLockIV.setImageResource(drawable);
        mLockIV.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}
