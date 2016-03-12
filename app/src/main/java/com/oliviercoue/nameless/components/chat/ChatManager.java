package com.oliviercoue.nameless.components.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.github.nkzawa.emitter.Emitter;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.components.ActivityManager;
import com.oliviercoue.nameless.components.ActivityManagerImp;
import com.oliviercoue.nameless.components.search.SearchActivity;
import com.oliviercoue.nameless.components.start.StartActivity;
import com.oliviercoue.nameless.models.User;
import com.oliviercoue.nameless.network.NamelessRestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 18/02/2016.
 *
 */
public class ChatManager extends ActivityManager implements ActivityManagerImp{

    private ChatManagerImp chatManagerImp;
    private ChatImageHelper chatImageHelper;
    private Context context;
    private boolean canceling = false, friendUserHere = false;
    private Bundle extras;

    public ChatManager(ChatActivity chatActivity){
        super(chatActivity);
        context = chatActivity;
        chatManagerImp = chatActivity;
        chatImageHelper = new ChatImageHelper();
        if(initExtrasOk())
            initFriendUserHere();
        getSocketManager().addSocketListener("friend_enter", getOnFriendEnterListener());
        getSocketManager().addSocketListener("message_received", getOnMessageReceivedListener());
        getSocketManager().addSocketListener("friend_quit", getOnFriendQuitListener());
    }

    private boolean initExtrasOk(){
        extras = ((Activity)context).getIntent().getExtras();
        if(!isExtrasValid(extras)) {
            ((Activity) context).finish();
            return false;
        }else
            return true;
    }

    private boolean isExtrasValid(Bundle extras){
        return extras != null && extras.containsKey("CURRENT_USER_ID") && extras.containsKey("FRIEND_USER_ID") && extras.containsKey("COMMING_FROM_CLASS_NAME");
    }

    private void initFriendUserHere(){
        if(extras.getString("COMMING_FROM_CLASS_NAME").equals(SearchActivity.class.getName())) {
            friendUserHere = true;
            postFriendEnterConversation();
        }else
            friendUserHere = false;
    }

    private void postFriendEnterConversation(){
        HashMap<String, String> paramMap = new HashMap<>();
        NamelessRestClient.post("chat/enter", new RequestParams(paramMap), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }
        });
    }

    private Emitter.Listener getOnFriendEnterListener(){
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                friendUserHere = true;
                chatManagerImp.onFriendEnter((JSONObject) args[0]);
            }
        };
    }

    private Emitter.Listener getOnMessageReceivedListener(){
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                chatManagerImp.onMessageReceived((JSONObject) args[0]);
            }
        };
    }

    private Emitter.Listener getOnFriendQuitListener(){
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                chatManagerImp.onFriendQuit((JSONObject) args[0]);
            }
        };
    }

    public void loadUsers() {
        final User[] users = new User[2];
        NamelessRestClient.get("users/" + extras.getInt("CURRENT_USER_ID"), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    users[0] = User.fromJson(response.getJSONObject("data"));
                    chatManagerImp.onUsersLoaded(users);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        NamelessRestClient.get("users/" + extras.getInt("FRIEND_USER_ID"), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    users[1] = User.fromJson(response.getJSONObject("data"));
                    chatManagerImp.onUsersLoaded(users);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendMessage(String messageText){
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("messageText", messageText);
        NamelessRestClient.post("message", new RequestParams(paramMap), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }
        });
    }

    public void sendImage(String filePath){

        Bitmap thumbnailBM = chatImageHelper.getBitmap(filePath, 700);
        Bitmap fullBM = chatImageHelper.getBitmap(filePath, 1440);

        if(thumbnailBM!=null && fullBM !=null) {
            chatManagerImp.onImageHandled(thumbnailBM);
            ByteArrayInputStream thumbnailIS = chatImageHelper.toByteArray(thumbnailBM);
            ByteArrayInputStream fullIS = chatImageHelper.toByteArray(fullBM);

            RequestParams params = new RequestParams();
            params.put("thumbnail", thumbnailIS, "thumbnail.jpeg");
            params.put("full", fullIS, "thumbnail.jpeg");

            NamelessRestClient.post("message/image", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                }
            });
        }
    }

    public void changeUserState(final int state){
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("state", String.valueOf(state));
        RequestParams params = new RequestParams(paramMap);
        NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String s, Throwable t ){
                chatManagerImp.onStateChanged(false, state);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                chatManagerImp.onStateChanged(true, state);
            }
        });
    }

    public void next(){
        friendUserHere = false;
        NamelessRestClient.get("chat/next", null, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String s, Throwable t ){
                canceling = false;
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("found")) {
                        User[] users = new User[2];
                        users[0] = User.fromJson(response.getJSONObject("currentUser").getJSONObject("data"));
                        users[1] = User.fromJson(response.getJSONObject("friend").getJSONObject("data"));
                        chatManagerImp.onNextUserFounded(users);
                        canceling = false;
                    } else {
                        search();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void search(){
        if(!canceling) {
            canceling = true;
            Intent intentSearchAct = new Intent(context, SearchActivity.class);
            context.startActivity(intentSearchAct);
            ((Activity) context).finish();
        }
    }

    public void close(){
        if(!canceling) {
            NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String s, Throwable t) {
                    canceling = false;
                }
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Intent intentMainAct = new Intent(context, StartActivity.class);
                    context.startActivity(intentMainAct);
                    ((Activity) context).finish();
                }
            });
        }
    }

    public boolean isFriendUserHere() {
        return friendUserHere;
    }
}
