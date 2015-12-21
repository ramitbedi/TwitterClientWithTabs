package com.codepath.apps.restclienttemplate.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsArrayAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.utils.EndlessScrollListener;
import com.codepath.apps.restclienttemplate.utils.Util;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TweetFragmentStream extends Fragment{
    static final private int INITIAL = 0;
    static final private int ITEMS_NEWER = 1;
    static final private int ITEMS_OLDER = 2;

    private RestClient client;
    private TweetsArrayAdapter adapter;

    private SwipeRefreshLayout swipeContainer;
    private ImageView ivProgress;

    private long lvMaxId = 0;
    private long lvMinId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tweet_stream, container, false);

        ivProgress = (ImageView) v.findViewById(R.id.ivProgress);
        client = RestApplication.getRestClient();

        ArrayList<Tweet> tweets = new ArrayList<>();
        ListView lvTweets = (ListView) v.findViewById(R.id.lvTweets);
        lvTweets.setOnScrollListener(onEndlessScrollListener);
        adapter = new TweetsArrayAdapter(getActivity(), tweets);
        lvTweets.setAdapter(adapter);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(refreshListener);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTweets();
    }

    private EndlessScrollListener onEndlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            loadTweets(ITEMS_OLDER);
            Toast.makeText(getActivity(), "Page " + page, Toast.LENGTH_SHORT).show();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (Util.isNetworkAvailable(getActivity()))
                loadTweets(ITEMS_NEWER);
            else
                Toast.makeText(getActivity(),
                        "Network not available.  Please connect and try again.",
                        Toast.LENGTH_SHORT)
                        .show();
        }
    };

    private void loadTweets() {
        startProgress();
        loadTweets(INITIAL);
    }

    private void loadTweets(final int type) {
        long max_id = 0;
        long since_id = 0;

        if (!Util.isNetworkAvailable(getActivity())) {
            handleOffline(type);
        }

        if (type == ITEMS_NEWER)
            since_id = lvMaxId;
        else if (type == ITEMS_OLDER)
            max_id = lvMinId;

        client.getTimeline(max_id, since_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                JSONObject tweetJSON;
                long max_id = 0, min_id = 0;

                // Load data in database
                ActiveAndroid.beginTransaction();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        tweetJSON = response.getJSONObject(i);
                        long id = tweetJSON.getLong("id");

                        if (i == 0)
                            min_id = id;
                        if (id > max_id)
                            max_id = id;
                        else if (id < min_id)
                            min_id = id;

                        Tweet t = Tweet.getById(id);
                        if (t == null) {
                            t = new Tweet(tweetJSON);
                            t.save();
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    Log.e("tag", ex.toString());
                } finally {
                    ActiveAndroid.endTransaction();
                }

                if (type == INITIAL) {
                    adapter.clear();
                    lvMaxId = max_id;
                    lvMinId = min_id;
                }

                if (type == ITEMS_NEWER) {
                    for (Tweet t : Tweet.itemsInRange(min_id, max_id, "ASC")) {
                        adapter.insert(t, 0);
                    }
                    lvMaxId = max_id;
                } else {
                    for (Tweet t : Tweet.itemsInRange(min_id, max_id, "DESC")) {
                        adapter.add(t);
                    }
                    lvMinId = min_id;
                }
                stopProgress();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null)
                    try {
                        String message = errorResponse.getJSONArray("errors").getJSONObject(0).getString("message");
                        Toast.makeText(getActivity(), "Fetch Failed: " + message, Toast.LENGTH_LONG).show();
                    } catch (JSONException ex) {
                        Log.d("tag", errorResponse.toString());
                    }
                else
                    Log.d("tag", "Failed with status " + statusCode);
            }
        });
    }

    private void handleOffline(int type) {
        if (type == INITIAL) {
            for (Tweet t : Tweet.recentItems()) {
                long id = t.getTweet_id();
                if (lvMinId == 0 || lvMinId < id)
                    lvMinId = id;

                if (lvMaxId < id)
                    lvMaxId = id;

                adapter.add(t);
            }
        } else if (type == ITEMS_OLDER) {
            for (Tweet t : Tweet.itemsAfterId(lvMinId)) {
                long id = t.getTweet_id();
                if (lvMinId == 0 || lvMinId < id)
                    lvMinId = id;

                adapter.add(t);
            }
        }
        stopProgress();
        if (adapter.getCount() > 0)
            Toast.makeText(getActivity(),
                    "Internet not available, showing stored items.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "Internet is not available.  Please connect and try again.",
                    Toast.LENGTH_SHORT).show();
    }



    private void startProgress() {
        BitmapDrawable frame1 = (BitmapDrawable) getResources().getDrawable(R.mipmap.twitterbird1);
        BitmapDrawable frame2 = (BitmapDrawable) getResources().getDrawable(R.mipmap.twitterbird2);
        BitmapDrawable frame3 = (BitmapDrawable) getResources().getDrawable(R.mipmap.twitterbird3);
        BitmapDrawable frame4 = (BitmapDrawable) getResources().getDrawable(R.mipmap.twitterbird4);

        AnimationDrawable animationDrawable = new AnimationDrawable();
        animationDrawable.addFrame(frame1, 200);
        animationDrawable.addFrame(frame2, 200);
        animationDrawable.addFrame(frame3, 200);
        animationDrawable.addFrame(frame4, 200);
        animationDrawable.setOneShot(false);
        ivProgress.setBackground(animationDrawable);
        animationDrawable.start();
    }

    private void stopProgress() {
        swipeContainer.setRefreshing(false);
        ivProgress.setVisibility(View.GONE);
    }

}
