package com.oliviercoue.nameless.components.start;

import android.location.Location;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.components.ActivityManager;
import com.oliviercoue.nameless.components.ActivityManagerImp;
import com.oliviercoue.nameless.network.NamelessRestClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 24/02/2016.
 *
 */
public class StartManager extends ActivityManager implements ActivityManagerImp{

    private StartManagerImp startManagerImp;
    private String username;
    private int searchRange = 10;
    private Location userLocation;

    private boolean isWaitingForUserInRange = false;

    public StartManager(StartActivity startActivity){
        super(startActivity);
        this.startManagerImp = startActivity;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public boolean startChat(String username, int searchRange){
        this.username = username;
        this.searchRange = searchRange;

        if(startChatParamsValid() && super.isAuthenticated()) {
            postStartChatInformation();
            return true;
        }else
            return false;
    }

    private boolean startChatParamsValid(){
        return username != null && !username.isEmpty() && userLocation != null;
    }

    private void postStartChatInformation(){
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("username", username);
        paramMap.put("lat", String.valueOf(userLocation.getLatitude()));
        paramMap.put("long", String.valueOf(userLocation.getLongitude()));
        paramMap.put("range", String.valueOf(searchRange));
        NamelessRestClient.post("chat/start", new RequestParams(paramMap), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                handleStartChatServerResponse(response);
            }
        });
    }

    private void handleStartChatServerResponse(JSONObject serverResponse){
        try {
            if (serverResponse.getBoolean("found")) {
                startManagerImp.onStartChatResult(true, serverResponse);
            } else {
                startManagerImp.onStartChatResult(false, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUsersInRangeNumber(int searchRange){
        if(super.isAuthenticated() && userLocation != null && !isWaitingForUserInRange) {
            isWaitingForUserInRange = true;
            NamelessRestClient.get("chat/count?lat=" + String.valueOf(userLocation.getLatitude()) + "&long=" + String.valueOf(userLocation.getLongitude()) + "&range=" + searchRange, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                    isWaitingForUserInRange = false;
                    handleUsersInRangeNumberServerResponse(response);
                }
            });
        }
    }

    private void handleUsersInRangeNumberServerResponse(JSONObject serverResponse){
        try {
            startManagerImp.onUsersInRangeNumberResult(serverResponse.getInt("friendNb"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionLost() {
        Log.d("hello", "connection lost");
    }

    @Override
    public void onConnectionBack() {

    }

}
