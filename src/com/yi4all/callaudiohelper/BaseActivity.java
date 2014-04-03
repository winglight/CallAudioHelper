package com.yi4all.callaudiohelper;


import java.io.IOException;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.yi4all.callaudiohelper.service.DBService;
   
public class BaseActivity extends SherlockFragmentActivity{

	private static final String IMAGE_CACHE_DIR = "images";
	
	//Service instance for RPC or DB
	private DBService service;
	
	private final Object mClickLock = new Object();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);     
        
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        
        Log.d("BaseActivity", "dpi:"+metrics.density*160);
        Log.d("BaseActivity", "n Brand => "+Build.BRAND);
        Log.d("BaseActivity", "n Device => "+Build.DEVICE);
    }
	
	public void addFragment(Fragment f, String tag,int id){
		synchronized (mClickLock) {
            final FragmentTransaction ft = getSupportFragmentManager().
            		beginTransaction();
            if(getSupportFragmentManager().findFragmentByTag(tag) == null){
            	ft.add(id, f, tag).addToBackStack(tag);
            	
            }else{
            	ft.replace(id, f, tag).addToBackStack(tag);
            }
            ft.commit();
            mClickLock.notifyAll();
		}
            
	}
	
	public void clearFragment(){
		synchronized (mClickLock) {
		final FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
		    fm.popBackStack();
		}
		ft.commit();
		mClickLock.notifyAll();
	}
	}
	
	public void backFragment(Fragment f){
		synchronized (mClickLock) {
		final FragmentTransaction ft = getSupportFragmentManager().
        		beginTransaction();
		ft.remove(f);
		ft.commit();
		mClickLock.notifyAll();
	}
	}
	
	@Override
	protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(service != null){
    		service.close();
    	}
    }
    
    public DBService getService(){
    	if(service == null){
    		service = DBService.getInstance(this);
    	}
    	return service;
    }

}
