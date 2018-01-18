package com.sead.demand.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sead.demand.Adapters.DemandAdapter;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.User;
import com.sead.demand.Interfaces.RecyclerClickListener;
import com.sead.demand.R;
import com.sead.demand.RecycerSupport.RecyclerTouchListener;
import com.sead.demand.Tools.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    public String TAG = getClass().getSimpleName();
    private MyDBManager mDBManager;
    private RecyclerView.LayoutManager mLMSubject;
    private RecyclerView.LayoutManager mLMDescription;
    private RecyclerView.LayoutManager mLMReceiver;
    private RecyclerView.LayoutManager mLMSender;
    private RecyclerView mSubjectView;
    private RecyclerView mDescriptionView;
    private RecyclerView mReceiverView;
    private RecyclerView mSenderView;
    private List<Demand> mDemandsBySubject;
    private List<Demand> mDemandsByDescription;
    private List<Demand> mDemandsByReceiver;
    private List<Demand> mDemandsBySender;
    private DemandAdapter mSubjectAdapter;
    private DemandAdapter mDescriptionAdapter;
    private DemandAdapter mReceiverAdapter;
    private DemandAdapter mSenderAdapter;
    private TextView mTVSubject;
    private TextView mTVDescription;
    private TextView mTVReceiver;
    private TextView mTVSender;
    private int mPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDBManager = new MyDBManager(this);
        init();

        handleIntent(getIntent());
    }

    private void init() {
        mPage = Constants.SEARCH_PAGE;
        mSubjectView = (RecyclerView) findViewById(R.id.search_subject_list);
        mDescriptionView = (RecyclerView) findViewById(R.id.search_description_list);
        mReceiverView = (RecyclerView) findViewById(R.id.search_receiver_list);
        mSenderView = (RecyclerView) findViewById(R.id.search_sender_list);
        mTVSubject = (TextView) findViewById(R.id.search_subject_title);
        mTVDescription = (TextView) findViewById(R.id.search_description_title);
        mTVReceiver = (TextView) findViewById(R.id.search_receiver_title);
        mTVSender = (TextView) findViewById(R.id.search_sender_title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        init();
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    private void search(String query) {
        Log.d(TAG, "query: " + query.toString());
        setTitle("Busca \"" + query + "\"");

        mDemandsBySubject = searchDemandsBySubject(query);
        handleTitle(mDemandsBySubject.size(), mTVSubject);
        Log.d(TAG, "search subject: " + mDemandsBySubject.size());

        mDemandsByDescription = searchDemandsByDescription(query);
        handleTitle(mDemandsByDescription.size(), mTVDescription);
        Log.d(TAG, "search description: " + mDemandsByDescription.size());

        List<User> users = searchUserByNameOrEmail(query);
        Log.d(TAG, "search users: " + users.size());

        mDemandsByReceiver = searchDemandsByReceivers(users);
        handleTitle(mDemandsByReceiver.size(), mTVReceiver);
        Log.d(TAG, "search demands receivers: " + mDemandsByReceiver.size());

        mDemandsBySender = searchDemandsBySenders(users);
        handleTitle(mDemandsBySender.size(), mTVSender);
        Log.d(TAG, "search demands senders: " + mDemandsBySender.size());

        setAdapter(mDemandsBySubject, mDemandsByDescription, mDemandsByReceiver, mDemandsBySender);

        implementRecyclerViewClickListener(mSubjectView, mSubjectAdapter);
        implementRecyclerViewClickListener(mDescriptionView, mDescriptionAdapter);
        implementRecyclerViewClickListener(mReceiverView, mReceiverAdapter);
        implementRecyclerViewClickListener(mSenderView, mSenderAdapter);
    }

    public void implementRecyclerViewClickListener(RecyclerView rv, final DemandAdapter da) {
        rv.addOnItemTouchListener(new RecyclerTouchListener(this, rv,
                new RecyclerClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        da.showDemand(view, position);
                        Log.e(TAG, "On click action mode null");
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        //onListItemSelect(position, view);
                    }
                }));
    }

    private void handleTitle(int size, TextView textView) {
        if (size == 0)
            textView.setVisibility(View.GONE);
        else
            textView.setVisibility(View.VISIBLE);
    }

    private void setAdapter(List<Demand> mDemandsBySubject, List<Demand> mDemandsByDescription,
                            List<Demand> mDemandsByReceiver, List<Demand> mDemandsBySender) {

        if (mDemandsBySubject != null && mDemandsBySubject.size() != 0) {
            Log.d(TAG, "subj setAdapter");
            mSubjectAdapter = new DemandAdapter(mDemandsBySubject,this, mPage);
            adapt(mSubjectView, mSubjectAdapter);
        }

        if (mDemandsByDescription != null && mDemandsByDescription.size() != 0) {
            Log.d(TAG, "desc setAdapter");
            mDescriptionAdapter = new DemandAdapter(mDemandsByDescription,this, mPage);
            adapt(mDescriptionView, mDescriptionAdapter);
        }

        if (mDemandsByReceiver != null && mDemandsByReceiver.size() != 0) {
            Log.d(TAG, "rece setAdapter");
            mReceiverAdapter = new DemandAdapter(mDemandsByReceiver,this, mPage);
            adapt(mReceiverView, mReceiverAdapter);
        }

        if (mDemandsBySender != null && mDemandsBySender.size() != 0) {
            Log.d(TAG, "send setAdapter");
            mSenderAdapter = new DemandAdapter(mDemandsBySender,this, mPage);
            adapt(mSenderView, mSenderAdapter);
        }
    }

    private void adapt(RecyclerView rv, DemandAdapter da) {
        Log.d(TAG, "on adapt");
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setAdapter(da);
    }

    private List<Demand> searchDemandsBySubject(String query) {
        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_SUBJECT + " LIKE ?";
        Log.d(TAG, "selection: " + selection);
        String[] args = new String[] {"%" + query + "%"};
        return mDBManager.searchDemands(selection, args);
    }

    private List<Demand> searchDemandsByDescription(String query) {
        String selection = FeedReaderContract.DemandEntry.COLUMN_NAME_DESCRIPTION + " LIKE ?";
        String[] args = new String[] {"%" + query + "%"};
        return mDBManager.searchDemands(selection, args);
    }

    private List<User> searchUserByNameOrEmail(String query) {
        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_NAME + " LIKE ?"
                + " OR "
                + FeedReaderContract.UserEntry.COLUMN_NAME_USER_EMAIL + " LIKE ?";
        String[] args = new String[] {"%" + query + "%", "%" + query + "%"};
        return mDBManager.searchUsers(selection, args);
    }

    private List<Demand> searchDemandsByReceivers(List<User> users) {
        String selection = "";
        ArrayList<String> argsArray = new ArrayList<>();

        if (users.size() != 0) {
            // Run into every user found and look for every demand related.
            for (int i=0; i < users.size(); i++) {
                if(i > 0)  selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " = ?");
                argsArray.add("" + users.get(i).getId());
            }

            Log.e(TAG, "selection:" + selection.toString());

            // Converting array list into simple array.
            String[] args = new String[argsArray.size()];
            for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
            Log.e(TAG, "args:" + argsArray.toString());

            return mDBManager.searchDemands(selection, args);
        } else {
            return new ArrayList<>();
        }
    }

    private List<Demand> searchDemandsBySenders(List<User> users) {
        String selection = "";
        ArrayList<String> argsArray = new ArrayList<>();

        if (users.size() != 0) {
            // Run into every user found and look for every demand related.
            for (int i=0; i < users.size(); i++) {
                if(i > 0)  selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ?");
                argsArray.add("" + users.get(i).getId());
            }

            Log.e(TAG, "selection:" + selection.toString());

            // Converting array list into simple array.
            String[] args = new String[argsArray.size()];
            for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
            Log.e(TAG, "args:" + argsArray.toString());

            return mDBManager.searchDemands(selection, args);
        } else {
            return new ArrayList<>();
        }
    }
}
