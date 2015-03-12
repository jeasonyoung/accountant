package com.changheng.accountant.entity;

import com.changheng.accountant.util.QuestionContentDecoder;
import com.changheng.accountant.util.StringUtils;

public class ExamQuestion extends Entity implements Comparable<ExamQuestion> {
	private long _id;
	private String qid,ruleId,classId,knowledgeId,content,answer,analysis,linkQid,qType,userAnswer;
	private int optionNum,orderId,randNum;
	private String material; //案例材料
	public int getRandNum() {
		return randNum;
	}
	public void setRandNum(int randNum) {
		this.randNum = randNum;
	}
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getQid() {
		return qid;
	}
	public void setQid(String qid) {
		this.qid = qid;
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getKnowledgeId() {
		return knowledgeId;
	}
	public void setKnowledgeId(String knowledgeId) {
		this.knowledgeId = knowledgeId;
	}
	public String getqType() {
		return qType;
	}
	public void setqType(String qType) {
		this.qType = qType;
	}
	public String getContent() {
		return content;
	}
	public String getRealContent()
	{
		String typeStr = "1".equals(qType) ? "(单选题) " : "2".equals(qType) ? "(多选题) " : "3".equals(qType) ? "(不定项) "
				: "4".equals(qType) ? "(判断题) " : "(综合题) ";
			return typeStr + QuestionContentDecoder.decodeContent(content, randNum);
	}
	
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}
	public void encode()
	{
		this.content = QuestionContentDecoder.encodeContent(content, randNum);
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getAnalysis() {
		return analysis;
	}
	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}
	public String getLinkQid() {
		return linkQid;
	}
	public void setLinkQid(String linkQid) {
		this.linkQid = linkQid;
	}
	public String getQType() {
		return qType;
	}
	public void setQType(String qType) {
		this.qType = qType;
	}
	public int getOptionNum() {
		return optionNum;
	}
	public void setOptionNum(int optionNum) {
		this.optionNum = optionNum;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public ExamQuestion(String qid,String ruleid, String knowledgeId,String classId, String content,
			String answer, String analysis, String linkQid,
			String qType, int optionNum, int orderId) {
		super();
		this.qid = qid;
		this.ruleId = ruleid;
		this.knowledgeId = knowledgeId;
		this.classId = classId;
		this.content = content;
		this.answer = answer;
		this.analysis = analysis;
		this.linkQid = linkQid;
		this.qType = qType;
		this.optionNum = optionNum;
		this.orderId = orderId;
	}
	public ExamQuestion() {
		// TODO Auto-generated constructor stub
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleid) {
		this.ruleId = ruleid;
	}
	public String getUserAnswer() {
		return userAnswer;
	}
	public void setUserAnswer(String userAnswer) {
		this.userAnswer = userAnswer;
	}
	@Override
	public int compareTo(ExamQuestion o) {
		// TODO Auto-generated method stub
		int num = qType.compareTo(o.getQType());
		if(num==0)
		{
			return qid.compareTo(o.getQid());
		}
		return num;
	}
} 
