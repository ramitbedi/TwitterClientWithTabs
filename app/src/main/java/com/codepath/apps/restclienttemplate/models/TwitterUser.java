package com.codepath.apps.restclienttemplate.models;

import android.content.Context;
import android.graphics.Bitmap;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name="TwitterUsers")
public class TwitterUser extends Model {
    @Column(name="user_id", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long user_id;

    @Column(name="name")
    private String name;

    @Column(name="screen_name")
    private String screen_name;


    @Column(name="description")
    private String description;

    @Column(name="profile_image_url")
    private String profile_image_url;

    @Column(name="url")
    private String url;

    @Column(name="entities")
    private String entities;


    // Constructors


    public TwitterUser() { super(); }

    public TwitterUser(JSONObject userJSON) throws JSONException {
        super();

        user_id = userJSON.getLong("id");
        name = userJSON.getString("name");
        screen_name = userJSON.getString("screen_name");
       // location = userJSON.getString("location");
        description = userJSON.getString("description");
        profile_image_url = userJSON.getString("profile_image_url");

        url = userJSON.getString("url");
        entities = userJSON.getJSONObject("entities").toString();
    }

    // Getters

    public long getUserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getScreen_name() {
        return screen_name;
    }



    public String getDescription() {
        return description;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public String getUrl() {
        return url;
    }

    public String getEntities() {
        return entities;
    }

    // SQL Accessors
    public static TwitterUser getById(long id) {
        return new Select().from(TwitterUser.class).where("user_id = ?", id).executeSingle();
    }


    public static List<Tweet> recentUsers() {
        return new Select().from(TwitterUser.class).orderBy("user_id DESC").limit("300").execute();
    }

    public Bitmap getBitmap(Context context) {
        return TweetImage.getImage(this, context);
    }
}

