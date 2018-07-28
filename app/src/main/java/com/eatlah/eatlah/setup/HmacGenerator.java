package com.eatlah.eatlah.setup;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

import static android.support.constraint.Constraints.TAG;

public class HmacGenerator {

    public static String generateSignature(String txnReq,String secretKey) throws Exception{
        String concatPayloadAndSecretKey = txnReq + secretKey;
        String hmac = encodeBase64(hashSHA256ToBytes(concatPayloadAndSecretKey.getBytes()));
        Log.d(TAG, "hmac: " + hmac);
        return hmac;
    }

    public static byte[] hashSHA256ToBytes(byte[] input) throws Exception {
        byte[] byteData = null;

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input);

        byteData = md.digest();

        return byteData;
    }

    public static String encodeBase64(byte[] data) {
        return Base64.encodeToString(data, 0);
    }

}
