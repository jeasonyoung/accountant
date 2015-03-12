package com.changheng.accountant.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.CourseSelectAdapter;
import com.changheng.accountant.dao.CourseDao;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.CourseList;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.XMLParseUtil;
import com.changheng.accountant.view.wheel.NumericWheelAdapter;
import com.changheng.accountant.view.wheel.WheelView;

public class SetTimeActivity extends BaseActivity implements OnClickListener{
	private PopupWindow datePop;
	private TextView dateTxt,restDaysTxt,topTitle;
	private SimpleDateFormat format;
	private WheelView year,month,day;
	private Button setDateBtn,setCourseBtn;
	private long now ;
	private long setTime;
	private int curYear,curMonth,curDay;
	private AppConfig appConfig;
	private ViewSwitcher viewSwitcher;
	private ListView courseList;
	private CourseDao dao;
	private ArrayList<Course> courses;
	private ProgressDialog proDialog;
	private LinearLayout reloadLayout;
	private int flag;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_settime);
		format = new SimpleDateFormat("yyyy年MM月dd日",Locale.CHINA);
		appConfig = AppConfig.getAppConfig(this);
		flag = getIntent().getIntExtra("flag", 0);
		initViews();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		now = System.currentTimeMillis();
		dateTxt.setText(format.format(new Date(setTime==0?now:setTime)));
		restDaysTxt.setText(calculateRestDay(format.format(setTime)));
		super.onStart();
	}
	private void initViews()
	{
		topTitle = (TextView) this.findViewById(R.id.title);
		setDateBtn = (Button) this.findViewById(R.id.btn_enter_exam_date);
		setCourseBtn = (Button) this.findViewById(R.id.btn_set_course);
		viewSwitcher = (ViewSwitcher) this.findViewById(R.id.bottom);
		courseList = (ListView) this.findViewById(R.id.chooseCourse_listView);
		reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		topTitle.setText(R.string.setExamTime);
		dateTxt = (TextView) this.findViewById(R.id.dateTxt);
		setTime = appConfig.getExamTime();
		restDaysTxt = (TextView) this.findViewById(R.id.restDaysTxt);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		dateTxt.setOnClickListener(this);
		setDateBtn.setOnClickListener(this);
		setCourseBtn.setOnClickListener(this);
		if(flag==2)
			setCourse();
		
	}
	private void showPopWindow()
	{
		if(datePop==null)
		{
			View v = LayoutInflater.from(this).inflate(R.layout.date_set_pop, null);
			Calendar calendar = Calendar.getInstance();
			curYear = calendar.get(Calendar.YEAR);	//当前年,今年
			//设置的时间在当前时间之前
			if(setTime > now)
			{
				calendar.setTimeInMillis(setTime);
			}
			//定义wheelview
			month = (WheelView) v.findViewById(R.id.month);
	        year = (WheelView) v.findViewById(R.id.year);
	        day = (WheelView) v.findViewById(R.id.day);
	        //解决屏幕不同大小时,这个view不能自适应大小
	        int textSize = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight()/32;
	        month.setTextSize(textSize);
	        year.setTextSize(textSize);
	        day.setTextSize(textSize);
	        Button btn = (Button) v.findViewById(R.id.btn_ok);//确定按钮
	        btn.setOnClickListener(this);
	        //year 
	        int setYear = calendar.get(Calendar.YEAR);	//设置的年
	    	year.setAdapter(new NumericWheelAdapter(curYear, curYear+5));
	    	year.setCurrentItem(setYear - curYear);
	    	year.setCyclic(true);
			year.setLabel("年");
			
			//month
			curMonth = calendar.get(Calendar.MONTH);
			month.setAdapter(new NumericWheelAdapter(1, 12));
			month.setCurrentItem(curMonth);
			month.setCyclic(true);
			month.setLabel("月");
			
			//day
			curDay = calendar.get(Calendar.DAY_OF_MONTH);
			day.setAdapter(new NumericWheelAdapter(1, 31));
			day.setCurrentItem(curDay-1);
			day.setCyclic(true);
			day.setLabel("日");
			
			datePop = new PopupWindow(v,-1, ViewGroup.LayoutParams.WRAP_CONTENT);
			datePop.setBackgroundDrawable(new BitmapDrawable());
			datePop.setFocusable(true);
			datePop.setOutsideTouchable(true);
		}
		//datePop.showAtLocation(this.findViewById(R.id.settime), Gravity.BOTTOM, 0, 0);
		datePop.showAtLocation(this.findViewById(R.id.settime),80,0,0);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(datePop!=null&&datePop.isShowing())
		{
			datePop.dismiss();
		}
		super.onPause();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_ok:
			datePop.dismiss();
			String str = (year.getCurrentItem()+curYear)+"年"+
							(month.getCurrentItem()+1)+"月"+
								(day.getCurrentItem()+1)+"日";
			dateTxt.setText(str);
			restDaysTxt.setText(calculateRestDay(str));
			long value = 0;
			try {
				value = format.parse(str).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AppConfig.getAppConfig(this).setExamTime(value);
			break;
		case R.id.btn_goback:
			if(flag != 0)
				startActivity(new Intent(this,KnowledgeTodayActivity.class));
			this.finish();
			break;
		case R.id.dateTxt:
			showPopWindow();
			break;
		case R.id.btn_enter_exam_date:
			if(viewSwitcher.getDisplayedChild () == 0)
			{
				return;
			}
			setCourseBtn.setBackgroundResource(R.drawable.pregnancy_right_normal);
			setCourseBtn.setTextColor(getResources().getColor(R.color.black));
			viewSwitcher.setDisplayedChild(0);
			setDateBtn.setBackgroundResource(R.drawable.pregnancy_left_selected);
			setDateBtn.setTextColor(getResources().getColor(R.color.white));
			break;
		case R.id.btn_set_course:
			if(viewSwitcher.getDisplayedChild () == 1)
			{
				return;
			}
			setCourse();
			break;
		}
	}
	private void setCourse()
	{
		setDateBtn.setBackgroundResource(R.drawable.pregnancy_left_normal);
		setDateBtn.setTextColor(getResources().getColor(R.color.black));
		viewSwitcher.showNext();
		setCourseBtn.setBackgroundResource(R.drawable.pregnancy_right_selected);
		setCourseBtn.setTextColor(getResources().getColor(R.color.white));
		if(courses==null||courses.size()==0)
		{
			initData();
		}
	}
	private void initData()
	{
//		dao = new CourseDao(this,null);
		dao = new CourseDao(this,null);
		proDialog = ProgressDialog.show(SetTimeActivity.this, null, "数据初始化...",
				true, true);
		proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if(proDialog!=null)
				proDialog.dismiss();
				switch(msg.what)
				{
				case 0:
//					new GetDataTask().execute();
					break;
				case 1:
					//循环list获取id,设置到config当中
					String ids = appConfig.get(AppConfig.CONF_SELECTED_COURSEID);
					if(ids == null)
					{
						for(Course c:courses)
						{
							if(c == null) continue;
							ids += c.getCourseId()+",";
						}
						appConfig.set(AppConfig.CONF_SELECTED_COURSEID, ids);
					}
					courseList.setAdapter(new CourseSelectAdapter(SetTimeActivity.this,appConfig,courses));
					break;
				}
			}
		};
		new Thread(){
			public void run() {
				courses = dao.findAllClassFromPlanDB();
				if(courses==null||courses.size()==0)
				{
					handler.sendEmptyMessage(0);
				}else
				{
					handler.sendEmptyMessage(1);
				}
			};
		}.start();
		
