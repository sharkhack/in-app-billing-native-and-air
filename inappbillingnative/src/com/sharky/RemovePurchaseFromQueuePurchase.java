/*
 * in-app-billing-native-and-air
 * https://github.com/sharkhack/
 *
 * Copyright (c) 2012 Azer Bulbul
 * Licensed under the MIT license.
 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
 */

package com.sharky;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

public class RemovePurchaseFromQueuePurchase implements FREFunction {
	
	@Override
	public FREObject call(FREContext arg0, FREObject[] arg1) {
		
		String receipt = null;
		try {
			receipt = arg1[1].getAsString();
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
		

		
		if (receipt == null)
		{
			return null;
		}
			
		JSONObject object = null;
		try {
			object = new JSONObject(receipt);
        } catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (object == null)
		{
			return null;
		}
				
		String signedData = null;
		try {
			signedData = object.getString("signedData");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (signedData == null)
		{
			return null;
		}
		
		JSONObject signedObject = null;
		
		try {

			signedObject = new JSONObject(signedData);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (signedObject == null)
		{
			return null;
		}
		
		Long nonce = 0L;
		try {
			nonce = signedObject.getLong("nonce");
        } catch (JSONException e) {
			e.printStackTrace();
		}
		
		JSONArray orders = null;
		try {
			orders = signedObject.getJSONArray("orders");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String[] notifyIds = new String[orders.length()];

		for (int i =0; i < orders.length(); i++)
		{
			JSONObject order = null;
			try {
				order = orders.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (order == null)
			{
				continue;
			}
			
			try {
				notifyIds[i] = order.getString("notificationId");
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		Intent intent = new Intent(Consts.ACTION_CONFIRM_NOTIFICATION);
        intent.setClass(arg0.getActivity(), BillingService.class);
        intent.putExtra(Consts.NOTIFICATION_ID, notifyIds);
        arg0.getActivity().startService(intent);
		
		Security.removeNonce(nonce);

		return null;
		
	}

}
