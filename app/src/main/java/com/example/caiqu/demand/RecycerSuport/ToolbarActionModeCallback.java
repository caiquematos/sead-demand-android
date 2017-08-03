package com.example.caiqu.demand.RecycerSuport;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.caiqu.demand.Activities.MainActivity;
import com.example.caiqu.demand.Adapters.DemandAdapter;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.Fragments.ReceivedFragment;
import com.example.caiqu.demand.Fragments.SentFragment;
import com.example.caiqu.demand.Fragments.SuperiorFragment;
import com.example.caiqu.demand.R;

import java.util.List;

/**
 * Created by caiqu on 28/07/2017.
 */

public class ToolbarActionModeCallback implements ActionMode.Callback{
    private String TAG = getClass().getSimpleName();
    private DemandAdapter mDemandAdapter;
    private int mTabPosition;
    private Fragment mFragment;

    public ToolbarActionModeCallback(Fragment fragment, DemandAdapter mDemandAdapter, int tabPosition) {
        this.mDemandAdapter = mDemandAdapter;
        this.mTabPosition = tabPosition - 1;
        this.mFragment = fragment;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_multiple_select,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu according to SDK Levels
        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.main_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.main_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.main_delete:
                //Check if current action mode is from Receiver, Sent or Superir Fragment.
                if (mTabPosition == 0) {
                    Log.e(TAG,"On fragment:" + mTabPosition );
                    ((ReceivedFragment) mFragment).archiveRows();
                } else if (mTabPosition == 1){
                    Log.e(TAG,"On fragment:" + mTabPosition );
                    ((SentFragment) mFragment).archiveRows();
                } else {
                    Log.e(TAG,"On fragment:" + mTabPosition );
                    ((SuperiorFragment) mFragment).archiveRows();
                }
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mDemandAdapter.removeSelection();
        Log.e(TAG,"On destroy action mode");

        //Check if current action mode is from Receiver, Sent or Superior Fragment.
        if (mTabPosition == 0) {
                ((ReceivedFragment) mFragment).setNullToActionMode();
        } else if (mTabPosition == 1){
            ((SentFragment) mFragment).setNullToActionMode();
        } else {
            ((SuperiorFragment) mFragment).setNullToActionMode();//delete selected rows
        }
    }
}
