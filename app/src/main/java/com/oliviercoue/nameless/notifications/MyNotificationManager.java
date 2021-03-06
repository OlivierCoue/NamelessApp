package com.oliviercoue.nameless.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;

import com.oliviercoue.nameless.R;
import com.oliviercoue.nameless.components.chat.ChatActivity;
import com.oliviercoue.nameless.models.Message;
import com.oliviercoue.nameless.models.User;

/**
 * Created by Olivier on 17/02/2016.
 *
 */
public class MyNotificationManager {

    private Context context;
    private Vibrator vibrator;

    public MyNotificationManager(Context context){
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void displayMessageNotifiaction(Message message) {
        Intent resultIntent = new Intent(context, ChatActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), resultIntent, 0);

        Notification myNotification =  createNotificion(message.getAuthor().getUsername(), message.getMessageText(), pIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NotificationTypes.MESSAGE_RECEIVED, myNotification);
        vibrator.vibrate(250);
    }

    public void displayFriendFoundNotifiaction(User currentUser, User friendUser) {

        Intent resultIntent = new Intent(context, ChatActivity.class);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        resultIntent.putExtra("CURRENT_USER_ID", currentUser.getId());
        resultIntent.putExtra("FRIEND_USER_ID", friendUser.getId());

        PendingIntent pIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), resultIntent, 0);

        Notification myNotification =  createNotificion(context.getResources().getString(R.string.notification_friend_founded), context.getResources().getString(R.string.notification_friend_speak) + " " + friendUser.getUsername(), pIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NotificationTypes.FRIEND_FOUNDED, myNotification);
        vibrator.vibrate(250);
    }

    private Notification createNotificion(String title, String contentText, PendingIntent pIntent){
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.logo_nameless)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(contentText)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();
    }

}
