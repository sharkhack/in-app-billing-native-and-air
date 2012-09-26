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
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.sharky.Consts.PurchaseState;
import com.sharky.Consts.ResponseCode;

public class BillingService extends Service implements ServiceConnection {
     private static IMarketBillingService mService;

    private static LinkedList<BillingRequest> mPendingRequests = new LinkedList<BillingRequest>();

    private static HashMap<Long, BillingRequest> mSentRequests =
        new HashMap<Long, BillingRequest>();

   abstract class BillingRequest {
        private final int mStartId;
        protected long mRequestId;

        public BillingRequest(int startId) {
            mStartId = startId;
        }

        public int getStartId() {
            return mStartId;
        }

        public boolean runRequest() {
            if (runIfConnected()) {
                return true;
            }

            if (bindToMarketBillingService()) {
               mPendingRequests.add(this);
                return true;
            }
            return false;
        }

       public boolean runIfConnected() {
            if (mService != null) {
                try {
                    mRequestId = run();
                    if (mRequestId >= 0) {
                        mSentRequests.put(mRequestId, this);
                    }
                    return true;
                } catch (RemoteException e) {
                    onRemoteException(e);
                }
            }
            return false;
        }

        protected void onRemoteException(RemoteException e) {
            mService = null;
        }

        abstract protected long run() throws RemoteException;

        protected void responseCodeReceived(ResponseCode responseCode) {}

        protected Bundle makeRequestBundle(String method) {
            Bundle request = new Bundle();
            request.putString(Consts.BILLING_REQUEST_METHOD, method);
            request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
            request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, getPackageName());
            return request;
        }

