package com.oliviercoue.nameless.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.oliviercoue.nameless.network.session.SessionManager;

/**
 * Created by Olivier on 25/02/2016.
 *
 */
public class NetworkStateReceiver extends BroadcastReceiver{

    private NetworkStateImp networkStateImp;

    public NetworkStateReceiver(SessionManager networkStateImp){
        this.networkStateImp = networkStateImp;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                networkStateImp.onConnectionFound();
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                networkStateImp.onConnectionLost();
                Toast.makeText(context, "Connection Lost", Toast.LENGTH_LONG).show();
            }
        }
    }

}