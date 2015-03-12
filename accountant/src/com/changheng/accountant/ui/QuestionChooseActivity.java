package com.changheng.accountant.ui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ChooseListAdapter;
import com.changheng.accountant.adapter.ChooseListAdapter2;
import com.changheng.accountant.adapter.QuestionGridAdapter2;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class QuestionChooseActivity extends BaseActivity implements OnClickListener{
	private ImageButton scoreFlexImg;
	private LinearLayout scoreLayout,loadingLayout,nodataLayout,lookBtn,doAgainBtn;
	private GridView scoreGridView;
	private ListView questionListView;
	private List<ExamRule> ruleList;
	private int[] tOrF;
	private String action,ruleListJson,questionListJson;
	private String[] data ;
	private int[] trueOfFalse;
	private Intent intent;
	private String username;
	private SparseBooleanArray isDone;
	private PaperDao dao;
	private ExamRecord r;
	private Gson gson ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_question_exam_directory);
		findView();
		initData();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		initView();
		super.onStart();
	}
	private void findView()
	{
		((TextView)this.findViewById(R.id.title)).setText("答题情况");
		this.scoreFlexImg = (ImageButton) this.findViewById(R.id.scoreFlexImg);
		this.scoreLayout = (LinearLayout) this.findViewById(R.id.exam_scoreLayout);
		this.loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
//		this.examDirectoryLayout = (LinearLayout) this.findViewById(R.id.examDirectoryLayout);
		this.scoreGridView = (GridView) this.findViewById(R.id.scoreGridView);
		this.questionListView = (ListView) this.findViewById(R.id.question_directoryListView);
		this.lookBtn = (LinearLayout) this.findViewById(R.id.question_directory_lookBtn_Layout);
		this.doAgainBtn = (LinearLayout) this.findViewById(R.id.quesiton_directory_repeatBtn_layout);
		this.scoreFlexImg.setOnClickListener(this);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.lookBtn.setOnClickListener(this);
		this.doAgainBtn.setOnClickListener(this);
	}
	private void initData()
	{
		intent = this.getIntent();
		this.action = intent.getStringExtra("action");
		Type type = new TypeToken<ArrayList<ExamRule>>(){}.getType();
		gson = new Gson();
		this.ruleListJson = intent.getStringExtra("ruleListJson");
		this.ruleList = gson.fromJson(ruleListJson, type);
		this.isDone = gson.fromJson(intent.getStringExtra("isDone"), SparseBooleanArray.class);
		this.trueOfFalse = gson.fromJson(intent.getStringExtra("trueOfFalse"), int[].class);
		this.r = gson.fromJson(intent.getStringExtra("record"), ExamRecord.class);
		this.questionListJson =  intent.getStringExtra("tOrF");
		if(questionListJson != null)
		{
			this.tOrF = gson.fromJson(questionListJson, int[].class);
		}
		this.username = intent.getStringExtra("username");
//		this.paperId = intent.getStringExtra("paperid");
		this.dao = new PaperDao(this);
	}
	private void initView()
	{
		if("chooseQuestion".equals(action))
		{
			((ImageView)this.findViewById(R.id.colorTipsIV)).setImageResource(R.drawable.answer_color_tips2);
			this.scoreLayout.setVisibility(View.GONE);
			this.loadingLayout.setVisibility(View.GONE);
			if(this.ruleList!=null&&this.ruleList.size()>0)
			{
				System.out.println("trueOfFalse1 == "+trueOfFalse);
				this.questionListView.setAdapter(new ChooseListAdapter(this,this,ruleList,isDone,trueOfFalse));
			}else
			{
				this.nodataLayout.setVisibility(View.VISIBLE);
			}
		}else if("submitPaper".equals(action)||"showResult".equals(action))
		{
			this.scoreLayout.setVisibility(View.VISIBLE);
			this.data = new String[10];
			this.data[0] = "试题总分:"+intent.getIntExtra("paperScore",0)+"分";//总分
			this.data[1] = "试题限时:"+intent.getIntExtra("paperTime",0)+"分钟";//总时
			this.data[2] = "本次得分:"+intent.getDoubleExtra("userScore",0)+"分";//本次得分[红色]
			this.data[3] = "答题耗时:"+intent.getIntExtra("useTime",0)+"分钟";//耗时
			this.data[4] = "已做:"+isDone.size()+"题";//已做
			this.data[5] = "未做:"+(tOrF.length-isDone.size())+"题";//未做
			int right = getRightNum();
			this.data[6] = "做对:"+right+"题";//做对
			this.data[7] = "做错:"+(isDone.size()-right)+"题";//做错
			this.data[8] = "共计:"+tOrF.length+"题";//共计题
			if(isDone.size()==0)
			{
				this.data[9] = "正确率:0.0%";
			}else
				this.data[9] = "正确率:"+(((int)(right*10000/isDone.size())/100.0))+"%";//正确率
			this.scoreGridView.setAdapter(new QuestionGridAdapter2(this,null,data));
			this.questionListView.setAdapter(new ChooseListAdapter2(this,this,tOrF,action));
			this.loadingLayout.setVisibility(View.GONE);
		}else
		{
			this.scoreLayout.setVisibility(View.GONE);
			this.questionListView.setAdapter(new ChooseListAdapter2(this,this,tOrF,action));
			this.loadingLayout.setVisibility(View.GONE);
		}
	}
	private int getRightNum()
	{
		//将错题加入错题集
		int count = 0;
		for(int i:tOrF)
		{
			if(i==1)
			{
				count++;
			}
		}
		return count;
//		StringBuilder errorBuf = new StringBuilder();
//		for(ExamQuestion q:questionList)
//		{
//			if(q.getAnswer().equals(q.getUserAnswer()))
//			{
//				count++;
//			}
//			if("submitPaper".equals(action)&&q.getUserAnswer()!=null&&!q.getUserAnswer().equals(q.getAnswer()))
//			{
//				errorBuf.append(q.getQid()).append("_").append(q.getUserAnswer()).append("_").append(q.getAnswer()).append(":");
//				ExamErrorQuestion error = new ExamErrorQuestion(q.getQid(),username,paperId,q.getUserAnswer());
//				dao.insertError(error);
//			}
//		}
//		if(errorBuf.length()>0)
//		{
//			final String error = errorBuf.substring(0,errorBuf.length()-1); 	//11.11修改
//			new Thread(){
//				public void run() {
//					((AppContext) getApplication()).uploadError(error);
//				};
//			}.start();
//		}
//		return count;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			returnMethod();
			break;
		case R.id.scoreFlexImg:
			toggleShowView();
			break;
		case R.id.quesiton_directory_repeatBtn_layout:
			doItAgain();
			break;
		case R.id.question_directory_lookBtn_Layout:
			showAnswer(0);
			break;
		}
	}
	private void returnMethod()
	{
		if("chooseQuestion".equals(action)||"otherChooseQuestion".equals(action))
		{
			this.setResult(50);
			this.finish();
		}else
		{
			this.finish();
		}
	}
	private void toggleShowView()
	{
		if(this.scoreGridView.getVisibility()==View.GONE)
		{
			this.scoreFlexImg.setImageResource(R.drawable.shrink);
			this.scoreGridView.setVisibility(View.VISIBLE);
			return;
		}
		if(this.scoreGridView.getVisibility()==View.VISIBLE)
		{
			this.scoreFlexImg.setImageResource(R.drawable.unfold);
			this.scoreGridView.setVisibility(View.GONE);
			return;
		}
	}
	private void doItAgain()
	{
		if("submitPaper".equals(action))
		{
			Intent data = new Intent();
			data.putExtra("action", "DoExam");
			this.setResult(30, data);
			this.finish();
		}else
		{
			//启动DoExamQuestion
			Intent mIntent = new Intent(this,QuestionDoExamActivity2.class);
			mIntent.putExtra("action", "DoExam");
			mIntent.putExtra("paperName", r.getPapername());
			mIntent.putExtra("paperId", r.getPaperId());
			mIntent.putExtra("ruleListJson",ruleListJson);
			mIntent.putExtra("username", username);
			mIntent.putExtra("tempTime",0);
			mIntent.putExtra("paperTime", r.getPapertime());
			mIntent.putExtra("paperScore", r.getPaperscore());
			r.setTempAnswer("");
			r.setIsDone(null);
			r.setTempTime(r.getPapertime()*60);
			dao.saveOrUpdateRecord(r);
//			setNull4UserAnswer();
//			mIntent.putExtra("questionListJson", gson.toJson(questionList));
			this.startActivity(mIntent);
		}
	}
	public void showAnswer(int cursor)
	{
		if("submitPaper".equals(action))
		{
			Intent data = new Intent();
			data.putExtra("action", "showQuestionWithAnswer");  
        	data.putExtra("cursor", 0);  
         	//设置请求代码 
        	this.setResult(20, data);
        	this.finish();
		}else if("chooseQuestion".equals(action)||"otherChooseQuestion".equals(action))
		{
			Intent data=new Intent();  
         	data.putExtra("action", "showQuestionWithAnswer");  
         	data.putExtra("cursor", cursor);  
         	//设置请求代码  
         	this.setResult(20, data);  
         	//结束Activity
         	this.finish();
		}else if("myErrors".equals(action))
		{
			Intent data=new Intent();  
         	data.putExtra("action", "myErrors");  
         	data.putExtra("cursor", cursor);  
         	//设置请求代码  
         	this.setResult(20, data);  
         	//结束Activity
         	this.finish();
		}
		else
		{
			//启动DoExamQuestion
			Intent mIntent = new Intent(this,QuestionDoExamActivity2.class);
			mIntent.putExtra("action", "showQuestionWithAnswer");  
			mIntent.putExtra("paperName", r.getPapername());
			mIntent.putExtra("paperId", r.getPaperId());
			mIntent.putExtra("ruleListJson",ruleListJson);
			mIntent.putExtra("username", username);
			mIntent.putExtra("cursor", cursor);
			mIntent.putExtra("tempTime", r.getTempTime());
			mIntent.putExtra("paperTime", r.getPapertime());
			mIntent.putExtra("paperScore", r.getPaperscore());
//			mIntent.putExtra("questionListJson", questionListJson);
			this.startActivity(mIntent);
		}
			//��doexamQuestion
	}
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
	{
	    if ((paramKeyEvent.getKeyCode() == 4) && (paramKeyEvent.getRepeatCount() == 0))
	    {
	    	if("chooseQuestion".equals(action))
	    	{
	    		this.setResult(50);
				this.finish();
	    		return true;
	    	}
	    }
	    return super.onKeyDown(paramInt, paramKeyEvent);
	}
//	private void setNull4UserAnswer() {
//		for (ExamQuestion q : questionList) {
//			q.setUserAnswer(null);
//		}
//	}
}
