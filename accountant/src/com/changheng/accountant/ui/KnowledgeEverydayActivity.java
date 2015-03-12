package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.PlanDao;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.PlanAdapterData;
import com.changheng.accountant.view.PinnedHeaderListView;
import com.changheng.accountant.view.SectionListAdapter;
import com.changheng.accountant.view.SectionListItem;

public class KnowledgeEverydayActivity extends BaseActivity implements OnClickListener{
	// 这个类用来提供模拟数据
	public class StandardArrayAdapter extends ArrayAdapter<SectionListItem> {

		public final SectionListItem[] items;

		public StandardArrayAdapter(final Context context,
				final int textViewResourceId, final SectionListItem[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		// @Override
		// public View getView(final int position, final View convertView,
		// final ViewGroup parent) {}
	}

	private StandardArrayAdapter arrayAdapter;

	private SectionListAdapter sectionAdapter;

	private PinnedHeaderListView listView;
	private SimpleDateFormat format;
	private ProgressDialog proDialog;
	private MyHandler mHandler;
	private int index,today;
	private SectionListItem[] data;
	private AppConfig appConfig;
	private long examTime;
	private PlanAdapterData todayPlan;
	private PopupWindow coursePop;
	private ImageView menuIn;
	private PlanDao dao;
	private ArrayList<Course> courses;
	private String hasDay;
	private Course currentCourse;
	private Calendar calendar;
	private LinearLayout nodataLayout;
	private SampleAdapter menuAdapter;
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_knowledge_main);
		appConfig = AppConfig.getAppConfig(this);
		format = new SimpleDateFormat("EEE\nMM月dd日", Locale.CHINA);
		this.findViewById(R.id.btn_left).setOnClickListener(this);
		this.findViewById(R.id.menuTitleLayout).setOnClickListener(this);
		listView = (PinnedHeaderListView) findViewById(R.id.knoewledge_list);
		menuIn = (ImageView) this.findViewById(R.id.iv_item_bg_in);
		menuIn.setTag(0);
		nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		proDialog = ProgressDialog.show(this, null,"加载中...",true,true);
		proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mHandler = new MyHandler(this);
		dao = new PlanDao(KnowledgeEverydayActivity.this,null); //必须用这个构造方法
		calendar = Calendar.getInstance();	//现在的时间
		today = calendar.get(Calendar.DAY_OF_YEAR);	//今天
		examTime = appConfig.getExamTime();
		if(examTime == 0)
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+2); 	//今天往后推2个月,假定为考试日期
		else
			calendar.setTimeInMillis(examTime); //设置考试日期
		hasDay = calculateRestDay(examTime);
		new Thread(){
			public void run() {
				try
				{
					courses = dao.findCourses(appConfig.get(AppConfig.CONF_SELECTED_COURSEID));
					currentCourse = courses.get(0);
					Message msg = mHandler.obtainMessage();
					msg.what = 1;
					msg.obj = getAdapterData(courses.get(0).getCourseId());
					mHandler.sendMessage(msg);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			};
		}.start();
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				PlanAdapterData p = (PlanAdapterData) data[arg2].item;
				Intent intent = new Intent(KnowledgeEverydayActivity.this,
						KnowledgeTodayActivity.class);
				intent.putExtra("containsKid", p.getContainsKid());
				intent.putExtra("containsPid", p.getContainsPid());
				KnowledgeEverydayActivity.this.startActivity(intent);
			}
		});
	}

	private SectionListItem[] getAdapterData(String id)
	{
		//某科目下的计划
		ArrayList<PlanAdapterData> list = dao.findAllPlans(id);
		int size = list.size();
		if(size == 0) return null;
		SectionListItem[] items = new SectionListItem[size];
		int more = size%7;
		for(int k = 0; k<more;k++)
		{
			PlanAdapterData aPlan = list.get(k);
			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-(size-k));
			String formatDataString = format.format(new Date(calendar.getTimeInMillis()));
			aPlan.setDate(formatDataString);
			if(today == calendar.get(Calendar.DAY_OF_YEAR)){aPlan.setToday(true);index=k ;}
			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+(size-k));
			items[k] = new SectionListItem(aPlan, "第 1 周"); //
		}
		int week = size/7;
		for(int i = 0;i<week;i++)
		{
			for(int j = 0;j<7;j++)
			{
				PlanAdapterData aPlan = list.get(i*7+j+more);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-(week*7-i*7-j));
				String formatDataString = format.format(new Date(calendar.getTimeInMillis()));
				aPlan.setDate(formatDataString);
				if(today == calendar.get(Calendar.DAY_OF_YEAR))
				{
					aPlan.setToday(true);index=i*7+j+more ;
					aPlan.setKsDay(hasDay);
					todayPlan = aPlan;
				}
				calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+(week*7-i*7-j));
				items[i*7+j+more] = new SectionListItem(aPlan, "第"+(i+(more==0?1:2))+"周"); //
			}
		} 
		return items;
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		long time = appConfig.getExamTime();
		if(examTime!=time)
		{
			todayPlan.setKsDay(calculateRestDay(time));
			//表示更新过时间
			if(arrayAdapter!=null)
			{
				arrayAdapter.notifyDataSetChanged();
			}
			
		}
		super.onStart();
	}
	//计算剩余天数
	private String calculateRestDay(long examTime)
	{
		if(examTime == 0)
		{
			return "考试日期未设置";
		}
		String s = "考试日期已过";
		try {
			Date thatDay = new Date(examTime);
			long now = System.currentTimeMillis();
			if(thatDay.before(new Date(now))) //日期在今天之前
			{
				s = "考试日期已过";
			}else
			{
				long i = (thatDay.getTime()-now)/1000/60/60/24;
				s = "距离考试仅"+i+" 天";
				if(i==0)
				{
					s = "距离考试不到 1 天";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_left:
			this.finish();
			break;
		case R.id.menuTitleLayout:
			if(courses == null)
			{
				return;
			}
			showPop(v);
			break;
		}
	}
	private void showPop(View v)
	{
		if(coursePop == null)
		{
			View view = LayoutInflater.from(this).inflate(R.layout.choosecourse_menu,
					null);
			ListView listView = (ListView) view.findViewById(R.id.choosecourse_listView);
			menuAdapter = new SampleAdapter();
			listView.setAdapter(menuAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						final int arg2, long arg3) {
					// TODO Auto-generated method stub
					coursePop.dismiss();
					final String id = courses.get(arg2).getCourseId();
					if(currentCourse.getCourseId().equals(id))
					{
						menuIn.setTag(0);
						return;
					}
					currentCourse = courses.get(arg2);
					menuIn.setTag(1);
					proDialog.show();
					new Thread(){
						public void run() {
							try
							{
								Message msg = mHandler.obtainMessage();
								msg.what = 1;
								msg.obj = getAdapterData(currentCourse.getCourseId());
								mHandler.sendMessage(msg);
							}catch(Exception e)
							{
								e.printStackTrace();
								mHandler.sendEmptyMessage(-1);
							}
						};
					}.start();
				}
			});
			coursePop = new PopupWindow(view, -2, -2);
			// 设置点外面消失
			coursePop.setBackgroundDrawable(new BitmapDrawable());
			coursePop.setFocusable(true);
			coursePop.setOutsideTouchable(true);
			coursePop.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					menuIn.setVisibility(View.VISIBLE);
				}
			});
		}
		if(menuIn.getTag().equals(1)){
			menuAdapter.notifyDataSetChanged(); //改变选中项
		}
		coursePop.showAsDropDown(v, -9, -2);
		menuIn.setVisibility(View.GONE);
	}
	public class SampleAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return courses.size();
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return courses.get(position);
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(KnowledgeEverydayActivity.this).inflate(
						R.layout.knowledge_everyday_top_menu_item, null);
			}
			Course c = courses.get(position);
			TextView title = (TextView) convertView
					.findViewById(R.id.item_txt);
			title.setText(c.getCourseName());
			if(c.equals(currentCourse))
			{
				title.setTextColor(getResources().getColor(R.color.blue));
			}else
			{
				title.setTextColor(getResources().getColor(R.color.black));
			}
			return convertView;
		}
	}
	static class MyHandler extends Handler {
		WeakReference<KnowledgeEverydayActivity> weak;

		public MyHandler(KnowledgeEverydayActivity r) {
			// TODO Auto-generated constructor stub
			weak = new WeakReference<KnowledgeEverydayActivity>(r);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			KnowledgeEverydayActivity k = weak.get();
			if(k.proDialog!=null)
			{
				k.proDialog.dismiss();
			}
			switch(msg.what)
			{
			case 1:
				k.data = (SectionListItem[]) msg.obj;
				if(k.data == null)
				{
					k.nodataLayout.setVisibility(View.VISIBLE);
					return;
				}
				k.nodataLayout.setVisibility(View.GONE);
				if(k.sectionAdapter == null)
				{
					k.arrayAdapter = k.new StandardArrayAdapter(k, R.id.txt_title,
						k.data);
					k.sectionAdapter = new SectionListAdapter(k.getLayoutInflater(),
						k.arrayAdapter);
				
				// System.out.println((listView==null) + " "+ (sectionAdapter==null));
					k.listView.setAdapter(k.sectionAdapter);
					k.listView.setOnScrollListener(k.sectionAdapter);
					k.listView.setPinnedHeaderView(k.getLayoutInflater().inflate(
						R.layout.pinnedlistview_header, k.listView, false));
				}else
				{
					k.arrayAdapter.notifyDataSetChanged();
				}
				k.listView.setSelectionFromTop(k.index==0?0:k.index-1, 10);	//项目从0开始计数，跳到哪一条
																//10是举例顶部的距离
				break;
			case -1:
				k.nodataLayout.setVisibility(View.VISIBLE);
				break;
			}
		}
	}
}
