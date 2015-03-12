package com.changheng.accountant.entity;

import java.util.ArrayList;

public class SyncData extends Entity{

	private ArrayList<ExamFavor> favorList;
	private ArrayList<ExamErrorQuestion> errorList;
	private ArrayList<ExamQuestion> questionList;
	private ArrayList<Paper> paperList;
	private ArrayList<ExamRecord> recordList;
	private String favorNeedDeleteQids;
	private String errorNeedDeleteQids;
	private String username;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFavorNeedDeleteQids() {
		return favorNeedDeleteQids;
	}
	public void setFavorNeedDeleteQids(String favorNeedDeleteQids) {
		this.favorNeedDeleteQids = favorNeedDeleteQids;
	}
	public String getErrorNeedDeleteQids() {
		return errorNeedDeleteQids;
	}
	public void setErrorNeedDeleteQids(String errorNeedDeleteQids) {
		this.errorNeedDeleteQids = errorNeedDeleteQids;
	}
	public ArrayList<ExamFavor> getFavorList() {
		return favorList;
	}
	public void setFavorList(ArrayList<ExamFavor> favorList) {
		this.favorList = favorList;
	}
	public ArrayList<ExamErrorQuestion> getErrorList() {
		return errorList;
	}
	public void setErrorList(ArrayList<ExamErrorQuestion> errorList) {
		this.errorList = errorList;
	}
	public ArrayList<ExamQuestion> getQuestionList() {
		return questionList;
	}
	public void setQuestionList(ArrayList<ExamQuestion> questionList) {
		this.questionList = questionList;
	}
	public ArrayList<Paper> getPaperList() {
		return paperList;
	}
	public void setPaperList(ArrayList<Paper> paperList) {
		this.paperList = paperList;
	}
	public ArrayList<ExamRecord> getRecordList() {
		return recordList;
	}
	public void setRecordList(ArrayList<ExamRecord> recordList) {
		this.recordList = recordList;
	}
}
