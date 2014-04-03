package com.yi4all.callaudiohelper.broadcast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneMonitorService extends Service {

	static CustomPhoneStateListener phoneStateListener;
	
    @Override
    public IBinder onBind(Intent intent) {
        
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //register listener
    	if(phoneStateListener == null){
    		phoneStateListener = new CustomPhoneStateListener(this);
    	}
    	TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
    	telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregister listener
        phoneStateListener = null;
    }

}
