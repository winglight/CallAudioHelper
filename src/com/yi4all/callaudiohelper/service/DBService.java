package com.yi4all.callaudiohelper.service;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.yi4all.callaudiohelper.db.MemoDBOpenHelper;
import com.yi4all.callaudiohelper.db.MemoModel;
import com.yi4all.callaudiohelper.db.PersonModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ChenYu on 13-8-8.
 */
public class DBService {

    private static final String LOG_TAG = "DBService";

    private MemoDBOpenHelper helper;

    private DBService(Context context) {
        this.helper = MemoDBOpenHelper.getHelper(context);
    }

    public static DBService getInstance(Context context) {
        return new DBService(context);
    }

    public void close() {
        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }
    }

    public List<PersonModel> getPersons(){
        List<PersonModel> res = new ArrayList<PersonModel>();

        try {
            Dao<PersonModel, Integer> dao = helper.getPersonDAO();

            res = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  res;
    }
    
    public MemoModel findLastMemoByPerson(PersonModel pm){
    	MemoModel res = null;

        try {
            Dao<MemoModel, Integer> dao = helper.getMemoDAO();

            QueryBuilder<MemoModel, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy(MemoModel.CREATED_AT, false);
            
			Where<MemoModel, Integer> where = queryBuilder.where();
			where.eq(MemoModel.PERSON, pm);
			

			return dao.queryForFirst(queryBuilder.prepare());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  res;
    }
    
    public List<MemoModel> findMemosByPerson(PersonModel pm){
    	List<MemoModel> res = new ArrayList<MemoModel>();

        try {
            Dao<MemoModel, Integer> dao = helper.getMemoDAO();

            res = dao.queryForEq(MemoModel.PERSON, pm);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  res;
    }
    
    public PersonModel findPersonByPhoneNumber(String phoneNumber){
        try {
            Dao<PersonModel, Integer> dao = helper.getPersonDAO();

            QueryBuilder<PersonModel, Integer> queryBuilder = dao.queryBuilder();
			Where<PersonModel, Integer> where = queryBuilder.where();
			where.like(PersonModel.PHONE_NUMBERS, "%|" + phoneNumber + "|%");

			return dao.queryForFirst(queryBuilder.prepare());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  null;
    }
    
    public PersonModel findPersonByContactId(String contactId){
        try {
            Dao<PersonModel, Integer> dao = helper.getPersonDAO();

			List<PersonModel> list = dao.queryForEq(PersonModel.CONTACT_ID, contactId);
			
			if(list != null && list.size() > 0){
				return list.get(0);
			}
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  null;
    }
    

    public boolean createPerson(PersonModel sm){
        try {
            Dao<PersonModel, Integer> dao = helper.getPersonDAO();

            sm.setCreatedAt(new Date());
            int res = dao.create(sm);

            return  res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }

    public boolean updatePerson(PersonModel sm){
        try {
            Dao<PersonModel, Integer> dao = helper.getPersonDAO();

            int res = dao.update(sm);

            return  res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }

    public boolean deletePerson(PersonModel sm){
        try {
            Dao<PersonModel, Integer> dao = helper.getPersonDAO();

            int res = dao.delete(sm);

            return  res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }
    
    public boolean createMemo(MemoModel memo){
        try {
            Dao<MemoModel, Integer> dao = helper.getMemoDAO();

            memo.setCreatedAt(new Date());
            int res = dao.create(memo);

            return  res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }

    public boolean updateMemo(MemoModel sm){
        try {
            Dao<MemoModel, Integer> dao = helper.getMemoDAO();

            int res = dao.update(sm);

            return  res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }

    public boolean deleteMemo(MemoModel sm){
        try {
            Dao<MemoModel, Integer> dao = helper.getMemoDAO();

            int res = dao.delete(sm);

            return  res == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  false;
    }
}
