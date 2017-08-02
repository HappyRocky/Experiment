/*
 * 文件名：selectLines.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年7月3日下午4:43:38
 */
package whaty.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class selectLines {
	
	/**
	 * 从一个list中挑选出含有某些子字符串的字符串，包含任一子字符串即可
	 * @param lineList 待挑选的list
	 * @param containsStrs 子字符串list
	 * @return
	 */
	public static List<String> selectLinesByContain(List<String> lineList, List<String> containsStrs){
		List<String> result = new ArrayList<>();
		for (String line : lineList) {
			for (String str : containsStrs) {
				if (line.contains(str)) {
					result.add(line);
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		String path = "E:/myJava/yiaiSql/all.sql";
		String outPath = "E:/myJava/yiaiSql/all_out.sql";
		List<String> lineList = MyUtils.readFile(path);
		List<String> containsStrs = new ArrayList<>();
		containsStrs.add("INSERT INTO `pr_trainee_class_course_package`");
		containsStrs.add("INSERT INTO pr_student_certificate");
		List<String> result = selectLinesByContain(lineList, containsStrs);
		MyUtils.outputList(result, outPath);
	}
}
