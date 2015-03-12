package com.changheng.accountant.entity;

public class Plan extends Entity{
	
	private int id,daysId;
	private String classId ,summaryContent,containsKid,containsPaperId,areaId;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getDaysId() {
		return daysId;
	}
	public void setDaysId(int daysId) {
		this.daysId = daysId;
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getSummaryContent() {
		return summaryContent;
	}
	public void setSummaryContent(String summaryContent) {
		this.summaryContent = summaryContent;
	}
	public String getContainsKid() {
		return containsKid;
	}
	public void setContainsKid(String containsKid) {
		this.containsKid = containsKid;
	}
	public String getContainsPaperId() {
		return containsPaperId;
	}
	public void setContainsPaperId(String containsPaperId) {
		this.containsPaperId = containsPaperId;
	}
	public String getAreaId() {
		return areaId;
	}
	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}
}
