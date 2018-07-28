package com.eatlah.eatlah.activities.General;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.helpers.MyFirebaseMessagingService;
import com.eatlah.eatlah.helpers.NotificationUtils;
import com.eatlah.eatlah.setup.Config;

public class NotificationsViewActivity extends AppCompatActivity {

    private static final String TAG = NotificationsViewActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView txtTitle, txtBody;
    private ImageView stage1, stage2, stage3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Order Progress Notifications Page");
        setSupportActionBar(toolbar);

        Log.d(TAG, "on create called");

        txtTitle = (TextView) findViewById(R.id.txtTitle_textView);
        txtBody = (TextView) findViewById(R.id.txtBody_textView);
        stage1 = findViewById(R.id.stage1_imageView);
        stage2 = findViewById(R.id.stage2_imageView);
        stage3 = findViewById(R.id.stage3_imageView);
        setTextFields();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String[] title = intent.getStringExtra(MyFirebaseMessagingService.MSG_TITLE).split("@");
                    int stage = Integer.parseInt(title[0]);
                    String msg_title = title[1];

                    Log.d(TAG, "stage: " + stage);
                    Log.d(TAG, "msg title: " + msg_title);

                    String msg_body = intent.getStringExtra(MyFirebaseMessagingService.MSG_BODY);

                    highlightStage(stage);

                    txtTitle.setText(msg_title);
                    txtBody.setText(msg_body);
                }
            }
        };
    }

    /** highlights the stage the order is currently at */
    private void highlightStage(int stage) {
        switch (stage) {
            case 1:
                removeTint(stage1);
                setTint(stage3);
                setTint(stage2);
                break;
            case 2:
                removeTint(stage2);
                setTint(stage1);
                setTint(stage3);
                break;
            case 3:
                removeTint(stage3);
                setTint(stage2);
                setTint(stage1);
                break;
        }
    }

    private void setTint(ImageView imageView) {
        Log.d(TAG, "setting color filter");
        imageView.setColorFilter(Color.argb(150, 255, 255, 255));
    }

    private void removeTint(ImageView imageView) {
        Log.d(TAG, "clearing color filter");
        imageView.clearColorFilter();
    }

    private void setTextFields() {
        Intent intent = getIntent();
        String[] title = intent.getStringExtra(MyFirebaseMessagingService.MSG_TITLE).split("@");
        int stage = Integer.parseInt(title[0]);
        String msg_title = title[1];

        Log.d(TAG, "stage: " + stage);
        Log.d(TAG, "msg title: " + msg_title);

        String msg_body = intent.getStringExtra(MyFirebaseMessagingService.MSG_BODY);

        highlightStage(stage);

        txtTitle.setText(msg_title);
        txtBody.setText(msg_body);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
