package com.yi4all.callaudiohelper.db;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "memo")
public class MemoModel implements Serializable{

	public  static final String URL = "URL";
	public  static final String CONTENT = "CONTENT";
	public  static final String IS_CALL_OUT = "IS_CALL_OUT";
	public  static final String PERSON = "PERSON";
	public  static final String VOICE = "VOICE";
	public  static final String FIELD_SERVERID = "SERVERID";
	public  static final String CREATED_AT = "CREATED_AT";
	
	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(columnName = FIELD_SERVERID)
	private Long serverId;// ID of server
	@DatabaseField(index = true, columnName = CONTENT)
	private String content;
	@DatabaseField(columnName = VOICE, columnDefinition = "LONGBLOB", dataType = DataType.BYTE_ARRAY)
	private byte[] voice;//voice recorded, combined several records 
	@DatabaseField(foreignAutoRefresh = true, foreign = true, columnName = PERSON)
	private PersonModel person;
	@DatabaseField(index = true, columnName = IS_CALL_OUT)
	private boolean isCallOut;//true - call out ; false - call in
	@DatabaseField(columnName = CREATED_AT)
	private Date createdAt;
	
	public MemoModel(){
		
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public byte[] getVoice() {
		return voice;
	}

	public void setVoice(byte[] voice) {
		this.voice = voice;
	}


	public PersonModel getPerson() {
		return person;
	}

	public void setPerson(PersonModel person) {
		this.person = person;
	}

	public boolean isCallOut() {
		return isCallOut;
	}

	public void setCallOut(boolean isCallOut) {
		this.isCallOut = isCallOut;
	}

	
}
