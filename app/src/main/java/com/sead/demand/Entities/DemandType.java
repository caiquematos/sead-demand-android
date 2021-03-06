package com.sead.demand.Entities;

import com.sead.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiqu on 12/21/2017.
 */

public class DemandType implements Serializable{
    private long localId;
    private long id;
    private String title;
    private int complexity;
    private String priority;
    private Date created_at;
    private Date updated_at;

    public DemandType(long localId, long id, String title, String priority, int complexity, String created_at, String updated_at) {
        this.localId = localId;
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.complexity = complexity;
        this.created_at = CommonUtils.convertTimestampToDate(created_at);
        this.updated_at = CommonUtils.convertTimestampToDate(updated_at);
    }

    public static DemandType build(JSONObject json) throws JSONException {
        return new DemandType(
                -1,
                json.getLong("id"),
                json.getString("title"),
                json.getString("priority"),
                json.getInt("complexity"),
                json.getString("created_at"),
                json.getString("updated_at")
        );
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
