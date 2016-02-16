package com.oliviercoue.httpwww.nameless.activies;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.adapters.ChatArrayAdapter;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.api.Url;
import com.oliviercoue.httpwww.nameless.models.Message;
import com.oliviercoue.httpwww.nameless.models.States;
import com.oliviercoue.httpwww.nameless.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 06/02/2016.
 */
public class ChatActivity extends AppCompatActivity {

    // UI references.
    private ListView messageListView;
    private EditText messageInput;
    private Button sendMessageButton;
    private Button nextButton;
    private Button cancelButton;
    private LinearLayout friendLeaveLayout;
    private ActionBar actionBar;

    private ChatArrayAdapter chatArrayAdapter;
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

        messageInput = (EditText) findViewById(R.id.message_input);
        messageListView = (ListView) findViewById(R.id.message_list_view);
        sendMessageButton = (Button) findViewById(R.id.message_send_button);
        nextButton = (Button) findViewById(R.id.next_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        friendLeaveLayout = (LinearLayout) findViewById(R.id.friend_leave_layout);
        actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        init(getIntent().getExtras().getInt("CURRENT_USER_ID"), getIntent().getExtras().getInt("FRIEND_USER_ID"));

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.message_right);
        messageListView.setAdapter(chatArrayAdapter);

        // ON SOCKET EVENT
        ioSocket.on("message_received", onMessageReceived);
        ioSocket.on("friend_quit", onFriendQuit);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendMessage();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                next();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                close();
            }
        });
    }

    private void init(Integer currentUserId, Integer friendId){

        NamelessRestClient.get("users/"+currentUserId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    currentUser = User.fromJson(response.getJSONObject("data"));
                    if(friendUser != null)setupUI();
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
                    if(currentUser != null)setupUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void next(){
        NamelessRestClient.get("chat/next", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(response.getBoolean("found")){
                        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.message_right);
                        messageListView.setAdapter(chatArrayAdapter);
                        currentUser = User.fromJson(response.getJSONObject("currentUser").getJSONObject("data"));
                        friendUser = User.fromJson(response.getJSONObject("friend").getJSONObject("data"));
                        setupUI();
                    }else{
                        Intent intentSearchAct = new Intent(getApplicationContext(), SearchActivity.class);
                        startActivity(intentSearchAct);
                        finish();
                    }
                    friendLeaveLayout.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void close(){
        NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Intent intentMainAct = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intentMainAct);
                finish();
            }
        });
    }

    private void closeAlert() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Confirmation");
        alert.setMessage("Do you really want to leave conversation ?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                close();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    private void setupUI(){
        actionBar.setTitle(friendUser.getUsername());
    }

    private boolean sendMessage() {
        String messageText =  messageInput.getText().toString();
        if(messageText != null && !messageText.isEmpty()) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("messageText", messageText);
            RequestParams params = new RequestParams(paramMap);
            NamelessRestClient.post("message", params, new JsonHttpResponseHandler() {});

            chatArrayAdapter.add(new Message(1, messageText, true, new Date()));
            messageInput.setText("");
            return true;
        }else{
            return false;
        }
    }

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject response = (JSONObject) args[0];
            Log.d(this.getClass().getName(), response.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        chatArrayAdapter.add(Message.fromJson(response.getJSONObject("message").getJSONObject("data")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    private Emitter.Listener onFriendQuit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject response = (JSONObject) args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(this.getClass().getName(), "friend leave");
                    friendLeaveLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Override
    public void onBackPressed() {
        closeAlert();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                next();
                return true;
            case android.R.id.home:
                close();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
