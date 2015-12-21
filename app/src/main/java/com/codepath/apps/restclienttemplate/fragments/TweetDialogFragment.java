package com.codepath.apps.restclienttemplate.fragments;


import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.codepath.apps.restclienttemplate.models.TwitterUser;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
//import cz.msebera.android.httpclient.Header;
import org.json.JSONObject;



public class TweetDialogFragment extends DialogFragment {

    RestClient client;


    private View vBlackout;
    private EditText etEditor;


    private Button btnTweet;

    private long replyToId;



    public TweetDialogFragment() {
    }

    OnTweetSentListener onTweetSentListener;
    public interface OnTweetSentListener {
        void onTweetSent();
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_tweet, container);
        getDialog().setTitle(getResources().getString(R.string.compose_tweet));


        vBlackout = view.findViewById(R.id.vBlackout);
        etEditor = (EditText) view.findViewById(R.id.etTweetEditor);



        btnTweet = (Button) view.findViewById(R.id.btnSubmit);
        btnTweet.setOnClickListener(onTweetListener);

        if (replyToId > 0) {
            TwitterUser user = TwitterUser.getById(replyToId);
            if (user != null) {
                etEditor.setText("@" + user.getScreen_name() + " ");
                etEditor.setSelection(etEditor.getText().length());
            } else
                Log.d("tag", "Unable to find user with id: " + replyToId);
        }

        return view;
    }




    public View.OnClickListener onTweetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String status = etEditor.getText().toString();

            if (status.isEmpty()) {
                Toast.makeText(getActivity(), "Status is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            etEditor.setEnabled(false);

            btnTweet.setEnabled(false);

            vBlackout.setVisibility(View.VISIBLE);
            vBlackout.animate().alpha(0.5F).setDuration(2000);



            client = RestApplication.getRestClient();

            client.postStatus(status, "", onStatusSentHandler);

        }



        private JsonHttpResponseHandler onStatusSentHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("tag", "Status Response: " + response.toString());
                if (onTweetSentListener != null)
                    onTweetSentListener.onTweetSent();
                else
                    Log.d("tag", "Callback is null");

                dismiss();
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("tag", "Status Update failed with status: " + statusCode);
                if (errorResponse != null)
                    Log.d("tag", "Status Response: " + errorResponse.toString());
            }
        };



        };

}
