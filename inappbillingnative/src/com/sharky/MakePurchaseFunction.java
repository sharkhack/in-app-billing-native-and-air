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

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class MakePurchaseFunction implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		String purchaseId = null;
		String developerPayload = null;
				
		try {
			purchaseId = arg1[0].getAsString();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FRETypeMismatchException e) {
			e.printStackTrace();
		} catch (FREInvalidObjectException e) {
			e.printStackTrace();
		} catch (FREWrongThreadException e) {
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try {
			developerPayload = arg1[1].getAsString();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (FRETypeMismatchException e) {
			e.printStackTrace();
		} catch (FREInvalidObjectException e) {
			e.printStackTrace();
		} catch (FREWrongThreadException e) {
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		BillingService service = new BillingService();
		service.setContext(arg0.getActivity());
		
		ResponseHandler.register( new CashPurchaseObserver(new Handler()));
		
		if (purchaseId != null)
		{
			service.requestPurchase(purchaseId, developerPayload);
		}
		
		return null;
	}

}
