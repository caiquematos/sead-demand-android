package com.example.caiqu.demand.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.caiqu.demand.Activities.ViewDemandActivity;
import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Entities.Demand;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by caiqu on 10/03/2017.
 */

public class DemandAdapter extends RecyclerView.Adapter<DemandAdapter.ViewHolder> {
    public String TAG = getClass().getSimpleName();

    private List<Demand> mDemandList;
    private Context mContext;
    private int mPage; //identifies which tab called it
    private SeenTask mSeenTask;
    private SparseBooleanArray mSelectedItemsIds;

    public DemandAdapter(List<Demand> demandList, Context context, int page){
        this.mDemandList = demandList;
        this.mContext = context;
        this.mPage = page;
        this.mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public DemandAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demand_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DemandAdapter.ViewHolder holder, final int position) {
        final Demand demand = mDemandList.get(position);
        // Log.e("On ViewHolder", "Position:" +position + " Seen:" + demand.getSeen());

        showDemandStatus(demand.getStatus(), holder);
        showDemandPrior(demand.getPrior(), holder);

        // Log.e("On DemandAdapt", "Page: " + mPage);
        //For tab sent the name to show should be who the demand was sent
        String user;
        if (mPage == Constants.SENT_PAGE) user = "Para: " + demand.getReceiver().getName();
        else user = "De: " + demand.getSender().getName();

        String receiver;
        if (mPage == Constants.ADMIN_PAGE || mPage == Constants.STATUS_PAGE) {
            user = "De: " + demand.getSender().getName();
            receiver = "Para: " + demand.getReceiver().getName();
            holder.user_receiver.setText(receiver);
            holder.user_receiver.setVisibility(View.VISIBLE);
        }

        holder.user.setText(user);

        //If demand was already seen by the receiver change color of background and title
        if (demand.getSeen().equals(Constants.YES)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
            holder.subject.setTextColor(ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
            holder.subject.setTextColor(ContextCompat.getColor(mContext,R.color.black));
        }

        holder.subject.setText(demand.getSubject());
        holder.description.setText(demand.getDescription());
        holder.time.setText(CommonUtils.formatTime(demand.getCreatedAt()));
        holder.date.setText(CommonUtils.formatDate(demand.getCreatedAt()));

        if (mSelectedItemsIds.get(position)) markDemandSelected(holder);
    }

    private void setTransferIntent(Intent intent, Demand demand) {
        intent.putExtra(Constants.INTENT_ACTIVITY, mContext.getClass().getSimpleName());
        int menuType = -1;
        switch(mPage){
            case Constants.RECEIVED_PAGE:
                menuType = Constants.SHOW_NO_MENU;
                if (demand.getStatus().equals(Constants.ACCEPT_STATUS)
                        || demand.getStatus().equals(Constants.LATE_STATUS))
                    menuType = Constants.SHOW_DONE_MENU;
                break;
            case Constants.SENT_PAGE:
                menuType = Constants.SHOW_NO_MENU;
                if (demand.getStatus().equals(Constants.CANCEL_STATUS) || demand.getStatus().equals(Constants.REJECT_STATUS))
                    menuType = Constants.SHOW_RESEND_MENU;
                break;
            case Constants.ADMIN_PAGE:
                menuType = Constants.SHOW_TRIO_MENU;
                break;
            case Constants.STATUS_PAGE:
                switch (demand.getStatus()) {
                    case Constants.ACCEPT_STATUS:
                        menuType = Constants.SHOW_CANCEL_MENU;
                        break;
                    case Constants.REJECT_STATUS:
                        menuType = Constants.SHOW_NO_MENU;
                        break;
                    case Constants.CANCEL_STATUS:
                        menuType = Constants.SHOW_REOPEN_MENU;
                        break;
                    case Constants.POSTPONE_STATUS:
                        menuType = Constants.SHOW_TRIO_MENU;
                }
                break;
        }
        intent.putExtra(Constants.INTENT_PAGE, mPage);
        intent.putExtra(Constants.INTENT_MENU, menuType);
        intent.putExtra(Constants.INTENT_DEMAND, demand);
    }

    private void setSeenStatus(View v, Demand demand, int position) {
        if((mPage == Constants.ADMIN_PAGE || mPage == Constants.RECEIVED_PAGE) && demand.getSeen().equals(Constants.NO)){
            v.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
            TextView subject = (TextView) v.findViewById(R.id.demand_title);
            subject.setTextColor(ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light));
            demand.setSeen(Constants.YES);
            markAsSeen(demand, position); // On Received demands or Admin demands
        }
    }

    public void showDemand(View v, int position){
        Demand demand = mDemandList.get(position);
        //change color in order to indicate seen status
        setSeenStatus(v, demand, position);
        Intent intent = new Intent(mContext, ViewDemandActivity.class);
        setTransferIntent(intent, demand);
        mContext.startActivity(intent);
    }

