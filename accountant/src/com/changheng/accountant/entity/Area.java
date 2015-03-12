package com.changheng.accountant.entity;

public class Area extends Entity {
	private int id;
	private String areaCName;
	private String areaEName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAreaCName() {
		return areaCName;
	}

	public void setAreaCName(String areaCName) {
		this.areaCName = areaCName;
	}

	public String getAreaEName() {
		return areaEName;
	}

	public void setAreaEName(String areaEName) {
		this.areaEName = areaEName;
	}

	public Area(int id, String areaCName, String areaEName) {
		super();
		this.id = id;
		this.areaCName = areaCName;
		this.areaEName = areaEName;
	}

	public Area() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "public static final int areaCode = " + id + ";\n"
				+ "public static final String areaCName = \"" + areaCName
				+ "\";\n" + "public static final String areaEName = \""
				+ areaEName + "\";\n";
	}

}
