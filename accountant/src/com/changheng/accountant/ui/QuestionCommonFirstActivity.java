package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.QuestionCommonAdapter;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.QuestionAdapterData;
import com.changheng.accountant.view.ScrollLayout;
import com.changheng.accountant.view.ScrollLayout.OnViewChangeListener;
import com.google.gson.Gson;

public class QuestionCommonFirstActivity extends BaseActivity{
	private LinearLayout loadingLayout;
	private LinearLayout nodataLayout;
	private String actionName,username;
	private Class c;
	private int stringResId;
	private Gson gson;
	private PaperDao dao;
	private Handler mHandler;
	
	private ScrollLayout scrollLayout;
	private RadioGroup radioGroup;
	private ListView examListView,timeListView,numListView;
	
	private ArrayList<QuestionAdapterData> data_exam,data_time,data_num;
	private ProgressDialog proDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doproblem_record);
		Intent mIntent = this.getIntent();
		actionName = mIntent.getStringExtra("actionName");
		////恢复登录的状态，
		((AppContext) getApplication()).recoverLoginStatus();
		
		username = ((AppContext)getApplication()).getUsername();
		c = "myNotes".equals(actionName)?QuestionMyNotebookActivity.class:
			"myErrors".equals(actionName)?QuestionDoExamActivity2.class:
				QuestionDoExamActivity2.class;
		stringResId ="myNotes".equals(actionName)?R.string.my_notebookStr:
			"myErrors".equals(actionName)?R.string.errorQuesitionStr:
				R.string.my_favoriteStr;
		gson = new Gson();
		dao = new PaperDao(this);
		mHandler = new MyHandler(this);
		initView();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		initData();
//		this.loadingLayout.setVisibility(View.GONE);
//		if(data==null||data.size()==0)
//		{
//			this.contentLayout.setVisibility(View.GONE);
//			this.nodataLayout.setVisibility(View.VISIBLE);
//		}
//		else
//		{
//			this.notebookListView.setAdapter(new QuestionCommonAdapter(this, data));
//			this.notebookListView.setOnItemClickListener(new OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//						long arg3) {
					// TODO Auto-generated method stub
//					QuestionAdapterData qad = data.get(arg2);
//					Intent mIntent = new Intent(QuestionCommonFirstActivity.this,c);//!!!�޸�class
					//�����
//					mIntent.putExtra("paperId", qad.getPaperId());
//					mIntent.putExtra("username", username);
//					mIntent.putExtra("title", qad.getTitle());
//					mIntent.putExtra("action",actionName);
//					mIntent.putExtra("questionListJson", getQuestionListJson(qad.getPaperId(), username, actionName));
//					QuestionCommonFirstActivity.this.startActivity(mIntent);
					
