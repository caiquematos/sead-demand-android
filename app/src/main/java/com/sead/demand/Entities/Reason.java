package com.sead.demand.Entities;

import com.sead.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiqu on 19/07/2017.
 */

public class Reason implements Serializable{
    private int id;
    private long localId;
    private int demandId;
    private String status; // Rejected, Accepted, Canceled...
    private int reasonIndex;
    private String comment;
    private Date created_at;
    private Date updated_at;

    public Reason(long localId, int id, int demandId, String status, int reasonIndex, String comment, String created_at, String updated_at) {
        this.id = id;
        this.localId = localId;
        this.demandId = demandId;
        this.status = status;
        this.reasonIndex = reasonIndex;
        this.comment = comment;
        this.created_at = CommonUtils.convertTimestampToDate(created_at);
        this.updated_at = CommonUtils.convertTimestampToDate(updated_at);
    }

    public static Reason build(JSONObject json) throws JSONException {
        return new Reason(
                -1,
                json.getInt("id"),
                json.getInt("demand"),
                json.getString("status"),
                json.getInt("reason"),
                json.getString("comment"),
                json.getString("created_at"),
                json.getString("updated_at")
        );
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public int getDemandId() {
        return demandId;
    }

    public void setDemandId(int demandId) {
        this.demandId = demandId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReasonIndex() {
        return reasonIndex;
    }

    public void setReasonIndex(int reasonIndex) {
        this.reasonIndex = reasonIndex;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Reason{" +
                "id=" + id +
                ", localId=" + localId +
                ", reasonIndex=" + reasonIndex +
                ", comment='" + comment + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
