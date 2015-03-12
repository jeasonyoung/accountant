package com.changheng.accountant.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.QuestionMyNoteAdapter;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamNote;

public class QuestionMyNotebookActivity extends Activity{
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private ListView notebookListView;
	private LinearLayout nodataLayout;
	private ArrayList<ExamNote> data;
	private PaperDao dao;
	private QuestionMyNoteAdapter mAdapter;
	private String classid,username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doproblemrecord_tier2);
		initView();
		initData();
		this.notebookListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				showDeleteWindow(arg2);
				return false;
			}
		});
	}
	private void initData()
	{
		dao = new PaperDao(this);
		Intent intent = this.getIntent();
		((TextView) this.findViewById(R.id.paperTitle)).setText(intent.getStringExtra("className"));
		classid = intent.getStringExtra("classid");
		
		////恢复登录的状态，
		((AppContext) getApplication()).recoverLoginStatus();
		
		username = ((AppContext)getApplication()).getUsername();
		final Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch(msg.what)
				{
				case 1:
					loadingLayout.setVisibility(View.GONE);
					if(data==null||data.size()==0)
					{
						contentLayout.setVisibility(View.GONE);
						nodataLayout.setVisibility(View.VISIBLE);
					}
					else
					{
						mAdapter = new QuestionMyNoteAdapter(QuestionMyNotebookActivity.this,dao, data);
						notebookListView.setAdapter(mAdapter);
					}
					break;
				case -1:
					loadingLayout.setVisibility(View.GONE);
					Toast.makeText(QuestionMyNotebookActivity.this, "加载数据出错", Toast.LENGTH_SHORT).show();
					break;
				}
			};
		};
		loadingLayout.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				try
				{
					data = (ArrayList<ExamNote>) dao.findNotes(classid,username);
					handler.sendEmptyMessage(1);
				}catch(Exception e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
			
		}.start();
	}
	private void initView()
	{
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.contentLayout = (LinearLayout) this.findViewById(R.id.questionContentLayout);
		this.notebookListView = (ListView) this.findViewById(R.id.question_record_ListView);
		this.findViewById(R.id.returnbtn).setOnClickListener(new ReturnBtnClickListener(this));
	}
	@Override
	protected void onPause() {
		super.onPause();
//		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		MobclickAgent.onResume(this);
		
	}
	//选择是否删除
	private void showDeleteWindow(final int index)
	{
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setTitle("删除").setMessage("是否删除此笔记").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// TODO Auto-generated method stub
				//删除笔记
				dao.deleteNote(data.get(index));
				data.remove(index);
				mAdapter.notifyDataSetChanged();
				if(data.size()==0)
				{
					nodataLayout.setVisibility(View.VISIBLE);
				}
			}                      
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}                      
		});
		localBuilder.create().show();
	}
}