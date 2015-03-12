package com.changheng.accountant.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ViewPagerAdapter;
import com.umeng.analytics.MobclickAgent;

public class GuideActivity extends Activity implements OnPageChangeListener {

	private ViewPager vp;
//	private ViewPagerAdapter vpAdapter;
	private List<View> views;

	// 控制小点
	private ImageView[] dots;

	// 当前页号
	private int currentIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("GuideActivity", "on Create");
		super.onCreate(savedInstanceState);
		requestWindowFeature(1); // 去标题
		this.setContentView(R.layout.ui_first_start);
		// 初始化组件
		initViews();
		// 初始化
		initDots();
	}

	private void initViews() {
		LayoutInflater inflater = LayoutInflater.from(this);
		views = new ArrayList<View>();
		// 初始化引导图片列表
		views.add(inflater.inflate(R.layout.guide_page1, null));
		views.add(inflater.inflate(R.layout.guide_page2, null));
		views.add(inflater.inflate(R.layout.guide_page3, null));
		// 初始化Adapter
//		vpAdapter = new ViewPagerAdapter(views, this);
		vp = (ViewPager) findViewById(R.id.awesomepager);
		vp.setAdapter(new ViewPagerAdapter(views, this));
		// 绑定回调
		vp.setOnPageChangeListener(this);
	}

	private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.pagelist);
		dots = new ImageView[views.size()];

		// 循环取得小点图片
		for (int i = 0; i < views.size(); i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);// 都设置为灰色
		}

		currentIndex = 0;
		dots[currentIndex].setEnabled(false);// 设置为白色,即选中状态
	}

	private void setCurrentDot(int position) {
		if (position < 0 || position > views.size() - 1
				|| currentIndex == position) {
			return;
		}

		dots[position].setEnabled(false);
		dots[currentIndex].setEnabled(true);

		currentIndex = position;
	}

	// 当滑动状态改变时调用
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	// 当当前页面被滑动时调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	// 当心的页面被选中时调用
	@Override
	public void onPageSelected(int arg0) {
		// 设置底部小点选中状态
		setCurrentDot(arg0);
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("GuideActivity", "on destroy");
		super.onDestroy();
	}
}
