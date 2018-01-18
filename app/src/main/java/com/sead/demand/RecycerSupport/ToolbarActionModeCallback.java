package com.sead.demand.RecycerSupport;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.sead.demand.Adapters.DemandAdapter;
import com.sead.demand.Fragments.ReceivedFragment;
import com.sead.demand.Fragments.SentFragment;
import com.sead.demand.Fragments.SuperiorFragment;
import com.sead.demand.R;

/**
 * Created by caique on 28/07/2017.
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
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.main_archive), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.main_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.main_archive).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.main_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_archive:
                archive();
                break;
            case R.id.main_delete:
                delete();
                break;
        }
        return false;
    }

    private void archive() {
        //Check if current action mode is from Receiver, Sent or Superior Fragment.
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
    }

    private void delete() {
        //Check if current action mode is from Receiver, Sent or Superior Fragment.
        if (mTabPosition == 0) {
            Log.e(TAG,"On fragment:" + mTabPosition );
            ((ReceivedFragment) mFragment).deleteRows();
        } else if (mTabPosition == 1){
            Log.e(TAG,"On fragment:" + mTabPosition );
            ((SentFragment) mFragment).deleteRows();
        } else {
            Log.e(TAG,"On fragment:" + mTabPosition );
            ((SuperiorFragment) mFragment).deleteRows();
        }
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
