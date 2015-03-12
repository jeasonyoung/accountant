package com.changheng.accountant.entity;

import com.changheng.accountant.util.StringUtils;

public class AppUpdate extends Entity{
	private String url;//下载地址
	private String content;//更新说明
	private int size;//app大小
	private int versionCode;
	private String versionName; //版本号
	private String addTime; //添加时间
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public AppUpdate(String url,String content,int size,int versionCode,String versionName) {
		// TODO Auto-generated constructor stub
		this.url = url;
		this.content = content;
		this.size = size;
		this.versionCode = versionCode;
		this.versionName = versionName;
	}
	public AppUpdate() {
		// TODO Auto-generated constructor stub
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public boolean isNeedUpdate(int code)
	{
		return code < versionCode;	//当前版本号 小于 服务器上的版本号
	}
	public boolean isDataNeedUpdate(String addtime)
	{
		return StringUtils.compareDate(addTime,addtime);
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "最新版本："+versionName+"\n"+
				"大小："+ ((int)(size/1024.0*100))/100.0 +"MB \n"+
					"更新内容：\n"+content;
	}
}
