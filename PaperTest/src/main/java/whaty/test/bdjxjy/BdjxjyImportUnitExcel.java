package whaty.test.bdjxjy;

import java.util.ArrayList;
import java.util.List;

import utils.DateUtils;
import whaty.test.MyUtils;

/** 
 * @className:BdjxjyImportUnitExcel.java
 * @classDescription:北大继教院导入合作单位黑名单excel
 * @author:GongYanshang
 * @createTime:2017年12月29日
 */
public class BdjxjyImportUnitExcel {

	public void outputSqlList(){
		String path = "E:/b.xls";
		List<String[]> lineList = MyUtils.readExcel(path, 1);
		List<String> sqlList = new ArrayList<String>();
		for (int i = 0; i < lineList.size(); i++) {
			String[] strs = lineList.get(i);
			String code = MyUtils.valueOf(strs[0]).trim().replaceAll(" ", "").replaceAll("　", "");
			String name = MyUtils.valueOf(strs[1]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_person = MyUtils.valueOf(strs[2]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_number = MyUtils.valueOf(strs[3]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_address = MyUtils.valueOf(strs[4]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_capital = MyUtils.valueOf(strs[5]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_time = MyUtils.valueOf(strs[6]).trim().replaceAll(" ", "").replaceAll("　", "");
			String reason = MyUtils.valueOf(strs[7]).trim().replaceAll(" ", "").replaceAll("　", "");
			String sql = "INSERT INTO `pe_unit_blacklist` (`id`, `name`, `regist_person`,`regist_address`, `regist_number`, `regist_capital`, `regist_time`, `reason`, `fk_site_id`)  " 
					+ "VALUES ('" + MyUtils.uuid() + "','" + name + "','" + regist_person + "','" + regist_address + "','" + regist_number + "','" + regist_capital + "','" + regist_time + "','" + reason + "','bdjxjy');";
			sqlList.add(sql);
		}
		String outputPath = "F:/whaty/北大继教院/数据库迁移/" + DateUtils.getToday() + "/insertUnitBlack.sql";
		MyUtils.outputList(sqlList, outputPath);
	}
	
	public static void main(String[] args) {
		BdjxjyImportUnitExcel bdjxjyImportUnitExcel = new BdjxjyImportUnitExcel();
		bdjxjyImportUnitExcel.outputSqlList();
	}
}

