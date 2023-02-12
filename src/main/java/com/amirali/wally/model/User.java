package com.amirali.wally.model;

import org.bson.types.ObjectId;

import java.io.*;
import java.util.Base64;
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

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(getUsername());
        oos.writeUTF(Base64.getEncoder().encodeToString(getPassword().getBytes()));
        oos.writeUTF(getEmail());
        oos.writeUTF(getName());
        oos.writeObject(getUploadedWallpapers());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        setUsername(ois.readUTF());
        setPassword(new String(Base64.getDecoder().decode(ois.readUTF())));
        setEmail(ois.readUTF());
        setName(ois.readUTF());
        setUploadedWallpapers(((List<ObjectId>) ois.readObject()));
    }
}