    public void toggleSelection(int position){
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection(){
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value){
        Log.e(TAG, "Position:" + position + " Value:" + value + " Demanda:" + mDemandList.get(position).getSubject());
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount(){
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds(){
        return mSelectedItemsIds;
    }

    private void markDemandSelected(ViewHolder holder) {
        int color = ContextCompat.getColor(mContext, R.color.accent);
        int drawable = R.drawable.ic_check_black_24dp;

        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = mContext.getDrawable(drawable);
        } else {
            objDrawable = mContext.getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        holder.statusIcon.setColorFilter(color);
        holder.statusIcon.setImageDrawable(objDrawable);
    }

    private void showDemandPrior(String prior, ViewHolder holder) {
        int color;

        switch (prior){
            case Constants.VERY_HIGH_PRIOR_TAG:
                color = ContextCompat.getColor(mContext,R.color.darkred);
                break;
            case Constants.HIGH_PRIOR_TAG:
                color = ContextCompat.getColor(mContext,R.color.Red);
                break;
            case Constants.MEDIUM_PRIOR_TAG:
                color = ContextCompat.getColor(mContext,R.color.dyellow);
                break;
            default:
                color = ContextCompat.getColor(mContext,R.color.dGreen);
        }

        holder.prior_tag.setBackgroundColor(color);
    }

    private void showDemandStatus(String status, ViewHolder holder) {
        int color;
        int drawable;

        switch (status){
            case Constants.DONE_STATUS: // Done.
                drawable = R.drawable.ic_assignment_turned_in_white_24dp;
                color = ContextCompat.getColor(mContext,R.color.darkgreen);
                break;
            case Constants.ACCEPT_STATUS: // Accepted.
                drawable = R.drawable.ic_check_circle_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.green);
                break;
            case Constants.REJECT_STATUS: // Rejected.
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.darkred);
                break;
            case Constants.CANCEL_STATUS: // Cancelled.
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.red);
                break;
            case Constants.POSTPONE_STATUS: // Postponed.
                drawable = R.drawable.ic_alarm_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.dyellow);
                break;
            case Constants.RESEND_STATUS: // Resent.
                drawable = R.drawable.ic_send_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.blue);
                break;
            case Constants.LATE_STATUS: // Late.
                drawable = R.drawable.ic_alarm_off_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.primary_dark);
                break;
            case Constants.REOPEN_STATUS: // Reopen.
            case Constants.UNDEFINE_STATUS: // Undefined.
            default:
                drawable = R.drawable.ic_fiber_manual_record_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.gray);
        }

        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = mContext.getDrawable(drawable);
        } else {
            objDrawable = mContext.getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        holder.statusIcon.setColorFilter(color);
        holder.statusIcon.setImageDrawable(objDrawable);
    }

    @Override
    public int getItemCount() {
        return mDemandList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView subject;
        TextView user;
        TextView user_receiver;
        TextView description;
        TextView time;
        TextView date;
        View prior_tag;
        ImageView statusIcon;

        public ViewHolder(View view) {
            super(view);

            statusIcon = (ImageView) view.findViewById(R.id.demand_status_icon);
            prior_tag = view.findViewById(R.id.demand_prior_tag);
            subject = (TextView) view.findViewById(R.id.demand_title);
            user = (TextView) view.findViewById(R.id.demand_user);
            user_receiver = (TextView) view.findViewById(R.id.demand_user_receiver);
            description = (TextView) view.findViewById(R.id.demand_content);
            date = (TextView) view.findViewById(R.id.demand_date);
            time = (TextView) view.findViewById(R.id.demand_time);
        }
    }

    private void markAsSeen(Demand demand, int position){
        // Mark locally.
        CommonUtils.updateColumnDB(
                FeedReaderContract.DemandEntry.COLUMN_NAME_SEEN,
                Constants.YES,
                demand,
                Constants.UPDATE_READ,
                mContext
        );

        // Attempt to mark on server
        if(CommonUtils.isOnline(mContext)) {
            if (mSeenTask == null){
                mSeenTask = new SeenTask(demand, position);
                mSeenTask.execute();
            }
        } else {
            CommonUtils.handleLater(demand,Constants.MARK_AS_READ_JOB_TAG, mContext);
        }
    }

    public class SeenTask extends AsyncTask<Void, Void, String> {
        private Demand demand;
        private int position;

        public SeenTask(Demand demand, int position) {
            this.demand = demand;
            this.position = position;
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("demand", demand.getId());
            return CommonUtils.POST("/demand/mark-as-read/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mSeenTask = null;
            JSONObject jsonObject;
            boolean success = false;

            Log.e(TAG, "ON SEEN DEMAND: " + jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (success) mDemandList.get(position).setSeen(Constants.YES);
        }
    }



}
