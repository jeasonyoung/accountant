package com.changheng.accountant.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.changheng.accountant.AppConfig;

public class TimeBroadcast extends BroadcastReceiver {
	private AppConfig appConfig;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if ("repeating".equals(intent.getAction())) {
			Log.e("TimeBroadcast","接收到广播。。。");
			if (appConfig == null) {
				appConfig = AppConfig.getAppConfig(context);
			}
			Integer hours = appConfig.getRestHours();
			if(hours!=null&&hours > 0)
			{
				appConfig.setRestHours(hours-1);
			}else
			{
				this.cancelAlarm(context);
			}
		}
	}
	//取消定时
	private void cancelAlarm(Context context)
	{
		Intent intent =new Intent(context, TimeBroadcast.class);
		  intent.setAction("repeating");
		  PendingIntent sender=PendingIntent
		         .getBroadcast(context, 0, intent, 0);
		  AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		  alarm.cancel(sender);
	}
}
