package com.changheng.accountant.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ForumListAdapter;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;
import com.changheng.accountant.util.StringUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class UserInfoFragment extends Fragment implements
		android.view.View.OnClickListener {
	private AppContext appContext;
	private ProgressDialog mProDialog;
	private TextView nicknameTxt, examTimeTxt, locationTxt;
	private ImageView headView;
	private final static int CROP = 200;
	private final static String FILE_SAVEPATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/CHAccountant/Portrait/";
	private Uri origUri;
	private Uri cropUri;
	private File protraitFile;
	private Bitmap protraitBitmap;
	private String protraitPath;

	private Button postBtn, replyBtn, favorBtn;
	private ArrayList<ForumPost> mListItems;
	private PullToRefreshListView mPullRefreshListView;
	private ForumListAdapter mAdapter;
	private Handler mHandler;
	private LinearLayout loadLayout, reloadLayout,nodataLayout;
	private boolean isLastPage;

	private int mode;
	
	private static final Integer CURRENT_BTN_FLAG = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.userinfo_fragment, null);
		appContext = (AppContext) this.getActivity().getApplication();
		initViews(v);
		initPullToRefreshListView(v);
		return v;
	}

	private void initViews(View v) {
		nicknameTxt = (TextView) v.findViewById(R.id.txt_nickname);
		examTimeTxt = (TextView) v.findViewById(R.id.exam_date);
		locationTxt = (TextView) v.findViewById(R.id.txt_location);
		headView = (ImageView) v.findViewById(R.id.head_img);
		nodataLayout = (LinearLayout) v.findViewById(R.id.nodataLayout);
		loadLayout = (LinearLayout) v.findViewById(R.id.loadingLayout);
		reloadLayout = (LinearLayout) v.findViewById(R.id.reload);
		v.findViewById(R.id.user_nickname_layout).setOnClickListener(this);
		v.findViewById(R.id.exam_date_layout).setOnClickListener(this);
		v.findViewById(R.id.user_location_layout).setOnClickListener(this);
		v.findViewById(R.id.gotoLogin).setOnClickListener(this);
		postBtn = (Button) v.findViewById(R.id.main_post_btn);
		postBtn.setOnClickListener(this);
		replyBtn = (Button) v.findViewById(R.id.main_reply_btn);
		replyBtn.setOnClickListener(this);
		favorBtn = (Button) v.findViewById(R.id.main_collect_btn);
		favorBtn.setOnClickListener(this);
		headView.setOnClickListener(this);
	}

	private void initPullToRefreshListView(View v) {
		this.mPullRefreshListView = (PullToRefreshListView) v
				.findViewById(R.id.postList);
		this.mPullRefreshListView.setMode(Mode.PULL_FROM_END); // 上下都可以刷新
		this.mPullRefreshListView.getLoadingLayoutProxy(true, true)
				.setPullLabel("上拉加载更多");
		// this.mPullRefreshListView.setRefreshingLabel(refreshingLabel, mode);
		// this.mPullRefreshListView.setReleaseLabel(releaseLabel, mode)
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// Do work to refresh the list here.
						new GetDataTask().execute();
					}
				});
		mPullRefreshListView.setOnItemClickListener(new ItemClickListener());
		mHandler = new ForumPostListHandler(this);
	}

	private class GetDataTask extends
			AsyncTask<Void, Void, ArrayList<ForumPost>> {
		@Override
		protected ArrayList<ForumPost> doInBackground(Void... params) {
			// Simulates a background job.
			if (isLastPage) {
				return new ArrayList<ForumPost>();
			}
			try {
				int page = (mListItems.size() / AppContext.PAGE_SIZE + 1);
				ArrayList<ForumPost> list = ApiClient.getForumPostListOfUser(
						appContext, page, AppContext.PAGE_SIZE,
						appContext.getUsername(), mode);
				return list;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<ForumPost> result) {
			if (result == null) {
				Toast.makeText(getActivity(), "亲,网速不给力", Toast.LENGTH_SHORT)
						.show();
			} else if (result.size() == 0) {
				Toast.makeText(getActivity(), "没有更多的帖子了", Toast.LENGTH_SHORT)
						.show();
			} else {
				mListItems.addAll(result);
				if (result.size() < AppContext.PAGE_SIZE) {
					// 已经加载至最后一页
					Toast.makeText(getActivity(), "已加载至最后一页",
							Toast.LENGTH_SHORT).show();
					isLastPage = true;
				}
				mAdapter.notifyDataSetChanged();
			}
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

	static class ForumPostListHandler extends Handler {
		private WeakReference<UserInfoFragment> wr;

		public ForumPostListHandler(UserInfoFragment activity) {
			// TODO Auto-generated constructor stub
			wr = new WeakReference<UserInfoFragment>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			try {
				UserInfoFragment activity = wr.get();
				switch (msg.what) {
				case 1:
					Log.e("ForumActivity", "handler message");
					activity.mListItems = (ArrayList<ForumPost>) msg.obj;// (LinkedList<ForumPost>)
																			// msg.obj;
					Context context = activity.getActivity();
					if (activity.mListItems != null
							&& activity.mListItems.size() > 0) {
						activity.loadLayout.setVisibility(View.GONE);
						activity.reloadLayout.setVisibility(View.GONE);
						activity.nodataLayout.setVisibility(View.GONE);
						activity.isLastPage = activity.mListItems.size() < AppContext.PAGE_SIZE;
						activity.mAdapter = new ForumListAdapter(context,
								activity.mListItems, null);
						activity.mPullRefreshListView.getRefreshableView()
								.setAdapter(activity.mAdapter);
					} else {
						// 木有数据
						// activity.nodataLayout.setVisibility(View.VISIBLE);
						activity.loadLayout.setVisibility(View.GONE);
						activity.reloadLayout.setVisibility(View.GONE);
						activity.nodataLayout.setVisibility(View.VISIBLE);
					}
					break;
				case -1:
					// 加载出错重新加载
					activity.loadLayout.setVisibility(View.GONE);
					activity.nodataLayout.setVisibility(View.GONE);
					activity.reloadLayout.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
//
//	// 模拟获取数据
//	private ArrayList<ForumPost> getData() {
//		ArrayList<ForumPost> list = new ArrayList<ForumPost>();
//		for (int i = 0; i < 5; i++) {
//			ForumPost p = new ForumPost();
//			p.setAuthor("hahaha");
//			p.setAnswerCount(20);
//			p.setPubDate("20分钟前");
//			p.setViewCount(2301);
//			p.setTitle("考会计什么的,最烦躁了<IMG src='ic_picture'/>");
//			list.add(p);
//		}
//		return list;
//	}

	private class ItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(getActivity(),
					ForumPostDetailActivity.class);
			intent.putExtra("postId", mListItems.get(arg2 - 1).getId());
			startActivity(intent);

		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("UserInfo Fragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		if (appContext.getLoginState() == AppContext.LOGINING) // 正在登录,显示progress
		{
			if (mProDialog == null) {
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
							if (appContext.getLoginState() != AppContext.LOGINED) {
								nodataLayout.setVisibility(View.VISIBLE);
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
			} else {
				mProDialog.show();
			}
		}

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.e("UserInfo Fragment", "onStart " + appContext.getLoginState());
		super.onStart();
		MainActivity main = (MainActivity) getActivity();
		main.showFooter(View.GONE);
		initData();
	}

	private void initData() {
		if (appContext.getLoginState() == AppContext.LOGINED) {
			nodataLayout.setVisibility(View.GONE);
			nicknameTxt.setText(appContext.getUsername());
			String timeTxt = AppConfig.getAppConfig(getActivity())
					.getFormatExamTime();
			examTimeTxt.setText(timeTxt == null ? "未设置" : timeTxt);
			locationTxt.setText(AreaUtils.areaCName);
		}
		// 开一个线程去获取数据
		new GetDataThread().start();
	}
	private class GetDataThread extends Thread
	{
		public void run() {
			try {
				ArrayList<ForumPost> list = ApiClient
						.getForumPostListOfUser(appContext, 1,
								AppContext.PAGE_SIZE,
								appContext.getUsername(), mode);
				Message msg = mHandler.obtainMessage();
				msg.what = 1;
				msg.obj = list;
				mHandler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mHandler.sendEmptyMessage(-1); // 加载出错
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Context context = getActivity();
		switch (v.getId()) {
		case R.id.head_img:
			CharSequence[] items = { getString(R.string.img_from_album),
					getString(R.string.img_from_camera),
					getString(R.string.img_cancel_choose) };
//			imageChooseItem(items);
			break;
		case R.id.user_nickname_layout:
//			Intent intent1 = new Intent(context, UserInfoDetailActivity.class);
//			intent1.putExtra("username", appContext.getUsername());
//			intent1.putExtra("location", AreaUtils.areaCName);
//			startActivity(intent1);
			break;
		case R.id.exam_date_layout:
//			Intent intent2 = new Intent(context, SetTimeActivity.class);
//			startActivity(intent2);
			break;
		case R.id.user_location_layout:
			// UIHelper.ToastMessage(context, "正在定位");
			break;
		case R.id.gotoLogin:
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			intent.putExtra("loginFrom", LoginActivity.LOGIN_MAIN);
			startActivityForResult(intent, 10);
			break;
		case R.id.main_collect_btn:
			if (CURRENT_BTN_FLAG.equals(v.getTag())) {
				return;
			}
			favorBtn.setBackgroundResource(R.drawable.personal_info_pressed);
			favorBtn.setTag(1);
			postBtn.setBackgroundResource(R.drawable.personal_info_normal);
			postBtn.setTag(0);
			replyBtn.setBackgroundResource(R.drawable.personal_info_normal);
			replyBtn.setTag(0);
			mode = 2;
			new GetDataThread().start();
			break;
		case R.id.main_post_btn:
			if (CURRENT_BTN_FLAG.equals(v.getTag())) {
				return;
			}
			favorBtn.setBackgroundResource(R.drawable.personal_info_normal);
			favorBtn.setTag(0);
			postBtn.setBackgroundResource(R.drawable.personal_info_pressed);
			postBtn.setTag(1);
			replyBtn.setBackgroundResource(R.drawable.personal_info_normal);
			replyBtn.setTag(0);
			mode = 0;
			new GetDataThread().start();
			break;
		case R.id.main_reply_btn:
			if (CURRENT_BTN_FLAG.equals(v.getTag())) {
				return;
			}
			favorBtn.setBackgroundResource(R.drawable.personal_info_normal);
			favorBtn.setTag(0);
			postBtn.setBackgroundResource(R.drawable.personal_info_normal);
			postBtn.setTag(0);
			replyBtn.setBackgroundResource(R.drawable.personal_info_pressed);
			replyBtn.setTag(1);
			mode = 1;
			new GetDataThread().start();
			break;
		}
	}

	// 裁剪头像的绝对路径
	private Uri getUploadTempFile(Uri uri) {
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			File savedir = new File(FILE_SAVEPATH);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
		} else {
			UIHelper.ToastMessage(getActivity(), "无法保存上传的头像，请检查SD卡是否挂载");
			return null;
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date());
		String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(uri);

		// 如果是标准Uri
		if (StringUtils.isEmpty(thePath)) {
			thePath = ImageUtils.getAbsoluteImagePath(getActivity(), uri);
		}
		String ext = FileUtils.getFileFormat(thePath);
		ext = StringUtils.isEmpty(ext) ? "jpg" : ext;
		// 照片命名
		String cropFileName = "accountant_crop_" + timeStamp + "." + ext;
		// 裁剪头像的绝对路径
		protraitPath = FILE_SAVEPATH + cropFileName;
		protraitFile = new File(protraitPath);
		cropUri = Uri.fromFile(protraitFile);
		return this.cropUri;
	}

	// 拍照保存的绝对路径
	private Uri getCameraTempFile() {
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			File savedir = new File(FILE_SAVEPATH);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
		} else {
			UIHelper.ToastMessage(getActivity(), "无法保存上传的头像，请检查SD卡是否挂载");
			return null;
		}
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date());
		// 照片命名
		String cropFileName = "osc_camera_" + timeStamp + ".jpg";
		// 裁剪头像的绝对路径
		protraitPath = FILE_SAVEPATH + cropFileName;
		protraitFile = new File(protraitPath);
		cropUri = Uri.fromFile(protraitFile);
		this.origUri = this.cropUri;
		return this.cropUri;
	}

	/**
	 * 操作选择
	 * 
	 * @param items
	 */
	public void imageChooseItem(CharSequence[] items) {
		AlertDialog imageDialog = new AlertDialog.Builder(getActivity())
				.setTitle("上传头像").setIcon(android.R.drawable.btn_star)
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// 相册选图
						if (item == 0) {
							startImagePick();
						}
						// 手机拍照
						else if (item == 1) {
							startActionCamera();
						} else if (item == 2) {
							dialog.dismiss();
						}
					}
				}).create();

		imageDialog.show();
	}

	/**
	 * 选择图片裁剪
	 * 
	 * @param output
	 */
	private void startImagePick() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent, "选择图片"),
				ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
	}

	/**
	 * 相机拍照
	 * 
	 * @param output
	 */
	private void startActionCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, this.getCameraTempFile());
		startActivityForResult(intent,
				ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
	}

	/**
	 * 拍照后裁剪
	 * 
	 * @param data
	 *            原始图片
	 * @param output
	 *            裁剪后图片
	 */
	private void startActionCrop(Uri data) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(data, "image/*");
		intent.putExtra("output", this.getUploadTempFile(data));
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比例
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", CROP);// 输出图片大小
		intent.putExtra("outputY", CROP);
		intent.putExtra("scale", true);// 去黑边
		intent.putExtra("scaleUpIfNeeded", true);// 去黑边
		startActivityForResult(intent,
				ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
	}

	/**
	 * 上传新照片
	 */
	// private void uploadNewPhoto() {
	// final Handler handler = new Handler() {
	// public void handleMessage(Message msg) {
	// if (loading != null)
	// loading.dismiss();
	// if (msg.what == 1 && msg.obj != null) {
	// Result res = (Result) msg.obj;
	// // 提示信息
	// UIHelper.ToastMessage(UserInfo.this, res.getErrorMessage());
	// if (res.OK()) {
	// // 显示新头像
	// face.setImageBitmap(protraitBitmap);
	// }
	// } else if (msg.what == -1 && msg.obj != null) {
	// ((AppException) msg.obj).makeToast(UserInfo.this);
	// }
	// }
	// };
	//
	// if (loading != null) {
	// loading.setLoadText("正在上传头像···");
	// loading.show();
	// }
	//
	// new Thread() {
	// public void run() {
	// // 获取头像缩略图
	// if (!StringUtils.isEmpty(protraitPath) && protraitFile.exists()) {
	// protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath,
	// 200, 200);
	// } else {
	// loading.setLoadText("图像不存在，上传失败·");
	// loading.hide();
	// }
	//
	// if (protraitBitmap != null) {
	// Message msg = new Message();
	// try {
	// Result res = ((AppContext) getApplication())
	// .updatePortrait(protraitFile);
	// if (res != null && res.OK()) {
	// // 保存新头像到缓存
	// String filename = FileUtils.getFileName(user
	// .getFace());
	// ImageUtils.saveImage(UserInfo.this, filename,
	// protraitBitmap);
	// }
	// msg.what = 1;
	// msg.obj = res;
	// } catch (AppException e) {
	// loading.setLoadText("上传出错·");
	// loading.hide();
	// msg.what = -1;
	// msg.obj = e;
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// handler.sendMessage(msg);
	// } else {
	// loading.setLoadText("图像不存在，上传失败·");
	// loading.hide();
	// }
	// };
	// }.start();
	// }

	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
			startActionCrop(origUri);// 拍照后裁剪
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP:
			startActionCrop(data.getData());// 选图后裁剪
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			// uploadNewPhoto();// 上传新照片
			// 暂时只显示图片
			if (!StringUtils.isEmpty(protraitPath) && protraitFile.exists()) {
				protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath, 200,
						200);
			} else {
			}
			if (protraitBitmap != null) {
				headView.setImageBitmap(protraitBitmap);
			}
			break;
		}
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MainActivity main = (MainActivity) getActivity();
		main.showFooter(View.VISIBLE);
	}

}
