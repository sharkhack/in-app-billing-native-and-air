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
	
	import flash.events.EventDispatcher;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.system.Capabilities;
	
	public class Billing extends EventDispatcher
	{
		private static var _instance:Billing;
		
		private var extCtx:*;
		
		public function Billing()
		{
			if (!_instance)
			{
				extCtx = ExtensionContext.createExtensionContext("com.sharky", null);
				if (extCtx != null)
				{
					extCtx.addEventListener(StatusEvent.STATUS, onStatus);
				} 
				
				_instance = this;
			}
			
		}
		
		
		public static function getInstance():InAppPurchase
		{
			return _instance != null ? _instance : new InAppPurchase();
		}
		

		public function makePurchase(productId:String ):void
		{
			extCtx.call("makePurchase", productId);
		}
		
		public function removePurchaseFromQueue(productId:String, receipt:String):void
		{
			extCtx.call("removePurchaseFromQueue", productId, receipt);
		}

		public function restoreTransaction():void
		{
			extCtx.call("restoreTransaction");
		}
		
		
		private function onStatus(event:StatusEvent):void
		{
			trace(event);
			var e:BillingEvent;
			switch(event.code)
			{
				case "PURCHASE_SUCCESSFUL":
					e = new BillingEvent(BillingEvent.PURCHASE_SUCCESSFULL, event.level);
					break;
				case "PURCHASE_ERROR":
					e = new BillingEvent(BillingEvent.PURCHASE_ERROR, event.level);
					break;
				case "PURCHASE_ENABLED":
					e = new BillingEvent(BillingEvent.PURCHASE_ENABLED);
					break;
				case "PURCHASE_DISABLED":
					e = new BillingEvent(BillingEvent.PURCHASE_DISABLED);
					break;
				case "RESTORED_TRANSACTION":
					e = new BillingEvent(BillingEvent.RESTORED_TRANSACTION,event.level);
					break;
				default:
					
			}
			if (e)
			{
				this.dispatchEvent(e);
			}
			
		}
		
		
		
	}
}
