package com.example.caiqu.demand.Entities;

import java.security.Timestamp;

public class User {
    private int id;
    private String email;
    private String password;
    private String name;
    private String position; //Job Position SECRETARIO, COORDENADOR...
    private String superior; //Its Superior Email Address
    private String gcm;
    private Timestamp createdAt;

    public User(String email, String password, String name, String superior, String position, String gcm) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.superior = superior;
        this.position = position;
        this.gcm = gcm;
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

    public String getSuperior() {
        return superior;
    }

    public void setSuperior(String superior) {
        this.superior = superior;
    }

    public String getGcm() {
        return gcm;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
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
                ", superior='" + superior + '\'' +
                ", gcm='" + gcm + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
