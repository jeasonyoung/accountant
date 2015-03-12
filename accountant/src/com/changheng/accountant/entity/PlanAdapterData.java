package com.changheng.accountant.entity;

import java.util.List;

public class PlanAdapterData extends Entity{
	
	private boolean isToday;
	private String title,subTitle,content,containsKid,containsPid;
	private List<Knowledge> kList;
	public List<Knowledge> getKList() {
		return kList;
	}
	public void setKList(List<Knowledge> kList) {
		this.kList = kList;
	}
	public String getContainsKid() {
		return containsKid;
	}
	public void setContainsKid(String containsKid) {
		this.containsKid = containsKid;
	}
	public String getContainsPid() {
		return containsPid;
	}
	public void setContainsPid(String containsPid) {
		this.containsPid = containsPid;
	}
	private String week;
	private String date;
	private String ksDay;
	public boolean isToday() {
		return isToday;
	}
	public void setToday(boolean isToday) {
		this.isToday = isToday;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getWeek() {
		return week;
	}
	public void setWeek(String week) {
		this.week = week;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getKsDay() {
		return ksDay;
	}
	public void setKsDay(String ksDay) {
		this.ksDay = ksDay;
	}
	public PlanAdapterData(boolean isToday, String title, String subTitle,
			String content, String week, String date, String ksDay) {
		super();
		this.isToday = isToday;
		this.title = title;
		this.subTitle = subTitle;
		this.content = content;
		this.week = week;
		this.date = date;
		this.ksDay = ksDay;
	}
	public PlanAdapterData(String summaryContent,String containsKid,String containsPid)
	{
		this.setContent(summaryContent);
		this.setContainsKid(containsKid);
		this.setContainsPid(containsPid);
	}
	public PlanAdapterData() {
		// TODO Auto-generated constructor stub
	}
//	public PlanAdapterData(String title, String subTitle, String content) {
//		super();
//		this.title = title;
//		this.subTitle = subTitle;
//		this.content = content;
//	}
	
}	
