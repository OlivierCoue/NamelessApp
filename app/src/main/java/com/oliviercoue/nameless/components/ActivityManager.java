package com.oliviercoue.nameless.components;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.nameless.network.socket.SocketManager;
import com.oliviercoue.nameless.network.socket.SocketManagerImp;
import com.oliviercoue.nameless.services.CloseAppService;

/**
 * Created by Olivier on 26/02/2016.
 *
 */
public class ActivityManager implements SocketManagerImp {

    private ServiceConnection closeAppServiceConnection;
    private SocketManager socketManager;
    private Snackbar networkInfoSnackBar;
    private Context context;
    private static boolean isAuthenticated = false;
    private boolean activityPaused = false;

    public ActivityManager(Context context){
        this.socketManager = new SocketManager(context, this);
        this.context = context;
        initCloseAppService();
        changeMultiTaskHeader();
    }

    private void initCloseAppService(){
        closeAppServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                ((CloseAppService.KillBinder) binder).service.startService(new Intent(context, CloseAppService.class));
            }
            public void onServiceDisconnected(ComponentName className) {
            }
        };
        context.bindService(new Intent(context, CloseAppService.class), closeAppServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void changeMultiTaskHeader(){
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.app.ActivityManager.TaskDescription taskDescription = new android.app.ActivityManager.TaskDescription(context.getResources().getString(R.string.app_name), null, context.getResources().getColor(R.color.white));
            ((Activity)context).setTaskDescription(taskDescription);
        }
    }

    private void unBindCloseAppService(){
        try {
            context.unbindService(closeAppServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void showNetworkInfoSnackBar(){
        View rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        networkInfoSnackBar = Snackbar.make(rootView, "No network connection.", Snackbar.LENGTH_INDEFINITE);
        networkInfoSnackBar.show();
    }

    @Override
    public void onConnectSuccess() {
        isAuthenticated = true;
        if(networkInfoSnackBar!=null)
            networkInfoSnackBar.dismiss();
    }

    @Override
    public void onDisconnected() {
        isAuthenticated = false;
        showNetworkInfoSnackBar();
    }

    public void activityPaused(boolean isFinishing){
        if(isFinishing)
            unBindCloseAppService();
        activityPaused = true;
        socketManager.activityPaused(isFinishing);
    }

    public void activityResume(){
        activityPaused = false;
        socketManager.activityResume();
    }

    public SocketManager getSocketManager(){
        return socketManager;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public boolean isActivityPauses(){
        return activityPaused;
    }
}
