package com.changheng.accountant.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.AppException;
import com.changheng.accountant.entity.AppUpdate;
import com.changheng.accountant.entity.Area;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.entity.ForumPostReply;
import com.changheng.accountant.entity.Info;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.entity.NewsList;
import com.changheng.accountant.entity.Notice;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.Plan;
import com.changheng.accountant.entity.ReplyList;
import com.changheng.accountant.entity.SyncData;

/**
 * API客户端接口：用于访问网络数据
 * 
 * @author vigo
 * @version 1.0
 * @create 2014-1-4 上午11:37:20
 */
public class ApiClient {

	public static final String UTF_8 = "UTF-8";
	public static final String DESC = "descend";
	public static final String ASC = "ascend";

	private final static int TIMEOUT_CONNECTION = 5000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int RETRY_TIME = 3;

	private static String appCookie;
	private static String appUserAgent;

	public static void cleanCookie() {
		appCookie = "";
	}

	private static String getCookie(AppContext appContext) {
		if (appCookie == null || appCookie == "") {
			appCookie = appContext.getProperty("cookie");
		}
		return appCookie;
	}

	private static String getUserAgent(AppContext appContext) {
		if (appUserAgent == null || appUserAgent == "") {
			StringBuilder ua = new StringBuilder("OSChina.NET");
			ua.append('/' + appContext.getPackageInfo().versionName + '_'
					+ appContext.getPackageInfo().versionCode);// App版本
			ua.append("/Android");// 手机系统平台
			ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
			ua.append("/" + android.os.Build.MODEL); // 手机型号
			ua.append("/" + appContext.getAppId());// 客户端唯一标识
			appUserAgent = ua.toString();
		}
		return appUserAgent;
	}

