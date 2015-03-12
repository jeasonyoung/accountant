package com.changheng.accountant.entity;

import java.util.ArrayList;

public class PaperList extends Entity{
	private int pagesize;
	private int paperCount;
	private ArrayList<Paper> paperlist = new ArrayList<Paper>();
	public int getPagesize() {
		return pagesize;
	}
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}
	public int getPaperCount() {
		return paperCount;
	}
	public void setPaperCount(int paperCount) {
		this.paperCount = paperCount;
	}
	public ArrayList<Paper> getPaperlist() {
		return paperlist;
	}
	public void setPaperlist(ArrayList<Paper> paperlist) {
		this.paperlist = paperlist;
	}
}
