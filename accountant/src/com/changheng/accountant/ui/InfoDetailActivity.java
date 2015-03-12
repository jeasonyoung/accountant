package com.changheng.accountant.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.entity.Info;
import com.changheng.accountant.view.ShareWindow;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.db.OauthHelper;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socom.Log;

public class InfoDetailActivity extends BaseActivity{
	private TextView linkTv;
	private WebView webView;
	private RelativeLayout loading;
	private LinearLayout reload;
	private Info news;
	private int news_id;
	private AppContext appContext;
	private ShareWindow sharePop;
	private View parent;
	final UMSocialService mController = UMServiceFactory.getUMSocialService(
			"com.umeng.share", RequestType.SOCIAL);
	private boolean isConfiged;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_about2);
		initView();
		initData();
	}
	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void initView()
	{
		((TextView) findViewById(R.id.title)).setText("帖子详情");
		webView = (WebView) findViewById(R.id.web_view);
		linkTv = (TextView) findViewById(R.id.linkTv);
		loading = (RelativeLayout) findViewById(R.id.loading);
		reload = (LinearLayout) findViewById(R.id.reload);
		parent = findViewById(R.id.settime);
		findViewById(R.id.btn_goback).setOnClickListener(new ReturnBtnClickListener(this));
		findViewById(R.id.btn_share).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(news==null) return;
				share2();
			}
		});
		findViewById(R.id.btn_reload).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initData();
			}
		});
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new SampleWebViewClient());
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDefaultFontSize(15);
		//添加图片点击事件
//        UIHelper.addWebImageShow(this, webView);
		webView.addJavascriptInterface(new OnWebViewImageListener() {
			@Override
			public void onImageClick(String bigImageUrl) {
//				if (bigImageUrl != null)
//					UIHelper.showImageZoomDialog(cxt, bigImageUrl);
//				Toast.makeText(InfoDetailActivity.this, bigImageUrl, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(InfoDetailActivity.this,ImageZoomActivity.class);
				intent.putExtra("imgUrl", bigImageUrl);
				InfoDetailActivity.this.startActivity(intent);
			}
		}, "mWebViewImageListener");
