package com.sead.demand.Entities;

import android.util.Log;

import com.sead.demand.Tools.CommonUtils;
import com.sead.demand.Tools.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Demand implements Serializable {
    private String TAG = getClass().getSimpleName();
    private long localId;
    private int id;
    private User sender;
    private User receiver;
    private PredefinedReason reason;
    private DemandType type;
    private String subject;
    private String description;
    private String status;
    private String seen;
    private int postponed;
    private int late; // boolean must be int because mysql é tinyint and json doesnt conver
    private boolean archive;
    private Date createdAt;
    private Date updatedAt;

    public static Demand build(User sender, User receiver, PredefinedReason reason, DemandType type, JSONObject json) throws JSONException {
        return new Demand(
                -1,
                json.getInt("id"),
                sender,
                receiver,
                reason,
                type,
                json.getString("subject"),
                json.getString("description"),
                json.getString("status"),
                json.getString("seen"),
                json.getInt("postponed"),
                json.getInt("late"),
                json.getString("created_at"),
                json.getString("updated_at")
        );
    }

    public Demand(
            long localId,
            int demandId,
            User sender,
            User receiver,
            PredefinedReason reason,
            DemandType type,
            String subject,
            String description,
            String status,
            String seen,
            int postponed,
            int late,
            String created_at,
            String updated_at) {

        setLocalId(localId);
        setId(demandId);
        setSender(sender);
        setReceiver(receiver);
        setReason(reason);
        setSubject(subject);
        setDescription(description);
        setStatus(status);
        setType(type);
        setSeen(seen);
        setPostponed(postponed);
        setLate(late);
        setCreatedAt(CommonUtils.convertTimestampToDate(created_at));
        setUpdatedAt(CommonUtils.convertTimestampToDate(updated_at));
    }

    public DemandType getType() {
        return type;
    }

    public void setType(DemandType type) {
        this.type = type;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public PredefinedReason getReason() {
        return reason;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Sender:" + sender.getId()
                + " " + sender.getName()
                + " " + sender.getEmail()
                + " Receiver:" + (receiver != null ? receiver.getEmail() : "null")
                + " " + (receiver != null ? receiver.getId() : "null")
                + " " + (receiver != null ? receiver.getName() : "null")
                + " Demand:" + getLocalId()
                + " " + getId()
                + " status:" + getStatus()
                + " seen:" + getSeen()
                + " " + getSubject()
                + " " + getDescription()
                + " postponed:" + getPostponed()
                + " late:" + isLate()
                + " " + getCreatedAt()
                + " " + getUpdatedAt()
                + " reason: " + (this.getReason() != null ? getReason().getTitle() : "null")
                + " type: " + (this.getType() != null ? getType().getTitle() : "null")
                ;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public long getLocalId() {
        return localId;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReason(PredefinedReason reason) {
        this.reason = reason;
    }

    public int getPostponed() {
        return postponed;
    }

    public void setPostponed(int postponed) {
        this.postponed = postponed;
    }

    public int isLate() {
        return late;
    }

    public void setLate(int late) {
        this.late = late;
    }

    public long getDueTimeInMillis() {
        long time;
        Calendar c = Calendar.getInstance();
        Log.d(TAG, "c (now): " + c.getTimeInMillis());
        c.setTime(getCreatedAt());
        Log.d(TAG, "created_at: " + getCreatedAt().toString());
        Log.d(TAG, "c (created): " + c.getTimeInMillis());
        if (getType() != null) time = CommonUtils.getPriorityTime(getType().getPriority());
        else time = Constants.DUE_TIME[3];
        Log.d(TAG, "time to be added bf postpone method: "  + time);
        c.add(Calendar.DAY_OF_YEAR, (int) time);
        Log.d(TAG,  "c with added time 1: " + c.getTimeInMillis());
        Log.d(TAG, "times postponed: " + getPostponed());
        for (int i = 1; i <= getPostponed(); i++) {
            long timePostponed = time / i;
            Log.d(TAG, "time to be added af postpone method: "  + timePostponed);
            c.add(Calendar.DAY_OF_YEAR, (int) timePostponed);
            Log.d(TAG,  "c with added time n: " + c.getTimeInMillis());
        }
        return c.getTimeInMillis();
    }

    public String getDueDate() {
        long timeInMillis = getDueTimeInMillis();
        return CommonUtils.convertMillisToDate(timeInMillis);
    }

    public String getDueTime() {
        long timeInMillis = getDueTimeInMillis();
        return CommonUtils.convertMillisToTime(timeInMillis);
    }

}
