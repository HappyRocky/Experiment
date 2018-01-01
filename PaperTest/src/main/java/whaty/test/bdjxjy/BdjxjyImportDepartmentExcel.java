package whaty.test.bdjxjy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.MyUtils;

/** 
 * @className:BdjxjyImportDepartmentExcel.java
 * @classDescription:北大继教院导入院系excel
 * @author:GongYanshang
 * @createTime:2018.01.01
 */
public class BdjxjyImportDepartmentExcel {

	public void outputSqlList(){
		String path = "E:/c.xls";
		List<String[]> lineList = MyUtils.readExcel(path, 1);
		List<String> sqlList = new ArrayList<String>();
		int code = 4;
		for (int i = 0; i < lineList.size(); i++) {
			String[] strs = lineList.get(i);
			String name = MyUtils.valueOf(strs[1]).trim().replaceAll(" ", "").replaceAll("　", "");
			if (StringUtils.isNotBlank(name)) {
				String sql = "insert into pe_department (id, code, name, fk_site_id) values "
						+ "('" + MyUtils.uuid() + "','" + (code++) + "','" + name + "','bdjxjy');";
				sqlList.add(sql);
			}
			
		}
		String outputPath = "F:/whaty/北大继教院/数据库迁移/" + DateUtils.getToday() + "/insertDepart.sql";
		MyUtils.outputList(sqlList, outputPath);
	}
	
	public static void main(String[] args) {
		BdjxjyImportDepartmentExcel bdjxjyImportUnitExcel = new BdjxjyImportDepartmentExcel();
		bdjxjyImportUnitExcel.outputSqlList();
	}
}

