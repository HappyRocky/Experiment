package whaty.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;

/** 
 * @className:InsertNationSql.java
 * @classDescription:将民族插入常量表
 * @author:GongYanshang
 * @createTime:2017年12月14日
 */
public class InsertNationSql {
	public void outputSqlList(){
		List<String> result = new ArrayList<String>();
		String str = "汉族、蒙古族、回族、藏族、维吾尔族、苗族、彝族、壮族、布依族、朝鲜族、满族、侗族、瑶族、白族、土家族、哈尼族、哈萨克族、傣族、黎族、僳僳族、佤族、畲族、高山族、拉祜族、水族、东乡族、纳西族、景颇族、柯尔克孜族、土族、达斡尔族、仫佬族、羌族、布朗族、撒拉族、毛南族、仡佬族、锡伯族、阿昌族、普米族、塔吉克族、怒族、乌孜别克族、俄罗斯族、鄂温克族、德昂族、保安族、裕固族、京族、塔塔尔族、独龙族、鄂伦春族、赫哲族、门巴族、珞巴族、基诺族";
		String[] strs = str.split("、");
		int code = 0;
		for (String nation : strs) {
			if (StringUtils.isNotBlank(nation)) {
				String sql = "INSERT INTO `enum_const` (`name`, `namespace`, `code`, `note`, `fk_site_id`) VALUES ('" + nation + "', 'FlagNation', '" + (code++) + "', '民族', 'bdjxjy');";
				result.add(sql);
			}
		}
		
		String path = "E:/myJava/generalSql/" + DateUtils.getToday() + "/insertNation.sql";
		MyUtils.outputList(result, path);
	}
	
	public static void main(String[] args) {
		InsertNationSql insertNationSql = new InsertNationSql();
		insertNationSql.outputSqlList();
		System.exit(0);
	}
}

