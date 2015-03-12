package com.changheng.accountant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.PaperDao;
import com.changheng.accountant.entity.AppUpdate;
import com.changheng.accountant.entity.UpdateDataEntity;

/**
 * 题库数据更新工具包
 * 
 * @version 1.1
 */
public class UpdateDataManager {

	private static final int DOWN_NOSDCARD = 0;
	private static final int DOWN_UPDATE = 1;
	private static final int DOWN_OVER = 2;

	private static final int DIALOG_TYPE_LATEST = 0;
	private static final int DIALOG_TYPE_FAIL = 1;

	private static UpdateDataManager updateManager;

	private Context mContext;
	private AppContext appContext;
	// 通知对话框
	private Dialog noticeDialog;
	// 下载对话框
	private Dialog downloadDialog;
	// '已经是最新' 或者 '无法获取最新版本' 的对话框
	private Dialog latestOrFailDialog;
	// 进度条
	private ProgressBar mProgress;
	// 显示下载数值
	private TextView mProgressText;
	// 查询动画
	private ProgressDialog mProDialog;
	// 进度值
	private int progress;
	// 下载线程
	private Thread downLoadThread;
	// 终止标记
	private boolean interceptFlag;
	// 提示语
	private String updateMsg = "";
	// 返回的安装包url
	private String apkUrl = "";
	// 下载包保存路径
	private String savePath = "";
	// apk保存完整路径
	private String apkFilePath = "";
	// 临时下载文件路径
	private String tmpFilePath = "";
	// 下载文件大小
	private String apkFileSize;
	// 已下载文件大小
	private String tmpFileSize;

