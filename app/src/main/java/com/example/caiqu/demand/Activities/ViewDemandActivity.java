package com.example.caiqu.demand.Activities;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.Handlers.AlarmReceiver;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewDemandActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private TextView mSubjectTV;
    private TextView mImportanceTV;
    private TextView mStatusTV;
    private TextView mSenderTV;
    private TextView mReceiverTV;
    private TextView mTimeTV;
    private TextView mDescriptionTV;
    private View mScrollView;
    private StatusTask mStatusTask;
    private ProgressDialog mPDDemand;
    private FloatingActionButton mFabYes;
    private FloatingActionButton mFabLater;
    private FloatingActionButton mFabNo;
    private FloatingActionButton mFabReopen;
    private FloatingActionButton mFabReject;
    private FloatingActionButton mFabResend;
    private FloatingActionButton mFabMenu;
    private FloatingActionButton mFabDone;
    private TextView mYesTV;
    private TextView mNoTV;
    private TextView mLaterTV;
    private TextView mReopenTV;
    private TextView mRejectTV;
    private TextView mResendTV;
    private TextView mDoneTV;
    private int mPage; // Identifies which activity called this one.
    private int mMenuType; // Identifies which type of menu to be shown.
    private boolean mTurned;
    private SendDemandTask mDemandTask;
    private Demand mDemand;
    private AlertDialog.Builder mAlert;
    private String mAlertType;
    private ImportanceTask mImportanceTask;
    private AlertDialog.Builder mImportanceDialog;
    private AlertDialog.Builder mPostponeDialog;
    ViewDemandActivity mActivity;

    public ViewDemandActivity() {
        mActivity = this;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Before any change, get intent data.

        Intent intent = getIntent();
        mDemand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
        mPage = intent.getIntExtra(Constants.INTENT_PAGE, -1);
        mMenuType = intent.getIntExtra(Constants.INTENT_MENU, -1);
        Log.e(TAG, "Demand intent:" + mDemand.toString());
        Log.e(TAG, "Menu number: " + mMenuType);
        Log.e(TAG, "Page number: " + mPage);

        // Get object references.

        mSubjectTV = (TextView) findViewById(R.id.view_demand_subject);
        mImportanceTV = (TextView) findViewById(R.id.view_demand_importance);
        mStatusTV = (TextView) findViewById(R.id.view_demand_status);
        mSenderTV = (TextView) findViewById(R.id.view_demand_sender);
        mReceiverTV = (TextView) findViewById(R.id.view_demand_receiver);
        mTimeTV = (TextView) findViewById(R.id.view_demand_time);
        mDescriptionTV = (TextView) findViewById(R.id.view_demand_description);
        mPDDemand = new ProgressDialog(mActivity);

        // Finally set changes.

        setImportanceColor(mDemand.getImportance());

        mImportanceTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImportanceDialog = new AlertDialog.Builder(mActivity);
                mImportanceDialog.setTitle("Mudar para:");
                mImportanceDialog.setItems(Constants.DEMAND_IMPORTANCE, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDemandImportance(Constants.DEMAND_IMPORTANCE[which]);
                    }
                });
                mImportanceDialog.create();
                mImportanceDialog.show();
            }
        });
        if (mPage == Constants.ADMIN_PAGE) mImportanceTV.setClickable(true);
        else mImportanceTV.setClickable(false);

        if (mPage == Constants.CREATE_PAGE)
            Snackbar.make(mSubjectTV, R.string.send_demand_success, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        mSubjectTV.setText(mDemand.getSubject().toUpperCase());
        mImportanceTV.setText(mDemand.getImportance());
        mSenderTV.setText("De: " + mDemand.getSender().getName());
        mReceiverTV.setText("Para: " + mDemand.getReceiver().getName());
        mTimeTV.setText(CommonUtils.formatDate(mDemand.getCreatedAt()));
        mDescriptionTV.setText(mDemand.getDescription()+ "\n\n\n\n");

        showDemandStatus(mDemand.getStatus());

        mAlert = new AlertDialog.Builder(this);
        mAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAlertType.equals(Constants.RESEND_STATUS)) {
                    attemptSendDemand();
                } else {
                    setDemandStatus(mAlertType);
                }
            }
        });
        mAlert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        mYesTV = (TextView) findViewById(R.id.tv_yes);
        mFabYes = (FloatingActionButton) findViewById(R.id.fab_yes);
        mFabYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Aceitar a demanda?");
                mAlertType = Constants.ACCEPT_STATUS;
                mAlert.show();
            }
        }); //Accepted

        mLaterTV = (TextView) findViewById(R.id.tv_later);
        mFabLater = (FloatingActionButton) findViewById(R.id.fab_later);
        mFabLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostponeDialog = new AlertDialog.Builder(mActivity);
                mPostponeDialog.setTitle("Adiar para:");
                String[] postponeOptions = {
                        Constants.POSTPONE_OPTIONS[0] + " dia",
                        Constants.POSTPONE_OPTIONS[1] + " dias",
                        Constants.POSTPONE_OPTIONS[2] + " dias",
                        Constants.POSTPONE_OPTIONS[3] + " dias"
                };
               // mPostponeDialog.setMessage("Você receberá uma notificação te relembrando que precisa avaliar essa demanda.");
                mPostponeDialog.setItems(postponeOptions, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPostponeTime(Constants.POSTPONE_OPTIONS[which]);
                    }
                });
                mPostponeDialog.create();
                mPostponeDialog.show();
                /*
                mAlert.setTitle("Adiar a demanda?");
                mAlertType = Constants.POSTPONE_STATUS;
                mAlert.show();
                */
            }
        }); //Postponed

        mNoTV = (TextView) findViewById(R.id.tv_no);
        mFabNo = (FloatingActionButton) findViewById(R.id.fab_no);
        mFabNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Cancelar a demanda?");
                mAlertType = Constants.CANCEL_STATUS;
                mAlert.show();
            }
        }); //Cancelled

        mReopenTV = (TextView) findViewById(R.id.tv_repopen);
        mFabReopen = (FloatingActionButton) findViewById(R.id.fab_reopen);
        mFabReopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Reabrir a demanda?");
                mAlert.setMessage("Quando uma demanda é reaberta, ela é movida novamente para a aba Admin e seu status é configurado de volta para indefinido.");
                mAlertType = Constants.REOPEN_STATUS;
                mAlert.show();
            }
        }); //Reopen

        mRejectTV = (TextView) findViewById(R.id.tv_reject);
        mFabReject = (FloatingActionButton) findViewById(R.id.fab_reject);
        mFabReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Rejeitar a demanda?");
                mAlertType = Constants.REJECT_STATUS;
                mAlert.show();
            }
        }); //Reject

        mResendTV = (TextView) findViewById(R.id.tv_resend);
        mFabResend = (FloatingActionButton) findViewById(R.id.fab_resend);
        mFabResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Reenviar a demanda?");
                mAlertType = Constants.RESEND_STATUS;
                mAlert.show();
            }
        }); //Resend

        mDoneTV = (TextView) findViewById(R.id.tv_done);
        mFabDone = (FloatingActionButton) findViewById(R.id.fab_done);
        mFabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Concluir demanda?");
                mAlertType = Constants.DONE_STATUS;
                mAlert.show();
            }
        }); // Done.

        mFabMenu = (FloatingActionButton) findViewById(R.id.fab_menu);
        mFabMenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mTurned) { // When 'close menu' button is hit.
                    Log.e(TAG, "mTurned true. In first if");
                    ViewCompat.animate(mFabMenu).
                            rotation(0f).
                            withLayer().
                            setDuration(1000).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                    mTurned = false;
                    if (mMenuType == Constants.SHOW_DONE_MENU){
                        mFabDone.hide();
                        mDoneTV.setVisibility(View.INVISIBLE);
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
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
                    } else if (mMenuType == Constants.SHOW_REOPEN_MENU) { // Canceled Activity
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
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
                    } else if (mMenuType == Constants.SHOW_CANCEL_MENU){ // Accepted Activity.
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
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
                    } else if(mMenuType == Constants.SHOW_RESEND_MENU) {
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
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
                    } else {
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
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
                } else { // When 'open menu' button is hit.
                    Log.e(TAG, "mTurned false. In first else");
                    ViewCompat.animate(mFabMenu).
                            rotation(135f).
                            withLayer().
                            setDuration(1000).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                    mTurned = true;
                    if (mMenuType == Constants.SHOW_DONE_MENU) {
                        mFabDone.show();
                        mDoneTV.setVisibility(View.VISIBLE);
                        mFabReopen.setVisibility(View.GONE);
                        mReopenTV.setVisibility(View.GONE);
                        mFabReject.setVisibility(View.GONE);
                        mRejectTV.setVisibility(View.GONE);
                        mFabLater.setVisibility(View.GONE);
                        mLaterTV.setVisibility(View.GONE);
                        mFabYes.setVisibility(View.GONE);
                        mYesTV.setVisibility(View.GONE);
                        mFabNo.setVisibility(View.GONE);
                        mNoTV.setVisibility(View.GONE);
                    } else if (mMenuType == Constants.SHOW_REOPEN_MENU) { // Canceled Activity.
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
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
                    } else if (mMenuType == Constants.SHOW_CANCEL_MENU){ // Accepted Activity.
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
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
                    } else if(mMenuType == Constants.SHOW_RESEND_MENU) {
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
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
                    } else {
                        mFabDone.setVisibility(View.GONE);
                        mDoneTV.setVisibility(View.GONE);
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

        // Select which activities has no menu
        if ( mMenuType == Constants.SHOW_NO_MENU || mMenuType == -1 ){
            Log.e(TAG, "In second if");
            mFabMenu.hide();
            mFabDone.hide();
            mFabReject.hide();
            mFabNo.hide();
            mFabLater.hide();
            mFabYes.hide();
            mFabReopen.hide();
            mFabResend.hide();
        }else{ // Hide menu when screen scrolled (for activities with menu)
            Log.e(TAG, "In second else");
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

    private void setDemandImportance(String s) {
        if (mImportanceTask == null && CommonUtils.isOnline(mActivity)){
            mImportanceTask = new ImportanceTask(mDemand.getId(), s);
            mImportanceTask.execute();
        } else {
            Snackbar.make(mImportanceTV, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void showDemandImportance(String importance) {
        mImportanceTV.setText(importance);
        setImportanceColor(importance);
    }

    private void attemptSendDemand(){
        if (mDemandTask == null && CommonUtils.isOnline(mActivity)){
            mDemandTask = new SendDemandTask(mDemand);
            mDemandTask.execute();
        } else {
            Snackbar.make(mFabResend, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void showDemandStatus(String status) {
        int drawable;
        String description;
        int color;

        switch (status){
            case Constants.LATE_STATUS: // Late.
                description = "Essa demanda está atrasada";
                drawable = R.drawable.ic_alarm_off_black_24dp;
                color = ContextCompat.getColor(this,R.color.colorPrimary);
                break;
            case Constants.DONE_STATUS: // Done.
                description = "Essa demanda foi concluída";
                drawable = R.drawable.ic_assignment_turned_in_white_24dp;
                color = ContextCompat.getColor(this,R.color.darkgreen);
                break;
            case Constants.ACCEPT_STATUS: // Accepted.
                description = "Essa demanda foi aceita";
                drawable = R.drawable.ic_check_circle_black_24dp;
                color = ContextCompat.getColor(this,R.color.green);
                break;
            case Constants.REJECT_STATUS: //Rejected
                description = "Essa demanda foi rejeitada";
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(this,R.color.darkred);
                break;
            case Constants.CANCEL_STATUS: //Cancelled
                description = "Essa demanda foi cancelada";
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(this,R.color.red);
                break;
            case Constants.POSTPONE_STATUS: //Postponed
                description = "Essa demanda foi adiada";
                drawable = R.drawable.ic_alarm_black_24dp;
                color = ContextCompat.getColor(this,R.color.darkyellow);
                break;
            case Constants.REOPEN_STATUS: //Reopen
            case Constants.UNDEFINE_STATUS: //Undefined
                description = "Essa demanda ainda não foi avaliada";
                drawable = R.drawable.ic_fiber_manual_record_black_24dp;
                color = ContextCompat.getColor(this,R.color.gray);
                break;
            case Constants.RESEND_STATUS: //Resent
                description = "Essa demanda foi reenviada";
                drawable = R.drawable.ic_send_black_24dp;
                color = ContextCompat.getColor(this,R.color.blue);
                break;
            default:
                description = "Essa demanda ainda não foi avaliada";
                drawable = R.drawable.ic_fiber_manual_record_black_24dp;
                color = ContextCompat.getColor(this,R.color.gray);
        }

        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = getDrawable(drawable);
        } else {
            objDrawable = getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        mStatusTV.setText(description);
        mStatusTV.setCompoundDrawablesWithIntrinsicBounds(null,null, objDrawable,null);
        mStatusTV.getCompoundDrawables()[2].setColorFilter(color, PorterDuff.Mode.SRC_IN);
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

    private void setImportanceColor(String importance) {
        int color = ContextCompat.getColor(this,R.color.dGreen);
        if (importance.equals("Urgente")) color = ContextCompat.getColor(this,R.color.darkred);
        if (importance.equals("Importante")) color = ContextCompat.getColor(this,R.color.dyellow);
        mImportanceTV.getCompoundDrawables()[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPostponeTime(int postponeTime) {
        // Set an alarm notification.
        Intent receiverIntent = new Intent(this, AlarmReceiver.class);
        receiverIntent.putExtra(Constants.ALARM_TYPE_KEY, Constants.POSTPONE_ALARM_TAG);
        receiverIntent.putExtra(Constants.INTENT_DEMAND, mDemand);
        receiverIntent.putExtra(Constants.INTENT_PAGE, mPage);
        receiverIntent.putExtra(Constants.INTENT_MENU, Constants.SHOW_TRIO_MENU);
        PendingIntent alarmSender = PendingIntent.getBroadcast(this, mDemand.getId(), receiverIntent, 0);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, postponeTime); // TODO: Change to Days when ready.
        long timeInMillis = c.getTimeInMillis();
        Log.e(TAG, "Time in millis:" + timeInMillis);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, alarmSender);

        // Change status state on server.
        setDemandStatus(Constants.POSTPONE_STATUS);
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
            JSONObject senderJson;
            JSONObject receiverJson;
            JSONObject demandJson;
            Demand demandResponse = null;
            boolean success = false;

            Log.e(TAG, "Json Response (resend): " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                senderJson = jsonObject.getJSONObject("sender");
                receiverJson = jsonObject.getJSONObject("receiver");
                demandJson = jsonObject.getJSONObject("demand");

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                demandResponse = Demand.build(sender, receiver, demandJson);

                Log.e(TAG,
                        "Json Resend Response:" + demandResponse.toString()
                                + " sender:" + sender.toString()
                                + " receiver:" + receiver.toString()
                );
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
                intent.putExtra(Constants.INTENT_ACTIVITY, mActivity.getClass().getSimpleName());
                intent.putExtra(Constants.INTENT_PAGE, mPage);
                intent.putExtra(Constants.INTENT_DEMAND, demandResponse);
                finish();
                mActivity.startActivity(intent);
            } else {
                Snackbar.make(mFabResend, R.string.send_demand_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private class ImportanceTask extends AsyncTask<Void, Void, String>{
        private int id;
        private String importance;

        public ImportanceTask(int id, String importance) {
            this.id = id;
            this.importance = importance;
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
            values.put("importance", importance);
            return CommonUtils.POST("/demand/set-importance/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mImportanceTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e("ON POST EX VIEW DEMAND", jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Snackbar.make(mImportanceTV, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            String message;

            if (success) {
                message = "Demanda modificada com sucesso";
                showDemandImportance(importance);
            } else {
                message = String.valueOf(R.string.server_error);
            }

            Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
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

            Log.e(TAG, jsonResponse);

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
                    case Constants.ACCEPT_STATUS:
                        message = "Demanda Aceita com Sucesso.";
                        break;
                    case Constants.POSTPONE_STATUS:
                        message = "Demanda Adiada com Sucesso.";
                        break;
                    case Constants.CANCEL_STATUS:
                        message = "Demanda Cancelada com Sucesso.";
                        break;
                    case Constants.REOPEN_STATUS:
                        message = "Demanda Reaberta com Sucesso.";
                        break;
                     case Constants.REJECT_STATUS:
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
