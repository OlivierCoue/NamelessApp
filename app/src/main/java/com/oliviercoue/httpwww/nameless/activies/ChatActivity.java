package com.oliviercoue.httpwww.nameless.activies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;

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
import com.oliviercoue.httpwww.nameless.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 06/02/2016.
 */
public class ChatActivity extends AppCompatActivity {

    // UI references.
    private TextView currentUserNameView;
    private TextView friendUserNameView;
    private TextView infoView;
    private Button nextButton;
    private Button cancelButton;


    private User currentUser;
    private User friendUser;
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
        setContentView(R.layout.activity_chat);

        currentUserNameView = (TextView) findViewById(R.id.currentUserName);
        friendUserNameView = (TextView) findViewById(R.id.friendUserName);
        infoView = (TextView) findViewById(R.id.info);

        nextButton = (Button) findViewById(R.id.next_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);

        init(getIntent().getExtras().getInt("CURRENT_USER_ID"), getIntent().getExtras().getInt("FRIEND_USER_ID"));

        ioSocket.on("friend_quit", onFriendQuit);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                NamelessRestClient.get("chat/next", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            if(response.getBoolean("found")){

                                currentUser = User.fromJson(response.getJSONObject("currentUser").getJSONObject("data"));
                                friendUser = User.fromJson(response.getJSONObject("friend").getJSONObject("data"));

                                setupUI();
                            }else{
                                Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(intentSearchAct);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Intent intentMainAct = new Intent(getApplicationContext(), StartActivity.class);
                        startActivity(intentMainAct);
                        finish();
                    }
                });
            }
        });
    }

    private void init(Integer currentUserId, Integer friendId){

        NamelessRestClient.get("users/"+currentUserId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    currentUser = User.fromJson(response.getJSONObject("data"));
                    if(friendUser != null)
                        setupUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        NamelessRestClient.get("users/"+friendId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    friendUser = User.fromJson(response.getJSONObject("data"));
                    if(currentUser != null)
                        setupUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupUI(){
        infoView.setText("");
        currentUserNameView.setText(currentUser.getUsername());
        friendUserNameView.setText(friendUser.getUsername());
    }

    private Emitter.Listener onFriendQuit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject response = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        infoView.setText("Friend is not here anymore, reason : "+response.getString("reason"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        // change user state to CHATTING
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("state", States.CHATTING.toString());
        RequestParams params = new RequestParams(paramMap);
        NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {});
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(!isFinishing()){
            // change user state to AWAY
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("state", States.AWAY.toString());
            RequestParams params = new RequestParams(paramMap);
            NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {});
        }
    }

}
