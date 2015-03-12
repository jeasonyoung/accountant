package com.changheng.accountant.entity;


public class User extends Base{
	private long _id;
	private int uid;
	private String username;
	private String location;
	private String jointime;
	private String nickname;
	private String truename;
	private String password;
	private String face;
	private String deviceId; //设备码
	private String limitTime;//限制时间
	private Integer restTime; //剩余时间
	public Integer getRestTime() {
		return restTime;
	}
	public void setRestTime(Integer restTime) {
		this.restTime = restTime;
	}
	private boolean access;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public int getUid() {
		return uid;
	}
	public String getLimitTime() {
		return limitTime;
	}
	public void setLimitTime(String limitTime) {
		this.limitTime = limitTime;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public User() {
		// TODO Auto-generated constructor stub
	}
	public User(int uid, String username, String password) {
		super();
		this.uid = uid;
		this.username = username;
		this.password = password;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getJointime() {
		return jointime;
	}
	public void setJointime(String jointime) {
		this.jointime = jointime;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public boolean isAccess() {
		return access;
	}
	public void setAccess(boolean access) {
		this.access = access;
	}
	public User(int uid, boolean access) {
		super();
		this.uid = uid;
		this.access = access;
	}
	public String getTruename() {
		return truename;
	}
	public void setTruename(String truename) {
		this.truename = truename;
	}
}