	private AppUpdate mUpdate;
	private String addTime;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				mProgressText.setText(tmpFileSize + "/" + apkFileSize);
				break;
			case DOWN_OVER:
				downloadDialog.dismiss();
//				installApk();
				dealData();
				break;
			case DOWN_NOSDCARD:
				downloadDialog.dismiss();
				Toast.makeText(mContext, "无法下载数据，请检查SD卡是否挂载", 3000).show();
				break;
			}
		};
	};

	public static UpdateDataManager getUpdateManager() {
		if (updateManager == null) {
			updateManager = new UpdateDataManager();
		}

		updateManager.interceptFlag = false;
		return updateManager;
	}

	/**
	 * 检查数据更新
	 * @param context
	 * @param isShowMsg
	 *            是否显示提示消息
	 */
	public void checkDataUpdate(Context context, final boolean isShowMsg) {
		this.mContext = context;
		this.appContext = (AppContext) ((Activity) mContext)
				.getApplication();
		// getCurrentVersion();
		SharedPreferences pref = context.getSharedPreferences("first_pref", 0);
		final String addtime = pref.getString("DBAddTime", null);
		if (addtime == null) {
			if(isShowMsg)
				Toast.makeText(context, "暂无数据更新", Toast.LENGTH_SHORT).show();
			return;
		}
		if (isShowMsg) {
			if (mProDialog == null)
				mProDialog = ProgressDialog.show(mContext, null, "正在检测，请稍后...",
						true, true);
			else if (mProDialog.isShowing()
					|| (latestOrFailDialog != null && latestOrFailDialog
							.isShowing()))
				return;
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				// 进度条对话框不显示 - 检测结果也不显示
				if (mProDialog != null && !mProDialog.isShowing()) {
					return;
				}
				// 关闭并释放释放进度条对话框
				if (isShowMsg && mProDialog != null) {
					mProDialog.dismiss();
					mProDialog = null;
				}
				// 显示检测结果
				if (msg.what == 1) {
					mUpdate = (AppUpdate) msg.obj;
					if (mUpdate.isDataNeedUpdate(addtime)) {
						apkUrl = mUpdate.getUrl();
						updateMsg = mUpdate.toString();
						showNoticeDialog();
					} else if (isShowMsg) {
						showLatestOrFailDialog(DIALOG_TYPE_LATEST);
					}
				} else if (isShowMsg) {
					showLatestOrFailDialog(DIALOG_TYPE_FAIL);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					// String update =
					// ApiClient.checkVersion((AppContext)((Activity)mContext).getApplication());
					// ParseResult pr = JsonParseUtil.parseCheckUpdate(update);
					AppUpdate update = appContext.getDataUpdate();
					msg.what = 1;
					msg.obj = update;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 显示'已经是最新'或者'无法获取版本信息'对话框
	 */
	private void showLatestOrFailDialog(int dialogType) {
		if (latestOrFailDialog != null) {
			// 关闭并释放之前的对话框
			latestOrFailDialog.dismiss();
			latestOrFailDialog = null;
		}
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("系统提示");
		if (dialogType == DIALOG_TYPE_LATEST) {
			builder.setMessage("您当前已经是最新");
		} else if (dialogType == DIALOG_TYPE_FAIL) {
			builder.setMessage("无法获取更新信息");
		}
		builder.setPositiveButton("确定", null);
		latestOrFailDialog = builder.create();
		latestOrFailDialog.show();
	}

	/**
	 * 获取当前客户端版本信息
	 */
	// private void getCurrentVersion(){
	// try {
	// PackageInfo info =
	// mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
	// 0);
	// curVersionName = info.versionName;
	// curVersionCode = info.versionCode;
	// } catch (NameNotFoundException e) {
	// e.printStackTrace(System.err);
	// }
	// }

	/**
	 * 显示版本更新通知对话框
	 */
	private void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("数据更新");
		builder.setMessage("题库数据更新");
		builder.setPositiveButton("立即更新", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		builder.setNegativeButton("以后再说", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				appContext.setHasNewData(true);
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	/**
	 * 显示下载对话框
	 */
	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("正在下载新数据");

		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.update_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		mProgressText = (TextView) v.findViewById(R.id.update_progress_text);

		builder.setView(v);
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				appContext.setHasNewData(true);
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.show();

		downloadApk();
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				String dataName = "update_data.data";
				String tmpData = "update_data.tmp";
				// 判断是否挂载了SD卡
				String storageState = Environment.getExternalStorageState();
				if (storageState.equals(Environment.MEDIA_MOUNTED)) {
					savePath = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/CHAccountant/data/";
					File file = new File(savePath);
					if (!file.exists()) {
						file.mkdirs();
					}
					apkFilePath = savePath + dataName;
					tmpFilePath = savePath + tmpData;
				}

				// 没有挂载SD卡，无法下载文件
				if (apkFilePath == null || apkFilePath == "") {
					mHandler.sendEmptyMessage(DOWN_NOSDCARD);
					return;
				}

				File ApkFile = new File(apkFilePath);

				// 是否已下载更新文件
				if (ApkFile.exists()) {
					downloadDialog.dismiss();
					// installApk();
					dealData();
					return;
				}

				// 输出临时下载文件
				File tmpFile = new File(tmpFilePath);
				FileOutputStream fos = new FileOutputStream(tmpFile);
				URL url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();

				// 显示文件大小格式：2个小数点显示
				DecimalFormat df = new DecimalFormat("0.00");
				// 进度条下面显示的总文件大小
				apkFileSize = df.format((float) length / 1024 / 1024) + "MB";
				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					// 进度条下面显示的当前下载文件大小
					tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
					// 当前进度值
					progress = (int) (((float) count / length) * 100);
					// 更新进度
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// 下载完成 - 将临时下载文件转成APK文件
						if (tmpFile.renameTo(ApkFile)) {
							// 通知安装
							mHandler.sendEmptyMessage(DOWN_OVER);
						}
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// 点击取消就停止下载
				fos.close();
				is.close();
				// 如果没有下完,把这临时文件删除
				if (tmpFile.exists()) {
					tmpFile.delete();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};

	/**
	 * 下载apk
	 * 
	 * @param url
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 安装apk
	 * 
	 * @param url
	 */
	private void installApk() {
		File apkfile = new File(apkFilePath);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
	}

	/**
	 * 处理数据
	 */
	private void dealData() {

		final ProgressDialog dialog = ProgressDialog.show(mContext, null,
				"数据处理中...", true, true);
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (dialog != null) {
					dialog.dismiss();
				}
				//处理完删除文件
				File apkfile = new File(apkFilePath);
				if (!apkfile.exists()) {
					apkfile.delete();
				}
				appContext.setHasNewData(false);
			}
		};
		final PaperDao dao = new PaperDao(mContext);
		new Thread() {
			public void run() {
				try {
					UpdateDataEntity entity = (UpdateDataEntity) outputObject(Environment
							.getExternalStorageDirectory().getPath()
							+ "/CHAccountant/data/update_data.data");
					dao.updateDataBase(entity.getPaperList(),
							entity.getRuleList(), entity.getQList());
					setData(mUpdate.getAddTime());
					handler.sendEmptyMessage(1);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-1);
				}
			};
		}.start();
	}

	private void setData(String addtime) {
		SharedPreferences preferences = mContext.getSharedPreferences(
				"first_pref", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		// 存入数据
		editor.putString("DBAddTime", addtime);
		// 提交修改
		editor.commit();
	}

	private Object outputObject(String file) throws Exception {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			// 反序列化失败 - 删除缓存文件
			if (e instanceof InvalidClassException) {
				File data = new File(file);
				data.delete();
			}
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return null;
	}
}
