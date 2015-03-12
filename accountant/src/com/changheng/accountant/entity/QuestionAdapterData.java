package com.changheng.accountant.entity;

public class QuestionAdapterData extends Entity{
	private String courseId;
	private String title;
	private int count;
	
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public QuestionAdapterData(String courseId,String title, int count) {
		super();
		this.courseId = courseId;
		this.title = title;
		this.count = count;
	}
	public QuestionAdapterData() {
		// TODO Auto-generated constructor stub
	}
}
