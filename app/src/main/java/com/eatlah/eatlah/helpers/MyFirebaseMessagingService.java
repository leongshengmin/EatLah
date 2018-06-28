package com.eatlah.eatlah.helpers;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * This service is necessary to receive notifications in foregrounded apps,
 * to receive data payload, to send upstream messages, and so on.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "firebase Messaging";
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
        }

        // if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
    }
}
