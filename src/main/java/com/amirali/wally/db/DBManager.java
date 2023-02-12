package com.amirali.wally.db;

import com.amirali.wally.model.User;
import com.amirali.wally.model.WallpaperItem;
import com.amirali.wally.utils.Environment;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import javafx.scene.image.Image;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public final class DBManager {

    private static DBManager manager;

    private final MongoClient client;

    private DBManager() {
        client = new MongoClient("localhost", 27017);
    }

    public static DBManager getInstance() {
        if (manager == null)
            manager = new DBManager();
        return manager;
    }

    public MongoDatabase getDatabase() {
        return client.getDatabase("WallyDB");
    }

    public MongoCollection<Document> getCollection(String name) {
        Objects.requireNonNull(name);

        var db = getDatabase();
        var collectionNames = db.listCollectionNames().into(new ArrayList<>());
        var collectionCreated = false;
        for (String collectionName : collectionNames) {
            if (collectionName.equals(name)) {
                collectionCreated = true;
                break;
            }
        }
        if (!collectionCreated)
            db.createCollection(name);

        return db.getCollection(name);
    }


    public MongoCollection<Document> getUserCollection() {
        return getCollection("users");
    }

    public boolean createUser(User user) {
        Objects.requireNonNull(user);

        if (getUser(user.getUsername()).isPresent())
            return false;

        var users = getUserCollection();
        var userDocument = new Document();
        userDocument.put("username", user.getUsername());
        userDocument.put("password", user.getPassword());
        userDocument.put("name", user.getName());
        userDocument.put("email", user.getEmail());
        userDocument.put("uploadedWallpapers", user.getUploadedWallpapers());

        users.insertOne(userDocument);
        return true;
    }

    public Optional<User> getUser(String username) {
        Objects.requireNonNull(username);

        var userCollection = getUserCollection();
        User user = null;
        var users = userCollection.find(Filters.eq("username", username)).into(new ArrayList<>());
        if (!users.isEmpty()) {
            var userDocument = users.get(0);
            user = new User(
                    userDocument.getString("username"),
                    userDocument.getString("password"),
                    userDocument.getString("name"),
                    userDocument.getString("email"),
                    userDocument.getList("uploadedWallpapers", ObjectId.class)
            );
        }

        return Optional.ofNullable(user);
    }

    public void writeUser(User user) {
        Objects.requireNonNull(user);

        var path = getUserFilePath();
        try (var outputStream = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            outputStream.writeObject(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<User> readUser() {
        var path = getUserFilePath();
        if (Files.notExists(path))
            return Optional.empty();

        try (var inputStream = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            var user = ((User) inputStream.readObject());
            return Optional.ofNullable(user);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Path getUserFilePath() {
        return Paths.get(Environment.getAppData() + File.separator + "user.obj");
    }

    public void upload(WallpaperItem item, File wallpaper, File thumbnail, ProgressListener progressListener) throws FileNotFoundException {
        Objects.requireNonNull(item);
        Objects.requireNonNull(wallpaper);
        Objects.requireNonNull(thumbnail);

        if (wallpaper.length() > 1024 * 1024 * 50)
            throw new IllegalArgumentException("Cannot upload file bigger than 50MB");
        if (thumbnail.length() > 1024 * 1024)
            throw new IllegalArgumentException("Cannot upload thumbnail bigger than 1MB");
        if (Files.notExists(wallpaper.toPath()))
            throw new FileNotFoundException("File doesn't exist");

        var savedUser = readUser();
        if (savedUser.isEmpty())
            throw new IllegalArgumentException("user is empty please login");
        var userInDB = getUser(savedUser.get().getUsername());
        if (userInDB.isEmpty())
            throw new IllegalArgumentException("No user found with " + savedUser.get().getUsername() + " username");
        if (!userInDB.get().getPassword().equals(savedUser.get().getPassword()))
            throw new IllegalArgumentException("Password is incorrect please login");

        var db = getDatabase();
        var gridFSBucket = GridFSBuckets.create(db, "wallpapers");

        try (var wallpInputStream = new FileInputStream(wallpaper)) {
            var metadata = new Document();
            metadata.put("title", item.getTitle());
            metadata.put("description", item.getDescription());
            metadata.put("category", item.getCategory());
            metadata.put("artist", item.getArtist());
            metadata.put("publisher", savedUser.get().getUsername());

            metadata.put("thumbnail", Base64.getEncoder().encode(Files.readAllBytes(thumbnail.toPath())));

            var uploadOptions = new GridFSUploadOptions()
                    .chunkSizeBytes(1024 * 1024)
                    .metadata(metadata);

            var uploadStream = gridFSBucket.openUploadStream(wallpaper.getName(), uploadOptions);
            var buffer = new byte[1024 * 1024];
            var len = 0;
            var counter = 0.0;
            while ((len = wallpInputStream.read(buffer)) != -1) {
                uploadStream.write(buffer, 0, len);
                counter += len;
                if (progressListener != null)
                    progressListener.onProgress((counter / wallpaper.length()) * 100.0);
            }
            var objectId = uploadStream.getObjectId();
            uploadStream.close();

            var userCollection = getUserCollection();
            userCollection.updateOne(
                    Filters.eq("username", savedUser.get().getUsername()),
                    Updates.push("uploadedWallpapers", objectId)
            );
            userCollection.find(Filters.eq("username", savedUser.get().getUsername())).forEach((Consumer<? super Document>) document -> {
                savedUser.get().setUploadedWallpapers(document.getList("uploadedWallpapers", ObjectId.class));
                writeUser(savedUser.get());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(ObjectId objectId) {
        Objects.requireNonNull(objectId);

        var savedUser = readUser();
        if (savedUser.isEmpty())
            throw new IllegalArgumentException("savedUser is empty please login");
        var userInDB = getUser(savedUser.get().getUsername());
        if (userInDB.isEmpty())
            throw new IllegalArgumentException("No savedUser found with " + savedUser.get().getUsername() + " username");
        if (!userInDB.get().getPassword().equals(savedUser.get().getPassword()))
            throw new IllegalArgumentException("Password is incorrect please login");

        var db = getDatabase();
        var gridFSBucket = GridFSBuckets.create(db, "wallpapers");

        gridFSBucket.delete(objectId);

        var userCollection = getUserCollection();
        userCollection.updateOne(
                Filters.eq("username", savedUser.get().getUsername()),
                Updates.pull("uploadedWallpapers", objectId)
        );

        userCollection.find(Filters.eq("username", savedUser.get().getUsername())).forEach((Consumer<? super Document>) document -> {
            savedUser.get().setUploadedWallpapers(document.getList("uploadedWallpapers", ObjectId.class));
            writeUser(savedUser.get());
        });
    }

    public void remove(WallpaperItem item) {
        Objects.requireNonNull(item);
        remove(item.getWallpaperId());
    }

    public void update(WallpaperItem item, File wallpaper, File thumbnail, ProgressListener progressListener) throws FileNotFoundException {
        remove(item);
        upload(item, wallpaper, thumbnail, progressListener);
    }

    public List<WallpaperItem> getWallpapers(Bson query, int min, int max) {
        var db = getDatabase();
        var gridFSBucket = GridFSBuckets.create(db, "wallpapers");
        var wallpapers = new ArrayList<WallpaperItem>();
        if (query == null) {
            gridFSBucket.find().
                    skip(min).
                    limit(max).
                    sort(Sorts.descending("uploadDate")).
                    forEach((Consumer<? super GridFSFile>) gridFSFile -> {
                        var item = new WallpaperItem();

                        item.setWallpaperId(gridFSFile.getObjectId());
                        item.setTitle(gridFSFile.getMetadata().getString("title"));
                        item.setDescription(gridFSFile.getMetadata().getString("description"));
                        item.setCategory(gridFSFile.getMetadata().getString("category"));
                        item.setArtist(gridFSFile.getMetadata().getString("artist"));
                        item.setPublisher(gridFSFile.getMetadata().getString("publisher"));
                        item.setFilename(gridFSFile.getFilename());

                        var encodedThumbnail = gridFSFile.getMetadata().get("thumbnail", Binary.class);
                        var thumbnailImageInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(encodedThumbnail.getData()));

                        item.setThumbnail(new Image(thumbnailImageInputStream));

                        wallpapers.add(item);
                    });
        }else {
            gridFSBucket.find(query).
                    skip(min).
                    limit(max).
                    sort(Sorts.descending("uploadDate")).
                    forEach((Consumer<? super GridFSFile>) gridFSFile -> {
                        var item = new WallpaperItem();

                        item.setWallpaperId(gridFSFile.getObjectId());
                        item.setTitle(gridFSFile.getMetadata().getString("title"));
                        item.setDescription(gridFSFile.getMetadata().getString("description"));
                        item.setCategory(gridFSFile.getMetadata().getString("category"));
                        item.setArtist(gridFSFile.getMetadata().getString("artist"));
                        item.setPublisher(gridFSFile.getMetadata().getString("publisher"));
                        item.setFilename(gridFSFile.getFilename());

                        var encodedThumbnail = gridFSFile.getMetadata().get("thumbnail", Binary.class);
                        var thumbnailImageInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(encodedThumbnail.getData()));

                        item.setThumbnail(new Image(thumbnailImageInputStream));

                        wallpapers.add(item);
                    });
        }

        return wallpapers;
    }

    public void download(ObjectId wallpaperId, File destFile, ProgressListener progressListener) {
        Objects.requireNonNull(wallpaperId);
        Objects.requireNonNull(destFile);

        var db = getDatabase();
        var gridFSBucket = GridFSBuckets.create(db, "wallpapers");

        var buffer = new byte[1024 * 1024];
        var len = 0;
        var counter = 0.0;
        try (var downloadStream = gridFSBucket.openDownloadStream(wallpaperId); var fileOutputStream = new FileOutputStream(destFile)) {

            while ((len = downloadStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
                counter += len;
                if (progressListener != null)
                    progressListener.onProgress((counter / downloadStream.getGridFSFile().getLength()) * 100.0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download(WallpaperItem item, File destFile, ProgressListener progressListener) {
        download(item.getWallpaperId(), destFile, progressListener);
    }
}
