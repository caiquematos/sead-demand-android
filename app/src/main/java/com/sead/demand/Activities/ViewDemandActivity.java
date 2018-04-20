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
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.DemandType;
import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private TextView mReasonTV;
    private TextView mTypeTV;
    private TextView mComplexityTV;
    private TextView mLateTV;
    private View mScrollView;
    private StatusTask mStatusTask;
    private static ProgressDialog mPDDemand;
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
    private View mYesView;
    private View mNoView;
    private View mLaterView;
    private View mReopenView;
    private View mRejectView;
    private View mResendView;
    private View mDoneView;
    private View mTransferView;
    private View mFinishView;
    private View mDeadlineView;
    private int mPage; // Identifies which activity called this one.
    private int mMenuType; // Identifies which type of menu to be shown.
    private boolean mTurned;
    private Demand mDemand;
    private AlertDialog.Builder mMenuAlert;
    private AlertDialog.Builder mInfoAlert;
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
    private List<View> mMenuViewList;
    private String mMenuTag;
    private TransferTask mTransferTask;
    private DeadlineRequestTask mDeadlineRequestTask;
    private DeadlineAcceptedTask mDeadlineAcceptedTask;
    private AdminTransferTask mAdminTransferTask;
    private CancelTask mCancelTask;
    private MarkAsUnfinishedTask mMarkAsUnfinishedTask;

    public ViewDemandActivity() {
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_demand);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        setTitle(generateActivityTitle());

        // Get object references.
        mMenuButtonsList = new ArrayList<>();
        mMenuTitlesList = new ArrayList<>();
        mMenuViewList = new ArrayList<>();

        mSubjectTV = (TextView) findViewById(R.id.view_demand_subject);
        mPriorTV = (TextView) findViewById(R.id.view_demand_prior);
        mStatusTV = (TextView) findViewById(R.id.view_demand_status);
        mSenderTV = (TextView) findViewById(R.id.view_demand_sender);
        mReceiverTV = (TextView) findViewById(R.id.view_demand_receiver);
        mTimeTV = (TextView) findViewById(R.id.view_demand_time);
        mDueTimeTV = (TextView) findViewById(R.id.view_demand_due_time);
        mDescriptionTV = (TextView) findViewById(R.id.view_demand_description);
        mReasonTV = (TextView) findViewById(R.id.view_demand_reason);
        mTypeTV = (TextView) findViewById(R.id.view_demand_type);
        mComplexityTV = (TextView) findViewById(R.id.view_demand_complexity);
        mLateTV = (TextView) findViewById(R.id.view_demand_late);
        mPDDemand = new ProgressDialog(mActivity);

        /* Finally set changes */
        if (mPage == Constants.CREATE_PAGE)
            Snackbar.make(mSubjectTV, R.string.send_demand_success, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        mSubjectTV.setText(mDemand.getSubject().toUpperCase());
        mSenderTV.setText("De: " + mDemand.getSender().getName());
        mReceiverTV.setText("Para: " + mDemand.getReceiver().getName());
        mTimeTV.setText(CommonUtils.formatDate(mDemand.getCreatedAt()));
        mDescriptionTV.setText(mDemand.getDescription());

        setMenuAlert();
        setInfoAlert();
        showDemandStatus(mDemand.getStatus());
        handleDemandType(mDemand);
        handleDemandLate(mDemand);
        showDueTime();
        setInfoClickListeners();
        setMenuOptions();

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
    RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) myFab.getLayoutParams();
    p.setMargins(0, 0, 0, 0); // get rid of margins since shadow area is now the margin
    myFab.setLayoutParams(p);
         */

        mFabMenu = findViewById(R.id.fab_menu);
        mFabMenu.setOnClickListener(this);
        handleMenu(mMenuType, mDemand.getStatus());

        mScrollView = findViewById(R.id.view_demand_scroll_view);
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = mScrollView.getScrollY();
                hideMenuOnScroll( scrollY);
            }
        });
    }

    private void setInfoClickListeners() {
        mPriorTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "TEMPO DE EXECUÇÃO";
                String message = "MUITO LONGO: 1 a 3 dias\n\n" +
                        "LONGO: 4 a 8 dias\n\n" +
                        "MÉDIO: 9 a 15 dias\n\n" +
                        "CURTO: mais de 15 dias";
                showInfoAlert(title, message);
            }
        });

        mDueTimeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "PRAZO: " + mDemand.getDueDate() + " " + mDemand.getDueTime();
                String message = "HOJE: " + getToday();
                        //+ "\n\n" + getDaysLeft();;
                showInfoAlert(title, message);
            }
        });

        mComplexityTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "COMPLEXIDADE";
                String message = "" + mDemand.getType().getComplexity();
                showInfoAlert(title, message);
            }
        });

        mLateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "DEMANDA ATRASADA";
                String message = "PRAZO: " + mDemand.getDueDate() + " " + mDemand.getDueTime()
                        + "\n\nHOJE: " + getToday();
                        //+ "\n\n" + getDaysLeft();
                showInfoAlert(title, message);
            }
        });

        mReasonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDemand.getReason() != null) {
                    String title = mDemand.getReason().getTitle();
                    String message = "Referência " + mDemand.getReason().getServerId() + "\n\n"
                            + mDemand.getReason().getDescription();
                    showInfoAlert(title, message);
                }
            }
        });
    }

    private void showInfoAlert(String title, String message) {
        mInfoAlert.setTitle(title);
        mInfoAlert.setMessage(message);
        mInfoAlert.create();
        mInfoAlert.show();
    }

    private void setMenuOptions() {
        mTransferView = findViewById(R.id.view_fab_transfer);
        mTransferTV = (TextView) findViewById(R.id.tv_transfer);
        mFabTransfer = (FloatingActionButton) findViewById(R.id.fab_transfer);
        mMenuTitlesList.add(mTransferTV);
        mMenuButtonsList.add(mFabTransfer);
        mMenuViewList.add(mTransferView);
        mFabTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuAlert.setTitle("Repassar a demanda?");
                mAlertType = Constants.TRANSFER_STATUS;
                mMenuAlert.show();
            }
        }); // Transfer


        mDeadlineView = findViewById(R.id.view_fab_deadline);
        mDeadlineTV = (TextView) findViewById(R.id.tv_deadline);
        mFabDeadline = (FloatingActionButton) findViewById(R.id.fab_deadline);
        mMenuTitlesList.add(mDeadlineTV);
        mMenuButtonsList.add(mFabDeadline);
        mMenuViewList.add(mDeadlineView);
        mFabDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Postponed: " + mDemand.getPostponed());
                if (mDemand.getPostponed() < 3) {
                    mMenuAlert.setTitle("Solicitar aumento de prazo?");
                    mAlertType = Constants.DEADLINE_REQUESTED_STATUS;
                    mMenuAlert.show();
                } else {
                    Snackbar.make(mFabMenu, "Você já atingiu o limite de 3 solicitações!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }); // Deadline

        mFinishView = findViewById(R.id.view_fab_finish);
        mFinishTV = (TextView) findViewById(R.id.tv_finish);
        mFabFinish = (FloatingActionButton) findViewById(R.id.fab_finish);
        mMenuTitlesList.add(mFinishTV);
        mMenuButtonsList.add(mFabFinish);
        mMenuViewList.add(mFinishView);
        mFabFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildSenderFinishMenu();
            }
        }); // Finish

        mYesView = findViewById(R.id.view_fab_yes);
        mYesTV = (TextView) findViewById(R.id.tv_yes);
        mFabYes = (FloatingActionButton) findViewById(R.id.fab_yes);
        mMenuTitlesList.add(mYesTV);
        mMenuButtonsList.add(mFabYes);
        mMenuViewList.add(mYesView);
        mFabYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuAlert.setTitle("Deferir a demanda?");
                mAlertType = Constants.ACCEPT_STATUS;
                mMenuAlert.show();
            }
        }); //Accepted

        mLaterView = findViewById(R.id.view_fab_later);
        mLaterTV = (TextView) findViewById(R.id.tv_later);
        mFabLater = (FloatingActionButton) findViewById(R.id.fab_later);
        mMenuTitlesList.add(mLaterTV);
        mMenuButtonsList.add(mFabLater);
        mMenuViewList.add(mLaterView);
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
                mPostponeDialog.setItems(postponeOptions, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setReminder(mDemand, Constants.POSTPONE_OPTIONS[which]);
                    }
                });
                mPostponeDialog.create();
                mPostponeDialog.show();
            }
        }); //Postponed

        mNoView = findViewById(R.id.view_fab_no);
        mNoTV = (TextView) findViewById(R.id.tv_no);
        mFabNo = (FloatingActionButton) findViewById(R.id.fab_no);
        mMenuTitlesList.add(mNoTV);
        mMenuButtonsList.add(mFabNo);
        mMenuViewList.add(mNoView);
        mFabNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuAlert.setTitle("Cancelar a demanda?");
                mAlertType = Constants.CANCEL_ACCEPTED_STATUS;
                mMenuAlert.show();
            }
        }); //Cancelled

        mReopenView = findViewById(R.id.view_fab_reopen);
        mReopenTV = (TextView) findViewById(R.id.tv_repopen);
        mFabReopen = (FloatingActionButton) findViewById(R.id.fab_reopen);
        mMenuTitlesList.add(mReopenTV);
        mMenuButtonsList.add(mFabReopen);
        mMenuViewList.add(mReopenView);
        mFabReopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuAlert.setTitle("Reabrir a demanda?");
                mMenuAlert.setMessage("Quando uma demanda é reaberta, ela é movida novamente para a aba Admin e seu status é configurado de volta para indefinido.");
                mAlertType = Constants.REOPEN_STATUS;
                mMenuAlert.show();
            }
        }); //Reopen

        mRejectView = findViewById(R.id.view_fab_reject);
        mRejectTV = (TextView) findViewById(R.id.tv_reject);
        mFabReject = (FloatingActionButton) findViewById(R.id.fab_reject);
        mMenuTitlesList.add(mRejectTV);
        mMenuButtonsList.add(mFabReject);
        mMenuViewList.add(mRejectView);
        mFabReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, RejectDialogActivity.class);
                intent.putExtra("title", "Indeferir demanda?");
                intent.putExtra("message", "Escolha um motivo para o indeferimento.");
                startActivityForResult(intent,Constants.REJECT_DEMAND);
            }
        }); //Reject

        mResendView = findViewById(R.id.view_fab_resend);
        mResendTV = (TextView) findViewById(R.id.tv_resend);
        mFabResend = (FloatingActionButton) findViewById(R.id.fab_resend);
        mMenuTitlesList.add(mResendTV);
        mMenuButtonsList.add(mFabResend);
        mMenuViewList.add(mResendView);
        mFabResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuAlert.setTitle("Reenviar a demanda?");
                mAlertType = Constants.RESEND_STATUS;
                mMenuAlert.show();
            }
        }); //Resend

        mDoneView = findViewById(R.id.view_fab_done);
        mDoneTV = (TextView) findViewById(R.id.tv_done);
        mFabDone = (FloatingActionButton) findViewById(R.id.fab_done);
        mMenuTitlesList.add(mDoneTV);
        mMenuButtonsList.add(mFabDone);
        mMenuViewList.add(mDoneView);
        mFabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMenuAlert.setTitle("Concluir demanda?");
                mAlertType = Constants.DONE_STATUS;
                mMenuAlert.show();
            }
        }); // Done.
    }

    private void setInfoAlert() {
        mInfoAlert = new AlertDialog.Builder(this);
        mInfoAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    private void setMenuAlert() {
        mMenuAlert = new AlertDialog.Builder(this);
        mMenuAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (mAlertType) {
                    case Constants.TRANSFER_STATUS:
                        handleTransfer(mPage);
                        break;
                    case Constants.DEADLINE_REQUESTED_STATUS:
                        startReasonDialog("Solicitar Aumento de Prazo?",
                                "Por favor, insira o motivo da solicitação, e em seguida clique em \"ok\" para enviá-la para o seu superior.",
                                Constants.DEADLINE_DEMAND);
                        break;
                    case Constants.CANCEL_ACCEPTED_STATUS:
                        startReasonDialog("Solicitar cancelamento?",
                                "Por favor, insira o motivo da solicitação, e em seguida clique em \"ok\" para enviá-la para o colaborador responsável",
                                Constants.CANCEL_REQUEST_DEMAND);
                        break;
                    default:
                        setDemandStatus(mAlertType);
                }
            }

            private void handleTransfer(int page) {
                switch (page) {
                    case Constants.ADMIN_PAGE:
                        startTransferActivity(Constants.SUPERIOR_TRANSFER_DEMAND);
                        break;
                    case Constants.RECEIVED_PAGE:
                        startReasonDialog("Repassar Por Competência?",
                                getString(R.string.view_activity_transfer_reason_description),
                                Constants.NOT_ACCEPT_DEMAND);
                        break;
                }
            }
        });
        mMenuAlert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    private void buildSenderFinishMenu() {
        AlertDialog.Builder finishAlert = new AlertDialog.Builder(mActivity);
        finishAlert.setTitle("Finalizar?");
        finishAlert.setPositiveButton("Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setDemandStatus(Constants.FINISH_STATUS);
            }
        });
        finishAlert.setNegativeButton("Não Finalizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startReasonDialog("Marcar como Não Finalizada?",
                        "Por favor, insira o motivo da opção escohida,  e em seguida clique em \"ok\" para enviá-la para o colaborador responsável",
                        Constants.MARK_AS_UNFINISHED_DEMAND);
            }
        });
        finishAlert.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        finishAlert.show();
    }

    private void showDueTime() {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mDueTimeTV,8, 18, 2, TypedValue.COMPLEX_UNIT_SP);
        if (mDemand.getStatus().equals(Constants.FINISH_STATUS)) {
            int color = ContextCompat.getColor(this,R.color.secondary_text);
            mDueTimeTV.setTextColor(color);
        } else if (mDemand.isLate() == 1) {
            int color = ContextCompat.getColor(this,R.color.red);
            mDueTimeTV.setTextColor(color);
        }
        mDueTimeTV.setText(mDemand.getDueDate() + " " + mDemand.getDueTime());
    }

    private void hideDueTime() {
        mDueTimeTV.setVisibility(View.INVISIBLE);
    }

    private void startTransferActivity(int type) {
        Intent intent = new Intent(mActivity, TransferActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.INTENT_DEMAND, mDemand);
        intent.putExtra(Constants.INTENT_BUNDLE, bundle);
        startActivityForResult(intent,type);
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
        hideDueTime();
        switch (status) {
            case Constants.UNDEFINE_STATUS:
                senderUndefinedMenu();
                break;
            case Constants.REOPEN_STATUS:
                senderReopenMenu();
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
            case Constants.DEADLINE_REQUESTED_STATUS:
                senderDeadlineRequestedMenu();
                break;
            case Constants.DEADLINE_ACCEPTED_STATUS:
                senderDeadlineAcceptedMenu();
                break;
            case Constants.CANCEL_REQUESTED_STATUS:
                senderCancelRequestedMenu();
                break;
            case Constants.UNFINISH_STATUS:
                senderUnfinishedMenu();
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
            case Constants.REOPEN_STATUS:
                receiverReopenMenu();
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
            case Constants.CANCEL_REQUESTED_STATUS:
                receiverCancelRequestedMenu();
                break;
            case Constants.TRANSFER_STATUS:
                receiverTransferredMenu();
                break;
            case Constants.UNFINISH_STATUS:
                receiverUnfinishedMenu();
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
            case Constants.REOPEN_STATUS:
                superiorReopenMenu();
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
            case Constants.REJECT_STATUS:
                superiorRejectedMenu();
                break;
            case Constants.UNFINISH_STATUS:
                superiorUnfinishedMenu();
                break;
            case Constants.CANCEL_REQUESTED_STATUS:
                superiorCancelRequestedMenu();
                break;
            default:
                noMenu();
        }
    }

    private void senderCancelRequestedMenu() {
        showDemandReason(mDemand);
        mMenuTag = "sender_cancel_requested_menu";
    }

    private void senderUnfinishedMenu() {
        showDemandReason(mDemand);
        mMenuTag = "sender_unfinished_menu";
    }

    private void senderRejectedMenu() {
        if (mDemand.getReason() != null) {
            if (mDemand.getReason().getServerId() == Constants.REASON_MISSING_DOCUMENT) {
                mFabMenu.show();
            } else {
                noMenu();
            }
        }
        mMenuTag = "sender_reject_menu";
    }

    private void senderDoneMenu() {
        mFabMenu.show();
        mMenuTag = "sender_done_menu";
    }

    private void senderAcceptedMenu() {
        mFabMenu.show();
        mMenuTag = "sender_accepted_menu";
    }

    private void senderUndefinedMenu() {
        mFabMenu.show();
        mMenuTag = "sender_undefined_menu";
    }

    private void senderReopenMenu() {
        mFabMenu.show();
        mMenuTag = "sender_reopen_menu";
    }

    private void senderDeadlineRequestedMenu() {
        mFabMenu.show();
        mMenuTag = "sender_deadline_requested_menu";
    }

    private void senderDeadlineAcceptedMenu() {
        mFabMenu.show();
        mMenuTag = "sender_deadline_accepted_menu";
    }

    private void superiorCancelRequestedMenu() {
        showDemandReason(mDemand);
        mMenuTag = "superior_cancel_requested_menu";
    }

    private void superiorUnfinishedMenu() {
        showDemandReason(mDemand);
        mMenuTag = "superior_unfinished_menu";
    }

    private void superiorAcceptedMenu() {
        mMenuTag = "superior_accepted_menu";
    }

    private void superiorUndefinedMenu() {
        showDemandReason(mDemand);
        mMenuTag = "superior_undefined_menu";
    }

    private void superiorReopenMenu() {
        mMenuTag =  "superior_reopen_menu";
    }

    private void superiorTransferredMenu() {
        showDemandReason(mDemand);
        if (checkDemandAuthority(mDemand.getId(), mCurrentUser.getId())) {
            mFabMenu.show();
            mMenuTag = "superior_transferred_menu";
        } else {
            mMenuTag = "no_menu";
        }
    }

    private void superiorDeadlineRequestedMenu() {
        showDemandReason(mDemand);
        mFabMenu.show();
        mMenuTag = "superior_deadline_requested_menu";
        changeMenuState();
    }

    private void superiorDeadlineAcceptedMenu(){
        mMenuTag = "superior_deadline_accepted_menu";
    }

    private void superiorRejectedMenu() {
        mMenuTag = "superior_rejected_menu";
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
        CommonUtils.listAllAuthsDB(this);
        Log.d(TAG, "check authority:" + authorities.toString());

        return !authorities.isEmpty();
    }

    private void receiverDoneMenu() {
        mMenuTag = "receiver_done_menu";
    }

    private void receiverAcceptedMenu() {
        mFabMenu.show();
        mMenuTag = "receiver_accepted_menu";
    }

    private void receiverUnfinishedMenu() {
        showDemandReason(mDemand);
        mFabMenu.show();
        mMenuTag = "receiver_unfinished_menu";
    }

    private void receiverUndefinedMenu() {
        mFabMenu.show();
        mMenuTag = "receiver_undefined_menu";
        changeMenuState();
    }

    private void receiverReopenMenu() {
        mFabMenu.show();
        mMenuTag = "receiver_reopen_menu";
        changeMenuState();
    }

    private void receiverDeadlineRequestedMenu() {
        showDemandReason(mDemand);
        mFabMenu.show();
        mMenuTag = "receiver_deadline_requested_menu";
    }

    private void receiverDeadlineAcceptedMenu(){
        mFabMenu.show();
        mMenuTag = "receiver_deadline_accepted_menu";
    }

    private void receiverCancelRequestedMenu() {
        showDemandReason(mDemand);
        mFabMenu.show();
        mMenuTag = "receiver_cancel_requested_menu";
        changeMenuState();
    }

    private void receiverTransferredMenu(){
        mMenuTag = "receiver_transferred_menu";
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
            button.hide();
            button.setVisibility(View.GONE);
        }
        for (View view : mMenuViewList) {
            Log.d(TAG, "all views:" + view.getId());
            view.setVisibility(View.GONE);
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
                View view = mMenuViewList.get(j);
                if (btn.getId() == button.getId()) {
                    Log.d(TAG, "chosen buttons:" + btn.getId());
                    if(btn.isShown()) {
                        Log.d(TAG, "was showing...");
                        btn.hide();
                        title.setVisibility(View.GONE);
                        view.setVisibility(View.GONE);
                    } else {
                        Log.d(TAG, "wasn't showing...");
                        view.setVisibility(View.VISIBLE);
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
                               noMenu();
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
                    attemptDeadlineRequest(mDemand, predefinedReason);
                    break;
                case Constants.SUPERIOR_TRANSFER_DEMAND:
                    User user = (User) data.getSerializableExtra(Constants.INTENT_USER);
                    Log.d(TAG, user.toString());
                    attemptTransferDemand(mDemand.getId(), user.getId());
                    break;
                case Constants.CANCEL_REQUEST_DEMAND:
                    predefinedReason = (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                    attemptCancelRequest(mDemand, predefinedReason);
                    break;
                case Constants.CANCEL_ACCEPT_DEMAND:
                    predefinedReason = (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                    attemptNotAcceptCancelRequest(mDemand, predefinedReason);
                    break;
                case Constants.MARK_AS_UNFINISHED_DEMAND:
                    predefinedReason = (PredefinedReason) data.getSerializableExtra(Constants.INTENT_REJECT_PREDEFINED_REASON);
                    attemptMarkAsUnfinished(mDemand, predefinedReason);
                    break;
            }
        } else {
            Log.e(TAG, "OnActivityResult failed!!!");
        }
    }

    private void attemptMarkAsUnfinished(Demand demand, PredefinedReason predefinedReason) {
        if (mMarkAsUnfinishedTask == null && CommonUtils.isOnline(mActivity)) {
            mMarkAsUnfinishedTask = new MarkAsUnfinishedTask(demand.getId(), predefinedReason);
            mMarkAsUnfinishedTask.execute();
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void attemptCancelRequest(Demand demand, PredefinedReason predefinedReason) {
        if (mCancelTask == null && CommonUtils.isOnline(mActivity)) {
            mCancelTask = new CancelTask(demand.getId(), predefinedReason);
            mCancelTask.execute("request");
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void attemptNotAcceptCancelRequest(Demand demand, PredefinedReason predefinedReason) {
        if (mCancelTask == null && CommonUtils.isOnline(mActivity)) {
            mCancelTask = new CancelTask(demand.getId(), predefinedReason);
            mCancelTask.execute("not-accept");
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void attemptTransferDemand(int demandId, int userId) {
        if (mAdminTransferTask == null && CommonUtils.isOnline(mActivity)){
            mAdminTransferTask = new AdminTransferTask(demandId, userId);
            mAdminTransferTask.execute();
        } else {
            Snackbar.make(mFabMenu, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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

    private void attemptDeadlineAccept(Demand demand, String option) {
            if (mDeadlineAcceptedTask == null && CommonUtils.isOnline(mActivity)) {
                mDeadlineAcceptedTask = new DeadlineAcceptedTask(demand.getId());
                mDeadlineAcceptedTask.execute(option);
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

    private void showDemandPrior(String prior) {
        //Log.e(TAG, "Prior name:" + CommonUtils.getPriorName(prior, this));
        mPriorTV.setText(CommonUtils.getPriorName(prior, this));
        mPriorTV.setVisibility(View.VISIBLE);
    }

    private void handleDemandType(Demand demand) {
        if (demand.getType() != null) {
            mTypeTV.setText(demand.getType().getTitle());
            showDemandPrior(demand.getType().getPriority());
            showDemandComplexity(demand.getType().getComplexity());
        }else {
            mTypeTV.setVisibility(View.GONE);
            mPriorTV.setVisibility(View.GONE);
            mComplexityTV.setVisibility(View.GONE);
        }
    }

    private void handleDemandLate(Demand demand) {
        if (demand.isLate() == 1 && !(mCurrentUser.getId() == demand.getSender().getId() && mPage == Constants.SENT_PAGE)) {
            mLateTV.setVisibility(View.VISIBLE);
        } else {
            mLateTV.setVisibility(View.GONE);
        }
    }

    private void showDemandComplexity(int complexity) {
        mComplexityTV.setText("" + complexity);
        mComplexityTV.setVisibility(View.VISIBLE);
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
                drawable = R.drawable.ic_check_circle_black_24dp;
                color = ContextCompat.getColor(this,R.color.darkgreen);
                break;
            case Constants.FINISH_STATUS: // Done.
                description = R.string.finished_demand_info;
                drawable = R.drawable.ic_assignment_turned_in_white_24dp;
                color = ContextCompat.getColor(this,R.color.dGreen);
                break;
            case Constants.UNFINISH_STATUS: // Unfinished.
                description = R.string.unfinished_demand_info;
                drawable = R.drawable.ic_assignment_return_black_24dp;
                color = ContextCompat.getColor(this, R.color.Black);
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
            case Constants.CANCEL_REQUESTED_STATUS: //Request to cancel.
                description = R.string.cancel_requested_demand_info;
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(this,R.color.OrangeRed);
                break;
            case Constants.CANCEL_ACCEPTED_STATUS: //Cancelled
                description = R.string.cancel_accepted_demand_info;
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
                description = R.string.reopen_demand_info;
                drawable = R.drawable.ic_settings_backup_restore_black_24dp;
                color = ContextCompat.getColor(this,R.color.Orange);
                break;
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

        color = ContextCompat.getColor(this, R.color.icons);

        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = getDrawable(drawable);
        } else {
            objDrawable = getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        mStatusTV.setText(getString(description) + getPostponedCounter(mDemand));
        mStatusTV.setCompoundDrawablesWithIntrinsicBounds(null,null, objDrawable,null);
        mStatusTV.getCompoundDrawables()[2].setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private String getPostponedCounter(Demand demand) {
        String s = "";
        if(demand.getStatus().equals(Constants.DEADLINE_ACCEPTED_STATUS)) {
            s = " (" + demand.getPostponed() + "x)";
        }
        return s;
    }

    private void showDemandReason(Demand demand) {
        if (demand.getReason() != null) {
            mReasonTV.setVisibility(View.VISIBLE);
        } else {
            //Log.d(TAG, "Reason null");
            mReasonTV.setVisibility(View.GONE);
        }
    }

    private void hideDemandReason() {
            mReasonTV.setVisibility(View.GONE);
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

    public void setReminder(Demand demand, int postponeTime) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, postponeTime);
        long reminderTimeInMillis = c.getTimeInMillis();
        CommonUtils.setAlarm(
                this,
                reminderTimeInMillis,
                demand,
                Constants.REMIND_ME_ALARM_TAG,
                mPage,
                mMenuType
        );
        String reminderDate = CommonUtils.convertMillisToDate(reminderTimeInMillis);
        String reminderTime = CommonUtils.convertMillisToTime(reminderTimeInMillis);
        Snackbar.make(mFabLater, "Lembrete adicionado para "
                + reminderDate
                + " "
                + reminderTime, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mFabMenu.getId()) {
            rotate(mTurned);
            switch (mMenuTag) {
                /* receiver section --- */
                case "receiver_unfinished_menu":
                case "receiver_accepted_menu":
                    showItems(asList(mFabDone, mFabLater, mFabTransfer, mFabDeadline));
                    break;
                case "receiver_reopen_menu":
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
                case "receiver_cancel_requested_menu":
                    rotate(mTurned);
                    buildReceiverCancelRequestedMenu();
                    break;
                case "receiver_transferred_menu":
                case "receiver_done_menu":
                case "receiver_finished_menu":
                    noMenu();
                    break;
                /* --- receiver section */

                /* sender section --- */
                case "sender_done_menu":
                    showItems(asList(mFabFinish));
                    break;
                case "sender_reject_menu":
                    showItems(asList(mFabReopen));
                    break;
                case "sender_deadline_requested_menu":
                case "sender_deadline_accepted_menu":
                case "sender_accepted_menu":
                case "sender_reopen_menu":
                case "sender_undefined_menu":
                    showItems(asList(mFabNo));
                    break;
                /* --- sender section */

                /* superior section --- */
                case "superior_transferred_menu":
                    showItems(asList(mFabTransfer, mFabReject, mFabLater));
                    break;
                case "superior_deadline_requested_menu":
                    rotate(mTurned);
                    buildSuperiorDeadlineRequestedMenu();
                    break;
                case "superior_unfinished_menu":
                case "superior_accepted_menu":
                case "superior_reopen_menu":
                case "superior_undefined_menu":
                case "superior_deadline_accepted_menu":
                case "superior_rejected_menu":
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
    }

    private void buildSuperiorDeadlineRequestedMenu() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle(mDemand.getReceiver().getName());
        dialog.setMessage("Aceitar solicitação de aumento de prazo?");
        dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptDeadlineAccept(mDemand, "accept");
            }
        });
        dialog.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptDeadlineAccept(mDemand, "not-accept");
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

    private void buildReceiverCancelRequestedMenu() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle(mDemand.getReceiver().getName());
        dialog.setMessage("Aceita a solicitação de cancelamento?");
        dialog.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setDemandStatus(Constants.CANCEL_ACCEPTED_STATUS);
            }
        });
        dialog.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mActivity, RejectDialogActivity.class);
                intent.putExtra("title", "Não Aceita?");
                intent.putExtra("message", "Por favor, insira o motivo, e em seguida clique em \"OK\" para abortar o cancelamento.");
                startActivityForResult(intent,Constants.CANCEL_ACCEPT_DEMAND);
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

    public String generateActivityTitle() {
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
                    case Constants.CANCEL_ACCEPTED_STATUS:
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
        return activityTitle;
    }

    public String getToday() {
        Calendar c = Calendar.getInstance();
        return CommonUtils.convertMillisToDate(c.getTimeInMillis())
                + " "
                + CommonUtils.convertMillisToTime(c.getTimeInMillis());
    }

    public String getDaysLeft() {
        Calendar calendar = Calendar.getInstance();
        long dueTime = mDemand.getDueTimeInMillis();
        long result;
        float days;
        String string;

        if ( dueTime > calendar.getTimeInMillis()) {
            result = dueTime - calendar.getTimeInMillis();
            days = TimeUnit.DAYS.convert(result, TimeUnit.MILLISECONDS);
            string = "FALTAM " + (int) days + 1 + " DIAS";
        } else {
            result = calendar.getTimeInMillis() - dueTime;
            days = TimeUnit.DAYS.convert(result, TimeUnit.MILLISECONDS);
            string = "PASSARAM " + (int) days + 1 + " DIAS";
        }

        return string;
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
                        case Constants.CANCEL_ACCEPTED_STATUS:
                            mShouldCancelAlarm = true;
                            restoreMenuState();
                            showDemandReason(demandResponse);
                            handleMenu(mMenuType,demandResponse.getStatus());
                            message = "Demanda Cancelada com Sucesso.";
                            break;
                        case Constants.REOPEN_STATUS:
                            handleMenu(mMenuType, demandResponse.getStatus());
                            message = "Demanda Reaberta com Sucesso.";
                            break;
                        case Constants.REJECT_STATUS:
                            mShouldCancelAlarm = true;
                            message = "Demanda Indeferida com Sucesso.";
                            break;
                        case Constants.DONE_STATUS:
                            handleMenu(mMenuType,demandResponse.getStatus());
                            message = "Demanda Concluída com Sucesso.";
                            break;
                        case Constants.FINISH_STATUS:
                            handleMenu(mMenuType,demandResponse.getStatus());
                            message = "Demanda Finalizada com Sucesso.";
                            int color = ContextCompat.getColor(mActivity,R.color.secondary_text);
                            mDueTimeTV.setTextColor(color);
                            mShouldCancelAlarm = true;
                            break;
                        default:
                            message = "Feito.";
                    }

                    // In case status changed from postponed to another, cancel alarm.
                    if(mShouldCancelAlarm){
                        CommonUtils.cancelAllAlarms(demandResponse, mActivity);
                        mShouldCancelAlarm = false;
                        Log.d(TAG, "(StatusTask) should cancel alarm was true!");
                    }

                    CommonUtils.updateDemandDB(Constants.UPDATE_STATUS, demandResponse, mActivity);
                    showDemandStatus(demandResponse.getStatus());
                    Snackbar.make(mFabYes, message, Snackbar.LENGTH_LONG).show();
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabMenu, R.string.server_error, Snackbar.LENGTH_LONG)
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
                            handleMenu(mMenuType,Constants.REJECT_STATUS);
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
                            CommonUtils.updateDemandDB(Constants.UPDATE_STATUS, demandResponse, mActivity);
                            CommonUtils.cancelAllAlarms(demandResponse, mActivity);
                            showDemandReason(demandResponse);
                            handleMenu(mMenuType,Constants.TRANSFER_STATUS);
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
                            rotate(mTurned);
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

    public class DeadlineAcceptedTask extends AsyncTask<String, Void, String> {
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
        protected String doInBackground(String... strings) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            return CommonUtils.POST("/send/deadline-" + strings[0], values);
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
                            CommonUtils.updateDemandDB(Constants.UPDATE_STATUS, demandResponse, mActivity);
                            break;
                        default:
                            handleMenu(mMenuType, demandResponse.getStatus());
                            message = "Solicitação não acieta.";
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

    public class AdminTransferTask extends AsyncTask<Void, Void, String> {
        private long demandId;
        private long userId;

        public AdminTransferTask(long demandId, long userId) {
            this.demandId = demandId;
            this.userId = userId;
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
            values.put("new_user_id", userId);
            return CommonUtils.POST("/send/transfer", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mAdminTransferTask = null;

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
                        case Constants.UNDEFINE_STATUS:
                            mDemand = demandResponse;
                            message = "Demanda transferida com Sucesso.";
                            CommonUtils.cancelAllAlarms(demandResponse, mActivity);
                            CommonUtils.updateDemandDB(Constants.UPDATE_STATUS, demandResponse, mActivity);
                            handleMenu(mMenuType,Constants.UNDEFINE_STATUS);
                            Log.d(TAG, "(transfer) new receiver: " + demandResponse.getReceiver().toString());
                            mReceiverTV.setText("Para: " + demandResponse.getReceiver().getName());
                            hideDemandReason();
                            break;
                        default:
                            message = "A demanda não pôde ser transferida.";
                    }

                    Snackbar.make(mFabMenu, message, Snackbar.LENGTH_LONG).show();
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabYes, R.string.server_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }

    public class CancelTask extends AsyncTask<String, Void, String> {
        private long demandId;
        private PredefinedReason predefinedReason;

        public CancelTask(long demandId, PredefinedReason predefinedReason) {
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
        protected String doInBackground(String... strings) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            values.put("reason_id", predefinedReason.getServerId());
            values.put("reason_title", predefinedReason.getTitle());
            values.put("reason_description", predefinedReason.getDescription());
            return CommonUtils.POST("/send/cancel-" + strings[0], values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mCancelTask = null;

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
                        case Constants.CANCEL_REQUESTED_STATUS:
                            message = "Solicitação enviada com Sucesso.";
                            handleMenu(mMenuType,demandResponse.getStatus());
                            break;
                        default:
                            restoreMenuState();
                            showDemandReason(demandResponse);
                            message = "Solicitação recusada com Sucesso.";
                            handleMenu(mMenuType,demandResponse.getStatus());
                    }

                    CommonUtils.updateDemandDB(Constants.UPDATE_STATUS, demandResponse, mActivity);
                    showDemandStatus(demandResponse.getStatus());
                    showDemandReason(demandResponse);
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

    public class MarkAsUnfinishedTask extends AsyncTask<String, Void, String> {
        private long demandId;
        private PredefinedReason predefinedReason;

        public MarkAsUnfinishedTask(long demandId, PredefinedReason predefinedReason) {
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
        protected String doInBackground(String... strings) {
            ContentValues values = new ContentValues();
            values.put("demand_id", demandId);
            values.put("reason_id", predefinedReason.getServerId());
            values.put("reason_title", predefinedReason.getTitle());
            values.put("reason_description", predefinedReason.getDescription());
            return CommonUtils.POST("/send/mark-as-unfinished", values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mMarkAsUnfinishedTask = null;

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
                        case Constants.UNFINISH_STATUS:
                            showDemandReason(demandResponse);
                            message = "Demanda marcada como Não Finalizada.";
                            handleMenu(mMenuType,demandResponse.getStatus());
                            break;
                        default:
                            message = "Algo saiu errado! Por favor, tente mais tarde.";
                            handleMenu(mMenuType,demandResponse.getStatus());
                    }

                    Snackbar.make(mFabMenu, message, Snackbar.LENGTH_LONG).show();
                    showDemandStatus(demandResponse.getStatus());
                    Log.e(TAG, "demand reason: " + demandResponse.getReason().getTitle());
                } else {
                    throw new JSONException("success hit false!");
                }
            } catch (JSONException e) {
                Snackbar.make(mFabMenu, R.string.server_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    }
}
