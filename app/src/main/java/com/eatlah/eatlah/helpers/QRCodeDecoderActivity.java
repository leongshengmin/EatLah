package com.eatlah.eatlah.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.eatlah.eatlah.R;
import com.eatlah.eatlah.models.Order;

public class QRCodeDecoderActivity extends Activity implements QRCodeReaderView.OnQRCodeReadListener {
    private QRCodeReaderView qrCodeReaderView;
    public static final int QRCODE_DECODER_REQUEST_CODE = 17;
    public static final int QRCODE_DECODER_CAMERA_PERMISSIONS = 16;
    public static final String DECODED_MESSAGE = "qrcode_decoded_message";
    public static final String DECODED_POINTS = "qrcode_decoded_points";
    public static final String ORDER = "order";

    public QRCodeDecoderActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_decoder);

        qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrcodeReaderView);
        requestPermissions();

        qrCodeReaderView.setOnQRCodeReadListener(this);

        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrCodeReaderView.setTorchEnabled(true);

    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        QRCODE_DECODER_CAMERA_PERMISSIONS);
            }
        } else {
            // permissions granted
            onPermissionsGranted();
        }
    }

    private void onPermissionsGranted() {
        qrCodeReaderView.setBackCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case QRCODE_DECODER_CAMERA_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    onPermissionsGranted();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Intent intent = new Intent();
                    intent.putExtra(ORDER, retrieveOrder());
                    setResult(RESULT_CANCELED, intent);
                    finishActivity(requestCode);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Intent intent = new Intent();
        intent.putExtra(DECODED_MESSAGE, text);
        intent.putExtra(DECODED_POINTS, points);
        intent.putExtra(ORDER, retrieveOrder());
        onActivityResult(QRCODE_DECODER_REQUEST_CODE, RESULT_OK, intent);
    }

    private Order retrieveOrder() {
        return (Order) getIntent().getSerializableExtra(ORDER);
    }

}
