package com.oliviercoue.httpwww.nameless.chat;

import android.graphics.Bitmap;

import com.oliviercoue.httpwww.nameless.models.User;

/**
 * Created by Olivier on 18/02/2016.
 */
public interface ChatAsyncResponse {

    void usersLoaded(User[] users);

    void nextUserFounded(User[] users);

    void onImageHandled(Bitmap image);

}
