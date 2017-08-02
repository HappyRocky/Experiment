package whaty.test;

import java.util.ArrayList;
import java.util.List;

/** 
 * @className:RemoveDuplicateRecord.java
 * @classDescription:去除excel的重复行
 * @author:yourname
 * @createTime:2017年7月20日
 */
public class RemoveDuplicateRecord {

	/**
	 * record1减去record2的内容，返回剩下的内容
	 * @param record1
	 * @param record2
	 * @return
	 */
	public static List<Object[]> removeDuplicate(List<String[]> record1, List<String[]> record2){
		List<Object[]> result = new ArrayList<>();
		for (String[] strs : record1) {
			boolean hasSame = false; // record2中是否有相同记录
			for (String[] strs2 : record2) {
				if (MyUtils.isSame(strs, strs2)) {
					hasSame = true;
					break;
				}
			}
			if (!hasSame) {
				result.add(strs);
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
//		String path1 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-湖北全部419.xls";
//		String path2 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-湖北线下534.xls";
//		String outPath = "F:/myJava/yiaiSql/creditRecord/学分认证查询-湖北差额.xls";
		
//		String path1 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-四川全部009.xls";
//		String path2 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-四川线下378.xls";
//		String outPath = "F:/myJava/yiaiSql/creditRecord/学分认证查询-四川差额.xls";
		
//		String path1 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-江苏全部493.xls";
//		String path2 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-江苏线下569.xls";
//		String outPath = "F:/myJava/yiaiSql/creditRecord/学分认证查询-江苏差额.xls";
		
		String path1 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-湖南全部960.xls";
		String path2 = "F:/myJava/yiaiSql/creditRecord/学分认证查询-湖南线下077.xls";
		String outPath = "F:/myJava/yiaiSql/creditRecord/学分认证查询-湖南差额.xls";
		
		List<String[]> strs1 = MyUtils.readExcel(path1);
		List<String[]> strs2 = MyUtils.readExcel(path2);
		List<Object[]> diffs = removeDuplicate(strs1, strs2);
		String[] titles = {"姓名","培训名称","所属学科","培训编号","学分类型","学分","学时","省","市","区域","单位","科室","专业","职称","地址","邮编","电话","证书申请日期","性别","身份证号","userid","用户名","projid"};
		MyUtils.outputExcel(outPath, titles, diffs);
		System.out.println("全部：" + strs1.size() + ",线下：" + strs2.size() + ",全部-线下：" + (strs1.size() - strs2.size()) + ",结果：" + diffs.size());
		System.exit(0);
	}
	
}

