package com.sead.demand.Activities;

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
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.Entities.User;
import com.sead.demand.Handlers.AlarmReceiver;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class ViewDemandActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private TextView mSubjectTV;
    private TextView mPriorTV;
    private TextView mStatusTV;
    private TextView mSenderTV;
    private TextView mReceiverTV;
    private TextView mTimeTV;
    private TextView mDescriptionTV;
    private TextView mReason;
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
    private PriorTask mPriorTask;
    private AlertDialog.Builder mPriorDialog;
    private AlertDialog.Builder mPostponeDialog;
    private boolean mShouldCancelAlarm = false;
    private RejectTask mRejectTask;
    ViewDemandActivity mActivity;
    private User mCurrentUser;

    public ViewDemandActivity() {
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_demand);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = CommonUtils.getCurrentUserPreference(this);

        // Before any change, get intent data.

        Intent intent = getIntent();
        mDemand = (Demand) intent.getSerializableExtra(Constants.INTENT_DEMAND);
        mPage = intent.getIntExtra(Constants.INTENT_PAGE, -1);
        mMenuType = intent.getIntExtra(Constants.INTENT_MENU, -1);
        Log.e(TAG, "Demand intent:" + mDemand.toString());
        Log.e(TAG, "Menu number: " + mMenuType);
        Log.e(TAG, "Page number: " + mPage);

        // Setting activity title.
        String activityTitle;
        switch(mPage) {
            case Constants.ADMIN_PAGE:
                activityTitle = "Demanda (admin)";
                break;
            case Constants.RECEIVED_PAGE:
                activityTitle = "Demanda Recebida";
                break;
            case Constants.SENT_PAGE:
                activityTitle = "Demanda Enviada";
                break;
            case Constants.STATUS_PAGE:
                switch (mDemand.getStatus()){
                    case Constants.DONE_STATUS:
                        activityTitle = "Demanda Concluída (admin)";
                        break;
                    case Constants.ACCEPT_STATUS:
                        activityTitle = "Demanda Deferida (admin)";
                        break;
                    case Constants.POSTPONE_STATUS:
                        activityTitle = "Demanda Adiada (admin)";
                        break;
                    case Constants.CANCEL_STATUS:
                        activityTitle = "Demanda Cancelada (admin)";
                        break;
                    case Constants.REJECT_STATUS:
                        activityTitle = "Demanda Indeferida (admin)";
                        break;
                    // These last two won't be be accessed so far.
                    case Constants.UNDEFINE_STATUS:
                        activityTitle = "Demanda Não Definida (admin)";
                        break;
                    case Constants.LATE_STATUS:
                        activityTitle = "Demanda Atrasada (admin)";
                        break;
                    // These last two.
                    default:
                        activityTitle = "Demanda (admin)";
                }
                break;
            case Constants.CREATE_PAGE:
            default:
                activityTitle = "Demanda";
        }

        setTitle(activityTitle);

        // Get object references.

        mSubjectTV = (TextView) findViewById(R.id.view_demand_subject);
        mPriorTV = (TextView) findViewById(R.id.view_demand_prior);
        mStatusTV = (TextView) findViewById(R.id.view_demand_status);
        mSenderTV = (TextView) findViewById(R.id.view_demand_sender);
        mReceiverTV = (TextView) findViewById(R.id.view_demand_receiver);
        mTimeTV = (TextView) findViewById(R.id.view_demand_time);
        mDescriptionTV = (TextView) findViewById(R.id.view_demand_description);
        mReason = (TextView) findViewById(R.id.view_demand_reason);
        mPDDemand = new ProgressDialog(mActivity);

        // Finally set changes.

        showDemandPrior(mDemand.getType().getPriority());

        mPriorTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriorDialog = new AlertDialog.Builder(mActivity);
                mPriorDialog.setTitle("Mudar para:");
                mPriorDialog.setItems(getResources().getStringArray(R.array.array_status), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDemandPrior(CommonUtils.getPriorTag(which));
                    }
                });
                mPriorDialog.create();
                mPriorDialog.show();
            }
        });
        if (mPage == Constants.ADMIN_PAGE) mPriorTV.setClickable(true);
        else mPriorTV.setClickable(false);

        if (mPage == Constants.CREATE_PAGE)
            Snackbar.make(mSubjectTV, R.string.send_demand_success, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        mSubjectTV.setText(mDemand.getSubject().toUpperCase());
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
                mAlert.setTitle("Deferir a demanda?");
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
                Intent intent = new Intent(mActivity, RejectDialogActivity.class);
                startActivityForResult(intent,Constants.REJECT_DEMAND);
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

        // Select which activities has no menu.
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
        }else{ // Hide menu when screen scrolled (for activities with menu).
            Log.e(TAG, "In second else");
            mScrollView = findViewById(R.id.view_demand_scroll_view);
            mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    int scrollY = mScrollView.getScrollY();
                    hideMenuOnScroll( scrollY);
                }
            });
        }
    }

    private void hideMenuOnScroll(int scrollY) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REJECT_DEMAND) {
            if (resultCode == RESULT_OK) {
                PredefinedReason predefinedReason =
                        (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                attemptRejectDemand(
                        mDemand.getId(),
                        predefinedReason
                        );
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void setDemandPrior(String s) {
        if (mPriorTask == null && CommonUtils.isOnline(mActivity)){
            mPriorTask = new PriorTask(mDemand.getId(), s);
            mPriorTask.execute();
        } else {
            Snackbar.make(mPriorTV, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void showDemandPrior(String prior) {
        Log.e(TAG, "Prior tag:" + prior);
        Log.e(TAG, "Prior name:" + CommonUtils.getPriorName(prior, this));
        mPriorTV.setText(CommonUtils.getPriorName(prior, this));
        setPriorColor(prior);
    }

    private void attemptRejectDemand(int demandId, PredefinedReason predefinedReason){
        if (mRejectTask == null && CommonUtils.isOnline(mActivity)){
            mRejectTask = new RejectTask(
                    demandId,
                    (int) predefinedReason.getServerId(),
                    predefinedReason.getTitle(),
                    predefinedReason.getDescription()
            );
            mRejectTask.execute();
        } else {
            Snackbar.make(mFabReject, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
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
        int description;
        int color;

        switch (status){
            case Constants.LATE_STATUS: // Late.
                description = R.string.late_demand_info;
                drawable = R.drawable.ic_alarm_off_black_24dp;
                color = ContextCompat.getColor(this,R.color.colorPrimary);
                break;
            case Constants.DONE_STATUS: // Done.
                description = R.string.done_demand_info;
                drawable = R.drawable.ic_assignment_turned_in_white_24dp;
                color = ContextCompat.getColor(this,R.color.darkgreen);
                break;
            case Constants.ACCEPT_STATUS: // Accepted.
                description = R.string.accepted_demand_info;
                drawable = R.drawable.ic_thumb_up_black_24dp;
                color = ContextCompat.getColor(this,R.color.green);
                break;
            case Constants.REJECT_STATUS: //Rejected
                description = R.string.rejected_demand_info;
                drawable = R.drawable.ic_thumb_down_black_24dp;
                color = ContextCompat.getColor(this,R.color.darkred);
                showDemandReason(mDemand);
                break;
            case Constants.CANCEL_STATUS: //Cancelled
                description = R.string.canceled_demand_info;
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(this,R.color.red);
                break;
            case Constants.POSTPONE_STATUS: //Postponed
                description = R.string.postponed_demand_info;
                drawable = R.drawable.ic_timer_black_24dp;
                color = ContextCompat.getColor(this,R.color.darkyellow);
                break;
            case Constants.REOPEN_STATUS: //Reopen
            case Constants.UNDEFINE_STATUS: //Undefined
                description = R.string.undefined_demand_info;
                drawable = R.drawable.ic_adjust_black_24dp;
                color = ContextCompat.getColor(this,R.color.gray);
                break;
            case Constants.RESEND_STATUS: //Resent
                description = R.string.resent_demand_info;
                drawable = R.drawable.ic_send_black_24dp;
                color = ContextCompat.getColor(this,R.color.blue);
                break;
            default:
                description = R.string.undefined_demand_info;
                drawable = R.drawable.ic_adjust_black_24dp;
                color = ContextCompat.getColor(this,R.color.gray);
        }

        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = getDrawable(drawable);
        } else {
            objDrawable = getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        mStatusTV.setText(getString(description));
        mStatusTV.setCompoundDrawablesWithIntrinsicBounds(null,null, objDrawable,null);
        mStatusTV.getCompoundDrawables()[2].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void showDemandReason(Demand demand) {
        // TODO: show only a clickable title. When clicked show complete reason.
        PredefinedReason reason = demand.getReason();
        String reasonString;
        if (reason != null) {
            reasonString = "Ref.: "
                    + reason.getServerId()
                    + " - ("
                    + reason.getTitle()
                    + ") "
                    + reason.getDescription();
            mReason.setText(reasonString);
            mReason.setVisibility(View.VISIBLE);
        } else {
            mReason.setVisibility(View.GONE);
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

    private void setPriorColor(String prior) {
        int color = ContextCompat.getColor(this,R.color.dGreen);
        if (prior.equals(Constants.VERY_HIGH_PRIOR_TAG)) color = ContextCompat.getColor(this,R.color.darkred);
        if (prior.equals(Constants.HIGH_PRIOR_TAG)) color = ContextCompat.getColor(this,R.color.Red);
        if (prior.equals(Constants.MEDIUM_PRIOR_TAG)) color = ContextCompat.getColor(this,R.color.dyellow);
        mPriorTV.getCompoundDrawables()[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void setPostponeTime(int postponeTime) {
        // Set an alarm notification.
        Intent receiverIntent = new Intent(this, AlarmReceiver.class);
        receiverIntent.setType(Constants.POSTPONE_ALARM_TAG);
        receiverIntent.putExtra(Constants.INTENT_DEMAND, mDemand);
        receiverIntent.putExtra(Constants.INTENT_PAGE, mPage);
        receiverIntent.putExtra(Constants.INTENT_MENU, Constants.SHOW_TRIO_MENU);
        PendingIntent alarmSender = PendingIntent.getBroadcast(this, mDemand.getId(), receiverIntent, 0);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, postponeTime);
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
            return CommonUtils.POST("/demand/resend", values);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mDemandTask = null;

            try {
                JSONObject jsonObject;
                JSONObject senderJson;
                JSONObject receiverJson;
                JSONObject demandJson;
                JSONObject demandTypeJson;
                Demand demandResponse = null;
                boolean success = false;

                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                senderJson = jsonObject.getJSONObject("sender");
                receiverJson = jsonObject.getJSONObject("receiver");
                demandTypeJson = jsonObject.getJSONObject("demand_type");
                demandJson = jsonObject.getJSONObject("demand");

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                DemandType demandType = DemandType.build(demandTypeJson);
                demandResponse = Demand.build(sender, receiver, null, demandType, demandJson);

                Log.e(TAG,
                        "Json Resend Response:" + demandResponse.toString()
                                + " sender:" + sender.toString()
                                + " receiver:" + receiver.toString()
                                + " demandType:" + demandType.toString()
                );

                if (success) {
                    Intent intent = new Intent(mActivity, ViewDemandActivity.class);
                    intent.putExtra(Constants.INTENT_ACTIVITY, mActivity.getClass().getSimpleName());
                    intent.putExtra(Constants.INTENT_PAGE, mPage);
                    intent.putExtra(Constants.INTENT_DEMAND, demandResponse);
                    finish();
                    mActivity.startActivity(intent);
                } else {
                    throw new JSONException("success hit false");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabResend, R.string.send_demand_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

        }
    }

    private class PriorTask extends AsyncTask<Void, Void, String>{
        private int id;
        private String prior;

        public PriorTask(int id, String prior) {
            this.id = id;
            this.prior = prior;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Por favor aguarde...");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", id);
            values.put("prior", prior);
            return CommonUtils.POST("/demand/set-prior", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mPriorTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e(TAG, "POST DEMAND:" + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                Snackbar.make(mPriorTV, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            String message;

            if (success) {
                message = "Demanda modificada com sucesso";
                showDemandPrior(prior);
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
        private int user;

        public StatusTask(int id, String status) {
            this.id = id;
            this.status = status;
            if (mCurrentUser != null) this.user = mCurrentUser.getId();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage("Por favor aguarde");
            mPDDemand.setCancelable(false);
            mPDDemand.show();
            // If in this instant demand is "postponed", then make it cancel its alarm.
            if(mDemand.getStatus().equals(Constants.POSTPONE_STATUS)) mShouldCancelAlarm = true;
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", id);
            values.put("status", status);
            values.put("user", user);
            return CommonUtils.POST("/demand/set-status", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;

            Log.e(TAG, jsonResponse);

            try {
                JSONObject jsonObject;
                JSONObject demandJson;
                JSONObject senderJson;
                JSONObject receiverJson;
                JSONObject demandTypeJson;
                Demand demandResponse = null;
                boolean success = false;

                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                senderJson = jsonObject.getJSONObject("sender");
                receiverJson = jsonObject.getJSONObject("receiver");
                demandTypeJson = jsonObject.getJSONObject("demand_type");
                demandJson = jsonObject.getJSONObject("demand");

                PredefinedReason reason;
                JSONObject reasonJson;

                if(jsonObject.has("reason")){
                    reasonJson = jsonObject.getJSONObject("reason");
                    reason = PredefinedReason.build(reasonJson);
                    Log.e(TAG, " reason:" + reason.toString());
                }else{
                    reason = null;
                }

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                DemandType demandType = DemandType.build(demandTypeJson);
                demandResponse = Demand.build(sender, receiver, reason, demandType, demandJson);

                String message;

                if (success) {
                    switch(demandResponse.getStatus()){
                        case Constants.ACCEPT_STATUS:
                            message = "Demanda Deferida com Sucesso.";
                            break;
                        case Constants.POSTPONE_STATUS:
                            message = "Demanda Adiada com Sucesso.";
                            mShouldCancelAlarm = false;
                            break;
                        case Constants.CANCEL_STATUS:
                            message = "Demanda Cancelada com Sucesso.";
                            break;
                        case Constants.REOPEN_STATUS:
                            message = "Demanda Reaberta com Sucesso.";
                            break;
                        case Constants.REJECT_STATUS:
                            message = "Demanda Indeferida com Sucesso.";
                            break;
                        case Constants.DONE_STATUS:
                            message = "Demanda Concluída com Sucesso.";
                            Log.e(TAG, "On Done status, should cancel alarme");
                            // We can use the same logic here for DONE status, because before a demand
                            // is done, it would never be POSTPONE, so it is no problem.
                            mShouldCancelAlarm = true;
                            break;
                        default:
                            message = "Feito.";
                    }

                    // In case status changed from postponed to another, cancel alarm.
                    if(mShouldCancelAlarm){
                        CommonUtils.cancelDueTime(demandResponse,getApplicationContext(),Constants.WARN_DUE_TIME_ALARM_TAG);
                        CommonUtils.cancelDueTime(demandResponse,getApplicationContext(),Constants.DUE_TIME_ALARM_TAG);
                        CommonUtils.cancelDueTime(demandResponse,getApplicationContext(),Constants.POSTPONE_ALARM_TAG);
                        mShouldCancelAlarm = false;
                        Log.e(TAG, "should cancel alarm was true!");
                    }

                    Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG).show();
                    showDemandStatus(demandResponse.getStatus());
                    //TODO: Change status locally also.
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
        }
    }

    public class RejectTask extends AsyncTask<Void, Void, String> {
        private int id;
        private int reasonId;
        private String reasonTitle;
        private String reasonDescription;
        private int userId;

        public RejectTask(int id, int reasonId, String reasonTitle,
                          String reasonDescription) {
            this.id = id;
            this.reasonId = reasonId;
            this.reasonTitle = reasonTitle;
            this.reasonDescription = reasonDescription;
            if (mCurrentUser !=  null) this.userId = mCurrentUser.getId();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage(getString(R.string.progress_dialog_wait));
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demandId", id);
            values.put("reasonId", reasonId);
            values.put("reasonTitle", reasonTitle);
            values.put("reasonDescription", reasonDescription);
            values.put("userId", userId);
            return CommonUtils.POST("/demand/set-status-to-reject", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mRejectTask = null;

            Log.e(TAG, jsonResponse);

            try {
                JSONObject jsonObject;
                JSONObject demandJson;
                JSONObject senderJson;
                JSONObject receiverJson;
                JSONObject demandTypeJson;
                Demand demandResponse;
                boolean success;

                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
                senderJson = jsonObject.getJSONObject("sender");
                receiverJson = jsonObject.getJSONObject("receiver");
                demandTypeJson = jsonObject.getJSONObject("demand_type");
                demandJson = jsonObject.getJSONObject("demand");

                JSONObject reasonJson;
                PredefinedReason reason;

                if(jsonObject.has("reason")){
                    reasonJson = jsonObject.getJSONObject("reason");
                    reason = PredefinedReason.build(reasonJson);
                    Log.e(TAG, " reason:" + reason.toString());
                }else{
                    reason = null;
                }

                User sender = User.build(senderJson);
                User receiver = User.build(receiverJson);
                DemandType demandType = DemandType.build(demandTypeJson);
                demandResponse = Demand.build(sender, receiver, reason, demandType, demandJson);

                String message;

                if (success) {
                    switch(demandResponse.getStatus()){
                        case Constants.REJECT_STATUS:
                            message = "Demanda Indeferida com Sucesso.";
                            break;
                        default:
                            message = "Feito.";
                    }

                    Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG).show();
                    showDemandStatus(demandResponse.getStatus());
                    showDemandReason(demandResponse);
                    Log.e(TAG, "demand reason: " + demandResponse.getReason().getTitle());
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }
        }
    }
}
