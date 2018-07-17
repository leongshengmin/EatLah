package com.eatlah.eatlah.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.NotificationsViewActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * This service is necessary to receive notifications in foregrounded apps,
 * to receive data payload, to send upstream messages, and so on.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "firebase Messaging";
    public static final int NOTIFICATIONS_REQUEST_CODE = 81;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // handle FCM messages here
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // create notification
            createNotification(remoteMessage.getNotification());
        }
    }

    /**
     * creates a notification message on receipt of remoteMessage.
     * once user clicks on the notification, user will be directed to NotificationsViewActivity
     * @param notification
     */
    private void createNotification(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, NotificationsViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultIntent = PendingIntent.getActivity(this, NOTIFICATIONS_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder mNotificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher) //todo change icon to some food icon (EatLah logo)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATIONS_REQUEST_CODE, mNotificationBuilder.build());
    }

}
