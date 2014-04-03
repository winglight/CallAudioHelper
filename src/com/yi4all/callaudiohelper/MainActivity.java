package com.yi4all.callaudiohelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.umeng.update.UmengUpdateAgent;
import com.yi4all.callaudiohelper.db.PersonModel;
import com.yi4all.callaudiohelper.service.DBService;
import com.yi4all.callaudiohelper.util.DateUtils;
import com.yi4all.callaudiohelper.util.Utils;

public class MainActivity extends BaseActivity {

	private static final int REQ_GET_CONTACT = 1;

	private ListView listView;
	private List<PersonModel> persons = new ArrayList<PersonModel>();
	
	private TextView textView;
	
	private ActionMode mActionMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UmengUpdateAgent.update(this);
		
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.personList);
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
					final Intent i = new Intent(MainActivity.this, MemoActivity.class);
					i.putExtra("person", persons.get(position));

					startActivity(i);

				} else {
					// add or remove selection for current list item
					onListItemSelect(position);
				}
				
			}
		});
		
		textView = (TextView) findViewById(R.id.noPersonTxt);
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
				persons = getService().getPersons();
				
				if(persons == null || persons.size() == 0){
					//show hint
					textView.setVisibility(View.VISIBLE);
				} else {
					textView.setVisibility(View.GONE);
				}

				listView.setAdapter(new MyListAdapter(MainActivity.this));
			}
		}, 500);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add: {
			// create / import person from local contacts
			Uri uri = ContactsContract.Contacts.CONTENT_URI;
			System.out.println(uri);
			Intent intent = new Intent(Intent.ACTION_PICK, uri);
			startActivityForResult(intent, REQ_GET_CONTACT);

			return true;
		}
		case R.id.action_more: {
			// popup the more screen
			Intent intent = new Intent(MainActivity.this, MoreActivity.class);
			startActivity(intent);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onStart();

		refreshList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_GET_CONTACT) {
			switch (resultCode) {
			case Activity.RESULT_OK:
				// The Contacts API is about the most complex to use.
				// First we have to retrieve the Contact, since we only get its
				// URI from the Intent
				Uri resultUri = data.getData(); // e.g.,
												// content://contacts/people/123
				Cursor cont = getContentResolver().query(resultUri, null, null,
						null, null);
				if (!cont.moveToNext()) { // expect 001 row(s)
					Toast.makeText(this, "Cursor contains no data",
							Toast.LENGTH_LONG).show();
					return;
				}

				int columnIndexForId = cont
						.getColumnIndex(ContactsContract.Contacts._ID);
				String contactId = cont.getString(columnIndexForId);

				// Now we have to do another query to actually get the numbers!
				String[] projection = new String[] {
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						(Utils.hasHoneycomb()?ContactsContract.CommonDataKinds.Phone.PHOTO_URI: ContactsContract.CommonDataKinds.Phone.PHOTO_ID)};
				Cursor contact = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						projection,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
								+ contactId, // "selection",
						null, null);
				String phoneNumbers = "|";
				String personName = "";
				String photoUri = "";
				if (contact.moveToNext()) {
					personName = contact
							.getString(contact
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					photoUri = contact
							.getString(contact
									.getColumnIndex((Utils.hasHoneycomb()?ContactsContract.CommonDataKinds.Phone.PHOTO_URI: ContactsContract.CommonDataKinds.Phone.PHOTO_ID)));

					String aNumber = contact
							.getString(contact
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					phoneNumbers += aNumber + "|";
				}

				while (contact.moveToNext()) {
					String aNumber = contact
							.getString(contact
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					phoneNumbers += aNumber + "|";
				}
				if (phoneNumbers.length() < 3) {
					Toast.makeText(this,
							"Selected contact seems to have no phone numbers ",
							Toast.LENGTH_LONG).show();
					return;
				}
				final String name = personName;
				final String photo = photoUri;
				final String phones = phoneNumbers;
				if (cont.moveToNext()) {
					System.out
							.println("WARNING: More than one contact returned from picker!");
				}
				contact.close();
				cont.close();

				// judge if contactId exists in local db already
				final PersonModel person = getService()
						.findPersonByContactId(contactId);
				if (person != null) {
					// confirm to overwrite local person
					new AlertDialog.Builder(this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(R.string.confirm_overwrite_title)
							.setMessage(R.string.confirm_overwrite_msg)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											person.setContactName(name);
											person.setPhoneNumbers(phones);
											person.setContactHeadPhoto(photo);

											getService().updatePerson(person);
										}

									})
							.setNegativeButton(android.R.string.cancel, null)
							.show();
				} else {

					PersonModel person2 = new PersonModel();
					person2.setContactId(contactId);
					person2.setContactName(personName);
					person2.setContactHeadPhoto(photoUri);
					person2.setPhoneNumbers(phoneNumbers);
					person2.setCreatedAt(new Date());

					getService().createPerson(person2);
				}
				break;
			case Activity.RESULT_CANCELED:
				// nothing to do here
				break;
			default:
				Toast.makeText(this, "Unexpected resultCode: " + resultCode,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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
			return persons.size();
		}

		public PersonModel getItem(int i) {
			return persons.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			if (persons == null || position < 0 || position > persons.size())
				return null;

			final View row = mInflater.inflate(R.layout.person_list_item, null);

			ViewHolder holder = (ViewHolder) row.getTag();
			if (holder == null) {
				holder = new ViewHolder(row);
				row.setTag(holder);
			}

			// other normal row
			final PersonModel person = persons.get(position);

			// set name to label
			holder.name.setText(person.getContactName());

			// set phone numbers
			holder.phones.setText(Utils.stripPhoneNumbers(person.getPhoneNumbers()));

			holder.lastCall.setText(getString(R.string.last_call_datetime, DateUtils.formatMonthDayHourMinute(person.getLastPhoneAt())));

			holder.call.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// call by phone number
					Utils.call(MainActivity.this, person.getPhoneNumbers());
				}
			});

			if(person.getContactHeadPhoto() != null){
			holder.head.setImageURI(Uri.parse(person.getContactHeadPhoto()));
			}else{
				holder.head.setImageResource(R.drawable.default_head);
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
		TextView name = null;
		TextView phones = null;
		TextView lastCall = null;
		Button call = null;
		ImageView head = null;

		ViewHolder(View base) {
			this.name = (TextView) base.findViewById(R.id.row_name);
			this.phones = (TextView) base.findViewById(R.id.row_phones);
			this.lastCall = (TextView) base.findViewById(R.id.row_last_call);
			this.call = (Button) base.findViewById(R.id.row_call);
			this.head = (ImageView) base.findViewById(R.id.headPhoto);
		}
	}
	
	private class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// inflate contextual menu
			mode.getMenuInflater().inflate(R.menu.person_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			final List<PersonModel> selectedList = Utils.convertSelectedItem(persons, ((MyListAdapter) listView.getAdapter()).getSelectedIds());
			
			switch (item.getItemId()) {
			case R.id.action_person_delete:
				// delete Selected Items
				new AlertDialog.Builder(MainActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.confirm_delete)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for(PersonModel pm : selectedList){
							getService().deletePerson(pm);
						}
						refreshList();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
				
				
				mode.finish(); // Action picked, so close the CAB
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
}
