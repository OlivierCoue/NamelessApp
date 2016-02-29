package com.oliviercoue.nameless.services;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.oliviercoue.nameless.components.start.StartActivity;
import com.oliviercoue.nameless.network.NamelessRestClient;
import com.oliviercoue.nameless.notifications.NotificationTypes;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 19/02/2016.
 *
 */
public class CloseAppService extends Service{

    public static int NOTIFICATION_MESSAGE_ID = NotificationTypes.MESSAGE_RECEIVED;
    public static int NOTIFICATION_FRIEND_FIND_ID = NotificationTypes.FRIEND_FOUNDED;
    private final IBinder mBinder = new KillBinder(this);

    public class KillBinder extends Binder {

        public final Service service;

        public KillBinder(Service service) {
            this.service = service;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.cancel(NOTIFICATION_MESSAGE_ID);
        mNM.cancel(NOTIFICATION_FRIEND_FIND_ID);
        NamelessRestClient.post("chat/stop", null, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String s, Throwable t) {
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }
        });
    }

}
