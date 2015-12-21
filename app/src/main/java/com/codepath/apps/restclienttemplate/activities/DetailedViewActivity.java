package com.codepath.apps.restclienttemplate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TwitterMedia;
import com.codepath.apps.restclienttemplate.models.TwitterUser;
import com.squareup.picasso.Picasso;



public class DetailedViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);
        Intent intent = getIntent();
        long tweet_id = intent.getLongExtra("tweet_id", 0);
        if (tweet_id > 0) {
            Tweet tweet = Tweet.getById(tweet_id);
            TwitterUser user = tweet.getUser();
            setTitle(user.getScreen_name());

            ((TextView) findViewById(R.id.tvFullName)).setText(user.getName());
            ((TextView) findViewById(R.id.tvDescription)).setText(user.getDescription());

            ((TextView) findViewById(R.id.tvTweet)).setText(tweet.getText());

            ImageView ivProfile = (ImageView) findViewById(R.id.ivUserProfile);
            ImageView ivMedia = (ImageView) findViewById(R.id.ivMedia);

            Picasso.with(this).load(user.getProfile_image_url()).into(ivProfile);
            ivMedia.setImageResource(0);
            TwitterMedia twitterMedia = tweet.getMedia();
            if (twitterMedia != null)
                Picasso.with(this).load(twitterMedia.getMedia_url()).into(ivMedia);

        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
