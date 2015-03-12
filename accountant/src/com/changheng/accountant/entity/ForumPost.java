package com.changheng.accountant.entity;

import java.io.File;

/**
 * 论坛帖子实体类
 * @author Administrator
 *
 */
public class ForumPost {
	private String id;
	private String title;
	private String url;
	private String face;
	private String body;
	private String author;
	private String area;
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	private int authorId;
	private int answerCount;
	private int viewCount;
	private String pubDate;
	private int catalog;
	private int isNoticeMe;	
	private int favorite;
	private File imageFile;
	public File getImageFile() {
		return imageFile;
	}
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getAuthorId() {
		return authorId;
	}
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	public int getAnswerCount() {
		return answerCount;
	}
	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public int getCatalog() {
		return catalog;
	}
	public void setCatalog(int catalog) {
		this.catalog = catalog;
	}
	public int getIsNoticeMe() {
		return isNoticeMe;
	}
	public void setIsNoticeMe(int isNoticeMe) {
		this.isNoticeMe = isNoticeMe;
	}
	public int getFavorite() {
		return favorite;
	}
	public void setFavorite(int favorite) {
		this.favorite = favorite;
	}
	
}
