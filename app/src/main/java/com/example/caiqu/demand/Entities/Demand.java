package com.example.caiqu.demand.Entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.security.cert.CertPath;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Demand {
    private int id;
    private int fromId;
    private int toId;
    private String fromEmail;
    private String toEmail;
    private String from;
    private String to;
    private String importance;
    private String subject;
    private String description;
    private String status;
    private String seen;
    private Date createdAt;

    public Demand(int id, String fromEmail, String toEmail, String from, String to,
                  String importance, String subject, String description,
                  String status) {
        this.id= id;
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.from = from;
        this.to = to;
        this.importance = importance;
        this.subject = subject;
        this.description = description;
        this.status = status;
    }

    public Demand(JSONObject json) throws JSONException {
        setId(json.getInt("id"));
        setFromId(json.getInt("sender"));
        setToId(json.getInt("receiver"));
        setFrom(json.getString("senderName"));
        setTo(json.getString("receiverName"));
        setFromEmail(json.getString("senderEmail"));
        setToEmail(json.getString("receiverEmail"));
        setImportance(json.getString("importance"));
        setSeen(json.getString("seen"));
        setStatus(json.getString("status"));
        setSubject(json.getString("subject"));
        setDescription(json.getString("description") + "\n\n\n\n");
        setCreatedAt(datify(json.getString("created_at")));
    }

    private Date datify(String create_at) {
        try{
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US);
            return formatter.parse(create_at);
        }catch(Exception e){//this generic but you can control another types of exception
            return null;
        }
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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
        return "Demand{" +
                "id=" + id +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", importance='" + importance + '\'' +
                ", subject='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
