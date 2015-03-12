package com.changheng.accountant.importdao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.changheng.accountant.db.MyDBHelper;
import com.changheng.accountant.entity.User;

public class UserDao {
	private static final String TAG = "UserDao";
	private MyDBHelper dbhelper;
	public UserDao(Context context)
	{
		dbhelper = new MyDBHelper(context);
	}
	public long addUser(User user) throws IllegalArgumentException, IllegalAccessException
	{
		long i = 0;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.WRITE);
		Log.d(TAG, "addUser方法打开了数据库连接");
		db.beginTransaction();
		try{
			String sql1 = "insert into UserTab(uid,username,password,nickname)values(?,?,?,?)";
			String pwd = new String(Base64.encode(Base64.encode(user.getPassword().getBytes(), 0), 0));
			Object[] values = new Object[] { user.getUid(),
					user.getUsername(), pwd,user.getNickname() };
			db.execSQL(sql1, values);
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
		}
		dbhelper.closeDb();
		Log.d(TAG, "addUser方法关闭了数据库连接");
		return i;
	}
	public User findByUsername(String username)
	{
		User user = null;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "findByUsername方法打开了数据库连接");
		Cursor cursor= db.rawQuery("select uid,username,password from UserTab where username = ?", new String[]{username});
		if(cursor.moveToNext())
		{
			user = new User();
			user.setUid(cursor.getInt(0));
			user.setUsername(cursor.getString(1));
			user.setPassword(cursor.getString(2));
		}
		cursor.close();
		dbhelper.closeDb();
		Log.d(TAG, "findByUsername方法关闭了数据库连接");
		return user;
	}
	public void update(User user) throws IllegalArgumentException, IllegalAccessException
	{
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.WRITE);
		Log.d(TAG, "update方法打开了数据库连接");
		String sql = "update UserTab set uid = ?,password = ? where username = ?";
		db.execSQL(sql, new Object[] {user.getUid(),user.getPassword(),user.getUsername()});
		dbhelper.closeDb();
		Log.d(TAG, "update方法关闭了数据库连接");
	}
	public void saveOrUpdate(User user) throws IllegalArgumentException, IllegalAccessException
	{
		User user1 = null;
		SQLiteDatabase db = dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor= db.rawQuery("select uid,username,password from UserTab where username = ?", new String[]{user.getUsername()});
		if(cursor.moveToNext())
		{
			user1 = new User();
			user1.setUid(cursor.getInt(0));
			user1.setUsername(cursor.getString(1));
			user1.setPassword(cursor.getString(2));
		}
		cursor.close();
		String pwd = new String(Base64.encode(Base64.encode(user.getPassword().getBytes(), 0), 0));
		if(user1 == null)
		{
			//插入
			db.beginTransaction();
			try{
				String sql1 = "insert into UserTab(uid,username,password,nickname)values(?,?,?,?)";
				System.out.println(user.getUid());
				Object[] values = new Object[] { user.getUid(),
						user.getUsername(), pwd,user.getNickname() };
				db.execSQL(sql1, values);
				db.setTransactionSuccessful();
			}finally
			{
				db.endTransaction();
			}
		}else
		{
			//更新
			if(!user1.getPassword().equals(pwd))
			{
				String sql = "update UserTab set uid = ?,password = ? where username = ?";
				db.execSQL(sql, new Object[] {user.getUid(),pwd,user.getUsername()});
			}
		}
		dbhelper.closeDb();
	}
	public void closeDB()
	{
		dbhelper.closeDb();
	}
}