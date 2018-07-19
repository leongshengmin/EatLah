package com.eatlah.eatlah.helpers;

import android.content.SharedPreferences;
import android.util.Log;

import com.eatlah.eatlah.Config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * On initial startup of your app,
 * the FCM SDK generates a registration token for the client app instance.
 * If you want to target single devices, or create device groups,
 * you'll need to access this token.
 *
 * This class extends FirebaseInstanceIdService and by calling getToken
 * within onTokenRefresh, we can access the device registration token.
 */
public class FirebaseRegistrationToken extends FirebaseInstanceIdService {

    private static final String TAG = "FCM_token";
    private final FirebaseDatabase mDb;
    private final FirebaseAuth mAuth;

    public FirebaseRegistrationToken() {
        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    /**
     * this callback fires whenever a new token is generated,
     * so calling getToken() in its context ensures that you are accessing
     * a current, available registration token.
     *
     * FirebaseInstanceID.getToken() returns null if the token has not yet
     * been generated and the callback onTokenRefresh is executed if getToken()
     * returns null.
     */
    public void onTokenRefresh() {
        // Get updated InstanceId token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * send the FCM token generated to the server for cloud messaging service.
     *
     * Note the difference between Firebase.auth.uid & FirebaseInstanceId!!
     *
     * The Firebase Authentication ID token identifies the user.
     * A Firebase Authentication access token (auto-generated based on the ID token)
     * grants the user temporary access to the Firebase back-end.
     *
     * Firebase FirebaseInstanceId token (that is used by Firebase Cloud Messaging)
     * identifies the installation of the app on a specific device.
     *
     * If you sign in to an app on two different devices,
     * you will get the same authentication UID.
     * If you have the same app on two devices, the FCM token will be different.
     *
     * @param token
     */
    private void sendRegistrationToServer(String token) {

    }


    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.commit();
    }
}
