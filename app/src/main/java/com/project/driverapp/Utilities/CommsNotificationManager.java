package com.project.driverapp.Utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.project.driverapp.MainActivity;
import com.project.driverapp.R;

public class CommsNotificationManager {

    private Context context;
    private static CommsNotificationManager instance;
    private CommsNotificationManager(Context context){
        this.context = context;
    }

    public static synchronized CommsNotificationManager getInstance(Context context){
        if (instance == null){
            instance = new CommsNotificationManager(context);
        }
        return instance;
    }

    public void display(String Title, String body){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,Constants.channel_id);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle(Title).setContentText(body);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null){
            notificationManager.notify(1,builder.build());
        }
    }


}
