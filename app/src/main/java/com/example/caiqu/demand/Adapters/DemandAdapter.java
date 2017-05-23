package com.example.caiqu.demand.Adapters;

import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.caiqu.demand.Activities.ViewDemandActivity;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import java.util.Date;
import java.util.List;

/**
 * Created by caiqu on 10/03/2017.
 */

public class DemandAdapter extends RecyclerView.Adapter<DemandAdapter.ViewHolder> {
    private List<Demand> mDemandList;
    private Context mContext;
    private int mPage; //identifies which tab called it

    public DemandAdapter(List<Demand> demandList, Context context, int page){
        this.mDemandList = demandList;
        this.mContext = context;
        this.mPage = page;
    }

    @Override
    public DemandAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demand_adapter, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(DemandAdapter.ViewHolder holder, final int position) {
        final Demand demand = mDemandList.get(position);
        // Log.e("On ViewHolder", "Position:" +position + " Seen:" + demand.getSeen());

        switch (demand.getStatus()){
            case Constants.ACCEPT_STATUS: //Accepted
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.green));
                break;
            case Constants.REJECT_STATUS: //Rejected
            case Constants.CANCEL_STATUS: //Cancelled
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.red));
                break;
            case Constants.POSTPONE_STATUS: //Postponed
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.yellow));
                break;
            case Constants.REOPEN_STATUS: //Reopen
            case Constants.UNDEFINE_STATUS: //Undefined
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.gray));
                break;
            case Constants.RESEND_STATUS: //Resent
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.blue));
                break;
        }

        switch (demand.getImportance()){
            case "Urgente":
                holder.mImportance.setColorFilter(ContextCompat.getColor(mContext,R.color.transred));
                break;
            case "Importante":
                holder.mImportance.setColorFilter(ContextCompat.getColor(mContext,R.color.transyellow));
                break;
            default:
                holder.mImportance.setColorFilter(ContextCompat.getColor(mContext,R.color.transgreen));
        }

        // Log.e("On DemandAdap", "Page: " + mPage);
        //For tab sent the name to show should be who the demand was sent
        String user;
        if (mPage == 2) user = demand.getTo();
        else user = demand.getFrom();

        //If demand was already seen by the receiver change color of background and title
        if (demand.getSeen().equals("Y")) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
            holder.mSubject.setTextColor(ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
            holder.mSubject.setTextColor(ContextCompat.getColor(mContext,R.color.black));
        }

        holder.mSubject.setText(demand.getSubject());
        holder.mUser.setText(user);
        holder.mDescription.setText(demand.getDescription());
        holder.mTime.setText(CommonUtils.formatDate(demand.getCreatedAt()));

        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change color in order to indicate seen status
                if((mPage == 1 || mPage == 3) && demand.getSeen().equals("N")){
                    v.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
                    TextView subject = (TextView) v.findViewById(R.id.demand_title);
                    subject.setTextColor(ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light));
                    demand.setSeen("Y");
                    mDemandList.get(position).setSeen("Y");
                }
                Intent intent = new Intent(mContext, ViewDemandActivity.class);
                intent.putExtra("ACTIVITY", mContext.getClass().getSimpleName());
                intent.putExtra("PAGE", mPage);
                intent.putExtra("DEMAND", demand.getId());
                intent.putExtra("SUBJECT", demand.getSubject());
                intent.putExtra("STATUS", demand.getStatus());
                intent.putExtra("SENDERNAME", demand.getFrom());
                intent.putExtra("SEEN", demand.getSeen());
                intent.putExtra("DESCRIPTION", demand.getDescription());
                intent.putExtra("TIME", CommonUtils.formatDate(demand.getCreatedAt()));
                intent.putExtra("IMPORTANCE", demand.getImportance());
                intent.putExtra("RECEIVERNAME", demand.getTo());
                intent.putExtra("SENDEREMAIL", demand.getFromEmail());
                intent.putExtra("RECEIVEREMAIL", demand.getToEmail());
                Log.d("ON VIEW HOLDER", demand.getSubject() + " Importance:" + demand.getImportance()
                    + " PACKAGE:"  + mContext.getClass().getSimpleName() + " Page:" + mPage
                        + " Seen:" + demand.getSeen());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDemandList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mSubject;
        TextView mUser;
        TextView mDescription;
        TextView mTime;
        View mTag;
        ImageView mImportance;

        public ViewHolder(View view) {
            super(view);

            mImportance = (ImageView) view.findViewById(R.id.demand_importance);
            mTag = view.findViewById(R.id.demand_tag);
            mSubject = (TextView) view.findViewById(R.id.demand_title);
            mUser = (TextView) view.findViewById(R.id.demand_user);
            mDescription = (TextView) view.findViewById(R.id.demand_content);
            mTime = (TextView) view.findViewById(R.id.demand_time);
        }
    }
}
