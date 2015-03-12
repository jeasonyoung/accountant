package com.changheng.accountant.entity;

import java.util.ArrayList;
import java.util.List;


public class NewsList extends Entity{
	private int pageSize;
	private int newsCount;
	private List<Info> newslist = new ArrayList<Info>();
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getNewsCount() {
		return newsCount;
	}
	public void setNewsCount(int newsCount) {
		this.newsCount = newsCount;
	}
	public List<Info> getNewslist() {
		return newslist;
	}
	public void setNewslist(List<Info> newslist) {
		this.newslist = newslist;
	}
	
	
}
