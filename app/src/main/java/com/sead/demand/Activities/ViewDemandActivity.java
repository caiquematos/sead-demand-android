package com.sead.demand.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.Entities.User;
import com.sead.demand.Handlers.AlarmReceiver;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;
import com.sead.demand.Tools.MyAlarmManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.util.Arrays.asList;

public class ViewDemandActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();

    private TextView mSubjectTV;
    private TextView mPriorTV;
    private TextView mStatusTV;
    private TextView mSenderTV;
    private TextView mReceiverTV;
    private TextView mTimeTV;
    private TextView mDescriptionTV;
    private TextView mDueTimeTV;
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
    private FloatingActionButton mFabTransfer;
    private FloatingActionButton mFabFinish;
    private FloatingActionButton mFabDeadline;
    private TextView mYesTV;
    private TextView mNoTV;
    private TextView mLaterTV;
    private TextView mReopenTV;
    private TextView mRejectTV;
    private TextView mResendTV;
    private TextView mDoneTV;
    private TextView mTransferTV;
    private TextView mFinishTV;
    private TextView mDeadlineTV;
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
    private List<FloatingActionButton> mMenuButtonsList;
    private List<TextView> mMenuTitlesList;
    private String mMenuTag;
    private TransferTask mTransferTask;
    private DeadlineRequestTask mDeadlineRequestTask;
    private DeadlineAcceptedTask mDeadlineAcceptedTask;

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
        if (mDemand.getType() != null) Log.d(TAG, "demand type: " + mDemand.getType().getTitle());
        else Log.d(TAG, "demand type null");
        if (mDemand.getReason() != null) Log.d(TAG, "demand type: " + mDemand.getReason().getTitle());
        else Log.d(TAG, "demand reason null");


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
                    case Constants.TRANSFER_STATUS:
                        activityTitle = "Demanda Transferida (admin)";
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
        mMenuButtonsList = new ArrayList<>();
        mMenuTitlesList = new ArrayList<>();

        mSubjectTV = (TextView) findViewById(R.id.view_demand_subject);
        mPriorTV = (TextView) findViewById(R.id.view_demand_prior);
        mStatusTV = (TextView) findViewById(R.id.view_demand_status);
        mSenderTV = (TextView) findViewById(R.id.view_demand_sender);
        mReceiverTV = (TextView) findViewById(R.id.view_demand_receiver);
        mTimeTV = (TextView) findViewById(R.id.view_demand_time);
        mDueTimeTV = (TextView) findViewById(R.id.view_demand_due_time);
        mDescriptionTV = (TextView) findViewById(R.id.view_demand_description);
        mReason = (TextView) findViewById(R.id.view_demand_reason);
        mPDDemand = new ProgressDialog(mActivity);

        // Finally set changes.

        if (mDemand.getType() != null) showDemandPrior(mDemand.getType().getPriority());

        mPriorTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mPriorTV.setClickable(true);

        if (mPage == Constants.CREATE_PAGE)
            Snackbar.make(mSubjectTV, R.string.send_demand_success, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        mSubjectTV.setText(mDemand.getSubject().toUpperCase());
        mSenderTV.setText("De: " + mDemand.getSender().getName());
        mReceiverTV.setText("Para: " + mDemand.getReceiver().getName());
        mTimeTV.setText(CommonUtils.formatDate(mDemand.getCreatedAt()));
        mDescriptionTV.setText(mDemand.getDescription()+ "\n\n\n\n");

        Log.d(TAG, "Demand status: " + mDemand.getStatus());
        showDemandStatus(mDemand.getStatus());

        mAlert = new AlertDialog.Builder(this);
        mAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mAlertType.equals(Constants.RESEND_STATUS)) {
                    attemptSendDemand();
                } else if (mAlertType.equals(Constants.TRANSFER_STATUS)) {
                    startReasonDialog("Não Pode Atender?", "Quando a demanda não pode ser atendida, ela é repassada ao seu superior. Por favor, escolha um motivo.", Constants.NOT_ACCEPT_DEMAND);
                } else if (mAlertType.equals(Constants.DEADLINE_REQUESTED_STATUS)) {
                    startReasonDialog("Solicitar Aumento de Prazo?", "A solicitação será enviada ao seu superior.", Constants.DEADLINE_DEMAND);
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

        mTransferTV = (TextView) findViewById(R.id.tv_transfer);
        mFabTransfer = (FloatingActionButton) findViewById(R.id.fab_transfer);
        mMenuTitlesList.add(mTransferTV);
        mMenuButtonsList.add(mFabTransfer);
        mFabTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Repassar por competência?");
                mAlertType = Constants.TRANSFER_STATUS;
                mAlert.show();
            }
        }); // Transfer

        mDeadlineTV = (TextView) findViewById(R.id.tv_deadline);
        mFabDeadline = (FloatingActionButton) findViewById(R.id.fab_deadline);
        mMenuTitlesList.add(mDeadlineTV);
        mMenuButtonsList.add(mFabDeadline);
        mFabDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Postponed: " + mDemand.getPostponed());
                if (mDemand.getPostponed() < 3) {
                    mAlert.setTitle("Solicitar aumento de prazo?");
                    mAlertType = Constants.DEADLINE_REQUESTED_STATUS;
                    mAlert.show();
                } else {
                    Snackbar.make(mFabMenu, "Você já solicitou o aumento de prazo 3 vezes!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }); // Deadline

        mFinishTV = (TextView) findViewById(R.id.tv_finish);
        mFabFinish = (FloatingActionButton) findViewById(R.id.fab_finish);
        mMenuTitlesList.add(mFinishTV);
        mMenuButtonsList.add(mFabFinish);
        mFabFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Finalizar?");
                mAlertType = Constants.FINISH_STATUS;
                mAlert.show();
            }
        }); // Finish

        mYesTV = (TextView) findViewById(R.id.tv_yes);
        mFabYes = (FloatingActionButton) findViewById(R.id.fab_yes);
        mMenuTitlesList.add(mYesTV);
        mMenuButtonsList.add(mFabYes);
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
        mMenuTitlesList.add(mLaterTV);
        mMenuButtonsList.add(mFabLater);
        mFabLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostponeDialog = new AlertDialog.Builder(mActivity);
                mPostponeDialog.setTitle("Me lembre em:");
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
        mMenuTitlesList.add(mNoTV);
        mMenuButtonsList.add(mFabNo);
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
        mMenuTitlesList.add(mReopenTV);
        mMenuButtonsList.add(mFabReopen);
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
        mMenuTitlesList.add(mRejectTV);
        mMenuButtonsList.add(mFabReject);
        mFabReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, RejectDialogActivity.class);
                intent.putExtra("title", "Indeferir demanda?");
                intent.putExtra("message", "Escolha um motivo para o indeferimento.");
                startActivityForResult(intent,Constants.REJECT_DEMAND);
            }
        }); //Reject

        mResendTV = (TextView) findViewById(R.id.tv_resend);
        mFabResend = (FloatingActionButton) findViewById(R.id.fab_resend);
        mMenuTitlesList.add(mResendTV);
        mMenuButtonsList.add(mFabResend);
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
        mMenuTitlesList.add(mDoneTV);
        mMenuButtonsList.add(mFabDone);
        mFabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Concluir demanda?");
                mAlertType = Constants.DONE_STATUS;
                mAlert.show();
            }
        }); // Done.

        mFabMenu = (FloatingActionButton) findViewById(R.id.fab_menu);
        mFabMenu.setOnClickListener(this);
        handleMenu(mMenuType, mDemand.getStatus());

        showDueTime();

        /*

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
            noMenu();
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

        */

        Log.d(TAG, "Due Date | Time: " + mDemand.getDueDate() + " | " + mDemand.getDueTime());
    }

    private void showDueTime() {
        mDueTimeTV.setText(mDemand.getDueDate() + " " + mDemand.getDueTime());
    }

    private void startReasonDialog(String title, String message, int type) {
        Intent intent = new Intent(mActivity, RejectDialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        startActivityForResult(intent,type);
    }

    public void rotate(boolean isOn) {
        if (isOn) { // When 'close menu' button is hit.
            Log.e(TAG, "mTurned true. In first if");
            ViewCompat.animate(mFabMenu).
                    rotation(0f).
                    withLayer().
                    setDuration(1000).
                    setInterpolator(new OvershootInterpolator()).
                    start();
            mTurned = false;
        } else {
            // When 'open menu' button is hit.
            Log.e(TAG, "mTurned false. In first else");
            ViewCompat.animate(mFabMenu).
                    rotation(135f).
                    withLayer().
                    setDuration(1000).
                    setInterpolator(new OvershootInterpolator()).
                    start();
            mTurned = true;
        }
    }

    private void handleMenu(int menuType, String status) {
        noMenu(); // hide all menu options.
        switch (menuType) {
            case Constants.RECEIVER_MENU:
                handleReceiverMenu(status);
                break;
            case Constants.SUPERIOR_MENU:
                handleSuperiorMenu(status);
                break;
            case Constants.SENDER_MENU:
                handleSenderMenu(status);
                break;
        }
    }

    private void handleSenderMenu(String status) {
        switch (status) {
            case Constants.UNDEFINE_STATUS:
                senderUndefinedMenu();
                break;
            case Constants.ACCEPT_STATUS:
                senderAcceptedMenu();
                break;
            case Constants.DONE_STATUS:
                senderDoneMenu();
                break;
            case Constants.REJECT_STATUS:
                senderRejectedMenu();
                break;
            default:
                noMenu();
        }
    }

    private void handleReceiverMenu(String status) {
        switch (status) {
            case Constants.UNDEFINE_STATUS:
                receiverUndefinedMenu();
                break;
            case Constants.ACCEPT_STATUS:
                receiverAcceptedMenu();
                break;
            case Constants.DONE_STATUS:
                receiverDoneMenu();
                break;
            case Constants.DEADLINE_REQUESTED_STATUS:
                receiverDeadlineRequestedMenu();
                break;
            case Constants.DEADLINE_ACCEPTED_STATUS:
                receiverDeadlineAcceptedMenu();
                break;
            default:
                noMenu();
        }
    }

    private void handleSuperiorMenu(String status) {
        switch (status) {
            case Constants.UNDEFINE_STATUS:
                superiorUndefinedMenu();
                break;
            case Constants.ACCEPT_STATUS:
                superiorAcceptedMenu();
                break;
            case Constants.TRANSFER_STATUS:
                superiorTransferredMenu();
                break;
            case Constants.DEADLINE_ACCEPTED_STATUS:
                superiorDeadlineAcceptedMenu();
                break;
            case Constants.DEADLINE_REQUESTED_STATUS:
                superiorDeadlineRequestedMenu();
                break;
            default:
                noMenu();
        }
    }

    private void senderRejectedMenu() {
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "sender_reject_menu";
    }

    private void senderDoneMenu() {
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "sender_done_menu";
    }

    private void senderAcceptedMenu() {
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "sender_accepted_menu";
    }

    private void senderUndefinedMenu() {
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "sender_undefined_menu";
    }

    private void superiorAcceptedMenu() {
        mMenuTag = "superior_accepted_menu";
    }

    private void superiorUndefinedMenu() {
        mMenuTag = "superior_undefined_menu";
    }

    private void superiorTransferredMenu() {
        showDemandReason(mDemand);
        if (checkDemandAuthority(mDemand.getId(), mCurrentUser.getId())) {
            mFabMenu.setVisibility(View.VISIBLE);
            mFabMenu.show();
            mMenuTag = "superior_transferred_menu";
        } else {
            mMenuTag = "no_menu";
        }
    }

    private void superiorDeadlineRequestedMenu() {
        showDemandReason(mDemand);
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "superior_deadline_requested_menu";
        changeMenuState();
    }

    private void superiorDeadlineAcceptedMenu(){
        mMenuTag = "superior_deadline_accepted_menu";
    }

    private boolean checkDemandAuthority(int demandId, int userId) {
        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_USER + " = ? AND "
                + FeedReaderContract.AuthorityEntry.COLUMN_NAME_DEMAND + " = ?";

        String[] args = {
                "" + userId,
                "" + demandId
        };

        MyDBManager myDBManager = new MyDBManager(this);
        List<Authority> authorities = myDBManager.searchAuthorities(selection,args);

        Log.d(TAG, "check authority:" + authorities.toString());

        return !authorities.isEmpty();
    }

    private void receiverDoneMenu() {
        mMenuTag = "receiver_done_menu";
    }

    private void receiverAcceptedMenu() {
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "receiver_accepted_menu";
    }

    private void receiverUndefinedMenu() {
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "receiver_undefined_menu";
        changeMenuState();
    }

    private void receiverDeadlineRequestedMenu() {
        showDemandReason(mDemand);
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "receiver_deadline_requested_menu";
    }

    private void receiverDeadlineAcceptedMenu(){
        mFabMenu.setVisibility(View.VISIBLE);
        mFabMenu.show();
        mMenuTag = "receiver_deadline_accepted_menu";
    }

    private void changeMenuState() {
        int color = ContextCompat.getColor(this,R.color.OrangeRed);

        int drawable = R.drawable.ic_help_white_24dp;
        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = this.getDrawable(drawable);
        } else {
            objDrawable = this.getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        mFabMenu.setBackgroundTintList(ColorStateList.valueOf(color));
        mFabMenu.setImageDrawable(objDrawable);
    }

    private void restoreMenuState() {
        int color = ContextCompat.getColor(this,R.color.accent);
        int icColor = ContextCompat.getColor(this, R.color.white);

        int drawable = R.drawable.ic_add_black_24dp;
        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = this.getDrawable(drawable);
        } else {
            objDrawable = this.getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        mFabMenu.setBackgroundTintList(ColorStateList.valueOf(color));
        mFabMenu.setImageDrawable(objDrawable);
        mFabMenu.setColorFilter(icColor);
    }

    private void noMenu() {
        // first, set all buttons and titles to GONE.
        for (TextView title : mMenuTitlesList) {
            title.setVisibility(View.GONE);
        }
        for (FloatingActionButton button : mMenuButtonsList) {
            Log.d(TAG, "all buttons:" + button.getId());
            button.hide();
            button.setVisibility(View.GONE);
        }
        mFabMenu.hide();
        mFabMenu.setVisibility(View.GONE);
    }

    private void showItems(List<FloatingActionButton> floatingActionButtons) {
        // second, change only the chosen buttons.
        for (int i = 0; i < floatingActionButtons.size(); i++) {
            FloatingActionButton button = floatingActionButtons.get(i);
            for (int j = 0; j < mMenuButtonsList.size(); j ++) {
                FloatingActionButton btn = mMenuButtonsList.get(j);
                TextView title = mMenuTitlesList.get(j);
                if (btn.getId() == button.getId()) {
                    Log.d(TAG, "chosen buttons:" + btn.getId());
                    if(btn.isShown()) {
                        Log.d(TAG, "was showing...");
                        btn.hide();
                        title.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "wasn't showing...");
                        btn.show();
                        title.setVisibility(View.VISIBLE);
                    }
                }
            }
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
        PredefinedReason predefinedReason;
        if (resultCode == RESULT_OK){
            switch (requestCode) {
                case Constants.REJECT_DEMAND:
                    predefinedReason = (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                    attemptRejectDemand(
                            mDemand.getId(),
                            predefinedReason
                    );
                    break;
                case Constants.NOT_ACCEPT_DEMAND:
                    predefinedReason = (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                    attemptTransferDemand(
                            mDemand.getId(),
                            predefinedReason,
                            mDemand.getReceiver().getId()
                    );
                    break;
                case Constants.DEADLINE_DEMAND:
                    predefinedReason = (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                    attemptDeadlineRequest(mDemand,predefinedReason);
                    break;
            }
        } else {
            Log.e(TAG, "OnActivityResult failed!!!");
        }
    }

    private void attemptDeadlineRequest(Demand demand, PredefinedReason predefinedReason) {
        if (mDeadlineRequestTask == null && CommonUtils.isOnline(mActivity)) {
            mDeadlineRequestTask = new DeadlineRequestTask(demand.getId(), predefinedReason);
            mDeadlineRequestTask.execute();
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void attemptDeadlineAccept(Demand demand) {
            if (mDeadlineAcceptedTask == null && CommonUtils.isOnline(mActivity)) {
                mDeadlineAcceptedTask = new DeadlineAcceptedTask(demand.getId());
                mDeadlineAcceptedTask.execute();
            } else {
                Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
    }

    private void attemptTransferDemand(int demandId, PredefinedReason predefinedReason, int receiverId) {
        if (mTransferTask == null && CommonUtils.isOnline(mActivity)){
            mTransferTask = new TransferTask(receiverId, demandId, predefinedReason);
            mTransferTask.execute();
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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
            case Constants.DEADLINE_ACCEPTED_STATUS:
                description = R.string.deadline_accepted_demand_info;
                drawable = R.drawable.ic_alarm_on_black_24dp;
                color = ContextCompat.getColor(this,R.color.DarkOrchid);
                break;
            case Constants.DEADLINE_REQUESTED_STATUS:
                description = R.string.deadline_requested_demand_info;
                drawable = R.drawable.ic_alarm_add_black_24dp;
                color = ContextCompat.getColor(this, R.color.DarkOrchid);
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
            case Constants.TRANSFER_STATUS: //Transferred
                description = R.string.transferred_demand_info;
                drawable = R.drawable.ic_swap_calls_white_24dp;
                color = ContextCompat.getColor(this, R.color.Brown);
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
            Log.d(TAG, "Reason: " + reason.getTitle());
            reasonString = "Ref.: "
                    + reason.getServerId()
                    + " - ("
                    + reason.getTitle()
                    + ") "
                    + reason.getDescription();
            mReason.setText(reasonString);
            mReason.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Reason null");
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
        int type = Constants.POSTPONE_ALARM_TAG;
        receiverIntent.setType("" + type);
        receiverIntent.putExtra(Constants.INTENT_DEMAND, mDemand);
        receiverIntent.putExtra(Constants.INTENT_PAGE, mPage);
        receiverIntent.putExtra(Constants.INTENT_MENU, Constants.SHOW_TRIO_MENU);
        Calendar c = Calendar.getInstance();
        double cBefore = c.getTimeInMillis();
        Log.e(TAG, "Calendar instance:" + c.getTimeInMillis() + " postpon:" + postponeTime);
        c.add(Calendar.DAY_OF_YEAR, postponeTime);
        Log.e(TAG, "Calendar added postpone time:" + c.getTimeInMillis());
        long cAfter = c.getTimeInMillis();
        //Log.e(TAG, "Time in days added:" + ((cAfter - cBefore) * 1000)/3600/24);
        Log.e(TAG, "Time in days added:" + ((cAfter - cBefore) * 1000)/60);
       // AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
       // am.set(AlarmManager.RTC_WAKEUP, timeInMillis, alarmSender);
        MyAlarmManager.addAlarm(this, receiverIntent, mDemand.getId(), type, cAfter);
        // Change status state on server.
        //setDemandStatus(Constants.POSTPONE_STATUS);
    }

    @Override
    public void onClick(View v) {
        rotate(mTurned);
        switch (mMenuTag) {
            /* receiver section --- */
            case "receiver_accepted_menu":
                showItems(asList(mFabDone, mFabLater, mFabTransfer, mFabDeadline));
                break;
            case "receiver_undefined_menu":
                rotate(mTurned);
                buildReceiverUndefinedMenu();
                break;
            case "receiver_deadline_requested_menu":
                showItems(asList(mFabDone, mFabLater, mFabTransfer));
                break;
            case "receiver_deadline_accepted_menu":
                showItems(asList(mFabDone, mFabLater, mFabTransfer, mFabDeadline));
                break;
            case "receiver_done_menu":
            case "receiver_finished_menu":
                noMenu();
                break;
            /* --- receiver section */

            /* sender section --- */
            case "sender_done_menu":
                showItems(asList(mFabReopen, mFabFinish));
                break;
            case "sender_reject_menu":
                showItems(asList(mFabReopen));
                break;
            case "sender_accepted_menu":
                showItems(asList(mFabNo));
                break;
            case "sender_undefined_menu":
                showItems(asList(mFabNo));
                break;
            /* --- sender section */

            /* superior section --- */
            case "superior_accepted_menu":
            case "superior_undefined_menu":
                noMenu();
                break;
            case "superior_transferred_menu":
                showItems(asList(mFabTransfer, mFabReject, mFabLater));
                break;
            case "superior_deadline_requested_menu":
                rotate(mTurned);
                buildSuperiorDeadlineRequestedMenu();
                break;
            case "superior_deadline_accepted_menu":
                noMenu();
                break;
            /* --- superior section */

            case "no_menu":
                noMenu();
                break;

            default:
                noMenu();
        }
    }

    private void buildSuperiorDeadlineRequestedMenu() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle(mDemand.getReceiver().getName());
        dialog.setMessage("Aceitar solicitação de aumento de prazo?");
        dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptDeadlineAccept(mDemand);
            }
        });
        dialog.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.create();
        dialog.show();
    }

    private void buildReceiverUndefinedMenu() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle(mDemand.getReceiver().getName());
        dialog.setMessage("Você pode atender a essa demanda?");
        dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setDemandStatus(Constants.ACCEPT_STATUS);
            }
        });
        dialog.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mActivity, RejectDialogActivity.class);
                intent.putExtra("title", "Não Pode Atender?");
                intent.putExtra("message", "Quando a demanda não pode ser atendida, ela é repassada ao seu superior. Por favor, escolha um motivo.");
                startActivityForResult(intent,Constants.NOT_ACCEPT_DEMAND);
            }
        });
        dialog.create();
        dialog.show();
    }

    private void updateAccepted(Demand demandResponse) {
        restoreMenuState(); // back to normal form with a '+'.
        handleMenu(mMenuType, mDemand.getStatus());
    }

    private void updateDeadlineAccepted(Demand demandResponse) {
        restoreMenuState();
        handleMenu(mMenuType, mDemand.getStatus());
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
                Demand demandResponse;
                boolean success;

                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");

                String message;

                if (success) {
                    senderJson = jsonObject.getJSONObject("sender");
                    receiverJson = jsonObject.getJSONObject("receiver");
                    demandJson = jsonObject.getJSONObject("demand");

                    DemandType demandType = null;

                    if (!jsonObject.isNull("demand_type")) {
                        demandTypeJson = jsonObject.getJSONObject("demand_type");
                        demandType = DemandType.build(demandTypeJson);
                    }

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
                    demandResponse = Demand.build(sender, receiver, reason, demandType, demandJson);
                    mDemand = demandResponse;

                    switch(demandResponse.getStatus()){
                        case Constants.ACCEPT_STATUS:
                            message = "Demanda Deferida com Sucesso.";
                            updateAccepted(demandResponse);
                            break;
                        case Constants.DEADLINE_ACCEPTED_STATUS:
                            message = "Solicitacão Aceitac com Sucesso.";
                            updateDeadlineAccepted(demandResponse);
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
                            mShouldCancelAlarm = true;
                            message = "Demanda Indeferida com Sucesso.";
                            break;
                        case Constants.DONE_STATUS:
                            handleMenu(mMenuType, mDemand.getStatus());
                            message = "Demanda Concluída com Sucesso.";
                            mShouldCancelAlarm = true;
                            break;
                        default:
                            message = "Feito.";
                    }

                    // In case status changed from postponed to another, cancel alarm.
                    if(mShouldCancelAlarm){
                        CommonUtils.cancelAllAlarms(demandResponse, mActivity);
                        mShouldCancelAlarm = false;
                        Log.e(TAG, "should cancel alarm was true!");
                    }

                    Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG).show();
                    showDemandStatus(demandResponse.getStatus());
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

                showDemandStatus(demandResponse.getStatus());

                String message;
                if (success) {
                    switch(demandResponse.getStatus()){
                        case Constants.REJECT_STATUS:
                            message = "Demanda Indeferida com Sucesso.";
                            CommonUtils.cancelAllAlarms(demandResponse, mActivity);
                            showDemandReason(demandResponse);
                            break;
                        default:
                            message = "Feito.";
                    }

                    Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG).show();
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

    public class TransferTask extends AsyncTask<Void, Void, String> {
        private long demandId;
        private long userId;
        private PredefinedReason predefinedReason;

        public TransferTask(long userId, long demandId, PredefinedReason predefinedReason) {
            this.demandId = demandId;
            this.userId = userId;
            this.predefinedReason = predefinedReason;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage(getString(R.string.progress_dialog_wait));
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            values.put("reason_id", predefinedReason.getServerId());
            values.put("reason_title", predefinedReason.getTitle());
            values.put("reason_description", predefinedReason.getDescription());
            values.put("user_id", userId);
            return CommonUtils.POST("/send/transfer-to-superior", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mTransferTask = null;

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

            Log.e(TAG, s);

            try {
                JSONObject jsonObject;
                JSONObject demandJson;
                JSONObject senderJson;
                JSONObject receiverJson;
                JSONObject demandTypeJson;
                Demand demandResponse;
                boolean success;

                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    senderJson = jsonObject.getJSONObject("sender");
                    receiverJson = jsonObject.getJSONObject("receiver");
                    demandJson = jsonObject.getJSONObject("demand");

                    JSONObject reasonJson;
                    PredefinedReason reason = null;
                    DemandType demandType = null;

                    if(jsonObject.has("reason")){
                        reasonJson = jsonObject.getJSONObject("reason");
                        reason = PredefinedReason.build(reasonJson);
                        Log.e(TAG, " reason:" + reason.toString());
                    }

                    if (!jsonObject.isNull("demand_type")){
                        demandTypeJson = jsonObject.getJSONObject("demand_type");
                        demandType = DemandType.build(demandTypeJson);
                    }

                    User sender = User.build(senderJson);
                    User receiver = User.build(receiverJson);
                    demandResponse = Demand.build(sender, receiver, reason, demandType, demandJson);

                    showDemandStatus(demandResponse.getStatus());

                    String message;
                    switch(demandResponse.getStatus()){
                        case Constants.TRANSFER_STATUS:
                            message = "Demanda transferida com Sucesso.";
                            CommonUtils.cancelAllAlarms(demandResponse, mActivity);
                            showDemandReason(demandResponse);
                            noMenu();
                            break;
                        default:
                            message = "A demanda não pôde ser transferida.";
                    }

                    Snackbar.make(mFabMenu, message, Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "demand reason: " + demandResponse.getReason().getTitle());
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    public class DeadlineRequestTask extends AsyncTask<Void, Void, String> {
        private long demandId;
        private PredefinedReason predefinedReason;

        public DeadlineRequestTask(long demandId, PredefinedReason predefinedReason) {
            this.demandId = demandId;
            this.predefinedReason = predefinedReason;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage(getString(R.string.progress_dialog_wait));
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            values.put("reason_id", predefinedReason.getServerId());
            values.put("reason_title", predefinedReason.getTitle());
            values.put("reason_description", predefinedReason.getDescription());
            return CommonUtils.POST("/send/deadline-request", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDeadlineRequestTask = null;

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

            Log.e(TAG, s);

            try {
                JSONObject jsonObject;
                JSONObject demandJson;
                JSONObject senderJson;
                JSONObject receiverJson;
                JSONObject demandTypeJson;
                Demand demandResponse;
                boolean success;

                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    senderJson = jsonObject.getJSONObject("sender");
                    receiverJson = jsonObject.getJSONObject("receiver");
                    demandJson = jsonObject.getJSONObject("demand");

                    JSONObject reasonJson;
                    PredefinedReason reason = null;
                    DemandType demandType = null;

                    if(jsonObject.has("reason")){
                        reasonJson = jsonObject.getJSONObject("reason");
                        reason = PredefinedReason.build(reasonJson);
                        Log.e(TAG, " reason:" + reason.toString());
                    }

                    if (!jsonObject.isNull("demand_type")){
                        demandTypeJson = jsonObject.getJSONObject("demand_type");
                        demandType = DemandType.build(demandTypeJson);
                    }

                    User sender = User.build(senderJson);
                    User receiver = User.build(receiverJson);
                    demandResponse = Demand.build(sender, receiver, reason, demandType, demandJson);

                    String message;

                    switch(demandResponse.getStatus()){
                        case Constants.DEADLINE_REQUESTED_STATUS:
                            message = "Solicitação enviada com Sucesso.";
                            handleMenu(mMenuType,demandResponse.getStatus());
                            break;
                        default:
                            message = "Algo errado!.";
                    }

                    Snackbar.make(mFabMenu, message, Snackbar.LENGTH_LONG).show();
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

        }
    }

    public class DeadlineAcceptedTask extends AsyncTask<Void, Void, String> {
        private long demandId;

        public DeadlineAcceptedTask(long demandId) {
            this.demandId = demandId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDDemand.setMessage(getString(R.string.progress_dialog_wait));
            mPDDemand.setCancelable(false);
            mPDDemand.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            return CommonUtils.POST("/send/deadline-accept", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDeadlineAcceptedTask = null;

            if (mPDDemand.isShowing()){
                mPDDemand.dismiss();
            }

            Log.e(TAG, s);

            try {
                JSONObject jsonObject;
                JSONObject demandJson;
                JSONObject senderJson;
                JSONObject receiverJson;
                JSONObject demandTypeJson;
                Demand demandResponse;
                boolean success;

                jsonObject = new JSONObject(s);
                success = jsonObject.getBoolean("success");

                if (success) {
                    senderJson = jsonObject.getJSONObject("sender");
                    receiverJson = jsonObject.getJSONObject("receiver");
                    demandJson = jsonObject.getJSONObject("demand");

                    JSONObject reasonJson;
                    PredefinedReason reason = null;
                    DemandType demandType = null;

                    if(jsonObject.has("reason")){
                        reasonJson = jsonObject.getJSONObject("reason");
                        reason = PredefinedReason.build(reasonJson);
                        Log.e(TAG, " reason:" + reason.toString());
                    }

                    if (!jsonObject.isNull("demand_type")){
                        demandTypeJson = jsonObject.getJSONObject("demand_type");
                        demandType = DemandType.build(demandTypeJson);
                    }

                    User sender = User.build(senderJson);
                    User receiver = User.build(receiverJson);
                    demandResponse = Demand.build(sender, receiver, reason, demandType, demandJson);

                    String message;

                    switch(demandResponse.getStatus()){
                        case Constants.DEADLINE_ACCEPTED_STATUS:
                            message = "Solicitação aceita com sucesso.";
                            handleMenu(mMenuType,demandResponse.getStatus());
                            break;
                        default:
                            message = "Ops! Algo deu errado.";
                    }

                    Snackbar.make(mFabMenu, message, Snackbar.LENGTH_LONG).show();
                    showDemandStatus(demandResponse.getStatus());
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }
}
