package com.changheng.accountant.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.changheng.accountant.db.MyDBHelper;
import com.changheng.accountant.db.PlanDBHelper;
import com.changheng.accountant.db.PlanDBManager;
import com.changheng.accountant.entity.Area;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.entity.Plan;
import com.changheng.accountant.entity.PlanAdapterData;
import com.changheng.accountant.util.StringUtils;

public class PlanDao {
	private PlanDBHelper dbhelper;
	public PlanDao(Context context) {
		// TODO Auto-generated constructor stub
		this.dbhelper = new PlanDBHelper(context);
	}
	private PlanDBManager dbManager;
	public PlanDao(Context context,String s)
	{
		this.dbManager = new PlanDBManager(context);
	}
	public void insertClass(ArrayList<Course> list)
	{
		if(list == null) return;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			// 如果一开始数据库数据为空,直接加
			String sql = "select classid from ClassTab";
			Cursor cursor = db.rawQuery(sql, new String[] {});
			if (cursor.getCount() == 0) {
				cursor.close();
				//循环加
				for (Course c1 : list) {
					String sql1 = "insert into ClassTab(classid,classname,classpid) values (?,?,?)";
					Object[] values = new Object[] { c1.getCourseId(),
							c1.getCourseName(), "0"};
					db.execSQL(sql1, values);
				}
			} else
			{
				cursor.close();
			}
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			dbManager.closeDatabase();
		}
	}
	public void insertArea(ArrayList<Area> list)
	{
		if(list == null) return;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			// 如果一开始数据库数据为空,直接加
			String sql = "select did from AreaTab";
			Cursor cursor = db.rawQuery(sql, new String[] {});
			if (cursor.getCount() == 0) {
				cursor.close();
				//循环加
				for (Area c1 : list) {
					String sql1 = "insert into AreaTab(did,dcname,dename) values (?,?,?)";
					Object[] values = new Object[] { c1.getId(),
							c1.getAreaCName(),c1.getAreaEName()};
					db.execSQL(sql1, values);
				}
			} else
			{
				cursor.close();
			}
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			dbManager.closeDatabase();
		}
	}
	public void insertKnowledgeList(ArrayList<Knowledge> list)
	{
		if(list == null) return;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			for(Knowledge k: list){
				db.execSQL("insert into KnowledgeTab(knowledgeid,knowledgetitle,knowledgecontent,chapterid,classid,orderid) values (?,?,?,?,?,?)",
						new Object[] { k.getKnowledgeId(), k.getTitle(),
						k.getFullContent(), k.getChapterId(),k.getClassId(),
						k.getOrderId() });
			}
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			dbManager.closeDatabase();
		}
	}
	
	public void insertPlans(ArrayList<Plan> list)
	{
		if(list == null) return;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			for(Plan p: list){
				db.execSQL("insert into StudyPlanTab(_id,days_id,class_id,summary_content,contains_kid,contains_paperid,area_id) values (?,?,?,?,?,?,?)",
						new Object[] {p.getId(),p.getDaysId(),p.getClassId(),p.getSummaryContent(),p.getContainsKid(),p.getContainsPaperId(),p.getAreaId() });
			}
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public ArrayList<PlanAdapterData> findAllPlans(String classid)
	{
		ArrayList<PlanAdapterData> list = new ArrayList<PlanAdapterData>();
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select summary_content,contains_kid,contains_paperid from StudyPlanTab where class_id = ? order by days_id asc";
		Cursor cursor = db.rawQuery(sql, new String[]{classid});
		while(cursor.moveToNext())
		{
			PlanAdapterData p = new PlanAdapterData(cursor.getString(0),cursor.getString(1),cursor.getString(2));
			List<Knowledge> kList = new ArrayList<Knowledge>();
			Cursor cursor2 = db.rawQuery("select knowledgetitle,knowledgecontent from KnowledgeTab where knowledgeid in ("+cursor.getString(1)+")"
					, new String[]{});
			while(cursor2.moveToNext())
			{
				Knowledge k = new Knowledge();
				k.setTitle(cursor2.getString(0));
				k.setFullContent(cursor2.getString(1).substring(0,100));
				kList.add(k);
			}
			cursor2.close();
			p.setKList(kList);
			list.add(p);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}
	
	public ArrayList<Knowledge> findKnowledge(String kids)
	{
		ArrayList<Knowledge> list = new ArrayList<Knowledge>();
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select knowledgeid,chapterid,knowledgetitle,knowledgeContent,orderid from KnowledgeTab where knowledgeid in("+kids+")";
		Cursor cursor = db.rawQuery(sql, new String[]{});
		while(cursor.moveToNext())
		{
			Knowledge k = new Knowledge(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), cursor.getString(3),
					cursor.getInt(4));
			k.setQuestionCount(-1);
			list.add(k);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}
	public ArrayList<Knowledge> findKnowledgeByChapter(String chapterId)
	{
		ArrayList<Knowledge> list = new ArrayList<Knowledge>();
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select knowledgeid,chapterid,knowledgetitle,orderid from KnowledgeTab where chapterid = ? order by knowledgeid asc";
		Cursor cursor = db.rawQuery(sql, new String[]{chapterId});
		while(cursor.moveToNext())
		{
			Knowledge k = new Knowledge(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), null,
					cursor.getInt(3));
			list.add(k);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}
	public String findKnowledgeContent(String knowledgeId) {
		// TODO Auto-generated method stub
		String content = null;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select knowledgetitle,knowledgecontent from KnowledgeTab where knowledgeid = ?",
						new String[] { knowledgeId });
		if (cursor.moveToNext()) {
			content = "<P style='color:blue;font-size:25px'>" + cursor.getString(0) + "</P>"
					+ cursor.getString(1);
		}
		cursor.close();
		dbManager.closeDatabase();
		return content;
	}
	public ArrayList<Course> findCourses(String ids)
	{
		ArrayList<Course> list = new ArrayList<Course>();
		String sql = null;
		if(StringUtils.isEmpty(ids))
		{
			sql = "select classid,classname from ClassTab";
		}else
		{
			if(ids.lastIndexOf(",")==ids.length()) ids = ids.substring(0, ids.length()-1);
			sql = "select classid,classname from ClassTab where classid in("+ids+")";
		}
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db.rawQuery(sql,new String[] {  });
		Course c = null;
		while(cursor.moveToNext())
		{
			c = new Course(cursor.getString(0),cursor.getString(1));
			list.add(c);
			c = null;
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}
}
