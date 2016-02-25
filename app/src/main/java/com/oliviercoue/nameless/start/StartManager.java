package com.oliviercoue.nameless.start;

import android.location.Location;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.activities.StartActivity;
import com.oliviercoue.nameless.api.NamelessRestClient;
import com.oliviercoue.nameless.security.Security;
import com.oliviercoue.nameless.security.SecurityImp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 24/02/2016.
 *
 */
public class StartManager implements SecurityImp {

    private static boolean isAuthenticated = false;

    private StartManagerImp startNetworkImp;
    private String username;
    private String socketId;
    private int searchRange = 10;
    private Location userLocation;

    private boolean isWaitingForUserInRange = false;

    public StartManager(StartActivity startActivity){
        this.startNetworkImp = startActivity;
        if(!isAuthenticated)
            authenticate();
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    private void authenticate(){
        Security security = new Security(this);
        security.authentication();
    }

    public void startChat(String username, String socketId, int searchRange){
        this.username = username;
        this.socketId = socketId;
        this.searchRange = searchRange;

        if(startChatParamsValid() && isAuthenticated)
            postStartChatInformation();
    }

    private boolean startChatParamsValid(){
        return username != null && !username.isEmpty() && socketId != null && !socketId.isEmpty() && userLocation != null;
    }

    private void postStartChatInformation(){
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("username", username);
        paramMap.put("socketId", socketId);
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
                startNetworkImp.onStartChatResult(true, serverResponse);
            } else {
                startNetworkImp.onStartChatResult(false, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUsersInRangeNumber(int searchRange){
        if(isAuthenticated && userLocation != null && !isWaitingForUserInRange) {
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
            startNetworkImp.onUsersInRangeNumberResult(serverResponse.getInt("friendNb"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAuthenticationSuccess(){
        isAuthenticated = true;
        getUsersInRangeNumber(searchRange);
    }
}
