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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.CourseExpandableAdapter;
import com.changheng.accountant.dao.CourseDao;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.Chapter;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.CourseList;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.XMLParseUtil;

public class ChooseChapterActivity extends BaseActivity implements OnClickListener{
	private ExpandableListView expandView;
	private CourseDao dao;
	private LinearLayout reloadLayout;
	private ProgressDialog proDialog;
	private ArrayList<Course> courses;
	private ArrayList<ArrayList<Chapter>> chapters;
	
	private Button btnLast,btnShowPop;
	private PopupWindow popWindow;
	private PaperDao paperDao;
	private ExamRecord r;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_choosechapter);
		initViews();
		initData();
	}
	private void initData()
	{
		dao = new CourseDao(this);
		courses = dao.findAllClass();
		chapters = dao.findAllChapterByClass(courses);
		if(courses == null || courses.size()==0)
		{
			//开线程去获取数据
			new GetDataTask().execute();
		}else
		{
			this.expandView.setAdapter(new CourseExpandableAdapter(this, courses, chapters));
		}
	}
	private void initViews()
	{
		((TextView) this.findViewById(R.id.title)).setText("章节练习");
		this.expandView = (ExpandableListView) this.findViewById(R.id.course_list);
		this.reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.findViewById(R.id.btn_reload).setOnClickListener(this);
		this.btnShowPop = (Button) this.findViewById(R.id.showPop);
		this.btnShowPop.getBackground().setAlpha(200);
		this.btnShowPop.setOnClickListener(this);
//		this.expandView.setAdapter(new CourseExpandableAdapter(this, group, children));
		expandView.setGroupIndicator(null); //去掉默认样式
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
			gotoPractice();
			break;
		}
	}
	private void gotoPractice()
	{
		if(r == null ){
			popWindow.dismiss();
			return;
		}
		popWindow.dismiss();
		Intent intent = new Intent(this,QuestionPraticeInfoActivity.class);
		String[] arr = r.getPaperId().split("_");
		intent.putExtra("chapterId", arr[1]);
		intent.putExtra("classId", arr[0]);
		intent.putExtra("chapterName",r.getPapername2());
		this.startActivity(intent);
	}
	private class GetDataTask extends AsyncTask<String,Void,CourseList>
	{
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			proDialog = ProgressDialog.show(ChooseChapterActivity.this, null, "数据初始化...",
					true, true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			super.onPreExecute();
		}
		@Override
		protected CourseList doInBackground(String... params) {
			// TODO Auto-generated method stub
			try
			{
				CourseList result = XMLParseUtil.parseClass(ApiClient.getCourseData((AppContext)(ChooseChapterActivity.this.getApplication()),AreaUtils.areaCode));
				dao.save(result.getClassList(), result.getChapterList());
				chapters = getChapters(result);
				return result;
			}catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		private ArrayList<ArrayList<Chapter>> getChapters(CourseList result) {
			// TODO Auto-generated method stub
			ArrayList<ArrayList<Chapter>> list = new ArrayList<ArrayList<Chapter>>();
			ArrayList<Course> courses = result.getClassList();
			ArrayList<Chapter> chapters = result.getChapterList();
			for(Course c : courses)
			{
				ArrayList<Chapter> cList = new ArrayList<Chapter>();
				for(Chapter ch : chapters)
				{
					//章
					if(c.getCourseId().equals(ch.getClassId())&&"0".equals(ch.getPid()))
					{
						cList.add(ch);
					}
				}
				if(cList.size()>0)
				{
					list.add(cList);
				}
			}
			return list;
		}
		protected void onPostExecute(CourseList result) {
			if(proDialog!=null)
			{
				proDialog.dismiss();
			}
			if(result!=null)
			{
				courses = result.getClassList();
				expandView.setAdapter(new CourseExpandableAdapter(ChooseChapterActivity.this, courses, chapters));
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
		 r = paperDao.findLastRecord(1);
		 System.out.println("record = "+r);
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
