package com.lucazanrosso.welchome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyNotification extends BroadcastReceiver {

    String notificationTitle;
    String notificationText;
    int notificationIcon;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.notificationTitle = intent.getStringExtra("notification_title");
        this.notificationText = intent.getStringExtra("notification_text");
        this.notificationIcon = R.drawable.ic_stat_logo;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1")
                .setSmallIcon(this.notificationIcon)
                .setContentTitle(this.notificationTitle)
                .setContentText(this.notificationText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationText))
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1",
                    "Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }
}
