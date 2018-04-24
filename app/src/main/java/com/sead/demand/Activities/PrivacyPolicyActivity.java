package com.sead.demand.Activities;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sead.demand.R;

public class PrivacyPolicyActivity extends AppCompatActivity {
    public String TAG = getClass().getSimpleName();
    private CheckBox mAcceptCB;
    private Button mContinueBT;
    private boolean mIsAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        mIsAccepted = false;
        mAcceptCB = findViewById(R.id.privacy_policy_accept_cb);
        mContinueBT = findViewById(R.id.privacy_policy_continue_bt);

        mAcceptCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsAccepted = b;
            }
        });

        mContinueBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsAccepted) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Snackbar.make(mContinueBT, "Para continuar, você deve aceitar nossa Política de Privacidade", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
