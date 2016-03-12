package com.oliviercoue.nameless.network.socket;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.oliviercoue.nameless.network.NamelessRestClient;
import com.oliviercoue.nameless.network.NetworkStateImp;
import com.oliviercoue.nameless.network.NetworkStateReceiver;
import com.oliviercoue.nameless.network.Url;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Olivier on 26/02/2016.
 *
 */
public class SocketManager implements NetworkStateImp {

    private SocketManagerImp sessionManagerImp;
    private NetworkStateReceiver networkStateReceiver;
    private IntentFilter filter;
    private Context context;
    private static String socketId;
    private static boolean isSocketConnected = false;
    private List<String> socketListenersNames = new ArrayList<>();

    private Socket ioSocket;
    {
        try {
            ioSocket = IO.socket(Url.SOCKET_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public SocketManager(Context context, SocketManagerImp sessionManagerImp){
        this.sessionManagerImp = sessionManagerImp;
        this.context = context;

        initNetworkStateReceiver();
        addSocketListener("connect_success", getOnSocketConnectSuccess());
    }

    public void connectSocket(){
        ioSocket.connect();
    }

    private void initNetworkStateReceiver(){
        filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        networkStateReceiver = new NetworkStateReceiver(this);
    }

    public void addSocketListener(String name, Emitter.Listener listener){
        socketListenersNames.add(name);
        ioSocket.on(name, listener);
    }

    private Emitter.Listener getOnSocketConnectSuccess() {
        return new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String newSocketId = data.getString("socketId");
                    if (socketId != null)
                        updateServerSocketId(newSocketId);
                    else
                        setServerSocketId(newSocketId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void updateServerSocketId(final String newSocketId){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                HashMap<String, String> paramMap = new HashMap<>();
                paramMap.put("lastSocketId", socketId);
                paramMap.put("newSocketId", newSocketId);
                Log.d("hello", newSocketId);
                NamelessRestClient.post("users/updatesocket", new RequestParams(paramMap), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        sessionManagerImp.onConnectSuccess();
                        socketId = newSocketId;
                    }
                });
                Looper.loop();
            }
        };
        thread.start();
    }

    private void setServerSocketId(final String newSocketId){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                HashMap<String, String> paramMap = new HashMap<>();
                paramMap.put("socketId", newSocketId);
                NamelessRestClient.post("users/socket", new RequestParams(paramMap), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        sessionManagerImp.onConnectSuccess();
                        socketId = newSocketId;
                    }
                });
                Looper.loop();
            }
        };
        thread.start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void removeSocketListeners(){
        for(String name : socketListenersNames)
            ioSocket.off(name);
    }

    public void registerReceiver(){
        context.registerReceiver(networkStateReceiver, filter);
    }

    public void unregisterReceiver(){
        context.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void onConnectionLost() {
        if(!isNetworkAvailable()) {
            isSocketConnected = false;
            sessionManagerImp.onDisconnected();
        }
    }

    @Override
    public void onConnectionFound() {
        if(!isSocketConnected)
            connectSocket();
    }

    public void activityPaused(boolean isFinishing){
        unregisterReceiver();
        if(isFinishing)
            removeSocketListeners();
    }

    public void activityResume(){
        registerReceiver();
    }

}
