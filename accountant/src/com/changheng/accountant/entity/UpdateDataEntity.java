package com.changheng.accountant.entity;

import java.util.ArrayList;
import java.util.List;

public class UpdateDataEntity extends Entity{
	private String dataAddTime;
	public String getDataAddTime() {
		return dataAddTime;
	}
	public void setDataAddTime(String dataAddTime) {
		this.dataAddTime = dataAddTime;
	}
	private List<Paper> paperList = new ArrayList<Paper>();
	private List<ExamQuestion> qList = new ArrayList<ExamQuestion>();
	private List<ExamRule> ruleList = new ArrayList<ExamRule>();
	public List<Paper> getPaperList() {
		return paperList;
	}
	public void setPaperList(List<Paper> paperList) {
		this.paperList = paperList;
	}
	public List<ExamQuestion> getQList() {
		return qList;
	}
	public void setQList(List<ExamQuestion> qList) {
		this.qList = qList;
	}
	public List<ExamRule> getRuleList() {
		return ruleList;
	}
	public void setRuleList(List<ExamRule> ruleList) {
		this.ruleList = ruleList;
	}
}
