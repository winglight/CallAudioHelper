package com.yi4all.callaudiohelper.db;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "person")
public class PersonModel implements Serializable{

	public  static final String CONTACT_NAME = "CONTACT_NAME";
	public  static final String CONTACT_ID = "CONTACT_ID";
	public  static final String CONTACT_HEAD_PHOTO = "CONTACT_HEAD_PHOTO";
	public  static final String CREATED_AT = "CREATED_AT";
	public  static final String LAST_PHONE_AT = "LAST_PHONE_AT";
	public  static final String PHONE_NUMBERS = "PHONE_NUMBERS";
	
	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(columnName = CONTACT_ID)
	private String contactId;// caller/callee ID
	@DatabaseField(columnName = CONTACT_NAME)
	private String contactName;
	@DatabaseField(columnName = CONTACT_HEAD_PHOTO)
	private String contactHeadPhoto;
	@DatabaseField(index = true, columnName = PHONE_NUMBERS)
	private String phoneNumbers;//format like, |xxxxx|xxxxx|, for query condition, like "%|xxxxx|%"
	@DatabaseField(columnName = LAST_PHONE_AT)
	private Date lastPhoneAt;
	@DatabaseField(columnName = CREATED_AT)
	private Date createdAt;
	
	public PersonModel(){
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public Date getLastPhoneAt() {
		return lastPhoneAt;
	}

	public void setLastPhoneAt(Date lastPhoneAt) {
		this.lastPhoneAt = lastPhoneAt;
	}

	public String getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(String phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public String getContactHeadPhoto() {
		return contactHeadPhoto;
	}

	public void setContactHeadPhoto(String contactHeadPhoto) {
		this.contactHeadPhoto = contactHeadPhoto;
	}
	
}
