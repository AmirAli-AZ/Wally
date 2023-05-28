package com.amirali.wally.model;

import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String username, password, name, email;

    private List<ObjectId> uploadedWallpapers;

    public User(String username, String password, String name, String email, List<ObjectId> uploadedWallpapers) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.uploadedWallpapers = uploadedWallpapers;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ObjectId> getUploadedWallpapers() {
        return uploadedWallpapers;
    }

    public void setUploadedWallpapers(List<ObjectId> uploadedWallpapers) {
        this.uploadedWallpapers = uploadedWallpapers;
    }
}
