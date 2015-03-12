package com.changheng.accountant.ui;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.MainGridAdapter;
import com.changheng.accountant.db.MyDBManager;
import com.changheng.accountant.util.DBDownloadManager;
import com.changheng.accountant.util.StringUtils;
import com.changheng.accountant.util.URLs;
import com.changheng.accountant.view.HomeGrid;

public class MainFragment extends Fragment {
	private HomeGrid g;
	private RelativeLayout time_tip, setDateLayout;
	private TextView restDay, usernameTv;
	private SimpleDateFormat format;
	private AppConfig appConfig;
	private AppContext appContext;
	private ScrollView scrollView;
	private ProgressDialog mProDialog;
	
	//主界面适配的Activity
	private static final Class[] classes = { KnowledgeEverydayActivity.class,
			ChooseChapterActivity.class, QuestionCommonFirstActivity.class,
			QuestionCommonFirstActivity.class, ChooseCourseActivity.class,
			QuestionCommonFirstActivity.class, ForumActivity.class,
			QuestionRecordActivity.class, InformationMainActivity.class };
	private static final String[] actionName = { null, null, "myFavors",
			"myErrors", null, "myNotes", null, null, null };
	//是否需要登录
	private static final boolean[] needLogin = { false, true, true, true, true,
			true, false, true, false };
	private boolean flag = true;	//是否登录的标识

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i("Mainfragment", "onCreateView");
		appConfig = AppConfig.getAppConfig(getActivity());
		appContext = (AppContext) getActivity().getApplication();
		View v = inflater.inflate(R.layout.main_fragment, null);
		g = (HomeGrid) v.findViewById(R.id.gridview1);
		this.setDateLayout = (RelativeLayout) v
				.findViewById(R.id.set_date_layout);
		this.restDay = (TextView) v.findViewById(R.id.rest_day);
		this.usernameTv = (TextView) v.findViewById(R.id.usernameTv);
		this.usernameTv.setVisibility(View.GONE);
		restDay.getPaint().setFlags(
				Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		format = new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA);
		((TextView) v.findViewById(R.id.day_date)).setText(format
				.format(new Date()));
		scrollView = ((ScrollView) v.findViewById(R.id.scroll));
		g.setAdapter(new MainGridAdapter(getActivity()));
		g.setVisibility(View.VISIBLE);
//		scrollView.requestChildFocus(time_tip, null);
		g.setOnItemClickListener(new ItemClickListener());
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		this.setDateLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getActivity(), SetTimeActivity.class));
			}
		});
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		flag = true;
		if (appConfig.getExamTime() == 0) {
			this.restDay.setText("设置考试时间");
		} else
		{
			String time = calculateRestDay(appConfig.getExamTime());
			this.restDay.setText(time);
			int dayNums = StringUtils.toInt2(time);
			int color = dayNums<=0?getResources().getColor(R.color.black):
							dayNums<=30?getResources().getColor(R.color.red):
								dayNums<=90?getResources().getColor(R.color.blue):
									getResources().getColor(R.color.green);
			this.restDay.setTextColor(color);
		}
		setUsername();
	}

	private void setUsername() {
		// 开一个线程检测是不是已登录来更改主页用户名的设置
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				usernameTv.setText(appContext.getUsername());
				usernameTv.setVisibility(View.VISIBLE);
			};
		};
		new Thread() {
			public void run() {
				while (flag) {
					try {
						if (appContext.getLoginState() == AppContext.LOGINED
								|| appContext.getLoginState() == AppContext.LOCAL_LOGINED) {
							flag = false;
							handler.sendEmptyMessage(1);
						}else
							Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	// 计算日期
	private String calculateRestDay(long setTime) {
		String s = null;
		Date thatDay = new Date(setTime);
		long now = System.currentTimeMillis();
		if (thatDay.before(new Date(now))) // 在今天之前
		{
			s = "考试日期已过";
		} else {
			long i = (thatDay.getTime() - now) / 1000 / 60 / 60 / 24;
			s = i + " 天";
			if (i == 0) {
				s = "不到 1 天";
			}
		}
		return s;
	}

	// Item点击
	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			// 下数据包
			arg1.clearFocus();
			// if () {
			// UIHelper.ToastMessage(MainFragment.this.getActivity(),
			// R.string.network_not_connected);
			// // 没有网络连接，判断数据包有没有被下载
			// return;
			// }
			// 如果数据不存在 并且需要 登录 先去下载数据包
			if (!(new MyDBManager(MainFragment.this.getActivity()).isDBExist())
					&& needLogin[arg2]) {
				// 下数据包
				if (appContext.isNetworkConnected())
					DBDownloadManager.getManager().showNoticeDialog(
							MainFragment.this.getActivity());
				else {
					UIHelper.ToastMessage(MainFragment.this.getActivity(),
							"必须先下载数据包,请检查网络!");
				}
			} else if (needLogin[arg2]) // 要登陆才能进入
			{
				switch (appContext.getLoginState()) {
				case AppContext.LOCAL_LOGINED:
				case AppContext.LOGINED:
					startActivity(arg2,needLogin[arg2]);
					break;
				case AppContext.LOGINING:
					if (appContext.getLoginState() == AppContext.LOGINING) // 正在登录,显示progress
					{
						if (mProDialog != null) {
							mProDialog.show();
							return;
						}
						mProDialog = ProgressDialog.show(getActivity(), null,
								"正在登录，请稍后...", true, true);
						final Handler mHandler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								// TODO Auto-generated method stub
								switch (msg.what) {
								case 1:
									if (mProDialog != null) {
										mProDialog.dismiss();
									}
									if (appContext.getLoginState() != AppContext.LOGINED
											|| appContext.getLoginState() != AppContext.LOCAL_LOGINED) {
										Intent intent2 = new Intent(
												MainFragment.this.getActivity(),
												LoginActivity.class);
										intent2.putExtra("className",
												classes[arg2].getName());
										startActivity(intent2);
									} else {
										startActivity(arg2,needLogin[arg2]);
									}
									break;
								}
							}
						};
						Runnable checkIsLogin = new Runnable() {
							@Override
							public void run() {
								if (appContext.getLoginState() != AppContext.LOGINING) {
									// 去掉提示Dialog
									mHandler.sendEmptyMessage(1);
									mHandler.removeCallbacks(this);
								} else {
									// 如果没有登录完毕则等待500毫秒再次检测
									mHandler.postDelayed(this, 500);
								}
							}
						};
						mHandler.post(checkIsLogin);
					}
					break;
				default:
					Intent intent2 = new Intent(
							MainFragment.this.getActivity(),
							LoginActivity.class);
					intent2.putExtra("className", classes[arg2].getName());
					if (actionName[arg2] != null) {
						intent2.putExtra("actionName", actionName[arg2]);
					}
					startActivity(intent2);
					break;
				}
			} else {
				if (KnowledgeTodayActivity.class.equals(classes[arg2])) {
					if (appConfig.getExamTime() == 0) {
						Intent intent = new Intent(
								MainFragment.this.getActivity(),
								SetTimeActivity.class);
						intent.putExtra("flag", 1);
						startActivity(intent);
						Toast.makeText(getActivity(), "请设置考试时间和科目",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (StringUtils.isEmpty(appConfig
							.get(AppConfig.CONF_SELECTED_COURSEID))) {
						Intent intent = new Intent(
								MainFragment.this.getActivity(),
								SetTimeActivity.class);
						intent.putExtra("flag", 2);
						startActivity(intent);
						Toast.makeText(getActivity(), "请设置考试科目",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				//
				startActivity(arg2,needLogin[arg2]);
			}
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		flag = false;
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("MainFragment", "on Destroy");
		super.onDestroy();
	}
	private void startActivity(int arg2,boolean needCheck)
	{
		if(appContext.isTimeOver()&&needCheck)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("提示");
			builder.setMessage("抱歉，使用的期限已过，您可以去官网购买。");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					 Uri uri = Uri.parse(URLs.URL_BUY);          
				     Intent it = new Intent(Intent.ACTION_VIEW, uri);
				     startActivity(it);
				}
			});
			builder.setCancelable(true);
			builder.create().show();
		}else
		{
			Intent intent = new Intent(MainFragment.this.getActivity(),
				classes[arg2]);
			if (actionName[arg2] != null) {
				intent.putExtra("actionName", actionName[arg2]);
			}
			startActivity(intent);
		}
	}
}
