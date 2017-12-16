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
		String str = URLDecoder.decode("https%3A%2F%2Fchong.qq.com%2Fphp%2Findex.php%3Fd%3D%26c%3DwxAdapter%26m%3DmobileDeal%26showwxpaytitle%3D1%26vb2ctag%3D4_2030_5_1194_60", "GBK");
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

