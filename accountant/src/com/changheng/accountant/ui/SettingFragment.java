package com.changheng.accountant.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.BrightnessUtil;
import com.changheng.accountant.util.UpdateDataManager;
import com.changheng.accountant.util.UpdateManager;
import com.changheng.accountant.view.ShareWindow;
import com.umeng.fb.FeedbackAgent;
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

public class SettingFragment extends Fragment implements OnClickListener {
	private TextView dateTxt, versionTxt, cacheSizeTxt,
//					usernameTxt,  loginTxt 
					newDataFlag,newVersionFlag;
	private CheckBox checkBox;
	private AppConfig appConfig;
	private AppContext appContext;
	private Context mContext;
	private Button logoutBtn;
	private PopupWindow pop;
	private ShareWindow sharePop;
	private View parent;
	final UMSocialService mController = UMServiceFactory.getUMSocialService(
			"com.umeng.share", RequestType.SOCIAL);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.setting_fragment, null);
		dateTxt = (TextView) v.findViewById(R.id.txt_date);
		versionTxt = (TextView) v.findViewById(R.id.txt_version);
//		usernameTxt = (TextView) v.findViewById(R.id.txt_username);
		cacheSizeTxt = (TextView) v.findViewById(R.id.txt_cache_size);
		checkBox = (CheckBox) v.findViewById(R.id.checkWhenStart);
//		loginTxt = (TextView) v.findViewById(R.id.loginStr);
		logoutBtn = (Button) v.findViewById(R.id.btn_logout);
		newDataFlag = (TextView) v.findViewById(R.id.newDataFlag);
		newVersionFlag = (TextView) v.findViewById(R.id.newVersionFlag);
		parent = v.findViewById(R.id.setting_parent);
		appConfig = AppConfig.getAppConfig(getActivity());
		appContext = (AppContext) getActivity().getApplication();
		if (appContext.getLoginState() == AppContext.LOGINED
				|| appContext.getLoginState() == AppContext.LOCAL_LOGINED) {
//			loginTxt.setText("注销登录");
			logoutBtn.setText("退出当前帐号");
//			usernameTxt.setText(appContext.getUsername());
		} else {
//			loginTxt.setText("登录/注册");
			logoutBtn.setText("登录/注册");
//			usernameTxt.setText("未登录");
		}
		mContext = getActivity();
		checkBox.setOnClickListener(this);
		configShareInfo();
		v.findViewById(R.id.layout_about_app).setOnClickListener(this);
		v.findViewById(R.id.layout_share).setOnClickListener(this);
		v.findViewById(R.id.layout_edit).setOnClickListener(this);
		v.findViewById(R.id.layout_checkupdate).setOnClickListener(this);
		v.findViewById(R.id.layout_checkupdata).setOnClickListener(this);
		v.findViewById(R.id.layout_clear_cache).setOnClickListener(this);
		v.findViewById(R.id.layout_checkupdate_when_start).setOnClickListener(
				this);
		v.findViewById(R.id.layout_feedback).setOnClickListener(this);
