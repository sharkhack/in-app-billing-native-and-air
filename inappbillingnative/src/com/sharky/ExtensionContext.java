/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;

import java.util.HashMap;
import java.util.Map;


import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;

public class ExtensionContext extends FREContext {
	public ExtensionContext() {
	}
	
	@Override
	public void dispose() {
		Extension.context = null;
	}

	@Override
	public Map<String, FREFunction> getFunctions() {
		Map<String, FREFunction> functionMap = new HashMap<String, FREFunction>();
		functionMap.put("restoreTransaction", new RestoreTransactionFunction());
		functionMap.put("makePurchase", new MakePurchaseFunction());
		functionMap.put("removePurchaseFromQueue", new RemovePurchaseFromQueuePurchase());
		return functionMap;	
	}

}
