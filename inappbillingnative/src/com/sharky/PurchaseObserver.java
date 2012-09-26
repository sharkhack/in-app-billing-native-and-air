/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;

import com.sharky.BillingService.RequestPurchase;
import com.sharky.BillingService.RestoreTransactions;
import com.sharky.Consts.PurchaseState;
import com.sharky.Consts.ResponseCode;

public abstract class PurchaseObserver {
    private final Activity mActivity;
    private final Handler mHandler;
    private Method mStartIntentSender;
    private Object[] mStartIntentSenderArgs = new Object[5];
    private static final Class[] START_INTENT_SENDER_SIG = new Class[] {
        IntentSender.class, Intent.class, int.class, int.class, int.class
    };

    public PurchaseObserver(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;
        initCompatibilityLayer();
    }

    public abstract void onBillingSupported(boolean supported);
    public abstract void onPurchaseStateChange(PurchaseState purchaseState,
            String itemId, int quantity, long purchaseTime, String developerPayload);

    public abstract void onRequestPurchaseResponse(RequestPurchase request,
            ResponseCode responseCode);

    public abstract void onRestoreTransactionsResponse(RestoreTransactions request,
            ResponseCode responseCode);

    private void initCompatibilityLayer() {
        try {
            mStartIntentSender = mActivity.getClass().getMethod("startIntentSender",
                    START_INTENT_SENDER_SIG);
        } catch (SecurityException e) {
            mStartIntentSender = null;
        } catch (NoSuchMethodException e) {
            mStartIntentSender = null;
        }
    }

    void startBuyPageActivity(PendingIntent pendingIntent, Intent intent) {
    	if (mStartIntentSender != null) {
            try {
                mStartIntentSenderArgs[0] = pendingIntent.getIntentSender();
                mStartIntentSenderArgs[1] = intent;
                mStartIntentSenderArgs[2] = Integer.valueOf(0);
                mStartIntentSenderArgs[3] = Integer.valueOf(0);
                mStartIntentSenderArgs[4] = Integer.valueOf(0);
                mStartIntentSender.invoke(mActivity, mStartIntentSenderArgs);
            } catch (Exception e) {
                
            }
        } else {
            try {
                pendingIntent.send(mActivity, 0 , intent);
            } catch (CanceledException e) {
                
            }
        }
    }

    void postPurchaseStateChange(final PurchaseState purchaseState, final String itemId,
            final int quantity, final long purchaseTime, final String developerPayload) {
       
    	mHandler.post(new Runnable() {
        	
            public void run() {
                onPurchaseStateChange(
                        purchaseState, itemId, quantity, purchaseTime, developerPayload);
            }
        });
    }
}