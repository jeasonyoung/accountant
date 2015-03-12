package com.changheng.accountant.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.changheng.accountant.db.MyDBManager;
import com.changheng.accountant.db.PlanDBManager;
import com.changheng.accountant.entity.Chapter;
import com.changheng.accountant.entity.Course;

public class CourseDao {
	private MyDBManager dbManager;
	private PlanDBManager dbManager1;
	private static final String TAG = "CourseDao";

	public CourseDao(Context context) {
		dbManager = new MyDBManager(context);
	}
	public CourseDao(Context context,String s)
	{
		dbManager1 = new PlanDBManager(context);
	}
	//找父ID下所有的章节 pid=0表示顶级的考试科目
	public ArrayList<Course> findAllClass() {
		SQLiteDatabase db = dbManager.openDatabase();
		Log.d(TAG, "findAllClass方法打开了数据库连接");
		ArrayList<Course> list = new ArrayList<Course>();
		String sql = "select classid,classname from ClassTab";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		while (cursor.moveToNext()) {
			Course uc = new Course(cursor.getString(0), cursor.getString(1));
			list.add(uc);
		}
		cursor.close();
		dbManager.closeDatabase();
		Log.d(TAG, "findAllClass方法关闭了数据库连接");
		return list;
	}
	public ArrayList<Course> findAllClassFromPlanDB() {
		SQLiteDatabase db = dbManager1.openDatabase();
		Log.d(TAG, "findAllClass方法打开了数据库连接");
		ArrayList<Course> list = new ArrayList<Course>();
		String sql = "select classid,classname from ClassTab";
		Cursor cursor = db.rawQuery(sql, new String[] {});
		while (cursor.moveToNext()) {
			Course uc = new Course(cursor.getString(0), cursor.getString(1));
			list.add(uc);
		}
		cursor.close();
		dbManager1.closeDatabase();
		Log.d(TAG, "findAllClass方法关闭了数据库连接");
		return list;
	}
	public void save(List<Course> courses,List<Chapter> chapters) {
		if(courses==null || chapters==null)
		{
			return;
		}
		SQLiteDatabase db = dbManager.openDatabase();
		Log.d(TAG, "save方法打开了数据库连接");
		db.beginTransaction();
		try {
			//插入科目
			if (courses.size() == 0) {
				return;
			} 
			else {
				// 如果一开始数据库数据为空,直接加
				String sql = "select classid from ClassTab";
				Cursor cursor = db.rawQuery(sql, new String[] {});
				if (cursor.getCount() == 0) {
					cursor.close();
					//循环加
					for (Course c1 : courses) {
						String sql1 = "insert into ClassTab(classid,classname,classpid) values (?,?,?)";
						Object[] values = new Object[] { c1.getCourseId(),
								c1.getCourseName(), "0"};
						db.execSQL(sql1, values);
					}
				} else
				{
					cursor.close();
				}
//				else {
//					cursor.close();
//					for (Course c : courses) {
//						String sqleach = "select classid from CourseTab where fileurl = ? and username=?";
//						Cursor cursoreach = db.rawQuery(sqleach,
//								new String[] { c.getFileUrl(),c.getUsername() });
//						if (cursoreach.getCount() > 0) {	//有记录则跳过不再增加
//							cursoreach.close();
//							continue;
//						}
//						cursoreach.close();
//						String sql1 = "insert into CourseTab(courseid,coursename,classid,coursetype,coursemode,coursegroup,filesize,finishsize,filepath,fileurl,state,username) values (?,?,?,?,?,?,?,?,?,?,?,?)";
//						Object[] values = new Object[] { c.getCourseId(),
//								c.getCourseName(), c.getClassid(),
//								c.getCourseType(), c.getCourseMode(),
//								c.getCourseGroup(), c.getFilesize(),
//								c.getFinishsize() , c.getFilePath(),
//								c.getFileUrl(), c.getState() ,c.getUsername()};
//						db.execSQL(sql1, values);
//					}
//				}
			}
			//插入章
			if(chapters.size()==0)
			{
				return;
			}else
			{
				String sql = "select chapterid from ChapterTab";
				Cursor cursor = db.rawQuery(sql, new String[] {});
				if (cursor.getCount() == 0) {
					cursor.close();
					//循环加
					for (Chapter c1 : chapters) {
						String sql1 = "insert into ChapterTab(chapterid,chaptertitle,chaptercontent,classid,chapterpid,orderid) values (?,?,?,?,?,?)";
						Object[] values = new Object[] { c1.getChapterId(),
								c1.getTitle(), c1.getContent(), c1.getClassId(),c1.getPid(),c1.getOrderId()};
						db.execSQL(sql1, values);
					}
				} else
				{
					cursor.close();
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
		Log.d(TAG, "save方法关闭了数据库连接");
	}
	
	/**
	 * 找章
	 * CHAPTERID TEXT,CHAPTERNAME TEXT,CLASSID TEXT,CHAPTERPID TEXT,ORDERID INTEGER
	 */
	public List<Chapter> findAllChapterByPid(String pid)
	{
		if(pid==null)
		{
			return null;
		}
		SQLiteDatabase db = dbManager.openDatabase();
		Log.d(TAG, "findAllChapterByPid方法打开了数据库连接");
		List<Chapter> list = new ArrayList<Chapter>();
		String sql = "select chapterid,chaptertitle,chaptercontent,classid,chapterpid,orderid from ChapterTab where chapterpid = ? order by orderid asc";
		String sql2 = "select paperscore from ExamPaperTab where paperid = ?";
		Cursor cursor = db.rawQuery(sql, new String[] {pid});
		while (cursor.moveToNext()) {
			Chapter uc = new Chapter(cursor.getString(0), cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getInt(5));
			Cursor cursor2 = db.rawQuery(sql2, new String[]{uc.getClassId()+"_"+uc.getChapterId()});
			cursor2.moveToNext(); 	//必须把游标往下移动一次才能读到数据
			uc.setQuestionCount(cursor2.getInt(0));
			cursor2.close();
			list.add(uc);
		}
		cursor.close();
		dbManager.closeDatabase();
		Log.d(TAG, "findAllChapterByPid方法关闭了数据库连接");
		return list;
	}
	/**
	 * 找科目下的直属章节
	 * @return
	 */
	public ArrayList<ArrayList<Chapter>> findAllChapterByClass(List<Course> courses)
	{
		if(courses ==null ||courses.size()==0)
		{
			return null;
		}
		SQLiteDatabase db = dbManager.openDatabase();
		Log.d(TAG, "findAllChapterByClass方法打开了数据库连接");
		ArrayList<ArrayList<Chapter>> list = new ArrayList<ArrayList<Chapter>>();
		for(Course c:courses)
		{
			ArrayList<Chapter> cList = new ArrayList<Chapter>();
			String sql = "select chapterid,chaptertitle,chaptercontent,classid,chapterpid,orderid from ChapterTab where chapterpid = 0 and classid = ? order by orderid asc";
			Cursor cursor = db.rawQuery(sql, new String[] {c.getCourseId()});
			while (cursor.moveToNext()) {
				Chapter uc = new Chapter(cursor.getString(0), cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getInt(5));
				cList.add(uc);
			}
			cursor.close();
			list.add(cList);
		}
		dbManager.closeDatabase();
		Log.d(TAG, "findAllChapterByClass方法关闭了数据库连接");
		return list;
	}
}