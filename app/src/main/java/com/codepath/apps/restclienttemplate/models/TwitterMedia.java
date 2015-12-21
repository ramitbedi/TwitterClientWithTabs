package com.codepath.apps.restclienttemplate.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Table(name="TwitterMedia")
public class TwitterMedia extends Model {
    // Member Functions

    @Column(name = "media_id", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private long media_id;

    @Column(name = "media_url")
    private String media_url;

    @Column(name = "url")
    private String url;

    @Column(name = "type")
    private String type;

    @Column(name = "sizes")
    private Map<String, MediaSize> sizes;

    // Constructor

    public TwitterMedia() {
        super();
    }

    public TwitterMedia(JSONObject mediaObjJSON) throws JSONException {
        media_id = mediaObjJSON.getLong("id");
        media_url = mediaObjJSON.getString("media_url");
        url = mediaObjJSON.getString("url");
        type = mediaObjJSON.getString("type");

        sizes = new HashMap<>();
        JSONObject sizesJSON = mediaObjJSON.getJSONObject("sizes");
        Iterator<String> iter = sizesJSON.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            JSONObject size = sizesJSON.getJSONObject(key);
            MediaSize mediaSize = new MediaSize();
            mediaSize.w = size.getInt("w");
            mediaSize.h = size.getInt("h");
            mediaSize.resize = size.getString("resize");

            sizes.put(key, mediaSize);
        }
    }

    // Getters

    public long getMedia_id() {
        return media_id;
    }

    public String getMedia_url() {
        return media_url;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public Map<String, MediaSize> getSizes() {
        return sizes;
    }

    public static TwitterMedia getById(long id) {
        return new Select().from(TwitterMedia.class).where("media_id = ?", id).executeSingle();
    }


    /**
     * MediaSize for media objects
     */
    class MediaSize {
        public int h;
        public int w;
        public String resize;
    }

}

