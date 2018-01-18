package com.sead.demand.Entities;

import com.sead.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caiqu on 12/20/2017.
 */

public class Job implements Serializable{
    private long id;
    private String title;
    private String position;
    private Date createdAt;
    private Date updatedAt;

    public Job(long id, String title, String position, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.position = position;
        this.createdAt = CommonUtils.convertTimestampToDate(createdAt);
        this.updatedAt = CommonUtils.convertTimestampToDate(updatedAt);
    }

    public static Job build (JSONObject jobJson) throws JSONException {
        return new Job(
                jobJson.getInt("id"),
                jobJson.getString("title"),
                jobJson.getString("position"),
                jobJson.getString("created_at"),
                jobJson.getString("updated_at")
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", position='" + position + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
