package com.sead.demand.Entities;

import com.sead.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiqu on 12/21/2017.
 */

public class Department implements Serializable{
    private long id;
    private String title;
    private Date createdAt;
    private Date updatedAt;

    public Department(long id, String title, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.createdAt = CommonUtils.convertTimestampToDate(createdAt);
        this.updatedAt = CommonUtils.convertTimestampToDate(updatedAt);
    }

    public static Department build(JSONObject json) throws JSONException {
        return new Department(
                json.getLong("id"),
                json.getString("title"),
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
        return "Department{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
