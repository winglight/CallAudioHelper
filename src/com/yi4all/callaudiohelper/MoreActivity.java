package com.yi4all.callaudiohelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;
import com.yi4all.callaudiohelper.fragment.WebFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MoreActivity extends BaseActivity {
	
	private boolean isShowTips = true;
	
	@Override     
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);   
		setContentView(R.layout.activity_more);		
		String[]datas = getResources().getStringArray(R.array.more);
		int [] icons ={R.drawable.advice_cell,R.drawable.share_cell,
				R.drawable.activity_cell,R.drawable.about_us_cell,R.drawable.terms_cell};
		ListView lv =(ListView)findViewById(R.id.activity_more_lv);
		lv.setAdapter(new ListAdapter(datas,icons));		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				switch (position) {
				case 0:	
					FeedbackAgent agent = new FeedbackAgent(MoreActivity.this);
				    agent.startFeedbackActivity();					
					break;
				case 1:
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
					intent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.share_value));
					Uri uri = Uri.parse("R.drawable.ic_launcher");
					intent.putExtra(Intent.EXTRA_STREAM,uri);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 			
					
					
					startActivity(Intent.createChooser(intent, getTitle()));					
					break;
				case 2:
					UmengUpdateAgent.forceUpdate(MoreActivity.this);			
					break;
				case 3:
					addFragment(WebFragment.newInstance("file:///android_asset/aboutus.html"), WebFragment.TAG, android.R.id.content);
					break;
				case 4:
					addFragment(WebFragment.newInstance("file:///android_asset/terms.html"), WebFragment.TAG, android.R.id.content);
					break;
				}
			}
		});
	}
	class ListAdapter extends BaseAdapter{
		private String[] datas;
		private int [] icons;
		private ListAdapter(String[] datas,int [] icons){
			this.datas = datas;
			this.icons = icons;
		}
		@Override
		public int getCount() {
			return datas.length;
		}

		@Override
		public Object getItem(int arg0) {
			return datas[arg0];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView item_tv;
			ImageView icon_iv;
			LayoutInflater inflater = LayoutInflater.from(MoreActivity.this);
			convertView = inflater.inflate(R.layout.more_lv_item, null);
			convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,100));
			item_tv = (TextView)convertView.findViewById(R.id.more_lv_item_tv);
			icon_iv = (ImageView)convertView.findViewById(R.id.more_lv_item_icon);
			item_tv.setText(datas[position]);
			icon_iv.setImageResource(icons[position]);
			return convertView;
		}		
	}	
	private File getFile(InputStream is){
		File file = new File("");
		OutputStream os;
		try {
			os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[]buffer = new byte[1024];
			while ((bytesRead = is.read(buffer, 0, 1024)) != -1) { 
				os.write(buffer, 0, bytesRead); 
				} 
				os.close(); 
				is.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return file;
	}
}

