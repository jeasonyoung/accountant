package com.changheng.accountant.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.changheng.accountant.R;

/**
 * 资讯
 * @author Administrator
 *
 */
public class InformationMainActivity extends BaseActivity implements OnClickListener{
	//25,5,1,6,13,12
	public static final int INFO_OUTLINE = 25;
	public static final int INFO_GUIDE = 5;
	public static final int INFO_INFORMATION = 1;
	public static final int INFO_EXPERIENCE = 6;
	public static final int INFO_REVIEW = 13;
	public static final int INFO_TEACHER = 12;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_information);
		initViews();
	}
	private void initViews()
	{
		((TextView)findViewById(R.id.title)).setText("考试指南");
		((TextView) findViewById(R.id.txt_teacher)).setOnClickListener(this);//名师
		findViewById(R.id.btn_goback).setOnClickListener(this); //返回按钮
		((TextView) findViewById(R.id.txt_outline)).setOnClickListener(this);//大纲
		((TextView) findViewById(R.id.txt_guide)).setOnClickListener(this);//指导
		((TextView) findViewById(R.id.txt_imformation)).setOnClickListener(this);//资讯
		((TextView) findViewById(R.id.txt_review)).setOnClickListener(this);//复习
		((TextView) findViewById(R.id.txt_experience)).setOnClickListener(this);//经验
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			this.finish();
			return;
		}
		gotoInfoList(v.getId());
		
	}
	private void gotoInfoList(int rid)
	{
		Intent intent = new Intent(this,InformationListActivity.class);
		switch(rid)
		{
		case R.id.txt_outline:
			intent.putExtra("action", INFO_OUTLINE);
			intent.putExtra("title","考试大纲");
			break;
		case R.id.txt_guide:
			intent.putExtra("action", INFO_GUIDE);
			intent.putExtra("title","报考指南");
			break;
		case R.id.txt_imformation:
			intent.putExtra("action", INFO_INFORMATION);
			intent.putExtra("title","考试资讯");
			break;
		case R.id.txt_experience:
			intent.putExtra("action", INFO_EXPERIENCE);
			intent.putExtra("title","备考经验");
			break;
		case R.id.txt_review:
			intent.putExtra("action", INFO_REVIEW);
			intent.putExtra("title","复习指导");
			break;
		case R.id.txt_teacher:
			intent.putExtra("action", INFO_TEACHER);
			intent.putExtra("title","名师指点");
			break;
		}
		startActivity(intent);
	}
}
