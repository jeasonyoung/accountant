package com.changheng.accountant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.changheng.accountant.ui.UIHelper;

/**
 * 应用程序异常类：用于捕获异常和提示错误信息
 * 
 */
public class AppException extends Exception implements UncaughtExceptionHandler {

  /**
   * 
   */
  private static final long serialVersionUID = 6243307165131877535L;

  private final static boolean Debug = true;// 是否保存错误日志

  /** 定义异常类型 */
  public final static byte TYPE_NETWORK = 0x01;
  public final static byte TYPE_SOCKET = 0x02;
  public final static byte TYPE_HTTP_CODE = 0x03;
  public final static byte TYPE_HTTP_ERROR = 0x04;
  public final static byte TYPE_XML = 0x05;
  public final static byte TYPE_IO = 0x06;
  public final static byte TYPE_RUN = 0x07;

  private byte type;
  private int code;

  /** 系统默认的UncaughtException处理类 */
  private Thread.UncaughtExceptionHandler mDefaultHandler;

  private AppException() {
    this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
  }

  private AppException(byte type, int code, Exception excp) {
    super(excp);
    this.type = type;
    this.code = code;
    if (Debug) {
      this.saveErrorLog(excp);
    }
  }

  public int getCode() {
    return this.code;
  }

  public int getType() {
    return this.type;
  }

  /**
   * 提示友好的错误信息
   * 
   * @param ctx
   */
  public void makeToast(Context ctx) {
    switch (this.getType()) {
      case TYPE_HTTP_CODE:
        String err = ctx.getString(R.string.http_status_code_error, this.getCode());
        Toast.makeText(ctx, err, Toast.LENGTH_SHORT).show();
        break;
      case TYPE_HTTP_ERROR:
        Toast.makeText(ctx, R.string.http_exception_error, Toast.LENGTH_SHORT).show();
        break;
      case TYPE_SOCKET:
        Toast.makeText(ctx, R.string.socket_exception_error, Toast.LENGTH_SHORT).show();
        break;
      case TYPE_NETWORK:
        Toast.makeText(ctx, R.string.network_not_connected, Toast.LENGTH_SHORT).show();
        break;
      case TYPE_XML:
        Toast.makeText(ctx, R.string.xml_parser_failed, Toast.LENGTH_SHORT).show();
        break;
      case TYPE_IO:
        Toast.makeText(ctx, R.string.io_exception_error, Toast.LENGTH_SHORT).show();
        break;
      case TYPE_RUN:
        Toast.makeText(ctx, R.string.app_run_code_error, Toast.LENGTH_SHORT).show();
        break;
    }
  }

  /**
   * 保存异常日志
   * 
   * @param excp
   */
  public void saveErrorLog(Exception excp) {
	if(excp!=null)
		saveErrorLog(excp.getLocalizedMessage());
  }

