package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@Table(name = "Tweets")
public class Tweet extends Model {
    // Constants
    static final String TWITTER_TIME_FORMAT = "ccc MMM d HH:mm:ss z yyyy";
    static SimpleDateFormat twitterDateFormat = new SimpleDateFormat(TWITTER_TIME_FORMAT, Locale.US);

    // Member Variables
    @Column(name="tweet_id", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long tweet_id;

    @Column(name="text")
    private String text;

    @Column(name="created_at")
    private Long created_at;



    @Column(name="user_id")
    private long user_id;



    @Column(name="urls")
    private String entity_urls;

    @Column(name="hashtags")
    private String entity_hashtags;

    @Column(name="media_id")
    private long media_id;

    @Column(name="favorite_count")
    private int favorite_count;

    @Column(name="retweet_count")
    private int retweet_count;

    @Column(name="favorited")
    private boolean favorited;

    @Column(name="retweeted")
    private boolean retweeted;

    // Constructors
    public Tweet() {
        super();
    }

    public Tweet(JSONObject response) throws JSONException {
        super();

        tweet_id = response.getLong("id");
        created_at = parse_time(response.getString("created_at"));
        text = response.getString("text");

        // User
        JSONObject userJSON = response.getJSONObject("user");
        user_id = userJSON.getLong("id");

        TwitterUser user = TwitterUser.getById(user_id);
        if (user == null) {
            user = new TwitterUser(userJSON);
            user.save();
        }

        // Entities
        JSONObject entities = response.getJSONObject("entities");
        JSONArray urlJSON = entities.optJSONArray("urls");
        entity_urls = urlJSON == null ? "" : urlJSON.toString();
        JSONArray hashtagJSON = entities.optJSONArray("hashtags");
        entity_hashtags = hashtagJSON == null ? "" : hashtagJSON.toString();

        media_id = 0; //Only using first media object initially
        JSONArray mediaJSON = entities.optJSONArray("media");
        if (mediaJSON != null) {
            JSONObject firstMediaJSON = mediaJSON.optJSONObject(0);
            if (firstMediaJSON != null) {
                TwitterMedia twitterMedia = TwitterMedia.getById(firstMediaJSON.getLong("id"));
                if (twitterMedia == null) {
                    twitterMedia = new TwitterMedia(firstMediaJSON);
                    twitterMedia.save();
                }
                Log.d("tag", "Added item for " + tweet_id + ": " + twitterMedia.getMedia_url());
                media_id = twitterMedia.getMedia_id();
            }
        }
    }

    private Long parse_time(String twitterTime) {
        long dateMillis = 0;
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            dateMillis = sf.parse(twitterTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateMillis;
    }

    // Getters
    public Long getTweet_id() {
        return tweet_id;
    }

    public String getText() {
        return text;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public String getRelativeTimeCreated() {
        return DateUtils.getRelativeTimeSpanString(created_at,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
    }



    public TwitterUser getUser() {
        return TwitterUser.getById(user_id);
    }

    public TwitterMedia getMedia() {
        return TwitterMedia.getById(media_id);
    }





    public static Tweet getById(long id) {
        return new Select().from(Tweet.class).where("tweet_id = ?", id).executeSingle();
    }

    public static List<Tweet> recentItems() {
        return new Select().from(Tweet.class).orderBy("tweet_id DESC").limit("300").execute();
    }

    public static List<Tweet> itemsAfterId(long max_id) {
        return new Select()
                .from(Tweet.class)
                .where("tweet_id < ?", max_id)
                .orderBy("tweet_id DESC")
                .limit("300")
                .execute();
    }

    public static List<Tweet> itemsInRange(long min_id, long max_id, String order) {
        return new Select()
                .from(Tweet.class)
                .where("tweet_id <= ?", max_id)
                .where("tweet_id >= ?", min_id)
                .orderBy("tweet_id " + order)
                .limit("300")
                .execute();
    }


    public void favorited() {
        this.favorited = true;
        this.favorite_count ++;
        save();
    }

    public void unFavorited() {
        this.favorited = false;
        if (this.favorite_count > 0)
            this.favorite_count--;
        save();
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
        save();
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
        save();
    }

    public void setFavorite_count(int favorite_count) {
        this.favorite_count = favorite_count;
        save();
    }

    public void setRetweet_count(int retweet_count) {
        this.retweet_count = retweet_count;
        save();
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public int getRetweet_count() {
        return retweet_count;
    }
}


