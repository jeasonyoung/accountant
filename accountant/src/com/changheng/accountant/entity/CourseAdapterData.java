package com.changheng.accountant.entity;

public class CourseAdapterData extends Entity{
	private String[] courses;
	private String[][] chapters;
	public String[] getCourses() {
		return courses;
	}
	public void setCourses(String[] courses) {
		this.courses = courses;
	}
	public String[][] getChapters() {
		return chapters;
	}
	public void setChapters(String[][] chapters) {
		this.chapters = chapters;
	}
	public CourseAdapterData() {
		// TODO Auto-generated constructor stub
	}
	public CourseAdapterData(String[] courses,String[][] chapters) {
		this.courses = courses;
		this.chapters = chapters;
	}
}
