package com.changheng.accountant.ui;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ChapterListAdapter;
import com.changheng.accountant.dao.CourseDao;
import com.changheng.accountant.entity.Chapter;

public class ChapterDetailActivity extends BaseActivity implements OnClickListener{
	private ListView courseList;
	private CourseDao dao;
	private LinearLayout reloadLayout;
	private LinearLayout nodataLayout;
	private ProgressDialog proDialog;
	private List<Chapter> littleChapters;
	private String chapterPid;
	private String chapterName;
	private String className;
	private String classId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_choosecourse);
		initViews();
		initData();
	}
	@Override
	protected void onStart() {
		super.onStart();
	}
	private void initData()
	{
		proDialog = ProgressDialog.show(ChapterDetailActivity.this, null, "数据加载中...",
				true, true);
		proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		final Handler handler = new Handler()
		{
			public void handleMessage(android.os.Message msg) {
				if(proDialog!=null)
				{
					proDialog.dismiss();
				}
				switch(msg.what)
				{
				case 1:
					nodataLayout.setVisibility(View.GONE);
					reloadLayout.setVisibility(View.GONE);
					courseList.setAdapter(new ChapterListAdapter(ChapterDetailActivity.this,littleChapters));
					break;
				case 0:
					nodataLayout.setVisibility(View.VISIBLE);
					break;
				case -1:
					reloadLayout.setVisibility(View.VISIBLE);
					break;
				}
			};
		};
		new Thread(){
			public void run() {
				try
				{
					littleChapters = dao.findAllChapterByPid(chapterPid);
					handler.sendEmptyMessage((littleChapters!=null&&littleChapters.size()>0)?1:0);
				}catch(Exception e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
		}.start();
	}
	private void initViews()
	{
		this.courseList = (ListView) this.findViewById(R.id.course_list);
		this.reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.findViewById(R.id.btn_reload).setOnClickListener(this);
		this.courseList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ChapterDetailActivity.this,QuestionPraticeInfoActivity.class);
				intent.putExtra("chapterId", littleChapters.get(arg2).getChapterId());
				intent.putExtra("classId", classId);
				intent.putExtra("chapterName",littleChapters.get(arg2).getTitle());
				ChapterDetailActivity.this.startActivity(intent);
			}
		});
		
		dao = new CourseDao(this);
		Intent intent = getIntent();
		chapterPid = intent.getStringExtra("pid");
		chapterName = intent.getStringExtra("chapterName");
		className = intent.getStringExtra("className");
		classId = intent.getStringExtra("classid");
		((TextView) this.findViewById(R.id.title)).setText(className);
		((TextView) this.findViewById(R.id.littleTitle)).setText(chapterName);
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
			initData();
			break;
		}
	}
}
