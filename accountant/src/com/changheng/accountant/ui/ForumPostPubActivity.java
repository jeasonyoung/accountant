package com.changheng.accountant.ui;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.GridViewFaceAdapter;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;
import com.changheng.accountant.util.MediaUtils;
import com.changheng.accountant.util.StringUtils;

public class ForumPostPubActivity extends BaseActivity implements
		OnClickListener {
	private GridViewFaceAdapter mGVFaceAdapter;
	private GridView mGridView;
	private EditText mContent;
	private EditText mTitle;
	private ImageView mFace;
	private ImageView mPick;
	private TextView mNumberwords;
	private LinearLayout mClearwords;

	private AlertDialog clearWordsDialog;
	private ProgressDialog pubingDialog;
	private Handler mHandler;
	private AppContext appContext;
	private InputMethodManager imm;

	private String theLarge;
	private File imgFile;
	private ImageView mImage;
	private String theThumbnail;
	private String tempTweetKey = AppConfig.TEMP_TWEET;
	private String tempTweetTitleKey = AppConfig.TMEP_TWEET_TITLE;
	private String tempTweetImageKey = AppConfig.TEMP_TWEET_IMAGE;
	private static final int MAX_TEXT_LENGTH = 500;// 最大输入字数
	private static final int MAX_TITLE_LENGTH = 50;// 标题最大字数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tweet_pub);
		appContext = (AppContext) this.getApplication();
		mHandler = new MyHandler(this);
		initViews();
		// initGridView();
	}

	// 初始化基本控件
	private void initViews() {
		// 键盘
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		// 标题
		mTitle = (EditText) this.findViewById(R.id.txt_title);
		// 内容
		mContent = (EditText) this.findViewById(R.id.txt_content);
		// 表情(暂时没有添加)
		mFace = (ImageView) findViewById(R.id.tweet_pub_footbar_face);
		// 插入图片
		mPick = (ImageView) findViewById(R.id.btn_photo);
		// 显示图片区域
		mImage = (ImageView) findViewById(R.id.tweet_pub_image);
		// 显示字数
		mNumberwords = (TextView) findViewById(R.id.tweet_pub_numberwords);
		// 清除文字
		mClearwords = (LinearLayout) findViewById(R.id.tweet_pub_clearwords);
		// 编辑器添加文本监听
		mTitle.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 保存当前EditText正在编辑的内容
				((AppContext) getApplication()).setProperty(tempTweetTitleKey,
						s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				mClearwords.setVisibility(View.GONE);
			}

			public void afterTextChanged(Editable s) {
			}
		});
		mContent.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 保存当前EditText正在编辑的内容
				((AppContext) getApplication()).setProperty(tempTweetKey,
						s.toString());
				// 显示剩余可输入的字数
				mNumberwords.setText((MAX_TEXT_LENGTH - s.length()) + "");
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				mClearwords.setVisibility(View.VISIBLE);
			}

			public void afterTextChanged(Editable s) {
			}
		});
		// 编辑器点击事件
		// 设置最大输入字数
		InputFilter[] content_filters = new InputFilter[1];
		content_filters[0] = new InputFilter.LengthFilter(MAX_TEXT_LENGTH);
		mContent.setFilters(content_filters);
		InputFilter[] title_filters = new InputFilter[1];
		title_filters[0] = new InputFilter.LengthFilter(MAX_TITLE_LENGTH);
		mTitle.setFilters(title_filters);
		// 显示临时编辑内容
		UIHelper.showTempEditContent(this, mContent, tempTweetKey);
		showTempTitleEditContent();
		// 显示临时保存图片
		String tempImage = ((AppContext) getApplication())
				.getProperty(tempTweetImageKey);
		if (!StringUtils.isEmpty(tempImage)) {
			Bitmap bitmap = ImageUtils.loadImgThumbnail(tempImage, 100, 100);
			if (bitmap != null) {
				imgFile = new File(tempImage);
				mImage.setImageBitmap(bitmap);
				mImage.setVisibility(View.VISIBLE);
			}
		}
		// 设置监听
		// mFace.setOnClickListener(faceClickListener);
