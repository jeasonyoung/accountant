package com.changheng.accountant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.changheng.accountant.entity.AppUpdate;
import com.changheng.accountant.entity.Info;
import com.changheng.accountant.entity.NewsList;
import com.changheng.accountant.entity.Notice;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.User;
import com.changheng.accountant.ui.TimeBroadcast;
import com.changheng.accountant.ui.UIHelper;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.CyptoUtils;
import com.changheng.accountant.util.FileUtils;
import com.changheng.accountant.util.ImageUtils;
import com.changheng.accountant.util.MethodsCompat;
import com.changheng.accountant.util.StringUtils;
import com.changheng.accountant.util.XMLParseUtil;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 * 
 * @version 1.0
 */
public class AppContext extends Application {

	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;

	public static final int PAGE_SIZE = 20;// 默认分页大小
	private static final int CACHE_TIME = 60 * 60000;// 缓存失效时间
	public static final int LOGINING = 1;// 正在登录
	public static final int LOGIN_FAIL = -1;// 登录失败
	public static final int LOGINED = 2;// 已经登录
	public static final int UNLOGIN = 0;// 没有登录
	public static final int LOCAL_LOGINED = 3; // 本地登录

	private int loginState = 0; // 登录状态
	private int loginUid = 0; // 登录用户的id
	private String username = null;// 登录用的用户名
	private String nickname = null;// 昵称
	private boolean isAutoCheckuped, isAutoLogined, hasNewVersion, hasNewData;
	private String limitDate;

	public boolean isHasNewVersion() {
		return hasNewVersion;
	}

	public boolean isTimeOver() {
		// if(limitDate == null)
		// {
		// limitDate = AppConfig.getAppConfig(this).getExpires();
		// }
		// return StringUtils.compareDate(limitDate);
//		Integer hours = AppConfig.getAppConfig(this).getRestHours();
//		if (hours == null) {
//			return false;
//		}
//		return hours <= 0;
		return false;
	}

	public void setHasNewVersion(boolean hasNewVersion) {
		this.hasNewVersion = hasNewVersion;
	}

	public boolean isHasNewData() {
		return hasNewData;
	}

	public void setHasNewData(boolean hasNewData) {
		this.hasNewData = hasNewData;
	}

	public boolean isAutoCheckuped() {
		return isAutoCheckuped;
	}

	public void setAutoCheckuped(boolean isAutoCheckuped) {
		this.isAutoCheckuped = isAutoCheckuped;
	}

	public boolean isAutoLogined() {
		return isAutoLogined;
	}

	public void setAutoLogined(boolean isAutoLogined) {
		this.isAutoLogined = isAutoLogined;
	}

	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

	private String saveImagePath;// 保存图片路径

