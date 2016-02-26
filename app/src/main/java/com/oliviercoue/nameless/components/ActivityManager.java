package com.oliviercoue.nameless.components;

import android.content.Context;

import com.oliviercoue.nameless.network.session.SessionManager;
import com.oliviercoue.nameless.network.session.SessionManagerImp;

/**
 * Created by Olivier on 26/02/2016.
 *
 */
public class ActivityManager implements SessionManagerImp{

    private ActivityManagerImp activityManagerImp;
    private SessionManager sessionManager;
    private static boolean isAuthenticated = false;
    private boolean activityPaused = false;

    public ActivityManager(Context context){
        this.activityManagerImp = (ActivityManagerImp) this;
        this.sessionManager = new SessionManager(context, this);
    }

    @Override
    public void onConnectSuccess() {
        isAuthenticated = true;
        activityManagerImp.onConnectionBack();
    }

    @Override
    public void onDisconnected() {
        isAuthenticated = false;
        activityManagerImp.onConnectionLost();
    }

    public void activityPaused(boolean isFinishing){
        activityPaused = true;
        sessionManager.activityPaused(isFinishing);
    }

    public void activityResume(){
        activityPaused = false;
        sessionManager.activityResume();
    }

    protected boolean isAuthenticated() {
        return isAuthenticated;
    }

    protected SessionManager getSessionManager(){
        return sessionManager;
    }

    protected boolean isActivityPauses(){
        return activityPaused;
    }
}
