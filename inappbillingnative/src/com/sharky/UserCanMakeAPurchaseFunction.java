/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.adobe.fre.FREWrongThreadException;

public class UserCanMakeAPurchaseFunction implements FREFunction {

	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		BillingService service = new BillingService();
		
		
		// TODO Auo-generated method stub
		
		FREObject returnedValue;
		try {
			returnedValue = FREObject.newObject( service.checkBillingSupported() );
		} catch (FREWrongThreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			returnedValue = null;
		}
		return returnedValue;
	}

}
