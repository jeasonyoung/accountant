package com.changheng.accountant.entity;
/**
 * 解析的结果
 * @author Administrator
 *
 */
public class ParseResult extends Entity{
	public final static int EMPTY_JSON_CODE = -2;
	public final static int BAD_JSON_CODE = -3;
	public final static String EMPTY_JSON_MSG = "返回结果为空";
	public final static String BAD_JSON_MSG = "解析错误";
	private int errorCode;
	private String errorMsg;
	public Object obj;
	public boolean Ok()
	{
		return errorCode == 1;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public Object getObj() {
		if(!Ok())
		{
			return null;
		}
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public void setEmptyResult()
	{
		this.errorCode = ParseResult.EMPTY_JSON_CODE;
		this.errorMsg = ParseResult.EMPTY_JSON_MSG;
	}
	public void setBadResult()
	{
		this.errorCode = ParseResult.BAD_JSON_CODE;
		this.errorMsg = ParseResult.BAD_JSON_MSG;
	}
	public void setResult(int code,String msg)
	{
		this.errorCode = code;
		this.errorMsg = msg;
	}
	public void setResult(int code,String msg,Object obj)
	{
		this.errorCode = code;
		this.errorMsg = msg;
		this.obj = obj;
	}
}
