package com.changheng.accountant.importdao;

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

public class PlanDao {
	private PlanDBHelper dbhelper;
	public PlanDao(Context context) {
		// TODO Auto-generated constructor stub
		this.dbhelper = new PlanDBHelper(context);
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
			dbhelper.closeDb();
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
			dbhelper.closeDb();
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
			dbhelper.closeDb();
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
		dbhelper.closeDb();
	}
}
