package com.changheng.accountant.ui;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.R;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.User;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.view.ImgRightEditText;

public class RegisterActivity extends BaseActivity implements OnClickListener{
	private TextView nameInfo, pwdInfo, pwd2Info, emailInfo, phoneInfo;
	private ImgRightEditText nameView, pwdView, pwd2View, emailView, phoneView;
	private ProgressDialog dialog;
	private Handler handler;
	private SharedPreferences abfile;
	private AppContext appContext;
	private String username,pwd,name,phone;
	private InputMethodManager imm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_register);
		appContext = (AppContext) getApplication();
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		initViews();
	}

	private void initViews() {
		this.nameInfo = (TextView) this.findViewById(R.id.regist_tvUserName);
		this.pwdInfo = (TextView) this.findViewById(R.id.regist_tvPassword);
		this.pwd2Info = (TextView) this
				.findViewById(R.id.regist_tvConfirimPass);
		this.emailInfo = (TextView) this.findViewById(R.id.regist_tvEmail);
		this.phoneInfo = (TextView) this.findViewById(R.id.regist_tvPhone);

		this.nameView = (ImgRightEditText) this
				.findViewById(R.id.regist_etUserName);
		this.pwdView = (ImgRightEditText) this
				.findViewById(R.id.regist_etPassword);
		this.pwd2View = (ImgRightEditText) this
				.findViewById(R.id.regist_etConfirimPass);
		this.emailView = (ImgRightEditText) this
				.findViewById(R.id.regist_email);
		this.phoneView = (ImgRightEditText) this.findViewById(R.id.regist_tel);
		this.findViewById(R.id.btn_goback).setOnClickListener(this);
		this.findViewById(R.id.regist_btnSubmit).setOnClickListener(this);
		handler = new MyHandler(this);
		abfile = getSharedPreferences("abfile", 0);
		nameView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus)
				{
					final String str1 = nameView.getText().toString().trim();
					boolean bool6 = Pattern.compile("^[_,0-9,a-z,A-Z]+$").matcher(str1)
							.matches();
					if (str1.equals("")) {
						showInfo("用户名不能为空!",nameInfo,nameView,0);
						return;
					}
					if (!bool6) {
						showInfo("用户名只能使用字母,数字,下划线'_'组成!",nameInfo,nameView,0);
						return;
					}
					if ((str1.length() < 4) || (str1.length()> 20))
				    {
						showInfo("用户名应在4-18位之间！",nameInfo,nameView,0);
						return;
				    }
					//开线程去验证用户名是否被占用
					//
					new Thread(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							ParseResult result = null;
							try{
								result = ApiClient.checkUsername2(appContext, str1);
								if(result.Ok())
								{
									handler.sendEmptyMessage(1001);
								}else
								{
									handler.sendEmptyMessage(1002);
								}
							}catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
		});
		pwdView.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				String str3 = pwdView.getText().toString().trim();
				String str4 = pwd2View.getText().toString().trim();
				if(!str4.equals(str3))
				{
					showInfo("*确认密码:两次输入不一致",pwd2Info,pwd2View,0);
				}else
				{
					showInfo("*确认密码:",pwd2Info,pwd2View,1);
				}
				boolean bool7 = Pattern.compile("^[0-9,a-z,A-Z]+$").matcher(str3).matches();
				if (str3.equals("")) {
					showInfo("密码不能为空!",pwdInfo,pwdView,0);
					return ;
				}
				if (!bool7) {
					showInfo("密码只能使用字母,数字组成!",pwdInfo,pwdView,0);
					return ;
				}
				if ((str3.length() < 4) || (str3.length()> 15)) {
					showInfo("密码4-15位字符!",pwdInfo,pwdView,0);
					return ;
				}
				showInfo("*密码:",pwdInfo,pwdView,1);
			}
		});
		pwd2View.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String str3 = pwdView.getText().toString().trim();
				String str4 = pwd2View.getText().toString().trim();
				if(!str4.equals(str3))
				{
					showInfo("*确认密码:两次输入不一致",pwd2Info,pwd2View,0);
					return;
				}else
				{
					showInfo("*确认密码:",pwd2Info,pwd2View,1);
					return;
				}
			}
		});
		emailView.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String str2 = emailView.getText().toString().trim();
