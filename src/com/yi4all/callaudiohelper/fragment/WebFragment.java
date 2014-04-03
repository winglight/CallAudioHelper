package com.yi4all.callaudiohelper.fragment;

import com.yi4all.callaudiohelper.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class WebFragment extends Fragment{
	public final static String TAG = "FoodBibleFragment";
	public final static String FLAG = "FLAG";
	private String url;
	
	public static WebFragment newInstance(String url){
		WebFragment f = new WebFragment();
		f.url = url;
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		WebView webview = new WebView(container.getContext());
//		Log.d(TAG, url);
		webview.loadUrl(url);
		return webview;
	}
}


