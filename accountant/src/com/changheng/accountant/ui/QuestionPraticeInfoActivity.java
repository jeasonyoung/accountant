package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.KnowlegdgeListAdapter;
import com.changheng.accountant.adapter.KnowlegdgeListAdapter2;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.dao.PlanDao;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.entity.KnowledgeList;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.StringUtils;
import com.changheng.accountant.util.XMLParseUtil;
import com.changheng.accountant.view.NewDataToast;
import com.google.gson.Gson;

public class QuestionPraticeInfoActivity extends BaseActivity implements
		OnClickListener {
	private TextView chapterTitleTv, answeredSizeTv, questionSumTv,
			errorSizeTv, accuracyTv;
	private ListView knowledgeLv;
	private Handler handler;
	private ProgressDialog proDialog;
	// data
	private PaperDao dao;
	private Gson gson;
	private String zuheid, chapterid,classid;
	private String chapterName;
	private Paper paper;
	private ExamRecord record;
	private ArrayList<Knowledge> kList;
	private int errorNum,questionCursor;
	private List<ExamQuestion> questionList;
	private List<ExamRule> ruleList;
	private AppContext appContext;
	private LinearLayout header;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_chapter_practice_info);
		initViews();
		initData1();
	}

	private void initViews() {
		((TextView) findViewById(R.id.title)).setText("章节详情");
		header = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.practice_info_header, null);
		chapterTitleTv = (TextView) header.findViewById(R.id.chapterTitle);
		answeredSizeTv = (TextView) header.findViewById(R.id.answeredSize);
		questionSumTv = (TextView) header.findViewById(R.id.questionNumTotal);
		errorSizeTv = (TextView) header.findViewById(R.id.errorSize);
		accuracyTv = (TextView) header.findViewById(R.id.accuracy);
		knowledgeLv = (ListView) findViewById(R.id.knowledge_listView);
		findViewById(R.id.btn_goback).setOnClickListener(this);
		header.findViewById(R.id.btn_practice).setOnClickListener(this);
		findViewById(R.id.btn_refresh).setOnClickListener(this);
		knowledgeLv.addHeaderView(header);
		knowledgeLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Knowledge k = kList.get(arg2-1);
				Intent intent = new Intent(QuestionPraticeInfoActivity.this,
						ChapterPointerDetailActivity.class);
				intent.putExtra("knowledgeId", k.getKnowledgeId());
				intent.putExtra("knowledgeTitle",k.getTitle());
				intent.putExtra("content", k.getFullContent());
				intent.putExtra("flag", 1);
				startActivity(intent);
			}
		});
	}

	private void initData1() {
		Intent intent = getIntent();
		chapterName = intent.getStringExtra("chapterName");
		chapterid = intent.getStringExtra("chapterId");
		classid = intent.getStringExtra("classId");
		zuheid = classid+"_"+chapterid;
		chapterTitleTv.setText(chapterName);
		dao = new PaperDao(this);
		gson = new Gson();
		handler = new MyHandler(this);
	}

	private void initData2() {
		appContext = (AppContext) getApplication();
		
		////恢复登录的状态，
		appContext.recoverLoginStatus();
		
		if (proDialog == null) {
			proDialog = ProgressDialog.show(QuestionPraticeInfoActivity.this,
					null, "玩命加载中...", true, true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
			new Thread() {
				public void run() {
					paper = dao.findPaperById(zuheid);
					if (paper == null) {
						try {
							KnowledgeList list = XMLParseUtil.parseKnowledge(
									ApiClient.getKnowledgeList(appContext,
											chapterid), chapterName, zuheid);
							paper = list.getPaper();
							System.out.println(paper);
							if (paper == null) {
								kList = new PlanDao(QuestionPraticeInfoActivity.this,null).findKnowledgeByChapter(chapterid);
								handler.sendEmptyMessage(-2);
							} else {
								kList = list.getKList();
//								kList = new PlanDao(QuestionPraticeInfoActivity.this,null).findKnowledgeByChapter(chapterid);
								//插入试卷
//								dao.insertPracticePaper(paper);
								dao.insertKnowledge(list);
								handler.sendEmptyMessage(1);
							}
						} catch (Exception e) {
							e.printStackTrace();
							Message msg = handler.obtainMessage();
							msg.what = -1;
							if (e instanceof AppException) {
								msg.obj = e;
							}
							handler.sendMessage(msg);
						}
					} else {
						if(kList == null)
							kList =  new PlanDao(QuestionPraticeInfoActivity.this,null).findKnowledgeByChapter(chapterid);
						record = dao.findRecord(appContext.getUsername(), zuheid);
						if(record!=null)
							errorNum = calcErrorNum(gson,record.getAnswers());
						ruleList = dao.findRules(zuheid);
						handler.sendEmptyMessage(1);
					}
				};
				private int calcErrorNum(Gson gson,String tOrF)
				{
					if(tOrF==null||"".equals(tOrF))  return 0;
					int[] arr= gson.fromJson(tOrF, int[].class);
					int sum=0;boolean flag = true;
					for(int i=0;i<arr.length;i++)
					{
						if(arr[i] == 0 && flag)
						{
							questionCursor = i;
							flag = false;
						}
						if(arr[i] == -1)
						{
							sum++;
						}
					}
					return sum;
				}
			}.start();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		initData2();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.btn_practice:
			practice();
			break;
		case R.id.btn_refresh:
			refresh();
			break;
		}
	}
	private void refresh()
	{
		System.out.println("试卷添加时间："+paper.getAddDate());
		if(proDialog!=null)
		{
			proDialog.show();
		}
		if(paper == null)
		{
			NewDataToast.makeText(this, "没有更新", false).show();
			return;//还没有练习过，已经是最新
		}
		new Thread(){
			public void run() {
				try {
					//先不做更新
					Thread.sleep(2000); handler.sendEmptyMessage(-8);
					/*
					//加一个时间的参数
					ArrayList<ExamQuestion> list = XMLParseUtil.parseQuestionList(ApiClient
							.getChapterQuestionList(appContext, chapterid,paper.getAddDate(),AreaUtils.areaCode));
					if (list == null || list.size() == 0) {
						handler.sendEmptyMessage(-8);
					} else {
						refreshRule(list);
						dao.insertQuestions(list);
						paper.setPaperSorce(paper.getPaperSorce()+list.size());
						dao.updatePraticePaper(paper);
						dao.insertRules(ruleList,zuheid);
						handler.sendEmptyMessage(8);
					}*/
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-4);
				}
			};
		}.start();
	}
	private void practice() {
		if (paper == null || paper.getPaperSorce() == 0) {
			Toast.makeText(this, "暂无试题", Toast.LENGTH_SHORT).show();
			return;
		}
		// 获取题目数据
		if(proDialog!=null)
		{
			proDialog.show();
		}
		if(questionList!=null)
		{
			handler.sendEmptyMessage(4);
			return;
		}
		new Thread() {
			public void run() {
				questionList = dao.findQuestionByPaperId(zuheid);
				if (questionList == null || questionList.size() == 0) {
					try {
						questionList = XMLParseUtil.parseQuestionList(ApiClient
								.getChapterQuestionList(appContext, chapterid,"",AreaUtils.areaCode));
						if (questionList == null || questionList.size() == 0) {
							handler.sendEmptyMessage(0);
						} else {
							ruleList = getRuleList(questionList);
							dao.insertQuestions(questionList);
							dao.insertRules(ruleList,zuheid);
							handler.sendEmptyMessage(4);
						}
					} catch (Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(-4);
					}
				} else {
					ruleList = dao.findRules(zuheid);
					handler.sendEmptyMessage(4);
				}
			};
		}.start();
	}

	private List<ExamRule> getRuleList(List<ExamQuestion> questionList) {
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
				r.setRuleId(zuheid + "_" + i);
				r.setRuleTitle(getTitleStr(i));
				r.setPaperId(zuheid);
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
	//更新ruleList
	private void refreshRule(List<ExamQuestion> list)
	{
		if(ruleList==null)
		{
			ruleList = new ArrayList<ExamRule>();
		}
		LinkedList<ExamQuestion> temp = null;
		if(questionList != null )
		{
			temp = new LinkedList<ExamQuestion>(questionList);
			temp.addAll(list);
		}else
		{
			temp = new LinkedList<ExamQuestion>(list);
		}
		Collections.sort(temp);
		if(questionList!=null)
			questionList.clear();
		else{
			questionList = new ArrayList<ExamQuestion>();
			questionList.addAll(temp);
		}
		StringBuffer buf = new StringBuffer();
		//按题型的1,2,3,4来分
		for (int i = 1; i < 5; i++) {
			for (ExamQuestion q : questionList) {

				if (String.valueOf(i).equals(q.getQType())) {
					buf.append(q.getQid()).append(",");
				}
			}
			if(buf.length() > 0){
				ExamRule r = null;
				if(ruleList.size()<i)
				{
					r = new ExamRule();
					r.setRuleId(zuheid + "_" + i);
					r.setRuleTitle(getTitleStr(i));
					r.setPaperId(zuheid);
					r.setOrderInPaper(i);
				}
				else
					r = ruleList.get(i-1);
				if (buf.length() > 0) {
					if(StringUtils.isEmpty(r.getContainQids()))
					{
						r.setContainQids(buf.substring(0, buf.lastIndexOf(",")));
					}else
					{
						r.setContainQids(r.getContainQids()+","+buf.substring(0, buf.lastIndexOf(",")));
					}
					buf.delete(0, buf.length());
				}
				if(ruleList.size()<i)
					ruleList.add(r);
			}
		}
		temp.clear();
	}
	private String getTitleStr(int i) {
		return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
				: i == 4 ? "判断题" : "综合题";
	}

	static class MyHandler extends Handler {
		private WeakReference<QuestionPraticeInfoActivity> weak;

		public MyHandler(QuestionPraticeInfoActivity activity) {
			// TODO Auto-generated constructor stub
			weak = new WeakReference<QuestionPraticeInfoActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			QuestionPraticeInfoActivity qpi = weak.get();
			if (qpi.proDialog != null) {
				qpi.proDialog.dismiss();
			}
			switch (msg.what) {
			case 1:
				qpi.questionSumTv.setText(qpi.paper.getPaperSorce() + "");
				qpi.errorSizeTv.setText(qpi.errorNum + "");
				if (qpi.record != null && qpi.record.getTempAnswer() != null
						&& !"".equals(qpi.record.getTempAnswer())) {
					int num = qpi.record.getTempAnswer().split("&").length;
					int accuracy = (num - qpi.errorNum) * 10000 / num;
					qpi.accuracyTv.setText(accuracy / 100.0 + "");
					qpi.answeredSizeTv.setText(qpi.record.getTempAnswer()
							.split("&").length + "");
				} else {
					qpi.accuracyTv.setText("0.00");
					qpi.answeredSizeTv.setText("0");
				}
				if (qpi.kList != null && qpi.kList.size() > 0) {
					qpi.knowledgeLv.setAdapter(new KnowlegdgeListAdapter2(qpi,
							qpi.kList));
				}else
				{
					qpi.knowledgeLv.setAdapter(new KnowlegdgeListAdapter(qpi,
							qpi.kList));
				}
				break;
			case -1:
				Toast.makeText(qpi, "亲,网络不给力吖!", Toast.LENGTH_SHORT).show();
				qpi.knowledgeLv.setAdapter(new KnowlegdgeListAdapter(qpi,
						qpi.kList));
				break;
			case -2:
				Toast.makeText(qpi, "获取数据出问题", Toast.LENGTH_SHORT).show();
				qpi.knowledgeLv.setAdapter(new KnowlegdgeListAdapter(qpi,
						qpi.kList));
				break;
			case 4:
				Intent intent = new Intent(qpi, QuestionDoExamActivity1.class);
				intent.putExtra("paperId",qpi.zuheid);
				intent.putExtra("action", "practice");
				intent.putExtra("ruleListJson", qpi.gson.toJson(qpi.ruleList));
				intent.putExtra("cursor", qpi.questionCursor);
				qpi.startActivity(intent);
				break;
			case 0:
				Toast.makeText(qpi, "暂无题目数据", Toast.LENGTH_SHORT).show();
				break;
			case -4:
				Toast.makeText(qpi, "获取数据出问题", Toast.LENGTH_SHORT).show();
				break;
			case -8:
				NewDataToast.makeText(qpi, "没有更新", false).show();
				break;
			case 8:
				NewDataToast.makeText(qpi, "更新成功", false).show();
				qpi.questionSumTv.setText(qpi.paper.getPaperSorce() + "");
				break;
			}
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(kList!=null)
			kList.clear();
		if(questionList!=null)
			questionList.clear();
		if(ruleList!=null)
			ruleList.clear();
		super.onDestroy();
	}
}
