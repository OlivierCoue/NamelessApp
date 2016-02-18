package com.oliviercoue.httpwww.nameless.activies;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.api.Url;
import com.oliviercoue.httpwww.nameless.handlers.FriendFoundHandler;
import com.oliviercoue.httpwww.nameless.models.States;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 06/02/2016.
 */
public class SearchActivity extends AppCompatActivity {

    // UI references.
    private Button cancelSearchButton;

    private Activity activity;
    private boolean cancelClicked = false;
    private boolean isAway = false;
    private boolean friendFound = false;
    private Socket ioSocket;
    {
        try {
            ioSocket = IO.socket(Url.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seach);
        cancelSearchButton = (Button) findViewById(R.id.cancel_search_button);

        activity = this;

        ioSocket.on("friend_founded", onFriendFounded);

        cancelSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                cancel();
            }
        });
    }


    private void cancel(){
        if(!cancelClicked) {
            cancelClicked = true;
            NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Intent intentMainAct = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(intentMainAct);
                    finish();
                }
            });
        }
    }

    private Emitter.Listener onFriendFounded = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(!friendFound){
                friendFound = true;
                new FriendFoundHandler(activity, (JSONObject) args[0], isAway);
            }
        }
    };

    @Override
    public void onBackPressed() {
        cancel();
    }

    @Override
    protected void onResume(){
        super.onResume();
        isAway = false;
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(!isFinishing()) {
            isAway = true;
        }
    }
}
