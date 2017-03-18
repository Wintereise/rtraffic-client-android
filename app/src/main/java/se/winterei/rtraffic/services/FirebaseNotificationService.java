package se.winterei.rtraffic.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import se.winterei.rtraffic.R;
import se.winterei.rtraffic.activities.MainActivity;

/**
 * Created by reise on 3/18/2017.
 */

public class FirebaseNotificationService extends FirebaseMessagingService
{
    private static final String TAG = FirebaseNotificationService.class.getSimpleName();

    @Override
    public void onMessageReceived (RemoteMessage remoteMessage)
    {
        Log.d(TAG, "onMessageReceived: From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null)
            showNotification(remoteMessage.getNotification().getBody());
    }

    private void showNotification (String messageBody)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_explore_black_24dp)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
