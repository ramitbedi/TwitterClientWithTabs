package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.activities.DetailedViewActivity;
import com.codepath.apps.restclienttemplate.activities.HomeTimelineActivity;
import com.codepath.apps.restclienttemplate.activities.ViewProfileActivity;
import com.codepath.apps.restclienttemplate.models.Mentions;
import com.codepath.apps.restclienttemplate.models.TwitterMedia;
import com.codepath.apps.restclienttemplate.models.TwitterUser;
import com.codepath.apps.restclienttemplate.utils.Util;
import com.squareup.picasso.Picasso;

import java.util.List;



public class MentionsArrayAdapter extends ArrayAdapter {
    Context parentContext;
   RestClient client;

    private static class ViewHolder {
        ImageView ivUserProfile;
        TextView tvUserName;
        TextView tvUserScreenName;
        TextView tvTweet;
        TextView tvTime;
        ImageView ivMedia;
        ImageView ivReply;
        ImageView ivRetweet;
        TextView tvRetweet;
        ImageView ivFavorite;
        TextView tvFavorite;
    }

    public MentionsArrayAdapter(Context context, List<Mentions> mentions) {
        super(context, R.layout.tweet, mentions);
        parentContext = context;
        client = RestApplication.getRestClient();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tweet, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivUserProfile = (ImageView) convertView.findViewById(R.id.ivUserProfile);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
//            viewHolder.tvUserScreenName = (TextView) convertView.findViewById(R.id.tvUserScreenName);
            viewHolder.tvTweet = (TextView) convertView.findViewById(R.id.tvTweet);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            viewHolder.ivMedia = (ImageView) convertView.findViewById(R.id.ivMedia);
            viewHolder.ivReply = (ImageView) convertView.findViewById(R.id.ivReply);
            viewHolder.tvRetweet = (TextView) convertView.findViewById(R.id.tvRetweet);
            viewHolder.ivRetweet = (ImageView) convertView.findViewById(R.id.ivRetweet);
            viewHolder.tvFavorite = (TextView) convertView.findViewById(R.id.tvFavorite);
            viewHolder.ivFavorite = (ImageView) convertView.findViewById(R.id.ivFavorite);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Mentions mentions = (Mentions) getItem(position);
        TwitterUser user = mentions.getUser();
        TwitterMedia media = mentions.getMedia();


        viewHolder.ivUserProfile.setImageResource(0);
//        Bitmap profileImage = user.getBitmap(getContext());
//        if (profileImage != null)
//            viewHolder.ivUserProfile.setImageBitmap(profileImage);

        viewHolder.ivUserProfile.setTag(user.getUserId());
        Picasso.with(getContext()).load(user.getProfile_image_url()).resize(52, 52).into(viewHolder.ivUserProfile);
        viewHolder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long user_id = (long) v.getTag();
                Intent intent = new Intent(parentContext, ViewProfileActivity.class);
                intent.putExtra("user_id", user_id);
                parentContext.startActivity(intent);
            }
        });
        viewHolder.tvUserName.setTag(mentions.getTweet_id());
        viewHolder.tvUserName.setText(user.getName());
        viewHolder.tvUserName.setOnClickListener(viewDetailed);
//        viewHolder.tvUserScreenName.setText("@" + user.getScreen_name());
        viewHolder.tvTime.setText(mentions.getRelativeTimeCreated());
        viewHolder.tvTweet.setText(Html.fromHtml(mentions.getText()));
        viewHolder.tvRetweet.setText(mentions.getRetweet_count() > 0 ? String.valueOf(mentions.getRetweet_count()) : "");
        viewHolder.tvFavorite.setText(mentions.getFavorite_count() > 0 ? String.valueOf(mentions.getFavorite_count()) : "");
        viewHolder.ivMedia.setTag(mentions.getTweet_id());
        viewHolder.ivMedia.setImageResource(0); viewHolder.ivMedia.setImageDrawable(null);
        if (media != null) {
            Picasso.with(getContext()).load(media.getMedia_url()).into(viewHolder.ivMedia);
        }
        viewHolder.ivFavorite.setOnClickListener(viewDetailed);

        // Clickables
        viewHolder.ivReply.setTag(user.getUserId());
        viewHolder.ivReply.setOnClickListener(replySelected);

        viewHolder.ivRetweet.setTag(mentions.getTweet_id());
        if (mentions.isRetweeted()) viewHolder.ivRetweet.setImageResource(R.drawable.ic_retweeted);
        else viewHolder.ivRetweet.setImageResource(R.drawable.ic_retweet);


        viewHolder.ivFavorite.setTag(mentions.getTweet_id());
        if (mentions.isFavorited()) viewHolder.ivFavorite.setImageResource(R.drawable.ic_favorited);
        else viewHolder.ivFavorite.setImageResource(R.drawable.ic_favorite);


        return convertView;
    }

    private void incrementTv(TextView view) {
        if (view == null) return;

        int count = 1;
        if (view.getText() != null && !view.getText().toString().isEmpty())
            count = Integer.valueOf(view.getText().toString()) + 1;
        view.setText(String.valueOf(count));
    }

    private void decrementTv(TextView view) {
        if (view == null) return;

        if (!view.getText().toString().isEmpty()) {
            int count = Integer.valueOf(view.getText().toString()) - 1;
            view.setText(count > 0 ? String.valueOf(count) : "");
        }
    }

    private void addAnimation(View v) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 2, 1, 2, v.getWidth() / 2.0F, v.getHeight() / 2.0F);
        scaleAnimation.setDuration(500);
        v.startAnimation(scaleAnimation);
    }

    private View.OnClickListener viewDetailed = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long tweet_id = (long) v.getTag();
            Intent intent = new Intent(parentContext, DetailedViewActivity.class);
            intent.putExtra("tweet_id", tweet_id);
            parentContext.startActivity(intent);
        }
    };

    private View.OnClickListener replySelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!Util.isNetworkAvailable(parentContext)) {
                Toast.makeText(parentContext, "Network is not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            long user_id = (long) v.getTag();
            Log.d("blah", "Click Listener received id: " + user_id);
            if (parentContext instanceof HomeTimelineActivity)
                ((HomeTimelineActivity) parentContext).writeTweet(user_id);
        }
    };
//

}
