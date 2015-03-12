package com.changheng.accountant.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.CourseDao;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.CourseList;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.XMLParseUtil;
import com.google.gson.Gson;

public class ChooseCourseActivity extends BaseActivity implements OnClickListener{
	private ListView courseList;
	private CourseDao dao;
	private LinearLayout reloadLayout;
	private ProgressDialog proDialog;
	private ArrayList<Course> courses;
	
	private Button btnLast,btnShowPop;
	private PopupWindow popWindow;
	private PaperDao paperDao;
	private ExamRecord r;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_choosecourse);
		initViews();
		initData();
	}
	private void initData()
	{
		dao = new CourseDao(this);
		courses = dao.findAllClass();
		if(courses==null||courses.size()==0)
		{
			new GetDataTask().execute();
		}else
		{
			courseList.setAdapter(new ArrayAdapter<Course>(ChooseCourseActivity.this,R.layout.item_choosecourse_list,R.id.list_title,courses));
		}
	}
	private void initViews()
	{
		((TextView) this.findViewById(R.id.title)).setText("模拟考试");
		this.courseList = (ListView) this.findViewById(R.id.course_list);
		this.reloadLayout = (LinearLayout) this.findViewById(R.id.reload);//重载视图
		this.btnShowPop = (Button) this.findViewById(R.id.showPop);	//显示上一次的练习
		this.findViewById(R.id.btn_goback).setOnClickListener(this);//返回按钮
		this.btnShowPop.setOnClickListener(this);
		this.findViewById(R.id.btn_reload).setOnClickListener(this);//重载按钮
		this.courseList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ChooseCourseActivity.this,QuestionPaperListActivity.class);
				intent.putExtra("classid", courses.get(arg2).getCourseId());
				intent.putExtra("classname",courses.get(arg2).getCourseName());
				ChooseCourseActivity.this.startActivity(intent);
			}
		});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.btn_reload:
			new GetDataTask().execute();
			break;
		case R.id.showPop:
			showPop(v);
			break;
		case R.id.lastPractice:
			gotoDoExamActivity();
			break;
		}
	}
	private void gotoDoExamActivity()
	{
		if(r == null ){
			popWindow.dismiss();
			return;
		}
		popWindow.dismiss();
		Intent intent = new Intent(this,QuestionPaperInfoActivity.class);
		intent.putExtra("paperJson",new Gson().toJson(paperDao.findPaperById(r.getPaperId())));
		this.startActivity(intent);
	}
	private class GetDataTask extends AsyncTask<String,Void,CourseList>
	{
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			proDialog = ProgressDialog.show(ChooseCourseActivity.this, null, "数据初始化...",
					true, true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			super.onPreExecute();
		}
		@Override
		protected CourseList doInBackground(String... params) {
			// TODO Auto-generated method stub
			try
			{
				CourseList result = XMLParseUtil.parseClass(ApiClient.getCourseData((AppContext)(ChooseCourseActivity.this.getApplication()),AreaUtils.areaCode));
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
				courseList.setAdapter(new ArrayAdapter<Course>(ChooseCourseActivity.this,R.layout.item_choosecourse_list,R.id.list_title,courses));
				reloadLayout.setVisibility(View.GONE);
			}else
			{
				reloadLayout.setVisibility(View.VISIBLE);
			}
		};
	}
	private void initPop()
	{
		btnShowPop.setVisibility(View.GONE);
		if(paperDao == null) paperDao = new PaperDao(this);
		 r = paperDao.findLastRecord(0);
		if(r==null) return;
		final Handler mHandler = new Handler();
		if(popWindow == null)
		{
			View v = LayoutInflater.from(this).inflate(
					R.layout.pop_last_exam, null);
			btnLast = (Button) v.findViewById(R.id.lastPractice);
			btnLast.getBackground().setAlpha(100);
			btnLast.setOnClickListener(this);
			popWindow = new PopupWindow(v, ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			popWindow.setAnimationStyle(R.style.AnimationPreview);
			popWindow.setBackgroundDrawable(new BitmapDrawable());
			popWindow.setFocusable(true); // 使其聚焦
			popWindow.setOutsideTouchable(true);
			popWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					btnShowPop.setVisibility(View.VISIBLE);
				}
			});
		}
		/***************** 以下代码用来循环检测activity是否初始化完毕 ***************/
		Runnable showPopWindowRunnable = new Runnable() {
			@Override
			public void run() {
				// 得到activity中的根元素
				View view = findViewById(R.id.parent);
				// 如何根元素的width和height大于0说明activity已经初始化完毕
				if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
					// 显示popwindow
					popWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
					// 停止检测
					mHandler.removeCallbacks(this);
				} else {
					// 如果activity没有初始化完毕则等待5毫秒再次检测
					mHandler.postDelayed(this, 50);
				}
			}
		};
		// 开始检测
		mHandler.post(showPopWindowRunnable);
		/****************** 以上代码用来循环检测activity是否初始化完毕 *************/
	}
	private void showPop(View v)
	{
		if(popWindow!=null)
		{
			popWindow.showAtLocation(findViewById(R.id.parent), Gravity.BOTTOM, 0, 0);
		}
		v.setVisibility(View.GONE);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(courses == null || courses.size()==0)
			return;
		initPop();
	}
}
