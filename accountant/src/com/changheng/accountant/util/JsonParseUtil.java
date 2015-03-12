package com.changheng.accountant.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.changheng.accountant.entity.AppUpdate;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.User;

/**
 * 解析json的工具类
 * @author Administrator
 *
 */
public class JsonParseUtil {
	/**
	 * 解析检测更新返回的数据{"S":1,"Content":"暂时没有更新的介绍","url":"http%3A%2F%2F192.168.1.246%3A8080%2FmobileApp%2FHello.apk","apkSize":"1536","version":"1.1"}
	 * 一个jsonobj,S:0表示没有更新
	 * @param jsonStr
	 * @return
	 */
	public static ParseResult parseCheckUpdate(String jsonStr) 
	{
		ParseResult pr = new ParseResult();
		if(jsonStr == null || "".equals(jsonStr)||"null".equalsIgnoreCase(jsonStr))
		{
			pr.setEmptyResult();
			return pr;
		}
		try
		{
			JSONObject json  = new JSONObject(jsonStr);
			int code = json.optInt("S", 0); //
			String msg = json.optString("msg");
			if(msg!=null)
			{
				try {
					msg = URLDecoder.decode(msg, "gbk");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(code!=1)
			{
				pr.setResult(code, msg);
			}else
			{
				String content = json.optString("Content");
				int size = json.optInt("size", 0);
				String url = json.optString("url");
				if(url!=null&&!url.contains("/"))
				{
					try {
						url = URLDecoder.decode(json.optString("url"),"UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				String version = json.optString("versionName");
				int versionCode = json.optInt("versionCode");
				AppUpdate obj = new AppUpdate(url, content, size, versionCode,version);
				pr.setResult(code, msg, obj);
			}
		}catch(JSONException e)
		{
			e.printStackTrace();
			pr.setBadResult();
		}
		return pr;
	}
	/**
	 * 解析登录后返回的json数据,类似{"S":127,"OK":1,"msg":"登录成功"},一个jsonobj
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	public static ParseResult parseLogin(String jsonStr,String username,String pwd)
	{
		ParseResult pr = new ParseResult();
		if(jsonStr == null || "".equals(jsonStr)||"null".equalsIgnoreCase(jsonStr))
		{
			pr.setEmptyResult();
			return pr;
		}
		try
		{
			JSONObject json  = new JSONObject(jsonStr);
			int code = json.optInt("S", 0); //返回1表示登录成功
			String msg = json.optString("msg");
			if(msg!=null)
			{
				try {
					msg = URLDecoder.decode(msg, "gbk");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(code!=1)
			{
				pr.setResult(code, msg);
			}else
			{
				int uid = json.optInt("uid",0);
				boolean access = json.optBoolean("access",false);
				User u = new User(uid,access);
				u.setUsername(username);
				u.setPassword(pwd);
				pr.setResult(code, msg, u);
			}
		}catch(JSONException e)
		{
			e.printStackTrace();
			pr.setBadResult();
		}
		return pr;
	}
	
	/**
	 * 解析注册后返回的json数据,类似{"S":0,"msg":"用户名被占用"},一个jsonobj
	 * @param jsonStr
	 * @return
	 */
	public static ParseResult parseRegister(String jsonStr)
	{
		ParseResult pr = new ParseResult();
		if(jsonStr == null || "".equals(jsonStr)||"null".equalsIgnoreCase(jsonStr))
		{
			pr.setEmptyResult();
			return pr;
		}
		try
		{
			JSONObject json  = new JSONObject(jsonStr);
			int code = json.optInt("S", 0); //返回1表示注册成功
			String msg = json.optString("msg");
			if(code!=1)
			{
				pr.setResult(code, msg);
			}else
			{
				int uid = json.optInt("uid",0);
				User u = new User(uid,false);
				pr.setResult(code, msg, u);
			}
		}catch(JSONException e)
		{
			e.printStackTrace();
			pr.setBadResult();
		}
		return pr;
	}
	/**
	 * 解析模拟试卷列表 (一个jsonArray,jsonArray下每个jsonobj里包含一个rule的jsonArray)
	 * @param jsonStr
	 * @return
	 */
	public static ParseResult parsePaperList(String jsonStr,String gid)
	{
		ParseResult pr = new ParseResult();
		if(jsonStr == null || "".equals(jsonStr)||"null".equalsIgnoreCase(jsonStr))
		{
			pr.setEmptyResult();
			return pr;
		}
		try
		{
			JSONArray json = new JSONArray(jsonStr);
			int length = json.length();
			if(length>0)
			{
				ArrayList<Paper> papers = new ArrayList<Paper>();
				for(int i=0;i<length;i++)
				{
					JSONObject obj = json.getJSONObject(i);
					Paper p = new Paper(obj.optInt("paperId")+"",obj.optString("paperName"),obj.optInt("paperScore"),obj.optInt("paperTime"),gid,null,0);
					p.setJsonString(obj.toString());
					papers.add(p);
				}
				pr.setResult(1, "", papers);
			}
		}catch(JSONException e)
		{
			e.printStackTrace();
			pr.setBadResult();
		}
		return pr;
	}
	/**
	 * 解析一套试卷下的所有考题(一个jsonArray)
	 * @param jsonStr
	 * @return
	 */
	public static ParseResult parseQuestionList(String jsonStr)
	{
		ParseResult pr = new ParseResult();
		if(jsonStr == null || "".equals(jsonStr)||"null".equalsIgnoreCase(jsonStr))
		{
			pr.setEmptyResult();
			return pr;
		}
		try
		{
			JSONArray json = new JSONArray(jsonStr);
			int length = json.length();
			if(length>0)
			{
				ArrayList<ExamQuestion> questionList = new ArrayList<ExamQuestion>();
				for(int i=0;i<length;i++)
				{
					JSONObject obj = json.getJSONObject(i);
					/*
					 * String qid,String ruleid, String paperId, String content,
						String answer, String analysis, String linkQid,
							int qType, int optionNum, int orderId
					 */
					ExamQuestion q = new ExamQuestion(obj.optInt("questId")+"",obj.optInt("questRuleId")+"",obj.optInt("questKnowledgeId")+"",obj.optInt("questClassId")+"",obj.optString("questContent"),
									obj.optString("questAnswer"),obj.optString("questAnalysis"),obj.optString("questLinkQuestionId"),
									obj.optString("type"),obj.optInt("questOptionNum"),obj.optInt("questOrderId"));
					questionList.add(q);
				}
				pr.setResult(1, "", questionList);
			}
		}catch(JSONException e)
		{
			e.printStackTrace();
			pr.setBadResult();
		}
		return pr;
	}
	public static ParseResult parseTweentList(String jsonStr)
	{
		ParseResult pr = new ParseResult();
		if(jsonStr == null || "".equals(jsonStr)||"null".equalsIgnoreCase(jsonStr))
		{
			pr.setEmptyResult();
			return pr;
		}
		try
		{
			JSONArray json = new JSONArray(jsonStr);
		}catch(JSONException e)
		{
			e.printStackTrace();
			pr.setBadResult();
		}
		return pr;
	}
}
