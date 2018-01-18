package com.sead.demand.Fragments;

import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

import com.sead.demand.Adapters.DemandAdapter;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Interfaces.RecyclerClickListener;
import com.sead.demand.RecycerSupport.RecyclerTouchListener;
import com.sead.demand.RecycerSupport.ToolbarActionModeCallback;
import com.sead.demand.Tools.CommonUtils;

import java.util.List;

/**
 * Created by caique on 31/07/2017.
 */

public abstract class DemandFragment extends Fragment {
    protected String TAG = getClass().getSimpleName();
    protected RecyclerView mRecyclerView;
    protected ActionMode mActionMode;
    protected DemandAdapter mAdapter;
    protected List<Demand> mDemandSet;
    protected int mPage;

    public void implementRecyclerViewClickListener() {
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView,
                new RecyclerClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if(mActionMode != null){
                            onListItemSelect(position, view);
                            Log.e(TAG, "On click action mode NOT null");
                        } else {
                            mAdapter.showDemand(view, position);
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
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        mAdapter.toggleSelection(position);
        boolean hasCheckedItems = mAdapter.getSelectedCount() > 0;

        Log.e(TAG, "hasCheckedItems:" + hasCheckedItems);
        if (mActionMode == null)
            Log.e(TAG, "Action Mode null");
        else Log.e(TAG, "Action Mode not null");

        if (hasCheckedItems && mActionMode == null){ // If there are items selected, activate action mode.
            mActionMode = getActivity().startActionMode(new ToolbarActionModeCallback(this, mAdapter,mPage));
        } else if (!hasCheckedItems && mActionMode != null){ // If there are no items now, deactivate action mode.
            mActionMode.finish();
            setNullToActionMode();
        }

        // If there are items and action mode already activated, then count them.
        if(mActionMode != null){
            String selectedString;
            if (mAdapter.getSelectedCount() > 1)
                selectedString = "selecionados";
            else selectedString = "selecionado";

            mActionMode.setTitle(mAdapter.getSelectedCount() + " " + selectedString);
        }
    }

    //Set action mode null after use
    public void setNullToActionMode() {
        if (mActionMode != null){
            mActionMode = null;
            Log.e(TAG, "ActionMode changed to null");
        } else {
            Log.e(TAG, "ActionMode not changed!");
        }
    }

    //Archive selected rows
    public void archiveRows() {
        SparseBooleanArray selected = mAdapter.getSelectedIds();//Get selected ids
        //Loop all selected ids
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i)
                    &&  CommonUtils.archiveDemand(mDemandSet.get(selected.keyAt(i)),getContext(),true) > 0) {
                Log.e(TAG, "Position:" + selected.keyAt(i) + " Demanda:" + mDemandSet.get(i).getSubject());
                //If current id is selected remove the item via key
                mDemandSet.remove(selected.keyAt(i));
                mAdapter.notifyDataSetChanged();//notify adapter
            }
        }
        String toastInfo;
        if (selected.size() > 1)
            toastInfo = "itens arquivados";
        else
            toastInfo = "item arquivado";
        Toast.makeText(getActivity(), selected.size() + " " + toastInfo, Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use
    }

    // Delete selected rows. Obs.: this will delete demand only in this device.
    public void deleteRows() {
        SparseBooleanArray selected = mAdapter.getSelectedIds(); // Get selected ids.
        // Loop all selected ids.
        for (int i = (selected.size() - 1); i >= 0; i--) {
            if (selected.valueAt(i) && CommonUtils.deleteDemand(mDemandSet.get(selected.keyAt(i)), getContext()) > 0) {
                Log.e(TAG, "Position:" + selected.keyAt(i) + " Demanda:" + mDemandSet.get(i).getSubject());
                // If current id is selected, remove the item via key.
                mDemandSet.remove(selected.keyAt(i));
                mAdapter.notifyDataSetChanged(); // Notify adapter.
            }
        }
        String toastInfo;
        if (selected.size() > 1)
            toastInfo = "itens deletados";
        else
            toastInfo = "item deletado";
        Toast.makeText(getActivity(), selected.size() + " " + toastInfo, Toast.LENGTH_SHORT).show();//Show Toast
        mActionMode.finish();//Finish action mode after use
    }

    public void destroyActionMode(){
        if(mActionMode != null){
            mActionMode.finish();
            setNullToActionMode();
            Log.e(TAG, "on destroy action inside if");
        }
        Log.e(TAG, "on destroy action outside if");
    }

    @Override
    public void onPause() {
        super.onPause();
        destroyActionMode();
        Log.e(TAG, "on pause");
    }
}
