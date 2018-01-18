package com.sead.demand.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sead.demand.Entities.PredefinedReason;
import com.sead.demand.R;

import java.util.List;

/**
 * Created by caiqu on 10/08/2017.
 */

public class ReasonAdapter extends RecyclerView.Adapter<ReasonAdapter.ViewHolder> {
    public String TAG = getClass().getSimpleName();
    private List<PredefinedReason> mPredefinedReasonList;
    private Context mContext;

    public ReasonAdapter(List<PredefinedReason> reasonList, Context context) {
        this.mPredefinedReasonList = reasonList;
        this.mContext = context;
    }

    @Override
    public ReasonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reason_adapter,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReasonAdapter.ViewHolder holder, int position) {
        final PredefinedReason predefinedReason = mPredefinedReasonList.get(position);
        holder.setReasonTitle(predefinedReason.getTitle());
        holder.setReasonReferenceNumber(predefinedReason.getServerId());
        holder.setReasonDescription(predefinedReason.getDescription());
    }

    @Override
    public int getItemCount() {
        return mPredefinedReasonList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView reasonTitle;
        private TextView reasonReferenceNumber;
        private TextView reasonDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            reasonTitle = (TextView) itemView.findViewById(R.id.reason_item_title);
            reasonReferenceNumber = (TextView) itemView.findViewById(R.id.reason_item_reference_number);
            reasonDescription = (TextView) itemView.findViewById(R.id.reason_item_description);
        }

        public TextView getReasonTitle() {
            return reasonTitle;
        }

        public void setReasonTitle(String reasonTitle) {
            this.reasonTitle.setText(reasonTitle);
        }

        public TextView getReasonReferenceNumber() {
            return reasonReferenceNumber;
        }

        public void setReasonReferenceNumber(long reasonReferenceNumber) {
            this.reasonReferenceNumber.setText("Ref.: " + reasonReferenceNumber);
        }

        public TextView getReasonDescription() {
            return reasonDescription;
        }

        public void setReasonDescription(String reasonDescription) {
            this.reasonDescription.setText(reasonDescription);
        }

    }

    public void addItem(PredefinedReason predefinedReason) {
        mPredefinedReasonList.add(0,predefinedReason);
        notifyItemInserted(0);
    }
}
