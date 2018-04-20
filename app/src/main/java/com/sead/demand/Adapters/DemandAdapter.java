package com.sead.demand.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
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

import com.sead.demand.Activities.ViewDemandActivity;
import com.sead.demand.Databases.FeedReaderContract;
import com.sead.demand.Entities.Demand;
import com.sead.demand.Entities.User;
import com.sead.demand.R;
import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

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
    private User mCurrentUser;

    public DemandAdapter(List<Demand> demandList, Context context, int page){
        this.mDemandList = demandList;
        this.mContext = context;
        this.mPage = page;
        this.mSelectedItemsIds = new SparseBooleanArray();
        mCurrentUser = CommonUtils.getCurrentUserPreference(context);
    }

    @Override
    public DemandAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demand_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DemandAdapter.ViewHolder holder, final int position) {
        final Demand demand = mDemandList.get(position);

        showDemandStatus(demand.getStatus(), holder);
        showLateStatus(demand, holder);
        /*
        if(demand.getType() != null) showDemandPrior(demand.getType().getPriority(), holder);
        else showDemandPrior(null, holder);
        */

        /** Handle users' name **/
        handleUsersName(demand, holder);
        /** Handle users' name **/

        /** Handle seen demands **/
        handleSeenDemand(demand, holder);
        /** Handle seen demands **/

        holder.subject.setText(demand.getSubject());
        holder.description.setText(demand.getDescription());
        holder.time.setText(CommonUtils.formatTime(demand.getCreatedAt()));
        holder.date.setText(CommonUtils.formatDate(demand.getCreatedAt()));

        if (mSelectedItemsIds.get(position)) markDemandSelected(holder);
        else unmarkDemandSelected(holder);
    }

    private void handleUsersName(Demand demand, final DemandAdapter.ViewHolder holder) {
        String sender = "mim";
        String receiver = "mim";
        if (mPage == Constants.SENT_PAGE) {
            receiver = demand.getReceiver().getName();
        }   else if (mPage == Constants.RECEIVED_PAGE){
            sender = demand.getSender().getName();
        } else if (mPage == Constants.ADMIN_PAGE || mPage == Constants.STATUS_PAGE
                || mPage == Constants.ARCHIVE_PAGE || mPage == Constants.SEARCH_PAGE) {
            if (!mCurrentUser.getName().equals(demand.getSender().getName()))
                sender = demand.getSender().getName();
            if (!mCurrentUser.getName().equals(demand.getReceiver().getName()))
                receiver = demand.getReceiver().getName();
        }
        holder.user_sender.setText(sender);
        holder.user_receiver.setText(receiver);
    }

    private void handleSeenDemand(Demand demand, final DemandAdapter.ViewHolder holder) {
        int color;
        int drawable;
        // int visibility; (TODO) will be used for acknowledge messages. do not show any icon if message not received/acknowledge.
        if (demand.getSeen().equals(Constants.YES)) {
            color = ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light);
            drawable = R.drawable.ic_visibility_black_24dp;
            //holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
        } else {
            color = ContextCompat.getColor(mContext, R.color.black);
            drawable = R.drawable.ic_visibility_off_black_24dp;
        }
        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = mContext.getDrawable(drawable);
        } else {
            objDrawable = mContext.getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();
        holder.seenIcon.setImageDrawable(objDrawable);
        holder.seenIcon.setVisibility(View.VISIBLE);
        if (mCurrentUser.getName().equals(demand.getSender().getName())) {
            // No highlighted text if current user is the one who sent it.
            color = ContextCompat.getColor(mContext, R.color.common_google_signin_btn_text_light);
        }
        holder.user_sender.setTextColor(color);
        holder.user_receiver.setTextColor(color);
        holder.subject.setTextColor(color);
    }

    private void setTransferIntent(Intent intent, Demand demand) {
        intent.putExtra(Constants.INTENT_ACTIVITY, mContext.getClass().getSimpleName());
        int menuType = -1;
        switch(mPage){
            case Constants.RECEIVED_PAGE:
                menuType = Constants.RECEIVER_MENU;
                break;
            case Constants.SENT_PAGE:
                menuType = Constants.SENDER_MENU;
                break;
            case Constants.ADMIN_PAGE:
                menuType = Constants.SUPERIOR_MENU;
                break;
            case Constants.STATUS_PAGE:
                //TODO: Change this according to the new way.
                switch (demand.getStatus()) {
                    case Constants.ACCEPT_STATUS:
                        menuType = Constants.SHOW_CANCEL_MENU;
                        break;
                    case Constants.REJECT_STATUS:
                        menuType = Constants.SHOW_NO_MENU;
                        break;
                    case Constants.CANCEL_ACCEPTED_STATUS:
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
        if((mPage == Constants.RECEIVED_PAGE) && demand.getSeen().equals(Constants.NO)){
            int color = ContextCompat.getColor(mContext,R.color.common_google_signin_btn_text_light);
            int drawable = R.drawable.ic_visibility_black_24dp;
            //v.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transsilver));
            TextView subject = v.findViewById(R.id.demand_title);
            subject.setTextColor(color);
            TextView sender = v.findViewById(R.id.demand_user_sender);
            sender.setTextColor(color);
            TextView receiver =  v.findViewById(R.id.demand_user_receiver);
            receiver.setTextColor(color);
            ImageView visibilityIcon = v.findViewById(R.id.demand_seen_icon);
            Drawable objDrawable;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                objDrawable = mContext.getDrawable(drawable);
            } else {
                objDrawable = mContext.getResources().getDrawable(drawable);
            }
            objDrawable = objDrawable.mutate();
            visibilityIcon.setImageDrawable(objDrawable);
            visibilityIcon.setVisibility(View.VISIBLE);
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
        int color = ContextCompat.getColor(mContext, R.color.transsilver);
        holder.itemView.setBackgroundColor(color);
        holder.markIcon.setVisibility(View.VISIBLE);
    }

    private void unmarkDemandSelected(ViewHolder holder) {
        int color = ContextCompat.getColor(mContext, R.color.white);
        holder.itemView.setBackgroundColor(color);
        holder.markIcon.setVisibility(View.INVISIBLE);
    }

    private void showDemandPrior(String prior, ViewHolder holder) {
        int color;
        ImageView profilePic = holder.profileImage;

        if (prior == null) color = ContextCompat.getColor(mContext,R.color.white);
        else {
            switch (prior) {
                case Constants.VERY_HIGH_PRIOR_TAG:
                    color = ContextCompat.getColor(mContext, R.color.darkred);
                    break;
                case Constants.HIGH_PRIOR_TAG:
                    color = ContextCompat.getColor(mContext, R.color.Red);
                    break;
                case Constants.MEDIUM_PRIOR_TAG:
                    color = ContextCompat.getColor(mContext, R.color.dyellow);
                    break;
                default:
                    color = ContextCompat.getColor(mContext, R.color.white);
            }
        }

        LayerDrawable layerDrawable = (LayerDrawable) profilePic.getBackground();
        Drawable background = layerDrawable.getDrawable(1);
        ((GradientDrawable)background).setStroke(6, color);
        background.mutate();
    }

    private void showLateStatus(Demand demand, ViewHolder holder) {
        int color;
        ImageView profilePic = holder.profileImage;
        if(demand.isLate() == 1 && !(mCurrentUser.getId() == demand.getSender().getId() && this.mPage == Constants.SENT_PAGE) )
            color = ContextCompat.getColor(mContext, R.color.Red);
        else color = ContextCompat.getColor(mContext, R.color.accent);

        LayerDrawable layerDrawable = (LayerDrawable) profilePic.getBackground();
        Drawable background = layerDrawable.getDrawable(1);
        ((GradientDrawable)background).setStroke(6, color);
        background.mutate();
    }


    private void showDemandStatus(String status, ViewHolder holder) {
        int color;
        int drawable;

        switch (status){
            case Constants.DONE_STATUS: // Done.
                drawable = R.drawable.ic_check_circle_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.ForestGreen);
                break;
            case Constants.FINISH_STATUS: // Done.
                drawable = R.drawable.ic_assignment_turned_in_white_24dp;
                color = ContextCompat.getColor(mContext,R.color.dGreen);
                break;
            case Constants.UNFINISH_STATUS: // Unfinished.
                drawable = R.drawable.ic_assignment_return_black_24dp;
                color = ContextCompat.getColor(mContext, R.color.Black);
                break;
            case Constants.ACCEPT_STATUS: // Accepted.
                drawable = R.drawable.ic_thumb_up_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.green);
                break;
            case Constants.TRANSFER_STATUS: // Accepted.
                drawable = R.drawable.ic_swap_calls_white_24dp;
                color = ContextCompat.getColor(mContext,R.color.Brown);
                break;
            case Constants.REJECT_STATUS: // Rejected.
                drawable = R.drawable.ic_thumb_down_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.darkred);
                break;
            case Constants.CANCEL_REQUESTED_STATUS: //Request to cancel.
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.OrangeRed);
                break;
            case Constants.CANCEL_ACCEPTED_STATUS: //Cancelled
                drawable = R.drawable.ic_cancel_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.red);
                break;
            case Constants.DEADLINE_ACCEPTED_STATUS: // Postponed.
                drawable = R.drawable.ic_alarm_on_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.DarkOrchid);
                break;
            case Constants.DEADLINE_REQUESTED_STATUS: // Deadline.
                drawable = R.drawable.ic_alarm_add_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.DarkOrchid);
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
                drawable = R.drawable.ic_settings_backup_restore_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.Orange);
                break;
            case Constants.UNDEFINE_STATUS: // Undefined.
            default:
                drawable = R.drawable.ic_adjust_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.gray);
        }

        Drawable objDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            objDrawable = mContext.getDrawable(drawable);
        } else {
            objDrawable = mContext.getResources().getDrawable(drawable);
        }
        objDrawable = objDrawable.mutate();

        //holder.statusIcon.setColorFilter(color);
        holder.statusIcon.setImageDrawable(objDrawable);
    }

    @Override
    public int getItemCount() {
        return mDemandList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView subject;
        TextView user_sender;
        TextView user_receiver;
        TextView description;
        TextView time;
        TextView date;
        ImageView statusIcon;
        ImageView seenIcon;
        ImageView profileImage;
        ImageView markIcon;

        public ViewHolder(View view) {
            super(view);

            markIcon = (ImageView) view.findViewById(R.id.demand_mark);
            statusIcon = (ImageView) view.findViewById(R.id.demand_status_icon);
            seenIcon = (ImageView) view.findViewById(R.id.demand_seen_icon);
            profileImage = (ImageView) view.findViewById(R.id.demand_profile_image);
            subject = (TextView) view.findViewById(R.id.demand_title);
            user_sender = (TextView) view.findViewById(R.id.demand_user_sender);
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
            return CommonUtils.POST("/demand/mark-as-read", values);
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