//		webView.loadUrl(getIntent().getStringExtra("url"));
	}
	private void initData()
	{
		appContext = (AppContext) getApplication();
		news_id = getIntent().getIntExtra("news_id", 0);
		final Handler handler = new Handler()
		{
			public void handleMessage(android.os.Message msg) {
				switch(msg.what)
				{
				case 1:
					//全局样式后加 帖子详情body数据
					String body = UIHelper.WEB_STYLE + news.getContent();
					// 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
					boolean isLoadImage;
					AppContext ac = (AppContext) getApplication();
					if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
						isLoadImage = true;
					} else {
						isLoadImage = ac.isLoadImage();
					}
					//如果加载图片
					if (isLoadImage) {
						// 过滤掉 img标签的width,height属性 <IMG>标签大写
						body = body.replaceAll(
								"(<IMG[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
						body = body.replaceAll(
								"(<IMG[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

						// 添加点击图片放大支持
						body = body.replaceAll("(<IMG[^>]+src=\")(\\S+)\"",
								"$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");

					}
					//如果不要加载图片
					else {
						// 过滤掉 img标签
						body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
					}
//					System.out.println("body = "+body);
					//过滤掉[page]
					body = body.replaceAll("\\[[N]?[o]?Page\\]", "");
					body = body+"<br>";
					//webView加载数据
					webView.loadDataWithBaseURL(null, body, "text/html",
							"utf-8", null);
					linkTv.setText("原 网 页");
					linkTv.setVisibility(View.VISIBLE);
					linkTv.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(news.getUrl()));
				            startActivity(in);
						}
					});
					break;
				case -1:	//加载失败
					loading.setVisibility(View.GONE);
					Toast.makeText(InfoDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					reload.setVisibility(View.VISIBLE);
//					i.nodata.setVisibility(View.VISIBLE);
					break;
				case 0: //没找到数据
					loading.setVisibility(View.GONE);
					Toast.makeText(InfoDetailActivity.this, "没有找到数据", Toast.LENGTH_SHORT).show();
					reload.setVisibility(View.VISIBLE);
//					nodata.setVisibility(View.VISIBLE);
					break;
				}
			};
		};
		loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
			try
			{
				news = appContext.getNews(news_id, false);
//				news = appContext.getNews(113, false); 	//测试带图片的新闻
				handler.sendEmptyMessage((news != null && news.getId() > 0) ? 1	: 0);
			}catch(Exception e)
			{
				e.printStackTrace();
				handler.sendEmptyMessage(-1);
			}
			};
		}.start();
	}
	private class SampleWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if ( url.contains("file:///") == true){
	            view.loadUrl(url);
	            return true;
	        }else{
	            Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
	            startActivity(in);
	            return true;
	        }
		}
		@Override
		public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				loading.setVisibility(View.GONE);
				reload.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		UMSsoHandler ssoHandler2 = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler2 != null) {
			ssoHandler2.authorizeCallBack(requestCode, resultCode, data);
		}
	}
	private void share2() {
		//自定义的分享界面
		if(!isConfiged)
		{
			configShareInfo();
			isConfiged = true;
		}
		if (sharePop == null) {
			sharePop = new ShareWindow(this, itemClick);
		}
		sharePop.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}
	private void configShareInfo() {
		// 设置分享内容
		mController.setShareContent(news.getTitle());
		// 设置分享图片, 参数2为图片的地址
		// mController.setShareMedia(new UMImage(getActivity(),
		// "http://www.umeng.com/images/pic/banner_module_social.png"));
		// 设置分享图片，参数2为本地图片的资源引用
		mController.setShareMedia(new UMImage(this,
				R.drawable.tem_logo));
		// 设置分享图片，参数2为本地图片的路径
		// mController.setShareMedia(new UMImage(getActivity(),
		// BitmapFactory.decodeFile("/mnt/sdcard/icon.png")));
		// 微信AppID wxa0d6b428291ce621
		String appID = "wxc518ead89a5467bd";
		String qqAppID = "100571955";
		// 微信图文分享必须设置一个url
		String contentUrl = news.getUrl();
		// 添加微信平台，参数1为当前Activity, 参数2为用户申请的AppID, 参数3为点击分享内容跳转到的目标url
		mController.getConfig().supportWXPlatform(this, appID, contentUrl);
		// 支持微信朋友圈
		mController.getConfig().supportWXCirclePlatform(this, appID,
				contentUrl);
		// 添加对QQ平台的支持
//		mController.getConfig().supportQQPlatform((Activity) mContext, contentUrl);
		mController.getConfig().supportQQPlatform(this,true, qqAppID, contentUrl);
		// 免登录分享到QQ空间
		mController.getConfig().setSsoHandler(new QZoneSsoHandler(this));
		// 免登录分享到sina微博
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
	}
	private OnItemClickListener itemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			switch (arg2) {
			case 0:
				sharePlatform(SHARE_MEDIA.WEIXIN);
				break;
			case 1:
				sharePlatform(SHARE_MEDIA.WEIXIN_CIRCLE);
				break;
			case 2:
				sharePlatform(SHARE_MEDIA.SINA);
				break;
			case 3:
				sharePlatform(SHARE_MEDIA.QQ);
				break;
			}
		}
	};

	private void print(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private void sharePlatform(final SHARE_MEDIA media) {
		if (media.equals(SHARE_MEDIA.WEIXIN)
				|| media.equals(SHARE_MEDIA.WEIXIN_CIRCLE)) {
			directShare(media);
		} else {
			if (!OauthHelper.isAuthenticated(this, media)) {
				mController.doOauthVerify(this, media,
						new UMAuthListener() {
							@Override
							public void onStart(SHARE_MEDIA platform) {
								Toast.makeText(InfoDetailActivity.this, "授权开始",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onError(SocializeException e,
									SHARE_MEDIA platform) {
								Log.e("授权错误",e.getMessage());
								Toast.makeText(InfoDetailActivity.this, "授权错误",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onComplete(Bundle value,
									SHARE_MEDIA platform) {
								Toast.makeText(InfoDetailActivity.this, "授权完成",
										Toast.LENGTH_SHORT).show();
								// 获取相关授权信息或者跳转到自定义的分享编辑页面
								String uid = value.getString("uid");
								directShare(media);
							}

							@Override
							public void onCancel(SHARE_MEDIA platform) {
								Toast.makeText(InfoDetailActivity.this, "授权取消",
										Toast.LENGTH_SHORT).show();
							}
						});
			}else
			{
				directShare(media);
			}
		}
	}
	private void directShare(SHARE_MEDIA media)
	{
		if(sharePop!=null&&sharePop.isShowing()){sharePop.dismiss();}
		mController.postShare(this, media,
	            new SnsPostListener() {

	            @Override
	            public void onStart() {
	            	sharePop.dismiss();
	                Toast.makeText(InfoDetailActivity.this, "分享开始",Toast.LENGTH_SHORT).show();
	            }

	            @Override
	            public void onComplete(SHARE_MEDIA platform,int eCode, SocializeEntity entity) {
	                if(eCode == StatusCode.ST_CODE_SUCCESSED){
	                    Toast.makeText(InfoDetailActivity.this, "分享成功",Toast.LENGTH_SHORT).show();
	                }else{
	                    Toast.makeText(InfoDetailActivity.this, "分享失败",Toast.LENGTH_SHORT).show();
	                }
	            }
	    });
	}
}
