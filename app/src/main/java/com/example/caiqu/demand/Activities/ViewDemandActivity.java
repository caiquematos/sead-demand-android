package com.example.caiqu.demand.Activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewDemandActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private String mSeen;
    private TextView mSubjectTV;
    private TextView mImportanceTV;
    private TextView mStatusTV;
    private TextView mSenderTV;
    private TextView mReceiverTV;
    private TextView mTimeTV;
    private TextView mDescriptionTV;
    private View mScrollView;
    private StatusTask mStatusTask;
    private SeenTask mSeenTask;
    private ProgressDialog mPDDemand;
    private FloatingActionButton mFabYes;
    private FloatingActionButton mFabLater;
    private FloatingActionButton mFabNo;
    private FloatingActionButton mFabReopen;
    private FloatingActionButton mFabReject;
    private FloatingActionButton mFabResend;
    private FloatingActionButton mFabMenu;
    private TextView mYesTV;
    private TextView mNoTV;
    private TextView mLaterTV;
    private TextView mReopenTV;
    private TextView mRejectTV;
    private TextView mResendTV;
    private int mPage; //identifies which activity called this one
    private boolean mTurned;
    private SendDemandTask mDemandTask;
    private Demand mDemand;
    ViewDemandActivity mActivity;

    public ViewDemandActivity() {
        mActivity = this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubjectTV = (TextView) findViewById(R.id.view_demand_subject);
        mImportanceTV = (TextView) findViewById(R.id.view_demand_importance);
        mStatusTV = (TextView) findViewById(R.id.view_demand_status);
        mSenderTV = (TextView) findViewById(R.id.view_demand_sender);
        mReceiverTV = (TextView) findViewById(R.id.view_demand_receiver);
        mTimeTV = (TextView) findViewById(R.id.view_demand_time);
        mDescriptionTV = (TextView) findViewById(R.id.view_demand_description);
        mPDDemand = new ProgressDialog(mActivity);

        Intent intent = getIntent();
        String importance = intent.getStringExtra("IMPORTANCE");
        setImportanceColor(importance);
        mPage = intent.getIntExtra("PAGE", -1);
        mSeen = intent.getStringExtra("SEEN");

        if ( ( mPage == 1 || mPage == 3) ) markAsSeen(); // On Received demands or Admin demands

        if (mPage == 0) Snackbar.make(mSubjectTV, R.string.send_demand_success, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        mDemand = new Demand(
                intent.getIntExtra("DEMAND",-1),
                intent.getStringExtra("SENDEREMAIL"),
                intent.getStringExtra("RECEIVEREMAIL"),
                intent.getStringExtra("SENDERNAME"),
                intent.getStringExtra("RECEIVERNAME"),
                intent.getStringExtra("IMPORTANCE"),
                intent.getStringExtra("SUBJECT"),
                intent.getStringExtra("DESCRIPTION"),
                intent.getStringExtra("STATUS")
                );

        mSubjectTV.setText(mDemand.getSubject().toUpperCase());
        mImportanceTV.setText(mDemand.getImportance());
        mSenderTV.setText("De: " + mDemand.getFrom());
        mReceiverTV.setText("Para: " + mDemand.getTo());
        mTimeTV.setText(intent.getStringExtra("TIME"));
        mDescriptionTV.setText(mDemand.getDescription());

        showDemandStatus(mDemand.getStatus());
        Log.e(TAG, "Page number: " + mPage + " Status: " + mDemand.getStatus());

        mYesTV = (TextView) findViewById(R.id.tv_yes);
        mFabYes = (FloatingActionButton) findViewById(R.id.fab_yes);
        mFabYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus(Constants.ACCEPT_STATUS);
            }
        }); //Accepted

        mLaterTV = (TextView) findViewById(R.id.tv_later);
        mFabLater = (FloatingActionButton) findViewById(R.id.fab_later);
        mFabLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus(Constants.POSTPONE_STATUS);
            }
        }); //Postponed

        mNoTV = (TextView) findViewById(R.id.tv_no);
        mFabNo = (FloatingActionButton) findViewById(R.id.fab_no);
        mFabNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus(Constants.CANCEL_STATUS);
            }
        }); //Cancelled

        mReopenTV = (TextView) findViewById(R.id.tv_repopen);
        mFabReopen = (FloatingActionButton) findViewById(R.id.fab_reopen);
        mFabReopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus(Constants.REOPEN_STATUS);
            }
        }); //Reopen

        mRejectTV = (TextView) findViewById(R.id.tv_reject);
        mFabReject = (FloatingActionButton) findViewById(R.id.fab_reject);
        mFabReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDemandStatus(Constants.REJECT_STATUS);
            }
        }); //Reject

        mResendTV = (TextView) findViewById(R.id.tv_resend);
        mFabResend = (FloatingActionButton) findViewById(R.id.fab_resend);
        mFabResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CommonUtils.isOnline(getApplicationContext())) attemptSendDemand();
                else  Snackbar.make(mFabResend, R.string.internet_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); //Resend

        mFabMenu = (FloatingActionButton) findViewById(R.id.fab_menu);
        mFabMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTurned) {
                    ViewCompat.animate(mFabMenu).
                            rotation(0f).
                            withLayer().
                            setDuration(1000).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                    mTurned = false;
                    if (mPage == 4) { // if Canceled Activity
                        mFabReopen.hide();
                        mReopenTV.setVisibility(View.INVISIBLE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                        mFabResend.setVisibility(View.GONE);
                        mResendTV.setVisibility(View.GONE);
                    } else if (mPage == 5){ // if Accepted Activity
                        mFabNo.hide();
                        mNoTV.setVisibility(View.INVISIBLE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                        mFabResend.setVisibility(View.GONE);
                        mResendTV.setVisibility(View.GONE);
                    } else if(mPage == 2 // if Received Tab (canceled or rejected demand)
                            && (mDemand.getStatus().equals(Constants.CANCEL_STATUS)
                                || mDemand.getStatus().equals(Constants.REJECT_STATUS))) {
                        mFabResend.hide();
                        mResendTV.setVisibility(View.INVISIBLE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                    } else {
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                        mFabResend.setVisibility(View.GONE);
                        mResendTV.setVisibility(View.GONE);
                        mFabReject.hide();
                        mRejectTV.setVisibility(View.INVISIBLE);
                        mFabLater.hide();
                        mLaterTV.setVisibility(View.INVISIBLE);
                        mFabYes.hide();
                        mYesTV.setVisibility(View.INVISIBLE);
                    }
                } else {
                    ViewCompat.animate(mFabMenu).
                            rotation(135f).
                            withLayer().
                            setDuration(1000).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                    mTurned = true;
                    if (mPage == 4) { // if Canceled Activity
                        mFabReopen.show();
                        mReopenTV.setVisibility(View.VISIBLE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                    } else if (mPage == 5){ // if Accepted Activity
                        mFabNo.show();
                        mNoTV.setVisibility(View.VISIBLE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                    } else if(mPage == 2
                            && (mDemand.getStatus().equals(Constants.CANCEL_STATUS)
                                || mDemand.getStatus().equals(Constants.REJECT_STATUS))) { // if Received Tab (canceled or rejected demand)
                        mFabResend.show();
                        mResendTV.setVisibility(View.VISIBLE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                    } else {
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                        mFabResend.setVisibility(View.GONE);
                        mResendTV.setVisibility(View.GONE);
                        mFabYes.show();
                        mYesTV.setVisibility(View.VISIBLE);
                        mFabLater.show();
                        mLaterTV.setVisibility(View.VISIBLE);
                        mFabReject.show();
                        mRejectTV.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (mPage == -1
                || mPage == 1
                || mPage == 0
                || (mPage == 2
                    && !(mDemand.getStatus().equals(Constants.CANCEL_STATUS)
                        || mDemand.getStatus().equals(Constants.REJECT_STATUS)))
                || mPage == 6){
            mFabMenu.hide();
            mFabReject.hide();
            mFabNo.hide();
            mFabLater.hide();
            mFabYes.hide();
            mFabReopen.hide();
            mFabResend.hide();
        }else{
            mScrollView = findViewById(R.id.view_demand_scroll_view);
            mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(scrollY > 0){
                        if(mTurned){
                            ViewCompat.animate(mFabMenu).
                                    rotation(0f).
                                    withLayer().
                                    setDuration(800).
                                    setInterpolator(new OvershootInterpolator()).
                                    setListener(new ViewPropertyAnimatorListener() {
                                        @Override
                                        public void onAnimationStart(View view) {
                                            if (mPage == 4) { // if Canceled Activity
                                                mFabReopen.hide();
                                                mReopenTV.setVisibility(View.INVISIBLE);
                                                mFabReject.setVisibility(View.GONE);
                                                mRejectTV.setVisibility(View.GONE);
                                                mFabLater.setVisibility(View.GONE);
                                                mLaterTV.setVisibility(View.GONE);
                                                mFabYes.setVisibility(View.GONE);
                                                mYesTV.setVisibility(View.GONE);
                                                mFabNo.setVisibility(View.GONE);
                                                mNoTV.setVisibility(View.GONE);
                                            } else if (mPage == 5){ // if Accepted Activity
                                                mFabNo.hide();
                                                mNoTV.setVisibility(View.INVISIBLE);
                                                mFabReject.setVisibility(View.GONE);
                                                mRejectTV.setVisibility(View.GONE);
                                                mFabLater.setVisibility(View.GONE);
                                                mLaterTV.setVisibility(View.GONE);
                                                mFabYes.setVisibility(View.GONE);
                                                mYesTV.setVisibility(View.GONE);
                                                mFabReopen.setVisibility(View.GONE);
                                                mReopenTV.setVisibility(View.GONE);
                                            } else if(mPage == 2
                                                    && (mDemand.getStatus().equals(Constants.CANCEL_STATUS)
                                                        || mDemand.getStatus().equals(Constants.REJECT_STATUS))) { // if Received Tab (canceled demand)
                                                mFabResend.hide();
                                                mResendTV.setVisibility(View.INVISIBLE);
                                                mFabNo.setVisibility(View.GONE);
                                                mNoTV.setVisibility(View.GONE);
                                                mFabReject.setVisibility(View.GONE);
                                                mRejectTV.setVisibility(View.GONE);
                                                mFabLater.setVisibility(View.GONE);
                                                mLaterTV.setVisibility(View.GONE);
                                                mFabYes.setVisibility(View.GONE);
                                                mYesTV.setVisibility(View.GONE);
                                                mFabReopen.setVisibility(View.GONE);
                                                mReopenTV.setVisibility(View.GONE);
                                            } else {
                                                mFabReject.hide();
                                                mRejectTV.setVisibility(View.INVISIBLE);
                                                mFabLater.hide();
                                                mLaterTV.setVisibility(View.INVISIBLE);
                                                mFabYes.hide();
                                                mYesTV.setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onAnimationEnd(View view) {
                                            mTurned = false;
                                            mFabMenu.hide();
                                        }

                                        @Override
                                        public void onAnimationCancel(View view) {
                                            mTurned = true;
                                        }
                                    }).
                                    start();
                        }else {
                            mFabMenu.hide();
                        }
                    } else{
                        mFabMenu.show();
                    }
                }
            });
        }
    }

    private void attemptSendDemand(){
        boolean cancel = false;

        if (mDemandTask != null) cancel = true;

        if (cancel){

        } else {
            mDemandTask = new SendDemandTask(mDemand);
            mDemandTask.execute((Void) null);
        }
    }

    private class SendDemandTask extends  AsyncTask<Void, Void, String>{
        private final Demand demand;

        public SendDemandTask(Demand demand) {
            this.demand = demand;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Por favor aguarde.");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("id", demand.getId());
            return CommonUtils.POST("/demand/resend/", values);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mDemandTask = null;

            JSONObject jsonObject;
            JSONObject demandJson;
            Demand demandResponse = null;
            boolean success = false;

            Log.d("ON POST EXECUTE DEMAND", "string json: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                demandJson = jsonObject.getJSONObject("demand");

                Log.d("ON POST EXECUTE DEMAND", "string json demand: " + demandJson);
                demandResponse = new Demand(demandJson);
            } catch (JSONException e) {
                Snackbar.make(mFabResend, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

            if (success) {
                Intent intent = new Intent(mActivity, ViewDemandActivity.class);
                intent.putExtra("ACTIVITY", mActivity.getClass().getSimpleName());
                intent.putExtra("PAGE", mPage);
                intent.putExtra("DEMAND", demandResponse.getId());
                intent.putExtra("SUBJECT", demandResponse.getSubject());
                intent.putExtra("STATUS", demandResponse.getStatus());
                intent.putExtra("SENDERNAME", demandResponse.getFrom());
                intent.putExtra("SEEN", demandResponse.getSeen());
                intent.putExtra("DESCRIPTION", demandResponse.getDescription());
                intent.putExtra("TIME", CommonUtils.formatDate(demandResponse.getCreatedAt()));
                intent.putExtra("IMPORTANCE", demandResponse.getImportance());
                intent.putExtra("RECEIVERNAME", demandResponse.getTo());
                Log.d("ON VIEW HOLDER", demandResponse.getSubject() + " Importance:" + demandResponse.getImportance()
                        + " PACKAGE:"  + mActivity.getClass().getSimpleName() + " Page:" + mPage);
                finish();
                mActivity.startActivity(intent);
            } else {
                Snackbar.make(mFabResend, R.string.send_demand_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void showDemandStatus(String status) {
        if (status.equals("A"))
            mStatusTV.setText("Essa demanda foi Aceita");
        else if (status.equals("C"))
            mStatusTV.setText("Essa demanda foi Cancelada");
        else if (status.equals("P"))
            mStatusTV.setText("Essa demanda foi Adiada");
        else if (status.equals("R"))
            mStatusTV.setText("Essa demanda foi Reaberta");
        else if (status.equals("X"))
            mStatusTV.setText("Essa demanda foi Rejeitada");
        else
            mStatusTV.setText("Essa demanda ainda não foi avaliada");
    }

    private void markAsSeen(){
        Log.e("ON SEEN DEMAND", "on seen bfore if");
        if (mSeenTask == null){
            mSeenTask = new SeenTask();
            mSeenTask.execute();
            Log.e("ON SEEN DEMAND", "on seen after if");
        }
    }

    private void setDemandStatus(String status){
        if (mStatusTask == null && CommonUtils.isOnline(mActivity)){
            mStatusTask = new StatusTask(mDemand.getId(), status);
            mStatusTask.execute();
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setImportanceColor(String importance) {
        int color = ContextCompat.getColor(this,R.color.dGreen);
        if (importance.equals("Urgente")) color = ContextCompat.getColor(this,R.color.darkred);
        if (importance.equals("Importante")) color = ContextCompat.getColor(this,R.color.dyellow);
        mImportanceTV.getCompoundDrawables()[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public class SeenTask extends  AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", mDemand.getId());
            return CommonUtils.POST("/demand/mark-as-read/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mSeenTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e("ON SEEN DEMAND", jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) mSeen = "Y"; //TODO: Make this persistent for a while
        }
    }

    public class StatusTask extends AsyncTask<Void, Void, String> {
        private int id;
        private String status;

        public StatusTask(int id, String status) {
            this.id = id;
            this.status = status;
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
            values.put("demand", id);
            values.put("status", status);
            return CommonUtils.POST("/demand/set-status/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e("ON POST EX VIEW DEMAND", jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            String message;

            if (success) {
                switch(status){
                    case "A":
                        message = "Demanda Aceita com Sucesso.";
                        break;
                    case "P":
                        message = "Demanda Adiada com Sucesso.";
                        break;
                    case "C":
                        message = "Demanda Cancelada com Sucesso.";
                        break;
                    case "R":
                        message = "Demanda Reaberta com Sucesso.";
                        break;
                     case "X":
                        message = "Demanda Rejeitada com Sucesso.";
                        break;
                    default:
                        message = "Feito.";
                }
                Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showDemandStatus(status);
            } else {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
        }
    }
}
