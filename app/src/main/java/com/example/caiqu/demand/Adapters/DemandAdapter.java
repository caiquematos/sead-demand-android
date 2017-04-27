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
    public void onBindViewHolder(DemandAdapter.ViewHolder holder, int position) {
        final Demand demand = mDemandList.get(position);

        switch (demand.getStatus()){
            case "A": //Accepted
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.green));
                break;
            case "C": //Cancelled
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.red));
                break;
            case "P": //Postponed
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.yellow));
                break;
            case "R": //Reopen
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.blue));
                break;
            case "U": //Undefined
                holder.mTag.setBackgroundColor(ContextCompat.getColor(mContext,R.color.gray));
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

        Log.e("On DemandAdap", "Page: " + mPage);
        //For tab sent the name to show should be who the demand was sent
        String user;
        if (mPage == 2) user = demand.getTo();
        else user = demand.getFrom();

        //If demand was already seen by the receiver change color of background and title
        if (demand.getSeen().equals("Y")) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
            holder.mSubject.setTextColor(ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light));
        }

        holder.mSubject.setText(demand.getSubject());
        holder.mUser.setText(user);
        holder.mDescription.setText(demand.getDescription());
        holder.mTime.setText(formatDate(demand.getCreatedAt()));

        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change color in order to indicate seen status
                if(mPage == 1){
                    v.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
                    TextView subject = (TextView) v.findViewById(R.id.demand_title);
                    subject.setTextColor(ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light));
                }
                Intent intent = new Intent(mContext, ViewDemandActivity.class);
                intent.putExtra("ACTIVITY", mContext.getClass().getSimpleName());
                intent.putExtra("PAGE", mPage);
                intent.putExtra("DEMAND", "" + demand.getId());
                intent.putExtra("SUBJECT", demand.getSubject());
                intent.putExtra("STATUS", demand.getStatus());
                intent.putExtra("SENDER", demand.getFrom());
                intent.putExtra("SEEN", demand.getSeen());
                intent.putExtra("DESCRIPTION", demand.getDescription());
                intent.putExtra("TIME", formatDate(demand.getCreatedAt()));
                intent.putExtra("IMPORTANCE", demand.getImportance());
                intent.putExtra("RECEIVER", demand.getTo());
                Log.d("ON VIEW HOLDER", demand.getSubject() + " Importance:" + demand.getImportance()
                    + " PACKAGE:"  + mContext.getClass().getSimpleName() + " Page:" + mPage);
                mContext.startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formatDate(Date createdAt) {
        Calendar cal =  Calendar.getInstance();
        cal.setTime(createdAt);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return "Day " + day;
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
