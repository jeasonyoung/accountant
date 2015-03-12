package com.changheng.accountant.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.KnowlegdgeListAdapter;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.dao.PlanDao;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.StringUtils;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class KnowledgeTodayActivity extends BaseActivity implements
		OnClickListener {

	private TextView dateTv, timerTv;
	private PullToRefreshListView listView;
	private LinearLayout loadingLayout, nodataLayout, reloadLayout,
			footerLayout;
	private AppConfig appConfig;
	private View headerView;
	private View footerView;
	private ArrayList<Knowledge> kList;
	private AppContext appContext;
	private ListView paperView;
	private ArrayList<Paper> pList;
	private String containsKid, containsPid;
	private PaperDao dao;
	private KnowlegdgeListAdapter kAdapter;
	private ArrayAdapter<Paper> pAdapter;
	public static final String DB_NAME = "accountant.db"; // 保存的数据库文件名
	public static final String PACKAGE_NAME = "com.changheng.accountant";
	private static final String DB_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/databases"; // 在手机里存放数据库的位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_knowledge_today);
		appConfig = AppConfig.getAppConfig(this);
		appContext = (AppContext) this.getApplication();
		initViews();
		initData();
	}

	private void initViews() {
		initHeader();
		initFooter();
		((Button) this.findViewById(R.id.btn_goback)).setOnClickListener(this);
		((TextView) this.findViewById(R.id.title)).setText("今日知识");
		loadingLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		listView = (PullToRefreshListView) this.findViewById(R.id.list);
		listView.setMode(Mode.DISABLED); // 禁用刷新
		ListView realListView = listView.getRefreshableView();
		realListView.addHeaderView(headerView);
		realListView.addFooterView(footerView);
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				switch (refreshView.getCurrentMode()) {
				case PULL_FROM_START: // 来自顶部的下拉
					// do something
					// Call onRefreshComplete when the list has been refreshed.
					listView.onRefreshComplete();
					break;
				case PULL_FROM_END: // 来自底部的上拉(加载更多)
					break;
				}
				// Do work to refresh the list here.
				// new GetDataTask().execute();
			}
		});
		listView.setOnItemClickListener(new ItemClickListener());
	}

	private void initHeader() {
		headerView = LayoutInflater.from(this).inflate(
				R.layout.knowledge_today_header, null);
		dateTv = (TextView) headerView.findViewById(R.id.dateTv);
		timerTv = (TextView) headerView.findViewById(R.id.timerTv);
	}

	private void initFooter() {
		footerView = LayoutInflater.from(this).inflate(
				R.layout.knowledge_today_footer, null);
		footerLayout = (LinearLayout) footerView.findViewById(R.id.footer);
		paperView = (ListView) footerView.findViewById(R.id.paperList);
		paperView.setOnItemClickListener(new PaperItemClickListener());
	}

	// 计算日期
	private String calculateRestDay(long setTime) {
		long now = System.currentTimeMillis();
		String s = null;
		Date thatDay = new Date(setTime);
		if (thatDay.before(new Date(now))) // 在今天之前
		{
			s = "考试日期已过";
		} else {
			long i = (thatDay.getTime() - now) / 1000 / 60 / 60 / 24;
			s = "距离考试仅 " + i + " 天";
			if (i == 0) {
				s = "距离考试不到 1 天";
			}
		}
		return s;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		if (appConfig.getExamTime() == 0) {
			this.timerTv.setText("点击设置考试时间");
		} else
			this.timerTv.setText(calculateRestDay(appConfig.getExamTime()));
		super.onStart();
	}

	private void initData() {
		Intent intent = this.getIntent();
		containsKid = intent.getStringExtra("containsKid");
		containsPid = intent.getStringExtra("containsPid");
		dao = new PaperDao(this);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 EEE",
				Locale.CHINA);
		dateTv.setText(format.format(new Date()));
		listView.setVisibility(View.GONE);
		new GetDataTask().execute();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.timerTv:
			System.out.println("时间TextView被点击");
			startActivity(new Intent(this, SetTimeActivity.class));
			break;
		}
	}

	private class GetDataTask extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			loadingLayout.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {
				if(!new File(DB_PATH+File.separator+DB_NAME).exists())	//数据库文件不存在用这个方法
				{
					kList = new PlanDao(KnowledgeTodayActivity.this, null)
						.findKnowledge(containsKid);
				}else
				{
					kList = new PaperDao(KnowledgeTodayActivity.this).findKnowledgeByKids(containsKid);
				}
				if (!StringUtils.isEmpty(containsPid)) {
					pList = (ArrayList<Paper>) dao.findPapers(containsPid);
					if (pList == null) {
						pList = ApiClient.getPapersByIds(appContext,
								containsPid);
						dao.insertPaperList(pList);
					}
					// 数据库中的试卷少于计划中的试卷数
					// else if(pList.size()<containsPid.split(",").length)
					// {
					// pList.clear();
					// pList = ApiClient.getPapersByIds(appContext,
					// containsPid);
					// dao.insertPaperList(pList);
					// }
				}
				return "";
			} catch (AppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null) {
				reloadLayout.setVisibility(View.VISIBLE);
				return;
			}
			if (kList.size() == 0) {
				nodataLayout.setVisibility(View.VISIBLE);
				return;
			}
			if (pList == null || pList.size() == 0) {
				footerLayout.setVisibility(View.GONE);
			} else if (pAdapter == null) {
				pAdapter = new ArrayAdapter<Paper>(KnowledgeTodayActivity.this,
						R.layout.row2, R.id.menu_item, pList);
				paperView.setAdapter(pAdapter);
				// 重新计算高度
				// Utility.setListViewHeightBasedOnChildren(paperView);
			}
			if (kAdapter == null) {
				kAdapter = new KnowlegdgeListAdapter(
						KnowledgeTodayActivity.this, kList);
				listView.getRefreshableView().setAdapter(kAdapter);
			}
			loadingLayout.setVisibility(View.GONE);
			reloadLayout.setVisibility(View.GONE);
			nodataLayout.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			System.out.println("父ListView的点击 position=" + arg2);
			if (arg2 < 2) {
				if (arg2 == 1) {
					startActivity(new Intent(KnowledgeTodayActivity.this,
							SetTimeActivity.class));
					return;
				}
				return;
			}
			Knowledge k = kList.get(arg2 - 2); // 这里要减2，头和尾都占据了位置
			Intent intent = new Intent(KnowledgeTodayActivity.this,
					ChapterPointerDetailActivity.class);
			intent.putExtra("knowledgeId", k.getKnowledgeId());
			intent.putExtra("knowledgeTitle", k.getTitle());
			intent.putExtra("content", k.getFullContent());
			startActivity(intent);
		}
	}

	private class PaperItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			// 需要登陆
			if (appContext.getUsername() == null) {
				Intent intent = new Intent(KnowledgeTodayActivity.this,
						LoginActivity.class);
				KnowledgeTodayActivity.this.startActivity(intent);
				return;
			}
			Intent intent = new Intent(KnowledgeTodayActivity.this,
					QuestionPaperInfoActivity.class);
			intent.putExtra("paperJson", new Gson().toJson(pList.get(arg2)));
			KnowledgeTodayActivity.this.startActivity(intent);
		}
	}
}