//		mPick.setOnClickListener(this);
		findViewById(R.id.btn_goback).setOnClickListener(this);
		findViewById(R.id.tweet_pub_publish).setOnClickListener(this);
		mClearwords.setOnClickListener(this);
//		mImage.setOnClickListener(this);
//		mImage.setOnLongClickListener(imageLongClickListener);
//		mContent.setOnClickListener(this);
//		mTitle.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tweet_pub_clearwords:
			// 清除文字
			showClearWordsDialog();
			break;
		case R.id.btn_goback:
			// 返回
			this.finish();
			break;
		case R.id.tweet_pub_publish:
			// 发布
			pubPost();
			break;
		case R.id.btn_photo:
			pickPic();
			// 选择图片
			break;
		case R.id.tweet_pub_image:
			// 临时显示的小图片
			break;
		case R.id.txt_title:
			imm.showSoftInput(v, 0);
			mClearwords.setVisibility(View.GONE);
			break;
		case R.id.txt_content:
			imm.showSoftInput(v, 0);
			mClearwords.setVisibility(View.VISIBLE);
			break;
		}
	}

	// 清除文字
	private void showClearWordsDialog() {
		if (StringUtils.isEmpty(mContent.getText().toString())) {
			return;
		}
		if (clearWordsDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("清除文字吗");
			// builder.setIcon(icon);
			builder.setPositiveButton(R.string.sure,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							// 清除文字
							mContent.setText("");
							mNumberwords.setText(String
									.valueOf(MAX_TEXT_LENGTH));
						}
					});
			builder.setNegativeButton(R.string.cancle,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			clearWordsDialog = builder.create();
		}
		clearWordsDialog.show();
	}

	private void pickPic() {
		// 隐藏软键盘
		imm.hideSoftInputFromWindow(mContent.getWindowToken(), 0);
		// 隐藏表情
		CharSequence[] items = {
				ForumPostPubActivity.this.getString(R.string.img_from_album),
				ForumPostPubActivity.this.getString(R.string.img_from_camera) };
		imageChooseItem(items);
	}
	//发布帖子
	private void pubPost()
	{
		//检测输入
		String title = mTitle.getText().toString();
		String content = mContent.getText().toString();
		if(title.length()<=0)
		{
			Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
			return;
		}
		if(content.length()<=5)
		{
			Toast.makeText(this, "字数不够", Toast.LENGTH_SHORT).show();
			return;
		}
		final ForumPost post = new ForumPost();
		post.setTitle(title);
		post.setBody(content);
		post.setAuthor(appContext.getUsername());
		//开线程发帖子
		if(pubingDialog==null)
		{
			pubingDialog = new ProgressDialog(this);//ProgressDialog.show(this, null, "提交中...", true, true);
			pubingDialog.setMessage("提交中...");
			pubingDialog.setIndeterminate(true);
			pubingDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progressbar_style_icon));
		}
		pubingDialog.show();
		//发布成功就finish
		new Thread(){
			public void run() {
				try
				{
					ParseResult result= ApiClient.pubTweet2(appContext, post);
					Message msg = mHandler.obtainMessage();
					msg.what = 1;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}catch(Exception e)
				{
					e.printStackTrace();
					mHandler.sendEmptyMessage(-1);
				}
			};
		}.start();
		//发布失败提示错误
	}
	private void showTempTitleEditContent() {
		String tempContent = ((AppContext) this.getApplication()).getProperty(tempTweetTitleKey);
		if (!StringUtils.isEmpty(tempContent)) {
			mTitle.setText(tempContent);
		}
	}
	// 初始化表情控件
	private void initGridView() {
		mGVFaceAdapter = new GridViewFaceAdapter(this);
		mGridView = (GridView) findViewById(R.id.tweet_pub_faces);
		mGridView.setAdapter(mGVFaceAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 插入的表情
				SpannableString ss = new SpannableString(view.getTag()
						.toString());
				Drawable d = getResources().getDrawable(
						(int) mGVFaceAdapter.getItemId(position));
				d.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				ss.setSpan(span, 0, view.getTag().toString().length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// 在光标所在处插入表情
				mContent.getText().insert(mContent.getSelectionStart(), ss);
			}
		});
	}

	private void showIMM() {
		mFace.setTag(1);
		showOrHideIMM();
	}

	private void showFace() {
		mFace.setImageResource(R.drawable.widget_bar_keyboard);
		mFace.setTag(1);
		mGridView.setVisibility(View.VISIBLE);
	}

	private void hideFace() {
		mFace.setImageResource(R.drawable.widget_bar_face);
		mFace.setTag(null);
		mGridView.setVisibility(View.GONE);
	}

	private void showOrHideIMM() {
		if (mFace.getTag() == null) {
			// 隐藏软键盘
			imm.hideSoftInputFromWindow(mContent.getWindowToken(), 0);
			// 显示表情
			showFace();
		} else {
			// 显示软键盘
			imm.showSoftInput(mContent, 0);
			// 隐藏表情
			hideFace();
		}
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
										ForumPostPubActivity.this,
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
					mImage.setImageBitmap((Bitmap) msg.obj);
					mImage.setVisibility(View.VISIBLE);
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
								ForumPostPubActivity.this, thisUri);
					} else {
						theLarge = thePath;
					}

					String attFormat = FileUtils.getFileFormat(theLarge);
					if (!"photo".equals(MediaUtils.getContentType(attFormat))) {
						Toast.makeText(ForumPostPubActivity.this,
								"请选择图片文件！",
								Toast.LENGTH_SHORT).show();
						return;
					}

					// 获取图片缩略图 只有Android2.1以上版本支持
					if (AppContext
							.isMethodsCompat(android.os.Build.VERSION_CODES.ECLAIR_MR1)) {
						String imgName = FileUtils.getFileName(theLarge);
						bitmap = ImageUtils.loadImgThumbnail(ForumPostPubActivity.this,
								imgName,
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
								ImageUtils.createImageThumbnail(ForumPostPubActivity.this,
										theLarge, theThumbnail, 800, 80);
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
			//隐藏软键盘
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
			
			new AlertDialog.Builder(v.getContext())
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle("是否删除图片?")
			.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//清除之前保存的编辑图片
					((AppContext)getApplication()).removeProperty(tempTweetImageKey);
					
					imgFile = null;
					mImage.setVisibility(View.GONE);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create().show();
			return true;
		}
	};
	static class MyHandler extends Handler
	{
		private WeakReference<ForumPostPubActivity> weak;
		public MyHandler(ForumPostPubActivity context) {
			// TODO Auto-generated constructor stub
			weak = new WeakReference<ForumPostPubActivity>(context);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			ForumPostPubActivity activity = weak.get();
			activity.pubingDialog.dismiss();
			switch(msg.what)
			{
			case 1:
				ParseResult result = (ParseResult) msg.obj;
				if(result.Ok())
				{
					Toast.makeText(activity, "发帖成功", Toast.LENGTH_SHORT).show();
					//清除临时的内容
					activity.appContext.removeProperty(AppConfig.TEMP_TWEET,AppConfig.TMEP_TWEET_TITLE,AppConfig.TEMP_TWEET_IMAGE);
					activity.finish();
					break;
				}else if(result.getErrorCode()==-1) //没有登录
				{
					Toast.makeText(activity, "登录已失效,请重新登录", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(activity,LoginActivity.class);
					activity.startActivity(intent);
				}
			case -1:
				Toast.makeText(activity, "发帖失败", Toast.LENGTH_SHORT).show();
				break;
			}
			
		}
		
	}
}
