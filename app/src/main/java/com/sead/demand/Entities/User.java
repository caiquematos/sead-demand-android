package com.sead.demand.Entities;

import android.util.Log;

import com.sead.demand.Tools.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable{
    private String TAG = getClass().getSimpleName();
    private int id;
    private long localId;
    private String email;
    private String password;
    private String name;
    private Job job;
    private User superior;
    private Department department;
    private long jobId;
    private String status; // Unlock to access the app.
    private String position; // Job Position {SECRETARIO, COORDENADOR...}.
    private int superiorId;
    private String gcm;
    private String localImagePath;
    private String serverImagePath;
    private Date createdAt;
    private Date updatedAt;
    private String superiorEmail; // TODO: eliminate this. Its Superior Email Address.
    private long departmentId;
    private String type;
    private String institution;
    private String institutionType;

    public User(long localId, int id, String email, String name, String status, String position,
                int superiorId, String gcm, String createdAt,
                String updatedAt) {
        setLocalId(localId);
        setId(id);
        setName(name);
        setEmail(email);
        setPosition(position);
        setSuperiorId(superiorId);
        setStatus(status);
        setGcm(gcm);
        setCreatedAt(CommonUtils.convertTimestampToDate(createdAt));
        setUpdatedAt(CommonUtils.convertTimestampToDate(updatedAt));
    }

    public User(long localId, int id, String email, String name, String status, String position,
                String gcm, Job job, User superior, String createdAt,
                String updatedAt) {
        setLocalId(localId);
        setId(id);
        setName(name);
        setEmail(email);
        setPosition(position);
        setStatus(status);
        setGcm(gcm);
        setJob(job);
        setSuperior(superior);
        setCreatedAt(CommonUtils.convertTimestampToDate(createdAt));
        setUpdatedAt(CommonUtils.convertTimestampToDate(updatedAt));
    }

    // Internal User
    public User(String email, String password, String name, String superiorId, String position, String gcm, long jobId, String type) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.superiorEmail = superiorId;
        this.position = position;
        this.gcm = gcm;
        this.jobId = jobId;
        this.type = type;
    }

    // Univasf User
    public User(String email, String name,  String password, Department department,
                Job job, String gcm,String type) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.department = department;
        this.gcm = gcm;
        this.job = job;
        this.type = type;
    }

    // External User
    public User(String email, String name, String password, String gcm, String type,
                String institution, String institutionType) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.gcm = gcm;
        this.type = type;
        this.institution = institution;
        this.institutionType = institutionType;
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

    // Complete Build with Job and Superior objects.
    public static User build(Job job, User superior, JSONObject userJson) throws JSONException {
        return new User(
                -1,
                userJson.getInt("id"),
                userJson.getString("email"),
                userJson.getString("name"),
                userJson.getString("status"),
                userJson.getString("position"),
                userJson.getString("gcm"),
                job,
                superior,
                userJson.getString("created_at"),
                userJson.getString("updated_at")
        );
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getSuperior() {
        return superior;
    }

    public void setSuperior(User superior) {
        this.superior = superior;
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

    public int getSuperiorId() {
        return superiorId;
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

    public void setSuperiorId(int superiorId) {
        this.superiorId = superiorId;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public String getServerImagePath() {
        return serverImagePath;
    }

    public void setServerImagePath(String serverImagePath) {
        this.serverImagePath = serverImagePath;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(String institutionType) {
        this.institutionType = institutionType;
    }

    @Override
    public String toString() {
        return "(user) id: " + getId()
                + " name: " + getName()
                + " email: " + getEmail()
                + " superior id: " + getSuperiorId()
                + " superior: " + (getSuperior() == null ? "null" : "" + getSuperior().getId());
    }
}