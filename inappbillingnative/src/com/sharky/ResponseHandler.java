/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sharky.BillingService.RequestPurchase;
import com.sharky.BillingService.RestoreTransactions;
import com.sharky.Consts.PurchaseState;
import com.sharky.Consts.ResponseCode;


public class ResponseHandler {

	private static PurchaseObserver sPurchaseObserver;

    public static synchronized void register(PurchaseObserver observer) {
        sPurchaseObserver = observer;
    }

    public static synchronized void unregister(PurchaseObserver observer) {
        sPurchaseObserver = null;
    }

	public static void checkBillingSupportedResponse(boolean supported) {

    	if (sPurchaseObserver != null) {
            sPurchaseObserver.onBillingSupported(supported);
        }
    }

    public static void buyPageIntentResponse(PendingIntent pendingIntent, Intent intent) {
       if (sPurchaseObserver == null) {
            return;
        }
        sPurchaseObserver.startBuyPageActivity(pendingIntent, intent);

    }

    public static void purchaseResponse(
            final Context context, final PurchaseState purchaseState, final String productId,
            final String orderId, final long purchaseTime, final String developerPayload) {

       if (sPurchaseObserver != null) {
            sPurchaseObserver.postPurchaseStateChange(
                    purchaseState, productId, 1, purchaseTime, developerPayload);
        }
    	
    }

    public static void responseCodeReceived(Context context, RequestPurchase request,
            ResponseCode responseCode) {
    	
    	
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRequestPurchaseResponse(request, responseCode);
        }
    }

    public static void responseCodeReceived(Context context, RestoreTransactions request,
            ResponseCode responseCode) {
    	
    	    	
        if (sPurchaseObserver != null) {
            sPurchaseObserver.onRestoreTransactionsResponse(request, responseCode);
        }
    }
}