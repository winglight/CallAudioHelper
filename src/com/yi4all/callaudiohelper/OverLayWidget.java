package com.yi4all.callaudiohelper;

import java.util.Date;

import com.yi4all.callaudiohelper.db.MemoModel;
import com.yi4all.callaudiohelper.db.PersonModel;
import com.yi4all.callaudiohelper.service.DBService;
import com.yi4all.callaudiohelper.util.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class OverLayWidget extends LinearLayout {

	private String phoneNumber;
	private boolean isOut;
	private boolean isHide;

	private DBService service;
	private PersonModel person = null;
	private MemoModel lastMemo = null;

	public static OverLayWidget getInstance(Context context,
			String phoneNumber, boolean isOut) {
		OverLayWidget instance = new OverLayWidget(context);
		instance.setup(phoneNumber, isOut);

		return instance;
	}

	public OverLayWidget(Context context) {
		super(context);

		service = DBService.getInstance(context);
	}

	public void setup(String phoneNumber, boolean isOut) {
		this.phoneNumber = phoneNumber;
		this.isOut = isOut;

		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		LinearLayout overlay = (LinearLayout) inflater.inflate(
				R.layout.overlay, null);
		final TextView popMemoTxt = (TextView) overlay
				.findViewById(R.id.popMemoTxt);
		// get person by number
		person = service.findPersonByPhoneNumber(phoneNumber);
		if(person != null){
			person.setLastPhoneAt(new Date());
			service.updatePerson(person);
		}

		// get latest memo from db
		lastMemo = service.findLastMemoByPerson(person);
		if (lastMemo != null) {
			popMemoTxt.setText(lastMemo.getContent());
			isHide = false;
		}else{
			isHide = true;
		}

		// click to start main activity
		popMemoTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 1.start activity
				Intent i1 = new Intent(getContext(), MemoActivity.class);
				i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				i1.putExtra("person", person);

				getContext().startActivity(i1);
				// 2.hide this text view
				v.setVisibility(View.GONE);
				isHide = true;
			}
		});
		final Button helperBtn = (Button) overlay.findViewById(R.id.popRecBtn);
		helperBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (person == null) {
					// create a new person record
					person = getContactDisplayNameByNumber(OverLayWidget.this.phoneNumber);
					if(person.getContactName() == null){
						person.setContactName(OverLayWidget.this.phoneNumber);
					}
					person.setCreatedAt(new Date());
					person.setLastPhoneAt(new Date());
					service.createPerson(person);

				}
				
				// start memo activity
				if (lastMemo == null) {
					Intent i1 = new Intent(getContext(), MemoActivity.class);
					i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					i1.putExtra("person", person);

					getContext().startActivity(i1);

					return;
				}

				if (isHide) {
					popMemoTxt.setVisibility(View.VISIBLE);
					isHide = false;
					// refresh latest memo

				} else {
					//hide memo text
					popMemoTxt.setVisibility(View.GONE);
					isHide = true;
				}
			}
		});

		this.addView(overlay);
	}

	public PersonModel getContactDisplayNameByNumber(String number) {
		PersonModel pm = new PersonModel();
		pm.setPhoneNumbers("|" + number + "|");

		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = "?";

		ContentResolver contentResolver = getContext().getContentResolver();
		String[] projection = new String[] {
				BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME,
				(Utils.hasHoneycomb()?ContactsContract.PhoneLookup.PHOTO_URI: ContactsContract.PhoneLookup.PHOTO_ID) };
		Cursor contactLookup = contentResolver.query(uri, projection, null,
				null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				if (contactLookup.moveToNext()) {
					String contactId = contactLookup.getString(contactLookup
							.getColumnIndex(BaseColumns._ID));
					name = contactLookup
							.getString(contactLookup
									.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
					String photoUri = contactLookup
							.getString(contactLookup
									.getColumnIndex((Utils.hasHoneycomb()?ContactsContract.PhoneLookup.PHOTO_URI: ContactsContract.PhoneLookup.PHOTO_ID)));

					pm.setContactId(contactId);
					pm.setContactName(name);
					pm.setContactHeadPhoto(photoUri);
				}

			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return pm;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isOut() {
		return isOut;
	}

	public void setOut(boolean isOut) {
		this.isOut = isOut;
	}

}
