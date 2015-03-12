package com.changheng.accountant.ui;


import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.AppManager;
import com.changheng.accountant.R;
import com.changheng.accountant.adapter.GridViewFaceAdapter;
import com.changheng.accountant.entity.Notice;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;
import com.changheng.accountant.util.StringUtils;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UIHelper {

	public final static int LISTVIEW_ACTION_INIT = 0x01;
	public final static int LISTVIEW_ACTION_REFRESH = 0x02;
	public final static int LISTVIEW_ACTION_SCROLL = 0x03;
	public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

	public final static int LISTVIEW_DATA_MORE = 0x01;
	public final static int LISTVIEW_DATA_LOADING = 0x02;
	public final static int LISTVIEW_DATA_FULL = 0x03;
	public final static int LISTVIEW_DATA_EMPTY = 0x04;

	public final static int LISTVIEW_DATATYPE_NEWS = 0x01;
	public final static int LISTVIEW_DATATYPE_BLOG = 0x02;
	public final static int LISTVIEW_DATATYPE_POST = 0x03;
	public final static int LISTVIEW_DATATYPE_TWEET = 0x04;
	public final static int LISTVIEW_DATATYPE_ACTIVE = 0x05;
	public final static int LISTVIEW_DATATYPE_MESSAGE = 0x06;
	public final static int LISTVIEW_DATATYPE_COMMENT = 0x07;

	public final static int REQUEST_CODE_FOR_RESULT = 0x01;
	public final static int REQUEST_CODE_FOR_REPLY = 0x02;

	/** 表情图片匹配 */
	private static Pattern facePattern = Pattern
			.compile("\\[{1}([0-9]\\d*)\\]{1}");

	/** 全局web样式 */
	public final static String WEB_STYLE = "<style>* {font-size:16px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

	/**
	 * 显示登录页面
	 * 
	 * @param activity
	 */
	public static void showLoginDialog(Context context) {
		Intent intent = new Intent(context, LoginActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 显示帖子详情
	 * 
	 * @param context
	 * @param postId
	 */
	public static void showQuestionDetail(Context context, int postId) {
//		Intent intent = new Intent(context, QuestionDetail.class);
//		intent.putExtra("post_id", postId);
//		context.startActivity(intent);
	}

	/**
	 * 显示相关Tag帖子列表
	 * 
	 * @param context
	 * @param tag
	 */
	public static void showQuestionListByTag(Context context, String tag) {
//		Intent intent = new Intent(context, QuestionTag.class);
//		intent.putExtra("post_tag", tag);
//		context.startActivity(intent);
	}

	/**
	 * 显示我要提问页面
	 * 
	 * @param context
	 */
	public static void showQuestionPub(Context context) {
//		Intent intent = new Intent(context, QuestionPub.class);
//		context.startActivity(intent);
	}

	/**
	 * 调用系统安装了的应用分享
	 * 
	 * @param context
	 * @param title
	 * @param url
	 */
	public static void showShareMore(Activity context, final String title,
			final String url) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "分享：" + title);
		intent.putExtra(Intent.EXTRA_TEXT, title + " " + url);
		context.startActivity(Intent.createChooser(intent, "选择分享"));
	}

	/**
	 * 分享到'新浪微博'或'腾讯微博'的对话框
	 * 
	 * @param context
	 *            当前Activity
	 * @param title
	 *            分享的标题
	 * @param url
	 *            分享的链接
	 */
//	public static void showShareDialog(final Activity context,
//			final String title, final String url) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setIcon(android.R.drawable.btn_star);
//		builder.setTitle(context.getString(R.string.share));
//		builder.setItems(R.array.app_share_items,
//				new DialogInterface.OnClickListener() {
//					AppConfig cfgHelper = AppConfig.getAppConfig(context);
//					AccessInfo access = cfgHelper.getAccessInfo();
//
//					public void onClick(DialogInterface arg0, int arg1) {
//						switch (arg1) {
//						case 0:// 新浪微博
//								// 分享的内容
//							final String shareMessage = title + " " + url;
//							// 初始化微博
//							if (SinaWeiboHelper.isWeiboNull()) {
//								SinaWeiboHelper.initWeibo();
//							}
//							// 判断之前是否登陆过
//							if (access != null) {
//								SinaWeiboHelper.progressDialog = new ProgressDialog(
//										context);
//								SinaWeiboHelper.progressDialog
//										.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//								SinaWeiboHelper.progressDialog
//										.setMessage(context
//												.getString(R.string.sharing));
//								SinaWeiboHelper.progressDialog
//										.setCancelable(true);
//								SinaWeiboHelper.progressDialog.show();
//								new Thread() {
//									public void run() {
//										SinaWeiboHelper.setAccessToken(
//												access.getAccessToken(),
//												access.getAccessSecret(),
//												access.getExpiresIn());
//										SinaWeiboHelper.shareMessage(context,
//												shareMessage);
//									}
//								}.start();
//							} else {
//								SinaWeiboHelper
//										.authorize(context, shareMessage);
//							}
//							break;
//						case 1:// 腾讯微博
//							QQWeiboHelper.shareToQQ(context, title, url);
//							break;
//						case 2:// 截图分享
//							addScreenShot(context, new OnScreenShotListener() {
//
//								@SuppressLint("NewApi")
//								public void onComplete(Bitmap bm) {
//									Intent intent = new Intent(context,ScreenShotShare.class);
//									intent.putExtra("title", title);
//									intent.putExtra("url", url);
//									intent.putExtra("cut_image_tmp_path",ScreenShotView.TEMP_SHARE_FILE_NAME);
//									try {
//										ImageUtils.saveImageToSD(context,ScreenShotView.TEMP_SHARE_FILE_NAME,bm, 100);
//									} catch (IOException e) {
//										e.printStackTrace();
//									}
//									context.startActivity(intent);
//								}
//							});
//							break;
//						case 3:// 更多
//							showShareMore(context, title, url);
//							break;
//						}
//					}
//				});
//		builder.create().show();
//	}


	/**
	 * 显示图片对话框
	 * 
	 * @param context
	 * @param imgUrl
	 */
	public static void showImageDialog(Context context, String imgUrl) {
//		Intent intent = new Intent(context, ImageDialog.class);
//		intent.putExtra("img_url", imgUrl);
//		context.startActivity(intent);
	}

	public static void showImageZoomDialog(Context context, String imgUrl) {
		Intent intent = new Intent(context,ImageZoomActivity.class);
		intent.putExtra("imgUrl", imgUrl);
		context.startActivity(intent);
	}

	/**
	 * 显示搜索界面
	 * 
	 * @param context
	 */
	public static void showSearch(Context context) {
//		Intent intent = new Intent(context, Search.class);
//		context.startActivity(intent);
	}

	/**
	 * 显示我的资料
	 * 
	 * @param context
	 */
	public static void showUserInfo(Activity context) {
		Intent intent = new Intent(context, UserInfoDetailActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 加载显示用户头像
	 * 
	 * @param imgFace
	 * @param faceURL
	 */
	public static void showUserFace(final ImageView imgFace,
			final String faceURL) {
		showLoadImage(imgFace, faceURL,
				imgFace.getContext().getString(R.string.msg_load_userface_fail));
	}

	/**
	 * 加载显示图片
	 * 
	 * @param imgFace
	 * @param faceURL
	 * @param errMsg
	 */
	public static void showLoadImage(final ImageView imgView,
			final String imgURL, final String errMsg) {
		// 读取本地图片
		if (StringUtils.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
			Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(),
					R.drawable.photo_no_photo);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 是否有缓存图片
		final String filename = FileUtils.getFileName(imgURL);
		// Environment.getExternalStorageDirectory();返回/sdcard
		String filepath = imgView.getContext().getFilesDir() + File.separator
				+ filename;
		File file = new File(filepath);
		if (file.exists()) {
			Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 从网络获取&写入图片缓存
		String _errMsg = imgView.getContext().getString(
				R.string.msg_load_image_fail);
		if (!StringUtils.isEmpty(errMsg))
			_errMsg = errMsg;
		final String ErrMsg = _errMsg;
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					imgView.setImageBitmap((Bitmap) msg.obj);
					try {
						// 写图片缓存
						ImageUtils.saveImage(imgView.getContext(), filename,
								(Bitmap) msg.obj);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					ToastMessage(imgView.getContext(), ErrMsg);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Bitmap bmp = ApiClient.getNetBitmap(imgURL);
					msg.what = 1;
					msg.obj = bmp;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 打开浏览器
	 * 
	 * @param context
	 * @param url
	 */
	public static void openBrowser(Context context, String url) {
		try {
			Uri uri = Uri.parse(url);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
			ToastMessage(context, "无法浏览此网页", 500);
		}
	}

	/**
	 * 获取webviewClient对象
	 * 
	 * @return
	 */
	public static WebViewClient getWebViewClient() {
		return new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
//				showUrlRedirect(view.getContext(), url);
				return true;
			}
		};
	}

	/**
	 * 获取TextWatcher对象
	 * 
	 * @param context
	 * @param tmlKey
	 * @return
	 */
	public static TextWatcher getTextWatcher(final Activity context,
			final String temlKey) {
		return new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 保存当前EditText正在编辑的内容
				((AppContext) context.getApplication()).setProperty(temlKey,
						s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		};
	}

	/**
	 * 编辑器显示保存的草稿
	 * 
	 * @param context
	 * @param editer
	 * @param temlKey
	 */
	public static void showTempEditContent(Activity context, EditText editer,
			String temlKey) {
		String tempContent = ((AppContext) context.getApplication())
				.getProperty(temlKey);
		if (!StringUtils.isEmpty(tempContent)) {
			SpannableStringBuilder builder = parseFaceByText(context,
					tempContent);
			editer.setText(builder);
			editer.setSelection(tempContent.length());// 设置光标位置
		}
	}
	/**
	 * 将[12]之类的字符串替换为表情
	 * 
	 * @param context
	 * @param content
	 */
	public static SpannableStringBuilder parseFaceByText(Context context,
			String content) {
		SpannableStringBuilder builder = new SpannableStringBuilder(content);
		Matcher matcher = facePattern.matcher(content);
		while (matcher.find()) {
			// 使用正则表达式找出其中的数字
			int position = StringUtils.toInt(matcher.group(1));
			int resId = 0;
			try {
				if (position > 65 && position < 102)
					position = position - 1;
				else if (position > 102)
					position = position - 2;
				resId = GridViewFaceAdapter.getImageIds()[position];
				Drawable d = context.getResources().getDrawable(resId);
				d.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				builder.setSpan(span, matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} catch (Exception e) {
			}
		}
		return builder;
	}
	/**
	 * 组合动态的回复文本
	 * 
	 * @param name
	 * @param body
	 * @return
	 */
	public static SpannableString parseActiveReply(String name, String body) {
		SpannableString sp = new SpannableString(name + "：" + body);
		// 设置用户名字体加粗、高亮
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
				name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 0,
				name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sp;
	}


	/**
	 * 组合回复引用文本
	 * 
	 * @param name
	 * @param body
	 * @return
	 */
	public static SpannableString parseQuoteSpan(String name, String body) {
		SpannableString sp = new SpannableString("回复：" + name + "\n" + body);
		// 设置用户名字体加粗、高亮
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 3,
				3 + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 3,
				3 + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sp;
	}

	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}

	/**
	 * 点击返回监听事件
	 * 
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}

	/**
	 * 显示关于该应用的信息
	 * 
	 * @param context
	 */
	public static void showAbout(Context context) {
		Intent intent = new Intent(context, AboutAppActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 显示用户反馈
	 * 
	 * @param context
	 */
	public static void showFeedBack(Context context) {
		Intent intent = new Intent(context, FeedBackActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 文章是否加载图片显示
	 * 
	 * @param activity
	 */
	public static void changeSettingIsLoadImage(Activity activity) {
		AppContext ac = (AppContext) activity.getApplication();
		if (ac.isLoadImage()) {
			ac.setConfigLoadimage(false);
			ToastMessage(activity, "已设置文章不加载图片");
		} else {
			ac.setConfigLoadimage(true);
			ToastMessage(activity, "已设置文章加载图片");
		}
	}

	public static void changeSettingIsLoadImage(Activity activity, boolean b) {
		AppContext ac = (AppContext) activity.getApplication();
		ac.setConfigLoadimage(b);
	}

	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void clearAppCache(Activity activity) {
		final AppContext ac = (AppContext) activity.getApplication();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ToastMessage(ac, "缓存清除成功");
				} else {
					ToastMessage(ac, "缓存清除失败");
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					ac.clearAppCache();
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 发送App异常崩溃报告
	 * 
	 * @param cont
	 * @param crashReport
	 */
	public static void sendAppCrashReport(final Context cont,
			final String crashReport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		final String email = cont.getResources().getString(R.string.reportSendAccount);
		final String appName = cont.getResources().getString(R.string.app_name);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		builder.setPositiveButton(R.string.submit_report,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 发送异常报告
						Intent i = new Intent(Intent.ACTION_SEND);
						// i.setType("text/plain"); //模拟器
						i.setType("message/rfc822"); // 真机
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { email });
						i.putExtra(Intent.EXTRA_SUBJECT,
								appName+"Android客户端 - 错误报告");
						i.putExtra(Intent.EXTRA_TEXT, crashReport);
						cont.startActivity(Intent.createChooser(i, "发送错误报告"));
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.show();
	}

	/**
	 * 退出程序
	 * 
	 * @param cont
	 */
	public static void Exit(final Context cont) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_menu_surelogout);
		builder.setPositiveButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	/**
	 * 添加截屏功能
	 */
//	@SuppressLint("NewApi")
//	public static void addScreenShot(Activity context,
//			OnScreenShotListener mScreenShotListener) {
//		BaseActivity cxt = null;
//		if (context instanceof BaseActivity) {
//			cxt = (BaseActivity) context;
//			cxt.setAllowFullScreen(false);
//			ScreenShotView screenShot = new ScreenShotView(cxt,
//					mScreenShotListener);
//			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT);
//			context.getWindow().addContentView(screenShot, lp);
//		}
//	}
	/**
	 * 添加网页的点击图片展示支持
	 */
	@SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled" })
	public static void addWebImageShow(final Context cxt, WebView wv) {
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new OnWebViewImageListener() {
			@Override
			public void onImageClick(String bigImageUrl) {
				if (bigImageUrl != null)
					UIHelper.showImageZoomDialog(cxt, bigImageUrl);
			}
		}, "mWebViewImageListener");
	}

	public static void showUserCenter(Context context, String face,
			String replyAuthor, String location) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(context,UserInfoDetailActivity.class);
		intent.putExtra("face", face);
		intent.putExtra("username", replyAuthor);
		intent.putExtra("location", location);
		context.startActivity(intent);
	}
	/**
	 * 发送通知广播
	 * 
	 * @param context
	 * @param notice
	 */
	public static void sendBroadCast(Context context, Notice notice) {
		if (((AppContext) context.getApplicationContext()).getLoginState()!=AppContext.LOGINED
				|| notice == null)
			return;
		Intent intent = new Intent("net.oschina.app.action.APPWIDGET_UPDATE");
		intent.putExtra("title", notice.getTitle());
		intent.putExtra("newReplyCount", notice.getNewReplyCount());
		context.sendBroadcast(intent);
	}
}
