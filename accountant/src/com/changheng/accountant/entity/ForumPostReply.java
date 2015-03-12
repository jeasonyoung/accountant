package com.changheng.accountant.entity;

public class ForumPostReply extends Entity{
	private int id;
	private String face;
	private String replyAuthor;
	private String authorId;
	private String content;
	private String location;
	private String replyDate;
	private String replyTo;//回复谁
	private String postId;
	public String getPostId() {
		return postId;
	}
	public void setPostId(String postId) {
		this.postId = postId;
	}
	private int floor;
	private int replyFloor; //回复第几楼
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getReplyAuthor() {
		return replyAuthor;
	}
	public void setReplyAuthor(String replyAuthor) {
		this.replyAuthor = replyAuthor;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getReplyDate() {
		return replyDate;
	}
	public void setReplyDate(String replyDate) {
		this.replyDate = replyDate;
	}
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public String getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		this.floor = floor;
	}
	public int getReplyFloor() {
		return replyFloor;
	}
	public void setReplyFloor(int replyFloor) {
		this.replyFloor = replyFloor;
	}
	
}