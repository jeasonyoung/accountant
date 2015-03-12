package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ForumListAdapter;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.util.ApiClient;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ForumActivity extends BaseActivity implements OnClickListener {
	private ArrayList<ForumPost> mListItems;
	private PullToRefreshListView mPullRefreshListView;
	private ForumListAdapter mAdapter;
	private Handler mHandler;
	private LinearLayout loadLayout, reloadLayout, nodataLayout, topMenuLayout;
	private PopupWindow popWindow;
	private TextView topTitle;
	private ImageView menuIn;
	private AppContext appContext;
	private static final int PAGESIZE = 20;
	private static final int FLAG_FROM_TOP = 0;
	private static final int FLAG_FROM_BOTTOM = 1;
	private boolean isLastPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_forum);
		appContext = (AppContext) this.getApplication();
		initViews();
		initPullToRefreshListView();
	}

	private void initViews() {
		loadLayout = (LinearLayout) this.findViewById(R.id.load);
		reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		nodataLayout = (LinearLayout) this.findViewById(R.id.layout_empty);
		topMenuLayout = (LinearLayout) this.findViewById(R.id.topmenu);
		topTitle = (TextView) this.findViewById(R.id.txt_center);
		menuIn = (ImageView) this.findViewById(R.id.iv_item_bg_in);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.findViewById(R.id.btn_right).setOnClickListener(this);
		this.findViewById(R.id.btn_reload).setOnClickListener(this);
		topMenuLayout.setOnClickListener(this);
	}

	private void initPullToRefreshListView() {
		this.mPullRefreshListView = (PullToRefreshListView) this
				.findViewById(R.id.list);
		this.mPullRefreshListView.setMode(Mode.BOTH); // 上下都可以刷新
		// this.mPullRefreshListView.getLoadingLayoutProxy(true,
		// true).setPullLabel("下拉刷新");
		this.mPullRefreshListView.setPullLabel("下拉刷新", Mode.PULL_FROM_START);
		this.mPullRefreshListView.setPullLabel("上拉加载下一页", Mode.PULL_FROM_END);
		// this.mPullRefreshListView.setReleaseLabel(releaseLabel, mode)
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);
						switch (refreshView.getCurrentMode()) {
						case PULL_FROM_START: // 来自顶部的下拉
							// do something
							// refreshView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
							new GetDataTask(FLAG_FROM_TOP).execute();
							break;
						case PULL_FROM_END: // 来自底部的上拉(加载更多)
							// refreshView.getLoadingLayoutProxy().setPullLabel("上拉加载下一页");
							new GetDataTask(FLAG_FROM_BOTTOM).execute();
							break;
						}
					}
				});
		mPullRefreshListView.setOnItemClickListener(new ItemClickListener());
		mHandler = new ForumPostListHandler(this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 开一个线程去获取数据
		new Thread() {
			public void run() {
				try {
					ArrayList<ForumPost> list = ApiClient.getForumPostList(
							appContext, 1, PAGESIZE);
					Message msg = mHandler.obtainMessage();
					msg.what = 1;
					msg.obj = list;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					mHandler.sendEmptyMessage(-1); // 加载出错
					e.printStackTrace();
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
		case R.id.btn_right:
			startPostActivity();
			return;
		case R.id.topmenu:
			//showPopWindow(v);
			return;
		case R.id.btn_reload:
			reload();
			break;
		}
		menuItemClick(v.getId());
	}

	private void reload()
	{
		loadLayout.setVisibility(View.VISIBLE);
		nodataLayout.setVisibility(View.GONE);
		reloadLayout.setVisibility(View.GONE);
		new Thread() {
			public void run() {
				try {
					ArrayList<ForumPost> list = ApiClient.getForumPostList(
							appContext, 1, PAGESIZE);
					Message msg = mHandler.obtainMessage();
					msg.what = 1;
					msg.obj = list;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					mHandler.sendEmptyMessage(-1); // 加载出错
					e.printStackTrace();
				}
			};
		}.start();
	}
	private void startPostActivity() {
		Intent intent = null;
		if (appContext.getLoginState() == AppContext.LOGINED) {
			intent = new Intent(this, ForumPostPubActivity.class);
		} else {
			intent = new Intent(this, LoginActivity.class);
			intent.putExtra("loginFrom", 5);
		}
		startActivity(intent);
	}

	private void menuItemClick(int rid) {
		switch (rid) {
		case R.id.layout_sort_response: // 最后回复
			topTitle.setText("最后回复");
			break;
		case R.id.layout_sort_create: // 最新发布
			topTitle.setText("最新发布");
			break;
		case R.id.layout_sort_elite: // 精华
			topTitle.setText("精华贴");
			break;
		case R.id.layout_sort_datebaby_elite:
			topTitle.setText("精华2");
			break;
		case R.id.layout_sort_location: // 选地区
			topTitle.setText("地区");
			break;
		}
		if(popWindow!=null)
			popWindow.dismiss();
	}

	private class GetDataTask extends
			AsyncTask<Void, Void, ArrayList<ForumPost>> {
		private int flag;

		public GetDataTask(int flag) {
			// TODO Auto-generated constructor stub
			this.flag = flag;
		}

		@Override
		protected ArrayList<ForumPost> doInBackground(Void... params) {
			// Simulates a background job.
			if (flag == FLAG_FROM_BOTTOM && isLastPage) {
				return new ArrayList<ForumPost>();
			}
			try {
				int pageindex = mListItems.size()/ PAGESIZE;
				int page = flag == FLAG_FROM_TOP ? 1 : ( pageindex==0?(pageindex+2):(pageindex + 1));
				ArrayList<ForumPost> list = ApiClient.getForumPostList(
						appContext, page, PAGESIZE);
				return list;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<ForumPost> result) {
			if (result == null) {
				Toast.makeText(ForumActivity.this, "亲,网速不给力",
						Toast.LENGTH_SHORT).show();
			} else if (result.size() == 0) {
				Toast.makeText(ForumActivity.this, "没有更多的帖子了",
						Toast.LENGTH_SHORT).show();
			} else if (flag == FLAG_FROM_BOTTOM) {
				mListItems.addAll(result);
				if (result.size() < PAGESIZE) {
					// 已经加载至最后一页
					Toast.makeText(ForumActivity.this, "已加载至最后一页",
							Toast.LENGTH_SHORT).show();
					isLastPage = true;
				}
				mAdapter.notifyDataSetChanged();
			} else {
				mListItems.clear();
				mListItems.addAll(result);
				mAdapter.notifyDataSetChanged();
				isLastPage = false;
			}
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

	static class ForumPostListHandler extends Handler {
		private WeakReference<ForumActivity> wr;

		public ForumPostListHandler(ForumActivity activity) {
			// TODO Auto-generated constructor stub
			wr = new WeakReference<ForumActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			try {
				ForumActivity activity = wr.get();
				switch (msg.what) {
				case 1:
					Log.e("ForumActivity", "handler message");
					activity.mListItems = (ArrayList<ForumPost>) msg.obj;// (LinkedList<ForumPost>)
																			// msg.obj;
					if (activity.mListItems != null
							&& activity.mListItems.size() > 0) {
						activity.loadLayout.setVisibility(View.GONE);
						activity.reloadLayout.setVisibility(View.GONE);
						activity.nodataLayout.setVisibility(View.GONE);
						activity.mAdapter = new ForumListAdapter(activity,
								activity.mListItems,null);
						activity.mPullRefreshListView.getRefreshableView()
								.setAdapter(activity.mAdapter);
						if(activity.mListItems.size()<20)
						{
							activity.isLastPage = true;
						}
					} else {
						// 木有数据
						activity.nodataLayout.setVisibility(View.VISIBLE);
					}
					break;
				case -1:
					// 加载出错重新加载
					Toast.makeText(activity, "亲,网络不给力吖", Toast.LENGTH_SHORT).show();
					activity.loadLayout.setVisibility(View.GONE);
					activity.nodataLayout.setVisibility(View.GONE);
					activity.reloadLayout.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// throw new RuntimeException(e);
				// Log.e("ForumActivity",e.getMessage());
			}
		}
	}

	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ForumActivity.this,
					ForumPostDetailActivity.class);
			intent.putExtra("postId", mListItems.get(arg2 - 1).getId());
			ForumActivity.this.startActivity(intent);
		}
	}

	private void showPopWindow(View view) {
		if (popWindow == null) {
			View v = LayoutInflater.from(this).inflate(R.layout.forum_menu,
					null);
			popWindow = new PopupWindow(v, -2, -2);
			// 设置点外面消失
			popWindow.setBackgroundDrawable(new BitmapDrawable());
			popWindow.setFocusable(true);
			popWindow.setOutsideTouchable(true);
			v.findViewById(R.id.layout_sort_response).setOnClickListener(this);
			v.findViewById(R.id.layout_sort_create).setOnClickListener(this);
			v.findViewById(R.id.layout_sort_elite).setOnClickListener(this);
			v.findViewById(R.id.layout_sort_datebaby_elite).setOnClickListener(
					this);
			v.findViewById(R.id.layout_sort_location).setOnClickListener(this);
			popWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					menuIn.setVisibility(View.VISIBLE);
				}
			});
		}
		if (popWindow.isShowing()) {
			popWindow.dismiss();
			return;
		}
		popWindow.showAsDropDown(this.findViewById(R.id.iv_item_bg_out), 4, -7);
		menuIn.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (popWindow != null && popWindow.isShowing()) {
			popWindow.dismiss();
		}
		super.onDestroy();
	}
}