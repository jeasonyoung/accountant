package com.changheng.accountant.entity;

import java.util.ArrayList;

public class KnowledgeList extends Entity{
	private Paper paper;
	private ArrayList<Knowledge> kList = new ArrayList<Knowledge>();
	public Paper getPaper() {
		return paper;
	}
	public void setPaper(Paper paper) {
		this.paper = paper;
	}
	public ArrayList<Knowledge> getKList() {
		return kList;
	}
	public void setKList(ArrayList<Knowledge> kList) {
		this.kList = kList;
	}
}
