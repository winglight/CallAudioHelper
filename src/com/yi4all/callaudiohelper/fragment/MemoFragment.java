package com.yi4all.callaudiohelper.fragment;

import java.util.Date;

import org.apache.http.cookie.SM;

import com.yi4all.callaudiohelper.MemoActivity;
import com.yi4all.callaudiohelper.R;
import com.yi4all.callaudiohelper.db.MemoModel;
import com.yi4all.callaudiohelper.service.DBService;
import com.yi4all.callaudiohelper.util.DateUtils;
import com.yi4all.callaudiohelper.util.Utils;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by ChenYu on 13-8-8.
 */
public class MemoFragment extends DialogFragment  {

    public final static String SM_DATA = "server";

    private MemoModel mm;
    
    private EditText contentTxt;
    private TextView datetimeTxt;
    
    private Button playBtn;
    
    private DBService dbService;

    public MemoFragment() {
    }

    public static MemoFragment newInstance(MemoModel sm) {
        final MemoFragment f = new MemoFragment();

        final Bundle args = new Bundle();
        args.putSerializable(SM_DATA, sm);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mm = getArguments() != null ? (MemoModel) getArguments().getSerializable(SM_DATA) : new MemoModel();
        
        dbService = DBService.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	getDialog().setTitle(R.string.memo_editor);
    	
        final View v = inflater.inflate(R.layout.fragment_memo, container, false);
        
        contentTxt = (EditText) v.findViewById(R.id.memoContentTxt);
        datetimeTxt =  (TextView) v.findViewById(R.id.memo_datetime);
        
        Button saveBtn = (Button) v.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mm == null){
					mm = new MemoModel();
				}
				mm.setContent(contentTxt.getText().toString());
				mm.setPerson(((MemoActivity)getActivity()).getPerson());
				mm.setCreatedAt(new Date());
				if(mm.getId() <= 0){
					//new book
					dbService.createMemo(mm);
				}else{
					//old book
					dbService.updateMemo(mm);
				}
				MemoFragment.this.dismiss();
				
				((MemoActivity)getActivity()).refreshList();
			}
		});
        
        playBtn = (Button) v.findViewById(R.id.playBtn);
        if(mm == null || mm.getVoice() == null){
        	playBtn.setBackgroundResource(R.drawable.record_button_check);
        	playBtn.setOnClickListener(new OnClickListener() {
    			
    			@Override
    			public void onClick(View v) {
    				MemoFragment.this.dismiss();
    				//voice recognize audio
    				((MemoActivity)getActivity()).speak(mm);
    			}
    		});
        }else{
        	playBtn.setBackgroundResource(R.drawable.volume);
        playBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//play audio
				Utils.playPCM(mm.getVoice());
			}
		});
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(mm != null && mm.getContent() != null){
        	contentTxt.setText(mm.getContent());
      		datetimeTxt.setText(getString(R.string.memo_call_datetime, DateUtils.formatMonthDayHourMinute(mm.getCreatedAt())));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    public void toastMsg(int resId, String... args) {
		final String msg = getActivity().getString(resId, args);
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}
}