	private static HttpClient getHttpClient() {
		HttpClient httpClient = new HttpClient();
		// 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		// 设置 默认的超时重试处理策略
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());
		// 设置 连接超时时间
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(TIMEOUT_CONNECTION);
		// 设置 读数据超时时间
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(TIMEOUT_SOCKET);
		// 设置 字符集
		httpClient.getParams().setContentCharset(UTF_8);
		return httpClient;
	}

	private static GetMethod getHttpGet(String url, String cookie,
			String userAgent) {
		GetMethod httpGet = new GetMethod(url);
		// 设置 请求超时时间
		httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGet.setRequestHeader("Host", URLs.HOST);
		httpGet.setRequestHeader("Connection", "Keep-Alive");
		httpGet.setRequestHeader("Cookie", cookie);
		httpGet.setRequestHeader("User-Agent", userAgent);
		return httpGet;
	}

	private static PostMethod getHttpPost(String url, String cookie,
			String userAgent) {
		PostMethod httpPost = new PostMethod(url);
		// 设置 请求超时时间
		httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpPost.setRequestHeader("Host", URLs.HOST);
		httpPost.setRequestHeader("Connection", "Keep-Alive");
		httpPost.setRequestHeader("Cookie", cookie);
		httpPost.setRequestHeader("User-Agent", userAgent);
		return httpPost;
	}

	private static String _MakeURL(String p_url, Map<String, Object> params) {
		StringBuilder url = new StringBuilder(p_url);
		if (url.indexOf("?") < 0)
			url.append('?');

		for (String name : params.keySet()) {
			url.append('&');
			url.append(name);
			url.append('=');
			url.append(String.valueOf(params.get(name)));
			// 不做URLEncoder处理
			// url.append(URLEncoder.encode(String.valueOf(params.get(name)),
			// UTF_8));
		}
		return url.toString().replace("?&", "?");
	}

	private static String _MakePostParams(Map<String, Object> params) {
		StringBuilder url = new StringBuilder();
		for (String name : params.keySet()) {
			url.append(name);
			url.append('=');
			try {
				url.append(URLEncoder.encode(String.valueOf(params.get(name)),
						"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			url.append('&');
			// 不做URLEncoder处理
			// url.append(URLEncoder.encode(String.valueOf(params.get(name)),
			// UTF_8));
		}
		return url.substring(0, url.length());
	}

	private static InputStream _urlPost(AppContext appContext, String url,
			String params) throws AppException {
		HttpURLConnection conn = null;
		InputStream in = null;
		String cookie = getCookie(appContext);
		try {
			URL url1 = new URL(url);
			conn = (HttpURLConnection) url1.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			// post请求通过输入流传递参数，把输入输出都打开
			conn.setDoOutput(true);// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
									// http正文内，因此需要设为true, 默认情况下是false;
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			// post请求不使用缓存？？
			conn.setUseCaches(false);
			// 设置content-type
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// 设置编码
			conn.setRequestProperty("Charset", "utf-8");
			// 设置cookie
			conn.setRequestProperty("Cookie", cookie);
			// 输入参数
			DataOutputStream dop = new DataOutputStream(conn.getOutputStream());// 隐式调用了conn.connect();
			dop.writeBytes(params);// 多参数
			dop.flush();
			dop.close();
			// 连接错误
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw AppException.http(conn.getResponseCode());
			} else if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String cookieval = conn.getHeaderField("set-cookie");
				String tmpcookies = "";
				if (cookieval != null) {
					tmpcookies = cookieval.substring(0, cookieval.indexOf(";"));
				}
				// 保存cookie
				if (appContext != null && tmpcookies != "") {
					appContext.setProperty("cookie", tmpcookies);
					System.out.println(tmpcookies);
					appCookie = tmpcookies;
				}
			}
			in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer buf = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				buf.append(line);
			}
			System.out.println("返回信息为: "+buf);
			return new ByteArrayInputStream(buf.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw AppException.network(e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * get请求URL
	 * 
	 * @param url
	 * @throws AppException
	 */
	private static String http_get(AppContext appContext, String url)
			throws AppException {
		// System.out.println("get_url==> "+url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		GetMethod httpGet = null;

		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, cookie, userAgent);
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				// /////////////// 添加cookie
				else if (statusCode == HttpStatus.SC_OK) {
					Cookie[] cookies = httpClient.getState().getCookies();
					String tmpcookies = "";
					for (Cookie ck : cookies) {
						tmpcookies += ck.toString() + ";";
					}
					// 保存cookie
					if (appContext != null && tmpcookies != "") {
						appContext.setProperty("cookie", tmpcookies);
						System.out.println(tmpcookies);
						appCookie = tmpcookies;
					}
				}
				// ///////////////////////////////////////
				responseBody = httpGet.getResponseBodyAsString();
				// System.out.println("XMLDATA=====>"+responseBody);
				break;
			} catch (HttpException e) {
				e.printStackTrace();
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				e.printStackTrace();
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				if (httpGet != null)
					httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		return responseBody;
	}

	/**
	 * 公用post方法
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 */
	private static InputStream _post(AppContext appContext, String url,
			Map<String, Object> params, Map<String, File> files)
			throws AppException {
		// System.out.println("post_url==> "+url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		PostMethod httpPost = null;

		// post表单参数处理
		int length = (params == null ? 0 : params.size())
				+ (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		int i = 0;
		if (params != null)
			for (String name : params.keySet()) {
				parts[i++] = new StringPart(name, String.valueOf(params
						.get(name)), UTF_8);
				System.out.println("post_key==> " + name + "    value==>"
						+ String.valueOf(params.get(name)));
			}
		if (files != null)
			for (String file : files.keySet()) {
				try {
					parts[i++] = new FilePart(file, files.get(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				// System.out.println("post_key_file==> "+file);
			}

		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpPost = getHttpPost(url, cookie, userAgent);
				httpPost.setRequestEntity(new MultipartRequestEntity(parts,
						httpPost.getParams()));
				int statusCode = httpClient.executeMethod(httpPost);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				} else if (statusCode == HttpStatus.SC_OK) {
					Cookie[] cookies = httpClient.getState().getCookies();
					String tmpcookies = "";
					for (Cookie ck : cookies) {
						tmpcookies += ck.toString() + ";";
					}
					// 保存cookie
					if (appContext != null && tmpcookies != "") {
						appContext.setProperty("cookie", tmpcookies);
						System.out.println(tmpcookies);
						appCookie = tmpcookies;
					}
				}
				responseBody = httpPost.getResponseBodyAsString();
				// System.out.println("XMLDATA=====>"+responseBody);
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpPost.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		// return responseBody;
		return new ByteArrayInputStream(responseBody.getBytes());
	}

	/**
	 * get请求URL
	 * 
	 * @param url
	 * @throws AppException
	 */
	private static InputStream http_get_stream(AppContext appContext, String url)
			throws AppException {
//		System.out.println("get_url==> " + url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		GetMethod httpGet = null;

		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, cookie, userAgent);
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				} else if (statusCode == HttpStatus.SC_OK) {
					Cookie[] cookies = httpClient.getState().getCookies();
					String tmpcookies = "";
					for (Cookie ck : cookies) {
						tmpcookies += ck.toString() + ";";
					}
					// 保存cookie
					if (appContext != null && tmpcookies != "") {
						appContext.setProperty("cookie", tmpcookies);
						System.out.println(tmpcookies);
						appCookie = tmpcookies;
					}
				}
				responseBody = httpGet.getResponseBodyAsString();
//				System.out.println("XMLDATA=SIZE====>"+ httpGet.getResponseContentLength());
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				if (httpGet != null)
					httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);

		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
		// if(responseBody.contains("result") &&
		// responseBody.contains("errorCode") &&
		// appContext.containsProperty("user.uid")){
		// try {
		// Result res = Result.parse(new
		// ByteArrayInputStream(responseBody.getBytes()));
		// if(res.getErrorCode() == 0){
		// appContext.Logout();
		// appContext.getUnLoginHandler().sendEmptyMessage(1);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
//		try {
//			if (responseBody.contains("<html>")) {
//				System.out.println("响应数据：---》" + responseBody);
//			} else
//				System.out
//						.println("响应数据：---》" + responseBody.substring(0, 100));
//		} catch (Exception e) {
//			System.out.println(responseBody);
//		}
		return new ByteArrayInputStream(responseBody.getBytes());
	}

	/**
	 * 公用post方法
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 */
	private static InputStream _post_stream(AppContext appContext, String url,
			Map<String, Object> params, Map<String, File> files)
			throws AppException {
		// System.out.println("post_url==> "+url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);

		HttpClient httpClient = null;
		PostMethod httpPost = null;

		// post表单参数处理
		int length = (params == null ? 0 : params.size())
				+ (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		int i = 0;
		if (params != null)
			for (String name : params.keySet()) {
				parts[i++] = new StringPart(name, String.valueOf(params
						.get(name)), UTF_8);
				// System.out.println("post_key==> "+name+"    value==>"+String.valueOf(params.get(name)));
			}
		if (files != null)
			for (String file : files.keySet()) {
				try {
					parts[i++] = new FilePart(file, files.get(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				// System.out.println("post_key_file==> "+file);
			}

		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpPost = getHttpPost(url, cookie, userAgent);
				httpPost.setRequestEntity(new MultipartRequestEntity(parts,
						httpPost.getParams()));
				int statusCode = httpClient.executeMethod(httpPost);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				} else if (statusCode == HttpStatus.SC_OK) {
					Cookie[] cookies = httpClient.getState().getCookies();
					String tmpcookies = "";
					for (Cookie ck : cookies) {
						tmpcookies += ck.toString() + ";";
					}
					// 保存cookie
					if (appContext != null && tmpcookies != "") {
						appContext.setProperty("cookie", tmpcookies);
						appCookie = tmpcookies;
					}
				}
				responseBody = httpPost.getResponseBodyAsString();
				// System.out.println("XMLDATA=====>"+responseBody);
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpPost.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);

		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
		// if(responseBody.contains("result") &&
		// responseBody.contains("errorCode") &&
		// appContext.containsProperty("user.uid")){
		// try {
		// Result res = Result.parse(new
		// ByteArrayInputStream(responseBody.getBytes()));
		// if(res.getErrorCode() == 0){
		// appContext.Logout();
		// appContext.getUnLoginHandler().sendEmptyMessage(1);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		return new ByteArrayInputStream(responseBody.getBytes());
	}

	/**
	 * post请求URL
	 * 
	 * @param url
	 * @param params
	 * @param files
	 * @throws AppException
	 * @throws IOException
	 * @throws
	 */
	// private static Result http_post(AppContext appContext, String url,
	// Map<String, Object> params, Map<String,File> files) throws AppException,
	// IOException {
	// return Result.parse(_post(appContext, url, params, files));
	// }

	/**
	 * 获取网络图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getNetBitmap(String url) throws AppException {
		// System.out.println("image_url==> "+url);
		HttpClient httpClient = null;
		GetMethod httpGet = null;
		Bitmap bitmap = null;
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, null, null);
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				InputStream inStream = httpGet.getResponseBodyAsStream();
				bitmap = BitmapFactory.decodeStream(inStream);
				inStream.close();
				break;
			} catch (HttpException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生致命的异常，可能是协议不对或者返回的内容有问题
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if (time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				// 发生网络异常
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 释放连接
				httpGet.releaseConnection();
				httpClient = null;
			}
		} while (time < RETRY_TIME);
		return bitmap;
	}

	/**
	 * 检查版本更新
	 * 
	 * @param url
	 * @return
	 */
	public static AppUpdate checkVersion(AppContext appContext)
			throws AppException {
		try {
			// return http_get(appContext,
			// URLs.URL_CHECKUPDATE+"?appType=1&oldVersion="+appContext.getVersionName());
			System.out.println("检测版本更新。。。。。。。");
			return XMLParseUtil.parseAppUpdate(http_get_stream(appContext,
					URLs.URL_CHECKUPDATE));
			// + "?appType=1&oldVersion="
			// + appContext.getVersionCode() + "&Diqu="
			// + AreaUtils.areaCode));
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 检查数据更新
	 * 
	 * @param url
	 * @return
	 */
	public static AppUpdate checkDataUpdate(AppContext appContext)
			throws AppException {
		try {
			// return http_get(appContext,
			// URLs.URL_CHECKUPDATE+"?appType=1&oldVersion="+appContext.getVersionName());
			return XMLParseUtil.parseAppUpdate(http_get_stream(appContext,
					URLs.URL_CHECKDATAUPDATE + "Diqu=" + AreaUtils.areaCode));
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 登录， 自动处理cookie
	 * 
	 * @param url
	 * @param username
	 * @param pwd
	 * @return
	 * @throws AppException
	 */
	public static InputStream login(AppContext appContext, String username,
			String pwd, String deviceId) throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("UserName", username);
		params.put("PassWord", pwd);
		params.put("keep_login", 1);
		params.put("PhoneCode", deviceId);
		String loginurl = URLs.URL_LOGIN;
		try {
			return _post(appContext, loginurl, params, null);
		} catch (Exception e) {
			appContext.setLoginState(AppContext.LOGIN_FAIL);
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static String login_get(AppContext appContext, String username,
			String pwd, String deviceId) throws AppException {
		StringBuilder builder = new StringBuilder();
		builder.append(URLs.URL_LOGIN).append("UserName=").append(username)
				.append("&").append("PassWord=").append(pwd).append("&")
				.append("PhoneCode=").append(deviceId);
		// .append("keep_online").append(1);
		System.out.println("login_url = " + builder.toString());
		try {
			// return _post(appContext, loginurl, params, null);
			return http_get(appContext, builder.toString());
		} catch (Exception e) {
			appContext.setLoginState(AppContext.LOGIN_FAIL);
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream login_get_stream(AppContext appContext,
			String username, String pwd, String deviceId) throws AppException {
		StringBuilder builder = new StringBuilder();
		builder.append(URLs.URL_LOGIN).append("UserName=").append(username)
				.append("&").append("PassWord=").append(pwd).append("&")
				.append("PhoneCode=").append(deviceId);
		// .append("keep_online").append(1);
		System.out.println("login_url = " + builder.toString());
		try {
			// return _post(appContext, loginurl, params, null);
			return http_get_stream(appContext, builder.toString());
		} catch (Exception e) {
			if (appContext.getLoginState() != AppContext.LOCAL_LOGINED)
				appContext.setLoginState(AppContext.LOGIN_FAIL);
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 注册
	 * 
	 * @param appContext
	 * @param username
	 * @param pwd
	 * @param deviceId
	 * @param name
	 * @param phone
	 * @param location
	 * @return
	 * @throws AppException
	 */
	public static String register_get(AppContext appContext, String username,
			String pwd, String deviceId, String name, String phone,
			String location) throws AppException {
		// UserName=111&PassWord=44444444&r_name=%BF%EE%BE%AD&Mobile=13578789898&PhoneCode=4755554&State=%B
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(URLs.URL_REGISTER).append("UserName=")
					.append(username).append("&").append("PassWord=")
					.append(pwd).append("&").append("PhoneCode=")
					.append(deviceId).append("&").append("r_name=")
					.append(URLEncoder.encode(name, "UTF-8")).append("&")
					.append("Mobile=").append(phone).append("&")
					.append("State=")
					.append(URLEncoder.encode(AreaUtils.areaCName, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("register_url = " + builder.toString());
		try {
			// return _post(appContext, registerUrl, params, null);
			return http_get(appContext, builder.toString());
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream register(AppContext appContext, String username,
			String pwd, String deviceId, String name, String phone,
			String location) throws AppException {
		Map<String, Object> params = new HashMap<String, Object>();
		// UserName=111&PassWord=44444444&r_name=%BF%EE%BE%AD&Mobile=13578789898&PhoneCode=4755554&State=%B
		params.put("UserName", username);
		params.put("PassWord", pwd);
		params.put("r_name", name);
		params.put("Mobile", phone);
		params.put("State", AreaUtils.areaCName);
		params.put("keep_login", 1);
		params.put("PhoneCode", deviceId);
		String registerUrl = URLs.URL_REGISTER;
		try {
			return _post(appContext, registerUrl, params, null);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ParseResult register2(AppContext appContext, String username,
			String pwd, String deviceId, String name, String phone,
			String location) throws AppException {
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(URLs.URL_REGISTER).append("UserName=")
					.append(username).append("&").append("PassWord=")
					.append(pwd).append("&").append("PhoneCode=")
					.append(deviceId).append("&").append("r_name=")
					.append(URLEncoder.encode(name, "UTF-8")).append("&")
					.append("Mobile=").append(phone).append("&")
					.append("State=")
					.append(URLEncoder.encode(AreaUtils.areaCName, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			// return _post(appContext, registerUrl, params, null);
			return XMLParseUtil.parseRegister(
					http_get_stream(appContext, builder.toString()), username);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static String checkUsername(AppContext appContext, String username)
			throws AppException {
		try {
			return http_get(appContext, URLs.URL_CHECKUSERNAME + "UserName="
					+ username);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * xml数据返回 检测用户名
	 * 
	 * @param appContext
	 * @param username
	 * @return
	 * @throws AppException
	 */
	public static ParseResult checkUsername2(AppContext appContext,
			String username) throws AppException {
		try {
			return XMLParseUtil.parseCheckUsername(http_get_stream(appContext,
					URLs.URL_CHECKUSERNAME + "UserName=" + username));

		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream getCourseData(AppContext appContext, int areaCode)
			throws AppException {
		try {
			return http_get_stream(appContext, URLs.URL_GETCOURSEDATA
					+ "&Diqu=" + areaCode);

		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream getPaperListData(AppContext appContext,
			String classId, String areaCode) throws AppException {
		try {
			return http_get_stream(appContext, URLs.URL_GETPAPERLIST
					+ "ClassID=" + classId + "&Diqu=" + areaCode
			// + 46 //需要修改
			);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream getPaperDetail(AppContext appContext,
			String paperId) throws AppException {
		try {
			return http_get_stream(appContext, URLs.URL_GETPAPERDETAIL
					+ "classid=" + paperId);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream getQuestionList(AppContext appContext,
			String paperId) throws AppException {
		try {
			return http_get_stream(appContext, URLs.URL_GET_PAPER_QUESTIONLIST
					+ "classid=" + paperId);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// ClassID=2&ChapterID=22
	public static InputStream getChapterQuestionList(AppContext appContext,
			String chapterId, String lasttime, int areaCode)
			throws AppException {
		try {
			// !!!!!!!!!!!! 测试数据
			// (需要修改)!!!!!!!!!!!!!!!!!!!!!!!http://192.168.1.240/mobile/?Action=ShiTia
			if (lasttime != null)
				lasttime = lasttime.replaceAll(" ", "%20");
			else
				lasttime = "";
			return http_get_stream(appContext,
					URLs.URL_GET_CHAPTER_QUESTIONLIST + "ChapterID="
							+ chapterId + "&Diqu=" + areaCode + "&NDate="
							+ lasttime);
			// 返回全部的试题
			// return
			// http_get_stream(appContext,"http://www.cyedu.org/UserCenter/mobile/?Action=ShiTia");
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static InputStream getKnowledgeQuestionList(AppContext appContext,
			String knowledgeId, String notIn) throws AppException {
		try {
			// !!!!!!!!!!!! 测试数据
			// (需要修改)!!!!!!!!!!!!!!!!!!!!!!!http://192.168.1.240/mobile/?Action=ShiTia
			return http_get_stream(appContext,
					URLs.URL_GET_KNOWLEDGE_QUESTIONLIST + "classid="
							+ knowledgeId + notIn);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// public static InputStream getChapterQuestionList2(AppContext
	// appContext,String chapterId)throws AppException
	// {
	// try
	// {
	// //!!!!!!!!!!!! 测试数据
	// (需要修改)!!!!!!!!!!!!!!!!!!!!!!!http://192.168.1.240/mobile/?Action=ShiTia
	// return http_get_stream(appContext,
	// "http://192.168.1.240/mobile/?Action=ShiTia");
	// }catch(Exception e)
	// {
	// if(e instanceof AppException)
	// throw (AppException)e;
	// throw AppException.network(e);
	// }
	// }
	public static InputStream getKnowledgeList(AppContext appContext,
			String chapterid) throws AppException {
		try {
			return http_get_stream(appContext, URLs.URL_GETKNOWLEDGEDATA
					+ "classid=" + chapterid);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static NewsList getNewsList(AppContext appContext, int catalog,
			int pageIndex, int pageSize, int areaCode) throws AppException {
		// TODO Auto-generated method stub
		try {
			return XMLParseUtil.parseNewsList(http_get_stream(appContext,
					URLs.URL_GETNEWSLIST + "ClassID=" + catalog + "&Diqu="
							+ areaCode + "&pageindex=" + pageIndex));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static Info getNewsDetail(AppContext appContext, int news_id)
			throws AppException {
		// TODO Auto-generated method stub
		try {
			return XMLParseUtil.parseInfo(http_get_stream(appContext,
					URLs.URL_GETNEWSDETAIL + "ClassID=" + news_id));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 发帖
	 * 
	 * @param Tweet
	 *            -uid & msg & image
	 * @return
	 * @throws AppException
	 */
	public static ParseResult pubTweet(AppContext appContext, ForumPost post)
			throws AppException {
		// Title=11111&Content=订单&UserName=jhddd
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("UserName", post.getAuthor());
		params.put("Title", post.getTitle());
		params.put("Content", post.getBody());

		Map<String, File> files = new HashMap<String, File>();
		if (post.getImageFile() != null)
			files.put("img", post.getImageFile());

		try {
			return XMLParseUtil.parseCheckUsername(_post(appContext,
					URLs.TWEET_PUB, params, files));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ParseResult pubTweet2(AppContext appContext, ForumPost post)
			throws AppException {
		// Title=11111&Content=订单&UserName=jhddd
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("UserName", post.getAuthor());
		params.put("Title", post.getTitle());
		params.put("Content", post.getBody());
		String param = _MakePostParams(params);
		try {
			return XMLParseUtil.parseCheckUsername(_urlPost(appContext,
					URLs.TWEET_PUB, param));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// 删帖
	public static ParseResult deleteTweet(AppContext appContext, ForumPost post)
			throws AppException {
		String url = URLs.TWEET_DELETE + "ClassID=" + post.getId()
				+ "&UserName=" + appContext.getUsername();
		try {
			return XMLParseUtil.parseCheckUsername(http_get_stream(appContext,
					url));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 回帖
	 * 
	 * @param appContext
	 * @param post
	 * @return
	 * @throws AppException
	 */
	public static ParseResult replyTweet2(AppContext appContext,
			ForumPostReply reply) throws AppException {
		// Title=11111&Content=订单&UserName=jhddd
		String url = URLs.TWEET_REPLY1;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("UserName", reply.getReplyAuthor());
		params.put("ClassID", reply.getPostId());
		params.put("Content", reply.getContent());
		if (reply.getReplyFloor() != 0) {
			params.put("ParentID", reply.getReplyFloor());
			params.put("TName", reply.getReplyTo());
			url = URLs.TWEET_REPLY2;
		}
		String param = _MakePostParams(params);
		System.out.println("url ==> " + url + params);
		try {
			return XMLParseUtil.parseCheckUsername(_urlPost(appContext, url,
					param));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 帖子列表
	 * 
	 * @param appContext
	 * @param chapterid
	 * @return
	 * @throws AppException
	 */
	public static ArrayList<ForumPost> getForumPostList(AppContext appContext,
			int pageIndex, int pageSize) throws AppException {
		try {
			// 地区或者其他参数添加/.....Page=1&CountID=20
			return XMLParseUtil.parsePostList(http_get_stream(appContext,
					URLs.TWEET_LIST + "Page=" + pageIndex + "&CountID="
							+ pageSize));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ArrayList<ForumPost> getForumPostListOfUser(
			AppContext appContext, int pageIndex, int pageSize,
			String username, int mode) throws AppException {
		System.out.println("Mode = " + mode);
		try {
			// 地区或者其他参数添加/.....Page=1&CountID=20http://192.168.1.240/mobile/?Action=Onebbslist&Page=1&CountID=20&UserName=zhaozhimin3
			if (mode == 0)
				return XMLParseUtil.parsePostList(http_get_stream(appContext,
						URLs.TWEET_LIST_OF_USER + "Page=" + pageIndex
								+ "&CountID=" + pageSize + "&UserName="
								+ username));
			else
				return XMLParseUtil.parsePostList(http_get_stream(appContext,
						URLs.TWEET_LIST_OF_USER2 + "Page=" + pageIndex
								+ "&CountID=" + pageSize + "&UserName="
								+ username));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	/**
	 * 帖子详情
	 * 
	 * @param appContext
	 * @param postid
	 * @return
	 * @throws AppException
	 */
	public static ForumPost getPostDetail(AppContext appContext, String postid)
			throws AppException {
		try {
			return XMLParseUtil.parsePost(http_get_stream(appContext,
					URLs.TWEET_DETAIL + "classid=" + postid));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ReplyList getReplyList(AppContext appContext, int page,
			int pagesize, String postId) throws AppException {
		try {
			// 参数 Page=1&CountID=20&classid=20
			return XMLParseUtil.parseComments(http_get_stream(appContext,
					URLs.TWEET_REPLY_LIST + "Page=" + page + "&CountID="
							+ pagesize + "&classid=" + postId));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static Notice getUserNotice(AppContext appContext, int uid)
			throws AppException {
		// TODO Auto-generated method stub
		try {
			return XMLParseUtil.parseNotice(http_get_stream(appContext,
					URLs.USER_NOTICE + "UserName=" + uid));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static boolean uploadErrorQuestions(AppContext appContext,
			String username, String error, String paperId) throws AppException {
		try {
			return XMLParseUtil.parseCheckUsername(
					http_get_stream(appContext, URLs.URL_UPLOAD_ERROR_QUESTION
							+ "UserName=" + username + "&ClassID=" + error
							+ "&PaperId=" + paperId)).Ok();

		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static SyncData getSyncData(AppContext appContext, String username,
			String url, String params) throws AppException {
		// TODO Auto-generated method stub
		try {
			return XMLParseUtil.parseSyncData(
					http_get_stream(appContext, url + "username=" + username
							+ params), username);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ArrayList<Knowledge> getKnowledge(AppContext appContext)
			throws AppException {
		try {
			return XMLParseUtil.parseKnowledge(http_get_stream(appContext,
					URLs.URL_GET_ALL_KNOWLEDGE));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ArrayList<Plan> getPlan(AppContext appContext,
			String classid, String areaCode) throws AppException {
		try {
			return XMLParseUtil.parsePlan(http_get_stream(appContext,
					URLs.URL_GET_ALL_PLAN + "ClassID=" + classid + "&Diqu="
							+ areaCode));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	public static ArrayList<Paper> getPapersByIds(AppContext appContext,
			String pids) throws AppException {
		try {
			return (XMLParseUtil.parsePaperList(http_get_stream(appContext,
					URLs.URL_GET_PAPERS_BYID + "ClassID=" + pids)))
					.getPaperlist();
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// 注销
	public static void logout(AppContext appContext, String username)
			throws AppException {
		try {
			http_get_stream(appContext, URLs.URL_LOGOUT + "UserName="
					+ username);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// 获取地区数据
	public static ArrayList<Area> getArea(AppContext appContext)
			throws AppException {
		try {
			return XMLParseUtil.parseArea(http_get_stream(appContext,
					URLs.URL_GET_AREA));
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}

	// 获取更新数据
	public static InputStream getUpdateData(AppContext appContext, String date,
			int areaCode) throws AppException {
		try {
			// !!!!!!!!!!!! 测试数据
			// (需要修改)!!!!!!!!!!!!!!!!!!!!!!!http://192.168.1.240/mobile/?Action=ShiTia
			return http_get_stream(appContext, URLs.URL_GET_UPDATE_DATA
					+ "adddate=" + date + "&Diqu=" + areaCode);
		} catch (Exception e) {
			if (e instanceof AppException)
				throw (AppException) e;
			throw AppException.network(e);
		}
	}
}
