package com.eatlah.eatlah.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.activities.General.NotificationsViewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This service is necessary to receive notifications in foregrounded apps,
 * to receive data payload, to send upstream messages, and so on.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "firebase Messaging";
    public static final int NOTIFICATIONS_REQUEST_CODE = 81;
    public static final String MSG_BODY = "messageBody";
    public static final String MSG_TITLE = "messageTitle";

    @Override
    public void onMessageSent(String msgId) {
        Log.e(TAG, "onMessageSent: " + msgId);
    }

    @Override
    public void onSendError(String msgId, Exception e) {
        Log.e(TAG, "onSendError: " + msgId);
        Log.e(TAG, "Exception: " + e);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // handle FCM messages here
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            System.out.println("parsing data message");
            Log.d(TAG, "Message data : " + remoteMessage.getData());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

    }

    private Intent createPushNotification(Intent pushNotificationIntent, String title, String body) {
        pushNotificationIntent.putExtra(MSG_TITLE, title);
        pushNotificationIntent.putExtra(MSG_BODY, body);
        return pushNotificationIntent;
    }

    private void handleDataMessage(JSONObject json) {
        System.out.println("handling data message");
        Log.d(TAG, "push json: " + json.toString());

        try {
            String title = json.getString(MSG_TITLE);
            String body = json.getString(MSG_BODY);
            boolean isBackground = json.getBoolean("is_background");
            String timestamp = json.getString("timestamp");
            String to = json.getString("to_uid");
            JSONObject payload = json.getJSONObject("payload");

            Log.d(TAG, "title: " + title);
            Log.d(TAG, "message: " + body);
            Log.d(TAG, "to: " + to);
            Log.d(TAG, "Current user UID: " + FirebaseAuth.getInstance().getUid());
            Log.d(TAG, "isBackground: " + isBackground);
            Log.d(TAG, "payload: " + payload);
            Log.d(TAG, "timestamp: " + timestamp);

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(getApplicationContext(), NotificationsViewActivity.class);
                pushNotification = createPushNotification(pushNotification, title, body);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(pushNotification);

                showNotificationMessage(getApplicationContext(), title, body, timestamp, pushNotification);
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), NotificationsViewActivity.class);
                resultIntent = createPushNotification(resultIntent, title, body);

                showNotificationMessage(getApplicationContext(), title, body, timestamp, resultIntent);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

        /**
         * creates a notification message on receipt of remoteMessage.
         * once user clicks on the notification, user will be directed to NotificationsViewActivity
         */
    private void createNotification(String msgBody, String msgTitle) {
        Intent intent = new Intent(this, NotificationsViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MSG_BODY, msgBody)
                .putExtra(MSG_TITLE, msgTitle);

        PendingIntent resultIntent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATIONS_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_ID = "channel_ver26up";
            final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, CHANNEL_IMPORTANCE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[] {100, 200, 300, 400});
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification.Builder mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher) //todo change icon to some food icon (EatLah logo)
                .setContentTitle(msgTitle)
                .setContentText(msgBody)
                .setAutoCancel(true)
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        notificationManager.notify(NOTIFICATIONS_REQUEST_CODE, mNotificationBuilder.build());
    }

}
