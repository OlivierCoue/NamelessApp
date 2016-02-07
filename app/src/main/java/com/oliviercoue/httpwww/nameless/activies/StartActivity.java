package com.oliviercoue.httpwww.nameless.activies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.api.Url;
import com.oliviercoue.httpwww.nameless.handlers.FriendFoundHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Olivier on 06/02/2016.
 */
public class StartActivity extends AppCompatActivity{

    // UI references.
    private EditText    usernameView;
    private Button      startChatButton;


    private Activity activity;
    private static String usernameTxt;
    private static String socketId;
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
        setContentView(R.layout.activity_start);

        // init socket connection
        ioSocket.connect();
        ioSocket.on("connect_success", onConnectSuccess);

        usernameView = (EditText) findViewById(R.id.username);
        startChatButton = (Button) findViewById(R.id.start_chat_button);

        activity = this;

        if(usernameTxt != null && !usernameTxt.isEmpty()){
            usernameView.setText(usernameTxt);
        }

        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usernameTxt = usernameView.getText().toString();

                if(usernameTxt != null && !usernameTxt.isEmpty() && socketId != null && !socketId.isEmpty()) {
                    HashMap<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("username", usernameTxt);
                    paramMap.put("socketId", socketId);
                    RequestParams params = new RequestParams(paramMap);
                    NamelessRestClient.post("chat/start", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            try {
                                if (response.getBoolean("found")) {
                                    // user to speak with founded
                                    new FriendFoundHandler(activity, response);

                                } else {
                                    // user to speak with not founded
                                    Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
                                    startActivity(intentSearchAct);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        ;
                    });
                }else{
                    Log.d(this.getClass().getName(), "bad params");
                }
            }
        });
    }

    private Emitter.Listener onConnectSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                socketId = data.getString("socketId");
            } catch (JSONException e) {
                return;
            }
        }
    };
}

