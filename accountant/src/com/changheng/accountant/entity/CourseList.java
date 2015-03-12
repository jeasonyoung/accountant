package com.changheng.accountant.entity;

import java.util.ArrayList;

public class CourseList extends Entity{
	private ArrayList<Chapter> chapterList = new ArrayList<Chapter>();
	private ArrayList<Course> classList = new ArrayList<Course>();
	public ArrayList<Chapter> getChapterList() {
		return chapterList;
	}
	public void setChapterList(ArrayList<Chapter> chapterList) {
		this.chapterList = chapterList;
	}
	public ArrayList<Course> getClassList() {
		return classList;
	}
	public void setClassList(ArrayList<Course> classList) {
		this.classList = classList;
	}
	
}
