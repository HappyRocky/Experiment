/*
 * 文件名：handleCardNumber.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月5日下午4:14:13
 */
package whaty.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class handleCardNumber {
	
	
	public static void main(String[] args) throws IOException {
		String readPath = "E:/myJava/yiaiSql/insertCardNumber_category5.sql";
		String writePath = "E:/myJava/yiaiSql/insertCardNumber_category__5.sql";
		List<String> list = MyUtils.readFile(readPath);
		List<String> newList = new ArrayList<String>();
		for (String str : list) {
			int idx = str.indexOf("VALUES('");
			if (idx >= 0) {
				String id = MyUtils.uuid();
				String newStr = str.substring(0, idx + 8) + id + "'," + str.substring(idx + 43);
				newList.add(newStr);
			}
		}
		MyUtils.outputList(newList, writePath);
		
//		for (int i = 1; i <= 10; i++) {
//			String readPath = "E:/myJava/yiaiSql/insertCardNumber_category5.sql";
//			String writePath = "E:/myJava/yiaiSql/insertCardNumber_category__5.sql";
//			List<String> list = MyUtils.readFile(readPath);
//			List<String> newList = new ArrayList<>();
//			for (String str : list) {
//				int idx = str.indexOf("VALUES('");
//				if (idx >= 0) {
//					String id = MyUtils.uuid();
//					String newStr = str.substring(0, idx + 8) + id + "'," + str.substring(idx + 43);
//					newList.add(newStr);
//				}
//			}
//			MyUtils.outputList(newList, writePath);
//		}
	}
}