//		v.findViewById(R.id.layout_logout).setOnClickListener(this);
		v.findViewById(R.id.layout_deal).setOnClickListener(this);
		v.findViewById(R.id.layout_website).setOnClickListener(this);
		v.findViewById(R.id.layout_screenlight).setOnClickListener(this);
		v.findViewById(R.id.layout_sync).setOnClickListener(this);
		logoutBtn.setOnClickListener(this);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.e("settingFragment",requestCode+" " + resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		UMSsoHandler ssoHandler2 = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler2 != null) {
			ssoHandler2.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		String dateStr = appConfig.getFormatExamTime();
		this.dateTxt.setText(dateStr == null ? "设置" : dateStr);
		this.versionTxt.setText(appContext.getVersionName());
		if(appContext.isHasNewData()) newDataFlag.setVisibility(View.VISIBLE);
		if(appContext.isHasNewVersion()) newVersionFlag.setVisibility(View.VISIBLE);
//		this.usernameTxt.setText(appContext.getUsername());
		this.cacheSizeTxt.setText(appContext.calculateCacheSize());
		checkBox.setChecked(appContext.isCheckUp());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_edit:
			Intent intent = new Intent(mContext, SetTimeActivity.class);
			startActivity(intent);
			break;
		case R.id.layout_share:
			share2();
			break;
		case R.id.layout_about_app:
			startActivity(new Intent(mContext, AboutActivity.class));
			break;
		case R.id.layout_website:
			viewWebsite();
			break;
		case R.id.layout_checkupdate:
			// 检测更新,显示消息
			// UmengUpdateAgent.update(this.getActivity());
			// UmengUpdateAgent.setUpdateAutoPopup(false);
			// UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			// @Override
			// public void onUpdateReturned(int updateStatus,UpdateResponse
			// updateInfo) {
			// switch (updateStatus) {
			// case 0: // has update
			// UmengUpdateAgent.showUpdateDialog(mContext, updateInfo);
			// break;
			// case 1: // has no update
			// Toast.makeText(mContext, "没有更新", Toast.LENGTH_SHORT)
			// .show();
			// break;
			// case 2: // none wifi
			// Toast.makeText(mContext, "没有wifi连接， 只在wifi下更新",
			// Toast.LENGTH_SHORT)
			// .show();
			// break;
			// case 3: // time out
			// Toast.makeText(mContext, "超时", Toast.LENGTH_SHORT)
			// .show();
			// break;
			// }
			// }
			// });
			newVersionFlag.setVisibility(View.GONE);
			UpdateManager.getUpdateManager().checkAppUpdate(this.getActivity(),
					true);
			break;
		case R.id.layout_checkupdata:
			newDataFlag.setVisibility(View.GONE);
			UpdateDataManager.getUpdateManager().checkDataUpdate(this.getActivity(),
					true);
			break;
		case R.id.layout_clear_cache:
			// 清理缓存
			appContext.clearAppCache();
			// 重新计算缓存
			this.cacheSizeTxt.setText(appContext.calculateCacheSize());
			// 提示消息
			print("缓存清除成功");
			break;
		case R.id.layout_deal:
			startActivity(new Intent(mContext, AboutAppActivity.class));
			break;
		case R.id.layout_feedback:
			feedBack();
			break;
//		case R.id.layout_logout:
//			loginOrLogout();
//			break;
		case R.id.btn_logout:
			loginOrLogout();
			break;
		case R.id.layout_screenlight:
			showPop();
			// print("屏幕亮度");
			break;
		case R.id.checkWhenStart:
			if (checkBox.isChecked()) {
				appConfig.set(AppConfig.CONF_CHECKUP, String.valueOf(true));
			} else {
				appConfig.set(AppConfig.CONF_CHECKUP, String.valueOf(false));
			}
			break;
		case R.id.layout_checkupdate_when_start:
			// 启动时是否检测更新
			checkWhenStart();
			break;
		case R.id.layout_sync:
			Intent mIntent = new Intent(this.getActivity(), SysncActivity.class);
			mIntent.putExtra("loginFrom", "sysnc");
			startActivity(mIntent);
			break;
		}
	}

	private void loginOrLogout() {
		if (appContext.getLoginState() == AppContext.LOGINED) {
			showLogoutDialog();
		} else {
			// 转到登录界面
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			intent.putExtra("loginFrom", LoginActivity.LOGIN_SETTING);
			startActivityForResult(intent, 10);
		}
	}

	// 分享
	private void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, "应用分享");
		intent.putExtra(Intent.EXTRA_TEXT,
				"会计证考试一点通，让考试一点就通 (分享自会计证考试一点通Android客户端)");
		// Uri uri = Uri.fromFile(new
		// File("file:///android_asset/other/logo-formobile.png"));
		// intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, getActivity().getTitle()));
	}

	private void configShareInfo() {
		// 设置分享内容
		mController.setShareContent(getResources().getString(
				R.string.share_content));
		// 设置分享图片, 参数2为图片的地址
		// mController.setShareMedia(new UMImage(getActivity(),
		// "http://www.umeng.com/images/pic/banner_module_social.png"));
		// 设置分享图片，参数2为本地图片的资源引用
		mController.setShareMedia(new UMImage(getActivity(),
				R.drawable.tem_logo));
		// 设置分享图片，参数2为本地图片的路径
		// mController.setShareMedia(new UMImage(getActivity(),
		// BitmapFactory.decodeFile("/mnt/sdcard/icon.png")));
		// 微信AppID wxa0d6b428291ce621
		String appID = "wxc518ead89a5467bd";
		String qqAppID = "100571955";
		// 微信图文分享必须设置一个url
		String contentUrl = getResources().getString(R.string.share_content_url);
		// 添加微信平台，参数1为当前Activity, 参数2为用户申请的AppID, 参数3为点击分享内容跳转到的目标url
		mController.getConfig().supportWXPlatform(mContext, appID, contentUrl);
		// 支持微信朋友圈
		mController.getConfig().supportWXCirclePlatform(mContext, appID,
				contentUrl);
		// 添加对QQ平台的支持
//		mController.getConfig().supportQQPlatform((Activity) mContext, contentUrl);
		mController.getConfig().supportQQPlatform(getActivity(),true, qqAppID, contentUrl);
		// 免登录分享到QQ空间
		mController.getConfig().setSsoHandler(new QZoneSsoHandler(this.getActivity()));
		// 免登录分享到sina微博
		mController.getConfig().setSsoHandler(new SinaSsoHandler());
	}

	private void share2() {
		//自定义的分享界面
		if (sharePop == null) {
			sharePop = new ShareWindow(mContext, itemClick);
		}
		sharePop.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
	}

	private void feedBack() {
		FeedbackAgent agent = new FeedbackAgent(this.getActivity());
		agent.startFeedbackActivity();
	}

	// 访问网站
	private void viewWebsite() {
		Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources()
				.getString(R.string.website)));
		startActivity(in);
	}


	// 是否启动时检测更新
	private void checkWhenStart() {
		if (checkBox.isChecked()) {
			checkBox.setChecked(false);
			appConfig.set(AppConfig.CONF_CHECKUP, String.valueOf(false));
		} else {
			checkBox.setChecked(true);
			appConfig.set(AppConfig.CONF_CHECKUP, String.valueOf(true));
		}

	}

	// 调节屏幕亮度
	// private void showDialog() {
	// AlertDialog.Builder builder = new Builder(getActivity());
	// builder.setTitle("调节亮度");
	// final LayoutInflater inflater = LayoutInflater.from(mContext);
	// View v = inflater.inflate(R.layout.brightness_dlg, null);
	// final SeekBar seekBar = (SeekBar) v.findViewById(R.id.seekBar1);
	// seekBar.setMax(BrightnessUtil.MAX_BRIGHTNESS);
	// seekBar.setProgress((BrightnessUtil.getScreenBrightness(getActivity())));
	// seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	// @Override
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	// // TODO Auto-generated method stub
	// BrightnessUtil.setBrightness(getActivity(), progress);
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekBar arg0) {
	//
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar arg0) {
	// }
	// });
	// CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox1);
	// checkBox.setChecked(BrightnessUtil.isAutoBrightness(mContext
	// .getContentResolver()));
	// if (checkBox.isChecked())
	// seekBar.setEnabled(false);
	// checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	// @Override
	// public void onCheckedChanged(CompoundButton buttonView,
	// boolean isChecked) {
	// // TODO Auto-generated method stub
	// if (isChecked) {
	// BrightnessUtil.startAutoBrightness(getActivity());
	// seekBar.setEnabled(false);
	// } else {
	// BrightnessUtil.stopAutoBrightness(getActivity());
	// seekBar.setEnabled(true);
	// }
	// }
	// });
	// builder.setView(v);
	// builder.setPositiveButton("确定",
	// new android.content.DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// // TODO Auto-generated method stub
	// if (seekBar.isEnabled()) {
	// BrightnessUtil.saveBrightness(
	// mContext.getContentResolver(),
	// seekBar.getProgress());
	// } else {
	// dialog.dismiss();
	// }
	// }
	// });
	// builder.setNegativeButton("取消",
	// new android.content.DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// }
	// });
	// AlertDialog downloadDialog = builder.create();
	// downloadDialog.setCanceledOnTouchOutside(false);
	// downloadDialog.show();
	// }
	private void showPop() {
		if (pop == null) {
			View v = LayoutInflater.from(this.getActivity()).inflate(
					R.layout.brightness_dlg, null);
			final SeekBar seekBar = (SeekBar) v
					.findViewById(R.id.light_seekbar);
			seekBar.setMax(BrightnessUtil.MAX_BRIGHTNESS);
			seekBar.setProgress((BrightnessUtil
					.getScreenBrightness(getActivity())));
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
					BrightnessUtil.setBrightness(getActivity(), progress);
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {

					// TODO Auto-generated method stub
				}

				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}
			});
			CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox1);
			checkBox.setChecked(BrightnessUtil.isAutoBrightness(mContext
					.getContentResolver()));
			if (checkBox.isChecked())
				seekBar.setEnabled(false);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					if (isChecked) {
						BrightnessUtil.startAutoBrightness(getActivity());
						seekBar.setEnabled(false);
					} else {
						BrightnessUtil.stopAutoBrightness(getActivity());
						seekBar.setEnabled(true);
					}
				}
			});
			Button sureBtn = (Button) v.findViewById(R.id.btn_sure);
			sureBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (seekBar.isEnabled()) {
						BrightnessUtil.saveBrightness(
								mContext.getContentResolver(),
								seekBar.getProgress());
						pop.dismiss();
					} else {
						pop.dismiss();
					}
				}
			});
			Button cancelBtn = (Button) v.findViewById(R.id.btn_cancel);
			cancelBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					pop.dismiss();
				}
			});
			pop = new PopupWindow(v, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			// 使其聚集
			pop.setFocusable(true);
			// 设置允许在外点击消失
			pop.setOutsideTouchable(true);

			// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
			pop.setBackgroundDrawable(new BitmapDrawable());
		}
		pop.showAtLocation(this.getView(), Gravity.CENTER, 0, 0);
	}

	public void setLoginTxt() {
		// TODO Auto-generated method stub
		if (appContext.getLoginState() == AppContext.LOGINED) {
//			loginTxt.setText("注销登录");
//			usernameTxt.setText(appContext.getUsername());
		}
	}

	private void showLogoutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.app_setting_surelogout);
		builder.setMessage(" 当前帐号：" + appContext.getUsername());
		builder.setPositiveButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						// 退出登录,清除登录信息
