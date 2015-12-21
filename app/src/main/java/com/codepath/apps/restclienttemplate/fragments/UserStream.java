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

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsArrayAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.utils.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserStream extends Fragment {
    private RestClient client;
    private TweetsArrayAdapter adapter;

    private SwipeRefreshLayout swipeContainer;
    private ImageView ivProgress;

    long last_id;
    long user_id;


    public static UserStream newInstance(long user_id) {
        UserStream myFragment = new UserStream();

        Bundle args = new Bundle();
        args.putLong("user_id", user_id);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tweet_stream, container, false);

        user_id = getArguments().getLong("user_id", 0);

        ivProgress = (ImageView) v.findViewById(R.id.ivProgress);
        client = RestApplication.getRestClient();

        ArrayList<Tweet> tweets = new ArrayList<>();
        ListView lvTweets = (ListView) v.findViewById(R.id.lvTweets);
        lvTweets.setOnScrollListener(onEndlessScrollListener);
        adapter = new TweetsArrayAdapter(getActivity(), tweets);
        lvTweets.setAdapter(adapter);

        loadTweets();

        return v;
    }
    private EndlessScrollListener onEndlessScrollListener = new EndlessScrollListener() {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
           // startProgress();
            loadTweets();
            Toast.makeText(getActivity(), "Page " + page, Toast.LENGTH_SHORT).show();
        }
    };


    private void loadTweets() {
        if (user_id == 0) return;
        if (last_id == 0) adapter.clear();
        client.getUserTimeline(user_id, last_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                JSONObject tweetJSON;
                try {
                    for (int i = 0; i < response.length(); i++) {
                        tweetJSON = response.getJSONObject(i);
                        Tweet t = new Tweet(tweetJSON);
                        adapter.add(t);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    Log.e("blah", ex.toString());
                }
              //  stopProgress();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null)
                    try {
                        String message = errorResponse.getJSONArray("errors").getJSONObject(0).getString("message");
                        Toast.makeText(getActivity(), "Fetch Failed: " + message, Toast.LENGTH_LONG).show();
                    } catch (JSONException ex) {
                        Log.d("blah", errorResponse.toString());
                    }
                else
                    Log.d("blah", "Failed with status " + statusCode);
            }
        });

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
