package com.oliviercoue.httpwww.nameless.handlers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.oliviercoue.httpwww.nameless.activies.ChatActivity;
import com.oliviercoue.httpwww.nameless.models.User;
import com.oliviercoue.httpwww.nameless.notifications.MyNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Olivier on 07/02/2016.
 */
public class FriendFoundHandler {

    private Context context;

    public FriendFoundHandler(Context context, JSONObject data, boolean displayNotif) {
        this.context = context;
        run(data, displayNotif);
    }

    private void run(JSONObject data, boolean displayNotif) {
        try{
            User currentUser = User.fromJson( data.getJSONObject("currentUser").getJSONObject("data"));

            User friendUser = User.fromJson(data.getJSONObject("friend").getJSONObject("data"));

            Intent intentChatAct = new Intent(context, ChatActivity.class);
            if(displayNotif){
                MyNotificationManager myNotificationManager = new MyNotificationManager(context);
                myNotificationManager.displayFriendFoundNotifiaction(friendUser);
            }

            intentChatAct.putExtra("HAVE_NOTIFICATION", displayNotif);
            intentChatAct.putExtra("CURRENT_USER_ID", currentUser.getId());
            intentChatAct.putExtra("FRIEND_USER_ID", friendUser.getId());
            context.startActivity(intentChatAct);
            ((Activity) context).finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
