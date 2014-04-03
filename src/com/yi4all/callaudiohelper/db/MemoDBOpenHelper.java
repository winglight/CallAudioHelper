package com.yi4all.callaudiohelper.db;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MemoDBOpenHelper extends OrmLiteSqliteOpenHelper {
	
	public  static final int DATABASE_VERSION = 1;
    
	public static final String DATABASE_NAME = "helper.db";
	
	// we do this so there is only one helper
		private static MemoDBOpenHelper helper = null;
		
	private Dao<MemoModel, Integer> memoDao;
	private Dao<PersonModel, Integer> personDao;

    public MemoDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MemoDBOpenHelper getHelper(Context context) {
		if (helper == null) {
			helper = new MemoDBOpenHelper(context);
			helper.getWritableDatabase();
		}
		return helper;
	}
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    	try {
			Log.i(MemoDBOpenHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, MemoModel.class);
			TableUtils.createTable(connectionSource, PersonModel.class);

		} catch (SQLException e) {
			Log.e(MemoDBOpenHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
        
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(MemoDBOpenHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, MemoModel.class, true);
			TableUtils.dropTable(connectionSource, PersonModel.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(MemoDBOpenHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
		
	}
	
    public Dao<MemoModel, Integer> getMemoDAO() throws SQLException{
    	if(memoDao == null){
    		memoDao = getDao(MemoModel.class);
    	}
    	return memoDao;
    }
    
    public Dao<PersonModel, Integer> getPersonDAO() throws SQLException{
    	if(personDao == null){
    		personDao = getDao(PersonModel.class);
    	}
    	return personDao;
    }
    
    @Override
	public void close() {
		super.close();
		memoDao = null;
		personDao = null;
	}

}
