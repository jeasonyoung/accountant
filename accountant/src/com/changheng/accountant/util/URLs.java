package com.changheng.accountant.util;

import java.io.Serializable;

public class URLs implements Serializable {
	
	public final static String HOST = "www.cyedu.org";//"192.168.1.240";//"192.168.1.246:8080";// www.oschina.net
	public final static String HTTP = "http://";
	
	private final static String URL_SPLITTER = "/";
	private final static String URL_UNDERLINE = "_";
	private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;
	//检测更新
	public final static String URL_CHECKUPDATE = URL_API_HOST + "UserCenter/mobile/MobileAppVersion"+AreaUtils.areaCode+".xml";
	public final static String URL_CHECKDATAUPDATE = URL_API_HOST + "UserCenter/mobile/?Action=UpVersion&";

	//登录
	public final static String URL_LOGIN = URL_API_HOST + "UserCenter/mobile/?Action=login&";
	public final static String URL_LOGOUT = URL_API_HOST + "UserCenter/mobile/?Action=LoginOut&";
	//注册
	public final static String URL_REGISTER = URL_API_HOST + "UserCenter/mobile/?Action=reg&";
//	public final static String URL_REGISTER = "http://192.168.1.246:8080/struts01/day04/register";
	//验证用户名
	public final static String URL_CHECKUSERNAME = URL_API_HOST + "UserCenter/mobile/?Action=Check&"; 
	//获取科目以及章节数据
	public final static String URL_GETCOURSEDATA = URL_API_HOST + "UserCenter/mobile/?Action=zds";
	//获取章节下所有的知识点
	public final static String URL_GETKNOWLEDGEDATA = URL_API_HOST + "UserCenter/mobile/?Action=Dian&";
	//帖子列表
	public final static String TWEET_LIST = URL_API_HOST+"UserCenter/mobile/?Action=bbslist&";
	//用户帖子列表
	public final static String TWEET_LIST_OF_USER = URL_API_HOST+"UserCenter/mobile/?Action=Onebbslist&";
	//用户回复的帖子列表
	public final static String TWEET_LIST_OF_USER2 = URL_API_HOST+"UserCenter/mobile/?Action=OneReplyList&";
	//帖子详情
	public final static String TWEET_DETAIL = URL_API_HOST+"UserCenter/mobile/?Action=GetOnelist&";
	//回复列表
	public final static String TWEET_REPLY_LIST = URL_API_HOST+"UserCenter/mobile/?Action=ReplyList&";
	//发帖
	public final static String TWEET_PUB = URL_API_HOST+"UserCenter/mobile/?Action=addBBS";
	//回帖直接回复楼主
	public final static String TWEET_REPLY1 = URL_API_HOST+"UserCenter/mobile/?Action=addReply";
	//回帖恢复楼层
	public final static String TWEET_REPLY2 = URL_API_HOST+"UserCenter/mobile/?Action=AddOneReply";
	//删帖
	public final static String TWEET_DELETE = URL_API_HOST+"UserCenter/mobile/?Action=DelBBs&";
	//
	public final static String USER_NOTICE = URL_API_HOST+"UserCenter/mobile/?Action=&";
	//模拟试卷列表
	public final static String URL_GETPAPERLIST = URL_API_HOST + "UserCenter/mobile/?Action=AllPaper&";
	//试卷详情
	public final static String URL_GETPAPERDETAIL = URL_API_HOST + "UserCenter/mobile/?Action=OnePaper&";
	//试卷下所有考题 http://192.168.1.240/mobile/?Action=PaperShiTi&classid=2
	public final static String URL_GET_PAPER_QUESTIONLIST = URL_API_HOST + "UserCenter/mobile/?Action=PaperShiTi&";
	//某章节下所有题		http://192.168.1.240/mobile/?action=lianxi&ClassID=2&ChapterID=22
	public final static String URL_GET_CHAPTER_QUESTIONLIST = URL_API_HOST + "UserCenter/mobile/?Action=lianxi&";
	public final static String URL_GET_KNOWLEDGE_QUESTIONLIST = URL_API_HOST + "UserCenter/mobile/?Action=OneDian&";
	//资讯列表
	public final static String URL_GETNEWSLIST = URL_API_HOST + "UserCenter/mobile/?Action=Column&";
	//错题(如果要同步的话)
	public final static String URL_SYNC_ERROR = URL_API_HOST + "UserCenter/mobile/?Action=CollectTi2&";
	//上传错题http://192.168.1.240/mobile/?Action=UpdateTi&
	public final static String URL_UPLOAD_ERROR_QUESTION = URL_API_HOST +"UserCenter/mobile/?Action=UpdateTi&";
	//资讯
	public static final String URL_GETNEWSDETAIL = URL_API_HOST + "UserCenter/mobile/?Action=News&";;
	
	//收藏(如果要同步的话)
	public static final String URL_SYNC_FAVOR = URL_API_HOST + "UserCenter/mobile/?Action=CollectTi&";
	//笔记(如果要同步的话)
	
	//考试记录（同步）
	//http://192.168.1.240/mobile/?Action=CollectTi3&username=ozhh123&ClassID=5.3.1123-BQ1213-C.2l3l5l6.1&DelID=111
	public static final String URL_SYNC_EXAM = URL_API_HOST + "UserCenter/mobile/?Action=CollectTi3&";
	//调所有的知识点
	public static final String URL_GET_ALL_KNOWLEDGE =URL_API_HOST + "UserCenter/mobile/?Action=AllDian";
	//所有计划
	public static final String URL_GET_ALL_PLAN =URL_API_HOST + "UserCenter/mobile/?Action=JiHua&";
	//按IDs查询试卷
	public static final String URL_GET_PAPERS_BYID = URL_API_HOST + "UserCenter/mobile/?Action=FindPapers&";
	//下载数据包的地址
	public static final String URL_DOWNLOAD_DATA = URL_API_HOST + "ydt/app/data"+AreaUtils.areaCode+".zip";
	//地区数据
	public static final String URL_GET_AREA = URL_API_HOST +"UserCenter/mobile/?Action=DiQuList";
	//题库更新数据
	public static final String URL_GET_UPDATE_DATA = URL_API_HOST +"UserCenter/mobile/?Action=UpPaper&";
	
	public static final String URL_BUY = "http://www.cyedu.org/";
}