//		if(courses==null||courses.size()==0)
//		{
//			new GetDataTask().execute();
//		}else
//		{
//			courseList.setAdapter(new CourseSelectAdapter(SetTimeActivity.this,appConfig,courses));
//		}
	}
	//计算剩余的天数
	private String calculateRestDay(String dateStr)
	{
		String s = null;
		try {
			Date thatDay = format.parse(dateStr);
			if(thatDay.before(new Date(now))) //日期在今天之前
			{
				s = "考试日期已过";
			}else
			{
				long i = (thatDay.getTime()-now)/1000/60/60/24;
				System.out.println("剩余天数："+i);
				s = i+" 天";
				if(i==0)
				{
					s = "不到 1 天";
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ((event.getKeyCode() == 4)
				) {
			if(datePop!=null&&datePop.isShowing())
			{
				datePop.dismiss();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(datePop!=null&&datePop.isShowing())
		{
			datePop.dismiss();
		}
		super.onDestroy();
	}
	private class GetDataTask extends AsyncTask<String,Void,CourseList>
	{
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			proDialog = ProgressDialog.show(SetTimeActivity.this, null, "数据初始化...",
					true, true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			super.onPreExecute();
		}
		@Override
		protected CourseList doInBackground(String... params) {
			// TODO Auto-generated method stub
			try
			{
				CourseList result = XMLParseUtil.parseClass(ApiClient.getCourseData((AppContext)(SetTimeActivity.this.getApplication()),AreaUtils.areaCode));
				dao.save(result.getClassList(), result.getChapterList());
				return result;
			}catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		protected void onPostExecute(CourseList result) {
			if(proDialog!=null)
			{
				proDialog.dismiss();
			}
			if(result!=null)
			{
				courses = result.getClassList();
//				courseList.setAdapter(new ArrayAdapter<Course>(SetTimeActivity.this,R.layout.item_choose_course_list,R.id.checkBox,courses));
				courseList.setAdapter(new CourseSelectAdapter(SetTimeActivity.this,appConfig,courses));
				reloadLayout.setVisibility(View.GONE);
			}else
			{
				reloadLayout.setVisibility(View.VISIBLE);
			}
		};
	}
}
