package com.sead.demand.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sead.demand.Entities.DemandType;
import com.sead.demand.R;

import java.util.List;

/**
 * Created by caiqu on 12/22/2017.
 */

public class DemandTypeAdapter extends RecyclerView.Adapter<DemandTypeAdapter.ViewHolder> {
    private String TAG = getClass().getSimpleName();
    private List<DemandType> demandTypeList;
    private Context context;
    private SparseBooleanArray mSelectedItemsIds;
    private SparseBooleanArray mGeneralSelectedItems;

    public DemandTypeAdapter(List<DemandType> demandTypeList, SparseBooleanArray generalSelectedItems, Context context) {
        this.demandTypeList = demandTypeList;
        this.context = context;
        this.mSelectedItemsIds = new SparseBooleanArray();
        this.mGeneralSelectedItems = generalSelectedItems;
    }

    @Override
    public DemandTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demand_type_adapter,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DemandTypeAdapter.ViewHolder holder, final int position) {
        final DemandType demandType = demandTypeList.get(position);
        holder.getCheckBox().setText(demandType.getTitle());

        if (mGeneralSelectedItems.get((int) demandType.getId())) {
            holder.getCheckBox().setChecked(true);
        } else {
            holder.getCheckBox().setChecked(false);
        }

        holder.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSelectedItemsIds.put((int) demandType.getId(),isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return demandTypeList.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }
    }
}
