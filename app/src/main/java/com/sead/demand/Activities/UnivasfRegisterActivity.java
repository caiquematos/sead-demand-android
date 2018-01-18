package com.sead.demand.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Spinner;

import com.sead.demand.R;

public class UnivasfRegisterActivity extends AppCompatActivity {
    private UnivasfRegisterActivity mActivity;

    private EditText mName;
    private EditText mEmail;
    private Spinner mDepartment;
    private Spinner mJob;
    private EditText mPassword;
    private EditText mConfirmPassword;

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

        mName = (EditText) findViewById(R.id.register_univasf_name);
        mEmail = (EditText) findViewById(R.id.register_univasf_email);
        mDepartment = (Spinner) findViewById(R.id.register_univasf_sector_spinner);
        mJob = (Spinner) findViewById(R.id.register_univasf_job_spinner);
        mPassword = (EditText) findViewById(R.id.register_univasf_password);
        mConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
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

}
