package com.changheng.accountant.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class MyDBManager {
	private final int BUFFER_SIZE = 10000; 
    public static final String DB_NAME = "accountant.db"; //保存的数据库文件名 
    public static final String ZIP_NAME = "data.zip";
    public static final String PACKAGE_NAME = "com.changheng.accountant"; 
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME + "/databases";  //在手机里存放数据库的位置 
    
    public static final String ZIP_PATH = Environment.getExternalStorageDirectory()+File.separator+"CHAccountant"+File.separator+"data";
    private SQLiteDatabase database; 
    private Context context; 
  
    public MyDBManager(Context context) { 
        this.context = context; 
    } 
  
    public synchronized SQLiteDatabase openDatabase() { 
        this.database = this.openDatabase(DB_PATH + "/" + DB_NAME,ZIP_PATH + "/" + ZIP_NAME); 
        return this.database;
    } 
    
    //判断数据库文件存不存在
    public boolean isDBExist()
    {
    	//如果数据库文件不存在
    	if(!(new File(DB_PATH+File.separator+DB_NAME).exists()))
    	{
    		//看SD卡里的数据文件在不在
    		System.out.println(ZIP_PATH + File.separator + ZIP_NAME);
    		boolean b= new File(ZIP_PATH + File.separator + ZIP_NAME).exists();
    		System.out.println("数据库文件存不存在？？？"+b);
    		return b ;
    	}
    	return true;
    }
    private SQLiteDatabase openDatabase(String dbfile,String zipFile) { 
        try { 
            if (!(new File(dbfile).exists())) {//判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
            	Log.i("LocationDB", "执行数据库导入");
//                InputStream is = this.context.getResources().getAssets()
//						.open("databases/study_plan.zip");
                InputStream is = new FileInputStream(zipFile);
//                FileOutputStream fos = new FileOutputStream(dbfile); 
//                byte[] buffer = new byte[BUFFER_SIZE]; 
//                int count = 0; 
//                while ((count = is.read(buffer)) > 0) { 
//                    fos.write(buffer, 0, count); 
//                } 
//                fos.close(); 
//                is.close();
            	Unzip(is,DB_PATH);
            	SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, 
                        null);
            	//查个询
            	String addtime = null;
        		Cursor cursor = db.rawQuery(
        				"select addtime from DataAddTimeTab order by _id desc",
        				new String[] {});
        		if (cursor.moveToNext()) {
        			addtime = cursor.getString(0);
        		}
        		cursor.close();
            	setData(addtime);
            	return db;
            } 
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, 
                    null); 
            return db; 
        } catch (FileNotFoundException e) { 
            Log.e("Database", "File not found"); 
            Toast.makeText(context, "请先下载数据包", Toast.LENGTH_SHORT).show();
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
     private void setData(String addtime) {
 		SharedPreferences preferences = context.getSharedPreferences(
 				"first_pref", Context.MODE_PRIVATE);
 		Editor editor = preferences.edit();
 		// 存入数据
 		editor.putString("DBAddTime", addtime);
 		// 提交修改
 		editor.commit();
 	}
}
