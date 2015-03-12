package com.changheng.accountant.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.changheng.accountant.AppException;
import com.changheng.accountant.entity.AppUpdate;
import com.changheng.accountant.entity.Area;
import com.changheng.accountant.entity.Base;
import com.changheng.accountant.entity.Chapter;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.CourseList;
import com.changheng.accountant.entity.ExamErrorQuestion;
import com.changheng.accountant.entity.ExamFavor;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.ForumPost;
import com.changheng.accountant.entity.ForumPostReply;
import com.changheng.accountant.entity.Info;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.entity.KnowledgeList;
import com.changheng.accountant.entity.NewsList;
import com.changheng.accountant.entity.Notice;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.entity.PaperList;
import com.changheng.accountant.entity.ParseResult;
import com.changheng.accountant.entity.Plan;
import com.changheng.accountant.entity.ReplyList;
import com.changheng.accountant.entity.SyncData;
import com.changheng.accountant.entity.UpdateDataEntity;
import com.changheng.accountant.entity.User;

public class XMLParseUtil {
	public final static String UTF8 = "UTF-8";
	public final static String NODE_ROOT = "cyedu";
	private final static Random random= new Random();

	// 更新客户端的解析
	public static AppUpdate parseAppUpdate(InputStream inputStream)
			throws IOException, AppException {
		AppUpdate update = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(inputStream, Base.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					// 通知信息
					if (tag.equalsIgnoreCase("android")) {
						update = new AppUpdate();
					} else if (update != null) {
						if (tag.equalsIgnoreCase("versionCode")) {
							update.setVersionCode(StringUtils.toInt(
									xmlParser.nextText(), 0));
						} else if (tag.equalsIgnoreCase("versionName")) {
							update.setVersionName(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("size")) {
							update.setSize(StringUtils.toInt(
									xmlParser.nextText(), 0));
						} else if (tag.equalsIgnoreCase("downloadUrl")) {
							update.setUrl(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("updateLog")) {
							update.setContent(xmlParser.nextText());
						} else if(tag.equalsIgnoreCase("addTime"))
						{
							update.setAddTime(xmlParser.nextText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			inputStream.close();
		}
		return update;
	}

	// 登录解析
	public static ParseResult parseLogin(InputStream stream, String username,
			String pwd) throws IOException, AppException {
		User user = null;
		ParseResult res = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {

				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象了
					if (tag.equalsIgnoreCase("result")) {
						res = new ParseResult();
					} else if (tag.equalsIgnoreCase("errorCode")) {
						res.setErrorCode(StringUtils.toInt(
								xmlParser.nextText(), -1));
					} else if (tag.equalsIgnoreCase("errorMessage")) {
						res.setErrorMsg(xmlParser.nextText().trim());
					} else if (tag.equalsIgnoreCase("user") && res != null
							&& res.Ok()) {
						user = new User();
					} else if (tag.equalsIgnoreCase("uid") && user != null) {
						user.setUid(StringUtils.toInt(xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("location") && user != null) {
						user.setLocation(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("name") && user != null) {
						user.setTruename(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("face") && user != null) {
						user.setFace(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("jointime") && user != null) {
						user.setJointime(xmlParser.nextText());
					} else if(tag.equalsIgnoreCase("limittime") && user != null) {
						user.setLimitTime(xmlParser.nextText());
					} else if(tag.equalsIgnoreCase("resttime") && user != null) {//小时
						user.setRestTime(Integer.valueOf(xmlParser.nextText()));
					}
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合中
					if (tag.equalsIgnoreCase("user") && res != null && res.Ok() && user!=null) {
						user.setUsername(username);
						user.setPassword(pwd);
						res.setObj(user);
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return res;
	}

	// 检测用户名解析
	public static ParseResult parseCheckUsername(InputStream stream)
			throws IOException, AppException {
		ParseResult res = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					// 通知信息
					if (tag.equalsIgnoreCase("result")) {
						res = new ParseResult();
					} else if (tag.equalsIgnoreCase("errorCode")) {
						res.setErrorCode(StringUtils.toInt(
								xmlParser.nextText(), -1));
					} else if (tag.equalsIgnoreCase("errorMessage")) {
						res.setErrorMsg(xmlParser.nextText().trim());
					}
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合中
					if (tag.equalsIgnoreCase("result") && res != null) {

					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return res;
	}

	// 注册解析
	public static ParseResult parseRegister(InputStream stream, String username)
			throws IOException, AppException {
		User user = null;
		ParseResult res = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {

				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象了
					if (tag.equalsIgnoreCase("result")) {
						res = new ParseResult();
					} else if (tag.equalsIgnoreCase("errorCode")) {
						res.setErrorCode(StringUtils.toInt(
								xmlParser.nextText(), -1));
					} else if (tag.equalsIgnoreCase("errorMessage")) {
						res.setErrorMsg(xmlParser.nextText().trim());
					} else if (res != null && res.Ok()) {
						user = new User();
						if (tag.equalsIgnoreCase("uid")) {
							user.setUid(StringUtils.toInt(xmlParser.nextText(),
									0));
						} else if (tag.equalsIgnoreCase("location")) {
							user.setLocation(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("name")) {
							user.setTruename(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("face")) {
							user.setFace(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("jointime")) {
							user.setJointime(xmlParser.nextText());
						}
						// 通知信息
					}
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合中
					if (tag.equalsIgnoreCase("user") && res != null && res.Ok()) {
						user.setUsername(username);
						res.setObj(user);
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return res;
	}

	// 解析试卷列表[只包含试卷的基本信息]
	public static PaperList parsePaperList(InputStream stream)
			throws IOException, AppException {
		PaperList list = new PaperList();
		Paper p = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象了
					if (tag.equalsIgnoreCase("pagesize")) {
						list.setPagesize(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("paperCount")) {
						list.setPaperCount(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("paperList")) {
						// 试卷集合开始
						// papers = new ArrayList<Paper>();
					} else if (tag.equalsIgnoreCase("paper")) {
						p = new Paper();
					} else if (tag.equalsIgnoreCase("paperId") && p != null) {
						p.setPaperId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperName") && p != null) {
						p.setPaperName(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("addDate") && p != null) {
						p.setAddDate(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId") && p != null) {
						p.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("examTime") && p != null) {
						p.setPaperTime(StringUtils.toInt(xmlParser.nextText(),
								60));
					} else if (tag.equalsIgnoreCase("paperScore") && p != null) {
						p.setPaperSorce((StringUtils.toInt(
								xmlParser.nextText(), 100)));
					} else if (tag.equalsIgnoreCase("clickNum") && p != null) {
						p.setClickNum(StringUtils.toInt(xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("year") && p != null) {
						p.setYear(StringUtils.toInt(xmlParser.nextText(),
								Calendar.getInstance().get(Calendar.YEAR)));
					} else if (tag.equalsIgnoreCase("price") && p != null) {
						p.setPrice(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("paper") && p != null) {
						list.getPaperlist().add(p);
						p = null;
					}
					// else if(tag.equalsIgnoreCase("paperList")&& papers!=
					// null)
					// {
					// list.setPaperlist(papers);
					// }
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return list;
	}

	// 解析单张试卷
	public static Paper parsePaper(InputStream stream) throws IOException,
			AppException {
		Paper p = new Paper();
		ExamRule r = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("id")) {
						p.setPaperId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperName")) {
						p.setPaperName(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("addDate")) {
						p.setAddDate(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId")) {
						p.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("examTime")) {
						p.setPaperTime(StringUtils.toInt(xmlParser.nextText(),
								60));
					} else if (tag.equalsIgnoreCase("paperScore")) {
						p.setPaperSorce((StringUtils.toInt(
								xmlParser.nextText(), 100)));
					} else if (tag.equalsIgnoreCase("clickNum")) {
						p.setClickNum(StringUtils.toInt(xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("year")) {
						p.setYear(StringUtils.toInt(xmlParser.nextText(),
								Calendar.getInstance().get(Calendar.YEAR)));
					} else if (tag.equalsIgnoreCase("price")) {
						p.setPrice(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("rule")) {
						r = new ExamRule();
					} else if (tag.equalsIgnoreCase("paperId") && r != null) {
						r.setPaperId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleId") && r != null) {
						r.setRuleId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleTitle") && r != null) {
						r.setFullTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleQuestionNum")
							&& r != null) {
						r.setQuestionNum(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("ruleScoreForEach")
							&& r != null) {
						r.setScoreForEach(StringUtils.toDouble(
								xmlParser.nextText(), 0.0));
					} else if (tag.equalsIgnoreCase("ruleScoreSet")
							&& r != null) {
						r.setScoreSet(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("type") && r != null) {
						String type = xmlParser.nextText();
						r.setRuleType(type);
						r.setRuleTitle(StringUtils.toRuleTitle(type));
					} else if (tag.equalsIgnoreCase("orderInPaper")
							&& r != null) {
						r.setOrderInPaper(StringUtils.toInt(
								xmlParser.nextText(), 1));
					}// containQids
					else if (tag.equalsIgnoreCase("containQids") && r != null) {
						r.setContainQids(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("rule") && r != null) {
						p.getRuleList().add(r);
						r = null;
					}
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return p;
	}

	// 解析试题[加密了试题]
	public static ArrayList<ExamQuestion> parseQuestionList(InputStream stream)
			throws IOException, AppException {
		ArrayList<ExamQuestion> qList = new ArrayList<ExamQuestion>();
		ExamQuestion q = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("question")) {
						q = new ExamQuestion();
					} else if (tag.equalsIgnoreCase("id") && q != null) {
						q.setQid(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("type") && q != null) {
						q.setQType(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("optionNum") && q != null) {
						q.setOptionNum(StringUtils.toInt(xmlParser.nextText(),
								0));
					} else if (tag.equalsIgnoreCase("content") && q != null) {
						String content = xmlParser.nextText();
						if(content!=null)
							q.setContent(content.replaceAll("&nbsp;", " "));
					} else if (tag.equalsIgnoreCase("answer") && q != null) {
						String answer = xmlParser.nextText();
						if (answer != null) {
							q.setAnswer(answer.replaceAll("\\s", ""));
						} else
							q.setAnswer(answer);
					} else if (tag.equalsIgnoreCase("analysis") && q != null) {
						q.setAnalysis(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId") && q != null) {
						q.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("knowledgeId") && q != null) {
						q.setKnowledgeId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperId") && q != null) {
						// q.setQid(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleId") && q != null) {
						q.setRuleId(xmlParser.nextText());
					}else if(tag.equalsIgnoreCase("randNum")&&q!=null)
					{
//						q.setRandNum(StringUtils.toInt(xmlParser.nextText(), random.nextInt(26)+1));
						q.setRandNum(random.nextInt(26)+1);
					}else if(tag.equalsIgnoreCase("material")&&q!=null)
					{
						q.setMaterial(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("question")&&q!=null) {
						q.encode();
						qList.add(q);
						q = null;
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (Exception e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return qList;
	}
	private String htmlDecode(String content)
	{
		return content.replaceAll("&nbsp;", " ")
				.replaceAll("&gt;", ">")
				.replaceAll("&lt;", "<")
				.replaceAll("&quot;", "\"")
				.replaceAll("&#39;", "\'")
				.replaceAll("<br/> ", "\n");
	}
	// 解析科目章节
	public static CourseList parseClass(InputStream stream) throws IOException,
			AppException {
		CourseList cList = new CourseList();
		Chapter chapter = null;
		Course course = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("class")) {
						course = new Course();
					} else if (tag.equalsIgnoreCase("classid")
							&& course != null) {
						course.setCourseId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("name") && course != null) {
						course.setCourseName(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("chapter")) {
						chapter = new Chapter();
					} else if (tag.equalsIgnoreCase("id") && chapter != null) {
						chapter.setChapterId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("title") && chapter != null) {
						chapter.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("content")
							&& chapter != null) {
						chapter.setContent(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId")
							&& chapter != null) {
						chapter.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("pid") && chapter != null) {
						chapter.setPid(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("orderId")
							&& chapter != null) {
						chapter.setOrderId(StringUtils.toInt(
								xmlParser.nextText(), 0));
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("class") && course != null) {
						cList.getClassList().add(course);
						course = null;
					} else if (tag.equalsIgnoreCase("chapter")
							&& chapter != null) {
						cList.getChapterList().add(chapter);
						chapter = null;
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return cList;
	}

	/**
	 * 解析知识点
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public static KnowledgeList parseKnowledge(InputStream stream,
			String chaptername, String chapterid) throws IOException,
			AppException {
		KnowledgeList list = new KnowledgeList();
		ArrayList<Knowledge> kList = list.getKList();
		Knowledge k = new Knowledge();
		Paper p = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
//				System.out.println("tag= "+tag+",evtType="+evtType);
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("questionCount")) {
						p = new Paper();
						p.setPaperSorce(StringUtils.toInt(xmlParser.nextText(),0));
						System.out.println("获取的题目数量："+p.getPaperSorce());
					} else if (tag.equalsIgnoreCase("knowledge")) {
						k = new Knowledge();
					} else if (tag.equalsIgnoreCase("id") && k != null) {
						k.setKnowledgeId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("pid") && k != null) {
						k.setChapterId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId") && k != null) {
						k.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("title") && k != null) {
						k.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("content") && k != null) {
						k.setFullContent(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("orderId") && k != null) {
						k.setOrderId(StringUtils.toInt(xmlParser.nextText()));
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("knowledge") && k != null) {
						kList.add(k);
						k = null;
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		if(p!=null)
		{
			p.setPaperId(chapterid);
			p.setPaperName(chaptername);
			p.setPaperTime(-1);
			p.setAddDate(StringUtils.toDateStr(new Date()));
			list.setPaper(p);
		}
		return list;
	}

	/**
	 * 资讯列表
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public static NewsList parseNewsList(InputStream stream)
			throws IOException, AppException {
		NewsList list = new NewsList();
		Info i = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, Base.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象了
					if (tag.equalsIgnoreCase("result")) {
						return list;
					} else if (tag.equalsIgnoreCase("pagesize")) {
						list.setPageSize(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("paperCount")) {
						list.setNewsCount(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("newsList")) {
						// 试卷集合开始
						// papers = new ArrayList<Paper>();
					} else if (tag.equalsIgnoreCase("news")) {
						i = new Info();
					} else if (tag.equalsIgnoreCase("id") && i != null) {
						i.setId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("title") && i != null) {
						i.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("author") && i != null) {
						i.setAuthor(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("addDate") && i != null) {
						i.setAddTime(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("news") && i != null) {
						list.getNewslist().add(i);
						i = null;
					}
					// else if(tag.equalsIgnoreCase("paperList")&& papers!=
					// null)
					// {
					// list.setPaperlist(papers);
					// }
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return list;
	}

	/**
	 * 解析单条资讯
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public static Info parseInfo(InputStream stream) throws IOException,
			AppException {
		Info i = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("news")) {
						i = new Info();
					} else if (tag.equalsIgnoreCase("id") && i != null) {
						i.setId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("title") && i != null) {
						i.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("author") && i != null) {
						i.setAuthor(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("url") && i != null) {
						i.setUrl(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("content") && i != null) {
						i.setContent(xmlParser.nextText());
					}
					break;
				}
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return i;
	}

	/**
	 * 解析帖子列表
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public static ArrayList<ForumPost> parsePostList(InputStream stream)
			throws IOException, AppException {
		ArrayList<ForumPost> list = new ArrayList<ForumPost>();
		XmlPullParser xmlParser = Xml.newPullParser();
		ForumPost p = null;
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("post")) {
						p = new ForumPost();
					} else if (tag.equalsIgnoreCase("id") && p != null) {
						p.setId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("portrait") && p != null) {
						p.setFace(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("author") && p != null) {
						p.setAuthor(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("authorid") && p != null) {
						p.setAuthorId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("title") && p != null) {
						p.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("body") && p != null) {
						p.setBody(StringUtils.dealImgOfBody(xmlParser
								.nextText()));
					} else if (tag.equalsIgnoreCase("commentCount")
							&& p != null) {
						p.setAnswerCount(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("seeCount") && p != null) {
						p.setViewCount(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("addTime") && p != null) {
						// 友好时间显示
						p.setPubDate(StringUtils.friendly_time(xmlParser
								.nextText()));
					} else if (tag.equalsIgnoreCase("location") && p != null) {
						p.setArea(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("post") && p != null) {
						list.add(p);
						p = null;
					}
					break;
				}
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return list;
	}

	/**
	 * 解析单个帖子
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public static ForumPost parsePost(InputStream stream) throws IOException,
			AppException {
		XmlPullParser xmlParser = Xml.newPullParser();
		ForumPost p = null;
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("post")) {
						p = new ForumPost();
					} else if (tag.equalsIgnoreCase("id") && p != null) {
						p.setId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("portrait") && p != null) {
						p.setFace(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("author") && p != null) {
						p.setAuthor(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("authorid") && p != null) {
						p.setAuthorId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("title") && p != null) {
						p.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("body") && p != null) {
						p.setBody(StringUtils.dealImgOfBody(xmlParser
								.nextText()));
					} else if (tag.equalsIgnoreCase("commentCount")
							&& p != null) {
						p.setAnswerCount(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("seeCount") && p != null) {
						p.setViewCount(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("addTime") && p != null) {
						p.setPubDate(StringUtils.friendly_time(xmlParser
								.nextText()));
					} else if (tag.equalsIgnoreCase("location") && p != null) {
						p.setArea(xmlParser.nextText());
					}
					break;
				}
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return p;
	}

	public static ReplyList parseComments(InputStream stream)
			throws IOException, AppException {
		ReplyList replyList = new ReplyList();
		ForumPostReply r = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("pagesize")) {
						replyList.setPageSize(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("commentCount")) {
						replyList.setCommentCount(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("comment")) {
						r = new ForumPostReply();
					} else if (tag.equalsIgnoreCase("id") && r != null) {
						r.setId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("portrait") && r != null) {
						r.setFace(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("author") && r != null) {
						r.setReplyAuthor(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("authorid") && r != null) {
						r.setAuthorId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("content") && r != null) {
						r.setContent(StringUtils.dealImgOfBody(xmlParser
								.nextText()));
					} else if (tag.equalsIgnoreCase("pubDate") && r != null) {
						r.setReplyDate(StringUtils.friendly_time(xmlParser
								.nextText()));
					} else if (tag.equalsIgnoreCase("floor") && r != null) {
						r.setFloor(StringUtils.toInt(xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("replyfloor") && r != null) {
						r.setReplyFloor(StringUtils.toInt(xmlParser.nextText(),
								0));
					} else if (tag.equalsIgnoreCase("replyTo") && r != null) {
						r.setReplyTo(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("location") && r != null) {
						r.setLocation(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("comment") && r != null) {
						replyList.getReplyList().add(r);
						r = null;
					}
					break;
				}
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return replyList;
	}

	public static Notice parseNotice(InputStream stream) throws IOException,
			AppException {
		Notice notice = null;
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("notice")) {
						notice = new Notice();
					} else if (tag.equalsIgnoreCase("id")) {
						notice.setPostId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("title")) {
						notice.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("commentCount")) {
						notice.setNewReplyCount(StringUtils.toInt(
								xmlParser.nextText(), 0));
					}
					break;
				}
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return notice;
	}

	// 解析同步过来的数据
	public static SyncData parseSyncData(InputStream stream, String username)
			throws IOException, AppException {
		SyncData data = new SyncData();
		ExamQuestion q1 = null;
		ExamQuestion q2 = null;
		Paper p = null;
		ExamRecord r = null;
		ExamErrorQuestion e = null;
		ExamFavor f = null;
		Random random = new Random();
		ArrayList<ExamQuestion> questionList = null;
		ArrayList<ExamFavor> favorList = null;
		ArrayList<ExamErrorQuestion> errorList = null;
		ArrayList<Paper> paperList = null;
		ArrayList<ExamRecord> recordList = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					System.out.println("tag:"+tag+", "+tag.equalsIgnoreCase("errorRandnum"));
					if (tag.equalsIgnoreCase("errorCode")) {
						int code = Integer.parseInt(xmlParser.nextText());
						if(code == -1)
						{
							return null;
						}
					}
					if (tag.equalsIgnoreCase("favorList")) {
						favorList = new ArrayList<ExamFavor>();
						questionList = new ArrayList<ExamQuestion>();
					} else if (tag.equalsIgnoreCase("favor")) {
						f = new ExamFavor();
						q1 = new ExamQuestion();
					} else if (tag.equalsIgnoreCase("favorAddtime")
							&& f != null) {
						f.setAddTime(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("favorQid") && f != null
							&& q1 != null) {
						f.setQid(xmlParser.nextText());
						q1.setQid(f.getQid());
					} else if (tag.equalsIgnoreCase("favorType") && q1 != null) {
						q1.setQType(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("favorOptionNum")
							&& q1 != null) {
						q1.setOptionNum(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("favorContent")
							&& q1 != null) {
						q1.setContent(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("favorAnswer")
							&& q1 != null) {
						String answer = xmlParser.nextText();
						if (answer != null) {
							q1.setAnswer(answer.replaceAll("\\s", ""));
						} else
							q1.setAnswer(answer);
					} else if (tag.equalsIgnoreCase("favorAnalysis")
							&& q1 != null) {
						q1.setAnalysis(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("favorClassId")
							&& q1 != null) {
						q1.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("favorKnowledgeId")
							&& q1 != null) {
						q1.setKnowledgeId(xmlParser.nextText());
					} else if(tag.equalsIgnoreCase("favorRandNum")&&q1!=null)
					{
						q1.setRandNum(1);
					} else if(tag.equalsIgnoreCase("favorMaterial")&&q1!=null)
					{
						q1.setMaterial(xmlParser.nextText());
					}
					else if (tag.equalsIgnoreCase("deleteFavorQids")) {
						data.setFavorNeedDeleteQids(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("errorList")) {
						errorList = new ArrayList<ExamErrorQuestion>();
						if (questionList == null)
							questionList = new ArrayList<ExamQuestion>();
					} else if (tag.equalsIgnoreCase("error")) {
						e = new ExamErrorQuestion();
						q2 = new ExamQuestion();
					} else if (tag.equalsIgnoreCase("errorAddtime")
							&& e != null) {
						e.setAddTime(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("useranswer") && e != null) {
						e.setLastAnswer(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("errorQid") && e != null
							&& q2 != null) {
						e.setQid(xmlParser.nextText());
						q2.setQid(e.getQid());
					} else if (tag.equalsIgnoreCase("errorType") && q2 != null) {
						q2.setQType(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("errorOptionNum")
							&& q2 != null) {
						q2.setOptionNum(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("errorContent")
							&& q2 != null) {
						q2.setContent(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("errorAnswer")
							&& q2 != null) {
						String answer = xmlParser.nextText();
						if (answer != null) {
							q2.setAnswer(answer.replaceAll("\\s", ""));
						} else
							q2.setAnswer(answer);
					} else if (tag.equalsIgnoreCase("errorAnalysis")
							&& q2 != null) {
						q2.setAnalysis(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("errorClassId")
							&& q2 != null) {
						q2.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("errorKnowledgeId")
							&& q2 != null) {
						q2.setKnowledgeId(xmlParser.nextText());
					} else if(tag.equalsIgnoreCase("errorRandnum")&&q2!=null)//tag: errorRandnum
					{
						q2.setRandNum(1);
					}else if(tag.equalsIgnoreCase("errorMaterial")&&q1!=null)
					{
						q1.setMaterial(xmlParser.nextText());
					} 
					else if (tag.equalsIgnoreCase("deleteErrorQids")) {
						data.setErrorNeedDeleteQids(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("examRecordList")) {
						paperList = new ArrayList<Paper>();
						recordList = new ArrayList<ExamRecord>();
					} else if (tag.equalsIgnoreCase("record")) {
						p = new Paper();
						r = new ExamRecord();
					} else if (tag.equalsIgnoreCase("paperid") && p != null
							&& r != null) {
						p.setPaperId(xmlParser.nextText());
						r.setPaperId(p.getPaperId());
					} else if (tag.equalsIgnoreCase("paperName") && p != null) {
						p.setPaperName(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperAddDate")
							&& p != null) {
						p.setAddDate(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperClassId")
							&& p != null) {
						p.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperExamTime")
							&& p != null) {
						p.setPaperTime(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("paperScore") && p != null) {
						p.setPaperSorce(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("clickNum") && p != null) {
						p.setClickNum(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("year") && p != null) {
						p.setYear(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("price") && p != null) {
						p.setPrice(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("userscore") && r != null) {
						r.setScore(StringUtils.toDouble(xmlParser.nextText(),
								0.0));
					} else if (tag.equalsIgnoreCase("lasttime") && r != null) {
						r.setLastTime(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("usetime") && r != null) {
						r.setUseTime(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("useranswers") && r != null) {
						String s = xmlParser.nextText();
						if (s != null) {
							if (s.indexOf(",") == 0)
								s = s.substring(1);
						}
						r.setAnswers(s);
					} else if (tag.equalsIgnoreCase("completed") && r != null) {
						int i = StringUtils.toInt(xmlParser.nextText(), 0);
						if (i == 0) {
							r.setTempAnswer(r.getAnswers());
							r.setAnswers(null);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("favor") && f != null
							&& q1 != null) {
						f.setUsername(username);
						favorList.add(f);
						q1.setRandNum(random.nextInt(26)+1);
						q1.encode();
						questionList.add(q1);
						f = null;
						q1 = null;
					} else if (tag.equalsIgnoreCase("favorList")
							&& favorList != null) {
						data.setFavorList(favorList);
					} else if (tag.equalsIgnoreCase("error") && e != null
							&& q2 != null) {
						e.setUsername(username);
						errorList.add(e);
						q2.setRandNum(random.nextInt(26)+1);
						q2.encode();
						questionList.add(q2);
						e = null;
						q2 = null;
					} else if (tag.equalsIgnoreCase("errorList")
							&& errorList != null) {
						data.setErrorList(errorList);
					} else if (tag.equalsIgnoreCase("record") && r != null
							&& p != null) {
						r.setUsername(username);
						recordList.add(r);
						paperList.add(p);
						r = null;
						p = null;
					} else if (tag.equalsIgnoreCase("examRecordList")
							&& recordList != null) {
						data.setPaperList(paperList);
						data.setRecordList(recordList);
					}
					break;
				}
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException exce) {
			throw AppException.xml(exce);
		} finally {
			stream.close();
		}
		data.setUsername(username);
		data.setQuestionList(questionList);
		return data;
	}

	// 解析所有的知识点
	public static ArrayList<Knowledge> parseKnowledge(InputStream stream)
			throws IOException, AppException {
		ArrayList<Knowledge> kList = new ArrayList<Knowledge>();
		Knowledge k = new Knowledge();
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("knowledge")) {
						k = new Knowledge();
					} else if (tag.equalsIgnoreCase("id") && k != null) {
						k.setKnowledgeId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("chapterid") && k != null) {
						k.setChapterId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId") && k != null) {
						k.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("title") && k != null) {
						k.setTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("content") && k != null) {
						k.setFullContent(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("orderId") && k != null) {
						k.setOrderId(StringUtils.toInt(xmlParser.nextText()));
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("knowledge") && k != null) {
						kList.add(k);
						k = null;
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException exce) {
			throw AppException.xml(exce);
		} finally {
			stream.close();
		}
		return kList;
	}

	// 解析计划
	public static ArrayList<Plan> parsePlan(InputStream stream)
			throws IOException, AppException {
		ArrayList<Plan> kList = new ArrayList<Plan>();
		Plan k = new Plan();
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, UTF8);
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("plan")) {
						k = new Plan();
					} else if (tag.equalsIgnoreCase("id") && k != null) {
						k.setId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("areaid") && k != null) {
						k.setAreaId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classid") && k != null) {
						k.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("dayid") && k != null) {
						k.setDaysId(StringUtils.toInt(xmlParser.nextText()));
					} else if (tag.equalsIgnoreCase("containsKid") && k != null) {
						k.setContainsKid(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("containsPid") && k != null) {
						k.setContainsPaperId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("summaryContent")
							&& k != null) {
						k.setSummaryContent(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("plan") && k != null) {
						kList.add(k);
						k = null;
					}
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				int a = xmlParser.next();
				evtType = a;
			}
		} catch (XmlPullParserException exce) {
			throw AppException.xml(exce);
		} finally {
			stream.close();
		}
		return kList;
	}
	public static ArrayList<Area> parseArea(InputStream stream) throws IOException,AppException {
		ArrayList<Area> list = new ArrayList<Area>();
		Area area = null;
		// 获得XmlPullParser解析�?
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(stream, "UTF-8");
			// 获得解析到的事件类别，这里有�?��文档，结束文档，�?��标签，结束标签，文本等等事件�?
			int evtType = xmlParser.getEventType();
			// �?��循环，直到文档结�?
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象�?
					if (tag.equalsIgnoreCase("area")) {
						area = new Area();
					} else if (tag.equalsIgnoreCase("id")) {
						area.setId(StringUtils.toInt(
								xmlParser.nextText(), -1));
					} else if (tag.equalsIgnoreCase("cname")&& area!=null) {
						area.setAreaCName(xmlParser.nextText().trim());
					}  else if (tag.equalsIgnoreCase("ename")&& area!=null) {
						area.setAreaEName(xmlParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					// 如果遇到标签结束，则把对象添加进集合�?
					if (tag.equalsIgnoreCase("area")&& area!=null) {
						list.add(area);
					}
					break;
				}
				// 如果xml没有结束，则导航到下�?��节点
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return list;
	}
	
	//测试解析
	public static UpdateDataEntity parseTest(InputStream stream1) throws AppException, IOException
	{
		InputStream stream = new FileInputStream("/mnt/sdcard/CHAccountant/data.xml");
		UpdateDataEntity entity = new UpdateDataEntity();
		XmlPullParser xmlParser = Xml.newPullParser();
		Paper p = null;
		ExamRule r = null;
		ExamQuestion q = null;
		try {
			xmlParser.setInput(stream, "UTF-8");
			int evtType = xmlParser.getEventType();
			// �?��循环，直到文档结�?
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {
				case XmlPullParser.START_TAG:
					if (tag.equalsIgnoreCase("addtime")) {
						entity.setDataAddTime(xmlParser.nextText());
					}else if (tag.equalsIgnoreCase("paper")) {
						p = new Paper();
					} else if (tag.equalsIgnoreCase("paperId") && p != null) {
						p.setPaperId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("paperName") && p != null) {
						p.setPaperName(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("addDate") && p != null) {
						p.setAddDate(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("classId") && p != null) {
						p.setClassId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("examTime") && p != null) {
						p.setPaperTime(StringUtils.toInt(xmlParser.nextText(),
								60));
					} else if (tag.equalsIgnoreCase("paperScore") && p != null) {
						p.setPaperSorce((StringUtils.toInt(
								xmlParser.nextText(), 100)));
					} else if (tag.equalsIgnoreCase("clickNum") && p != null) {
						p.setClickNum(StringUtils.toInt(xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("year") && p != null) {
						p.setYear(StringUtils.toInt(xmlParser.nextText(),
								Calendar.getInstance().get(Calendar.YEAR)));
					} else if (tag.equalsIgnoreCase("price") && p != null) {
						p.setPrice(xmlParser.nextText());
					}else if (tag.equalsIgnoreCase("rule")) {
						r = new ExamRule();
					} else if (tag.equalsIgnoreCase("paperId") && r != null) {
						r.setPaperId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleId") && r != null) {
						r.setRuleId(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleTitle") && r != null) {
						r.setFullTitle(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("ruleQuestionNum")
							&& r != null) {
						r.setQuestionNum(StringUtils.toInt(
								xmlParser.nextText(), 0));
					} else if (tag.equalsIgnoreCase("ruleScoreForEach")
							&& r != null) {
						r.setScoreForEach(StringUtils.toDouble(
								xmlParser.nextText(), 0.0));
					} else if (tag.equalsIgnoreCase("ruleScoreSet")
							&& r != null) {
						r.setScoreSet(xmlParser.nextText());
					} else if (tag.equalsIgnoreCase("type") && r != null) {
						String type = xmlParser.nextText();
						r.setRuleType(type);
						r.setRuleTitle(StringUtils.toRuleTitle(type));
					} else if (tag.equalsIgnoreCase("orderInPaper")
							&& r != null) {
						r.setOrderInPaper(StringUtils.toInt(
								xmlParser.nextText(), 1));
					}// containQids
					else if (tag.equalsIgnoreCase("containQids") && r != null) {
						r.setContainQids(xmlParser.nextText());
					}else if (tag.equalsIgnoreCase("question")) {
							q = new ExamQuestion();
						} else if (tag.equalsIgnoreCase("id") && q != null) {
							q.setQid(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("type") && q != null) {
							q.setQType(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("optionNum") && q != null) {
							q.setOptionNum(StringUtils.toInt(xmlParser.nextText(),
									0));
						} else if (tag.equalsIgnoreCase("content") && q != null) {
							String content = xmlParser.nextText();
							if(content!=null)
								q.setContent(content.replaceAll("&nbsp;", ""));
						} else if (tag.equalsIgnoreCase("answer") && q != null) {
							String answer = xmlParser.nextText();
							if (answer != null) {
								q.setAnswer(answer.replaceAll("\\s", ""));
							} else
								q.setAnswer(answer);
						} else if (tag.equalsIgnoreCase("analysis") && q != null) {
							q.setAnalysis(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("classId") && q != null) {
							q.setClassId(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("knowledgeId") && q != null) {
							q.setKnowledgeId(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("paperId") && q != null) {
							// q.setQid(xmlParser.nextText());
						} else if (tag.equalsIgnoreCase("ruleId") && q != null) {
							q.setRuleId(xmlParser.nextText());
						}else if(tag.equalsIgnoreCase("randNum")&&q!=null)
						{
//							q.setRandNum(StringUtils.toInt(xmlParser.nextText(), random.nextInt(26)+1));
							q.setRandNum(random.nextInt(26)+1);
						}else if(tag.equalsIgnoreCase("material")&&q!=null)
						{
							q.setMaterial(xmlParser.nextText());
						}
					break;
				case XmlPullParser.END_TAG:
					if (tag.equalsIgnoreCase("paper")&&p!=null) {
						entity.getPaperList().add(p);
						p = null;
					}else if (tag.equalsIgnoreCase("rule")&&r!=null) {
						entity.getRuleList().add(r);
						r = null;
					}else if (tag.equalsIgnoreCase("question")) {
						q.encode();
						entity.getQList().add(q);
						q = null;
					}
					break;
				}
				evtType = xmlParser.next();
			}
		}catch(XmlPullParserException e){
			throw AppException.xml(e);
		} finally {
			stream.close();
		}
		return entity;
	}
}
