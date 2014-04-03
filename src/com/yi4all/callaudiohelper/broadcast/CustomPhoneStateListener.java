package com.yi4all.callaudiohelper.broadcast;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import com.yi4all.callaudiohelper.MainActivity;
import com.yi4all.callaudiohelper.OverLayWidget;

public class CustomPhoneStateListener extends PhoneStateListener {

    // private static final String TAG = "PhoneStateChanged";
    Context context; // Context to make Toast if required
    Intent i1;
    
    WindowManager wm;
    WindowManager.LayoutParams params;
    
    private LinearLayout overlay;
    
    private boolean isOut;
    
    public CustomPhoneStateListener(final Context context) {
        super();
        this.context = context;
        
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        params = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

//            params.height = LayoutParams.WRAP_CONTENT;
//            params.width = LayoutParams.WRAP_CONTENT;
//            params.format = PixelFormat.TRANSLUCENT;

            params.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
            
        i1 = new Intent(context, MainActivity.class);       
        i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
    }
    
    private void addBtn(String phoneNumber, boolean isOut){
    	if(overlay == null){
    		overlay = OverLayWidget.getInstance(context, phoneNumber, isOut);
        	wm.addView(overlay, params);
        }
    }
    
    private void removeBtn(){
    	if(overlay !=null)
        {
            wm.removeView(overlay);
            overlay = null;
        }
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
        	removeBtn();

            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
        	try {
                Thread.sleep(1000);
//                addBtn();           
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            break;
        case TelephonyManager.CALL_STATE_RINGING:           
            try {
                Thread.sleep(1000);
                //TODO:get incoming number
//                addBtn();             
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            break;
        default:
            break;
        }
    }
}