	private Handler unLoginHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				UIHelper.ToastMessage(AppContext.this,
						getString(R.string.msg_login_error));
				UIHelper.showLoginDialog(AppContext.this);
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		// 注册App异常崩溃处理器
		// Thread.setDefaultUncaughtExceptionHandler(AppException
		// .getAppExceptionHandler());
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 设置保存图片的路径
		saveImagePath = getProperty(AppConfig.SAVE_IMAGE_PATH);
		if (StringUtils.isEmpty(saveImagePath)) {
			setProperty(AppConfig.SAVE_IMAGE_PATH,
					AppConfig.DEFAULT_SAVE_IMAGE_PATH);
			saveImagePath = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
		}
	}

	// 进程被杀死，application里的对象丢失
	public void recoverLoginStatus() {
		String name = getProperty("user.account");
		if (username == null && name != null) {
			username = name;
			loginState = LOGINED;
		}
	}

	/**
	 * 检测当前系统声音是否为正常模式
	 * 
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}

	/**
	 * 应用程序是否发出提示音
	 * 
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}

	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (!StringUtils.isEmpty(extraInfo)) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}

	/**
	 * 判断当前版本是否兼容目标版本的方法
	 * 
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}

	/**
	 * 获取App安装包信息
	 * 
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace(System.err);
		}
		if (info == null)
			info = new PackageInfo();
		return info;
	}

	/**
	 * 获取版本号
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getVersionName() {
		return getPackageInfo().versionName;
	}

	public int getVersionCode() {
		return getPackageInfo().versionCode;
	}

	/**
	 * 获取App唯一标识
	 * 
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if (StringUtils.isEmpty(uniqueID)) {
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}

	/**
	 * 获取设备唯一标识
	 */
	public String getDeviceId() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 用户是否登录
	 * 
	 * @return
	 */
	public int getLoginState() {
		return loginState;
	}

	/**
	 * 获取登录用户id
	 * 
	 * @return
	 */
	public int getLoginUid() {
		return this.loginUid;
	}

	public String getUsername() {
		return this.username;
	}

	public String getNickname() {
		return this.nickname;
	}

	/**
	 * 用户注销
	 */
	public void Logout() {
		ApiClient.cleanCookie();
		this.cleanCookie();
		this.loginState = UNLOGIN;
		this.loginUid = 0;
		this.username = this.nickname = null;
	}

	/**
	 * 未登录或修改密码后的处理
	 */
	public Handler getUnLoginHandler() {
		return this.unLoginHandler;
	}

	//
	// /**
	// * 初始化用户登录信息
	// */
	// public void initLoginInfo() {
	// User loginUser = getLoginInfo();
	// if(loginUser!=null && loginUser.getUid()!=null &&
	// loginUser.isRememberMe()){
	// this.loginUid = loginUser.getUid();
	// this.login = true;
	// }else{
	// this.Logout();
	// }
	// }

	/**
	 * 用户登录验证
	 * 
	 * @param account
	 * @param pwd
	 * @return
	 * @throws AppException
	 */
	// public User loginVerify(String account, String pwd) throws AppException {
	// return ApiClient.login(this, account, pwd);
	// }

	/**
	 * 保存登录信息
	 * 
	 * @param username
	 * @param pwd
	 */
	public void saveLoginInfo(final User user) {
		this.loginUid = user.getUid();
		this.loginState = LOGINED;
		this.username = user.getUsername();
		this.nickname = user.getNickname();
		setProperties(new Properties() {
			{
				setProperty("user.uid", String.valueOf(user.getUid()));
				// setProperty("user.name", user.getNickname());
				// setProperty("user.face",
				// FileUtils.getFileName(user.getFace()));// 用户头像-文件名
				setProperty("user.account", user.getUsername());
				setProperty("user.pwd",
						CyptoUtils.encode("changheng", user.getPassword()));
				setProperty(
						AppConfig.CONF_LIMIT_DATE,
						user.getLimitTime() == null ? CyptoUtils.encode(
								"changheng", "2013-12-30") : CyptoUtils.encode(
								"changheng", user.getLimitTime()));
//				setProperty(
//						AppConfig.CONF_REST_HOURS,
//						user.getRestTime() == null ? CyptoUtils.encode(
//								"changheng", 24 * 90 + "") : CyptoUtils.encode(
//								"changheng", user.getRestTime().toString()));
				// setProperty("user.location", user.getLocation());
				// setProperty("user.deviceid", user.getDeviceId());
				// setProperty("user.isRememberMe",
				// String.valueOf(user.isRememberMe()));//是否记住我的信息
			}
		});
		AppConfig appConfig = AppConfig.getAppConfig(this);
		if(appConfig.getRestHours()==null)
		{
			appConfig.setRestHours(user.getRestTime()==null?24*90:user.getRestTime());
		}
		alarm();
	}

	public void saveLocalLoginInfo(String username) {
		this.loginState = LOCAL_LOGINED;
		this.username = username;
	}

	/**
	 * 清除登录信息
	 */
	public void cleanLoginInfo() {
		this.loginUid = 0;
		this.loginState = UNLOGIN;
		this.username = this.nickname = null;
		// removeProperty("user.uid", "user.name", "user.face", "user.account",
		// "user.pwd", "user.location", "user.followers", "user.fans",
		// "user.score", "user.isRememberMe");
	}

	/**
	 * 获取登录信息
	 * 
	 * @return
	 */
	public User getLoginInfo() {
		User lu = new User();
		lu.setUid(StringUtils.toInt(getProperty("user.uid")));
		lu.setNickname(getProperty("user.name"));
		lu.setFace(getProperty("user.face"));
		lu.setUsername(getProperty("user.account"));
		lu.setPassword(CyptoUtils.decode("changheng", getProperty("user.pwd")));
		lu.setLocation(getProperty("user.location"));
		return lu;
	}

	/**
	 * 保存用户头像
	 * 
	 * @param fileName
	 * @param bitmap
	 */
	public void saveUserFace(String fileName, Bitmap bitmap) {
		try {
			ImageUtils.saveImage(this, fileName, bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取用户头像
	 * 
	 * @param key
	 * @return
	 * @throws AppException
	 */
	public Bitmap getUserFace(String key) throws AppException {
		FileInputStream fis = null;
		try {
			fis = openFileInput(key);
			return BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			throw AppException.run(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 是否加载显示文章图片
	 * 
	 * @return
	 */
	public boolean isLoadImage() {
		String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
		// 默认是加载的
		if (StringUtils.isEmpty(perf_loadimage))
			return true;
		else
			return StringUtils.toBool(perf_loadimage);
	}

	/**
	 * 设置是否加载文章图片
	 * 
	 * @param b
	 */
	public void setConfigLoadimage(boolean b) {
		setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
	}

	/**
	 * 是否发出提示音
	 * 
	 * @return
	 */
	public boolean isVoice() {
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		// 默认是开启提示声音
		if (StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}

	/**
	 * 设置是否发出提示音
	 * 
	 * @param b
	 */
	public void setConfigVoice(boolean b) {
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}

	/**
	 * 是否启动检查更新
	 * 
	 * @return
	 */
	public boolean isCheckUp() {
		String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
		// 默认是开启
		if (StringUtils.isEmpty(perf_checkup))
			return true;
		else
			return StringUtils.toBool(perf_checkup);
	}

	/**
	 * 是否自动登录
	 */
	public boolean isAutoLogin() {
		String perf_autoLogin = getProperty(AppConfig.CONF_AUTOLOGIN);
		if (StringUtils.isEmpty(perf_autoLogin))
			return false;
		else
			return StringUtils.toBool(perf_autoLogin);
	}

	/**
	 * 设置启动检查更新
	 * 
	 * @param b
	 */
	public void setConfigCheckUp(boolean b) {
		setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
	}

	/**
	 * 是否左右滑动
	 * 
	 * @return
	 */
	public boolean isScroll() {
		String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
		// 默认是关闭左右滑动
		if (StringUtils.isEmpty(perf_scroll))
			return false;
		else
			return StringUtils.toBool(perf_scroll);
	}

	/**
	 * 清除保存的缓存
	 */
	public void cleanCookie() {
		removeProperty(AppConfig.CONF_COOKIE);
	}

	/**
	 * 判断缓存数据是否可读
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isReadDataCache(String cachefile) {
		return readObject(cachefile) != null;
	}

	/**
	 * 判断缓存是否存在
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isExistDataCache(String cachefile) {
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists())
			exist = true;
		return exist;
	}

	/**
	 * 判断缓存是否失效
	 * 
	 * @param cachefile
	 * @return
	 */
	public boolean isCacheDataFailure(String cachefile) {
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists()
				&& (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if (!data.exists())
			failure = true;
		return failure;
	}

	/**
	 * 清除app缓存
	 */
	public void clearAppCache() {
		// 清除webview缓存
		// File file = CacheManager.getCacheFileBaseDir();
		// if (file != null && file.exists() && file.isDirectory()) {
		// for (File item : file.listFiles()) {
		// item.delete();
		// }
		// file.delete();
		// }
		deleteDatabase("webview.db");
		deleteDatabase("webview.db-shm");
		deleteDatabase("webview.db-wal");
		deleteDatabase("webviewCache.db");
		deleteDatabase("webviewCache.db-shm");
		deleteDatabase("webviewCache.db-wal");
		// 清除数据缓存
		clearCacheFolder(getFilesDir(), System.currentTimeMillis());
		clearCacheFolder(getCacheDir(), System.currentTimeMillis());
		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
					System.currentTimeMillis());
		}
		// 清除编辑器保存的临时内容
		Properties props = getProperties();
		for (Object key : props.keySet()) {
			String _key = key.toString();
			if (_key.startsWith("temp"))
				removeProperty(_key);
		}
	}

	/**
	 * 清除缓存目录
	 * 
	 * @param dir
	 *            目录
	 * @param numDays
	 *            当前系统时间
	 * @return
	 */
	private int clearCacheFolder(File dir, long curTime) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, curTime);
					}
					if (child.lastModified() < curTime) {
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	/**
	 * 将对象保存到内存缓存中
	 * 
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}

	/**
	 * 从内存缓存中获取对象
	 * 
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key) {
		return memCacheRegion.get(key);
	}

	/**
	 * 保存磁盘缓存
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取磁盘缓存数据
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try {
			fis = openFileInput("cache_" + key + ".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存对象
	 * 
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 计算缓存大小
	 * 
	 * @return
	 */
	public String calculateCacheSize() {
		long fileSize = 0;
		String cacheSize = "0KB";
		File filesDir = getFilesDir();
		File cacheDir = getCacheDir();

		fileSize += FileUtils.getDirSize(filesDir);
		fileSize += FileUtils.getDirSize(cacheDir);

		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (AppContext.isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			File externalCacheDir = MethodsCompat.getExternalCacheDir(this);
			fileSize += FileUtils.getDirSize(externalCacheDir);
		}
		if (fileSize > 0)
			cacheSize = FileUtils.formatFileSize(fileSize);
		return cacheSize;
	}

	/**
	 * 读取对象
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file) {
		if (!isExistDataCache(file))
			return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			// 反序列化失败 - 删除缓存文件
			if (e instanceof InvalidClassException) {
				File data = getFileStreamPath(file);
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

	public boolean containsProperty(String key) {
		Properties props = getProperties();
		return props.containsKey(key);
	}

	public void setProperties(Properties ps) {
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties() {
		return AppConfig.getAppConfig(this).get();
	}

	public void setProperty(String key, String value) {
		AppConfig.getAppConfig(this).set(key, value);
	}

	public String getProperty(String key) {
		return AppConfig.getAppConfig(this).get(key);
	}

	public void removeProperty(String... key) {
		AppConfig.getAppConfig(this).remove(key);
	}

	/**
	 * 获取内存中保存图片的路径
	 * 
	 * @return
	 */
	public String getSaveImagePath() {
		return saveImagePath;
	}

	/**
	 * 设置内存中保存图片的路径
	 * 
	 * @return
	 */
	public void setSaveImagePath(String saveImagePath) {
		this.saveImagePath = saveImagePath;
	}

	public void setLoginState(int loginState) {
		this.loginState = loginState;
	}

	/**
	 * 新闻列表
	 * 
	 * @param catalog
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws ApiException
	 */
	public NewsList getNewsList(int catalog, int pageIndex, int areacode,
			boolean isRefresh) throws AppException {
		NewsList list = null;
		String key = "newslist_" + catalog + "_" + pageIndex + "_" + PAGE_SIZE;
		if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try {
				list = ApiClient.getNewsList(this, catalog, pageIndex,
						PAGE_SIZE, AreaUtils.areaCode);
				if (list != null && pageIndex == 0) {
					list.setCacheKey(key);
					saveObject(list, key);
				}
			} catch (AppException e) {
				list = (NewsList) readObject(key);
				if (list == null)
					throw e;
			}
		} else {
			list = (NewsList) readObject(key);
			if (list == null && !isNetworkConnected())
				throw AppException.http(0);
		}
		return list;
	}

	/**
	 * 新闻详情
	 * 
	 * @param news_id
	 * @return
	 * @throws ApiException
	 */
	public Info getNews(int news_id, boolean isRefresh) throws AppException {
		Info news = null;
		String key = "news_" + news_id;
		// 如果连了网,并且是重新加载 或者 连了网但是缓存数据不可读,重新去联网加载数据
		if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try {
				// 联网加载数据
				news = ApiClient.getNewsDetail(this, news_id);
				// 不为空
				if (news != null && news.getId() > 0) {
					news.setCacheKey(key);
					// 将序列化数据保存到文件系统中file文件夹下
					saveObject(news, key);
				}
			} catch (AppException e) {
				// 从手机文件系统中file下读取文件
				news = (Info) readObject(key);
				if (news == null)
					// 若为空,抛异常
					throw e;
			}
		} else {
			news = (Info) readObject(key);
			if (news == null)
				news = new Info();
		}
		return news;
	}

	// /**
	// * 帖子列表
	// * @param pageIndex
	// * @param pageSize
	// * @return
	// * @throws AppException
	// */
	// public ForumPostList getPostList(int catalog, int pageIndex, boolean
	// isRefresh) throws AppException {
	// ForumPostList list = new ForumPostList();
	// String key = "postlist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
	// if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
	// try{
	// ArrayList<ForumPost> l = ApiClient.getForumPostList(this, pageIndex,
	// PAGE_SIZE);
	// if(l != null && pageIndex == 0){
	// list.setCacheKey(key);
	// saveObject(list, key);
	// }
	// }catch(AppException e){
	// list = (ForumPostList)readObject(key);
	// if(list == null)
	// throw e;
	// }
	// } else {
	// list = (ForumPostList)readObject(key);
	// if(list == null)
	// list = new ForumPostList();
	// }
	// return list;
	// }

	public Notice getUserNotice(int uid) throws AppException {
		// TODO Auto-generated method stub
		return ApiClient.getUserNotice(this, uid);
	}

	// 自动登录线程
	public class AutoLoginThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(60 * 1000 * 3);
				System.out.println("主界面检测登录线程启动。。。。。。");
				String username = getProperty("user.account");
				String pwd = CyptoUtils.decode("changheng",
						getProperty("user.pwd"));
				if (!isAutoLogin()) {
					return;
				}
				while (getLoginState() != LOGINED) {
					if (username == null) {
						return;
					}
					// setLoginState(AppContext.LOGINING); // 登录中
					InputStream stream = ApiClient.login_get_stream(
							AppContext.this, username, pwd,
							AppContext.this.getDeviceId());
					ParseResult pr = XMLParseUtil.parseLogin(stream, username,
							pwd);
					if (pr.Ok()) {
						saveLoginInfo((User) pr.obj);
					}
				}
			} catch (Exception e) {
				if (getLoginState() != AppContext.LOCAL_LOGINED)
					setLoginState(AppContext.LOGIN_FAIL);
			}
		}
	}

	public void uploadError(String error, String paperId) {
		String key = "uploaderrortempstring";
		String pre_error = (String) readObject(key);
		System.out.println("pre_error = " + pre_error);
		if (pre_error != null) {
			error = error + "|||" + pre_error;
		}
		if (isNetworkConnected() && (!isReadDataCache(key))) {
			try {
				if (!ApiClient.uploadErrorQuestions(this, username, error,
						paperId)) {
					saveObject(error, key);
				}
			} catch (AppException e) {
				e.printStackTrace();
				saveObject(error, key);
			}
		} else {
			saveObject(error, key);
		}
	}

	public AppUpdate getAppUpdate() throws AppException {
		AppUpdate update = null;
		String key = "appUpdateInfo";
		if (!isNetworkConnected()) {
			throw AppException.http(0);
		}
		if (isReadDataCache(key)) // 可读
		{
			System.out.println("可读..........");
			update = (AppUpdate) readObject(key);
			if (!update.isNeedUpdate(getVersionCode())) {
				update = ApiClient.checkVersion(this);
				if (update != null) {
					update.setCacheKey(key);
					saveObject(update, key);
				}
			}
		} else {
			System.out.println("不可读...........");
			update = ApiClient.checkVersion(this);
			if (update != null) {
				update.setCacheKey(key);
				saveObject(update, key);
			}
		}
		System.out.println(update);
		return update;
	}

	public AppUpdate getDataUpdate() throws AppException {
		AppUpdate update = null;
		String key = "dataUpdateInfo";
		if (!isNetworkConnected()) {
			throw AppException.http(0);
		}
		SharedPreferences pref = getSharedPreferences("first_pref", 0);
		final String addtime = pref.getString("DBAddTime", null);
		if (isReadDataCache(key)) // 可读
		{
			System.out.println("可读..........");
			update = (AppUpdate) readObject(key);
			if (!update.isDataNeedUpdate(addtime)) {
				update = ApiClient.checkDataUpdate(this);
				if (update != null) {
					update.setCacheKey(key);
					saveObject(update, key);
				}
			}
		} else {
			System.out.println("不可读...........");
			update = ApiClient.checkDataUpdate(this);
			if (update != null) {
				update.setCacheKey(key);
				saveObject(update, key);
			}
		}
		System.out.println(update);
		return update;
	}

	public void alarm() {
		Integer hours = AppConfig.getAppConfig(this).getRestHours();
		if(hours==null||hours <= 0)
		{
			return;
		}
		Log.e("AppContext","启动alarm.....");
		Intent intent = new Intent(this, TimeBroadcast.class);
		intent.setAction("repeating");
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

		// 开始时间，一小时以后
		long firstime = SystemClock.elapsedRealtime()+ 60*60*1000;

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		// 一个小时一个周期，不停的发送广播
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,
				60*60*1000, sender);
	}
	// public AppUpdate isNeedUpdate()
	// {
	// AppUpdate update = (AppUpdate)readObject("appUpdateInfo");
	// if(update == null) return null;
	// if(update.getVersionCode()>getVersionCode())
	// {
	// return update;
	// }
	// return null;
	// }
}
