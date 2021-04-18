package com.example.diginote.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.diginote.R;
import com.example.diginote.activities.AddImageActivity;
import com.example.diginote.activities.AddNotesActivity;
import com.example.diginote.activities.ChecklistActivity;
import com.example.diginote.activities.DrawActivity;
import com.example.diginote.activities.SpeechActivity;

public class AlarmBrodcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String text = bundle.getString("event");
        int noteId = bundle.getInt("noteId");
        String itemType = bundle.getString("itemType");
        String date = bundle.getString("date") + " " + bundle.getString("time");

        //Click on Notification
        Intent intent1 = null;
        if(itemType.equals("note"))
        {
            intent1 = new Intent(context, AddNotesActivity.class);
        }
        else if(itemType.equals("speech"))
        {
            intent1 = new Intent(context, SpeechActivity.class);
        }
        else if(itemType.equals("checklist"))
        {
            intent1 = new Intent(context, ChecklistActivity.class);
        }
        else if(itemType.equals("drawing"))
        {
            intent1 = new Intent(context, DrawActivity.class);
        }
        else if(itemType.equals("image"))
        {
            intent1 = new Intent(context, AddImageActivity.class);
        }

       // intent1 = new Intent(context, NotificationMessage.class);

        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("message", text);
        intent1.putExtra("noteId", noteId);

        //Notification Builder
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent1, PendingIntent.FLAG_ONE_SHOT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001");

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
        contentView.setTextViewText(R.id.message, text);
        contentView.setTextViewText(R.id.date, date);
        mBuilder.setSmallIcon(R.drawable.ic_alarm_white_24dp);
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH;
        mBuilder.setContent(contentView);
        mBuilder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        Notification notification = mBuilder.build();
        notificationManager.notify(1, notification);


    }
}
