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

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;

public class GetProductsInfoFunction implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		BillingService service = new BillingService();
		service.setContext(arg0.getActivity());
		
		ResponseHandler.register( new CashPurchaseObserver(new Handler()));
		
		service.restoreTransactions();
		
		return null;
	}

}