  /**
   * 保存异常日志
   * 
   * @param excp
   */
  public void saveErrorLog(String excpMessage) {
    String errorlog = "errorlog.txt";
    String savePath = "";
    String logFilePath = "";
    FileWriter fw = null;
    PrintWriter pw = null;
    try {
      // 判断是否挂载了SD卡
      String storageState = Environment.getExternalStorageState();
      if (storageState.equals(Environment.MEDIA_MOUNTED)) {
        savePath =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "CHAccountant" + File.separator + "Log/";
        File file = new File(savePath);
        if (!file.exists()) {
          file.mkdirs();
        }
        logFilePath = savePath + errorlog;
      }
      // 没有挂载SD卡，无法写文件
      if (logFilePath == "") {
        return;
      }
      File logFile = new File(logFilePath);
      if (!logFile.exists()) {
        logFile.createNewFile();
      }
      fw = new FileWriter(logFile, true);
      pw = new PrintWriter(fw);
      pw.println("--------------------" + (DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()))
          + "---------------------");
      pw.println(excpMessage);
      pw.close();
      fw.close();
    } catch (Exception e) {
      Log.e("AppException", "[Exception]" + e.getLocalizedMessage());
    } finally {
      if (pw != null) {
        pw.close();
      }
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {}
      }
    }

  }

  public static AppException http(int code) {
    return new AppException(TYPE_HTTP_CODE, code, null);
  }

  public static AppException http(Exception e) {
    return new AppException(TYPE_HTTP_ERROR, 0, e);
  }

  public static AppException socket(Exception e) {
    return new AppException(TYPE_SOCKET, 0, e);
  }

  public static AppException io(Exception e) {
    if (e instanceof UnknownHostException || e instanceof ConnectException) {
      return new AppException(TYPE_NETWORK, 0, e);
    } else if (e instanceof IOException) {
      return new AppException(TYPE_IO, 0, e);
    }
    return run(e);
  }

  public static AppException xml(Exception e) {
    return new AppException(TYPE_XML, 0, e);
  }

  public static AppException network(Exception e) {
    if (e instanceof UnknownHostException || e instanceof ConnectException) {
      return new AppException(TYPE_NETWORK, 0, e);
    } else if (e instanceof HttpException) {
      return http(e);
    } else if (e instanceof SocketException) {
      return socket(e);
    }
    return http(e);
  }

  public static AppException run(Exception e) {
    return new AppException(TYPE_RUN, 0, e);
  }

  /**
   * 获取APP异常崩溃处理对象
   * 
   * @param context
   * @return
   */
  public static AppException getAppExceptionHandler() {
    return new AppException();
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {

    if (!handleException(ex) && mDefaultHandler != null) {
      mDefaultHandler.uncaughtException(thread, ex);
    } else {
      try {
        Thread.sleep(15000);
      } catch (InterruptedException e) {
        Log.e("AppException", "error : ", e);
      }
      // 退出程序
      android.os.Process.killProcess(android.os.Process.myPid());
      System.exit(1);
    }

  }

  /**
   * 自定义异常处理:收集错误信息&发送错误报告
   * 
   * @param ex
   * @return true:处理了该异常信息;否则返回false
   */
  private boolean handleException(Throwable ex) {
    if (ex == null) {
      return false;
    }

    final Context context = AppManager.getAppManager().currentActivity();

    if (context == null) {
      return false;
    }

    final String crashReport = getCrashReport(context, ex);
    // 显示异常信息&发送报告
    new Thread() {
      public void run() {
        Looper.prepare();
        UIHelper.sendAppCrashReport(context, crashReport);
        Looper.loop();
      }

    }.start();
    Log.e("com.accountant",crashReport);
    //saveErrorLog(crashReport); //保存错误日志
    return true;
  }

  /**
   * 获取APP崩溃异常报告
   * 
   * @param ex
   * @return
   */
  private String getCrashReport(Context context, Throwable ex) {
    PackageInfo pinfo = ((AppContext) context.getApplicationContext()).getPackageInfo();
    StringBuffer exceptionStr = new StringBuffer();
    exceptionStr.append("Version: " + pinfo.versionName + "(" + pinfo.versionCode + ")\n");
    exceptionStr.append("Android: " + android.os.Build.VERSION.RELEASE + "("
        + android.os.Build.MODEL + ")\n");
    exceptionStr.append("System package Info:" + collectDeviceInfo(context) + "\n");
    exceptionStr.append("System os Info:" + getMobileInfo() + "\n");
    exceptionStr.append("Exception: " + ex.getMessage() + "\n");
    exceptionStr.append("Exception stack：" + getTraceInfo((Activity) context, ex) + "\n");

    return exceptionStr.toString();
  }

  /**
   * 收集设备参数信息
   * 
   * @param ctx
   */
  public String collectDeviceInfo(Context ctx) {
    StringBuilder sb = new StringBuilder();
    JSONObject activePackageJson = new JSONObject();

    try {
      PackageManager pm = ctx.getPackageManager();
      PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
      if (pi != null) {
        String versionName = pi.versionName == null ? "null" : pi.versionName;
        String versionCode = pi.versionCode + "";

        activePackageJson.put("versionName", versionName);
        activePackageJson.put("versionCode", versionCode);
      }
    } catch (NameNotFoundException e) {
      Log.e("AppException", "an error occured when collect package info", e);
    } catch (JSONException e) {
      Log.e("AppException", "jsonException", e);
    }
    sb.append("[active Package]");
    sb.append(activePackageJson.toString());

    return sb.toString();
  }

  public static StringBuffer getTraceInfo(Activity a, Throwable e) {
    StringBuffer sb = new StringBuffer();

    Throwable ex = e.getCause() == null ? e : e.getCause();
    StackTraceElement[] stacks = ex.getStackTrace();
    for (int i = 0; i < stacks.length; i++) {
      sb.append("class: ").append(stacks[i].getClassName()).append("; method: ")
          .append(stacks[i].getMethodName()).append("; line: ").append(stacks[i].getLineNumber())
          .append(";  Exception: ").append(ex.toString() + "\n");
    }
    return sb;
  }

  /**
   * 获取手机的硬件信息
   * 
   * @return
   */
  public String getMobileInfo() {
    JSONObject osJson = new JSONObject();
    // 通过反射获取系统的硬件信息

    Field[] fields = Build.class.getDeclaredFields();
    for (Field field : fields) {
      try {
        field.setAccessible(true);
        osJson.put(field.getName(), field.get(null).toString());
        Log.d("AppException", field.getName() + " : " + field.get(null));
      } catch (Exception e) {
        Log.e("AppException", "an error occured when collect crash info", e);
      }
    }
    return osJson.toString();
  }
}
