package com.changheng.accountant.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.ForumPostReplyListAdapter;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.entity.ForumPostReply;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.ReplyList;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.BitmapManager;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;
import com.changheng.accountant.util.MediaUtils;
import com.changheng.accountant.util.StringUtils;
import com.changheng.accountant.view.KeyboardListenRelativeLayout;
import com.changheng.accountant.view.KeyboardListenRelativeLayout.IOnKeyboardStateChangedListener;
import com.changheng.accountant.view.TextWithLocalDrawableView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@SuppressLint("JavascriptInterface")
public class ForumPostDetailActivity extends BaseActivity implements
		OnClickListener {
	private View lvHeader; // 帖子详情作为头部
	// 帖子头部的一些组件
	private TextView headViewCount, headReplyCount, headFrom, headArea,
			headContentTv, headAddTime, headUsername;
	private ImageView headFace;
	private Button headDeleteBtn;

	private PullToRefreshListView mListView;
	private KeyboardListenRelativeLayout keyboardListenLayout;
	private RelativeLayout addComentBtn;
	private LinearLayout addComentLayout, loadingLayout, reloadLayout;
	private EditText editText;
	private PopupWindow menuPop;
	private ForumPostReplyListAdapter adapter;
	private WebView contentWebView;
	private TextWithLocalDrawableView postTitle;
	private ImageView addImageBtn;

	private InputMethodManager imm;

	private AppContext appContext;

	private String theLarge;
	private File imgFile;
	private String theThumbnail;
	private String tempTweetImageKey = AppConfig.TEMP_REPLY_IMAGE;

	private String postId; // 帖子ID
	private ForumPost post = null;
	private Handler mHandler;
	private ArrayList<ForumPostReply> replyList;
	private static final int PAGESIZE = 20;
	private int replySum;
	private int curPage = 1;
	private ForumPostReply reply; // 带发送的回复对象
	private BitmapManager bmpManager;
	private String tempTweetReplyKey = AppConfig.TEMP_COMMENT;
	private static final int MAX_TEXT_LENGTH = 200;
	private ProgressDialog pubingDialog;
	private int replyFloor;
	private String replyTo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_forum_post_detail);
		appContext = (AppContext) this.getApplication();
		this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(
				this.getResources(), R.drawable.img_head_default));
		// 软键盘管理类
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		initViews();
		initData();
	}

	private void initViews() {
		this.mListView = (PullToRefreshListView) this
				.findViewById(R.id.answerList);
		this.keyboardListenLayout = (KeyboardListenRelativeLayout) this
				.findViewById(R.id.keyboardRelativeLayout);
		this.addComentBtn = (RelativeLayout) this
				.findViewById(R.id.layout_add_comment2);
		this.addComentLayout = (LinearLayout) this
				.findViewById(R.id.layout_add_comment);
		this.loadingLayout = (LinearLayout) this
				.findViewById(R.id.loadingLayout);
		this.reloadLayout = (LinearLayout) this.findViewById(R.id.reload);
		this.addImageBtn = (ImageView) this.findViewById(R.id.btn_take_photo);
		this.addImageBtn.setOnClickListener(this);
		this.addImageBtn.setOnLongClickListener(imageLongClickListener);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.findViewById(R.id.more).setOnClickListener(this);
		this.findViewById(R.id.btn_send_comment).setOnClickListener(this);
		this.findViewById(R.id.btn_reload).setOnClickListener(this);
		// this.moreBtn.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// // TODO Auto-generated method stub
		// System.out.println("menu touch "+event.getAction());
		// if(event.getAction()==MotionEvent.ACTION_OUTSIDE)
		// showPopWindow(v);
		// return true;
		// }
		// });
		this.addComentBtn.setOnClickListener(this);
		this.editText = (EditText) this.findViewById(R.id.et_add_comment);
		editText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 保存当前EditText正在编辑的内容
				((AppContext) getApplication()).setProperty(tempTweetReplyKey,
						s.toString());
				// 显示剩余可输入的字数
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		// 编辑器点击事件
		// 设置最大输入字数
		InputFilter[] content_filters = new InputFilter[1];
		content_filters[0] = new InputFilter.LengthFilter(MAX_TEXT_LENGTH);
		editText.setFilters(content_filters);
		// 显示临时编辑内容
		UIHelper.showTempEditContent(this, editText, tempTweetReplyKey);
		// 显示临时保存图片
		String tempImage = ((AppContext) getApplication())
				.getProperty(tempTweetImageKey);
		if (!StringUtils.isEmpty(tempImage)) {
			Bitmap bitmap = ImageUtils.loadImgThumbnail(tempImage, 100, 100);
			if (bitmap != null) {
				imgFile = new File(tempImage);
				addImageBtn.setImageBitmap(bitmap);
				addImageBtn.setVisibility(View.VISIBLE);
			}
		}
		// 初始化头部帖子
		initHeadView();
		// 设置下拉list
		this.mListView.setMode(Mode.BOTH);
		// 下拉刷新监听
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				// Do work to refresh the list here.
				switch (refreshView.getCurrentMode()) {
				case PULL_FROM_START: // 来自顶部的刷新
					if (curPage > 1) {
						curPage = curPage - 1;
					}
					break;
				case PULL_FROM_END:
					if (curPage < replySum / PAGESIZE + 1) {
						curPage = curPage + 1;
					}
				}
				new GetDataTask().execute();
			}
		});
		// 软键盘监听
		keyboardListenLayout
				.setOnKeyboardStateChangedListener(new IOnKeyboardStateChangedListener() {
					@Override
					public void onKeyboardStateChanged(int state) {
						// TODO Auto-generated method stub
						switch (state) {
						case KeyboardListenRelativeLayout.KEYBOARD_STATE_HIDE:
							addComentBtn.setVisibility(View.VISIBLE);
							addComentLayout.setVisibility(View.GONE);
							break;
						case KeyboardListenRelativeLayout.KEYBOARD_STATE_SHOW:
							break;
						}
					}
				});
	}

	private void initHeadView() {
		lvHeader = View.inflate(this, R.layout.forum_detail_content, null);
		this.contentWebView = (WebView) lvHeader
				.findViewById(R.id.news_detail_webview);
		this.headContentTv = (TextView) lvHeader
				.findViewById(R.id.post_content_tv);
		this.postTitle = (TextWithLocalDrawableView) lvHeader
				.findViewById(R.id.topic_list_header_title_item);
		this.headAddTime = (TextView) lvHeader
				.findViewById(R.id.topic_user_time);
		this.headArea = (TextView) lvHeader.findViewById(R.id.topic_user_city);
		this.headFrom = (TextView) lvHeader.findViewById(R.id.txt_topic_group);
		this.headReplyCount = (TextView) lvHeader
				.findViewById(R.id.topic_user_reply_count);
		this.headViewCount = (TextView) lvHeader
				.findViewById(R.id.topic_user_view_count);
		this.headUsername = (TextView) lvHeader
				.findViewById(R.id.topic_user_name);
		this.headFace = (ImageView) lvHeader
				.findViewById(R.id.topic_user_avator);
		this.headDeleteBtn = (Button) lvHeader
				.findViewById(R.id.topic_user_del);
		this.headDeleteBtn.setOnClickListener(this);
		this.headFace.setOnClickListener(this);
		contentWebView.getSettings().setJavaScriptEnabled(true);
		// 设置显示图片适应屏幕
		contentWebView.getSettings().setLayoutAlgorithm(
				LayoutAlgorithm.SINGLE_COLUMN);
		contentWebView.setWebViewClient(new SampleWebViewClient());
		// contentWebView.loadUrl("http://www.babytree.com/community/topic_mobile.php?id=42052");//
		// contentWebView.loadUrl("file:///android_asset/other/about.html");
		contentWebView.addJavascriptInterface(new OnWebViewImageListener() {
			@Override
			public void onImageClick(String bigImageUrl) {
				// if (bigImageUrl != null)
				// UIHelper.showImageZoomDialog(cxt, bigImageUrl);
				System.out.println("iamgeUrl = " + bigImageUrl);
				Toast.makeText(ForumPostDetailActivity.this, bigImageUrl,
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(ForumPostDetailActivity.this,
						ImageZoomActivity.class);
				intent.putExtra("imgUrl", bigImageUrl);
				ForumPostDetailActivity.this.startActivity(intent);
			}
		}, "mWebViewImageListener");
	}

	private void initData() {
		// 开线程加载数据
		postId = getIntent().getStringExtra("postId");
		reloadLayout.setVisibility(View.GONE);
		keyboardListenLayout.setVisibility(View.GONE);
		loadingLayout.setVisibility(View.VISIBLE);
		mHandler = new MyHandler(this);
		new Thread() {
			public void run() {
				try {
					post = ApiClient.getPostDetail(appContext, postId);
					if (post != null) {
						ReplyList list = ApiClient.getReplyList(appContext,
								curPage, PAGESIZE, postId);
						replyList = (ArrayList<ForumPostReply>) list
								.getReplyList();
						replySum = list.getCommentCount();
						System.out.println(replyList);
					}
					Message msg = mHandler.obtainMessage();
					msg.what = 1;
					msg.obj = post;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					if (post != null) {

					}
					mHandler.sendEmptyMessage(-1);
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
			break;
		case R.id.more:
			showPopWindow(v);
			break;
		case R.id.layout_add_comment2:
			showKeyboard(null, 0, null);
			break;
		case R.id.btn_take_photo:
			addImage();
			break;
		case R.id.btn_send_comment:
			sendComment();
			break;
		case R.id.btn_reload:
			initData();
			break;
		case R.id.topic_user_del:
			deletePost();
			break;
		case R.id.topic_user_avator:
			gotoUserDetailActivity();
			break;
		}
	}

	private void deletePost() {
		// 提示是否删除
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("是否确定删除该帖?")
				.setPositiveButton(R.string.sure,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								// 开线程去删除
								if (pubingDialog == null) {
									pubingDialog = new ProgressDialog(
											ForumPostDetailActivity.this);// ProgressDialog.show(this,
									// null, "提交中...", true,
									// true);
									pubingDialog.setMessage("提交请求中...");
									pubingDialog.setIndeterminate(true);
									pubingDialog
											.setIndeterminateDrawable(getResources()
													.getDrawable(
															R.drawable.progressbar_style_icon));
								}
								pubingDialog.show();
								// 发送
								new Thread() {
									public void run() {
										try {
											ParseResult result = ApiClient
													.deleteTweet(appContext,
															post);
											Message msg = mHandler
													.obtainMessage();
											msg.what = 8;
											msg.obj = result;
											mHandler.sendMessage(msg);
										} catch (Exception e) {
											e.printStackTrace();
											mHandler.sendEmptyMessage(-8);
										}
									};
								}.start();
							}
						})
				.setNegativeButton(R.string.cancle,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create().show();
	}

	private void gotoUserDetailActivity() {
		UIHelper.showUserCenter(this, post.getFace(), post.getAuthor(),
				post.getArea());
	}

	private void sendComment() {
		// 检测输入
		String content = editText.getText().toString();
		if (StringUtils.isEmpty(content) || content.length() < 3) {
			Toast.makeText(this, "请输入至少3个字", Toast.LENGTH_SHORT).show();
			return;
		}
		// 检测有木有登录
		if (appContext.getLoginState() != AppContext.LOGINED) {
			// 没有登录跳转到登录界面
			Intent intent = new Intent(this, LoginActivity.class);
			intent.putExtra("loginFrom", LoginActivity.LOGIN_POST_REPLY);
			startActivityForResult(intent, 10);
			return;
		}
		// 构造回复对象
		if (reply == null) {
			reply = new ForumPostReply();
		}
		reply.setPostId(postId);
		reply.setContent(content);
		reply.setReplyAuthor(appContext.getUsername());
		reply.setReplyFloor(replyFloor); // 回复几楼
		reply.setReplyTo(replyTo); // 回复谁
		// 开线程发帖子
		if (pubingDialog == null) {
			pubingDialog = new ProgressDialog(this);// ProgressDialog.show(this,
													// null, "提交中...", true,
													// true);
			pubingDialog.setMessage("提交请求中...");
			pubingDialog.setIndeterminate(true);
			pubingDialog.setIndeterminateDrawable(getResources().getDrawable(
					R.drawable.progressbar_style_icon));
		}
		pubingDialog.show();
		// 发送
		new Thread() {
			public void run() {
				try {
					ParseResult result = ApiClient.replyTweet2(appContext,
							reply);
					Message msg = mHandler.obtainMessage();
					msg.what = 4;
					msg.obj = result;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(-4);
				}
			};
		}.start();

	}

	private void showPopWindow(View parent) {
		if (menuPop == null) {
			View v = LayoutInflater.from(this).inflate(
					R.layout.topic_beta_more_menu, null);
			ListView listView = (ListView) v
					.findViewById(R.id.forum_menu_listView1);
			SampleAdapter adapter = new SampleAdapter(this);
			adapter.add(new SampleItem("分享", R.drawable.btn_icon_fenxiang));
			// adapter.add(new SampleItem("收藏",
			// R.drawable.btn_icon_shoucang_no));
			adapter.add(new SampleItem("跳页", R.drawable.btn_icon_fanye));
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

	public void showKeyboard(String hint, int floor, String name) {
		addComentLayout.setVisibility(View.VISIBLE);
		addComentBtn.setVisibility(View.GONE);
		editText.requestFocus();
		editText.requestFocusFromTouch();
		if (hint == null) {
			editText.setHint("回复楼主:");
		} else {
			editText.setHint(hint);
		}
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.showSoftInput(editText, 0);
		replyFloor = floor;
		replyTo = name;
		System.out.println(replyFloor + "  ... " + replyTo);
	}

	private class GetDataTask extends AsyncTask<Void, Void, ReplyList> {
		@Override
		protected ReplyList doInBackground(Void... params) {
			// Simulates a background job.
			try {
				return ApiClient.getReplyList(appContext, curPage, PAGESIZE,
						postId);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(ReplyList result) {
			if (result != null) {
				if (result.getReplyList().size() > 0) {
					replyList.clear();
					replyList.addAll(result.getReplyList());
				}
				// 修改头部帖子的数量
				headReplyCount
						.setText(String.valueOf(result.getCommentCount()));
				adapter.notifyDataSetChanged();
			}
			// Call onRefreshComplete when the list has been refreshed.
			mListView.onRefreshComplete();
			super.onPostExecute(result);
		}
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
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row, null);
			}
			TextView title = (TextView) convertView
					.findViewById(R.id.menu_item);
			title.setCompoundDrawablesWithIntrinsicBounds(
					getItem(position).iconRes, 0, 0, 0);
			title.setText(getItem(position).tag);
			return convertView;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ((event.getKeyCode() == 4) && event.getRepeatCount() == 0) {
			if (menuPop != null && menuPop.isShowing()) {
				menuPop.dismiss();
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MenuItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			menuPop.dismiss();
			switch (arg2) {
			case 0:
				share();
				break;
			case 2:
				Toast.makeText(ForumPostDetailActivity.this, "收藏",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				// 跳页
				turnToOtherPage();
				break;
			}
		}
	}

	// 分享
	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, "话题分享");
		intent.putExtra(Intent.EXTRA_TEXT,
				post.getBody()+"(分享自考试一点通(会计))");
//		try{
//			String filePath = Environment.getExternalStorageDirectory().getPath()+File.separator+"CHAccountant"+File.separator;
//			File mRoot = new File(filePath);
//			if(!mRoot.exists()) mRoot.mkdirs();
//			File logo = new File(filePath+"logo.png");
//			if(!logo.exists())
//			{
//				FileOutputStream fos = new FileOutputStream(logo);
//				InputStream in = getAssets().open("tem_logo.png");
//				byte[] buf = new byte[1024];
//				int count = 0;
//				while((count = in.read(buf))!=-1)
//				{
//					fos.write(buf,0,count);
//				}
//				fos.flush();
//				fos.close();
//				in.close();
//			}
//			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logo));
//		}catch(Exception e)
//		{
//			e.printStackTrace();
//		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent,getResources().getString(R.string.app_name)));
	}

	private void turnToOtherPage() {
		int totalPage = (replySum / PAGESIZE) + 1;
		CharSequence[] items = new CharSequence[totalPage];
		for (int i = 0; i < totalPage; i++) {
			items[i] = String.valueOf(i + 1);
		}
		AlertDialog pageDialog = new AlertDialog.Builder(this).setItems(items,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						curPage = item + 1;
						initData();
					}
				}).create();
		pageDialog.show();
	}

	private class SampleWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains("file:///") == true) {
				view.loadUrl(url);
				return true;
			} else {
				Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(in);
				return true;
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			// loading.setVisibility(View.GONE);
			super.onPageFinished(view, url);
		}
	}

	private void addImage() {
		CharSequence[] items = { this.getString(R.string.img_from_album),
				this.getString(R.string.img_from_camera) };
		imageChooseItem(items);
	}

	/**
	 * 点击图片时的操作选择
	 * 
	 * @param items
	 */
	public void imageChooseItem(CharSequence[] items) {
		AlertDialog imageDialog = new AlertDialog.Builder(this)
				.setTitle("插入图片").setIcon(android.R.drawable.btn_star)
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// 手机选图
						if (item == 0) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							startActivityForResult(
									Intent.createChooser(intent, "选择图片"),
									ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
						}
						// 拍照
						else if (item == 1) {
							String savePath = "";
							// 判断是否挂载了SD卡
							String storageState = Environment
									.getExternalStorageState();
							if (storageState.equals(Environment.MEDIA_MOUNTED)) {
								savePath = Environment
										.getExternalStorageDirectory()
										.getAbsolutePath()
										+ "/OSChina/Camera/";// 存放照片的文件夹
								File savedir = new File(savePath);
								if (!savedir.exists()) {
									savedir.mkdirs();
								}
							}

							// 没有挂载SD卡，无法保存文件
							if (StringUtils.isEmpty(savePath)) {
								UIHelper.ToastMessage(
										ForumPostDetailActivity.this,
										"无法保存照片，请检查SD卡是否挂载");
								return;
							}

							String timeStamp = new SimpleDateFormat(
									"yyyyMMddHHmmss").format(new Date());
							String fileName = "osc_" + timeStamp + ".jpg";// 照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);

							theLarge = savePath + fileName;// 该照片的绝对路径

							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent,
									ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}
					}
				}).create();

		imageDialog.show();
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (resultCode != RESULT_OK)
			return;
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					// 显示图片
					addImageBtn.setImageBitmap((Bitmap) msg.obj);
					addImageBtn.setVisibility(View.VISIBLE);
					// 显示软键盘
					addComentBtn.setVisibility(View.GONE);
					addComentLayout.setVisibility(View.VISIBLE);
					editText.requestFocus();
				}
			}
		};

		new Thread() {
			public void run() {
				Bitmap bitmap = null;

				if (requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD) {
					if (data == null)
						return;

					Uri thisUri = data.getData();
					String thePath = ImageUtils
							.getAbsolutePathFromNoStandardUri(thisUri);

					// 如果是标准Uri
					if (StringUtils.isEmpty(thePath)) {
						theLarge = ImageUtils.getAbsoluteImagePath(
								ForumPostDetailActivity.this, thisUri);
					} else {
						theLarge = thePath;
					}

					String attFormat = FileUtils.getFileFormat(theLarge);
					if (!"photo".equals(MediaUtils.getContentType(attFormat))) {
						Toast.makeText(ForumPostDetailActivity.this,
								"请选择图片文件！", Toast.LENGTH_SHORT).show();
						return;
					}

					// 获取图片缩略图 只有Android2.1以上版本支持
					if (AppContext
							.isMethodsCompat(android.os.Build.VERSION_CODES.ECLAIR_MR1)) {
						String imgName = FileUtils.getFileName(theLarge);
						bitmap = ImageUtils.loadImgThumbnail(
								ForumPostDetailActivity.this, imgName,
								MediaStore.Images.Thumbnails.MICRO_KIND);
					}

					if (bitmap == null && !StringUtils.isEmpty(theLarge)) {
						bitmap = ImageUtils
								.loadImgThumbnail(theLarge, 100, 100);
					}
				}
				// 拍摄图片
				else if (requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA) {
					if (bitmap == null && !StringUtils.isEmpty(theLarge)) {
						bitmap = ImageUtils
								.loadImgThumbnail(theLarge, 100, 100);
					}
				}

				if (bitmap != null) {
					// 存放照片的文件夹
					String savePath = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/CHAccountant/Camera/";
					File savedir = new File(savePath);
					if (!savedir.exists()) {
						savedir.mkdirs();
					}

					String largeFileName = FileUtils.getFileName(theLarge);
					String largeFilePath = savePath + largeFileName;
					// 判断是否已存在缩略图
					if (largeFileName.startsWith("thumb_")
							&& new File(largeFilePath).exists()) {
						theThumbnail = largeFilePath;
						imgFile = new File(theThumbnail);
					} else {
						// 生成上传的800宽度图片
						String thumbFileName = "thumb_" + largeFileName;
						theThumbnail = savePath + thumbFileName;
						if (new File(theThumbnail).exists()) {
							imgFile = new File(theThumbnail);
						} else {
							try {
								// 压缩上传的图片
								ImageUtils.createImageThumbnail(
										ForumPostDetailActivity.this, theLarge,
										theThumbnail, 800, 80);
								imgFile = new File(theThumbnail);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					// 保存动弹临时图片
					((AppContext) getApplication()).setProperty(
							tempTweetImageKey, theThumbnail);

					Message msg = new Message();
					msg.what = 1;
					msg.obj = bitmap;
					handler.sendMessage(msg);
				}
			};
		}.start();
	}

	private View.OnLongClickListener imageLongClickListener = new View.OnLongClickListener() {
		public boolean onLongClick(View v) {
			// 隐藏软键盘
			// imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			if (theThumbnail == null)
				return true;
			new AlertDialog.Builder(v.getContext())
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle("是否删除图片?")
					.setPositiveButton(R.string.sure,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// 清除之前保存的编辑图片
									((AppContext) getApplication())
											.removeProperty(tempTweetImageKey);
									imgFile = null;
									theThumbnail = null;
									addImageBtn
											.setImageResource(R.drawable.btn_addpic_new);
									dialog.dismiss();
								}
							})
					.setNegativeButton(R.string.cancle,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create().show();
			return true;
		}
	};

	static class MyHandler extends Handler {
		private WeakReference<ForumPostDetailActivity> wr;

		public MyHandler(ForumPostDetailActivity activity) {
			// TODO Auto-generated constructor stub
			wr = new WeakReference<ForumPostDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			ForumPostDetailActivity aty = wr.get();
			if (aty.pubingDialog != null && aty.pubingDialog.isShowing()) {
				aty.pubingDialog.dismiss();
			}
			switch (msg.what) {
			case 1:
				aty.loadingLayout.setVisibility(View.GONE);
				aty.reloadLayout.setVisibility(View.GONE);
				aty.keyboardListenLayout.setVisibility(View.VISIBLE);
				ListView realListView = aty.mListView.getRefreshableView();
				if (msg.obj != null && aty.adapter == null) {
					ForumPost post = (ForumPost) msg.obj;
					initHeadViewData(aty, post);
					realListView.addHeaderView(aty.lvHeader);
				}
				// if(aty.replyList.size()>0)
				// {
				aty.headReplyCount.setText(aty.replySum + "");
				if (aty.adapter == null) {
					aty.adapter = new ForumPostReplyListAdapter(aty,
							aty.replyList, aty.bmpManager);
					realListView.setAdapter(aty.adapter);
				} else {
					aty.adapter.notifyDataSetChanged();
				}
				// }else
				// {
				// aty.keyboardListenLayout.setVisibility(View.VISIBLE);
				// }
				break;
			case -1:
				// 加载出错重新加载
				aty.loadingLayout.setVisibility(View.GONE);
				aty.reloadLayout.setVisibility(View.VISIBLE);
				break;
			case 4:
				ParseResult result = (ParseResult) msg.obj;
				if (result.Ok()) {
					if (aty.imm.isActive()) {
						aty.imm.hideSoftInputFromWindow(
								aty.editText.getWindowToken(), 0);
					}
					// 清除临时的内容
					aty.appContext.removeProperty(AppConfig.TEMP_COMMENT,
							AppConfig.TEMP_REPLY_IMAGE);
					Toast.makeText(aty, "回帖成功", Toast.LENGTH_SHORT).show();
					// 如果是最后一页,刷新列表
					if (aty.replyList.size() < PAGESIZE) {
						// 刷新
						aty.new GetDataTask().execute();
					}
				}else if(result.getErrorCode()==-1) //没有登录
				{
					Toast.makeText(aty, "您的登录已失效,请重新登录", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(aty,LoginActivity.class);
					aty.startActivity(intent);
				}
				break;
			case -4:
				Toast.makeText(aty, "回帖失败", Toast.LENGTH_SHORT).show();
				break;
			case 8:
				ParseResult result2 = (ParseResult) msg.obj;
				if (result2.Ok()) {
					if (aty.imm.isActive()) {
						aty.imm.hideSoftInputFromWindow(
								aty.editText.getWindowToken(), 0);
					}
					Toast.makeText(aty, "删帖帖成功", Toast.LENGTH_SHORT).show();
					// 如果是最后一页,刷新列表
					aty.finish();
				}else if(result2.getErrorCode()==-1) //没有登录
				{
					Toast.makeText(aty, "您的登录已失效,请重新登录", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(aty,LoginActivity.class);
					aty.startActivity(intent);
				}
				break;
			case -8:
				Toast.makeText(aty, "删帖失败", Toast.LENGTH_SHORT).show();
				break;

			}
		}

		private void initHeadViewData(ForumPostDetailActivity aty,
				ForumPost post) {
			// TODO Auto-generated method stub
			// 标题
			aty.postTitle.setEmojiText(post.getTitle());
			// 查看次数,回复次数
			aty.headReplyCount.setText(String.valueOf(post.getAnswerCount()));
			aty.headViewCount.setText(String.valueOf(post.getViewCount()));
			aty.headAddTime.setText(post.getPubDate());
			aty.headArea.setText(post.getArea());
			aty.headUsername.setText(post.getAuthor());
			String faceURL = post.getFace();
			if (StringUtils.isEmpty(faceURL)) {
				aty.headFace.setImageResource(R.drawable.img_head_default);
			} else if (faceURL.endsWith("portrait.gif")) {
				aty.headFace.setImageResource(R.drawable.img_head_default);
			} else {
				aty.bmpManager.loadBitmap(faceURL, aty.headFace);
			}
			if (post.getAuthor().equals(aty.appContext.getUsername())) {
				aty.headDeleteBtn.setVisibility(View.VISIBLE);
			} else {
				aty.headDeleteBtn.setVisibility(View.GONE);
			}
			// 帖子内容
			String body = post.getBody();
			if (isContainImg(body)) {
				aty.headContentTv.setVisibility(View.GONE);
				aty.contentWebView.loadDataWithBaseURL(null, body, "text/html",
						"utf-8", null);
			} else {
				aty.headContentTv.setText(body);
				aty.contentWebView.setVisibility(View.GONE);
			}
		}

		private boolean isContainImg(String content) {
			// 不包含图片直接就用TextView了
			boolean bool1 = Pattern
					.compile("[\\s\\S]*(<img[^>]+src=\")(\\S+)\"(/?>)")
					.matcher(content).matches();
			boolean bool2 = Pattern
					.compile("[\\s\\S]*(<IMG[^>]+src=\")(\\S+)\"(/?>)")
					.matcher(content).matches();
			return bool1 || bool2;

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		appContext.removeProperty(AppConfig.TEMP_COMMENT,
				AppConfig.TEMP_REPLY_IMAGE);
	}
}
