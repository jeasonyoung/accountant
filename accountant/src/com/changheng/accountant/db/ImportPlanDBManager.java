package com.changheng.accountant.db;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class ImportPlanDBManager {
	
	private SQLiteDatabase database; 
	private String dirPath = Environment.getExternalStorageDirectory().getPath()+File.separator+"kuaiji"+File.separator+
    		"database"+File.separator;
    private String dbName;
  
    public ImportPlanDBManager(int areaCode) { 
    	if(!new File(dirPath).exists())
        {
        	new File(dirPath).mkdirs();
        } 
        dbName = "study_plan"+areaCode+".db";
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
    		db.execSQL("CREATE TABLE AreaTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,DID INTEGER,DCNAME TEXT,DENAME TEXT)");
			db.execSQL("CREATE TABLE ClassTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CLASSID TEXT,CLASSNAME TEXT,CLASSPID TEXT)");
			db.execSQL("CREATE TABLE StudyPlanTab(_ID INTEGER,DAYS_ID INTEGER,CLASS_ID TEXT,SUMMARY_CONTENT TEXT,CONTAINS_KID TEXT,CONTAINS_PAPERID TEXT,AREA_ID TEXT)");
			db.execSQL("CREATE TABLE KnowledgeTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,KNOWLEDGEID TEXT,KNOWLEDGETITLE TEXT,KNOWLEDGECONTENT TEXT,CHAPTERID TEXT,CLASSID TEXT,ORDERID INTEGER)");
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
