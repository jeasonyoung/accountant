package com.changheng.accountant.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.dao.PlanDao;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRule;
import com.google.gson.Gson;

public class ChapterPointerDetailActivity extends BaseActivity{
	
	private WebView webView;
	private TextView linkTv;
	private RelativeLayout loading;
	private String body,knowledgeId;
	private List<ExamQuestion> questionList;
	private List<ExamRule> ruleList;
	private ProgressDialog proDialog;
	private int flag;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_about2);
		flag = getIntent().getIntExtra("flag", 0);
		knowledgeId = getIntent().getStringExtra("knowledgeId");
		initView();
	}
	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void initView()
	{
		webView = (WebView) findViewById(R.id.web_view);
		linkTv = (TextView) findViewById(R.id.linkTv);
		loading = (RelativeLayout) findViewById(R.id.loading);
		findViewById(R.id.btn_goback).setOnClickListener(new ReturnBtnClickListener(this));
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDefaultFontSize(16);
		webView.addJavascriptInterface(new OnKnowledgePracticeListener() {
			@Override
			public void onPracticeClick(final String knowledgeId) {
				System.out.println(knowledgeId);
				final Handler handler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						if(proDialog!=null &&proDialog.isShowing())
						{
							proDialog.dismiss();
						}
						switch(msg.what)
						{
						case 1:
							if(questionList==null||questionList.size()==0)
							{
								Toast.makeText(ChapterPointerDetailActivity.this, "暂时没有题目", Toast.LENGTH_SHORT).show();
								return;
							}
							Gson gson = new Gson();
							Intent intent = new Intent(ChapterPointerDetailActivity.this, QuestionDoExamActivity1.class);
//							intent.putExtra("paperId",qpi.zuheid);
							intent.putExtra("knowledgeId", knowledgeId);
							intent.putExtra("action", "practice");
							intent.putExtra("ruleListJson", gson.toJson(ruleList));
							intent.putExtra("cursor", 0);
							ChapterPointerDetailActivity.this.startActivity(intent);
							break;
						case 2:
							Toast.makeText(ChapterPointerDetailActivity.this, "加载出错，稍后再试", Toast.LENGTH_SHORT).show();
							break;
						}
					}
				};
				if(proDialog == null)
				{
					proDialog = ProgressDialog.show(ChapterPointerDetailActivity.this, null, "加载中...", true, true);
					proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				}
				new Thread(){
					public void run() {
						try{
							PaperDao dao = new PaperDao(ChapterPointerDetailActivity.this);
							questionList = dao.findQuestionOfKnowledge(null, knowledgeId);
							ruleList = getRuleList(questionList);
							handler.sendEmptyMessage(1);
						}catch(Exception e)
						{
							e.printStackTrace();
							handler.sendEmptyMessage(-1);
						}
					};
					private List<ExamRule> getRuleList(List<ExamQuestion> questionList) {
						if(questionList==null||questionList.size()==0)
						{
							return null;
						}
						List<ExamRule> list = new ArrayList<ExamRule>();
						StringBuffer buf = new StringBuffer();
						//按题型的1,2,3,4来分
						for (int i = 1; i < 5; i++) {
							for (ExamQuestion q : questionList) {

								if (String.valueOf(i).equals(q.getQType())) {
									buf.append(q.getQid()).append(",");
								}
							}
							if(buf.length() > 0){
								ExamRule r = new ExamRule();
								r.setRuleTitle(getTitleStr(i));
								r.setOrderInPaper(i);
								if (buf.length() > 0) {
									r.setContainQids(buf.substring(0, buf.lastIndexOf(",")));
									buf.delete(0, buf.length());
								}
								list.add(r);
							}
						}
						return list;
					}
					private String getTitleStr(int i) {
						return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
								: i == 4 ? "判断题" : "综合题";
					}
				}.start();
			};
		}, "mPracticeListener");
		webView.setWebViewClient(new SampleWebViewClient());
		//webView加载数据
		Intent intent = getIntent();
		body = intent.getStringExtra("content");
		((TextView) findViewById(R.id.title)).setText(intent.getStringExtra("knowledgeTitle"));
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		initData();
		super.onStart();
	}
	private void initData()
	{
		if(body == null)
		{
			final Handler handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					switch(msg.what)
					{
					case 1:
						webView.loadDataWithBaseURL(null, body, "text/html",
								"utf-8", null);
						if(flag==0)
						{
							linkTv.setVisibility(View.VISIBLE);
							linkTv.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									practice();
								}
							});
						}
						
						break;
					}
				}
			};
			new Thread(){
				public void run() {
					body = new PlanDao(ChapterPointerDetailActivity.this,null).findKnowledgeContent(knowledgeId);
//					if(flag == 0)
//					{
//						body = body + "<P>&nbsp;&nbsp;<a style='color:blue;font-size:20px' onClick=\"window.mPracticeListener.onPracticeClick('"+knowledgeId+"')\">开始练习</P>";
//					}
					handler.sendEmptyMessage(1);
				};
			}.start();
		}else{
			webView.loadDataWithBaseURL(null, body, "text/html",
					"utf-8", null);
			if(flag==0)
			{
				linkTv.setVisibility(View.VISIBLE);
				linkTv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						practice();
					}
				});
			}
		}
	}
	private class SampleWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if ( url.contains("file:///") == true){
	            view.loadUrl(url);
	            return true;
	        }else{
	            Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
	            startActivity(in);
	            return true;
	        }
		}
		@Override
		public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				loading.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}
	}
	public interface OnKnowledgePracticeListener{
		void onPracticeClick(String knowledgeId);
	}
	private void practice()
	{
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if(proDialog!=null &&proDialog.isShowing())
				{
					proDialog.dismiss();
				}
				switch(msg.what)
				{
				case 1:
					if(questionList==null||questionList.size()==0)
					{
						Toast.makeText(ChapterPointerDetailActivity.this, "暂时没有题目", Toast.LENGTH_SHORT).show();
						return;
					}
					Gson gson = new Gson();
					Intent intent = new Intent(ChapterPointerDetailActivity.this, QuestionDoExamActivity1.class);
//					intent.putExtra("paperId",qpi.zuheid);
					intent.putExtra("knowledgeId", knowledgeId);
					intent.putExtra("action", "practice");
					intent.putExtra("ruleListJson", gson.toJson(ruleList));
					intent.putExtra("cursor", 0);
					ChapterPointerDetailActivity.this.startActivity(intent);
					break;
				case 2:
					Toast.makeText(ChapterPointerDetailActivity.this, "加载出错，稍后再试", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
		if(proDialog == null)
		{
			proDialog = ProgressDialog.show(ChapterPointerDetailActivity.this, null, "加载中...", true, true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		new Thread(){
			public void run() {
				try{
					PaperDao dao = new PaperDao(ChapterPointerDetailActivity.this);
					questionList = dao.findQuestionOfKnowledge(null, knowledgeId);
					ruleList = getRuleList(questionList);
					handler.sendEmptyMessage(1);
				}catch(Exception e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
			private List<ExamRule> getRuleList(List<ExamQuestion> questionList) {
				if(questionList==null||questionList.size()==0)
				{
					return null;
				}
				List<ExamRule> list = new ArrayList<ExamRule>();
				StringBuffer buf = new StringBuffer();
				//按题型的1,2,3,4来分
				for (int i = 1; i < 5; i++) {
					for (ExamQuestion q : questionList) {

						if (String.valueOf(i).equals(q.getQType())) {
							buf.append(q.getQid()).append(",");
						}
					}
					if(buf.length() > 0){
						ExamRule r = new ExamRule();
						r.setRuleTitle(getTitleStr(i));
						r.setOrderInPaper(i);
						if (buf.length() > 0) {
							r.setContainQids(buf.substring(0, buf.lastIndexOf(",")));
							buf.delete(0, buf.length());
						}
						list.add(r);
					}
				}
				return list;
			}
			private String getTitleStr(int i) {
				return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
						: i == 4 ? "判断题" : "综合题";
			}
		}.start();
	}
}
