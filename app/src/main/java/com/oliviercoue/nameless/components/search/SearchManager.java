package com.oliviercoue.nameless.components.search;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.github.nkzawa.emitter.Emitter;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.components.ActivityManager;
import com.oliviercoue.nameless.components.ActivityManagerImp;
import com.oliviercoue.nameless.components.start.StartActivity;
import com.oliviercoue.nameless.handlers.FriendFoundHandler;
import com.oliviercoue.nameless.models.States;
import com.oliviercoue.nameless.network.NamelessRestClient;
import com.oliviercoue.nameless.notifications.NotificationTypes;

import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 26/02/2016.
 *
 */
public class SearchManager extends ActivityManager implements ActivityManagerImp {

    private final int MAX_FRIEND_MISS = 5;

    private Context context;
    private JSONObject serverResponse;
    private boolean friendFound = false, haveMissedToMuch = false;
    private int friendMissed = 0;
    private NotificationManager notificationManager;

    public SearchManager(SearchActivity searchActivity){
        super(searchActivity);
        this.context = searchActivity;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        getSocketManager().addSocketListener("friend_founded", getOnFriendFounded());
        getSocketManager().addSocketListener("friend_quit", getOnFriendQuitListener());
    }

    private Emitter.Listener getOnFriendFounded(){
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (!friendFound) {
                    friendFound = true;
                    serverResponse = (JSONObject) args[0];
                    new FriendFoundHandler(context, serverResponse, isActivityPauses());
                }
            }
        };
    }

    private Emitter.Listener getOnFriendQuitListener(){
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                friendFound = false;
                notificationManager.cancel(NotificationTypes.FRIEND_FOUNDED);
                handleFriendMissed();
            }
        };
    }

    public void changeUserState(final int state){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                HashMap<String, String> paramMap = new HashMap<>();
                paramMap.put("state", String.valueOf(state));
                RequestParams params = new RequestParams(paramMap);
                NamelessRestClient.post("users/states", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String s, Throwable t) {
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    }
                });
                Looper.loop();
            }
        };
        thread.start();
    }

    private void handleFriendMissed(){
        friendMissed++;
        if(friendMissed == MAX_FRIEND_MISS) {
            changeUserState(States.CLOSED);
            haveMissedToMuch = true;
        }else
            changeUserState(States.SEARCHING);
    }

    private void handleToMuchFriendMiss(){
        Intent intentMainAct = new Intent(context, StartActivity.class);
        intentMainAct.putExtra("HAVE_MISSED_TO_MUSH_FRIEND", true);
        context.startActivity(intentMainAct);
        ((Activity)context).finish();
    }

    @Override
    public void activityResume(){
        super.activityResume();

        if(haveMissedToMuch)
            handleToMuchFriendMiss();
        else if(friendFound)
            new FriendFoundHandler(context, serverResponse, false);

        friendMissed = 0;
    }

}