        protected void logResponseCode(String method, Bundle response) {
            ResponseCode responseCode = ResponseCode.valueOf(
                    response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE));
            
        }
    }

    class CheckBillingSupported extends BillingRequest {
        public CheckBillingSupported() {
            super(-1);
        }

        @Override
        protected long run() throws RemoteException {
            Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
            Bundle response = mService.sendBillingRequest(request);
            int responseCode = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
            
            boolean billingSupported = (responseCode == ResponseCode.RESULT_OK.ordinal());
            ResponseHandler.checkBillingSupportedResponse(billingSupported);
            return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
        }
    }

    class RequestPurchase extends BillingRequest {
        public final String mProductId;
        public final String mDeveloperPayload;

        public RequestPurchase(String itemId) {
            this(itemId, null);
        }

        public RequestPurchase(String itemId, String developerPayload) {
            super(-1);
            mProductId = itemId;
            mDeveloperPayload = developerPayload;
        }

        @Override
        protected long run() throws RemoteException {
        	
            Bundle request = makeRequestBundle("REQUEST_PURCHASE");
            request.putString(Consts.BILLING_REQUEST_ITEM_ID, mProductId);
            if (mDeveloperPayload != null) {
                request.putString(Consts.BILLING_REQUEST_DEVELOPER_PAYLOAD, mDeveloperPayload);
            }
            Bundle response = mService.sendBillingRequest(request);
            PendingIntent pendingIntent
                    = response.getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT);
            if (pendingIntent == null) {
                return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
            }

            Intent intent = new Intent();
            ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
                    Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
        }

        
        
        @Override
        protected void responseCodeReceived(ResponseCode responseCode) {
        	ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
        }
    }

    class ConfirmNotifications extends BillingRequest {
        final String[] mNotifyIds;

        public ConfirmNotifications(int startId, String[] notifyIds) {
            super(startId);
            mNotifyIds = notifyIds;
        }

        @Override
        protected long run() throws RemoteException {
        
            Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
            request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
            Bundle response = mService.sendBillingRequest(request);
            logResponseCode("confirmNotifications", response);
            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
                    Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
        }
    }

    class GetPurchaseInformation extends BillingRequest {
        long mNonce;
        final String[] mNotifyIds;

        public GetPurchaseInformation(int startId, String[] notifyIds) {
            super(startId);
            mNotifyIds = notifyIds;
        }

        @Override
        protected long run() throws RemoteException {
            mNonce = Security.generateNonce();

            Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
            request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
            request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
            Bundle response = mService.sendBillingRequest(request);
            logResponseCode("getPurchaseInformation", response);
            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
                    Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
        }

        @Override
        protected void onRemoteException(RemoteException e) {
            super.onRemoteException(e);
            Security.removeNonce(mNonce);
        }
    }

    class RestoreTransactions extends BillingRequest {
        long mNonce;

        public RestoreTransactions() {
            super(-1);
        }

        @Override
        protected long run() throws RemoteException {
            mNonce = Security.generateNonce();
            Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
            request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
            Bundle response = mService.sendBillingRequest(request);
            logResponseCode("restoreTransactions", response);
            return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
                    Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
        }

        @Override
        protected void onRemoteException(RemoteException e) {
            super.onRemoteException(e);
            Security.removeNonce(mNonce);
        }

        @Override
        protected void responseCodeReceived(ResponseCode responseCode) {
        	ResponseHandler.responseCodeReceived(BillingService.this, this, responseCode);
        }
    }
    
    public BillingService() {
        super();
    }

    public void setContext(Context context) {
        attachBaseContext(context);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent, startId);
    }

    public void handleCommand(Intent intent, int startId) {
        String action = intent.getAction();
        if (Consts.ACTION_CONFIRM_NOTIFICATION.equals(action)) {
            String[] notifyIds = intent.getStringArrayExtra(Consts.NOTIFICATION_ID);
            confirmNotifications(startId, notifyIds);
        } else if (Consts.ACTION_GET_PURCHASE_INFORMATION.equals(action)) {
            String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
            getPurchaseInformation(startId, new String[] { notifyId });
        } else if (Consts.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            String signedData = intent.getStringExtra(Consts.INAPP_SIGNED_DATA);
            String signature = intent.getStringExtra(Consts.INAPP_SIGNATURE);
            purchaseStateChanged(startId, signedData, signature);
        } else if (Consts.ACTION_RESPONSE_CODE.equals(action)) {
            long requestId = intent.getLongExtra(Consts.INAPP_REQUEST_ID, -1);
            int responseCodeIndex = intent.getIntExtra(Consts.INAPP_RESPONSE_CODE,
                    ResponseCode.RESULT_ERROR.ordinal());
            ResponseCode responseCode = ResponseCode.valueOf(responseCodeIndex);
            checkResponseCode(requestId, responseCode);
        }
    }

    private boolean bindToMarketBillingService() {
        try {
            boolean bindResult = bindService(new Intent(Consts.MARKET_BILLING_SERVICE_ACTION), this,Context.BIND_AUTO_CREATE);
	    if (bindResult)
                return true;
           
        } catch (SecurityException e) {}
        return false;
    }

    public boolean checkBillingSupported() {
        return new CheckBillingSupported().runRequest();
    }

    public boolean requestPurchase(String productId, String developerPayload) {
        return new RequestPurchase(productId, developerPayload).runRequest();
    }

    public boolean restoreTransactions() {
        return new RestoreTransactions().runRequest();
    }

    private boolean confirmNotifications(int startId, String[] notifyIds) {
        return new ConfirmNotifications(startId, notifyIds).runRequest();
    }

    private boolean getPurchaseInformation(int startId, String[] notifyIds) {
        return new GetPurchaseInformation(startId, notifyIds).runRequest();
    }

    private void purchaseStateChanged(int startId, String signedData, String signature) {
        
    	if (Extension.context != null)
        {
            Extension.context.dispatchStatusEventAsync("PURCHASE_SUCCESSFUL", signedData);
        }
    }

    
    private void checkResponseCode(long requestId, ResponseCode responseCode) {
        BillingRequest request = mSentRequests.get(requestId);
        if (request != null) {
            if (Consts.DEBUG) {
                Log.d(TAG, request.getClass().getSimpleName() + ": " + responseCode);
            }
            request.responseCodeReceived(responseCode);
        }
        mSentRequests.remove(requestId);
    }

    private void runPendingRequests() {
        int maxStartId = -1;
        BillingRequest request;
        while ((request = mPendingRequests.peek()) != null) {
            if (request.runIfConnected()) {
                mPendingRequests.remove();

                if (maxStartId < request.getStartId()) {
                    maxStartId = request.getStartId();
                }
            } else {
                bindToMarketBillingService();
                return;
            }
        }

        if (maxStartId >= 0) {
            stopSelf(maxStartId);
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = IMarketBillingService.Stub.asInterface(service);
        runPendingRequests();
    }

    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    public void unbind() {
        try {
            unbindService(this);
        } catch (IllegalArgumentException e) {
           
        }
    }
}
