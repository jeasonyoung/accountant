package com.changheng.accountant.entity;

public class Knowledge extends Entity{
	private int _id;
	private String knowledgeId;
	private String classId;
	private String chapterId;
	private String topic;
	private String title;
	private String summaryContent;
	private String fullContent;
	private int orderId;
	private int questionCount;
	public int getQuestionCount() {
		return questionCount;
	}
	public void setQuestionCount(int questionCount) {
		this.questionCount = questionCount;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public String getFullContent() {
		return fullContent;
	}
	public void setFullContent(String fullContent) {
		this.fullContent = fullContent;
	}
	private String url;
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getKnowledgeId() {
		return knowledgeId;
	}
	public void setKnowledgeId(String knowledgeId) {
		this.knowledgeId = knowledgeId;
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getChapterId() {
		return chapterId;
	}
	public void setChapterId(String chapterId) {
		this.chapterId = chapterId;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSummaryContent() {
		return summaryContent;
	}
	public void setSummaryContent(String summaryContent) {
		this.summaryContent = summaryContent;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Knowledge(String knowledgeId, String classId, String chapterId,
			String topic, String title, String summaryContent,
			String fullContent, int orderId) {
		super();
		this.knowledgeId = knowledgeId;
		this.classId = classId;
		this.chapterId = chapterId;
		this.topic = topic;
		this.title = title;
		this.summaryContent = summaryContent;
		this.fullContent = fullContent;
		this.orderId = orderId;
	}
	public Knowledge() {
		// TODO Auto-generated constructor stub
	}
	public Knowledge(String knowledgeId, String chapterId, String title,
			String fullContent, int orderId) {
		super();
		this.knowledgeId = knowledgeId;
		this.chapterId = chapterId;
		this.title = title;
		this.fullContent = fullContent;
		this.orderId = orderId;
	}
	public String getFullContentWithTitle() {
		// TODO Auto-generated method stub
		if(fullContent == null)
			return null;
		return "<P style='color:blue;font-size:20px'>"+title+"</Font></P>"+fullContent;
	}
	
}
