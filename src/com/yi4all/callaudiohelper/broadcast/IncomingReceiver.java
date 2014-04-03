package com.yi4all.callaudiohelper.broadcast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.yi4all.callaudiohelper.MainActivity;
import com.yi4all.callaudiohelper.OverLayWidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * auto start this service for register listener of phone call 
 * @author chenyu2
 *
 */
public class IncomingReceiver extends BroadcastReceiver {

	private WindowManager wm;
	private WindowManager.LayoutParams params;
    
    private LinearLayout overlay;
    
    private Context context;
    
    @Override
    public void onReceive(final Context context, Intent intent) {

    	this.context = context;
    	
                final String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if(incomingNumber ==  null) return;
                Log.d("APP", "incoming,ringing:" + incomingNumber);
            
            //register listener for close popup when quit
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int events = PhoneStateListener.LISTEN_CALL_STATE;
            tm.listen(phoneStateListener, events);
            
            //start popup window
            wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            
            params = new WindowManager.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);

//                params.height = LayoutParams.WRAP_CONTENT;
//                params.width = LayoutParams.WRAP_CONTENT;
//                params.format = PixelFormat.TRANSLUCENT;

                params.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
                
    }
    
    private void removeBtn(){
    	if(overlay !=null)
        {
            wm.removeView(overlay);
            overlay = null;
        }
    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

        	super.onCallStateChanged(state, incomingNumber);

            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
            	removeBtn();

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
            case TelephonyManager.CALL_STATE_RINGING:   
            	if(overlay == null){
            		overlay = OverLayWidget.getInstance(context, incomingNumber, false);
            	}
            	wm.addView(overlay, params);
                break;
            default:
                break;
            }
        }
    };
    
}