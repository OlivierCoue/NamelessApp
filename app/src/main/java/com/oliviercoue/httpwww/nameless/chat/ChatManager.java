package com.oliviercoue.httpwww.nameless.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.httpwww.nameless.activities.ChatActivity;
import com.oliviercoue.httpwww.nameless.activities.SearchActivity;
import com.oliviercoue.httpwww.nameless.activities.StartActivity;
import com.oliviercoue.httpwww.nameless.api.NamelessRestClient;
import com.oliviercoue.httpwww.nameless.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 18/02/2016.
 */
public class ChatManager {

    private ChatAsyncResponse chatAsyncResponse;
    private Context context;
    private boolean canceling = false;
    private ChatImageHelper chatImageHelper;

    public ChatManager(ChatActivity chatActivity){
        context = chatActivity;
        chatAsyncResponse = chatActivity;
        chatImageHelper = new ChatImageHelper();
    }

    public void loadUsers(int currentUserId, int friendUserId) {
        final User[] users = new User[2];
        NamelessRestClient.get("users/" + currentUserId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    users[0] = User.fromJson(response.getJSONObject("data"));
                    chatAsyncResponse.usersLoaded(users);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        NamelessRestClient.get("users/" + friendUserId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    users[1] = User.fromJson(response.getJSONObject("data"));
                    chatAsyncResponse.usersLoaded(users);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendMessage(String messageText){
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("messageText", messageText);
        RequestParams params = new RequestParams(paramMap);
        NamelessRestClient.post("message", params, new JsonHttpResponseHandler() {
        });
    }

    public void sendImage(String filePath){

        Bitmap thumbnailBM = chatImageHelper.getBitmap(filePath, 700);
        chatAsyncResponse.onImageHandled(thumbnailBM);
        ByteArrayInputStream thumbnailIS = chatImageHelper.toByteArray(thumbnailBM);

        Bitmap fullBM = chatImageHelper.getBitmap(filePath, 1440);
        ByteArrayInputStream fullIS = chatImageHelper.toByteArray(fullBM);

        RequestParams params = new RequestParams();
        params.put("thumbnail", thumbnailIS, "thumbnail.jpeg");
        params.put("full", fullIS, "thumbnail.jpeg");

        NamelessRestClient.post("message/image", params, new JsonHttpResponseHandler() {
        });

    }

    public void next(){
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
                        chatAsyncResponse.nextUserFounded(users);
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

}
