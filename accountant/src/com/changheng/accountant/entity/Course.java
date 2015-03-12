package com.changheng.accountant.entity;


public class Course extends Entity{
	private long _id;
	private String courseId;
	private String courseName;
	private String coursePid;	//父ID
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public String getCoursePid() {
		return coursePid;
	}
	public void setCoursePid(String coursePid) {
		this.coursePid = coursePid;
	}
	public Course() {
		// TODO Auto-generated constructor stub
	}
	public Course(String courseId, String courseName, String coursePid) {
		super();
		this.courseId = courseId;
		this.courseName = courseName;
		this.coursePid = coursePid;
	}
	public Course(String courseId, String courseName)
	{
		super();
		this.courseId = courseId;
		this.courseName = courseName;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.courseName;
	}
}
