package com.example.caiqu.demand.Entities;

import com.example.caiqu.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
    private int id;
    private long localId;
    private String email;
    private String password;
    private String name;
    private String status; // Unlock to access the app.
    private String position; // Job Position {SECRETARIO, COORDENADOR...}.
    private int superior;
    private String gcm;
    private Date createdAt;
    private Date updatedAt;
    private String superiorEmail; // TODO: eliminate this. Its Superior Email Address.

    public User(long localId, int id, String email, String name, String status, String position, int superior, String gcm, String createdAt, String updatedAt) {
        setLocalId(localId);
        setId(id);
        setName(name);
        setEmail(email);
        setPosition(position);
        setSuperior(superior);
        setStatus(status);
        setGcm(gcm);
        setCreatedAt(CommonUtils.datify(createdAt));
        setUpdatedAt(CommonUtils.datify(updatedAt));
    }

    public User(String email, String password, String name, String superior, String position, String gcm) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.superiorEmail = superior;
        this.position = position;
        this.gcm = gcm;
    }

    public User(int id, String name, String email){
        setId(id);
        setName(name);
        setEmail(email);
    }

    public static User build(JSONObject userJson) throws JSONException {
        return new User(
                -1,
                userJson.getInt("id"),
                userJson.getString("email"),
                userJson.getString("name"),
                userJson.getString("status"),
                userJson.getString("position"),
                userJson.getInt("superior"),
                userJson.getString("gcm"),
                userJson.getString("created_at"),
                userJson.getString("updated_at")
        );
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getSuperior() {
        return superior;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getSuperiorEmail() {
        return superiorEmail;
    }

    public void setSuperiorEmail(String superiorEmail) {
        this.superiorEmail = superiorEmail;
    }

    public String getGcm() {
        return gcm;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", superiorEmail='" + superiorEmail + '\'' +
                ", gcm='" + gcm + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public void setSuperior(int superior) {
        this.superior = superior;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
