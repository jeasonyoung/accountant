package com.changheng.accountant.entity;

public class ExamErrorQuestion extends Entity{
	private long _id;
	private String qid,username,examId,paperId,lastAnswer;
	private int status;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getLastAnswer() {
		return lastAnswer;
	}
	public void setLastAnswer(String lastAnswer) {
		this.lastAnswer = lastAnswer;
	}
	private String addTime;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getQid() {
		return qid;
	}
	public void setQid(String qid) {
		this.qid = qid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public ExamErrorQuestion(String qid, String username,String paperId,String answer) {
		super();
		this.qid = qid;
		this.username = username;
		this.paperId = paperId;
		this.lastAnswer = answer;
	}
	
	public String getExamId() {
		return examId;
	}
	public void setExamId(String examId) {
		this.examId = examId;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public ExamErrorQuestion() {
		// TODO Auto-generated constructor stub
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
}
