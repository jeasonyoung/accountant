package com.changheng.accountant.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import com.changheng.accountant.entity.AppUpdate;

import android.util.Xml;

public class XmlCreator {
	/*
	 * 生成xml格式字符串 
	 * <cyedu> 
	 * 	<update> 
	 * 		<wp7>1.0</wp7> 
	 * 		<ios>1.0</ios> 
	 * 		<android>
	 * 			<versionCode>2</versionCode> 
	 * 			<versionName>1.1</versionName>
	 * 			<!-- 数据包大小kb--> 
	 * 			<size>3</size>
	 * 			<!-- 数据包下载地址 -->
	 * 			<downloadUrl>http://www.cyedu.org/UserCenter/mobile/update_data46.zip</downloadUrl> 
	 * 			<addtime>2014-02-12 00:00:00</addtime> 
	 * 		</android> 
	 * 	</update> 
	 * </cyedu>
	 */
	public static String WriteXmlStr(AppUpdate update) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "cyedu");
				serializer.startTag("", "update");
					serializer.startTag("", "wp7");
						serializer.text("1.0");
					serializer.endTag("", "wp7");
					serializer.startTag("", "ios");
						serializer.text("1.0");
					serializer.endTag("", "ios");
					serializer.startTag("", "android");
						serializer.startTag("", "versionCode");
							serializer.text(update.getVersionCode()+"");
						serializer.endTag("", "versionCode");
						serializer.startTag("", "versionName");
							serializer.text(update.getVersionName());
						serializer.endTag("", "versionName");
						serializer.startTag("", "size");
							serializer.text(update.getSize()+"");
						serializer.endTag("", "size");
						serializer.startTag("", "downloadUrl");
							serializer.text(update.getUrl());
						serializer.endTag("", "downloadUrl");
						serializer.startTag("", "addtime");
							serializer.text(update.getAddTime());
						serializer.endTag("", "addtime");
					serializer.endTag("", "android");
				serializer.endTag("", "update");
			serializer.endTag("", "cyedu");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* 将字符串写入私有文件夹下 文件存放在data/data/package/files */
	public static void WriteFileData(String filePath, String fileName, String message) {
		try {
			File f = new File(filePath);
			if(f.exists())
			{
				f.mkdirs();
			}
			FileOutputStream fout = new FileOutputStream(filePath+fileName);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
