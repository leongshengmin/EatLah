package com.eatlah.eatlah.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.support.constraint.Constraints.TAG;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class QRCodeGenerator extends AsyncTask<Void, Void, Bitmap> {
    final String DISPLAY_MESSAGE;

    public QRCodeGenerator(final String _DISPLAY_MESSAGE) {
        DISPLAY_MESSAGE = _DISPLAY_MESSAGE;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        try {
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();

            String baseURL = "https://chart.googleapis.com/chart?";
            final String CHT = "cht=qr";
            final String CHS = "chs=300x300";
            String chl = String.format("chl=%s", DISPLAY_MESSAGE);

            URL url = new URL(String.format("%s%s&%s&%s", baseURL, CHT, CHS, chl));
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream input = conn.getInputStream();
            Bitmap qrCode = BitmapFactory.decodeStream(input);
            return qrCode;

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * for out of mem err
     * @param bm
     * @param newHeight
     * @param newWidth
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }
}
