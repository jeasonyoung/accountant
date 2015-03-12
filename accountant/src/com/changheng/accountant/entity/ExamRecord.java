package com.changheng.accountant.entity;

public class ExamRecord {
	private long _id;
	private String examId,paperId,username,answers,tempAnswer,lastTime,isDone,papername;
	private double score;
	private int useTime,tempTime,papertime,paperscore;
	private int mode,errorNum;
	private int hasDone;
	private int isSync;
	public int getIsSync() {
		return isSync;
	}
	public void setIsSync(int isSync) {
		this.isSync = isSync;
	}
	public int getHasDone() {
		try{
		return tempAnswer.split("&").length;
		}catch(Exception e)
		{
			return 0;
		}
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public int getErrorNum() {
		return errorNum;
	}
	public void setErrorNum(int errorNum) {
		this.errorNum = errorNum;
	}
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getExamId() {
		return examId;
	}
	public void setExamId(String examId) {
		this.examId = examId;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAnswers() {
		return answers;
	}
	public void setAnswers(String answers) {
		this.answers = answers;
	}
	public String getTempAnswer() {
		return tempAnswer;
	}
	public void setTempAnswer(String tempAnswer) {
		this.tempAnswer = tempAnswer;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getUseTime() {
		return useTime;
	}
	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}
	public int getTempTime() {
		return tempTime;
	}
	public void setTempTime(int tempTime) {
		this.tempTime = tempTime;
	}
	
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	
	public String getIsDone() {
		return isDone;
	}
	public void setIsDone(String isDone) {
		this.isDone = isDone;
	}
	public ExamRecord(String paperId,
			String username, String answers, String tempAnswer, double score,
			int useTime, int tempTime,String lastTime,String isDone) {
		super();
		this.paperId = paperId;
		this.username = username;
		this.answers = answers;
		this.tempAnswer = tempAnswer;
		this.score = score;
		this.useTime = useTime;
		this.tempTime = tempTime;
		this.lastTime = lastTime;
		this.isDone = isDone;
	}
	public ExamRecord() {
		// TODO Auto-generated constructor stub
	}
	public ExamRecord(String paperId, String username) {
		super();
		this.paperId = paperId;
		this.username = username;
	}
	public String getPapername() {
		return (mode==1?"[章节练习] ":"[模拟考试] ")+papername;
	}
	public String getPapername2() {
		return papername;
	}
	public void setPapername(String papername) {
		this.papername = papername;
	}
	public int getPapertime() {
		return papertime;
	}
	public void setPapertime(int papertime) {
		this.papertime = papertime;
	}
	public int getPaperscore() {
		return paperscore;
	}
	public void setPaperscore(int paperscore) {
		this.paperscore = paperscore;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(answers != null)
		return paperId+"."+score+"."+answers.replaceAll("&", "Q");
		return paperId+"."+score+".";
	}
}
