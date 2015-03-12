package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ForumListAdapter;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.BitmapManager;
import com.changheng.accountant.util.StringUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class UserInfoDetailActivity extends BaseActivity implements OnClickListener{
	private ImageView ivFace;
	private String faceURL;
	private BitmapManager bmpManager;
	
	private Button postListBtn,replyPostListBtn;
	private ArrayList<ForumPost> mListItems;
	private PullToRefreshListView mPullRefreshListView;
	private ForumListAdapter mAdapter;
	private Handler mHandler;
	private LinearLayout loadLayout,reloadLayout;
	private boolean isLastPage;
	private String username;
	private AppContext appContext;
	private int mode;
	private static final Integer CURRENT_BTN_FLAG = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_userinfo);
		appContext = (AppContext) this.getApplication();
		bmpManager = new BitmapManager(BitmapFactory.decodeResource(this.getResources(), R.drawable.img_head_default));
		initViews();
		initPullToRefreshListView();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	private void initViews()
	{
		((Button)findViewById(R.id.btn_goback)).setOnClickListener(this);
		((TextView)findViewById(R.id.title)).setText("用户信息");
		Intent intent = getIntent();
		username = intent.getStringExtra("username");
		faceURL = intent.getStringExtra("face");
		((TextView)findViewById(R.id.txt_nickname)).setText(username);
		((TextView)findViewById(R.id.txt_location)).setText(intent.getStringExtra("location"));
		ivFace = (ImageView) this.findViewById(R.id.head_img);
		if(StringUtils.isEmpty(faceURL)){
			ivFace.setImageResource(R.drawable.img_head_default);
		}else if(faceURL.endsWith("portrait.gif"))
		{
			ivFace.setImageResource(R.drawable.img_head_default);
		}else{
			bmpManager.loadBitmap(faceURL, ivFace);
		}
		postListBtn = (Button) this.findViewById(R.id.main_post_btn);
		replyPostListBtn = (Button) this.findViewById(R.id.main_reply_btn);
		loadLayout = (LinearLayout) this.findViewById(R.id.loadingLayout);
		reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		postListBtn.setOnClickListener(this);
		replyPostListBtn.setOnClickListener(this);
		postListBtn.setTag(1);
	}
	private void initPullToRefreshListView()
	{
		this.mPullRefreshListView = (PullToRefreshListView)findViewById(R.id.postList);
		this.mPullRefreshListView.setMode(Mode.PULL_FROM_END); //上下都可以刷新
		this.mPullRefreshListView.getLoadingLayoutProxy(true, true).setPullLabel("上拉加载更多");
//		this.mPullRefreshListView.setRefreshingLabel(refreshingLabel, mode);
//		this.mPullRefreshListView.setReleaseLabel(releaseLabel, mode)
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});
		mPullRefreshListView.setOnItemClickListener(new ItemClickListener());
		mHandler = new ForumPostListHandler(this);
		//开一个线程去获取数据
		new GetDataThread().start();
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.main_post_btn:
			if(CURRENT_BTN_FLAG.equals(v.getTag()))
			{
				return;
			}
			postListBtn.setBackgroundResource(R.drawable.user_info_left_select);
			replyPostListBtn.setBackgroundResource(R.drawable.user_info_middle_no_select);
			postListBtn.setTag(1);
			replyPostListBtn.setTag(0);
			mode = 0;
			new GetDataThread().start();
		case R.id.main_reply_btn:
			if(CURRENT_BTN_FLAG.equals(v.getTag()))
			{
				return;
			}
			replyPostListBtn.setBackgroundResource(R.drawable.user_info_left_select);
			postListBtn.setBackgroundResource(R.drawable.user_info_middle_no_select);
			postListBtn.setTag(0);
			replyPostListBtn.setTag(1);
			mode = 1;
			new GetDataThread().start();
			break;
		}
	}
	private class GetDataTask extends AsyncTask<Void, Void, ArrayList<ForumPost>> {
		@Override
		protected ArrayList<ForumPost> doInBackground(Void... params) {
			// Simulates a background job.
			if(isLastPage)
			{
				return new ArrayList<ForumPost>();
			}
			try {
				int page = (mListItems.size() / AppContext.PAGE_SIZE +1);
				ArrayList<ForumPost> list = ApiClient.getForumPostListOfUser(appContext, page, AppContext.PAGE_SIZE,username,mode);
				return list;
			}catch(Exception e)
			{
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<ForumPost> result) {
			if(result == null)
			{
				Toast.makeText(UserInfoDetailActivity.this, "亲,网速不给力", Toast.LENGTH_SHORT).show();
			}
			else if(result.size() == 0)
			{
				Toast.makeText(UserInfoDetailActivity.this, "没有更多的帖子了", Toast.LENGTH_SHORT).show();
			}
			else
			{
				mListItems.addAll(result); 
				if(result.size()< AppContext.PAGE_SIZE)
				{
					//已经加载至最后一页
					Toast.makeText(UserInfoDetailActivity.this, "已加载至最后一页", Toast.LENGTH_SHORT).show();
					isLastPage = true;
				}
				mAdapter.notifyDataSetChanged();
			}
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
	private class GetDataThread extends Thread
	{
		public void run() {
			try {
				ArrayList<ForumPost> list = ApiClient.getForumPostListOfUser(appContext, 1, AppContext.PAGE_SIZE,username,mode);
				Message msg = mHandler.obtainMessage();
				msg.what = 1;
				msg.obj = list;
				mHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mHandler.sendEmptyMessage(-1); //加载出错
				e.printStackTrace();
			}
		};
	}
	static class ForumPostListHandler extends Handler
	{
		private WeakReference<UserInfoDetailActivity> wr ;
		public ForumPostListHandler(UserInfoDetailActivity activity) {
			// TODO Auto-generated constructor stub
			wr = new WeakReference<UserInfoDetailActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			try{
				UserInfoDetailActivity activity = wr.get();
			switch(msg.what)
			{
			case 1:
				Log.e("ForumActivity", "handler message");
				activity.mListItems = (ArrayList<ForumPost>) msg.obj;//(LinkedList<ForumPost>) msg.obj;
				if(activity.mListItems!=null&&activity.mListItems.size()>0)
				{
					activity.loadLayout.setVisibility(View.GONE);
					activity.reloadLayout.setVisibility(View.GONE);
					activity.isLastPage = activity.mListItems.size()<AppContext.PAGE_SIZE;
					activity.mAdapter = new ForumListAdapter(activity,activity.mListItems,activity.bmpManager);
					activity.mPullRefreshListView.getRefreshableView().setAdapter(activity.mAdapter);
				}else
				{
					//木有数据
//					activity.nodataLayout.setVisibility(View.VISIBLE);
				}
				break;
			case -1:
				//加载出错重新加载
				activity.loadLayout.setVisibility(View.GONE);
				activity.reloadLayout.setVisibility(View.VISIBLE);
			}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	private class ItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(UserInfoDetailActivity.this,ForumPostDetailActivity.class);
			intent.putExtra("postId", mListItems.get(arg2-1).getId());
			startActivity(intent);
		}
	}
	
	protected void onNewIntent (Intent intent)  
	{  
	     super.onNewIntent(intent);    
	     setIntent(intent);  
	}
}
