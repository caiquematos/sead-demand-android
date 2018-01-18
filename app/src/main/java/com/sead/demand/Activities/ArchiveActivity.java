package com.sead.demand.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sead.demand.Adapters.DemandAdapter;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Databases.MyDBManager;
import com.sead.demand.Entities.Authority;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.User;
import com.sead.demand.Interfaces.RecyclerClickListener;
import com.sead.demand.R;
import com.sead.demand.RecycerSupport.RecyclerTouchListener;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArchiveActivity extends AppCompatActivity {
    private String TAG = getClass().getSimpleName();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private DemandAdapter mDemandAdapter;
    private List<Demand> mDemandSet;
    private int mPage;
    private boolean mMenuEnabled;
    private Menu mMenu;
    private SharedPreferences mPrefs;
    private User mCurrentUser;

    public ArchiveActivity() {
        this.mPage = Constants.ARCHIVE_PAGE;
        this.mMenuEnabled = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.demand_recycler);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        mPrefs = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);

        // Fetch info about current user logged
        try {
            JSONObject userJson = new JSONObject(mPrefs.getString(Constants.USER_PREFERENCES, ""));
            mCurrentUser = User.build(userJson);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get user from preferences!!!");
        }

        if (mCurrentUser.getId() != -1){
            List<Authority> authorities = fetchAuthoritiesBySuperior(mCurrentUser.getId());
            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUser.getId(), authorities);
            loadArchivedDemands(mCurrentUser.getId(), usersUnderMySupervision);
        } else Log.e(TAG, "Logged User id not found!");

        implementRecyclerViewClickListener();
    }

    public void implementRecyclerViewClickListener() {
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView,
                new RecyclerClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if(mMenuEnabled){
                            onListItemSelect(position, view);
                            Log.e(TAG, "On click action mode NOT null");
                        } else {
                            mDemandAdapter.showDemand(view, position);
                            Log.e(TAG, "On click action mode null");
                        }
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        onListItemSelect(position, view);
                    }
                }));
    }

    public void onListItemSelect(int position, View view) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); // Vibrate.
        mDemandAdapter.toggleSelection(position);
        boolean hasCheckedItems = mDemandAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && !mMenuEnabled){ // If there are items selected, activate action mode.
           setUnarchiveMenuVisible();
        } else if (!hasCheckedItems && mMenuEnabled){ // If there are no items now, deactivate action mode.
            mMenuEnabled = false;
            setUnarchiveMenuInvisible();
        }

        // If there are items and action mode already activated, then count them.
        if(mMenuEnabled){
            String selectedString;
            if (mDemandAdapter.getSelectedCount() > 1)
                selectedString = "selecionados";
            else selectedString = "selecionado";

            setTitle(mDemandAdapter.getSelectedCount() + " " + selectedString);
        }
    }

    private void setUnarchiveMenuVisible() {
        mMenuEnabled = true;
        mMenu.setGroupVisible(R.id.unarchive_action_mode_items,true);
    }

    public void setUnarchiveMenuInvisible() {
        mMenu.setGroupVisible(R.id.unarchive_action_mode_items,false);
        mMenuEnabled = false;
        setTitle("Demandas Arquivadas");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentUser.getId() != -1){
            List<Authority> authorities = fetchAuthoritiesBySuperior(mCurrentUser.getId());
            List<User> usersUnderMySupervision = fetchUsersUnderMySupervision(mCurrentUser.getId(), authorities);
            loadArchivedDemands(mCurrentUser.getId(), usersUnderMySupervision);
        } else Log.e(TAG, "Logged User id not found!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unarchive, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.item_unarchive:
                unarchiveRows();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void unarchiveRows() {
        SparseBooleanArray selected = mDemandAdapter.getSelectedIds();//Get selected ids
        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)
                    &&  CommonUtils.archiveDemand(mDemandSet.get(selected.keyAt(i)),this,false) > 0) {
                Log.e(TAG, "Position:" + selected.keyAt(i) + " Demanda:" + mDemandSet.get(i).getSubject());
                //If current id is selected remove the item via key
                mDemandSet.remove(selected.keyAt(i));
                mDemandAdapter.notifyDataSetChanged();//notify adapter
            }
        }
        Toast.makeText(this, selected.size() + " ítens desarquivados.", Toast.LENGTH_SHORT).show();//Show Toast
       setUnarchiveMenuInvisible();
    }

    // This is necessary, because even though current user is not a direct superior of certain
    // receivers, they can be the receiver's superior's superior.
    // So authority has a list of each user which this current user has authority over.
    private List<Authority> fetchAuthoritiesBySuperior(int superiorId) {
        List<Authority> authorities;

        String selection = FeedReaderContract.AuthorityEntry.COLUMN_NAME_SUPERIOR + " = ?";

        String[] args = {
                "" + superiorId,
        };

        MyDBManager myDBManager = new MyDBManager(this);
        authorities = myDBManager.searchAuthorities(selection,args);

        return authorities;
    }

    private List<User> fetchUsersUnderMySupervision(int superiorId, List<Authority> authorities) {
        List<User> users;
        ArrayList<String> argsArray = new ArrayList<>();

        String selection = FeedReaderContract.UserEntry.COLUMN_NAME_USER_SUPERIOR + " = ?";
        argsArray.add("" + superiorId);

        if (authorities != null) {
            Log.d(TAG, "Authorities different from null");
            for(int i = 0; i < authorities.size(); i++){
                selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.UserEntry.COLUMN_NAME_USER_ID + " = ?");
                argsArray.add("" + authorities.get(i).getUser());
            }
        }

        String[] args = new String[argsArray.size()];
        for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
        Log.e(TAG, "args fetch user:" + argsArray.toString());

        MyDBManager myDBManager = new MyDBManager(this);
        users = myDBManager.searchUsers(selection,args);

        return users;
    }

    private void loadArchivedDemands(int currentUserId, List<User> usersUnderMySupervision) {
        String selection = "";
        ArrayList<String> argsArray = new ArrayList<>();

        selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_ARCHIVE + " = ?");
        argsArray.add("" + true);
        selection = selection.concat(" AND ");
        selection = selection.concat("(");

        if (usersUnderMySupervision != null && !usersUnderMySupervision.isEmpty()) {
            for (int i = 0; i < usersUnderMySupervision.size(); i++) {
                if (i > 0) selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ?");
                argsArray.add("" + usersUnderMySupervision.get(i).getId());
                selection = selection.concat(" OR ");
                selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " = ?");
                argsArray.add("" + usersUnderMySupervision.get(i).getId());
            }
        }

        selection = selection.concat(" OR ");
        selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_SENDER_ID + " = ?");
        argsArray.add("" + currentUserId);
        selection = selection.concat(" OR ");
        selection = selection.concat(FeedReaderContract.DemandEntry.COLUMN_NAME_RECEIVER_ID + " = ?");
        argsArray.add("" + currentUserId);
        selection = selection.concat(")");


        Log.e(TAG, "selection:" + selection.toString());

        String[] args = new String[argsArray.size()];
        for (int j = 0; j < args.length; j++) args[j] = argsArray.get(j);
        Log.e(TAG, "args:" + argsArray.toString());

        MyDBManager myDBManager = new MyDBManager(this);
        mDemandSet = myDBManager.searchDemands(selection,args);

        Log.e(TAG, "Demands:" + mDemandSet.size());

        mRecyclerView.setLayoutManager(mLayoutManager);

        mDemandAdapter = new DemandAdapter(mDemandSet,this, mPage);
        mRecyclerView.setAdapter(mDemandAdapter);
        mDemandAdapter.notifyDataSetChanged();
    }

}
