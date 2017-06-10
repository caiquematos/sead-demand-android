package com.example.caiqu.demand.Entities;

import com.example.caiqu.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Demand implements Serializable {
    private long localId;
    private int id;
    private User sender;
    private User receiver;
    private String importance;
    private String subject;
    private String description;
    private String status;
    private String seen;
    private Date createdAt;
    private Date updatedAt;

   public static Demand build(JSONObject json) throws JSONException {
        User sender = new User(
                json.getInt("sender"),
                json.getString("senderName"),
                json.getString("senderEmail")
        );

        User receiver = new User(
                json.getInt("receiver"),
                json.getString("receiverName"),
                json.getString("receiverEmail")
        );

        return Demand.build(sender, receiver, json);
    }

    public static Demand build(User sender, User receiver, JSONObject json) throws JSONException {

        return new Demand(
                -1,
                json.getInt("id"),
                sender,
                receiver,
                json.getString("importance"),
                json.getString("subject"),
                json.getString("description"),
                json.getString("status"),
                json.getString("seen"),
                json.getString("created_at"),
                json.getString("updated_at")
        );
    }

    public Demand(
            long localId,
            int demandId,
            User sender,
            User receiver,
            String importance,
            String subject,
            String description,
            String status,
            String seen,
            String created_at,
            String updated_at) {

        setLocalId(localId);
        setId(demandId);
        setSender(sender);
        setReceiver(receiver);
        setSubject(subject);
        setDescription(description);
        setStatus(status);
        setImportance(importance);
        setSeen(seen);
        setCreatedAt(CommonUtils.datify(created_at));
        setUpdatedAt(CommonUtils.datify(updated_at));
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

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
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
                + " Receiver:" + receiver.getId()
                + " " + receiver.getEmail()
                + " " + receiver.getName()
                + " Demand:" + getLocalId()
                + " " + getId()
                + " " + getStatus()
                + " " + getSeen()
                + " " + getImportance()
                + " " + getCreatedAt()
                + " " + getUpdatedAt()
                + " " + getSubject()
                + " " + getDescription()
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
}
