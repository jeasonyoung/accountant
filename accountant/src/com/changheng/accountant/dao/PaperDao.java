package com.changheng.accountant.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.changheng.accountant.db.MyDBManager;
import com.changheng.accountant.entity.ExamErrorQuestion;
import com.changheng.accountant.entity.ExamFavor;
import com.changheng.accountant.entity.ExamNote;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRecord;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.Knowledge;
import com.changheng.accountant.entity.KnowledgeList;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.entity.PaperList;
import com.changheng.accountant.entity.QuestionAdapterData;
import com.changheng.accountant.entity.SyncData;
import com.changheng.accountant.util.StringUtils;

public class PaperDao {
	private MyDBManager dbManager;
	private static final String TAG = "PaperDao";
	public static final int pagesize = 10;

	public PaperDao(Context context) {
		dbManager = new MyDBManager(context);
	}

	/**
	 * 插入试卷和大题
	 * 
	 * @param paper
	 *            试卷
	 * @param rules
	 *            大题的集合
	 */
	public void insertPaper(Paper paper, List<ExamRule> rules) {
		/*
		 * 先看存不存在,不存在就加入
		 */
		if (paper == null)
			return;
		SQLiteDatabase db = dbManager.openDatabase();
		Log.d(TAG, "insertPaper方法打开了数据库连接");
		Cursor cursor = db.rawQuery(
				"select * from ExamPaperTab where paperid = ?",
				new String[] { paper.getPaperId() });
		if (cursor.getCount() > 0) {
			Log.d(TAG, "该试卷已经加过了");
			cursor.close();
			dbManager.closeDatabase();
			return;
		}
		cursor.close();
		// PAPERID ,PAPERNAME ,PAPERTIME ,PAPERSCORE ,PAPERYEAR ,CLASSID
		String sql = "insert into ExamPaperTab(paperid,papername,addtime,paperscore,papertime,year,clicknum,classid,totalnum)values(?,?,?,?,?,?,?,?,?)";
		Object[] params = new Object[] { paper.getPaperId(),
				paper.getPaperName(), paper.getAddDate(),
				paper.getPaperSorce(), paper.getPaperTime(), paper.getYear(),
				paper.getClickNum(), paper.getClassId(), paper.getTotalNum() };
		db.beginTransaction();
		try {
			db.execSQL(sql, params);
			if (rules != null && rules.size() > 0) {
				for (ExamRule r : rules) {
					db.execSQL(
							"insert into ExamRuleTab(ruleid,paperid,ruletitle,ruletitleinfo,ruletype,questionnum,scoreforeach,scoreset,orderinpaper)values(?,?,?,?,?,?,?,?,?)",
							new Object[] { r.getRuleId(), r.getPaperId(),
									r.getRuleTitle(), r.getFullTitle(),
									r.getRuleType(), r.getQuestionNum(),
									r.getScoreForEach(), r.getScoreSet(),
									r.getOrderInPaper() });
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
		Log.d(TAG, "insertPaper方法关闭了数据库连接");
	}

	public void insertPracticePaper(Paper p) {
		if (p == null)
			return;
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			// 插入试卷
			db.execSQL(
					"insert into ExamPaperTab(paperid,papername,addtime,paperscore,papertime,totalNum) values (?,?,datetime('now','localtime'),?,?,?)",
					new Object[] { p.getPaperId(), p.getPaperName(),
							p.getPaperSorce(), -1, p.getTotalNum() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public void updatePraticePaper(Paper paper) {
		if (paper == null)
			return;
		System.out.println("更新练习试卷。。。。。。。。。"+paper.getPaperSorce());
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "update ExamPaperTab set paperid=?,papername=?,addtime=datetime('now','localtime'),paperscore=?,papertime=?,year=?,clicknum=?,classid=?,totalnum=? where paperid = ?";
		Object[] params = new Object[] { paper.getPaperId(),
				paper.getPaperName(), paper.getPaperSorce(),
				paper.getPaperTime(), paper.getYear(), paper.getClickNum(),
				paper.getClassId(), paper.getTotalNum(),paper.getPaperId() };
		db.beginTransaction();
		try {
			db.execSQL(sql, params);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	/**
	 * 插入试卷的集合
	 * 
	 * @param list
	 * @return 返回更新的数量
	 */
	public int insertPaperList(List<Paper> list) {
		int count = 0;
		if (list != null && list.size() > 0) {
			SQLiteDatabase db = dbManager.openDatabase();
			String sql1 = "select * from ExamPaperTab where paperid = ?";
			String sql2 = "insert into ExamPaperTab(paperid,papername,addtime,paperscore,papertime,year,clicknum,price,classid,totalnum)values(?,?,?,?,?,?,?,?,?,?)";
			db.beginTransaction();
			try {
				for (Paper p : list) {
					Cursor cursor = db.rawQuery(sql1,
							new String[] { p.getPaperId() });
					if (cursor.getCount() > 0) {
						cursor.close();
						continue;
					}
					Object[] params = new Object[] { p.getPaperId(),
							p.getPaperName(), p.getAddDate(),
							p.getPaperSorce(), p.getPaperTime(), p.getYear(),
							p.getClickNum(), p.getPrice(), p.getClassId(),
							p.getTotalNum() };
					db.execSQL(sql2, params);
					count++;
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			dbManager.closeDatabase();
		}
		return count;
	}

	public List<Paper> findPapers(String ids) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String paperId, String paperName, int paperSorce, int
		// paperTime,String courseId,String examId
		String sql = "select paperid,papername,addtime,paperscore,papertime,year,clicknum,price,classid,totalNum from ExamPaperTab where paperid in ("
				+ ids + ")";
		String[] params = new String[] {};
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<Paper> list = new ArrayList<Paper>();
		while (cursor.moveToNext()) {
			Paper p = new Paper(cursor.getString(0), cursor.getString(1),
					cursor.getString(2), cursor.getInt(3), cursor.getInt(4),
					cursor.getInt(5), cursor.getInt(6), cursor.getInt(7),
					cursor.getString(8), cursor.getInt(9));
			list.add(p);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;

	}

	public PaperList findAllPapers(String classid, int page) {
		PaperList list = new PaperList();
		SQLiteDatabase db = dbManager.openDatabase();
		// String paperId, String paperName, int paperSorce, int
		// paperTime,String courseId,String examId
		String sql1 = "select count(*) from ExamPaperTab where papertime > -1";
		Cursor cursor = db.rawQuery(sql1, new String[] {});
		cursor.moveToNext();
		int total = cursor.getInt(0);
		cursor.close();
		if (total == 0) {
			dbManager.closeDatabase();
			return list;
		}
		list.setPaperCount(total);
		String sql = "select paperid,papername,addtime,paperscore,papertime,year,clicknum,price,classid,totalNum from ExamPaperTab where classid =? and papertime > 0 order by addtime desc limit 10 offset ? ";
		String[] params = new String[] { classid, page * 10 + "" };
		Cursor cursor1 = db.rawQuery(sql, params);
		ArrayList<Paper> pList = list.getPaperlist();
		while (cursor1.moveToNext()) {
			Paper p = new Paper(cursor1.getString(0), cursor1.getString(1),
					cursor1.getString(2), cursor1.getInt(3), cursor1.getInt(4),
					cursor1.getInt(5), cursor1.getInt(6), cursor1.getInt(7),
					cursor1.getString(8), cursor1.getInt(9));
			pList.add(p);
		}
		cursor1.close();
		dbManager.closeDatabase();
		return list;
	}

	// findpaperby id
	public Paper findPaperById(String paperid) {
		Paper p = null;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select paperid,papername,paperscore,addtime from ExamPaperTab where paperid = ? ",
						new String[] { paperid });
		if (cursor.moveToNext()) {
			p = new Paper(cursor.getString(0), cursor.getString(1),
					cursor.getInt(2));
			p.setAddDate(cursor.getString(3));
		}
		cursor.close();
		dbManager.closeDatabase();
		return p;
	}

	/**
	 * 插入大题组
	 * 
	 * @param rules
	 */
	public void insertRules(List<ExamRule> rules, String paperid) {
		if (rules != null && rules.size() > 0) {
			SQLiteDatabase db = dbManager.openDatabase();
			Log.i("PaperDao", "插入大题");
			db.beginTransaction();
			try {
				db.execSQL("delete from ExamRuleTab where paperid = ? ",
						new Object[] { paperid });
				for (ExamRule r : rules) {
					// RULEID TEXT,PAPERID TEXT,RULETITLE TEXT,RULETITLEINFO
					// TEXT,RULETYPE TEXT,QUESTIONNUM INTEGER,SCOREFOREACH
					// FLOAT,SCORESET TEXT,ORDERINPAPER INTEGER
					db.execSQL(
							"insert into ExamRuleTab(ruleid,paperid,ruletitle,ruletitleinfo,ruletype,questionnum,scoreforeach,scoreset,orderinpaper,containQids)values(?,?,?,?,?,?,?,?,?,?)",
							new Object[] { r.getRuleId(), r.getPaperId(),
									r.getRuleTitle(), r.getFullTitle(),
									r.getRuleType(), r.getQuestionNum(),
									r.getScoreForEach(), r.getScoreSet(),
									r.getOrderInPaper(), r.getContainQids() });
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			dbManager.closeDatabase();
		}
	}

	public List<ExamRule> findRules(String paperid) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String ruleId, String paperId, String ruleTitle,String
		// ruleTitleInfo,String ruleType,String scoreSet, int questionNum,
		// double scoreForEach, int orderInPaper
		String sql = "select ruleid,paperid,ruletitle,ruletitleinfo,ruletype,scoreset,questionnum,scoreforeach,orderinpaper,containQids from ExamRuleTab where paperid = ? order by orderinpaper asc";
		String[] params = new String[] { paperid };
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamRule> list = new ArrayList<ExamRule>();
		while (cursor.moveToNext()) {
			ExamRule r = new ExamRule(cursor.getString(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5), cursor.getInt(6),
					cursor.getDouble(7), cursor.getInt(8), cursor.getString(9));
			list.add(r);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public void insertQuestions(List<ExamQuestion> questions) {
		if (questions != null && questions.size() > 0) {
			SQLiteDatabase db = dbManager.openDatabase();
			// /////////
			// Cursor cursor =
			// db.rawQuery("select qid from ExamQuestionTab where qid = ?", new
			// String[]{questions.get(0).getPaperId()});
			// if(cursor.getCount()> 0)
			// {
			// cursor.close();
			// dbManager.closeDatabase();
			// return;
			// }
			// ////////
			try {
				db.beginTransaction();
				for (ExamQuestion q : questions) {
					// QID ,PAPERID ,EXAMID ,CONTENT ,ANSWER ,ANALYSIS ,QTYPE
					// ,OPTIONNUM ,ORDERID ,LINKQID
					// QID TEXT,RULEID TEXT,KNOWLEDGEID TEXT,CLASSID
					// TEXT,CONTENT TEXT,ANSWER TEXT,ANALYSIS TEXT,QTYPE
					// TEXT,OPTIONNUM INTEGER,ORDERID INTEGER,LINKQID TEXT
					Cursor cursor = db.rawQuery(
							"select qid from ExamQuestionTab where qid = ?",
							new String[] { q.getQid() });
					if (cursor.getCount() > 0) {
						cursor.close();
						continue;
					}
					cursor.close();
					db.execSQL(
							"insert into ExamQuestionTab(qid,ruleid,knowledgeid,classid,content,answer,analysis,qtype,optionnum,orderid,linkqid,randnum,material)values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new Object[] { q.getQid(), q.getRuleId(),
									q.getKnowledgeId(), q.getClassId(),
									q.getContent(), q.getAnswer(),
									q.getAnalysis(), q.getQType(),
									q.getOptionNum(), q.getOrderId(),
									q.getLinkQid(), q.getRandNum(),q.getMaterial() });
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			dbManager.closeDatabase();
		}
	}

	public List<ExamQuestion> findQuestionsByRuleId(String ruleId) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		String sql = "select qid,ruleid,knowledgeid,classid,content,answer,analysis,linkqid,qtype,optionnum,orderid,randnum,materail from ExamQuestionTab where ruleid = ? order by orderid asc";
		String[] params = new String[] { ruleId };
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public List<ExamQuestion> findQuestionByPaperId(String paperid) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		if (StringUtils.isEmpty(paperid))
			return null;
		String sql1 = "select containqids from ExamRuleTab where paperid = ? order by orderinpaper";
		String sql2 = "select qid,ruleid,knowledgeid,classid,content,answer,analysis,linkqid,qtype,optionnum,orderid,randnum,material from ExamQuestionTab where qid in (@@)";
		String[] params = new String[] { paperid };
		Cursor cursor = db.rawQuery(sql1, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			String qids = cursor.getString(0);
			Cursor cursor2 = db.rawQuery(sql2.replaceAll("@@", qids),
					new String[] {});
			while (cursor2.moveToNext()) {
				ExamQuestion q = new ExamQuestion(cursor2.getString(0),
						cursor2.getString(1), cursor2.getString(2),
						cursor2.getString(3), cursor2.getString(4),
						cursor2.getString(5), cursor2.getString(6),
						cursor2.getString(7), cursor2.getString(8),
						cursor2.getInt(9), cursor2.getInt(10));
				q.setRandNum(cursor2.getInt(11));
				q.setMaterial(cursor2.getString(12));
				list.add(q);
			}
			cursor2.close();
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public List<ExamQuestion> findQuestionById(String qid) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		String sql = "select qid,ruleid,knowledgeid,classid,content,answer,analysis,linkqid,qtype,optionnum,orderid,randnum,material from ExamQuestionTab where qid = ?";
		String[] params = new String[] { qid };
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public List<ExamQuestion> findQuestionByKnowledgeId(String knowledgeid) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		String sql = "select qid,ruleid,knowledgeid,classid,content,answer,analysis,linkqid,qtype,optionnum,orderid,randnum,material from ExamQuestionTab where knowledgeid = ?";
		String[] params = new String[] { knowledgeid };
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	/**
	 * 根据一组id,查找对应题目的集合
	 * 
	 * @param qids
	 *            类似"1,2,3"
	 * @return
	 */
	public List<ExamQuestion> findQuestionFromQids(String qids) {
		if (qids == null) {
			return null;
		}
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		String sql = "select q.qid,q.ruleid,q.knowledgeid,q.classid,q.content,q.answer,q.analysis,q.linkqid,q.qtype,q.optionnum,q.orderid,q.randnum,q.material from ExamQuestionTab q where q.qid in ("
				+ qids + ") order by q.ruleid asc,q.qid asc";
		String[] params = new String[] {};
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	/**
	 * 查询错题,题目的集合
	 * 
	 * @param username
	 * @param param
	 *            参数(类似 q.classid = ? or e.errornum = 1 or e.addtime >
	 *            datetime('2013-09-09') )
	 * @return
	 */
	public List<ExamQuestion> findQuestionFromErrors(String username,
			String param) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		String sql = "select distinct q.qid,q.ruleid,q.knowledgeid,q.classid,q.content,q.answer,q.analysis,q.linkqid,q.qtype,q.optionnum,q.orderid,q.randnum,q.material from ExamQuestionTab q join ExamErrorQuestionTab e on q.qid = e.qid where "
				+ param + " and e.status = 0 order by q.ruleid asc,q.qid asc";
		String[] params = new String[] {};
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public List<ExamQuestion> findQuestionFromFavors(String username,
			String classid) {
		Log.i(TAG, "find QuestionfromFavors");
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		String sql = "select distinct q.qid,q.ruleid,q.knowledgeid,q.classid,q.content,q.answer,q.analysis,q.linkqid,q.qtype,q.optionnum,q.orderid,q.randnum,q.material from ExamQuestionTab q,ExamFavorTab f where q.qid = f.qid and q.classid = ? and (f.username = ? or f.username = '中文') and f.status = 0 order by q.ruleid asc,q.qid asc";
		String[] params = new String[] { classid, username };
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	/**
	 * 保存或更新考试记录
	 * 
	 * @param r
	 *            PAPERID ,USERNAME ,SCORE ,LASTIME ,USETIME ,TEMPTIME ,ANSWERS
	 *            ,TEMPANSWER
	 */
	public void saveOrUpdateRecord(ExamRecord r) // 每人每套试卷只有一个记录
	{
		if (r == null)
			return;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select * from ExamRecordTab where paperid = ? and username = ?",
						new String[] { r.getPaperId(), r.getUsername() });
		if (cursor.getCount() > 0) {
			cursor.close();
			String sql = "update ExamRecordTab set score = ?,usetime=?,temptime=?,answers=?,tempanswer=?,lasttime = datetime(?),isDone = ?,errornum = ? where paperid = ? and username = ? ";
			Object[] params = new Object[] { r.getScore(), r.getUseTime(),
					r.getTempTime(), r.getAnswers(), r.getTempAnswer(),
					r.getLastTime(), r.getIsDone(), r.getErrorNum(),
					r.getPaperId(), r.getUsername() };
			db.execSQL(sql, params);
			dbManager.closeDatabase();
			return;
		}
		cursor.close();
		String sql = "insert into ExamRecordTab(paperid,username,score,usetime,temptime,answers,tempanswer,lasttime,isDone,mode,errornum)values(?,?,?,?,?,?,?,?,?,?,?)";
		System.out.println("插入学习记录2");
		Object[] params = new Object[] { r.getPaperId(), r.getUsername(),
				r.getScore(), r.getUseTime(), r.getTempTime(), r.getAnswers(),
				r.getTempAnswer(), r.getLastTime(), r.getIsDone(), r.getMode(),
				r.getErrorNum() };
		db.beginTransaction();
		try {
			db.execSQL(sql, params);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	/*
	 * 开始考试就要加记录
	 */
	public ExamRecord insertRecord(ExamRecord r, int mode) {
		if (r == null)
			return null;
		SQLiteDatabase db = dbManager.openDatabase();
		/*
		 * String paperId, String username, String answers, String tempAnswer,
		 * double score, int useTime, int tempTime,String lastTime
		 */
		Cursor cursor = db
				.rawQuery(
						"select paperid,username,answers,tempanswer,score,usetime,temptime,lasttime,isDone,mode,errornum from ExamRecordTab where paperid = ? and username = ?",
						new String[] { r.getPaperId(), r.getUsername() });
		if (cursor.moveToNext()) {
			ExamRecord record = new ExamRecord(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getDouble(4), cursor.getInt(5),
					cursor.getInt(6), cursor.getString(7), cursor.getString(8));
			record.setMode(cursor.getInt(9));
			record.setErrorNum(cursor.getInt(10));
			cursor.close();
			dbManager.closeDatabase();
			return record;
		}
		cursor.close();
		String sql = "insert into ExamRecordTab(paperid,username,score,usetime,temptime,answers,tempanswer,lasttime,isDone,mode,errornum)values(?,?,?,?,?,?,?,?,?,?,?)";
		System.out.println("插入学习记录1");
		Object[] params = new Object[] { r.getPaperId(), r.getUsername(),
				r.getScore(), r.getUseTime(), r.getTempTime(), r.getAnswers(),
				r.getTempAnswer(), r.getLastTime(), r.getIsDone(), mode,
				r.getErrorNum() };
		db.beginTransaction();
		try {
			db.execSQL(sql, params);
			db.setTransactionSuccessful();
			return r;
		} finally {
			db.endTransaction();
			dbManager.closeDatabase();
		}
	}

	/**
	 * 查找考试记录
	 */
	public ExamRecord findRecord(String username, String paperId) {
		ExamRecord r = null;
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = null;
		String[] params = null;
		if (username != null) {
			sql = "select r.paperid,r.username,r.answers,r.tempanswer,r.score,r.usetime,r.temptime,r.lasttime,r.isDone,r.mode,r.errornum,p.papername,p.papertime from ExamRecordTab r,ExamPaperTab p where r.paperid = p.paperid and r.paperid = ? and r.username = ?";
			params = new String[] { paperId, username };
		} else {
			sql = "select r.paperid,r.username,r.answers,r.tempanswer,r.score,r.usetime,r.temptime,r.lasttime,r.isDone,r.mode,r.errornum,p.papername,p.papertime from ExamRecordTab r,ExamPaperTab p where r.paperid = p.paperid and r.paperid = ?";
			params = new String[] { paperId };
		}
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.moveToNext()) {
			r = new ExamRecord(cursor.getString(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getDouble(4), cursor.getInt(5), cursor.getInt(6),
					cursor.getString(7), cursor.getString(8));
			r.setMode(cursor.getInt(9));
			r.setErrorNum(cursor.getInt(10));
			r.setPapername(cursor.getString(11));
			r.setPapertime(cursor.getInt(12));
		}
		cursor.close();
		dbManager.closeDatabase();
		return r;
	}

	public List<ExamRecord> findRecordsByUsername(String username) {
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select distinct r.paperid,r.username,r.answers,r.tempanswer,r.score,r.usetime,r.temptime,r.lasttime,r.isDone ,p.papername,p.papertime,p.paperscore,r.mode,r.errornum  from ExamRecordTab r,ExamPaperTab p where r.paperid = p.paperid and username = ?";
		Cursor cursor = db.rawQuery(sql, new String[] { username });
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamRecord> list = new ArrayList<ExamRecord>();
		while (cursor.moveToNext()) {
			ExamRecord r = new ExamRecord(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getDouble(4), cursor.getInt(5),
					cursor.getInt(6), cursor.getString(7), cursor.getString(8));
			r.setPapername(cursor.getString(9));
			r.setPapertime(cursor.getInt(10));
			r.setPaperscore(cursor.getInt(11));
			r.setMode(cursor.getInt(12));
			r.setErrorNum(cursor.getInt(13));
			list.add(r);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public void deleteRecord(ExamRecord r) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			System.out.println("删除考试记录");
			db.execSQL(
					"delete from ExamRecordTab where username = ? and paperid = ?",
					new Object[] { r.getUsername(), r.getPaperId() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	/**
	 * 更新临时的答案
	 * 
	 * @param r
	 */
	public void updateTempAnswerForRecord(ExamRecord r) {
		if (r == null)
			return;
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			db.execSQL(
					"update ExamRecordTab set tempanswer = ? ,isdone = ? where paperid = ? and username = ?",
					new Object[] { r.getTempAnswer(), r.getIsDone(),
							r.getPaperId(), r.getUsername() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public void insertError(ExamErrorQuestion e) {
		if (e == null)
			return;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select errornum,status from ExamErrorQuestionTab where qid = ? and username = ?",
						new String[] { e.getQid(), e.getUsername() });
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			int number = 0;
			if (cursor.getInt(1) == 0) // 如果没有被删除
				number = cursor.getInt(0) + 1;
			cursor.close();
			// 更新次数+1
			db.execSQL(
					"update ExamErrorQuestionTab set errornum = ? ,lastAnswer = ?,status = 0 where qid = ? and username = ?",
					new Object[] { number, e.getLastAnswer(), e.getQid(),
							e.getUsername() });
			dbManager.closeDatabase();
			return;
		}
		cursor.close();
		db.beginTransaction();
		try {
			// QID TEXT,EXAMID TEXT,ERRORNUM INTEGER,USERNAME TEXT
			// 第一次加入,errornum为1,状态为0
			db.execSQL(
					"insert into ExamErrorQuestionTab(qid,paperid,errornum,username,lastanswer,status)values(?,?,?,?,?,0) ",
					new Object[] { e.getQid(), e.getPaperId(), 1,
							e.getUsername(), e.getLastAnswer() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public int findErrorNumOfPractice(String paperid) {
		int num = 0;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select count(qid) from ExamErrorQuestionTab where paperid = ? and status = 0",
						new String[] { paperid });
		cursor.moveToNext();
		num = cursor.getInt(0);
		cursor.close();
		dbManager.closeDatabase();
		return num;
	}

	public void insertFavor(ExamFavor f) {
		if (f == null)
			return;
		Log.i(TAG, "inserFavor");
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select status from ExamFavorTab where qid = ? and username = ?",
						new String[] { f.getQid(), f.getUsername() });
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			if (cursor.getInt(0) == 1) // 如果被删除
			{
				cursor.close();
				// 更新
				db.execSQL(
						"update ExamFavorTab set status = 0 where qid = ? and username = ?",
						new Object[] { f.getQid(), f.getUsername() });
				dbManager.closeDatabase();
				return;
			} else {
				return;
			}
		}
		cursor.close();
		db.beginTransaction();
		try {
			// QID TEXT,EXAMID TEXT,USERNAME TEXT)
			db.execSQL(
					"insert into ExamFavorTab (qid,paperid,username,status) values (?,?,?,0) ",
					new Object[] { f.getQid(), f.getPaperId(), f.getUsername() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public void insertNote(ExamNote n) {
		// QID TEXT,EXAMID TEXT,CONTENT TEXT,ADDTIME DATETIME DEFAULT
		// (datetime('now','localtime')),USERNAME TEXT
		if (n == null)
			return;
		SQLiteDatabase db = dbManager.openDatabase();
		String[] params = new String[] { n.getQid(), n.getUsername() };
		Cursor cursor = db.rawQuery(
				"select * from ExamNoteTab where qid=? and username = ?",
				params);
		if (cursor.getCount() > 0) {
			cursor.close();
			db.beginTransaction();
			try {
				db.execSQL(
						"update ExamNoteTab set content = ?, paperid = ?, addtime=datetime(?) where qid = ? and username = ? ",
						params);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			dbManager.closeDatabase();
			return;
		}
		cursor.close();
		db.beginTransaction();
		try {
			db.execSQL(
					"insert into ExamNoteTab(qid,paperid,content,addtime,username)values(?,?,?,datetime(?),?) ",
					new Object[] { n.getQid(), n.getPaperId(), n.getContent(),
							n.getAddTime(), n.getUsername() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public String findNoteContent(String qid, String username) {
		String content = null;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select content from ExamNoteTab where qid = ? and username = ?",
						new String[] { qid, username });
		if (cursor.moveToNext()) {
			content = cursor.getString(0);
		}
		cursor.close();
		dbManager.closeDatabase();
		return content;
	}

	// 假移除
	public void deleteFavor(ExamFavor f) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			db.execSQL(
					"update ExamFavorTab set status = 1 where username = ? and qid = ?",
					new Object[] { f.getUsername(), f.getQid() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public void deleteFavors(String username, String qids) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			db.execSQL(
					"delete from ExamFavorTab where username = ? and qid in ("
							+ qids + ")", new Object[] { username });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	// 假移除
	public void moveOutError(String username, String qid) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			db.execSQL(
					"update ExamErrorQuestionTab set status = 1 where username = ? and qid = ?",
					new Object[] { username, qid });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	// 真移除
	public void deleteErrors(String username, String qids) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			db.execSQL(
					"delete from ExamErrorQuestionTab where username = ? and qid in ("
							+ qids + ")", new Object[] { username });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public void deleteNote(ExamNote note) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			db.execSQL(
					"delete from ExamNoteTab where username = ? and qid = ?",
					new Object[] { note.getUsername(), note.getQid() });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	public List<ExamNote> findNotes(String classid, String username) {
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select n.qid,n.addtime,n.content from ExamNoteTab n join ExamQuestionTab q on n.qid = q.qid where q.classid =? and username = ? order by addtime desc",
						new String[] { classid, username });
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamNote> list = new ArrayList<ExamNote>();
		while (cursor.moveToNext()) {
			ExamNote note = new ExamNote(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), username, classid);
			list.add(note);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public List<ExamFavor> findFavors(String username) {
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select qid,paperid from ExamFavorTab where username = ? and status = 0",
						new String[] { username });
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<ExamFavor> list = new ArrayList<ExamFavor>();
		while (cursor.moveToNext()) {
			ExamFavor favor = new ExamFavor(cursor.getString(0), username,
					cursor.getString(1));
			list.add(favor);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	public List<QuestionAdapterData> findAdapterData(String actionName) {
		SQLiteDatabase db = dbManager.openDatabase();
		String[] params = new String[] {};
		String tabName = "myNotes".equals(actionName) ? "ExamNoteTab"
				: "myErrors".equals(actionName) ? "ExamErrorQuestionTab"
						: "myFavors".equals(actionName) ? "ExamFavorTab" : "";
		if ("".equals(tabName)) {
			return null;
		}
		String sql = "select c.classid,c.classname, count(temTab.temqid) from ClassTab c"
				+ " left join (select q.qid temqid,q.classid temclassid from "
				+ tabName
				+ " e join ExamQuestionTab q on e.qid = q.qid where e.status = 0) as temTab"
				+ " on c.classid = temTab.temclassid group by c.classid";
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		List<QuestionAdapterData> list = new ArrayList<QuestionAdapterData>();
		while (cursor.moveToNext()) {
			QuestionAdapterData data = new QuestionAdapterData(
					cursor.getString(0), cursor.getString(1), cursor.getInt(2));
			list.add(data);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	/**
	 * 找错题的的另外两个模式的数据统计
	 * 
	 * @param mode
	 * @return
	 */
	public List<QuestionAdapterData> findErrorAdapterData(int mode) {
		List<QuestionAdapterData> list = new ArrayList<QuestionAdapterData>();
		SQLiteDatabase db = dbManager.openDatabase();
		switch (mode) {
		case 2:
			// 遗忘曲线

			for (int i = 1; i < 5; i++) {
				// 0:当天,1:三天内,2:一周内,3:更久的
				// 1 2 3 4
				// 0 3 7 8
				String sql = "select count(qid) from ExamErrorQuestionTab where addtime > datetime(?) and status = 0";
				int k = (i - 1) * (i - 1) + i;
				if (i == 1) {
					k = 0;
				} else if (i == 4) // 更久的
				{
					k = 7;
					sql = "select count(qid) from ExamErrorQuestionTab where addtime < datetime(?) and status = 0";
				}
				Calendar c = Calendar.getInstance();
				c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
						c.get(Calendar.DAY_OF_MONTH) - k, 0, 0, 0); // 当天
				QuestionAdapterData data = new QuestionAdapterData();
				data.setCourseId("e.addtime > datetime('"
						+ StringUtils.toDateStr(c.getTime()) + "') ");
				if (i == 4) {
					data.setCourseId("e.addtime < datetime('"
							+ StringUtils.toDateStr(c.getTime()) + "') ");
				}
				data.setTitle(i == 1 ? "当天" : i == 2 ? "三天内" : i == 3 ? "一周内"
						: "更久的");
				System.out.println(data.getTitle() + " "
						+ StringUtils.toDateStr(c.getTime()));
				Cursor cursor = db.rawQuery(sql,
						new String[] { StringUtils.toDateStr(c.getTime()) });
				cursor.moveToNext();
				data.setCount(cursor.getInt(0));
				cursor.close();
				list.add(data);
			}
			break;
		case 3:
			// 出错频率,1,2,>2
			String sql = "select count(qid) from ExamErrorQuestionTab where errornum = ? and status = 0";
			for (int j = 1; j < 4; j++) {
				QuestionAdapterData data = new QuestionAdapterData();
				data.setCourseId("e.errornum = " + j);
				data.setTitle("错 " + j + " 次");
				if (j > 2) {
					data.setTitle("错 3 次或以上");
					data.setCourseId("e.errornum >= " + j);
					sql = "select count(qid) from ExamErrorQuestionTab where errornum >= ? and status = 0";
				}
				Cursor cursor = db.rawQuery(sql,
						new String[] { String.valueOf(j) });
				cursor.moveToNext();
				data.setCount(cursor.getInt(0));
				cursor.close();
				list.add(data);
			}
			break;
		}
		dbManager.closeDatabase();
		return list;
	}

	public StringBuilder findFavorQids(String username, String paperId) {
		// TODO Auto-generated method stub
		StringBuilder buf = new StringBuilder();
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = null;
		String[] params = null;
		// if (paperId != null) {
		// sql =
		// "select qid from ExamFavorTab where username = ? and paperId = ? and status = 0";
		// params = new String[] { username, paperId };
		// } else {
		sql = "select qid from ExamFavorTab where username = ? and status = 0";
		params = new String[] { username };
		// }
		Cursor cursor = db.rawQuery(sql, params);
		while (cursor.moveToNext()) {
			buf.append(cursor.getString(0)).append(",");
		}
		cursor.close();
		dbManager.closeDatabase();
		return buf;
	}

	public void insertKnowledge(KnowledgeList list) {
		if (list == null) {
			return;
		}
		if (list.getPaper() == null) {
			return;
		}
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			Paper p = list.getPaper();
			// 插入试卷
			db.execSQL(
					"insert into ExamPaperTab(paperid,papername,addtime,paperscore,papertime,totalNum) values (?,?,datetime('now','localtime'),?,?,?)",
					new Object[] { p.getPaperId(), p.getPaperName(),
							p.getPaperSorce(), -1, p.getTotalNum() });

			// KNOWLEDGEID,KNOWLEDGETITLE,KNOWLEDGECONTENT,CHAPTERID,CLASSID,ORDERID
			ArrayList<Knowledge> kList = list.getKList();
			for (Knowledge k : kList) {
				db.execSQL(
						"insert into KnowledgeTab(knowledgeid,knowledgetitle,knowledgecontent,chapterid,orderid) values (?,?,?,?,?)",
						new Object[] { k.getKnowledgeId(), k.getTitle(),
								k.getFullContent(), k.getChapterId(),
								k.getOrderId() });
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			dbManager.closeDatabase();
		}
	}

	public ArrayList<Knowledge> findKnowledge(String chapterid) {
		ArrayList<Knowledge> kList = new ArrayList<Knowledge>();
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select knowledgeid,chapterid,knowledgetitle,orderid from KnowledgeTab where chapterid = ?",
						new String[] { chapterid });
		while (cursor.moveToNext()) {
			Knowledge k = new Knowledge(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), null,
					cursor.getInt(3));
			kList.add(k);
		}
		cursor.close();
		dbManager.closeDatabase();
		return kList;

	}
	public ArrayList<Knowledge> findKnowledgeByKids(String kids)
	{
		ArrayList<Knowledge> list = new ArrayList<Knowledge>();
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select k.knowledgeid,k.chapterid,k.knowledgetitle,k.knowledgeContent,k.orderid,count(q.qid) from KnowledgeTab k,ExamQuestionTab q"
				+	" where q.knowledgeid = k.knowledgeid and k.knowledgeid in("+kids+") group by k.knowledgeid";
		Cursor cursor = db.rawQuery(sql, new String[]{});
		while(cursor.moveToNext())
		{
			Knowledge k = new Knowledge(cursor.getString(0),
					cursor.getString(1), cursor.getString(2), cursor.getString(3),
					cursor.getInt(4));
			k.setQuestionCount(cursor.getInt(5));
			list.add(k);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}
	public String findKnowledgeContent(String knowledgeId) {
		// TODO Auto-generated method stub
		String content = null;
		SQLiteDatabase db = dbManager.openDatabase();
		Cursor cursor = db
				.rawQuery(
						"select knowledgetitle,knowledgecontent from KnowledgeTab where knowledgeid = ?",
						new String[] { knowledgeId });
		if (cursor.moveToNext()) {
			content = "<P>" + cursor.getString(0) + "</P>"
					+ cursor.getString(1);
		}
		cursor.close();
		dbManager.closeDatabase();
		return content;
	}

	public ExamRecord findLastRecord(int mode) {
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select r.paperid,r.username,r.answers,r.tempanswer,r.score,r.usetime,r.temptime,r.lasttime,r.isDone ,p.papername,p.papertime,p.paperscore,r.mode,r.errornum  from ExamRecordTab r,ExamPaperTab p where r.paperid = p.paperid and mode = ? order by lasttime desc limit 1";
		Cursor cursor = db.rawQuery(sql, new String[] { String.valueOf(mode) });
		ExamRecord r = null;
		if (cursor.moveToNext()) {
			r = new ExamRecord(cursor.getString(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getDouble(4), cursor.getInt(5), cursor.getInt(6),
					cursor.getString(7), cursor.getString(8));
			r.setPapername(cursor.getString(9));
			r.setPapertime(cursor.getInt(10));
			r.setPaperscore(cursor.getInt(11));
			r.setMode(cursor.getInt(12));
			r.setErrorNum(cursor.getInt(13));
		}
		cursor.close();
		dbManager.closeDatabase();
		return r;
	}

	public String findSyncFavorData(String username) {
		StringBuilder buf = new StringBuilder();
		buf.append("&ClassID=");
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select qid from ExamFavorTab where username = ? and status = 0";
		Cursor cursor = db.rawQuery(sql, new String[] { username });
		while (cursor.moveToNext()) {
			buf.append(cursor.getString(0));
			buf.append(",");
		}
		cursor.close();
		if (buf.lastIndexOf(",") == buf.length() - 1) {
			buf.deleteCharAt(buf.length() - 1);
		}
		buf.append("&DelID=");
		String sql2 = "select qid from ExamFavorTab where username = ? and status = 1";
		Cursor cursor2 = db.rawQuery(sql2, new String[] { username });
		while (cursor2.moveToNext()) {
			buf.append(cursor2.getString(0));
			buf.append(",");
		}
		cursor2.close();
		dbManager.closeDatabase();
		if (buf.lastIndexOf(",") == buf.length() - 1)
			return buf.substring(0, buf.length() - 1);
		;
		return buf.toString();
	}

	public String findSyncErrorData(String username) {
		StringBuilder buf = new StringBuilder();
		buf.append("&ClassID=");
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select qid,lastanswer from ExamErrorQuestionTab where username = ? and status = 0";
		Cursor cursor = db.rawQuery(sql, new String[] { username });
		while (cursor.moveToNext()) {
			buf.append(cursor.getString(0)).append(".")
					.append(cursor.getString(1).replaceAll(",", "L"));
			buf.append(",");
		}
		cursor.close();
		if (buf.lastIndexOf(",") == buf.length() - 1) {
			buf.deleteCharAt(buf.length() - 1);
		}
		buf.append("&DelID=");
		String sql2 = "select qid from ExamErrorQuestionTab where username = ? and status = 1";
		Cursor cursor2 = db.rawQuery(sql2, new String[] { username });
		while (cursor2.moveToNext()) {
			buf.append(cursor2.getString(0));
			buf.append(",");
		}
		cursor2.close();
		dbManager.closeDatabase();
		if (buf.lastIndexOf(",") == buf.length() - 1)
			return buf.substring(0, buf.length() - 1);
		;
		return buf.toString();
	}

	public String findSyncRecordData(String username) {
		StringBuilder buf = new StringBuilder();
		buf.append("&ClassID=");
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select paperid,score,answers,tempanswer,lasttime from ExamRecordTab where username = ? and mode = 0";
		Cursor cursor = db.rawQuery(sql, new String[] { username });
		while (cursor.moveToNext()) {
			String answers = cursor.getString(2);
			String tempanswer = cursor.getString(3);
			if (StringUtils.isEmpty(answers) && StringUtils.isEmpty(tempanswer)) {
				continue;
			} else {
				buf.append(cursor.getString(0)).append(".");
				if (StringUtils.isEmpty(tempanswer)) {
					buf.append(cursor.getString(1)).append(".")
							.append(answers.replaceAll("&", "Q")).append(".")
							.append(cursor.getString(4).replaceAll(" ", "D"))
							.append(".").append(1);
				} else {
					buf.append(0).append(".")
							.append(tempanswer.replaceAll("&", "Q"))
							.append(".")
							.append(cursor.getString(4).replaceAll(" ", "D"))
							.append(".").append(0);
				}
				buf.append(",");
			}
		}
		cursor.close();
		// if (buf.lastIndexOf(",") == buf.length() - 1) {
		// buf.deleteCharAt(buf.length() - 1);
		// }
		// buf.append("&DelID=");
		// String sql2 =
		// "select qid from ExamErrorQuestionTab where username = ? and status = 1";
		// Cursor cursor2 = db.rawQuery(sql2, new String[] { username });
		// while (cursor2.moveToNext()) {
		// buf.append(cursor2.getString(0));
		// buf.append(",");
		// }
		// cursor2.close();
		dbManager.closeDatabase();
		if (buf.lastIndexOf(",") == buf.length() - 1)
			return buf.substring(0, buf.length() - 1);
		return buf.toString();
	}

	public boolean syncIntoDB(SyncData data) {
		if (data == null) {
			return false;
		}
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			ArrayList<ExamQuestion> questions = data.getQuestionList();
			if (questions != null && questions.size() > 0) {
				System.out.println("同步数据：问题总数=" + questions.size());
				// 插入试题
				for (ExamQuestion q : questions) {
					Cursor cursor = db.rawQuery(
							"select qid from ExamQuestionTab where qid = ?",
							new String[] { q.getQid() });
					if (cursor.getCount() > 0) {
						cursor.close();
						continue;
					}
					cursor.close();
					db.execSQL(
							"insert into ExamQuestionTab(qid,ruleid,knowledgeid,classid,content,answer,analysis,qtype,optionnum,orderid,linkqid,randnum,material)values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new Object[] { q.getQid(), q.getRuleId(),
									q.getKnowledgeId(), q.getClassId(),
									q.getContent(), q.getAnswer(),
									q.getAnalysis(), q.getQType(),
									q.getOptionNum(), q.getOrderId(),
									q.getLinkQid(), q.getRandNum(),q.getMaterial() });
				}
				// 插入favor , errors
				if (data.getFavorList() != null
						&& data.getFavorList().size() > 0) {
					System.out.println("同步数据：新增收藏总数=" + questions.size());
					for (ExamFavor f : data.getFavorList()) {
						db.execSQL(
								"insert into ExamFavorTab (qid,paperid,username,status) values (?,?,?,0) ",
								new Object[] { f.getQid(), f.getPaperId(),
										f.getUsername() });
					}
				}
				if (data.getErrorList() != null
						&& data.getErrorList().size() > 0) {
					System.out.println("同步数据：新增错误总数=" + questions.size());
					for (ExamErrorQuestion e : data.getErrorList()) {
						db.execSQL(
								"insert into ExamErrorQuestionTab(qid,paperid,errornum,username,lastanswer,status)values(?,?,?,?,?,0) ",
								new Object[] { e.getQid(), e.getPaperId(), 1,
										e.getUsername(), e.getLastAnswer() });
					}
				}
			}
			if (!StringUtils.isEmpty(data.getFavorNeedDeleteQids())) {
				db.execSQL(
						"delete from ExamFavorTab where username = ? and qid in ("
								+ data.getFavorNeedDeleteQids() + ")",
						new Object[] { data.getUsername() });
			}
			if (!StringUtils.isEmpty(data.getErrorNeedDeleteQids())) {
				db.execSQL(
						"delete from ExamErrorQuestionTab where username = ? and qid in ("
								+ data.getErrorNeedDeleteQids() + ")",
						new Object[] { data.getUsername() });
			}
			ArrayList<Paper> list = data.getPaperList();
			if (list != null && list.size() > 0) {
				String sql1 = "select * from ExamPaperTab where paperid = ?";
				for (Paper p : list) {
					Cursor cursor = db.rawQuery(sql1,
							new String[] { p.getPaperId() });
					if (cursor.getCount() > 0) {
						cursor.close();
					} else {
						cursor.close();
						System.out.println("插入试卷，点解");
						db.execSQL(
								"insert into ExamPaperTab(paperid,papername,addtime,paperscore,papertime,year,clicknum,price,classid,totalnum)values(?,?,?,?,?,?,?,?,?,?)",
								new Object[] { p.getPaperId(),
										p.getPaperName(), p.getAddDate(),
										p.getPaperSorce(), p.getPaperTime(),
										p.getYear(), p.getClickNum(),
										p.getPrice(), p.getClassId(),
										p.getTotalNum() });
					}
				}
				ArrayList<ExamRecord> recordList = data.getRecordList();
				System.out.println("recordList size = " + recordList.size());
				for (ExamRecord r : recordList) {
					Cursor cursor = db
							.rawQuery(
									"select * from ExamRecordTab where paperid = ? and username = ?",
									new String[] { r.getPaperId(),
											r.getUsername() });
					if (cursor.getCount() > 0) {
						cursor.close();
						String sql = null;
						Object[] params = new Object[] { r.getScore(),
								r.getUseTime(), r.getTempTime(),
								r.getTempAnswer(), r.getLastTime(),
								r.getIsDone(), r.getErrorNum(), r.getPaperId(),
								r.getUsername() };
						if (r.getAnswers() == null)
							sql = "update ExamRecordTab set score = ?,usetime=?,temptime=?,tempanswer=?,lasttime = datetime(?),isDone = ?,errornum = ?,issync = 1 where paperid = ? and username = ? ";
						else {
							sql = "update ExamRecordTab set score = ?,usetime=?,temptime=?,answers=?,lasttime = datetime(?),isDone = ?,errornum = ?,issync = 1 where paperid = ? and username = ? ";
							params[3] = r.getAnswers();
						}
						db.execSQL(sql, params);
					} else {
						cursor.close();
						db.execSQL(
								"insert into ExamRecordTab(paperid,username,score,usetime,temptime,answers,tempanswer,lasttime,isDone,mode,errornum,issync)values(?,?,?,?,?,?,?,?,?,?,?,1)",
								new Object[] { r.getPaperId(), r.getUsername(),
										r.getScore(), r.getUseTime(),
										r.getTempTime(), r.getAnswers(),
										r.getTempAnswer(), r.getLastTime(),
										r.getIsDone(), r.getMode(),
										r.getErrorNum() });
					}
				}
			}
			System.out.println("db.setTransactionSuccessful................");
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			db.endTransaction();
			dbManager.closeDatabase();
		}
		return true;
	}

	// 获取某知识点下的考题的ID,用","分隔
	public String getQidsUnderKnowledge(String knowledgeId) {
		StringBuilder buf = new StringBuilder();
		buf.append("&&NotID=");
		SQLiteDatabase db = dbManager.openDatabase();
		String sql = "select qid from ExamQuestionTab where knowledgeid = ?";
		Cursor cursor = db.rawQuery(sql, new String[] { knowledgeId });
		while (cursor.moveToNext()) {
			buf.append(cursor.getString(0)).append(",");
		}
		cursor.close();
		dbManager.closeDatabase();
		if (buf.lastIndexOf(",") == buf.length() - 1)
			return buf.substring(0, buf.length() - 1);
		return buf.toString();
	}

	public ArrayList<ExamQuestion> findQuestionOfKnowledge(
			ArrayList<ExamQuestion> qList, String knowledgeId) {
		SQLiteDatabase db = dbManager.openDatabase();
		// String qid, String paperId, String content,
		// String answer, String analysis, String linkQid,
		// int qType, int optionNum, int orderId
		// 先插入，再查找
		if (qList != null && qList.size() > 0) {
			db.beginTransaction();
			try {
				for (ExamQuestion q : qList) {
					db.execSQL(
							"insert into ExamQuestionTab(qid,ruleid,knowledgeid,classid,content,answer,analysis,qtype,optionnum,orderid,linkqid,randnum,material)values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new Object[] { q.getQid(), q.getRuleId(),
									q.getKnowledgeId(), q.getClassId(),
									q.getContent(), q.getAnswer(),
									q.getAnalysis(), q.getQType(),
									q.getOptionNum(), q.getOrderId(),
									q.getLinkQid(), q.getRandNum(),q.getMaterial() });
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				qList.clear();
			}
		}
		String sql = "select qid,ruleid,knowledgeid,classid,content,answer,analysis,linkqid,qtype,optionnum,orderid,randnum,material from ExamQuestionTab where knowledgeid = ? order by qtype asc,qid asc";
		String[] params = new String[] { knowledgeId };
		Cursor cursor = db.rawQuery(sql, params);
		if (cursor.getCount() == 0) {
			cursor.close();
			dbManager.closeDatabase();
			return null;
		}
		ArrayList<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while (cursor.moveToNext()) {
			ExamQuestion q = new ExamQuestion(cursor.getString(0),
					cursor.getString(1), cursor.getString(2),
					cursor.getString(3), cursor.getString(4),
					cursor.getString(5), cursor.getString(6),
					cursor.getString(7), cursor.getString(8), cursor.getInt(9),
					cursor.getInt(10));
			q.setRandNum(cursor.getInt(11));
			q.setMaterial(cursor.getString(12));
			list.add(q);
		}
		cursor.close();
		dbManager.closeDatabase();
		return list;
	}

	// 更新题库数据
	public void updateDataBase(List<Paper> list, List<ExamRule> list2,
			List<ExamQuestion> list3) {
		SQLiteDatabase db = dbManager.openDatabase();
		db.beginTransaction();
		try {
			if (list != null && list.size() > 0) {
				String paperids = "";
				for (Paper paper : list) {
					Cursor cursor = db.rawQuery(
							"select * from ExamPaperTab where paperid = ?",
							new String[] { paper.getPaperId() });
					if (cursor.getCount() > 0) {
						cursor.close();
						continue;
					}
					cursor.close();
					// PAPERID ,PAPERNAME ,PAPERTIME ,PAPERSCORE ,PAPERYEAR
					// ,CLASSID
					String sql = "insert into ExamPaperTab(paperid,papername,addtime,paperscore,papertime,year,clicknum,classid,totalnum)values(?,?,?,?,?,?,?,?,?)";
					Object[] params = new Object[] { paper.getPaperId(),
							paper.getPaperName(), paper.getAddDate(),
							paper.getPaperSorce(), paper.getPaperTime(),
							paper.getYear(), paper.getClickNum(),
							paper.getClassId(), paper.getTotalNum() };
					db.execSQL(sql, params);
					paperids = paperids + paper.getPaperId() + ",";
				}
				db.execSQL(
						"delete from ExamRuleTab where paperid in ("
								+ ("".equals(paperids) ? 0 : paperids + "0")
								+ ")", new Object[] {});
				for (ExamRule r : list2) {
					// RULEID TEXT,PAPERID TEXT,RULETITLE TEXT,RULETITLEINFO
					// TEXT,RULETYPE TEXT,QUESTIONNUM INTEGER,SCOREFOREACH
					// FLOAT,SCORESET TEXT,ORDERINPAPER INTEGER
					db.execSQL(
							"insert into ExamRuleTab(ruleid,paperid,ruletitle,ruletitleinfo,ruletype,questionnum,scoreforeach,scoreset,orderinpaper,containQids)values(?,?,?,?,?,?,?,?,?,?)",
							new Object[] { r.getRuleId(), r.getPaperId(),
									r.getRuleTitle(), r.getFullTitle(),
									r.getRuleType(), r.getQuestionNum(),
									r.getScoreForEach(), r.getScoreSet(),
									r.getOrderInPaper(), r.getContainQids() });
				}
			}
			ArrayList<ExamQuestion> real = new ArrayList<ExamQuestion>();
			System.out.println("list3 size = " + list3.size());
			if (list3 != null && list3.size() > 0) // 插入题目
			{
				for (ExamQuestion q : list3) {
					// QID ,PAPERID ,EXAMID ,CONTENT ,ANSWER ,ANALYSIS ,QTYPE
					// ,OPTIONNUM ,ORDERID ,LINKQID
					// QID TEXT,RULEID TEXT,KNOWLEDGEID TEXT,CLASSID
					// TEXT,CONTENT TEXT,ANSWER TEXT,ANALYSIS TEXT,QTYPE
					// TEXT,OPTIONNUM INTEGER,ORDERID INTEGER,LINKQID TEXT
					Cursor cursor = db.rawQuery(
							"select qid from ExamQuestionTab where qid = ?",
							new String[] { q.getQid() });
					if (cursor.getCount() > 0) {
						cursor.close();
						continue;
					}
					real.add(q);
					cursor.close();
					db.execSQL(
							"insert into ExamQuestionTab(qid,ruleid,knowledgeid,classid,content,answer,analysis,qtype,optionnum,orderid,linkqid,randnum,material)values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
							new Object[] { q.getQid(), q.getRuleId(),
									q.getKnowledgeId(), q.getClassId(),
									q.getContent(), q.getAnswer(),
									q.getAnalysis(), q.getQType(),
									q.getOptionNum(), q.getOrderId(),
									q.getLinkQid(), q.getRandNum(),q.getMaterial() });
				}
				list3.clear();
				// 找节下的知识点,试题分类
				Cursor c_cursor = db
						.rawQuery(
								"select chapterid,classid,chaptertitle from ChapterTab where chapterpid <> 0",
								new String[] {});
				while (c_cursor.moveToNext()) {
					String zuheid = c_cursor.getString(1) + "_"
							+ c_cursor.getString(0);
					Cursor k_cursor = db
							.rawQuery(
									"select k.knowledgeid from KnowledgeTab k where k.chapterid = ?",
									new String[] { c_cursor.getString(0) });
					StringBuilder kids = new StringBuilder(",");
					while (k_cursor.moveToNext()) {
						kids.append(k_cursor.getString(0)).append(",");
					}
					k_cursor.close();
					getRules(db, zuheid, c_cursor.getString(2), real,
							kids.toString());
				}
				c_cursor.close();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		dbManager.closeDatabase();
	}

	private void getRules(SQLiteDatabase db, String zuheid, String name,
			List<ExamQuestion> qList, String kids) {
		StringBuffer buf = new StringBuffer();
		// 按题型的1,2,3,4来分
		for (int i = 1; i < 5; i++) {
			int count = 0;
			for (ExamQuestion q : qList) {

				if (String.valueOf(i).equals(q.getQType())
						&& (kids.indexOf("," + q.getKnowledgeId() + ",") != -1)) {
					buf.append(q.getQid()).append(",");
					count++;
				}
			}
			if (buf.length() > 0) {
				String ruleid = zuheid + "_" + i;
				String containQids = buf.substring(0, buf.lastIndexOf(","));
				int num = containQids.split(",").length;
				// 添加rule的增或者改
				Cursor cursor = db
						.rawQuery(
								"select questionnum,containqids from ExamRuleTab where ruleid = ?",
								new String[] { ruleid });
				if (cursor.getCount() == 0) {
					db.execSQL(
							"insert into ExamRuleTab(ruleid,ruletitle,paperid,questionnum,orderinpaper,containqids)values(?,?,?,?,?,?)",
							new Object[] { ruleid, (getTitleStr(i)), zuheid,
									num, i, containQids });
				} else {
					cursor.moveToNext();
					db.execSQL(
							"update ExamRuleTab set questionnum=?,containqids=? where ruleid = ?",
							new Object[] { (cursor.getInt(0) + num),
									(cursor.getString(1) + "," + containQids),
									ruleid });
				}
				buf.delete(0, buf.length());
				cursor.close();

				Cursor cursor2 = db
						.rawQuery(
								"select paperid,paperscore from ExamPaperTab where paperid = ?",
								new String[] { zuheid });
				if (cursor2.getCount() == 0) {
					db.execSQL(
							"insert into ExamPaperTab(paperid,papername,paperscore,papertime,addtime)values(?,?,?,?,datetime('now','localtime'))",
							new Object[] { zuheid, name, count, -1 });
				} else {
					cursor2.moveToNext();
					db.execSQL(
							"update ExamPaperTab set paperscore=?,addtime=datetime('now','localtime') where paperid = ?",
							new Object[] { (cursor2.getInt(1) + count), zuheid });
				}
				cursor2.close();
			}
		}
	}

	private String getTitleStr(int i) {
		return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
				: i == 4 ? "判断题" : "综合题";
	}

	public String findAddTime() {
		SQLiteDatabase db = dbManager.openDatabase();
		String addtime = null;
		Cursor cursor = db.rawQuery(
				"select addtime from DataAddTimeTab order by _id desc",
				new String[] {});
		if (cursor.moveToNext()) {
			addtime = cursor.getString(0);
		}
		cursor.close();
		dbManager.closeDatabase();
		return addtime;
	}
}