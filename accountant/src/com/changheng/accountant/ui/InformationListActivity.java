package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.InfoListAdapter;
import com.changheng.accountant.entity.Info;
import com.changheng.accountant.entity.NewsList;
import com.changheng.accountant.view.NewDataToast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class InformationListActivity extends BaseActivity{
	private int action;
	private PullToRefreshListView mListView;
	private LinearLayout loading,reload,nodata;
	private InfoListAdapter mAdapter;
	private ArrayList<Info> mData;
	private Handler handler;
	private AppContext appContext;
	private View lvNews_footer;
	private ProgressBar lvNews_foot_progress;
	private TextView lvNews_foot_more;
	private static final int PAGESIZE = 20;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_information_list);
		initViews();
		initData();
	}
	private void initViews()
	{
		mListView = (PullToRefreshListView) findViewById(R.id.list);
		loading = (LinearLayout) findViewById(R.id.load);
		reload = (LinearLayout) findViewById(R.id.reload);
		nodata = (LinearLayout) findViewById(R.id.layout_empty);
		((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
		this.lvNews_footer = getLayoutInflater().inflate(R.layout.listview_footer,null);
		this.lvNews_foot_more = (TextView) lvNews_footer.findViewById(R.id.listview_foot_more);
		this.lvNews_foot_progress = (ProgressBar) lvNews_footer.findViewById(R.id.listview_foot_progress);
		findViewById(R.id.btn_goback).setOnClickListener(new ReturnBtnClickListener(this));
		mListView.getRefreshableView().addFooterView(lvNews_footer);
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				new GetDataTask().execute();
			}
		});
		findViewById(R.id.btn_reload).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				reload.setVisibility(View.GONE);
				loading.setVisibility(View.VISIBLE);
				new GetDataThread(0,true).start();
			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//arg2 从1开始
				if(arg2>mData.size())
				{
					footerClick();
					return;
				}
				Intent intent = new Intent(InformationListActivity.this,InfoDetailActivity.class);
				//webView.loadUrl("file:///android_asset/other/about.html");
				intent.putExtra("news_id",mData.get(arg2-1).getId());
				InformationListActivity.this.startActivity(intent);
			}
		});
	}
	private void initData()
	{
		appContext = (AppContext) getApplication();
		action = getIntent().getIntExtra("action", 0);
		handler = new MyHandler(this);
		new GetDataThread(0,false).start();
	}
	private void footerClick()
	{
		//已加载全部
		if(Integer.valueOf(1).equals((Integer)lvNews_footer.getTag()))
		{
			return;
		}
		lvNews_foot_more.setText("玩命加载中");
		lvNews_foot_progress.setVisibility(View.VISIBLE);
		new GetDataThread(mData.size()/PAGESIZE+1,true).start();
	}
	private class GetDataThread extends Thread
	{
		private int index;
		private boolean isRefresh;
		public GetDataThread(int index,boolean isRefresh) {
			// TODO Auto-generated constructor stub
			this.index = index;
			this.isRefresh = isRefresh;
		}
		public void run() {
			//getdata
			try{
				NewsList list = appContext.getNewsList(action, index, 15, isRefresh);
				Message msg = handler.obtainMessage();
				msg.what = index==0?1:4;
				msg.obj = list.getNewslist();
				handler.sendMessage(msg);
			}catch(Exception e)
			{
				e.printStackTrace();
				handler.sendEmptyMessage(index==0?-1:-4);
			}
		}
	}
	static class MyHandler extends Handler
	{
		private WeakReference<InformationListActivity> weak;
		public MyHandler(InformationListActivity context) {
			// TODO Auto-generated constructor stub
			weak = new WeakReference<InformationListActivity>(context);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			InformationListActivity i = weak.get();
			switch(msg.what)
			{
				case 1:
					//判断所得集合的size
					i.mData = (ArrayList<Info>) msg.obj;
					if(i.mData ==null||i.mData.size()==0)
					{
						i.loading.setVisibility(View.GONE);
						i.reload.setVisibility(View.GONE);
						i.nodata.setVisibility(View.VISIBLE);
					}else
					{
						i.loading.setVisibility(View.GONE);
						i.reload.setVisibility(View.GONE);
						i.nodata.setVisibility(View.GONE);
						i.mAdapter = new InfoListAdapter(i, i.mData);
						if(i.mData.size() >= PAGESIZE)
						{
							i.lvNews_footer.setVisibility(View.VISIBLE);
							i.lvNews_foot_more.setText("更多");
							i.lvNews_foot_progress.setVisibility(View.GONE);
							i.lvNews_footer.setTag(0);
						}else
						{
							i.lvNews_footer.setVisibility(View.VISIBLE);
							i.lvNews_foot_more.setText("已加载全部");
							i.lvNews_foot_progress.setVisibility(View.GONE);
							i.lvNews_footer.setTag(1);
						}
						i.mListView.setAdapter(i.mAdapter);
					}
					break;
				case -1:
					//重试
					i.loading.setVisibility(View.GONE);
					i.reload.setVisibility(View.VISIBLE);
					i.nodata.setVisibility(View.GONE);
					break;
				case 4:
					ArrayList<Info> list = (ArrayList<Info>) msg.obj;
					if(list.size()==0 || list.size()<PAGESIZE)
					{
						i.lvNews_footer.setVisibility(View.VISIBLE);
						i.lvNews_foot_more.setText("已加载全部");
						i.lvNews_foot_progress.setVisibility(View.GONE);
						i.lvNews_footer.setTag(1);
					}else
					{
						i.lvNews_footer.setVisibility(View.VISIBLE);
						i.lvNews_foot_more.setText("更多");
						i.lvNews_foot_progress.setVisibility(View.GONE);
						i.lvNews_footer.setTag(0);
					}
					//刷新数据
					i.mData.addAll(list);
					i.mAdapter.notifyDataSetChanged();
					break;
				case -4:
					i.lvNews_footer.setVisibility(View.VISIBLE);
					i.lvNews_foot_more.setText("加载失败,点击加载");
					i.lvNews_foot_progress.setVisibility(View.GONE);
					i.lvNews_footer.setTag(0);
					break;
			}
		}
	}
	
	/**
	 * 异步数据更新(下拉更新)
	 * @author Administrator
	 *
	 */
	private class GetDataTask extends AsyncTask<Void, Void, List<Info>> {

		@Override
		protected List<Info> doInBackground(Void... params) {
			// Simulates a background job.
			String[] mStrings = null;
			try {
				NewsList list = appContext.getNewsList(action, 0, 15, true); //true表示更新
				return list.getNewslist();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Info> result) {
			if(result == null)
			{
				NewDataToast.makeText(InformationListActivity.this, "加载出错请稍候再试", false).show();
				return;
			}
			int newDataNum = 0;
			if(mData!=null && mData.size()>0)
			{
				for(Info i1 : result)
				{
					boolean b = false;
					for(Info i2 : mData)
					{
						if(i1.getId() == i2.getId())
						{
							b = true;
							break;
						}
					}
					if (!b)
						newDataNum++;
					}
			}else
			{
				newDataNum = result.size();
			}
			mData.clear();
			mData.addAll(result);
			if(newDataNum>0)
				NewDataToast.makeText(InformationListActivity.this, "有"+newDataNum+"条更新", true).show();
			NewDataToast.makeText(InformationListActivity.this, "没有更新", false).show();
			mAdapter.notifyDataSetChanged();
			// Call onRefreshComplete when the list has been refreshed.
			mListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
}
