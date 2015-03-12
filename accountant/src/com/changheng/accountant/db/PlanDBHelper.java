package com.changheng.accountant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PlanDBHelper extends SQLiteOpenHelper {
		public static final String DATABASENAME = "study_plan.db";
		public static final int VERSION = 1;
		public static int openedNum = 0;

		public PlanDBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, DATABASENAME, null, VERSION);
			// TODO Auto-generated constructor stub
		}

		public PlanDBHelper(Context context) {
			super(context, DATABASENAME, null, VERSION);
		}
		
		// 建库
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE AreaTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,DID INTEGER,DCNAME TEXT,DENAME TEXT)");
			db.execSQL("CREATE TABLE ClassTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CLASSID TEXT,CLASSNAME TEXT,CLASSPID TEXT)");
			db.execSQL("CREATE TABLE StudyPlanTab(_ID INTEGER,DAYS_ID INTEGER,CLASS_ID TEXT,SUMMARY_CONTENT TEXT,CONTAINS_KID TEXT,CONTAINS_PAPERID TEXT,AREA_ID TEXT)");
			db.execSQL("CREATE TABLE KnowledgeTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,KNOWLEDGEID TEXT,KNOWLEDGETITLE TEXT,KNOWLEDGECONTENT TEXT,CHAPTERID TEXT,CLASSID TEXT,ORDERID INTEGER)");
		}

		// 升级
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE  IF EXISTS AreaTab");
			db.execSQL("DROP TABLE  IF EXISTS ClassTab");
			db.execSQL("DROP TABLE  IF EXISTS StudyPlanTab");
			db.execSQL("DROP TABLE  IF EXISTS KnowledgeTab");
			onCreate(db);
		}

		private SQLiteDatabase db;
		public synchronized void closeDb() {
			// һ��ʼ��û�п�
			System.out.println("closeDb, openedNum = "+openedNum);
			if (openedNum == 0) {
				if (db != null) {
					db.close();
					db=null;
				}
				return;
			}
			openedNum--;
			if (openedNum == 0) {
				if (db != null) {
					db.close();
					db = null;
				}
			}
		}

		public synchronized SQLiteDatabase getDatabase(int i) {
			System.out.println("getDatabase, openedNum = "+openedNum);
			if (db == null || !db.isOpen()) {
				switch (i) {
				case 0:
					db = getReadableDatabase();
					break;
				case 1:
					db = getWritableDatabase();
					break;
				}
			}
			openedNum++;
			return db;
		}

		public static final int READ = 0;
		public static final int WRITE = 1;
}
