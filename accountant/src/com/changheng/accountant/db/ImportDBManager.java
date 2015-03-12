package com.changheng.accountant.db;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class ImportDBManager {
	private SQLiteDatabase database; 
    private Context context; 
    private String dirPath = Environment.getExternalStorageDirectory().getPath()+File.separator+"kuaiji"+File.separator+
    		"database"+File.separator;
    private String dbName;
  
    public ImportDBManager(Context context,int areaCode) { 
        this.context = context; 
        if(!new File(dirPath).exists())
        {
        	new File(dirPath).mkdirs();
        } 
        dbName = "accountant"+areaCode+".db";
    } 
  
    public synchronized SQLiteDatabase openDatabase() { 
        this.database = this.openDatabase(dirPath+dbName); 
        return this.database;
    } 
    private SQLiteDatabase openDatabase(String dbPath) { 
    	if(!new File(dbPath).exists())
    	{
    		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, 
                    null); 
    		//插表
    		db.execSQL("CREATE TABLE UserTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,UID TEXT,USERNAME TEXT,PASSWORD TEXT,NICKNAME TEXT,FACE TEXT,JOINTIME TEXT,LOCATION TEXT,TRUENAME TEXT)");
    		// 科目表
    		db.execSQL("CREATE TABLE ClassTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CLASSID TEXT,CLASSNAME TEXT,CLASSPID TEXT)");
    		// 章节表 (Pid为0表示章)
    		db.execSQL("CREATE TABLE ChapterTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CHAPTERID TEXT,CHAPTERTITLE TEXT,CHAPTERCONTENT TEXT,CLASSID TEXT,CHAPTERPID TEXT,ORDERID INTEGER)");
    		// 知识点表
    		db.execSQL("CREATE TABLE KnowledgeTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,KNOWLEDGEID TEXT,KNOWLEDGETITLE TEXT,KNOWLEDGECONTENT TEXT,CHAPTERID TEXT,CLASSID TEXT,ORDERID INTEGER)");
    		// 试卷表
    		db.execSQL("CREATE TABLE ExamPaperTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,PAPERID TEXT,PAPERNAME TEXT,ADDTIME TEXT,PAPERTIME INTEGER,PAPERSCORE INTEGER ,YEAR INTEGER,CLICKNUM INTEGER,PRICE INTEGER,CLASSID TEXT,TOTALNUM INTEGER)");
    		// 大题
    		db.execSQL("CREATE TABLE ExamRuleTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,RULEID TEXT,PAPERID TEXT,RULETITLE TEXT,RULETITLEINFO TEXT,RULETYPE TEXT,QUESTIONNUM INTEGER,SCOREFOREACH FLOAT,SCORESET TEXT,ORDERINPAPER INTEGER,CONTAINQIDS TEXT)");
    		// 试题
    		db.execSQL("CREATE TABLE ExamQuestionTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,RULEID TEXT,KNOWLEDGEID TEXT,CLASSID TEXT,CONTENT TEXT,ANSWER TEXT,ANALYSIS TEXT,QTYPE TEXT,OPTIONNUM INTEGER,ORDERID INTEGER,LINKQID TEXT,RANDNUM INTEGER,MATERIAL TEXT)");
    		// 学习记录
    		db.execSQL("CREATE TABLE ExamRecordTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,PAPERID TEXT,USERNAME TEXT,SCORE FLOAT,LASTTIME DATETIME DEFAULT (datetime('now','localtime')),USETIME INTEGER,TEMPTIME INTEGER,ANSWERS TEXT,TEMPANSWER TEXT,ISDONE TEXT,MODE INTEGER,ERRORNUM INTEGER,ISSYNC INTEGER DEFAULT 0)");
    		// 错题表
    		db.execSQL("CREATE TABLE ExamErrorQuestionTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,CLASSID TEXT,ERRORNUM INTEGER,USERNAME TEXT,LASTANSWER TEXT,STATUS INTEGER,ADDTIME DATETIME DEFAULT (datetime('now','localtime')))");
    		// 笔记表
    		db.execSQL("CREATE TABLE ExamNoteTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,CLASSID TEXT,CONTENT TEXT,STATUS INTEGER DEFAULT 0,ADDTIME DATETIME DEFAULT (datetime('now','localtime')),USERNAME TEXT)");
    		// 收藏表
    		db.execSQL("CREATE TABLE ExamFavorTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,EXAMID TEXT,USERNAME TEXT,STATUS INTEGER,ADDTIME DATETIME DEFAULT (datetime('now','localtime')))");
    		// 地区表
    		db.execSQL("CREATE TABLE AreaTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,AREAID TEXT,AREANAME TEXT, AREACODE INTEGER)");
    		// 数据库添加时间表
    		db.execSQL("CREATE TABLE DataAddTimeTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,DID INTEGER,ADDTIME DATETIME DEFAULT (datetime('now','localtime')))");
            return db; 
    	}
    	SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, 
                null); 
        return db; 
    }
    public synchronized void closeDatabase() { 
        this.database.close(); 
    } 
}
