package com.changheng.accountant.entity;

import java.util.ArrayList;

public class Paper extends Entity{
	private long _id;
	private String paperId;
	private String paperName;
	private int paperSorce;
	private int paperTime;
	private String addDate;
	private int clickNum;
	private int year;
	private String price;
	private int totalNum;	//总题数
	public int getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public int getClickNum() {
		return clickNum;
	}
	public void setClickNum(int clickNum) {
		this.clickNum = clickNum;
	}
	public String getAddDate() {
		return addDate;
	}
	public void setAddDate(String addDate) {
		this.addDate = addDate;
	}
	private String courseId,classId;
	private String jsonString;
	private ArrayList<ExamRule> examRules = new ArrayList<ExamRule>();
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getPaperName() {
		return paperName;
	}
	public void setPaperName(String paperName) {
		this.paperName = paperName;
	}
	public int getPaperSorce() {
		return paperSorce;
	}
	public void setPaperSorce(int paperSorce) {
		this.paperSorce = paperSorce;
	}
	public int getPaperTime() {
		return paperTime;
	}
	public void setPaperTime(int paperTime) {
		this.paperTime = paperTime;
	}
	
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public Paper(String paperId, String paperName, int paperSorce, int paperTime,String courseId,String examId,int totalNum) {
		super();
		this.paperId = paperId;
		this.paperName = paperName;
		this.paperSorce = paperSorce;
		this.paperTime = paperTime;
		this.courseId = courseId;
		this.classId = examId;
		this.totalNum = totalNum;
	}
	//paperid,papername,adddate,paperscore,papertime,year,clicknum,classid
	public Paper(String paperId, String paperName,String addDate, int paperSorce, int paperTime,int year,int clicknum,int price,String examId,int totalNum) {
		super();
		this.paperId = paperId;
		this.paperName = paperName;
		this.addDate = addDate;
		this.paperSorce = paperSorce;
		this.paperTime = paperTime;
		this.price = price+"";
		this.year = year;
		this.clickNum = clicknum;
		this.classId = examId;
		this.totalNum = totalNum;
	}
	public Paper() {
		// TODO Auto-generated constructor stub
	}
	public Paper(String paperid, String papername, int score) {
		// TODO Auto-generated constructor stub
		this.paperId = paperid;
		this.paperName = papername;
		this.paperSorce = score;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public ArrayList<ExamRule> getRuleList() {
		return examRules;
	}
	public void setRuleList(ArrayList<ExamRule> ruleList) {
		this.examRules = ruleList;
	}
	public String getJsonString() {
		return jsonString;
	}
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return paperName;
	}
}
