package com.changheng.accountant.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.XMLParseUtil;
import com.google.gson.Gson;

public class QuestionPaperInfoActivity extends BaseActivity implements
		OnClickListener {
	private LinearLayout ruleInfo;
	private TextView totalNum, ruleSize, paperScore, paperTime;
	private List<ExamRule> ruleList;
	private Button startBtn;
	private Button restarBtn;
	private Paper paper;
	private String paperid;
	private PaperDao dao;
	private Gson gson;
	private ProgressDialog dialog;
	private Handler handler;
	private String username;
	private ExamRecord record;
	private int tempTime;
	private List<ExamQuestion> questionList;
	private AppContext appContext;
	private SparseBooleanArray isDone = new SparseBooleanArray();
	private int[] tOrF;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_paperinfo);
		initViews();
		initViewData();
		// new GetQuestionListThread().start();
	}

	private void initViews() {
		this.ruleSize = (TextView) this.findViewById(R.id.rulesize);
		this.totalNum = (TextView) this.findViewById(R.id.questionNumTotal);
		this.paperScore = (TextView) this.findViewById(R.id.paperscore);
		this.paperTime = (TextView) this.findViewById(R.id.papertime);
		this.ruleInfo = (LinearLayout) this.findViewById(R.id.ruleInfoLayout);
		this.startBtn = (Button) this.findViewById(R.id.btn_pratice);
		this.restarBtn = (Button) this.findViewById(R.id.btn_restart);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.startBtn.setOnClickListener(this);
		this.restarBtn.setOnClickListener(this);
		((TextView) findViewById(R.id.title)).setText("试卷详情");
	}

	private void initViewData() {
		Intent intent = this.getIntent();
		gson = new Gson();
		paper = gson.fromJson(intent.getStringExtra("paperJson"), Paper.class);
		paperid = paper.getPaperId();
		((TextView) this.findViewById(R.id.papertitle)).setText(paper.getPaperName());
		this.paperScore.setText(paper.getPaperSorce() + "");
		this.paperTime.setText(paper.getPaperTime() + "");
		if (dao == null)
			dao = new PaperDao(this);
		appContext = (AppContext) getApplication();
		
		////恢复登录的状态，
		appContext.recoverLoginStatus();
		
		username = appContext.getUsername();
		dialog = ProgressDialog.show(QuestionPaperInfoActivity.this, null,
				"加载中请稍候...", true, true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		handler = new MyHandler(this);
		// 开线程 findRuleList
		new Thread() {
			public void run() {
				ruleList = dao.findRules(paperid);
				if (ruleList != null && ruleList.size() > 0) {
					//
					questionList = dao.findQuestionByPaperId(paperid);
					record = dao.findRecord(username, paperid);
					if (questionList == null || questionList.size() == 0) {
						try {
							questionList = XMLParseUtil
									.parseQuestionList(ApiClient
											.getQuestionList(appContext,
													paperid));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (AppException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Message msg = handler.obtainMessage();
							msg.what = -2;
							msg.obj = e;
							handler.sendMessage(msg);
						}
					}
					tOrF = new int[questionList.size()];
					if(record!=null)
						addAnswer(isDone,questionList,record.getAnswers());
					handler.sendEmptyMessage(1);
				} else {
					try {
						Paper parsePaper = XMLParseUtil.parsePaper(ApiClient
								.getPaperDetail(appContext, paperid));
						questionList = XMLParseUtil.parseQuestionList(ApiClient
								.getQuestionList(appContext, paperid));
						ruleList = parsePaper.getRuleList();
						dao.insertRules(ruleList,paperid);
						dao.insertQuestions(questionList);
						tOrF = new int[questionList.size()];
						handler.sendEmptyMessage(1);
					} catch (Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(-1);
					}
				}
			};
		}.start();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_goback:
			this.finish();
			return;
		case R.id.btn_pratice:
			gotoDoExamActivity();
			return;
		case R.id.btn_restart:
			gotoChooseActivity();
			return;
		}
	}
	private void gotoChooseActivity()
	{
		Intent mIntent = new Intent(this,QuestionDoExamActivity2.class);
		mIntent.putExtra("action", "DoExam");
		mIntent.putExtra("paperName", paper.getPaperName());
		mIntent.putExtra("paperId", record.getPaperId());
		mIntent.putExtra("ruleListJson",gson.toJson(ruleList));
		mIntent.putExtra("username", username);
		mIntent.putExtra("tempTime", 0);
		mIntent.putExtra("paperTime", record.getPapertime());
		mIntent.putExtra("paperScore", record.getPaperscore());
		record.setTempAnswer("");
		record.setIsDone(null);
		record.setTempTime(record.getPapertime()*60);
		dao.saveOrUpdateRecord(record);
		mIntent.putExtra("tOrF", gson.toJson(tOrF));
//		mIntent.putExtra("questionListJson", gson.toJson(questionList));
		this.startActivity(mIntent);
		this.finish();
	}
	private void gotoDoExamActivity() {
		if (questionList == null || questionList.size() == 0) {
			Toast.makeText(this, "没有题目数据暂时不能练习", Toast.LENGTH_SHORT).show();
			return;
		}
		if (dialog != null) {
			dialog.show();
		}
		Intent intent = null;
		if (record != null && record.getAnswers() != null) {
//			SparseBooleanArray isDone = new SparseBooleanArray();
//			addAnswer(isDone,questionList,record.getAnswers());
			intent = new Intent(this,QuestionChooseActivity.class);
			intent.putExtra("action", "showResult");
			intent.putExtra("ruleListJson", gson.toJson(ruleList));
			intent.putExtra("tOrF", gson.toJson(tOrF));
			intent.putExtra("paperScore", record.getPaperscore());
			intent.putExtra("paperTime", record.getPapertime());
			intent.putExtra("username", username);
			intent.putExtra("paperid", record.getPaperId());
			intent.putExtra("useTime", record.getUseTime());
			intent.putExtra("record", gson.toJson(record));
			intent.putExtra("isDone", gson.toJson(isDone));
			intent.putExtra("userScore", record.getScore());  // 本次得分
		}else
		{
			intent = new Intent(this, QuestionDoExamActivity2.class);
			intent.putExtra("paperName", paper.getPaperName());
			intent.putExtra("paperId", paper.getPaperId());
			intent.putExtra("paperTime", paper.getPaperTime());
			intent.putExtra("tempTime", tempTime);
			intent.putExtra("paperScore", paper.getPaperSorce());
			intent.putExtra("action", "DoExam");
			Gson gson = new Gson();
			intent.putExtra("ruleListJson", gson.toJson(ruleList));
//			intent.putExtra("questionListJson", gson.toJson(questionList));
			intent.putExtra("tOrF", gson.toJson(tOrF));
			intent.putExtra("username", username);
		}
		this.startActivity(intent);
		this.finish(); // 结束生命
	}

	// private class GetQuestionListThread extends Thread
	// {
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// if("local".equals(loginType))
	// {
	// paper = new Gson().fromJson(paperJson, Paper.class);
	// ruleList = dao.findRules(paper.getPaperId());
	// questionList = dao.findQuestionByPaperId(paper.getPaperId());
	//
	// }
	// try{
	// String result =
	// HttpConnectUtil.httpGetRequest(QuestionPaperInfoActivity.this,
	// "http://192.168.1.246:8080/mobile/questionListofPaper?paperid=1001");
	// //解析result
	// if(result!=null&&!result.equals("null"))
	// {
	// //解析json字符串,配置expandableListView的adapter
	// try
	// {
	// JSONArray json = new JSONArray(result);
	// int length = json.length();
	// if(length>0)
	// {
	// questionList = new ArrayList<ExamQuestion>();
	// for(int i=0;i<length;i++)
	// {
	// JSONObject obj = json.getJSONObject(i);
	// /*
	// * String qid,String ruleid, String paperId, String content,
	// String answer, String analysis, String linkQid,
	// int qType, int optionNum, int orderId
	// */
	// ExamQuestion q = new
	// ExamQuestion(obj.optInt("questId")+"",obj.optInt("questRuleId")+"",obj.optInt("questPaperId")+"",obj.optString("questContent"),
	// obj.optString("questAnswer"),obj.optString("questAnalysis"),obj.optString("questLinkQuestionId"),
	// obj.optString("type"),obj.optInt("questOptionNum"),obj.optInt("questOrderId"));
	// questionList.add(q);
	// }
	// }
	// dao.insertQuestions(questionList);
	// Message msg = handler.obtainMessage();
	// msg.what = 1;
	// handler.sendMessage(msg);
	// }catch(Exception e)
	// {
	// e.printStackTrace();
	// handler.sendEmptyMessage(-2);
	// }
	// }else
	// {
	// Message msg = handler.obtainMessage();
	// msg.what = -2;
	// handler.sendMessage(msg);
	// }
	// }catch(Exception e)
	// {
	// Message msg = handler.obtainMessage();
	// msg.what = -1;
	// handler.sendMessage(msg);
	// }
	// }
	// }
	static class MyHandler extends Handler {
		WeakReference<QuestionPaperInfoActivity> mActivity;

		MyHandler(QuestionPaperInfoActivity activity) {
			mActivity = new WeakReference<QuestionPaperInfoActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			QuestionPaperInfoActivity theActivity = mActivity.get();
			switch (msg.what) {
			case 1:
				theActivity.dialog.dismiss();
				if (theActivity.ruleList.size() == 0) {
					Toast.makeText(theActivity, "暂时没有数据", Toast.LENGTH_SHORT)
							.show();
				} else {
					theActivity.initTextView();
				}
				break;
			case -2:
				theActivity.dialog.dismiss();
				Toast.makeText(theActivity,
						((AppException) msg.obj).getMessage(),
						Toast.LENGTH_SHORT).show();
				break;
			case -1:
				// 连不上,
				theActivity.dialog.dismiss();
				Toast.makeText(theActivity, "连不上服务器", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}

	private void initTextView() {
		// TODO Auto-generated method stub
		int length = ruleList.size();
		this.ruleSize.setText(length + "");
		int total_n = 0;
		for (int i = 0; i < length; i++) {
			ExamRule r = ruleList.get(i);
			total_n += r.getQuestionNum();
			View v = LayoutInflater.from(this).inflate(R.layout.list_ruleinfo,
					null);
			TextView ruleTitle = (TextView) v.findViewById(R.id.ruleTitle);
			ruleTitle.setText("第" + (i + 1) + "大题" + r.getRuleTitle());
			TextView ruleTitleInfo = (TextView) v
					.findViewById(R.id.ruleTitleInfo);
			ruleTitleInfo.setText("说明:" + r.getFullTitle());
			this.ruleInfo.addView(v, i);
		}
		if (record != null && record.getTempAnswer() != null
				&& !"".equals(record.getTempAnswer())) {
			this.startBtn.setText("继续考试");
			this.tempTime = record.getTempTime();
		} else if (record != null && record.getAnswers() != null) {
			this.startBtn.setText("查看成绩");
		} else {
			this.startBtn.setText("开始考试");
			this.restarBtn.setVisibility(View.GONE);
		}
		this.totalNum.setText(total_n + "");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (dialog != null) {
			dialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// MobclickAgent.onPause(this);
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// MobclickAgent.onResume(this);

	}
	private void addAnswer(SparseBooleanArray isDone,List<ExamQuestion> list,String tempAnswer)
	{
		if (tempAnswer == null || "".equals(tempAnswer.trim())) {
			return;
		}
		if(tempAnswer.lastIndexOf("&")!=tempAnswer.length()-1)
		{
			tempAnswer = tempAnswer + "&";
		}
		int listSize = list.size();
		String choiceAnswer = null, textAnswer = null;
		if (tempAnswer.indexOf("   ") != -1) {
			choiceAnswer = tempAnswer.substring(0, tempAnswer.indexOf("   "));
			textAnswer = tempAnswer.substring(tempAnswer.indexOf("   ") + 3);
		} else {
			choiceAnswer = tempAnswer;
		}
		for (int i = 0; i < listSize; i++) {
			ExamQuestion q = list.get(i);
			String str = q.getQid() + "-";
			if ((!"问答题".equals(q.getQType()))
					&& choiceAnswer.indexOf(str) != -1) {
				String temp = choiceAnswer.substring(choiceAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(), temp.indexOf("&")));
				isDone.append(i, true);
			} else if (textAnswer != null && "问答题".equals(q.getQType())
					&& textAnswer.indexOf(str) != -1) {
				String temp = textAnswer.substring(textAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(),
						temp.indexOf("   ")));
				isDone.append(i, true);
			}
			tOrF[i] = q.getUserAnswer() == null ? 0 : q.getAnswer().equals(
					q.getUserAnswer()) ? 1 : -1;
		}
	}
}