package com.changheng.accountant.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.PaperListAdapter2;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.XMLParseUtil;
import com.google.gson.Gson;

public class QuestionRecordActivity extends BaseActivity {
	private LinearLayout contentLayout, nodataLayout, loadingLayout;
	private ListView paperListView;
	private String username;
	private List<ExamRecord> recordList;
	private PaperDao dao;
	private PaperListAdapter2 mAdapter;
	private Handler handler;
	private ProgressDialog dialog;
	private List<ExamQuestion> questionList;
	private List<ExamRule> ruleList;
	private AppContext appContext;
	private SparseBooleanArray isDone = new SparseBooleanArray();
	private int[] tOrF;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_questionblank_examtier2);
		findViews();
		dao = new PaperDao(this);
		appContext = (AppContext) getApplication();
		
		appContext.recoverLoginStatus();
		
		username = appContext.getUsername();
		handler = new MyHandler(this);
		initData();
		this.paperListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				ExamRecord r = recordList.get(arg2);
				loadingLayout.setVisibility(View.VISIBLE);
				itemClickMethod(r);
			}
		});
		this.paperListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						// TODO Auto-generated method stub
						showDeleteWindow(arg2);
						return true;
					}
				});
	}

	private void findViews() {
		this.nodataLayout = (LinearLayout) this
				.findViewById(R.id.noneDataLayout);
		this.contentLayout = (LinearLayout) this
				.findViewById(R.id.questionContentLayout);
		this.loadingLayout = (LinearLayout) this
				.findViewById(R.id.loadingLayout);
		this.paperListView = (ListView) this.findViewById(R.id.contentListView);
		this.findViewById(R.id.btn_goback).setOnClickListener(new ReturnBtnClickListener(this));
		((TextView) (findViewById(R.id.title))).setText("学习记录");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		this.loadingLayout.setVisibility(View.GONE);
		super.onStart();
	}

	private void initData() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case 1:
					if (recordList == null || recordList.size() == 0) {
						contentLayout.setVisibility(View.GONE);
						nodataLayout.setVisibility(View.VISIBLE);
					} else {
						mAdapter = new PaperListAdapter2(
								QuestionRecordActivity.this, recordList);
						paperListView.setAdapter(mAdapter);
					}
					break;
				case -1:
					loadingLayout.setVisibility(View.GONE);
					Toast.makeText(QuestionRecordActivity.this, "加载错误",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
		new Thread() {
			public void run() {
				try {
					recordList = dao.findRecordsByUsername(username);
					handler.sendEmptyMessage(1);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
		}.start();
	}

	private void showDeleteWindow(final int index) {
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setTitle("删除").setMessage("是否删除此记录").setCancelable(false)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO Auto-generated method stub
						// 停止下载服务
						dao.deleteRecord(recordList.get(index));
						recordList.remove(index);
						if (recordList.size() == 0) {
							contentLayout.setVisibility(View.GONE);
							nodataLayout.setVisibility(View.VISIBLE);
						} else
							mAdapter.notifyDataSetChanged();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
		localBuilder.create().show();
	}

	private void itemClickMethod(final ExamRecord r) {
		// 章节练习的记录
		if (r.getMode() == 1) {
			Intent intent = new Intent(this, QuestionPraticeInfoActivity.class);
			String[] arr = r.getPaperId().split("_");
			intent.putExtra("chapterId", arr[1]);
			intent.putExtra("classId", arr[0]);
			intent.putExtra("chapterName", r.getPapername());
			this.startActivity(intent);
			return;
		}
		// 没有交卷的
		if (dialog == null) {
			dialog = ProgressDialog.show(this, null, "加载中请稍候", true, true);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		dialog.show();
		final String paperid = r.getPaperId();
		new Thread() {
			public void run() {
				ruleList = dao.findRules(paperid);
				if (ruleList != null && ruleList.size() > 0) {
					questionList = dao.findQuestionByPaperId(paperid);
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
					addAnswer(isDone,questionList,r.getAnswers());
					Message msg = handler.obtainMessage();
					msg.what = 1;
					msg.obj = r;
					handler.sendMessage(msg);
				} else {
					try {
						Paper parsePaper = XMLParseUtil.parsePaper(ApiClient
								.getPaperDetail(appContext, paperid));
						questionList = XMLParseUtil.parseQuestionList(ApiClient
								.getQuestionList(appContext, paperid));
						ruleList = parsePaper.getRuleList();
						dao.insertRules(ruleList, paperid);
						dao.insertQuestions(questionList);
						tOrF = new int[questionList.size()];
						Message msg = handler.obtainMessage();
						msg.what = 1;
						msg.obj = r;
						handler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(-1);
					}
				}
			};
		}.start();
	}

	private void addAnswer(SparseBooleanArray isDone, List<ExamQuestion> list,
			String tempAnswer) {
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
	private void startMyActivity(ExamRecord r)
	{
		Gson gson = new Gson();
		Intent mIntent = null;
		String ruleListJson = gson.toJson(ruleList);
		if (r.getAnswers() == null) {
			mIntent = new Intent(this, QuestionDoExamActivity2.class);
			mIntent.putExtra("action", "DoExam");
			mIntent.putExtra("ruleListJson", ruleListJson);
			mIntent.putExtra("paperName", r.getPapername());
			mIntent.putExtra("paperId", r.getPaperId());
			mIntent.putExtra("username", username);
			mIntent.putExtra("tempTime", r.getPapertime() * 60);
			mIntent.putExtra("paperTime", r.getPapertime());
			mIntent.putExtra("paperScore", r.getPaperscore());
//			mIntent.putExtra("questionListJson", gson.toJson(questionList));
//			mIntent.putExtra("tOrF", gson.toJson(tOrF));
			this.startActivity(mIntent);
		} else {
//			SparseBooleanArray isDone = new SparseBooleanArray();
//			addAnswer(isDone, questionList, r.getAnswers());
			mIntent = new Intent(this, QuestionChooseActivity.class);
			mIntent.putExtra("action", "showResult");
			mIntent.putExtra("ruleListJson", ruleListJson);
//			mIntent.putExtra("questionList", gson.toJson(questionList));
			mIntent.putExtra("tOrF", gson.toJson(tOrF));
			mIntent.putExtra("paperScore", r.getPaperscore());
			mIntent.putExtra("paperTime", r.getPapertime());
			mIntent.putExtra("username", username);
			mIntent.putExtra("paperid", r.getPaperId());
			mIntent.putExtra("useTime", r.getUseTime());
			mIntent.putExtra("record", gson.toJson(r));
			mIntent.putExtra("isDone", gson.toJson(isDone));
			mIntent.putExtra("userScore", r.getScore()); // 本次得分
			this.startActivity(mIntent); // 仍然是要启动这个Activity不带结果返回
		}
	}
	static class MyHandler extends Handler {
		WeakReference<QuestionRecordActivity> mActivity;

		MyHandler(QuestionRecordActivity activity) {
			mActivity = new WeakReference<QuestionRecordActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			QuestionRecordActivity theActivity = mActivity.get();
			switch (msg.what) {
			case 1:
				theActivity.dialog.dismiss();
				if (theActivity.ruleList.size() == 0) {
					Toast.makeText(theActivity, "暂时没有数据", Toast.LENGTH_SHORT)
							.show();
				}else
				{
					theActivity.startMyActivity((ExamRecord)msg.obj);
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
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(questionList!=null)
			questionList.clear();
		if(ruleList!=null)
			ruleList.clear();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(dialog!=null) dialog.dismiss();
		super.onDestroy();
	}
}
