package com.oliviercoue.nameless.chat;

import android.graphics.Bitmap;

import com.oliviercoue.nameless.models.User;

/**
 * Created by Olivier on 18/02/2016.
 *
 */
public interface ChatManagerImp {

    void onUsersLoaded(User[] users);

    void onNextUserFounded(User[] users);

    void onImageHandled(Bitmap image);

    void onStateChanged(boolean success, int state);

}
