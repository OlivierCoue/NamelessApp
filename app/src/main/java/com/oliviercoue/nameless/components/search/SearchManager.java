package com.oliviercoue.nameless.components.search;

import android.content.Context;

import com.github.nkzawa.emitter.Emitter;
import com.oliviercoue.nameless.components.ActivityManager;
import com.oliviercoue.nameless.components.ActivityManagerImp;
import com.oliviercoue.nameless.handlers.FriendFoundHandler;

import org.json.JSONObject;

/**
 * Created by Olivier on 26/02/2016.
 */
public class SearchManager extends ActivityManager implements ActivityManagerImp {

    private Context context;
    private JSONObject serverResponse;
    private boolean friendFound = false;

    public SearchManager(SearchActivity searchActivity){
        super(searchActivity);
        this.context = searchActivity;
        getSocketManager().addSocketListener("friend_founded", onFriendFounded);
    }

    private Emitter.Listener onFriendFounded = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(!friendFound){
                friendFound = true;
                serverResponse = (JSONObject) args[0];
                new FriendFoundHandler(context, serverResponse, isActivityPauses());
            }
        }
    };

    @Override
    public void activityResume(){
        super.activityResume();
        if(friendFound)
            new FriendFoundHandler(context, serverResponse, false);
    }

}
