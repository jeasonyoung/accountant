package com.changheng.accountant.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class PlanDBManager {
	private final int BUFFER_SIZE = 10000; 
    public static final String DB_NAME = "study_plan.db"; //保存的数据库文件名 
    public static final String PACKAGE_NAME = "com.changheng.accountant"; 
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME + "/databases";  //在手机里存放数据库的位置 
  
    private SQLiteDatabase database; 
    private Context context; 
  
    public PlanDBManager(Context context) { 
        this.context = context; 
    } 
  
    public synchronized SQLiteDatabase openDatabase() { 
        this.database = this.openDatabase(DB_PATH + "/" + DB_NAME); 
        return this.database;
    } 
  
    private SQLiteDatabase openDatabase(String dbfile) { 
        try { 
            if (!(new File(dbfile).exists())) {//判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
            	Log.i("LocationDB", "执行数据库导入");
                InputStream is = this.context.getResources().getAssets()
						.open("databases/study_plan.zip");
//                FileOutputStream fos = new FileOutputStream(dbfile); 
//                byte[] buffer = new byte[BUFFER_SIZE]; 
//                int count = 0; 
//                while ((count = is.read(buffer)) > 0) { 
//                    fos.write(buffer, 0, count); 
//                } 
//                fos.close(); 
//                is.close();
            	Unzip(is,DB_PATH);
            } 
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, 
                    null); 
            return db; 
        } catch (FileNotFoundException e) { 
            Log.e("Database", "File not found"); 
            e.printStackTrace(); 
        } catch (IOException e) { 
            Log.e("Database", "IO exception"); 
            e.printStackTrace(); 
        } 
        return null; 
    } 
    //do something else here<BR> 
     public synchronized void closeDatabase() { 
        this.database.close(); 
    } 
     private static void Unzip(InputStream is, String targetDir) {
 		int BUFFER = 4096; // 这里缓冲区我们使用4KB，
 		String strEntry; // 保存每个zip的条目名称

 		try {
 			BufferedOutputStream dest = null; // 缓冲输出流
// 			FileInputStream fis = new FileInputStream(is);
 			ZipInputStream zis = new ZipInputStream(
 					new BufferedInputStream(is));
 			ZipEntry entry; // 每个zip条目的实例

 			while ((entry = zis.getNextEntry()) != null) {

 				try {
 					Log.i("Unzip: ", "=" + entry);
 					int count;
 					byte data[] = new byte[BUFFER];
 					strEntry = entry.getName();

 					File entryFile = new File(targetDir  + "/" + strEntry);
 					File entryDir = new File(entryFile.getParent());
 					if (!entryDir.exists()) {
 						entryDir.mkdirs();
 					}

 					FileOutputStream fos = new FileOutputStream(entryFile);
 					dest = new BufferedOutputStream(fos, BUFFER);
 					while ((count = zis.read(data, 0, BUFFER)) != -1) {
 						dest.write(data, 0, count);
 					}
 					dest.flush();
 					dest.close();
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 			zis.close();
 		} catch (Exception cwj) {
 			cwj.printStackTrace();
 		}
 	}
}
