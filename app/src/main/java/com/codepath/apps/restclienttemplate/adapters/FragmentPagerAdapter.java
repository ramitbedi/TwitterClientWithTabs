package com.codepath.apps.restclienttemplate.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.codepath.apps.restclienttemplate.fragments.MentionStream;
import com.codepath.apps.restclienttemplate.fragments.TweetFragmentStream;


public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private static int MAX_PAGES = 2;
    private String tabTitles[] = new String[] { "Home Stream", "Mentions" };

    private TweetFragmentStream tweetStream;
    private MentionStream mentionStream;

    public FragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (tweetStream == null) {
                    tweetStream = new TweetFragmentStream();
                }
                return tweetStream;
            case 1:
                if (mentionStream == null) {
                    mentionStream = new MentionStream();
                }
                return mentionStream;
        }
        return tweetStream;
    }

    @Override
    public int getCount() {
        return MAX_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
