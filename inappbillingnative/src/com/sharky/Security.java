/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;

import com.sharky.Consts.PurchaseState;
import com.sharky.util.Base64;
import com.sharky.util.Base64DecoderException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;

public class Security {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static HashSet<Long> sKnownNonces = new HashSet<Long>();

    public static class VerifiedPurchase {
        public PurchaseState purchaseState;
        public String notificationId;
        public String productId;
        public String orderId;
        public long purchaseTime;
        public String developerPayload;

        public VerifiedPurchase(PurchaseState purchaseState, String notificationId,
                String productId, String orderId, long purchaseTime, String developerPayload) {
            this.purchaseState = purchaseState;
            this.notificationId = notificationId;
            this.productId = productId;
            this.orderId = orderId;
            this.purchaseTime = purchaseTime;
            this.developerPayload = developerPayload;
        }
    }
    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        sKnownNonces.add(nonce);
        return nonce;
    }

    public static void removeNonce(long nonce) {
        sKnownNonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(nonce);
    }

    public static ArrayList<VerifiedPurchase> verifyPurchase(String signedData, String signature) {
        if (signedData == null) {
            Log.e(TAG, "data is null");
            return null;
        }
        if (Consts.DEBUG) {
            Log.i(TAG, "signedData: " + signedData);
        }
        boolean verified = false;
        if (!TextUtils.isEmpty(signature)) {
            //write your public key
            String base64EncodedPublicKey = "XXXXXXXXXXXXXXXXXXXXXXXXX";
            PublicKey key = Security.generatePublicKey(base64EncodedPublicKey);
            verified = Security.verify(key, signedData, signature);
            if (!verified) {
                return null;
            }
        }

        JSONObject jObject;
        JSONArray jTransactionsArray = null;
        int numTransactions = 0;
        long nonce = 0L;
        try {
            jObject = new JSONObject(signedData);
            nonce = jObject.optLong("nonce");
            jTransactionsArray = jObject.optJSONArray("orders");
            if (jTransactionsArray != null) {
                numTransactions = jTransactionsArray.length();
            }
        } catch (JSONException e) {
            return null;
        }

        if (!Security.isNonceKnown(nonce)) {
            return null;
        }

        ArrayList<VerifiedPurchase> purchases = new ArrayList<VerifiedPurchase>();
        try {
            for (int i = 0; i < numTransactions; i++) {
                JSONObject jElement = jTransactionsArray.getJSONObject(i);
                int response = jElement.getInt("purchaseState");
                PurchaseState purchaseState = PurchaseState.valueOf(response);
                String productId = jElement.getString("productId");
                String packageName = jElement.getString("packageName");
                long purchaseTime = jElement.getLong("purchaseTime");
                String orderId = jElement.optString("orderId", "");
                String notifyId = null;
                if (jElement.has("notificationId")) {
                    notifyId = jElement.getString("notificationId");
                }
                String developerPayload = jElement.optString("developerPayload", null);

                if (purchaseState == PurchaseState.PURCHASED && !verified) {
                    continue;
                }
                purchases.add(new VerifiedPurchase(purchaseState, notifyId, productId,
                        orderId, purchaseTime, developerPayload));
            }
        } catch (JSONException e) {
            return null;
        }
        removeNonce(nonce);
        return purchases;
    }

    public static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
           throw new IllegalArgumentException(e);
        } catch (Base64DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean verify(PublicKey publicKey, String signedData, String signature) {
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(Base64.decode(signature))) {
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            //code here
        } catch (InvalidKeyException e) {
           // code here
        } catch (SignatureException e) {
            // code here
        } catch (Base64DecoderException e) {
            // code here
        }
        return false;
    }
}

