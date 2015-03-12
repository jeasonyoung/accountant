package com.changheng.accountant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {
	public static final String DATABASENAME = "user.db";
	public static final int VERSION = 1;
	public static int openedNum = 0;

	public UserDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DATABASENAME, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	public UserDBHelper(Context context) {
		super(context, DATABASENAME, null, VERSION);
	}

	// 建库
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
//		 this.db = db;
		db.execSQL("CREATE TABLE UserTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,UID TEXT,USERNAME TEXT,PASSWORD TEXT,NICKNAME TEXT,FACE TEXT,JOINTIME TEXT,LOCATION TEXT,TRUENAME TEXT)");
		// 科目表
//		db.execSQL("CREATE TABLE ClassTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CLASSID TEXT,CLASSNAME TEXT,CLASSPID TEXT)");
		// 章节表 (Pid为0表示章)
//		db.execSQL("CREATE TABLE ChapterTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CHAPTERID TEXT,CHAPTERTITLE TEXT,CHAPTERCONTENT TEXT,CLASSID TEXT,CHAPTERPID TEXT,ORDERID INTEGER)");
		// 知识点表
//		db.execSQL("CREATE TABLE KnowledgeTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,KNOWLEDGEID TEXT,KNOWLEDGETITLE TEXT,KNOWLEDGECONTENT TEXT,CHAPTERID TEXT,CLASSID TEXT,ORDERID INTEGER)");
		// 试卷表
//		db.execSQL("CREATE TABLE ExamPaperTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,PAPERID TEXT,PAPERNAME TEXT,ADDTIME TEXT,PAPERTIME INTEGER,PAPERSCORE INTEGER ,YEAR INTEGER,CLICKNUM INTEGER,PRICE INTEGER,CLASSID TEXT,TOTALNUM INTEGER)");
		// 大题
//		db.execSQL("CREATE TABLE ExamRuleTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,RULEID TEXT,PAPERID TEXT,RULETITLE TEXT,RULETITLEINFO TEXT,RULETYPE TEXT,QUESTIONNUM INTEGER,SCOREFOREACH FLOAT,SCORESET TEXT,ORDERINPAPER INTEGER,CONTAINQIDS TEXT)");
		// 试题
//		db.execSQL("CREATE TABLE ExamQuestionTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,RULEID TEXT,KNOWLEDGEID TEXT,CLASSID TEXT,CONTENT TEXT,ANSWER TEXT,ANALYSIS TEXT,QTYPE TEXT,OPTIONNUM INTEGER,ORDERID INTEGER,LINKQID TEXT,RANDNUM INTEGER)");
		// 学习记录
//		db.execSQL("CREATE TABLE ExamRecordTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,PAPERID TEXT,USERNAME TEXT,SCORE FLOAT,LASTTIME DATETIME DEFAULT (datetime('now','localtime')),USETIME INTEGER,TEMPTIME INTEGER,ANSWERS TEXT,TEMPANSWER TEXT,ISDONE TEXT,MODE INTEGER,ERRORNUM INTEGER,ISSYNC INTEGER DEFAULT 0)");
		// 错题表
//		db.execSQL("CREATE TABLE ExamErrorQuestionTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,CLASSID TEXT,ERRORNUM INTEGER,USERNAME TEXT,LASTANSWER TEXT,STATUS INTEGER,ADDTIME DATETIME DEFAULT (datetime('now','localtime')))");
//		 笔记表
//		db.execSQL("CREATE TABLE ExamNoteTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,CLASSID TEXT,CONTENT TEXT,STATUS INTEGER DEFAULT 0,ADDTIME DATETIME DEFAULT (datetime('now','localtime')),USERNAME TEXT)");
		// 收藏表
//		db.execSQL("CREATE TABLE ExamFavorTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,EXAMID TEXT,USERNAME TEXT,STATUS INTEGER,ADDTIME DATETIME DEFAULT (datetime('now','localtime')))");
		// 地区表
//		db.execSQL("CREATE TABLE AreaTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,AREAID TEXT,AREANAME TEXT, AREACODE INTEGER)");
	}

	// 升级
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE  IF EXISTS UserTab");
//		db.execSQL("DROP TABLE  IF EXISTS ClassTab");
//		db.execSQL("DROP TABLE  IF EXISTS ChapterTab");
//		db.execSQL("DROP TABLE  IF EXISTS KnowledgeTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamPaperTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamRuleTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamQuestionTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamErrorQuestionTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamRecordTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamNoteTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamFavorTab");
//		db.execSQL("DROP TABLE  IF EXISTS AreaTab");
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
