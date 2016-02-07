package com.oliviercoue.httpwww.nameless.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.oliviercoue.httpwww.nameless.activies.ChatActivity;
import com.oliviercoue.httpwww.nameless.models.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Olivier on 07/02/2016.
 */
public class FriendFoundHandler {

    private Context context;

    public FriendFoundHandler(Context context, JSONObject data) {
        this.context = context;
        run(data);
    }

    private void run(JSONObject data) {
        try{
            User currentUser = User.fromJson( data.getJSONObject("currentUser").getJSONObject("data"));

            User friendUser = User.fromJson(data.getJSONObject("friend").getJSONObject("data"));

            Intent intentChatAct = new Intent(context, ChatActivity.class);
            intentChatAct.putExtra("CURRENT_USER_ID", currentUser.getId());
            intentChatAct.putExtra("FRIEND_USER_ID", friendUser.getId());
            context.startActivity(intentChatAct);
            ((Activity) context).finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
