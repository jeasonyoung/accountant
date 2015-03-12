package com.changheng.accountant.entity;

public class Chapter extends Entity{
	private int _id;
	private String title;
	private String content;
	private String chapterId;
	private String classId;
	private int orderId;
	private String pid;
	private int questionCount;
	public int getQuestionCount() {
		return questionCount;
	}
	public void setQuestionCount(int questionCount) {
		this.questionCount = questionCount;
	}
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getChapterId() {
		return chapterId;
	}
	public void setChapterId(String chapterId) {
		this.chapterId = chapterId;
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public Chapter( String chapterId,String title, String content,
			String classId, String pid, int orderId) {
		super();
		this.title = title;
		this.content = content;
		this.chapterId = chapterId;
		this.classId = classId;
		this.orderId = orderId;
		this.pid = pid;
	}
	public Chapter() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if("0".equals(pid))
		{
			if(this.title.length()>9)
			{
				return "第 "+orderId+" 章 \n"+this.title;
			}else
			{
				return "第 "+orderId+" 章 "+this.title;
			}
		}else
		{
			if(this.title.length()>9)
			{
				return "第 "+orderId+" 节 \n"+this.title;
			}else
			{
				return "第 "+orderId+" 节 "+this.title;
			}
		}
	}
}
