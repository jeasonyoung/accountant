package com.changheng.accountant.ui;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.changheng.accountant.AppConfig;
import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.dao.UserDao;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.User;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.URLs;
import com.changheng.accountant.util.XMLParseUtil;

public class LoginActivity extends BaseActivity implements TextWatcher,
		OnClickListener {
	private AutoCompleteTextView usernameText;
	private String[] items;// 适配autoCompleteTextView的数据
	private EditText pwdText;
	private Button localLoginBtn;
	private ProgressDialog o;
	private Handler handler;
	private CheckBox rememeberCheck;
	private CheckBox autoLogin;
	private String username;
	private String password;
	private SharedPreferences share;
	private SharedPreferences share2;
	private AppConfig appConfig;
	private AppContext appContext;
	private int curLoginType;
	private Class fromClass;
	private String actionName;
	private UserDao userdao;
	private InputMethodManager imm;
	public final static int LOGIN_OTHER = 0x00;
	public final static int LOGIN_MAIN = 0x01;
	public final static int LOGIN_SETTING = 0x02;
	public final static int LOGIN_POST_PUB = 0x05;
	public final static int LOGIN_POST_REPLY = 0x06;
	
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_login);
		appConfig = AppConfig.getAppConfig(this);
		appContext = (AppContext) this.getApplication();
		curLoginType = this.getIntent().getIntExtra("loginFrom", LOGIN_OTHER);
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		try {
			fromClass = Class.forName(this.getIntent().getStringExtra(
					"className"));
			actionName = this.getIntent().getStringExtra("actionName");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initView();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		String name = usernameText.getText().toString();
		pwdText.setText(new String(Base64.decode(
				Base64.decode(share.getString(name, ""), 0), 0)));
		if (pwdText.getText().toString().length() > 0)
			pwdText.requestFocus();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	// click事件
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.registerBtn:
			gotoRegister();
			break;
		case R.id.btnLogin:
			login();
			break;
		case R.id.btnLocalLogin:
			localLogin();
			break;
		}
	}

	// 登录方法
	private void login() {
		// 隐藏键盘
		if (imm.isActive(usernameText)) {
			imm.hideSoftInputFromWindow(usernameText.getWindowToken(), 0);
		}
		if (imm.isActive(pwdText)) {
			imm.hideSoftInputFromWindow(pwdText.getWindowToken(), 0);
		}
		if(alertDialog!=null)
		{
			alertDialog.show();
			return;
		}
		username = usernameText.getText().toString().trim();
		password = pwdText.getText().toString().trim();
		final String deviceId = appContext.getDeviceId();
		// 检查输入
		if (checkInput(username, password)) {
			// 检查网络
			if (checkNetWork()) {
				// 提示登陆中
				if (appContext.getLoginState() == AppContext.LOGINING) {
					if (o != null) {
						o.show();
						return;
					}
				}
				o = ProgressDialog.show(LoginActivity.this, null, "登录中请稍候",
						true, true);
				o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				new Thread() {
					public void run() {
						// String result = null;
						try {
							// result = ApiClient.login_get(appContext,
							// username, password, deviceId);
							// // 解析字符串
							// System.out.println(result);
							// ParseResult pr =
							// JsonParseUtil.parseLogin(result,username,password);
							// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
							appContext.setLoginState(AppContext.LOGINING);// 正在登录
							InputStream stream = ApiClient.login_get_stream(
									appContext, username, password, deviceId);
							ParseResult pr = XMLParseUtil.parseLogin(stream,
									username, password);
							if (pr.Ok()) { // 登陆成功
								// 是否记住我
								if (isRememberMe()) {
									saveSharePreferences();
								}
								// 保存是否自动登录的信息
								saveAutoLoginPreferences(isAutoLogin());
								// 保存信息至数据库
								// System.out.println("user.getUid ="+((User)pr.obj).getUid()+"=");
								saveToLocaleDB((User) pr.obj);
							}
							Message message = handler.obtainMessage();
							message.what = 1;
							message.obj = pr;
							handler.sendMessage(message);
						} catch (Exception e) {
							e.printStackTrace();
							handler.sendEmptyMessage(-1); // 连接问题
						}
					};
				}.start();
			} else {
				if (appContext.getLoginState() != AppContext.LOCAL_LOGINED
						|| "sysnc".equals(curLoginType)) // 本地已经登录就不再显示
					localLoginBtn.setVisibility(View.VISIBLE);
			}
		}
	}

	private void localLogin() {
		if(alertDialog!=null)
		{
			alertDialog.show();
			return;
		}
		if (userdao == null)
			userdao = new UserDao(this);
		username = usernameText.getText().toString().trim();
		password = pwdText.getText().toString().trim();
		if (checkInput(username, password)) {
			// String name = usernameText.getText().toString();
			User user = userdao.findByUsername(username);
			if (user != null) {
				String password = pwdText.getText().toString();
				if (password.equals(new String(Base64.decode(
						Base64.decode(user.getPassword(), 0), 0)))) {
					appContext.saveLocalLoginInfo(username);
					showToast("本地登录成功");
					if (fromClass == null) {
						LoginActivity.this.finish(); // 找不到类直接finish
					} else {
//						Intent intent = new Intent(LoginActivity.this,
//								fromClass);
//						if (LoginActivity.this.actionName != null) {
//							intent.putExtra("actionName",
//									LoginActivity.this.actionName);
//						}
//						LoginActivity.this.startActivity(intent);
//						LoginActivity.this.finish();
						startActivity();
					}
				} else {
					showToast("请先在线登录");
				}
			} else {
				showToast("请先在线登录");
			}
		}
	}

	private void showToast(String content) {
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}

	// 检查输入 check input
	private boolean checkInput(String username, String password) {
		if ("".equals(username.trim()) || "".equals(password.trim())) {
			Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	// 检查网络 check network
	private boolean checkNetWork() {
		ConnectivityManager manager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			Toast.makeText(LoginActivity.this, "请检查网络", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	// 是否记住密码
	private boolean isRememberMe() {
		return rememeberCheck.isChecked();
	}

	// 是否自动登录
	private boolean isAutoLogin() {
		return autoLogin.isChecked();
	}

	// 保存信息
	private void saveSharePreferences() {
		share.edit()
				.putString(
						usernameText.getText().toString(),
						Base64.encodeToString(
								Base64.encode(password.getBytes(), 0), 0))
				.commit();
		share2.edit().putString("n", usernameText.getText().toString())
				.commit();
		share2.edit()
				.putString(
						"p",
						Base64.encodeToString(
								Base64.encode(password.getBytes(), 0), 0))
				.commit();
	}

	// 保存自动登录信息
	private void saveAutoLoginPreferences(boolean flag) {
		appConfig.set(AppConfig.CONF_AUTOLOGIN, String.valueOf(flag));
	}

	// 注册
	private void gotoRegister() {
		this.startActivity(new Intent(this, RegisterActivity.class));
	}

	// 保存用户信息至本地数据库
	public void saveToLocaleDB(User user) {
		if (userdao == null) {
			userdao = new UserDao(this);
		}
		try {
			userdao.saveOrUpdate(user);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 初始化组件
	private void initView() {
		usernameText = (AutoCompleteTextView) this
				.findViewById(R.id.usernameText);// 用户名
		pwdText = (EditText) this.findViewById(R.id.pwdText);// 密码
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		((TextView) this.findViewById(R.id.title)).setText("登录");
		this.findViewById(R.id.btnLogin).setOnClickListener(this);// 登录按钮
		localLoginBtn = (Button) this.findViewById(R.id.btnLocalLogin); // 本地登录
		rememeberCheck = (CheckBox) this.findViewById(R.id.rememeberCheck);// 记住密码
		autoLogin = (CheckBox) this.findViewById(R.id.cbAutoLogin); // 自动登录
		this.findViewById(R.id.registerBtn).setOnClickListener(this);
		// userdao = new UserDao(new MyDBHelper(this)); //������ݿ�
		share = getSharedPreferences("passwordfile", 0);
		share2 = getSharedPreferences("abfile", 0);
		items = share.getAll().keySet().toArray(new String[0]);
		usernameText.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, items));
		pwdText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_GO)
					// 去登陆
					login();
				return true;
			}
		});
		usernameText.addTextChangedListener(this);
		localLoginBtn.setOnClickListener(this);
		handler = new MyHandler(this);
	}

	static class MyHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		public MyHandler(LoginActivity activity) {
			mActivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// ȥ������
			LoginActivity login = mActivity.get();
			if (login.o != null && login.o.isShowing()) {
				login.o.dismiss();
			}
			switch (msg.what) {
			case 1:
				// 登录成功
				ParseResult pr = (ParseResult) msg.obj;
				if (pr.Ok()) {
					login.appContext.saveLoginInfo((User) pr.obj);
					int code = 0;
					if (login.curLoginType == LOGIN_MAIN) {
						code = 20;
						Intent intent = new Intent();
						login.setResult(code, intent);
						login.finish();
					} else if (login.curLoginType == LOGIN_SETTING) {
						code = 30;
						Intent intent = new Intent();
						login.setResult(code, intent);
						login.finish();
					} else if (login.curLoginType == LOGIN_POST_PUB) {
						Intent intent = new Intent(login,
								ForumPostPubActivity.class);
						login.startActivity(intent);
						login.finish();
					} else if (login.curLoginType == LOGIN_POST_REPLY) {
						code = 40;
						Intent intent = new Intent();
						login.setResult(code, intent);
						login.finish();
					} else {
						// 登录之后,
						if (login.fromClass == null) {
							login.finish(); // 找不到类直接finish
						} else {
							login.startActivity();
						}
					}
				} else {
					// 修改登录状态
					login.appContext.setLoginState(AppContext.LOGIN_FAIL);
					Toast.makeText(login, pr.getErrorMsg(), Toast.LENGTH_SHORT)
							.show();
					// login.localLoginBtn.setVisibility(View.VISIBLE);
				}
				break;
			case -1:
				// 修改登录状态
				login.appContext.setLoginState(AppContext.LOGIN_FAIL);
				Toast.makeText(login, "无法连接服务器", Toast.LENGTH_SHORT).show();
				// login.localLoginBtn.setVisibility(View.VISIBLE);
				login.showLocalLoginBtn();
				break;
			}
		}
	}

	private void showLocalLoginBtn() {
		if (userdao == null)
			userdao = new UserDao(this);
		User user = userdao.findByUsername(username);
		if (user != null)
			localLoginBtn.setVisibility(View.VISIBLE);
	}

	// 初始化输入框
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.usernameText.setText(share2.getString("n", ""));
		String pwd = share2.getString("p", "");
		this.pwdText
				.setText(new String(Base64.decode(Base64.decode(pwd, 0), 0)));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (o != null) {
			o.dismiss();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
	}

	private void startActivity() {
		if (appContext.isTimeOver()) {
			if(alertDialog==null)
			{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("抱歉，使用的期限已过，您可以去官网购买。");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							Uri uri = Uri.parse(URLs.URL_BUY);
							Intent it = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(it);
							finish();
						}
					});
			builder.setCancelable(true);
			alertDialog = builder.create();
			}
			alertDialog.show();
		} else {
			Intent intent = new Intent(this, this.fromClass);
			if (this.actionName != null) {
				intent.putExtra("actionName", this.actionName);
			}
			this.startActivity(intent);
			this.finish();
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(alertDialog!=null&&alertDialog.isShowing())
		{
			alertDialog.dismiss();
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
