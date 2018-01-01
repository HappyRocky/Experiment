/*
 * 文件名：HandleInsertAndUpdate.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月6日上午2:42:57
 */
package whaty.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class HandleInsertAndUpdate {
	
	public static void dispath(String path, String insertPath, String updatePath){
		List<String> list = MyUtils.readFile(path);
		List<String> insertList = new ArrayList<String>();
		List<String> updateList = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String sql = list.get(i);
			if (sql.contains("UPDATE")) {
				updateList.add(sql);
			} else {
				insertList.add(sql);
			}
		}
		MyUtils.outputList(insertList, insertPath);
		MyUtils.outputList(updateList, updatePath);
	}
	
	/**
	 * 为insert语句最后加上 ON DUPLICATE KEY UPDATE id=id，防止出错被卡主
	 * @param path
	 * @param outPath
	 */
	public static void addDuplicateKey(String path, String outPath){
		List<String> list = MyUtils.readFile(path);
		List<String> insertList = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			String sql = list.get(i);
			if (sql.contains("INSERT")) {
				insertList.add(sql.replaceAll(";", " ON DUPLICATE KEY UPDATE id=id;"));
			}
		}
		MyUtils.outputList(insertList, outPath);
	}
	
	public static void main(String[] args) {
		int batch = 20000;
		for (int i = 0; i < 12; i++) {
			int start = batch * i;
			int end = batch * (i + 1);
			String path = "E:/myJava/yiaiSql/insert_insertApply_" + start + "_" + end + ".sql";
			String outPath = "E:/myJava/yiaiSql/insert_insertApply_addDuplicateKey" + start + "_" + end + ".sql";
			addDuplicateKey(path, outPath);
		}
		int start = 240000;
		int end = 252984;
		String path = "E:/myJava/yiaiSql/insert_insertApply_" + start + "_" + end + ".sql";
		String outPath = "E:/myJava/yiaiSql/insert_insertApply_addDuplicateKey" + start + "_" + end + ".sql";
		addDuplicateKey(path, outPath);
	}
}
