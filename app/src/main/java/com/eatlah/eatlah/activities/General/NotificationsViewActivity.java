package com.eatlah.eatlah.activities.General;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.eatlah.eatlah.Config;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.helpers.MyFirebaseMessagingService;
import com.eatlah.eatlah.helpers.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessaging;

public class NotificationsViewActivity extends AppCompatActivity {

    private static final String TAG = NotificationsViewActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView txtTitle, txtBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtTitle = (TextView) findViewById(R.id.txtTitle_textView);
        txtBody = (TextView) findViewById(R.id.txtBody_textView);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String msg_title = intent.getStringExtra(MyFirebaseMessagingService.MSG_TITLE);
                    String msg_body = intent.getStringExtra(MyFirebaseMessagingService.MSG_BODY);

                    Toast.makeText(getApplicationContext(), "Push notification (title): " + msg_title + " (body): " + msg_body, Toast.LENGTH_LONG).show();
                    txtTitle.setText(msg_title);
                    txtBody.setText(msg_body);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