//						loginTxt.setText("登录/注册");
						logoutBtn.setText("登录/注册");
//						usernameTxt.setText("未登录");
						// to do something...
						new Thread() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									ApiClient.logout(appContext,
											appContext.getUsername());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();
						appContext.cleanLoginInfo();
						((MainActivity) getActivity()).changeMenu(); // 改变菜单文字
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
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	private void sharePlatform(final SHARE_MEDIA media) {
		if (media.equals(SHARE_MEDIA.WEIXIN)
				|| media.equals(SHARE_MEDIA.WEIXIN_CIRCLE)) {
			directShare(media);
		} else {
			if (!OauthHelper.isAuthenticated(mContext, media)) {
				mController.doOauthVerify(mContext, media,
						new UMAuthListener() {
							@Override
							public void onStart(SHARE_MEDIA platform) {
								Toast.makeText(mContext, "授权开始",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onError(SocializeException e,
									SHARE_MEDIA platform) {
								Log.e("授权错误",e.getMessage());
								Toast.makeText(mContext, "授权错误",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onComplete(Bundle value,
									SHARE_MEDIA platform) {
								Toast.makeText(mContext, "授权完成",
										Toast.LENGTH_SHORT).show();
								// 获取相关授权信息或者跳转到自定义的分享编辑页面
								String uid = value.getString("uid");
								directShare(media);
							}

							@Override
							public void onCancel(SHARE_MEDIA platform) {
								Toast.makeText(mContext, "授权取消",
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
		mController.postShare(mContext, media,
	            new SnsPostListener() {

	            @Override
	            public void onStart() {
	            	sharePop.dismiss();
	                Toast.makeText(mContext, "分享开始",Toast.LENGTH_SHORT).show();
	            }

	            @Override
	            public void onComplete(SHARE_MEDIA platform,int eCode, SocializeEntity entity) {
	                if(eCode == StatusCode.ST_CODE_SUCCESSED){
	                    Toast.makeText(mContext, "分享成功",Toast.LENGTH_SHORT).show();
	                }else{
	                    Toast.makeText(mContext, "分享失败",Toast.LENGTH_SHORT).show();
	                }
	            }
	    });
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(pop!=null)
		{
			pop.dismiss();
		}
		if(sharePop!=null)
		{
			sharePop.dismiss();
		}
		
		super.onDestroy();
		
	}
}
