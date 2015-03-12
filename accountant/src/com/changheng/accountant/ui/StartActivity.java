package com.changheng.accountant.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;

/**
	应用启动进去的第一个Activity
*/
public class StartActivity extends Activity {
	private boolean isFirstIn;
	private static final String SHAREDPREFERENCES_NAME = "first_pref";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("StartActivity", "on create");
		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.ui_start, null);
		setContentView(view);
		// 渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				init();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});
		//友盟用户反馈推送 448
		FeedbackAgent agent = new FeedbackAgent(this);
		agent.sync();
		//启动记时
		((AppContext)getApplication()).alarm();
	}

	private void init() {
		// 读取SharedPreferences中需要的数据
		// 使用SharedPreferences来记录程序的使用次数
		SharedPreferences preferences = getSharedPreferences(
				SHAREDPREFERENCES_NAME, MODE_PRIVATE);
		// 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
		String isFirst = "isFirstIn" + ((AppContext)getApplication()).getVersionCode();
		isFirstIn = preferences.getBoolean(isFirst, true);
		// 判断程序与第几次运行，如果是第一次运行则跳转到引导界面，否则跳转到主界面
		if (!isFirstIn) { // 不是第一次启动
			// 检测版本更新,判断是否选中自动登录
			goMain();
		} else {
			// 转到引导界面
			goGuide();
		}

	}

	private void goMain() {
		Intent intent = new Intent(StartActivity.this, MainActivity.class);
		StartActivity.this.startActivity(intent);
		StartActivity.this.finish();
	}

	private void goGuide() {
		Intent intent = new Intent(StartActivity.this, GuideActivity.class);
		StartActivity.this.startActivity(intent);
		StartActivity.this.finish();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onPanelClosed(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		super.onPanelClosed(featureId, menu);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("StartActivity", "on destroy");
		super.onDestroy();

	}
}