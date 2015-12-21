package com.codepath.apps.restclienttemplate.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.adapters.FragmentPagerAdapter;
import com.codepath.apps.restclienttemplate.adapters.TweetsArrayAdapter;
import com.codepath.apps.restclienttemplate.fragments.TweetDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;





public class HomeTimelineActivity extends AppCompatActivity {
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter taa;
    FragmentPagerAdapter adapterViewPager;
    FragmentManager fm;

    long user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new FragmentPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(vpPager);
        updateScreename();

    }

    private void updateScreename() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String screen_name = prefs.getString("screen_name", "");
        setTitle("@" + screen_name);

        if (screen_name.isEmpty()) {
            RestClient client = RestApplication.getRestClient();
            client.getCurrentUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    SharedPreferences.Editor edit = prefs.edit();
                    try {
                        String screen_name = response.getString("screen_name");
                        setTitle("@" + screen_name);

                        user_id = response.getLong("id");
                        edit.putLong("user_id", user_id);
                        edit.putString("screen_name", screen_name);
                        edit.putString("name", response.getString("name"));
                        edit.putString("location", response.getString("location"));
                    } catch (JSONException e) {
                        Log.d("tag", "Unable to get user information.");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (errorResponse != null)
                        try {
                            String message = errorResponse.getJSONArray("errors").getJSONObject(0).getString("message");
                            Toast.makeText(HomeTimelineActivity.this, "Fetch Failed: " + message, Toast.LENGTH_LONG).show();
                        } catch (JSONException ex) {
                            Log.d("tag", errorResponse.toString());
                        }
                    else
                        Log.d("tag", "Failed with status " + statusCode);
                }
            });
        } else {
            user_id = prefs.getLong("user_id", 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_timeline, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_write_tweet) {
            writeTweet();
            return true;
        }
     else if (id == R.id.action_view_profile) {
        showProfile(user_id);
        return true;
    }

        return super.onOptionsItemSelected(item);
    }


    public void writeTweet() {
        writeTweet(1);
    }

    public void writeTweet(long replyTo) {
        fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }

        TweetDialogFragment dialog = new TweetDialogFragment();

        ft.addToBackStack(null);

        dialog.show(fm, "write_tweet_dialog_fragment");
    }

    private void showProfile(long user_id) {
        Intent intent = new Intent(HomeTimelineActivity.this, ViewProfileActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }

}
