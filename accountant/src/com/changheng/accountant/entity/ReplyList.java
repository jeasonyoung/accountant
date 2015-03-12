package com.changheng.accountant.entity;

import java.util.ArrayList;
import java.util.List;

public class ReplyList extends Entity{
	private int pageSize;
	private int commentCount;
	private List<ForumPostReply> replyList = new ArrayList<ForumPostReply>();
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	public List<ForumPostReply> getReplyList() {
		return replyList;
	}
	public void setReplyList(List<ForumPostReply> replyList) {
		this.replyList = replyList;
	}
	
}
