package com.yi4all.callaudiohelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.yi4all.callaudiohelper.db.MemoModel;
import com.yi4all.callaudiohelper.db.PersonModel;
import com.yi4all.callaudiohelper.fragment.MemoFragment;
import com.yi4all.callaudiohelper.service.DBService;
import com.yi4all.callaudiohelper.util.DateUtils;
import com.yi4all.callaudiohelper.util.Utils;

public class MemoActivity extends BaseActivity {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	private static final String TAG = "MemoActivity";

	private ListView listView;
	private List<MemoModel> memos = new ArrayList<MemoModel>();

	private PersonModel person;

	private boolean isSupportRecognize;

	private TextView textView;

	private ActionMode mActionMode;
	
	private SpeechRecognizer sr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memo);

		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null){
		actionBar.setDisplayHomeAsUpEnabled(true);
		}
		
		listView = (ListView) findViewById(R.id.memoList);
		// listView.setSelector(R.drawable.selector);
		// listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				onListItemSelect(position);
				return true;
			}
		});

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long l) {

				if (mActionMode == null) {
					popupMemoFragment(memos.get(position));

				} else {
					// add or remove selection for current list item
					onListItemSelect(position);
				}
			}
		});

		textView = (TextView) findViewById(R.id.noMemoTxt);

		Intent intent = getIntent();
		if (intent != null) {
			person = (PersonModel) getIntent().getSerializableExtra("person");

			// set title of the activity
			setTitle(getString(R.string.memo_title, person.getContactName()));

			// set name to label
			TextView nameTxt = (TextView) findViewById(R.id.person_name);
			TextView phoneTxt = (TextView) findViewById(R.id.person_phones);
			TextView lastCallTxt = (TextView) findViewById(R.id.person_last_call);
			TextView callBtn = (TextView) findViewById(R.id.person_call);
			ImageView photoTxt = (ImageView) findViewById(R.id.person_headPhoto);
			nameTxt.setText(person.getContactName());

			// set phone numbers
			phoneTxt.setText(Utils.stripPhoneNumbers(person.getPhoneNumbers()));

			lastCallTxt
					.setText(getString(R.string.last_call_datetime, DateUtils
							.formatMonthDayHourMinute(person.getLastPhoneAt())));

			callBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// call by phone number
					Utils.call(MemoActivity.this, person.getPhoneNumbers());
				}
			});

			if (person.getContactHeadPhoto() != null) {
				photoTxt.setImageURI(Uri.parse(person.getContactHeadPhoto()));
			} else {
				photoTxt.setImageResource(R.drawable.default_head);
			}

			// add memo button
			ImageView addMemoBtn = (ImageView) findViewById(R.id.person_record_btn);
			addMemoBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isSupportRecognize) {
						// start google voice recognize
						speak(null);
					} else {
						// hint voice recognize is not supported
						Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_not_supported);
					}
				}
			});

		}
	}

	private void onListItemSelect(int position) {
		((MyListAdapter) listView.getAdapter()).toggleSelection(position);
		boolean hasCheckedItems = ((MyListAdapter) listView.getAdapter())
				.getSelectedCount() > 0;

		if (hasCheckedItems && mActionMode == null){
			// there are some selected items, start the actionMode
			mActionMode = startActionMode(new ActionModeCallback());
		}else if (!hasCheckedItems && mActionMode != null){
			// there no selected items, finish the actionMode
			mActionMode.finish();
		}

		if (mActionMode != null){
			mActionMode.setTitle(getString(R.string.selected_amount, String.valueOf(((MyListAdapter) listView
					.getAdapter()).getSelectedCount())));
		}
	}

	public void refreshList() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				memos = getService().findMemosByPerson(person);
				if (memos == null || memos.size() == 0) {
					// show hint
					textView.setVisibility(View.VISIBLE);
				} else {
					textView.setVisibility(View.GONE);
				}

				listView.setAdapter(new MyListAdapter(MemoActivity.this));
			}
		}, 500);
	}

	public void checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			isSupportRecognize = false;
			Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_not_supported);
		} else {
			isSupportRecognize = true;
		}
	}

	public void speak(final MemoModel mm) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());

		// Display an hint to the user about what he should say.
		// intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
		// getString(R.string.speech_hint));

		// Given an hint to the recognizer about what the user is going to say
		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		// RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

		final ImageView iv = new ImageView(MemoActivity.this);
		iv.setImageResource(R.drawable.recording);

		if(sr == null){
			sr = SpeechRecognizer.createSpeechRecognizer(this);
		}
		sr.setRecognitionListener(new RecognitionListener() {

			AlertDialog dialog = null;
			byte[] sig = new byte[500000];
			int sigPos = 0;

			public void onBeginningOfSpeech() {
				Log.d(TAG, "onBeginningOfSpeech");
			}

			public void onRmsChanged(float rmsdB) {
				Log.d(TAG, "onRmsChanged");
			}

			public void onBufferReceived(byte[] buffer) {
				System.arraycopy(buffer, 0, sig, sigPos, buffer.length);
				sigPos += buffer.length;
				Log.d(TAG, "onBufferReceived");
			}

			public void onEndOfSpeech() {
				if (dialog != null) {
					dialog.dismiss();
				}
				Log.d(TAG, "onEndofSpeech");
			}

			public void onError(int error) {
				Log.d(TAG, "error " + error);
				if (error == RecognizerIntent.RESULT_AUDIO_ERROR) {
					Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_error_audio);
				} else if (error == RecognizerIntent.RESULT_CLIENT_ERROR) {
					Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_error_client);
				} else if (error == RecognizerIntent.RESULT_NETWORK_ERROR) {
					Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_error_network);
				} else if (error == RecognizerIntent.RESULT_NO_MATCH) {
					Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_error_nomatch);
				} else if (error == RecognizerIntent.RESULT_SERVER_ERROR) {
					Utils.showToastMessage(MemoActivity.this, R.string.voice_recognize_error_server);
				}
			}

			@Override
			public void onReadyForSpeech(Bundle params) {
				Log.d(TAG, "onReadyForSpeech");
				dialog = new AlertDialog.Builder(MemoActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.speech_title)
						.setMessage(R.string.speech_hint).setView(iv)
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			}

			@Override
			public void onResults(Bundle results) {
				Log.d(TAG, "onResults " + results);
				ArrayList<String> data = results
						.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				// populate the Matches
				if (!data.isEmpty()) {
					String result = data.get(0);
					// create or update a memo
					if(mm == null){
						MemoModel mm2 = new MemoModel();
						mm2.setContent(result);
						mm2.setPerson(person);
						mm2.setCreatedAt(new Date());

						if (sigPos > 0) {
							mm2.setVoice(Arrays.copyOfRange(sig, 0, sigPos));
						}
						// save record
						getService().createMemo(mm2);

					}else{
						mm.setContent(mm.getContent() + "\n" + result);
						mm.setPerson(person);
						mm.setCreatedAt(new Date());

						if (sigPos > 0) {
							if(mm.getVoice() != null){
								mm.setVoice(Utils.combineByteArray(mm.getVoice(), sig));
							}else{
								mm.setVoice(Arrays.copyOfRange(sig, 0, sigPos));
							}
						}
						// save record
						getService().updateMemo(mm);

					}
					
					refreshList();
				}

			}

			@Override
			public void onEvent(int arg0, Bundle arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPartialResults(Bundle partialResults) {
				// TODO Auto-generated method stub

			}

		});
		sr.startListening(intent);
		// startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * @Override protected void onActivityResult(int requestCode, int
	 *           resultCode, Intent data) { if (requestCode ==
	 *           VOICE_RECOGNITION_REQUEST_CODE)
	 * 
	 *           // If Voice recognition is successful then it returns RESULT_OK
	 *           if (resultCode == RESULT_OK) {
	 * 
	 *           ArrayList<String> textMatchList = data
	 *           .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	 * 
	 *           if (!textMatchList.isEmpty()) { // If first Match contains the
	 *           'search' word // Then start web search. if
	 *           (textMatchList.get(0).contains("search")) {
	 * 
	 *           String searchQuery = textMatchList.get(0).replace( "search",
	 *           " "); Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
	 *           search.putExtra(SearchManager.QUERY, searchQuery);
	 *           startActivity(search); } else { // populate the Matches String
	 *           result = textMatchList.get(0); // TODO: create a memo MemoModel
	 *           mm = new MemoModel(); mm.setContent(result);
	 *           mm.setPerson(person); // TODO: save record mm.setVoice(audio);
	 * 
	 *           service.createMemo(mm);
	 * 
	 *           refreshList(); }
	 * 
	 *           } // Result code for various error. } else if (resultCode ==
	 *           RecognizerIntent.RESULT_AUDIO_ERROR) {
	 *           Utils.showToastMessage(MemoActivity.this, "Audio Error"); } else if (resultCode ==
	 *           RecognizerIntent.RESULT_CLIENT_ERROR) {
	 *           Utils.showToastMessage(MemoActivity.this, "Client Error"); } else if (resultCode ==
	 *           RecognizerIntent.RESULT_NETWORK_ERROR) {
	 *           Utils.showToastMessage(MemoActivity.this, "Network Error"); } else if (resultCode ==
	 *           RecognizerIntent.RESULT_NO_MATCH) {
	 *           Utils.showToastMessage(MemoActivity.this, "No Match"); } else if (resultCode ==
	 *           RecognizerIntent.RESULT_SERVER_ERROR) {
	 *           Utils.showToastMessage(MemoActivity.this, "Server Error"); }
	 *           super.onActivityResult(requestCode, resultCode, data); }
	 */

	private void popupMemoFragment(final MemoModel bm) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				Fragment prev = getSupportFragmentManager().findFragmentByTag(
						"dialog");
				if (prev != null) {
					ft.remove(prev);
				}
				ft.addToBackStack(null);

				// Create and show the dialog.
				MemoFragment newFragment = MemoFragment.newInstance(bm);
				newFragment.show(ft, "dialog");

			}
		}, 1000);

	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.memo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add: {
			// popup a memo editor
			popupMemoFragment(null);

			return true;
		}
		case android.R.id.home: {
			finish();
	        return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onStart();

		refreshList();

		checkVoiceRecognition();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(sr != null){
			sr.destroy();
		}
	}

	public PersonModel getPerson() {
		return person;
	}

	class MyListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private SparseBooleanArray mSelectedItemsIds;

		public MyListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mSelectedItemsIds = new SparseBooleanArray();
		}

		@Override
		public int getCount() {
			return memos.size();
		}

		public MemoModel getItem(int i) {
			return memos.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			if (memos == null || position < 0 || position > memos.size())
				return null;

			final View row = mInflater.inflate(R.layout.memo_list_item, null);

			ViewHolder holder = (ViewHolder) row.getTag();
			if (holder == null) {
				holder = new ViewHolder(row);
				row.setTag(holder);
			}

			// other normal row
			final MemoModel rm = memos.get(position);

			// set name to label
			holder.content.setText(rm.getContent());

			// set phone numbers
			holder.datetime.setText(getString(R.string.memo_call_datetime,
					DateUtils.formatMonthDayHourMinute(rm.getCreatedAt())));

			// display call out/in icon on left side of datetime
			if (rm.isCallOut()) {
				holder.datetime.setCompoundDrawables(getResources()
						.getDrawable(R.drawable.call_out), null, null, null);
			} else {
				holder.datetime.setCompoundDrawables(getResources()
						.getDrawable(R.drawable.call_in), null, null, null);
			}

			if (rm.getVoice() == null) {
				holder.play.setVisibility(View.GONE);
			} else {
				holder.play.setVisibility(View.VISIBLE);
				holder.play.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// play record voice
						Utils.playPCM(rm.getVoice());
					}
				});
			}

			row.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
					: Color.TRANSPARENT);

			return (row);
		}

		public void toggleSelection(int position) {
			selectView(position, !mSelectedItemsIds.get(position));
		}

		public void removeSelection() {
			mSelectedItemsIds = new SparseBooleanArray();
			notifyDataSetChanged();
		}

		public void selectView(int position, boolean value) {
			if (value)
				mSelectedItemsIds.put(position, value);
			else
				mSelectedItemsIds.delete(position);

			notifyDataSetChanged();
		}

		public int getSelectedCount() {
			return mSelectedItemsIds.size();
		}

		public SparseBooleanArray getSelectedIds() {
			return mSelectedItemsIds;
		}
	}

	class ViewHolder {
		TextView content = null;
		TextView datetime = null;
		TextView play = null;

		ViewHolder(View base) {
			this.content = (TextView) base.findViewById(R.id.row_content);
			this.datetime = (TextView) base.findViewById(R.id.row_datetime);
			this.play = (TextView) base.findViewById(R.id.row_play);
		}
	}

	private class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// inflate contextual menu
			mode.getMenuInflater().inflate(R.menu.memo_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			final List<MemoModel> selectedList = Utils.convertSelectedItem(memos, ((MyListAdapter) listView.getAdapter()).getSelectedIds());
			
			switch (item.getItemId()) {
			case R.id.action_memo_combine:
				// merge a few memos
				MemoModel memo = new MemoModel();
				String content = "";
				byte[] sig = new byte[0];
					
				for(MemoModel mm : selectedList){
					if(mm.getContent() != null){
						content += "\n" + mm.getContent();
					}
					if(mm.getVoice() != null){
						sig = Utils.combineByteArray(sig, mm.getVoice());
					}
					getService().deleteMemo(mm);
				}
				memo.setContent(content);
				if(sig.length > 0){
					memo.setVoice(sig);
				}
				memo.setCreatedAt(new Date());
				memo.setPerson(person);
				getService().createMemo(memo);
				
				refreshList();
				
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.action_memo_delete:
				// delete Selected Items
				new AlertDialog.Builder(MemoActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.confirm_delete)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for(MemoModel mm : selectedList){
							getService().deleteMemo(mm);
						}
						refreshList();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
				
				
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.action_memo_share:
				// share only one memo at once
				if(selectedList.size() != 1){
					
				}else{
					Intent intent = createShareIntent(selectedList.get(0));
					if(intent != null){
						startActivity(intent);
					}
				mode.finish(); // Action picked, so close the CAB
				}
				return true;
				
			default:
				return false;
			}

		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// remove selection
			((MyListAdapter) listView.getAdapter()).removeSelection();
			mActionMode = null;
		}
	}
	
	private Intent createShareIntent(MemoModel mm) {
		if (mm != null ) {
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT,
					mm.getContent() + " " + getString(R.string.share_suffix));
			if(mm.getVoice() != null){
			try 
		    {
		        // create temp file that will hold byte array
		        File tempMp3 = File.createTempFile("memo", ".wav", getCacheDir());
		        tempMp3.deleteOnExit();
		        
		        FileOutputStream out = new FileOutputStream(tempMp3);  
	            writeWaveFileHeader(new FileOutputStream(tempMp3), mm.getVoice().length, mm.getVoice().length + 36, 8000, 1, 16000);  
	            out.write(mm.getVoice());  
	            out.close();  

		        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempMp3));
		        sendIntent.setType("audio/mp3");
		    } 
		    catch (IOException ex) 
		    {
		        String s = ex.toString();
		        ex.printStackTrace();
		    }
			}
			
			return sendIntent;
		}
		
        return null;
    }
	
	/** 
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav 
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 
     * 自己特有的头文件。 
     */  
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,  
            long totalDataLen, long longSampleRate, int channels, long byteRate)  
            throws IOException {  
        byte[] header = new byte[44];  
        header[0] = 'R'; // RIFF/WAVE header  
        header[1] = 'I';  
        header[2] = 'F';  
        header[3] = 'F';  
        header[4] = (byte) (totalDataLen & 0xff);  
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);  
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);  
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);  
        header[8] = 'W';  
        header[9] = 'A';  
        header[10] = 'V';  
        header[11] = 'E';  
        header[12] = 'f'; // 'fmt ' chunk  
        header[13] = 'm';  
        header[14] = 't';  
        header[15] = ' ';  
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk  
        header[17] = 0;  
        header[18] = 0;  
        header[19] = 0;  
        header[20] = 1; // format = 1  
        header[21] = 0;  
        header[22] = (byte) channels;  
        header[23] = 0;  
        header[24] = (byte) (longSampleRate & 0xff);  
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);  
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);  
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);  
        header[28] = (byte) (byteRate & 0xff);  
        header[29] = (byte) ((byteRate >> 8) & 0xff);  
        header[30] = (byte) ((byteRate >> 16) & 0xff);  
        header[31] = (byte) ((byteRate >> 24) & 0xff);  
        header[32] = (byte) (2 * 16 / 8); // block align  
        header[33] = 0;  
        header[34] = 16; // bits per sample  
        header[35] = 0;  
        header[36] = 'd';  
        header[37] = 'a';  
        header[38] = 't';  
        header[39] = 'a';  
        header[40] = (byte) (totalAudioLen & 0xff);  
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);  
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);  
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);  
        out.write(header, 0, 44);  
    }  
}
