package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.taptwo.android.widget.ViewFlow;
import org.taptwo.android.widget.ViewFlow.ViewSwitchListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.PopRuleListAdapter;
import com.changheng.accountant.adapter.QuestionAdapter;
import com.changheng.accountant.adapter.QuestionAdapter.AnswerViewHolder;
import com.changheng.accountant.adapter.QuestionAdapter.ContentViewHolder;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.ExamErrorQuestion;
import com.changheng.accountant.entity.ExamFavor;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.util.StringUtils;
import com.changheng.accountant.view.AnswerSettingLayout;
import com.changheng.accountant.view.AnswerSettingLayout.FontSizeChangeListerner;
import com.changheng.accountant.view.AnswerSettingLayout.ItemChangeListerner;
import com.changheng.accountant.view.AnswerSettingLayout.LightChangeListerner;
import com.changheng.accountant.view.QuestionMaterialLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 章节练习的做题界面
 * 
 * @author Administrator
 * 
 */
public class QuestionDoExamActivity1 extends BaseActivity implements
		OnClickListener {
	// 组件
	private ImageButton favoriteBtn,answerBtn;
	private TextView examTypeTextView;
	private LinearLayout nodataLayout, loadingLayout;
	private RelativeLayout ruleTitleLayout;
	// private EditText answerEditText;
//	private Handler timeHandler;
	// 数据
	private String username;
	private String paperid;
	private String action;
	private String ruleListJson;
	private StringBuilder favorQids;
	private int paperScore;
	private ArrayList<ExamRule> ruleList;
	private ArrayList<ExamQuestion> questionList;
	private ExamQuestion currentQuestion;
	private Integer questionCursor;
	private int initCursor;
	private ExamRule currentRule;
	private StringBuffer answerBuf, txtAnswerBuf;
	private ExamRecord record;
	private SparseBooleanArray isDone;
	private Gson gson;
//	private static boolean timerFlag = true;
	private ExamFavor favor;
	// 选择弹出框
	private PopupWindow popupWindow;
	private ListView lv_group;
	private AlertDialog exitDialog;
	// 更多
	private PopupWindow menuPop;
	// 提示界面
	private PopupWindow tipWindow;
	private Handler mHandler;
	private SharedPreferences guidefile;
	// 数据库操作
	private PaperDao dao;

	private ViewFlow viewFlow;
	private QuestionAdapter questionAdapter;
	// private String currentAnswer; // 当前题目的答案
	// private static final String TEMPNAME = "tempName";
	private int[] tOrF;
	private MyHandler handler;

	private StringBuilder errorBuf;
	private ProgressDialog proDialog;
	private PopupWindow pop;
	private Vibrator vibrator;
	private SharedPreferences preferences;

	private QuestionMaterialLayout material;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_question_doexam1);
		preferences = this.getSharedPreferences("wdkaoshi", 0);
		if (preferences.getInt("isFullScreen", 0) == 1) {
			setFullScreen();
		}
		initView();
		initData();
		// 数据初始化
	}

	private static boolean isfull = false;

	// 全屏设置和退出全屏
	private void setFullScreen() {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		isfull = true;
	}

	private void quitFullScreen() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(attrs);
		getWindow()
				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		isfull = false;
	}

	public void changescreen() {
		if (isfull == true) {
			quitFullScreen();
		} else {
			setFullScreen();
		}
	}

	// 取得主界面的组件,只取得不操作
	private void initView() {
		this.favoriteBtn = (ImageButton) this.findViewById(R.id.favoriteBtn);
		this.answerBtn = (ImageButton) this.findViewById(R.id.answerBtn);
		this.examTypeTextView = (TextView) this
				.findViewById(R.id.examTypeTextView);// 大题标题
		this.ruleTitleLayout = (RelativeLayout) this
				.findViewById(R.id.ruleTitleLayout);
		this.viewFlow = (ViewFlow) this.findViewById(R.id.viewflow);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.nodataLayout.setVisibility(8);
		this.loadingLayout = (LinearLayout) this
				.findViewById(R.id.loadingLayout);
		this.loadingLayout.setVisibility(View.GONE);

		this.findViewById(R.id.previousBtn).setOnClickListener(this);//上一题
		this.findViewById(R.id.nextBtn).setOnClickListener(this);//下一题
		this.findViewById(R.id.removeBtn).setOnClickListener(this);	//移除
		this.findViewById(R.id.btn_goback).setOnClickListener(this);//返回按钮
		this.findViewById(R.id.btn_more).setOnClickListener(this);//小菜单
		this.answerBtn.setOnClickListener(this);// 交卷或者查看答案
		this.favoriteBtn.setOnClickListener(this);//收藏
		this.material = (QuestionMaterialLayout) this.findViewById(R.id.questionMaterial); //案例材料的位置
		this.ruleTitleLayout.setOnClickListener(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE); // 实例化震动
		// 切题
		viewFlow.setOnViewSwitchListener(new ViewSwitchListener() {
			@Override
			public void onSwitched(View view, int position) {
				// TODO Auto-generated method stub
				System.out.println("执行onSwitch方法");
				questionCursor = position;
				currentQuestion = questionList.get(position);
				System.out.println(currentQuestion.getContent().substring(0,10));
				if(!StringUtils.isEmpty(currentQuestion.getMaterial()))
				{
					material.setVisibility(View.VISIBLE);
					material.initData(currentQuestion.getMaterial());
				}else
				{
					material.setVisibility(View.GONE);
				}
				if (ruleList != null && ruleList.size() > 0) {
					ExamRule currentRule = getCurrentRule(currentQuestion);
					examTypeTextView.setText(currentRule.getRuleTitle());
				}
				if (favorQids != null
						&& favorQids.indexOf(currentQuestion.getQid()) != -1) {
					favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
				} else {
					favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
				}
				System.out.println("action = "+action + "practice".equals(action));
				if ("practice".equals(action)) {
					// 有答案了,禁止选择,没答案继续选择
					System.out.println(currentQuestion.getUserAnswer() == null);
					System.out.println("aaa"+ currentQuestion.getUserAnswer()+"bbb");
					if (currentQuestion.getUserAnswer() != null) {
						((QuestionAdapter.ContentViewHolder) view
								.getTag(R.id.tag_first)).examOption
								.forbidden(false);
						// questionAdapter.setRadioEnable(((QuestionAdapter.ContentViewHolder)view.getTag(R.id.tag_first)).examOption,
						// false);
					} else {
						((QuestionAdapter.ContentViewHolder) view
								.getTag(R.id.tag_first)).examOption
								.forbidden(true);
						// questionAdapter.setRadioEnable(((QuestionAdapter.ContentViewHolder)view.getTag(R.id.tag_first)).examOption,
						// true);
					}
				}
				if (favor.getQid() != null) {
					// 收藏一个
					System.out.println("收藏或者取消了收藏");
					new Thread() {
						public void run() {
							if (favor.isNeedDelete()) {
								dao.deleteFavor(favor);
							} else
								dao.insertFavor(favor);
							favor.setQid(null);
						};
					}.start();
				}
			}

			private ExamRule getCurrentRule(ExamQuestion currentQuestion) {
				// TODO Auto-generated method stub
				ExamRule rule = null;
				for (ExamRule r : ruleList) {
					if (r.getContainQids2().contains(
							"," + currentQuestion.getQid() + ",")) {
						rule = r;
						break;
					}
				}
				return rule;
			}
		});
	}

	private void initData() {
		// 数据初始化
		errorBuf = new StringBuilder();
		Intent intent = getIntent();
		guidefile = this.getSharedPreferences("guidefile", 0);
		action = intent.getStringExtra("action");
		initCursor = intent.getIntExtra("cursor", 0);
		if ("DoExam".equals(action))
			answerBtn.setImageResource(R.drawable.exam_submit_img);
		else
			answerBtn.setImageResource(R.drawable.exam_answer_img);
		// timeHandler = new TimerHandler(this);
		mHandler = new Handler();
		handler = new MyHandler(this);
		////恢复登录的状态，
		((AppContext) getApplication()).recoverLoginStatus();
		proDialog = ProgressDialog.show(this, null, "加载中请稍候...", true, true);
		new Thread() {
			public void run() {
				try{
				Intent intent = getIntent();
				paperid = intent.getStringExtra("paperId");
//				papername = intent.getStringExtra("paperName");
				ruleListJson = intent.getStringExtra("ruleListJson");
				username = ((AppContext) getApplication()).getUsername();
//				paperTime = intent.getIntExtra("tempTime", 0);
//				time = intent.getIntExtra("paperTime", 0) * 60; // 秒
//				if (paperTime == 0) {
//					paperTime = time;
//				}
				paperScore = intent.getIntExtra("paperScore", 0);
				action = intent.getStringExtra("action");
				// /mnt/sdcard/CHAccountant/hahaha/image/question
				gson = new Gson();
				Type ruleType = new TypeToken<ArrayList<ExamRule>>() {
				}.getType();
				ruleList = gson.fromJson(ruleListJson, ruleType);
				String knowledgeId = intent.getStringExtra("knowledgeId");
				if (dao == null)
					dao = new PaperDao(QuestionDoExamActivity1.this);
				if(paperid!=null)
				{
					questionList = (ArrayList<ExamQuestion>) dao.findQuestionByPaperId(paperid);
				}
				else
				{
					questionList = (ArrayList<ExamQuestion>) dao.findQuestionByKnowledgeId(knowledgeId);
					paperid = "k_"+knowledgeId;
					username = "中文";
				}
				if (favor == null)
					favor = new ExamFavor(username, paperid);
				favorQids = dao.findFavorQids(username, paperid);
				// 根据action的不同,区分
				if ("DoExam".equals(action) || "practice".equals(action)) {
					record = dao.insertRecord(
							new ExamRecord(paperid, username),
							"practice".equals(action) ? 1 : 0);
					isDone = record.getIsDone() == null ? new SparseBooleanArray()
							: gson.fromJson(record.getIsDone(),
									SparseBooleanArray.class);
					// /////
					if ("practice".equals(action)) {
						tOrF = record.getAnswers() == null ? new int[questionList
								.size()] : gson.fromJson(record.getAnswers(),
								int[].class);
					}
					// ////
					String tempAnswer = record.getTempAnswer();
					if (tempAnswer == null) {
						answerBuf = new StringBuffer();
						txtAnswerBuf = new StringBuffer();
					} else if (tempAnswer.indexOf("   ") == -1) {
						answerBuf = new StringBuffer(tempAnswer);
						txtAnswerBuf = new StringBuffer();
					} else {
						answerBuf = new StringBuffer(tempAnswer.substring(0,
								tempAnswer.indexOf("   ")));
						txtAnswerBuf = new StringBuffer(
								tempAnswer.substring(tempAnswer.indexOf("   ") + 3));
					}
					initQuestionAnswer(tempAnswer);
					// examTitle.setText(this.papername); // 试卷名字
				}
				handler.sendEmptyMessage((questionList!=null&&questionList.size()>0)?1:-1);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
		}.start();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//在没题的情况屏蔽按钮
		if(questionList==null||questionList.size()==0)
		{
			if(v.getId()==R.id.btn_goback)
			{
				this.finish();
			}
			return;
		}
		switch (v.getId()) {
		case R.id.previousBtn:
			preQuestion();
			break;
		case R.id.nextBtn:
			nextQuestion();
			break;
		case R.id.favoriteBtn:
			favorQuestion();
			break;
		// case R.id.exitExamImgBtn:
		// if ("DoExam".equals(action)) {
		// showDialog();
		// } else {
		// this.finish();
		// }
		// break;
		case R.id.btn_goback:
			uploadErrorQuestions();
			this.finish();
			break;
		case R.id.exitExamBtn:
//			exitExam();
			break;
		case R.id.exitCancelExamBtn:
			this.exitDialog.dismiss();
			break;
		case R.id.exitSubmitExamBtn:
//			submitExam();
			break;
		case R.id.ruleTypeLayout:
			if (ruleList != null && ruleList.size() > 0) {
				showWindow(v);
			}
			break;
		case R.id.ruleTitleLayout:
			showWindow(v);
			break;
		case R.id.btn_more:
			showPopWindow(v);
			break;
		case R.id.notebook_ImgBtn:
			showNoteBookActivity();
			break;
		case R.id.selectTopicId_ImgBtn:
			gotoChooseActivity();
			break;
		case R.id.answerBtn:
			submitOrSeeAnswer();
			break;
		case R.id.removeBtn:
			removeFromErrors();
			break;
		}
	}

	private void uploadErrorQuestions() {
		if (errorBuf.length() > 0) {
			final String error = errorBuf.substring(0, errorBuf.length() - 1);
			new Thread() {
				public void run() {
					((AppContext) getApplication()).uploadError(error,"0");
				};
			}.start();
		}
	}

	private void removeFromErrors() {
		currentQuestion = questionList.get(questionCursor);
		dao.moveOutError(username, currentQuestion.getQid());
		Toast.makeText(this, "移除成功,下次不再显示", Toast.LENGTH_SHORT).show();
	}

	private void favorQuestion() {
		currentQuestion = questionList.get(questionCursor);
		String qid = currentQuestion.getQid();
		favor.setQid(qid);
		if ("myFavors".equals(action)) {
			// 表示已经收藏了,现在要取消收藏
			if (favorQids.indexOf(qid) == -1) {
				Toast.makeText(this, "已经取消", Toast.LENGTH_SHORT).show();
				return;
			}
			this.favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
			dao.deleteFavor(favor);
			favorQids.replace(favorQids.indexOf(qid), favorQids.indexOf(qid)
					+ qid.length() + 1, "");
			Toast.makeText(this, "取消成功,下次不再显示", Toast.LENGTH_SHORT).show();
			return;
		}
		if (favorQids.indexOf(qid) != -1) {
			this.favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
			favorQids.replace(favorQids.indexOf(qid), favorQids.indexOf(qid)
					+ qid.length() + 1, "");
			favor.setNeedDelete(true);
			return;
		} else {
			// 没收藏,要收藏
			this.favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
			// dao.insertFavor(favor);
			favorQids.append(qid).append(",");
			Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
		}
	}

	public void submitOrSeeAnswer() {
		if ("DoExam".equals(action)) {
			// showDialog();
//			submitExam();
		} else {
			// questionAdapter.showAnswer(questionList.get(questionCursor));
			// 多选题保存
			QuestionAdapter.AnswerViewHolder holder = (AnswerViewHolder) viewFlow
					.getSelectedView().getTag(R.id.tag_second);
			if ("practice".equals(action))// &&("2".equals(currentQuestion.getQType())||"3".equals(currentQuestion.getQType())))
			{
				QuestionAdapter.ContentViewHolder contentHolder = (ContentViewHolder) viewFlow
						.getSelectedView().getTag(R.id.tag_first);
				if (currentQuestion.getUserAnswer() == null) {
					currentQuestion.setUserAnswer("");
				}
				if (!currentQuestion.getAnswer().equals(
						currentQuestion.getUserAnswer())) {
					if (preferences.getInt("isVibrate", 0) == 1)
						vibrator.vibrate(new long[] { 800, 50, 400, 1000 }, -1);
					ExamErrorQuestion error = new ExamErrorQuestion(
							currentQuestion.getQid(), username, paperid,
							currentQuestion.getUserAnswer());
					dao.insertError(error);
					tOrF[questionCursor] = -1;
				} else {
					tOrF[questionCursor] = 1;
				}
				String str = currentQuestion.getQid() + "-";
				if (answerBuf.indexOf(str) == -1) {
					answerBuf.append(str + currentQuestion.getUserAnswer()).append("&");
					record.setTempAnswer(answerBuf.toString()
							+ (txtAnswerBuf.length() == 0 ? "" : "   "
									+ txtAnswerBuf.toString()));
				}
				contentHolder.examOption.forbidden(false);
				contentHolder.examOption.setFontColor(
						getResources().getColor(R.color.green),
						currentQuestion.getAnswer(),
						getResources().getColor(R.color.red),
						currentQuestion.getUserAnswer(),
						currentQuestion.getQType());
				questionAdapter.showAnswer(holder, currentQuestion,
						currentQuestion.getUserAnswer());
			}
			if (holder.examAnswerLayout.getVisibility() == View.VISIBLE)
				holder.examAnswerLayout.setVisibility(View.GONE);
			else
				holder.examAnswerLayout.setVisibility(View.VISIBLE);

		}
	}

	private void gotoChooseActivity() {
		Intent mIntent = new Intent(this, QuestionChooseActivity.class);
		// 绑数据
		if ("DoExam".equals(action) || "practice".equals(action)) {
			mIntent.putExtra("action", "chooseQuestion");
			mIntent.putExtra("ruleListJson", ruleListJson);
			mIntent.putExtra("trueOfFalse", gson.toJson(tOrF));
			mIntent.putExtra("isDone", gson.toJson(isDone));
		} else {
			mIntent.putExtra("action", "otherChooseQuestion");
			mIntent.putExtra("questionList", gson.toJson(questionList));
		}
		this.startActivityForResult(mIntent, 1);
	}

	private void gotoChooseActivity2() {
		Intent mIntent = new Intent(this, QuestionChooseActivity.class);
		// 绑数据
		mIntent.putExtra("action", "submitPaper");
		mIntent.putExtra("questionList", gson.toJson(questionList));
		mIntent.putExtra("paperScore", paperScore);
//		mIntent.putExtra("paperTime", time / 60);
		mIntent.putExtra("username", username);
		mIntent.putExtra("paperid", paperid);
		mIntent.putExtra("useTime", record.getUseTime());
		mIntent.putExtra("isDone", gson.toJson(isDone));
		mIntent.putExtra("userScore", record.getScore()); // 本次得分
		mIntent.putExtra("hasDoneNum", isDone.size()); // 做了多少题
		this.startActivityForResult(mIntent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (20 == resultCode) {
			// 更换试题,当前试题
			String ruleTitle = data.getStringExtra("ruleTitle");
			this.examTypeTextView.setText(ruleTitle);
			questionCursor = data.getIntExtra("cursor", 0);
			action = data.getStringExtra("action");
			// 更新
			data.setAction(action);
			System.out.println("data.getAction = " + action);
			questionAdapter.notifyDataSetChanged();
			viewFlow.setSelection(questionCursor);
		} else if (30 == resultCode) {
			action = "DoExam";
			questionCursor = 0;
			record.setTempAnswer("");
			record.setIsDone("");
			answerBuf.delete(0, answerBuf.length());
			txtAnswerBuf.delete(0, txtAnswerBuf.length());
			isDone.clear();
			setNull4UserAnswer();
		} else if (0 == resultCode) {
			this.finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void writeNote() {
		showNoteBookActivity();
	}

	private void showNoteBookActivity() {
		Intent mIntent = new Intent(this, QuestionWriteNoteActivity.class);
		// 绑数据,当前的试题的id,username
		mIntent.putExtra("paperid", paperid);
		mIntent.putExtra("qid", questionList.get(questionCursor).getQid());
		mIntent.putExtra("classid", questionList.get(questionCursor).getQid());
		this.startActivity(mIntent);

	}

	private void preQuestion() {
		if (questionCursor == 0) {
			Toast.makeText(this, "已经是第一题了", Toast.LENGTH_SHORT).show();
			return;
		}
		questionAdapter.clearCheck();
		questionCursor--;
		viewFlow.setSelection(questionCursor);
	}

	private void nextQuestion() {
		if (questionCursor == questionList.size() - 1) {
			Toast.makeText(this, "已经是最后一题了", Toast.LENGTH_SHORT).show();
			return;
		}
		questionAdapter.clearCheck();
		questionCursor++;
		viewFlow.setSelection(questionCursor);
	}

	// 保存选择题(单选和多选)答案
	public void saveChoiceAnswer(String abcd) // 1001-A&1002-B&
	{
		if (!"DoExam".equals(action) && !"practice".equals(action)) {
			return;
		}
		currentQuestion = questionList.get(questionCursor);
		// 判断题改变答案 A为对的,B为错的
		if ("4".equals(currentQuestion.getQType())) {
			abcd = "A".equals(abcd) ? "1" : "0";
		}
		String str = currentQuestion.getQid() + "-";
		if (answerBuf.indexOf(str) == -1) {
			answerBuf.append(str + abcd).append("&");
			isDone.append(questionCursor, true);
		} else {
			String left = answerBuf.substring(0, answerBuf.indexOf(str));
			String temp = answerBuf.substring(answerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("&") + 1);
			if ("".equals(abcd)) // 多选题,没有选答案
			{
				// 从答案里去除
				answerBuf.delete(0, answerBuf.length()).append(left)
						.append(right);
				isDone.delete(questionCursor);
			} else {
				answerBuf.delete(0, answerBuf.length()).append(left)
						.append(str).append(abcd).append("&").append(right);
				isDone.append(questionCursor, true);
			}
		}
		System.out.println("answerBuf = " + answerBuf.toString());
		record.setTempAnswer(answerBuf.toString()
				+ (txtAnswerBuf.length() == 0 ? "" : "   "
						+ txtAnswerBuf.toString()));
		// 每做完5道题自动保存答案
		if (answerBuf.toString().split("&").length % 5 == 0 && record != null) {
			record.setIsDone(gson.toJson(isDone));
			dao.updateTempAnswerForRecord(record);
		}
		currentQuestion.setUserAnswer("".equals(abcd) ? null : abcd); // 保存学员答案
		// 如果是练习,并且为单选题,判断对错添加错题
		if ("practice".equals(action)
				&& ("4".equals(currentQuestion.getQType()) || "1"
						.equals(currentQuestion.getQType()))) {
			// 显示答案
			QuestionAdapter.ContentViewHolder contentHolder = (ContentViewHolder) viewFlow
					.getSelectedView().getTag(R.id.tag_first);
			QuestionAdapter.AnswerViewHolder answerHolder = (AnswerViewHolder) viewFlow
					.getSelectedView().getTag(R.id.tag_second);
			// questionAdapter.setRadioEnable(contentHolder.examOption, false);
			contentHolder.examOption.forbidden(false);
			contentHolder.examOption.setFontColor(QuestionDoExamActivity1.this
					.getResources().getColor(R.color.green), currentQuestion
					.getAnswer(), QuestionDoExamActivity1.this.getResources()
					.getColor(R.color.red), currentQuestion.getUserAnswer(),
					currentQuestion.getQType());
			questionAdapter.showAnswer(answerHolder, currentQuestion, abcd);
			answerHolder.examAnswerLayout.setVisibility(View.VISIBLE);
			if (!currentQuestion.getAnswer().equals(
					currentQuestion.getUserAnswer())) {
				if (preferences.getInt("isVibrate", 0) == 1)
					vibrator.vibrate(new long[] { 800, 50, 400, 1000 }, -1);
				ExamErrorQuestion error = new ExamErrorQuestion(
						currentQuestion.getQid(), username, paperid,
						currentQuestion.getUserAnswer());
				dao.insertError(error);
				tOrF[questionCursor] = -1;
				errorBuf.append(currentQuestion.getQid()).append("_")
						.append(currentQuestion.getUserAnswer()).append("_")
						.append(currentQuestion.getAnswer()).append(":");
			} else {
				tOrF[questionCursor] = 1;
			}
		}
	}

	// 保存问答题答案
	public void saveTextAnswer(String txtAnswer) {
		if (!"DoExam".equals(action)) {
			return; // 非考试不必保存答案
		}
		String str = currentQuestion.getQid() + "-";
		if ("".equals(txtAnswer.trim())) {
			Toast.makeText(this, "请填写答案", Toast.LENGTH_LONG).show();
			return;
		}
		if (txtAnswerBuf.indexOf(str) == -1) {
			txtAnswerBuf.append(str + txtAnswer.replace("\\s", "")).append(
					"   ");
		} else {
			String left = txtAnswerBuf.substring(0, txtAnswerBuf.indexOf(str));
			String temp = txtAnswerBuf.substring(txtAnswerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("   ") + 3);
			txtAnswerBuf.delete(0, txtAnswerBuf.length()).append(left)
					.append(str).append(txtAnswer).append("   ").append(right);
		}
		isDone.append(questionCursor, true);
		currentQuestion.setUserAnswer(txtAnswer);
		if (record != null) {
			record.setTempAnswer(answerBuf.toString() + "   "
					+ txtAnswerBuf.toString());
			record.setIsDone(gson.toJson(isDone));
			dao.updateTempAnswerForRecord(record);
		}
		Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
	}

	// 交卷,评判分
	private void submitPaper() {
		/**
		 * 
		 */
		if (record.getTempAnswer() == null
				|| "".equals(record.getTempAnswer().trim())) {
			Toast.makeText(this, "还没做交毛卷啊", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			double score = 0; // 总分
			double score1 = 0; // 答错扣分的情况
			double score2 = 0; // 计算大题的临时变量
			StringBuffer buf = new StringBuffer();
			StringBuffer scoreBuf = new StringBuffer("eachScore&");
			for (int k = 0; k < ruleList.size(); k++) // 循环大题
			{
				ExamRule r = ruleList.get(k);
				double fen = r.getScoreForEach();// 每题的分数
				String fenRule = r.getScoreSet();// 判分规则 0|N表示每题多少分就是多少分，
													// 1|N,表示答对一个选项得N分，全部答对得该题的满分
													// 2|N,表示打错扣N分,最少得0分
				for (int j = 0; j < questionList.size(); j++) // 循环题目
				{
					ExamQuestion q = questionList.get(j);
					double tempScore = 0;
					if (q.getRuleId().equals(r.getRuleId())) // 属于该大题的题目，按该规则进行判分
					{
						System.out.println(q.getAnswer() + ", userAnswer:"
								+ q.getUserAnswer());
						if (fenRule.startsWith("0|")) // 答错不扣分，全对才得满分
						{
							if (q.getAnswer().equals(q.getUserAnswer())) {
								score = score + fen; // 得分
								tempScore = fen;
							}
						} else if (fenRule.startsWith("1|"))// 答对一个选项得多少分
						{
							String answer = q.getAnswer();
							String userAnswer = q.getUserAnswer() == null ? "@"
									: q.getUserAnswer();
							if (answer.contains(userAnswer)) { // 包含答案算分
								if (answer.equals(userAnswer)) {
									score = score + fen;
									tempScore = fen;
								} else {
									String[] ua = userAnswer.split("[,]"); // 少选得分，是每个选项的得分还是只要是少选就得多少分
									double fen1 = Double.parseDouble(fenRule
											.split("[|]")[1]) * ua.length;
									score = score + fen1;
									tempScore = fen1;
								}
							}
						} else if (fenRule.startsWith("2|"))// 答错扣分
						{
							if (q.getAnswer().equals(q.getUserAnswer())) // 答对
							{
								score1 = score1
										+ Double.parseDouble(fenRule
												.split("[|]")[1]);
								tempScore = Double.parseDouble(fenRule
										.split("[|]")[1]);
							} else // 答错
							{
								score1 = score1
										- Double.parseDouble(fenRule
												.split("[|]")[1]);
								tempScore = 0 - Double.parseDouble(fenRule
										.split("[|]")[1]);
							}
						}
						scoreBuf.append(r.getRuleId()).append("-")
								.append(q.getQid()).append("-")
								.append(tempScore).append("&"); // 每道题的得分
					}
				}
				// 每大题得分
				if (fenRule.startsWith("2|")) {
					buf.append(r.getRuleId());
					buf.append("=");
					buf.append(score1 > 0 ? score1 : 0);
					buf.append(";");
				} else {
					buf.append(r.getRuleId());
					buf.append("=");
					score2 = score - score2;
					buf.append(score2);
					buf.append(";");
					score2 = score;
				}
			}
			score = score1 > 0 ? (score + score1) : score;
			// 学员答案存进去
			record.setScore(score);
			System.out.println("scoreBuf = " + scoreBuf.toString());
			// 更新record记录
			// record.setRcdScoreForEachQuestion(scoreBuf.toString());//每题的得分情况
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 大题列表弹出窗口
	 * 
	 * @param parent
	 */
	private void showWindow(View parent) {
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = layoutInflater.inflate(
					R.layout.popupwindow_rule_layout, null);

			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			// 加载数据

			PopRuleListAdapter groupAdapter = new PopRuleListAdapter(this,
					ruleList);
			lv_group.setAdapter(groupAdapter);
			// 创建一个PopuWidow对象
			WindowManager wm = (WindowManager) this
					.getSystemService(Context.WINDOW_SERVICE);

			int width = (int) (wm.getDefaultDisplay().getWidth() / 2.4);
			// int height = (int) (wm.getDefaultDisplay().getHeight()/3.2);
			popupWindow = new PopupWindow(view, width,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
		int xPos = parent.getWidth() / 2
				- popupWindow.getWidth() / 2;

		// Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:"
		// + windowManager.getDefaultDisplay().getWidth() / 2);
		// //
		// Log.i("coder", "popupWindow.getWidth()/2:" + popupWindow.getWidth() /
		// 2);
		//
		// Log.i("coder", "xPos:" + xPos);

		popupWindow.showAsDropDown(parent, xPos, -5);
		// popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

		lv_group.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// 切题,改变大题名称,切到该大题第一题
				// 当前大题
				ExamRule rule = QuestionDoExamActivity1.this.ruleList
						.get(position);
				int questionPosition = 0;
				for (int i = position - 1; i >= 0; i--) {
					questionPosition += QuestionDoExamActivity1.this.ruleList
							.get(i).getQuestionNum();
				}
				QuestionDoExamActivity1.this.examTypeTextView.setText(rule
						.getRuleTitle());
				QuestionDoExamActivity1.this.questionAdapter.clearCheck();
				QuestionDoExamActivity1.this.questionCursor = questionPosition; // cursor从0开始
				QuestionDoExamActivity1.this.viewFlow
						.setSelection(questionCursor);
				if (popupWindow != null) {
					popupWindow.dismiss();
				}
			}
		});
	}


	// 按返回键,提示
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		if ((paramKeyEvent.getKeyCode() == 4)
				&& (paramKeyEvent.getRepeatCount() == 0)) {
			if ("DoExam".equals(action)) {
				showDialog();
				return true;
			}
			if ("practice".equals(action)) {
				uploadErrorQuestions();
				this.finish();
				return true;
			}
		}
		return super.onKeyDown(paramInt, paramKeyEvent);
	}

	private void showDialog() {
		if (exitDialog == null) {
			View v = LayoutInflater.from(this).inflate(R.layout.exit_layout,
					null);
			Button exitBtn = (Button) v.findViewById(R.id.exitExamBtn);
			Button submitBtn = (Button) v.findViewById(R.id.exitSubmitExamBtn);
			Button cancelBtn = (Button) v.findViewById(R.id.exitCancelExamBtn);
			AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			localBuilder.setTitle("退出").setMessage("是否退出考试").setView(v);
			exitDialog = localBuilder.create();
			exitBtn.setOnClickListener(this);
			submitBtn.setOnClickListener(this);
			cancelBtn.setOnClickListener(this);
		}
		exitDialog.show();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 保存记录
		if (record != null) {
			record.setLastTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA)
					.format(new Date()));
			record.setIsDone(gson.toJson(isDone));
			if ("practice".equals(action)) {
				record.setAnswers(gson.toJson(tOrF));
				record.setErrorNum(calcErrorNum(tOrF));
			}
			dao.saveOrUpdateRecord(record);
		}
		// MobclickAgent.onPause(this);
	}

	private int calcErrorNum(int[] arr) {
		if (tOrF == null)
			return 0;
		int sum = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == -1) {
				sum++;
			}
		}
		return sum;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
//		timerFlag = false;
		if (vibrator != null) {
			vibrator.cancel();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (exitDialog != null) {
			exitDialog.dismiss();
		}
		if (favor.getQid() != null) {
			// 收藏一个
			new Thread() {
				public void run() {
					if (favor.isNeedDelete()) {
						dao.deleteFavor(favor);
					} else
						dao.insertFavor(favor);
					favor.setQid(null);
				};
			}.start();
		}
		super.onDestroy();
	}


	private void initQuestionAnswer(String tempAnswer) {
		if (tempAnswer == null || "".equals(tempAnswer.trim())) {
			return;
		}
		int listSize = questionList.size();
		String choiceAnswer = null, textAnswer = null;
		if (tempAnswer.indexOf("   ") != -1) {
			choiceAnswer = tempAnswer.substring(0, tempAnswer.indexOf("   "));
			textAnswer = tempAnswer.substring(tempAnswer.indexOf("   ") + 3);
		} else {
			choiceAnswer = tempAnswer;
		}
		for (int i = 0; i < listSize; i++) {
			ExamQuestion q = questionList.get(i);
			String str = q.getQid() + "-";
			if ((!"问答题".equals(q.getQType()))
					&& choiceAnswer.indexOf(str) != -1) {
				String temp = choiceAnswer.substring(choiceAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(), temp.indexOf("&")));
			} else if (textAnswer != null && "问答题".equals(q.getQType())
					&& textAnswer.indexOf(str) != -1) {
				String temp = textAnswer.substring(textAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(),
						temp.indexOf("   ")));
			}
		}
	}

	private void setNull4UserAnswer() {
		int i = 0;
		for (ExamQuestion q : questionList) {
			q.setUserAnswer(null);
			tOrF[i] = 0;
			i++;
		}
	}

	private void openPopupwin() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.pop_practice_tips, null, true);
		tipWindow = new PopupWindow(menuView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		tipWindow.setFocusable(true);
		tipWindow.setBackgroundDrawable(new BitmapDrawable());
		tipWindow.setAnimationStyle(R.style.AnimationFade);

		/***************** 以下代码用来循环检测activity是否初始化完毕 ***************/
		Runnable showPopWindowRunnable = new Runnable() {
			@Override
			public void run() {
				// 得到activity中的根元素
				View view = findViewById(R.id.parent);
				// 如何根元素的width和height大于0说明activity已经初始化完毕
				if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
					// 显示popwindow
					tipWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
					// 停止检测
					mHandler.removeCallbacks(this);
				} else {
					// 如果activity没有初始化完毕则等待5毫秒再次检测
					mHandler.postDelayed(this, 5);
				}
			}
		};
		// 开始检测
		mHandler.post(showPopWindowRunnable);
		/****************** 以上代码用来循环检测activity是否初始化完毕 *************/
	}

	public void disPopupWin(View v) {
		if (tipWindow != null && tipWindow.isShowing())
			tipWindow.dismiss();
		SharedPreferences.Editor editor = guidefile.edit();
		if (guidefile.contains("isFirstPractice")) {
			editor.remove("isFirstPractice");
		}
		editor.putInt("isFirstPractice", 1);
		editor.commit();
	}

	public String getAction() {
		return action;
	}

	private void showPopWindow(View parent) {
		if (menuPop == null) {
			View v = LayoutInflater.from(this).inflate(
					R.layout.pop_question_more_menu, null);
			ListView listView = (ListView) v
					.findViewById(R.id.question_menu_listView1);
			SampleAdapter adapter = new SampleAdapter(this);
			adapter.add(new SampleItem("重新开始", R.drawable.restar_btn));
			adapter.add(new SampleItem("答题卡", R.drawable.btn_answer_progress));
			adapter.add(new SampleItem("答题设置", R.drawable.btn_settting));
			// adapter.add(new SampleItem("笔记", R.drawable.btn_analyze));
			// adapter.add(new SampleItem("截图",R.drawable.btn_analyze));
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new MenuItemClickListener());
			menuPop = new PopupWindow(v, ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			menuPop.setBackgroundDrawable(new BitmapDrawable());
			menuPop.setFocusable(true); // 使其聚焦
			menuPop.setOutsideTouchable(true);
		}
		if (menuPop.isShowing())// ||clickCount%2!=0)
		{
			menuPop.dismiss();
			return;
		}
		menuPop.showAsDropDown(parent, 0, -10);
	}

	private class SampleItem {
		public String tag;
		public int iconRes;

		public SampleItem(String tag, int iconRes) {
			this.tag = tag;
			this.iconRes = iconRes;
		}

		public SampleItem() {
			// TODO Auto-generated constructor stub
		}
	}

	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row, null);
				holder.title = (TextView) convertView
						.findViewById(R.id.menu_item);
				convertView.setTag(holder);

			}
			holder = (ViewHolder) convertView.getTag();
			holder.title.setCompoundDrawablesWithIntrinsicBounds(
					getItem(position).iconRes, 0, 0, 0);
			holder.title.setText(getItem(position).tag);
			return convertView;
		}

		class ViewHolder {
			TextView title;
		}
	}

	private class MenuItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			menuPop.dismiss();
			switch (arg2) {
			case 0:
				restart();
				break;
			case 1:
				// 答题卡,选题
				gotoChooseActivity();
				break;
			case 2:
				// 设置
				showPop();
				break;
			}
		}
	}

	private void showPop() {
		// AlertDialog.Builder builder = new Builder(this);
		if (pop == null) {
			View view = LayoutInflater.from(this).inflate(
					R.layout.answer_setting_pup, null);
			AnswerSettingLayout setLayout = (AnswerSettingLayout) view
					.findViewById(R.id.answer_set_view);
			setLayout.setOnLightChangeListerner(new LightChangeListerner() {

				@Override
				public void onLightChangeClick(int paramInt) {
					// TODO Auto-generated method stub
					Window localWindow = getWindow();
					WindowManager.LayoutParams localLayoutParams = localWindow
							.getAttributes();
					float f = paramInt / 255.0F;
					localLayoutParams.screenBrightness = f;
					localWindow.setAttributes(localLayoutParams);
				}
			});
			setLayout
					.setOnFontSizeChangeListener(new FontSizeChangeListerner() {
						@Override
						public void changeSize(float size) {
							// TODO Auto-generated method stub
							QuestionAdapter.ContentViewHolder contentHolder = (ContentViewHolder) viewFlow
									.getSelectedView().getTag(R.id.tag_first);
							QuestionAdapter.AnswerViewHolder answerHolder = (AnswerViewHolder) viewFlow
									.getSelectedView().getTag(R.id.tag_second);
							questionAdapter.setFontSize(contentHolder,
									answerHolder, size);
						}
					});
			setLayout.setOnItemChangeListener(new ItemChangeListerner() {

				@Override
				public void onItemClick(int paramInt) {
					// TODO Auto-generated method stub
					changescreen();
				}
			});
			pop = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			// 使其聚集
			pop.setFocusable(true);
			// 设置允许在外点击消失
			pop.setOutsideTouchable(true);

			// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
			pop.setBackgroundDrawable(new BitmapDrawable());
			pop.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					if (preferences.getFloat("fontsize", 16.0f) != 16.0f) {
						// 重新绘制
						questionAdapter.notifyDataSetChanged();
					}
				}
			});
		}
		pop.showAtLocation(findViewById(R.id.parent), Gravity.CENTER, 0, 0);
	}

	// 重新开始
	/*
	 * 重置答题记录,答案记录,跳回到第一题
	 */
	private void restart() {
		// showDialog,是否确定
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("是否重新开始?");
		builder.setCancelable(true);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				record.setTempAnswer("");
				record.setIsDone("");
				if (answerBuf.length() > 0) {
					answerBuf.delete(0, answerBuf.length());
				}
				isDone.clear();
				questionCursor = 0;
				setNull4UserAnswer();
				questionAdapter.notifyDataSetChanged();
				viewFlow.setSelection(questionCursor);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	static class MyHandler extends Handler {
		WeakReference<QuestionDoExamActivity1> weak;

		public MyHandler(QuestionDoExamActivity1 context) {
			// TODO Auto-generated constructor stub
			weak = new WeakReference<QuestionDoExamActivity1>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			QuestionDoExamActivity1 q2 = weak.get();
			if(q2.proDialog!=null)
			{
				q2.proDialog.dismiss();
			}
			switch (msg.what) {
			case 1:
				q2.questionAdapter = new QuestionAdapter(q2, q2,
						q2.questionList, q2.username, q2.paperid);
				q2.viewFlow.setAdapter(q2.questionAdapter);
				if ("DoExam".equals(q2.action) || "practice".equals(q2.action)) {
					// q2.examTitle.setText(q2.papername); // 试卷名字
					// this.examAnswerLayout.setVisibility(View.GONE);
					if (q2.ruleList != null && q2.ruleList.size() > 0) {
						q2.currentRule = q2.ruleList.get(0);
						q2.examTypeTextView.setText(q2.currentRule
								.getRuleTitle()); // 大题名字
						q2.viewFlow.setSelection(q2.initCursor);
					} else {
						q2.nodataLayout.setVisibility(0);
					}
				}
				q2.loadingLayout.setVisibility(View.GONE);
				if (q2.proDialog != null && q2.proDialog.isShowing()) {
					q2.proDialog.dismiss();
				}
				int firstExam = q2.guidefile.getInt("isFirstPractice", 0);
				if (firstExam == 0) {
					q2.openPopupwin();
				}
				break;
			case -1:
				q2.loadingLayout.setVisibility(View.GONE);
				q2.nodataLayout.setVisibility(View.VISIBLE);
			}
		}
	}
}
