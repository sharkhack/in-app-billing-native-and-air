package com.sharky
{
	/*
	 * in-app-billing-native-and-air
	 * https://github.com/sharkhack/
	 *
	 * Copyright (c) 2012 Azer Bulbul
	 * Licensed under the MIT license.
	 * https://github.com/sharkhack/in-app-billing-native-and-air/LICENSE-MIT
	 */

	import flash.events.Event;
	
	public class BillingEvent extends Event
	{
		
		public static const PURCHASE_SUCCESSFULL:String = "purchaseSuccesfull";
		public static const PURCHASE_ERROR:String   	= "purchaseError";
		
		public static const RESTORED_TRANSACTION:String = 'restoredTransaction';
		
		
		/*
			json encoded data,
			if call restored transaction or make purchase and than dispatch PURCHASE_SUCCESSFULL and this.data = 
			'{"orders":[{notificationId:xxxx,orderId:xxxx,packageName:xxx,productId:xxxx,purchaseTime:xxx,purchaseState:0/1},.....]}'
		*/
		public var data:String;
		
		public function BillingEvent(type:String, data:String = null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			this.data = data;
			super(type, bubbles, cancelable);
		}
	}
}
