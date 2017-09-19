package test;

import java.util.HashMap;
import java.util.Map;

import utils.FileUtils;


/** 
 * @className:Main.java
 * @classDescription:
 * @author:yourname
 * @createTime:2017年9月4日
 */
public class Main {
	public static void main(String[] args) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("srcExambatch", "1");
		paramsMap.put("targetExambatch", "2");
		paramsMap.put("token", "3");		
		System.out.println("" + paramsMap.toString());
		System.exit(0);
	}
}