//				boolean bool1 = Pattern
//						.compile(
//								"^([a-z0-9A-Z]+[-|_\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")
//						.matcher(str2).matches();
				boolean bool1 = Pattern.compile("^([\\u4e00-\\u9fa5]+|([a-z]+\\s?)+)$").matcher(str2).matches();
				if (str2.equals("")) {
					showInfo("姓名不能为空!",emailInfo,emailView,0);
					return ;
				}
				if (!bool1) {
					showInfo("姓名格式错误!",emailInfo,emailView,0);
					return ;
				}
				showInfo("*真实姓名:",emailInfo,emailView,1);
			}
		});
		phoneView.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String str5 = phoneView.getText().toString().trim();
				boolean bool3 = Pattern.compile("^[1][3,4,5,6,8]\\d{9}$")
						.matcher(str5).matches();
				if("".equals(str5))
				{
					showInfo("手机号不能为空!",phoneInfo,phoneView,0);
					return ;
				}
				if(!bool3)
				{
					showInfo("手机号格式错误!",phoneInfo,phoneView,0);
					return ;
				}
				showInfo("手机号码",phoneInfo,phoneView,1);
			}
		});
		phoneView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_GO)
				{
					register();
				}
				return true;
			}
		});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.regist_btnSubmit:
			register();
			break;
		}
	}
	private void register()
	{
		//隐藏软键盘
		imm.hideSoftInputFromWindow(phoneView.getWindowToken(), 0);
		//检查登录
		if(checkInput())
		{
			//开线程去登陆
			//检查网络
			if(!checkNetWork())
			{
				return;
			}
			dialog = ProgressDialog.show(RegisterActivity.this, null, "注册中请稍候",
					true, true);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			new Thread(){
				public void run() {
					ParseResult result = null;
					Message msg = handler.obtainMessage();
					try
					{
						result = ApiClient.register2(appContext, username, pwd, appContext.getDeviceId(), name, phone, "北京");
						msg.what = 1;
						msg.obj = result;
						handler.sendMessage(msg);
					}catch(Exception e)
					{
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
						handler.sendMessage(msg);
					}
				};
			}.start();
		}
	}
	private boolean checkInput() {
		username = this.nameView.getText().toString().trim();
		name = this.emailView.getText().toString().trim();
		pwd = this.pwdView.getText().toString().trim();
		String str4 = this.pwd2View.getText().toString().trim();
		phone = this.phoneView.getText().toString().trim();
		boolean bool6 = Pattern.compile("^[_,0-9,a-z,A-Z]+$").matcher(username)
				.matches();
		if (username.equals("")) {
			showMsg("用户名不能为空!");
			return false;
		}
		if (!bool6) {
			showMsg("用户名只能使用字母,数字,下划线'_'组成!");
			return false;
		}
		if ((username.length() < 4) || (username.length()> 20))
	    {
			showMsg("会员名应在4-18位之间！");
			return false;
	    }
		boolean bool7 = Pattern.compile("^[0-9,a-z,A-Z]+$").matcher(pwd).matches();
		if (pwd.equals("")) {
			showMsg("密码不能为空!");
			return false;
		}
		if (!bool7) {
			showMsg("密码只能使用字母,数字组成!");
			return false;
		}
		if ((pwd.length() < 4) || (pwd.length()> 15)) {
			showMsg("密码4-15位字符!");
			return false;
		}
		if(!pwd.equals(str4))
		{
			showMsg("两次密码输入不一致!");
			return false;
		}
//		boolean bool1 = Pattern
//				.compile(
//						"^([a-z0-9A-Z]+[-|_\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")
//				.matcher(str2).matches();
		boolean bool1 = Pattern
				.compile(
						"^([\\u4e00-\\u9fa5]+|([a-z]+\\s?)+)$")
				.matcher(name).matches();
		if (name.equals("")) {
			showMsg("姓名不能为空!");
			return false;
		}
		if (!bool1) {
			showMsg("姓名格式错误!");
			return false;
		}
		boolean bool3 = Pattern.compile("^[1][3,4,5,6,8]\\d{9}$")
				.matcher(phone).matches();
		if("".equals(phone))
		{
			showMsg("手机号不能为空!");
			return false;
		}
		if(!bool3)
		{
			showMsg("手机号格式错误!");
			return false;
		}
		return true;
	}
	// 检查网络
		private boolean checkNetWork() {
			ConnectivityManager manager = (ConnectivityManager) this
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info == null || !info.isConnected()) {
				showMsg("请检查网络");
				return false;
			}
			return true;
		}
	private void showMsg(String msg) {
		Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
	private void showInfo(String msg,TextView info,ImgRightEditText view,int i)
	{
		if(1==i)	//对的
		{
			info.setText(msg);
			info.setTextColor(getResources().getColor(R.color.black));
			view.setRightImg(R.drawable.can_regeist);
		}else
		{
			info.setText(msg);
			info.setTextColor(getResources().getColor(R.color.red));
			view.setRightImg(0);
		}
	}
	static class MyHandler extends Handler
	{
		WeakReference<RegisterActivity> mActivity;
		public MyHandler(RegisterActivity activity) {
			mActivity = new WeakReference<RegisterActivity>(activity);
		 }
		public MyHandler() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			RegisterActivity r = mActivity.get();
			if(r.dialog!=null)
			{
				r.dialog.dismiss();
			}
			switch(msg.what)
			{
			case 1:
				//
				ParseResult result = (ParseResult)msg.obj;
				if(result.Ok())	//注册成功
				{
					User u = (User) (result.getObj());
					Toast.makeText(r, "注册成功,请登录", Toast.LENGTH_SHORT).show();
					r.abfile.edit().putString("n",u.getUsername()).commit();
					r.abfile.edit().putString("p","").commit();
//					Intent intent = new Intent(r,LoginActivity.class);
//					r.startActivity(intent);
					r.finish();
				}else
				{
					Toast.makeText(r, "注册失败,"+result.getErrorMsg(), Toast.LENGTH_SHORT).show();
				}
				break;
			case -1:
				((AppException)msg.obj).makeToast(r);
				break;
//			case -2:
//			case -3:
//				Toast.makeText(r, "注册失败", Toast.LENGTH_LONG).show();
//				break;
			case 1001:
				r.showInfo("*用户名:",r.nameInfo,r.nameView,1);
				break;
			case 1002:
				r.showInfo("*该用户名被占用",r.nameInfo,r.nameView,0);
				break;
			}
		}
	}
}
