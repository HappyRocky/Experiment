package whaty.test.bdjxjy;

import java.util.ArrayList;
import java.util.List;

import utils.DateUtils;
import whaty.test.MyUtils;

/** 
 * @className:BdjxjyImportUnitExcel.java
 * @classDescription:北大继教院导入合作单位excel
 * @author:GongYanshang
 * @createTime:2017年12月29日
 */
public class BdjxjyImportBlacklistExcel {

	public void outputSqlList(){
		String path = "E:/a.xls";
		List<String[]> lineList = MyUtils.readExcel(path, 1);
		List<String> sqlList = new ArrayList<String>();
		for (int i = 0; i < lineList.size(); i++) {
			String[] strs = lineList.get(i);
			String code = MyUtils.valueOf(strs[0]).trim().replaceAll(" ", "").replaceAll("　", "");
			String name = MyUtils.valueOf(strs[1]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_person = MyUtils.valueOf(strs[2]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_fund = MyUtils.valueOf(strs[3]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_address = MyUtils.valueOf(strs[4]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_number = MyUtils.valueOf(strs[5]).trim().replaceAll(" ", "").replaceAll("　", "");
			String intro = MyUtils.valueOf(strs[6]).trim().replaceAll(" ", "").replaceAll("　", "");
			String photo = MyUtils.valueOf(strs[7]).trim().replaceAll(" ", "").replaceAll("　", "");
			String regist_capital = MyUtils.valueOf(strs[8]).trim().replaceAll(" ", "").replaceAll("　", "");
			String create_time = MyUtils.valueOf(strs[9]).trim().replaceAll(" ", "").replaceAll("　", "");
			String[] times = create_time.split("/");
			String month = times[0];
			String day = times[1];
			String year = "20" + times[2].substring(0, 2);
			String time = times[2].substring(2);
			create_time = year + "-" + month + "-" + day + " " + time; 
			String sql = "INSERT INTO `pe_unit_cooperation` (	`id`,	`code`,	`name`,	`regist_address`,	`regist_number`,	`regist_person`,	`regist_fund`,	`regist_capital`,	`intro`,	`fk_site_id`,	`create_time`) " 
					+ "VALUES ('" + MyUtils.uuid() + "','" + code + "','" + name + "','" + regist_address + "','" + regist_number + "','" + regist_person + "','" + regist_fund + "','" + regist_capital + "','" + intro + "','bdjxjy','" + create_time + "');";
			sqlList.add(sql);
		}
		String outputPath = "F:/whaty/北大继教院/数据库迁移/" + DateUtils.getToday() + "/insertUnit.sql";
		MyUtils.outputList(sqlList, outputPath);
	}
	
	public static void main(String[] args) {
		BdjxjyImportBlacklistExcel bdjxjyImportUnitExcel = new BdjxjyImportBlacklistExcel();
		bdjxjyImportUnitExcel.outputSqlList();
	}
}

