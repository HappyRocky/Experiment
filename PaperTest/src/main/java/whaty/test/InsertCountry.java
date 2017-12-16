package whaty.test;

import java.util.ArrayList;
import java.util.List;

import utils.DateUtils;

/** 
 * @className:InsertCountry.java
 * @classDescription:
 * @author:yourname
 * @createTime:2017年12月14日
 */
public class InsertCountry {
	public void outputSqlList(){
		List<String> result = new ArrayList<>();
		String path = "F:\\whaty\\country.txt";
		List<String> lineList = MyUtils.readFile(path);
		for (int i = 0; i < lineList.size(); i += 2) {
			if (i % 4 == 0) {
				String code = lineList.get(i);
				String name = lineList.get(i + 1);
				String id = MyUtils.uuid();
				String sql = "INSERT INTO `pe_country` (`id`, `name`, `code`, `fk_site_id`) VALUES ('" + id + "', '" + name + "', '" + code + "', 'bdjxjy');";
				result.add(sql);
			}
		}
		path = "E:/myJava/generalSql/" + DateUtils.getToday() + "/insertCountry.sql";
		MyUtils.outputList(result, path);
	}
	
	public static void main(String[] args) {
		InsertCountry insertCountry = new InsertCountry();
		insertCountry.outputSqlList();
		System.exit(0);
	}
}

