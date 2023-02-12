package com.amirali.wally.model;

import javafx.scene.image.Image;
import org.bson.types.ObjectId;

public class WallpaperItem {

    private ObjectId wallpaperId;

    private String title, description, artist, category, publisher, filename;

    private Image thumbnail;

    public ObjectId getWallpaperId() {
        return wallpaperId;
    }

    public void setWallpaperId(ObjectId wallpaperId) {
        this.wallpaperId = wallpaperId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
