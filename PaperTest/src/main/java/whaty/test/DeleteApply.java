/*
 * 文件名：DeleteApply.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月9日上午10:58:02
 */
package whaty.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class DeleteApply {
	public static void deleteApply(){
		String loginIdPath = "E:/myJava/yiaiSql/20170608/compareApplyInfo_diff_all.txt";
		List<String> lineList = MyUtils.readFile(loginIdPath);
		List<String> loginIdList = new ArrayList<String>();
		for (int i = 0; i < lineList.size(); i++) {
			String line = lineList.get(i);
			loginIdList.add(line.substring(0,line.indexOf(" ")).trim());
		}
		String loginIdCondition = MyUtils.list2Str(loginIdList, "','");
		System.out.println(loginIdCondition);
		
		
	}
	
	public static void main(String[] args) {
		deleteApply();
	}
}
