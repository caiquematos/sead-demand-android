package com.sead.demand.Entities;

import com.sead.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiqu on 11/23/2017.
 */

public class Authority implements Serializable{
    private String TAG = getClass().getSimpleName();
    private int id;
    private long localId;
    private int user;
    private int superior;
    private int level;
    private Date createdAt;
    private Date updatedAt;

    public Authority(long localId, int id, int user, int superior, int level, String createdAt, String updatedAt) {
        this.id = id;
        this.localId = localId;
        this.user = user;
        this.superior = superior;
        this.level = level;
        this.createdAt = CommonUtils.convertTimestampToDate(createdAt);
        this.updatedAt = CommonUtils.convertTimestampToDate(updatedAt);
    }

    public static Authority build(JSONObject authJson) throws JSONException {
        return new Authority(
                -1,
                authJson.getInt("id"),
                authJson.getInt("user"),
                authJson.getInt("superior"),
                authJson.getInt("level"),
                authJson.getString("created_at"),
                authJson.getString("updated_at")
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

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getSuperior() {
        return superior;
    }

    public void setSuperior(int superior) {
        this.superior = superior;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
        return "Authority{" +
                "TAG='" + TAG + '\'' +
                ", id=" + id +
                ", localId=" + localId +
                ", user=" + user +
                ", superior=" + superior +
                ", level=" + level +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
