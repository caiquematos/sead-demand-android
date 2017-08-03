package com.example.caiqu.demand.Adapters;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.caiqu.demand.Databases.FeedReaderContract;
import com.example.caiqu.demand.Entities.User;
import com.example.caiqu.demand.R;
import com.example.caiqu.demand.Tools.CommonUtils;
import com.example.caiqu.demand.Tools.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by caiqu on 30/06/2017.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private String TAG = getClass().getSimpleName();
    private StatusTask mStatusTask;
    private ProgressDialog mProgressDialogUser;

    private List<User> mUserList;
    private Context mContext;

    public UserAdapter(List<User> userList, Context context) {
        this.mUserList = userList;
        this.mContext = context;
        this.mProgressDialogUser = new ProgressDialog(context);
    }

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_adapter,parent,false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView userName;
        private TextView userJobPosition;
        private TextView userStatus;
        private TextView requestTime;
        private TextView requestDate;
        private ImageView userPhoto;
        private ImageView userIconStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.request_user_name);
            userJobPosition = (TextView) itemView.findViewById(R.id.request_user_job_position);
            userStatus = (TextView) itemView.findViewById(R.id.request_user_status);
            requestDate = (TextView) itemView.findViewById(R.id.request_date);
            requestTime = (TextView) itemView.findViewById(R.id.request_time);
            userPhoto = (ImageView) itemView.findViewById(R.id.request_user_photo);
            userIconStatus = (ImageView) itemView.findViewById(R.id.request_icon_status);
        }

        public String getUserName() {
            return this.userName.getText().toString();
        }

        public void setUserName(String userName) {
            this.userName.setText(userName);
        }

        public String getUserJobPosition() {
            return this.userJobPosition.getText().toString();
        }

        public void setUserJobPosition(String userJobPosition) {
            this.userJobPosition.setText(userJobPosition);
        }

        public String getUserStatus() {
            return this.userStatus.getText().toString();
        }

        public void setUserStatus(String userStatus) {
            this.userStatus.setText(userStatus);
        }

        public String getRequestTime() {
            return this.requestTime.getText().toString();
        }

        public void setRequestTime(String requestTime) {
            this.requestTime.setText(requestTime);
        }

        public String getRequestDate() {
            return this.requestDate.getText().toString();
        }

        public void setRequestDate(String requestDate) {
            this.requestDate.setText(requestDate);
        }

        public ImageView getUserPhoto() {
            return userPhoto;
        }

        public void setUserPhoto(ImageView userPhoto) {
            this.userPhoto = userPhoto;
        }

        public ImageView getUserIconStatus(){return userIconStatus;}

        public void setUserIconStatus(ImageView userIconStatus) {this.userIconStatus = userIconStatus;}
    }

    @Override
    public void onBindViewHolder(final UserAdapter.ViewHolder holder, int position) {
        final User user = mUserList.get(position);
        final int holderPosition = position;

        Log.e(TAG, "Job position:" + user.getPosition());
        Log.e(TAG, "Date:" + CommonUtils.formatDate(user.getCreatedAt()));
        holder.setUserName(user.getName());
        holder.setUserJobPosition(user.getPosition());
        holder.setRequestDate(CommonUtils.formatDate(user.getCreatedAt()));
        holder.setRequestTime(CommonUtils.formatTime(user.getCreatedAt()));
        showUserStatus(holder, user.getStatus());

        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String buttonName;
                String message;
                final String status;
                if(user.getStatus().equals(Constants.YES)) {
                    buttonName = "Bloquear";
                    message = "Bloqueando esse usuário, ele perderá o acesso ao aplicativo";
                    status = Constants.NO;
                } else{
                    buttonName = "Desbloquear";
                    message = "Desbloqueando esse usuário, ele terá acesso ao aplicativo";
                    status = Constants.YES;
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       attemptToSetUserStatus(user.getId(), status, holder, holderPosition);
                    }
                });

                alert.setTitle(holder.getUserName());
                alert.setMessage(message);
                alert.show();

            }

        });
    }

    private void showUserStatus(ViewHolder holder, String status) {
        int drawable = -1;
        int color = -1;
        switch (status) {
            case Constants.YES:
                holder.setUserStatus(mContext.getResources().getString(R.string.user_unlocked));
                drawable = R.drawable.ic_lock_open_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.secondary_text);
                break;
            case Constants.NO:
                holder.setUserStatus(mContext.getResources().getString(R.string.user_locked));
                drawable = R.drawable.ic_lock_outline_black_24dp;
                color = ContextCompat.getColor(mContext,R.color.transred);
                break;
        }
        holder.getUserIconStatus().setImageResource(drawable);
        holder.getUserIconStatus().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    private void attemptToSetUserStatus(int userId, String status, ViewHolder holder, int position) {
        if(mStatusTask == null && CommonUtils.isOnline(mContext)){
            mStatusTask = new StatusTask(userId, status, holder, position);
            mStatusTask.execute();
        } else {
            Snackbar.make(holder.itemView, R.string.internet_error, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public class StatusTask extends AsyncTask<Void, Void, String> {
        private int id;
        private String status;
        private ViewHolder holder;
        private int position;

        public StatusTask(int id, String status, ViewHolder holder, int position) {
            this.id = id;
            this.status = status;
            this.holder = holder;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialogUser.setMessage("Por favor aguarde...");
            mProgressDialogUser.setCancelable(false);
            mProgressDialogUser.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put("user", id);
            values.put("status", status);
            return CommonUtils.POST("/user/set-status/", values);
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            super.onPostExecute(jsonResponse);
            mStatusTask = null;
            JSONObject jsonObject;
            JSONObject jsonUser;
            User user = null;
            boolean success = false;

            Log.e(TAG, jsonResponse);

            try {
                jsonObject = new JSONObject(jsonResponse);
                success = jsonObject.getBoolean("success");

                jsonUser = jsonObject.getJSONObject("user");
                user = User.build(jsonUser);
            } catch (JSONException e) {
                Snackbar.make(holder.itemView, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                e.printStackTrace();
            }

            String message;

            if (success) {
                switch(user.getStatus()){
                    case Constants.YES:
                        message = "Usuário desbloqueado com sucesso.";
                        break;
                    case Constants.NO:
                        message = "Usuário bloqueado com sucesso.";
                        break;
                    default:
                        message = mContext.getString(R.string.server_error);
                }
                Snackbar.make(holder.itemView, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.e(TAG, "status after server:" + user.getStatus());
                mUserList.get(position).setStatus(user.getStatus());
                showUserStatus(holder,user.getStatus());
                CommonUtils.updateColumnUserDB(
                        FeedReaderContract.UserEntry.COLUMN_NAME_USER_STATUS,
                        user.getStatus(),
                        user,
                        mContext
                );
            } else {
                Snackbar.make(holder.itemView, R.string.server_error, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            if (mProgressDialogUser.isShowing()){
                mProgressDialogUser.dismiss();
            }
        }
    }

}
