package com.oliviercoue.nameless.components.chat;

import android.graphics.Bitmap;

import com.oliviercoue.nameless.models.User;

import org.json.JSONObject;

/**
 * Created by Olivier on 18/02/2016.
 *
 */
public interface ChatManagerImp {

    void onUsersLoaded(User[] users);

    void onNextUserFounded(User[] users);

    void onImageHandled(Bitmap image);

    void onStateChanged(boolean success, int state);

    void onMessageReceived(JSONObject serverResponse);

    void onFriendQuit(JSONObject serverResponse);

}
