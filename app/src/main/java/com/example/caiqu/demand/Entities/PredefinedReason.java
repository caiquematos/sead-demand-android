package com.example.caiqu.demand.Entities;

import com.example.caiqu.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiqu on 8/14/2017.
 */

public class PredefinedReason implements Serializable{
    public String TAG = getClass().getSimpleName();
    private long localId;
    private long serverId;
    private String type;
    private String title;
    private String description;
    private Date createdAt;
    private Date updatedAt;

    public PredefinedReason(long localId, long serverId, String type, String title, String description, String createdAt, String updatedAt) {
        this.localId = localId;
        this.serverId = serverId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.createdAt = CommonUtils.convertTimestampToDate(createdAt);
        this.updatedAt = CommonUtils.convertTimestampToDate(updatedAt);
    }

    public static PredefinedReason build(JSONObject json) throws JSONException {
        return new PredefinedReason(
                -1,
                json.getLong("id"),
                json.getString("type"),
                json.getString("title"),
                json.getString("description"),
                json.getString("created_at"),
                json.getString("updated_at")
        );
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PredefinedReason{" +
                "serverId=" + serverId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
