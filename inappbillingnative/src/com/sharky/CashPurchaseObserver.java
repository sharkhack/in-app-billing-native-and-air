/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
*/

package com.sharky;


import android.os.Handler;
import android.util.Log;

import com.sharky.BillingService.RequestPurchase;
import com.sharky.BillingService.RestoreTransactions;
import com.sharky.Consts.PurchaseState;
import com.sharky.Consts.ResponseCode;

public class CashPurchaseObserver extends PurchaseObserver {

	
    public CashPurchaseObserver(Handler handler) {
        super(Extension.context.getActivity(), handler);
    }

	@Override
	public void onBillingSupported(boolean supported) {
		if (supported)
		{
			Extension.context.dispatchStatusEventAsync("PURCHASE_ENABLED", "Yes");
		} else
		{
			Extension.context.dispatchStatusEventAsync("PURCHASE_DISABLED", "Yes");
		}
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState,
			String itemId, int quantity, long purchaseTime,
			String developerPayload) {
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request,
			ResponseCode responseCode) {
		if (responseCode != Consts.ResponseCode.RESULT_OK && responseCode != Consts.ResponseCode.RESULT_USER_CANCELED)
		{
			Extension.context.dispatchStatusEventAsync("PURCHASE_ERROR", responseCode.toString());
		}
		
	}

	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request,
			ResponseCode responseCode) {
		Extension.context.dispatchStatusEventAsync("RESTORED_TRANSACTION", responseCode.toString());
	}

}