//				}
//			});
//		}
		super.onStart();
	}

	private void initData()
	{
		//find from database.
		this.loadingLayout.setVisibility(View.VISIBLE);
		this.scrollLayout.setVisibility(View.GONE);
		new Thread(){
			public void run() {
				try{
					data_exam = (ArrayList<QuestionAdapterData>) dao.findAdapterData(actionName);
					if("myErrors".equals(actionName))
					{
						data_time = (ArrayList<QuestionAdapterData>) dao.findErrorAdapterData(2);
						data_num = (ArrayList<QuestionAdapterData>) dao.findErrorAdapterData(3);
					}
					mHandler.sendEmptyMessage(1);
				}catch(Exception e)
				{
					e.printStackTrace();
					mHandler.sendEmptyMessage(-1);
				}
			};
		}.start();
	}
	private void initView()
	{
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.scrollLayout = (ScrollLayout) this.findViewById(R.id.scrollLayout);
		this.radioGroup = (RadioGroup) this.findViewById(R.id.button_line_layout);
		this.examListView = (ListView) this.findViewById(R.id.follow_exam_ListView);
		this.timeListView = (ListView) this.findViewById(R.id.forget_line_ListView);
		this.numListView = (ListView) this.findViewById(R.id.frequent_ListView);
		((TextView) this.findViewById(R.id.TopTitle1)).setText(stringResId);
		this.scrollLayout.scrollToScreen(0);
		if("myErrors".equals(actionName))
		{
			this.scrollLayout.SetOnViewChangeListener(new OnViewChangeListener() {
				@Override
				public void OnViewChange(int view) {
					// TODO Auto-generated method stub
					((RadioButton)(radioGroup.getChildAt(view*2))).setChecked(true);
				}
			});
		}else
		{
			this.scrollLayout.setIsScroll(false);
			this.radioGroup.setVisibility(View.GONE);
			this.findViewById(R.id.questionContentLayout1).setVisibility(View.GONE);
			this.findViewById(R.id.questionContentLayout2).setVisibility(View.GONE);
		}
		//按科目
		this.examListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(data_exam==null||data_exam.size()==0)
				{
					Toast.makeText(QuestionCommonFirstActivity.this, "木有数据", Toast.LENGTH_SHORT).show();
					return;
				}
				if(data_exam.get(arg2).getCount()==0)
				{
					Toast.makeText(QuestionCommonFirstActivity.this, "木有数据", Toast.LENGTH_SHORT).show();
					return;
				}
				if("myNotes".equals(actionName))
				{
					QuestionAdapterData data = data_exam.get(arg2);
					Intent mIntent = new Intent(QuestionCommonFirstActivity.this,c);//!!!�޸�class
					mIntent.putExtra("classid",data.getCourseId());
					mIntent.putExtra("className", data.getTitle());
					QuestionCommonFirstActivity.this.startActivity(mIntent);
					return;
				}
				if("myFavors".equals(actionName))
				{
					Intent mIntent = new Intent(QuestionCommonFirstActivity.this,c);//!!!�޸�class
					mIntent.putExtra("action", actionName);
					mIntent.putExtra("username", username);
					mIntent.putExtra("paperId", data_exam.get(arg2).getCourseId());
					QuestionCommonFirstActivity.this.startActivity(mIntent);
					return;
				}
				if("myErrors".equals(actionName))
				{
					Intent intent = new Intent(QuestionCommonFirstActivity.this,c);//!!!�޸�class
					intent.putExtra("action", actionName);
					intent.putExtra("username", username);
					intent.putExtra("sqlStr", "q.classid = "+data_exam.get(arg2).getCourseId());
					QuestionCommonFirstActivity.this.startActivity(intent);
					return;
				}
//				if(proDialog == null)
//				{
//					proDialog = ProgressDialog.show(QuestionCommonFirstActivity.this, null, "加载中...",
//							true, false);
//					proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//				}else
//				{
//					proDialog.show();
//				}
//				new Thread(){
//					public void run() {
//						try{
//						String json = getQuestionListJson(data_exam.get(arg2).getCourseId(),username,actionName);
//						Message msg = mHandler.obtainMessage();
//						msg.what = 2;
//						msg.obj = json;
//						mHandler.sendMessage(msg);
//						}catch(Exception e)
//						{
//							e.printStackTrace();
//							mHandler.sendEmptyMessage(-1);
//						}
//					};
//				}.start();
			}
		});
		//按时间
		this.timeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(data_time.get(arg2).getCount()==0)
				{
					Toast.makeText(QuestionCommonFirstActivity.this, "木有数据", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(QuestionCommonFirstActivity.this,c);//!!!�޸�class
				intent.putExtra("action", actionName);
				intent.putExtra("username", username);
				intent.putExtra("sqlStr", data_time.get(arg2).getCourseId());
				QuestionCommonFirstActivity.this.startActivity(intent);
				return;
				
			}
		});
		//按出错次数
		this.numListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(data_num.get(arg2).getCount()==0)
				{
					Toast.makeText(QuestionCommonFirstActivity.this, "木有数据", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(QuestionCommonFirstActivity.this,c);//!!!�޸�class
				intent.putExtra("action", actionName);
				intent.putExtra("username", username);
				intent.putExtra("sqlStr", data_num.get(arg2).getCourseId());
				QuestionCommonFirstActivity.this.startActivity(intent);			}
		});
	}
	// android:onClick配置
	public void onRadioClick(View v)
	{
		int cur = this.scrollLayout.getCurScreen();
		int tag = Integer.valueOf((String) v.getTag());
		if(cur==tag)
			return ;
		this.scrollLayout.scrollToScreen(tag);
	}
	public void goBack(View v)
	{
		this.finish();
	}
	static class MyHandler extends Handler
	{
		private WeakReference<QuestionCommonFirstActivity> weak;
		public MyHandler(QuestionCommonFirstActivity activity) {
			// TODO Auto-generated constructor stub
			this.weak = new WeakReference<QuestionCommonFirstActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			QuestionCommonFirstActivity qcf = weak.get();
			switch(msg.what)
			{
			case 1:
				qcf.loadingLayout.setVisibility(View.GONE);
				if(qcf.data_exam==null||qcf.data_exam.size()==0)
				{
					qcf.scrollLayout.setVisibility(View.GONE);
					qcf.nodataLayout.setVisibility(View.VISIBLE);
				}
				else
				{
					qcf.examListView.setAdapter(new QuestionCommonAdapter(qcf, qcf.data_exam,0));
					if("myErrors".equals(qcf.actionName))
					{
						qcf.timeListView.setAdapter(new QuestionCommonAdapter(qcf, qcf.data_time,1));
						qcf.numListView.setAdapter(new QuestionCommonAdapter(qcf, qcf.data_num,2));
					}
					qcf.scrollLayout.setVisibility(View.VISIBLE);
				}
				break;
			case -1:
				if(qcf.proDialog != null)
				{
					qcf.proDialog.dismiss();
				}
				Toast.makeText(qcf, "加载数据出错", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				if(qcf.proDialog != null)
				{
					qcf.proDialog.dismiss();
				}
				Intent mIntent = new Intent(qcf,qcf.c);//!!!�޸�class
				mIntent.putExtra("action", qcf.actionName);
				mIntent.putExtra("questionListJson", (String)msg.obj);
				qcf.startActivity(mIntent);
				break;
			}
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(proDialog!=null){
			proDialog.dismiss();
		}
		super.onDestroy();
	}
}
