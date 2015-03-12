package com.changheng.accountant.entity;

public class Notice extends Entity {
	private String postId;
	public String getPostId() {
		return postId;
	}
	public void setPostId(String postId) {
		this.postId = postId;
	}
	private	String title;
	private int newReplyCount;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getNewReplyCount() {
		return newReplyCount;
	}
	public void setNewReplyCount(int newReplyCount) {
		this.newReplyCount = newReplyCount;
	}
	
}
