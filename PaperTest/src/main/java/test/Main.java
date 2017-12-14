package test;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import utils.FileUtils;


/** 
 * @className:Main.java
 * @classDescription:
 * @author:yourname
 * @createTime:2017年9月4日
 */
public class Main {
	public static void main(String[] args) throws Exception {
		String str = URLDecoder.decode("%26", "GBK");
		  System.out.println(str);
		System.exit(0);
	}
	
	public static String toSql(String str){
		if (StringUtils.isBlank(str)) {
			return str;
		}
		str = str.replaceAll("'", "\\\\'").replaceAll("#", "\\\\#").replaceAll("-", "\\\\-");
		if (str.endsWith("\\")) {
			str = str + " ";
		}
		return str;
	}
}

