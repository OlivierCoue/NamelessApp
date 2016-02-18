package com.oliviercoue.httpwww.nameless.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.oliviercoue.httpwww.nameless.R;
import com.oliviercoue.httpwww.nameless.activies.ChatActivity;
import com.oliviercoue.httpwww.nameless.models.Message;
import com.oliviercoue.httpwww.nameless.models.User;

/**
 * Created by Olivier on 17/02/2016.
 */
public class MyNotificationManager {

    private Context context;

    public MyNotificationManager(Context context){
        this.context = context;
    }

    public void displayMessageNotifiaction(Message message, User currentUser, User friendUser) {

        Intent resultIntent = new Intent(context, ChatActivity.class);

        resultIntent.putExtra("NOTIFICATION_ID", 123);
        resultIntent.putExtra("CURRENT_USER_ID", currentUser.getId());
        resultIntent.putExtra("FRIEND_USER_ID", friendUser.getId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), resultIntent, 0);

        Notification myNotification =  new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.mipmap.logo_nameless)
                                        .setContentTitle(message.getAuthor().getUsername())
                                        .setContentText(message.getMessageText())
                                        .setContentIntent(pIntent)
                                        .setAutoCancel(true).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, myNotification);
    }

}
