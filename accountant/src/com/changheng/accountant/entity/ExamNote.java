package com.changheng.accountant.entity;

public class ExamNote extends Entity{
	private long _id;
	private String qid,classId,content,username,paperId;
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
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public ExamNote(String qid, String addTime, String content,String username,String  classId) {
		super();
		this.qid = qid;
		this.addTime = addTime;
		this.content = content;
		this.username = username;
		this.classId = classId;
	}
	public ExamNote(String qid, String addTime, String content,String username,String  paperId,String classId) {
		super();
		this.qid = qid;
		this.addTime = addTime;
		this.content = content;
		this.username = username;
		this.paperId = paperId;
		this.classId = classId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	
}
