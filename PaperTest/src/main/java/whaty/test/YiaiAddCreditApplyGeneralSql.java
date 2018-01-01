/*
 * 文件名：YiaiAddCreditApplyGeneralSql.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月5日上午9:01:41
 */
package whaty.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class YiaiAddCreditApplyGeneralSql {
	
	public static String siteId = "ff80808155da5b850155dddbec9404c9";
	
	// 有效id
	public static String flagIsValid = "40288a962e9d9ac5012e9dd6b0aa0004"; // NAMESPACE='FlagIsvalid' and code='1'
	public static String flagIsNotValid = "40288a962e9d9ac5012e9dd70cd40005"; // 无效
	
	// FlagCMEType id
	public static String flagCMEType1 = "43531506cbdc11e6975b00251113d11d"; // NAMESPACE='FlagCMEType' and code='0' 国家一类
	public static String flagCMEType2 = "4377af1ccbdc11e6975b00251113d11d"; // NAMESPACE='FlagCMEType' and code='1' 远程二类
	
	public static List<String> isDuplicateApplyList; // 存放重复申请的查询sql
	public static String isDuplicateApplyPath = "E:/myJava/yiaiSql/isDuplicateApplySql.txt";
	
	/**
	 * 从旧医爱库中查询申请信息
	 * @return
	 */
	public List<List<String>> queryApplyInfo(){
		String gap = "___";
		String path = "E:/myJava/yiaiSql/20170712/applyRecord.txt";
		List<List<String>> result = new ArrayList<List<String>>();
		// 先查询本地是否存在数据
//		File file = new File(path);
//		if (file.exists()) {
//			List<String> lineList = MyUtils.readFile(path);
//			if (CollectionUtils.isNotEmpty(lineList)) {
//				for (String line : lineList) {
//					String[] strs = line.split(gap);
//					List<String> list = MyUtils.array2List(strs);
//					if (line.endsWith(gap)) {
//						list.add("");
//					}
//					result.add(list);
//				}
//			}
//			return result;
//		}
		
		// 需要重新申请的loginId
		String loginIdPath = "E:/myJava/yiaiSql/20170712/compareApplyInfo_diff_all.txt";
		List<String> lineList1 = MyUtils.readFile(loginIdPath);
		List<String> loginIdList = new ArrayList<String>();
		int count = 0;
		int count2 = 0;
		for (int i = 0; i < lineList1.size(); i++) {
			String line = lineList1.get(i);
			if(line.startsWith("kq") && line.contains("yiaiwang.com")){
				count++;
			} else {
				count2 ++;
			}
			loginIdList.add(line.substring(0,line.indexOf(" ")).trim());
		}
		System.out.println(count + " " + count2);
		String loginIdCondition = MyUtils.list2Str(loginIdList, "','");
		
		
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT   ");
		sb.append(" 	MU.username AS loginId,  ");
		sb.append(" 	from_unixtime(MJO.timecreated), ");
		sb.append(" 	IFNULL(MJO.certnumber,''),  ");
		
		sb.append(" 	MCC. NAME AS category_name,     "); // 3
		sb.append(" 	MCC.cme_credit_type_id AS type, ");
		sb.append(" 	floor(MCC.credit) AS credit     ");
		
//		sb.append(" 	MCC2014. NAME AS category_name2014,     "); // 6
//		sb.append(" 	MCC2014.cme_credit_type_id AS type2014, ");
//		sb.append(" 	floor(MCC2014.credit) AS credit2014,     ");
//		
//		sb.append(" 	MCC2015. NAME AS category_name2015,     "); // 9
//		sb.append(" 	MCC2015.cme_credit_type_id AS type2015, ");
//		sb.append(" 	floor(MCC2015.credit) AS credit2015,     ");
//		
//		sb.append(" 	MCC2016. NAME AS category_name2016,     "); // 12
//		sb.append(" 	MCC2016.cme_credit_type_id AS type2016, ");
//		sb.append(" 	floor(MCC2016.credit) AS credit2016     ");

		sb.append(" FROM   ");
		sb.append(" 	mdl_jscme_order MJO  ");
		sb.append(" LEFT JOIN mdl_user MU ON MJO.userid = MU.id  ");
		sb.append(" LEFT JOIN mdl_user_info_data MUID ON MUID.fieldid = 11 ");
		sb.append(" AND MU.id = MUID.userid  ");
		sb.append(" LEFT JOIN mdl_course_categories MCC ON MCC.id = MJO.category ");
		sb.append(" AND MCC.`is_cme` = 1                ");
		sb.append(" AND MCC.`credit` IS NOT NULL        ");
//		sb.append(" LEFT JOIN mdl_course_categories2014 MCC2014 ON MCC2014.id = MJO.category ");
//		sb.append(" AND MCC2014.`is_cme` = 1                ");
//		sb.append(" AND MCC2014.`credit` IS NOT NULL        ");
//		sb.append(" LEFT JOIN mdl_course_categories2015 MCC2015 ON MCC2015.id = MJO.category ");
//		sb.append(" AND MCC2015.`is_cme` = 1                ");
//		sb.append(" AND MCC2015.`credit` IS NOT NULL        ");
//		sb.append(" LEFT JOIN mdl_course_categories2016 MCC2016 ON MCC2016.id = MJO.category ");
//		sb.append(" AND MCC2016.`is_cme` = 1                ");
//		sb.append(" AND MCC2016.`credit` IS NOT NULL        ");
		sb.append(" WHERE ");
		sb.append(" 	MJO. STATUS = 2 ");
		sb.append(" AND MJO.category > 0 and MJO.timecreated >= UNIX_TIMESTAMP('2017-07-12 19:00:00') ");
		sb.append(" and MU.username in ('19216026@qq.com','805998570@qq.com','1825520817@qq.com','4609785@qq.com','2670628890@qq.com','215774301@qq.com','958553857@qq.com','849153603@qq.com','13383776@qq.com','752064756@qq.com','3449579530@qq.com','412060264@qq.com','522917859@qq.com','1036738968@qq.com','123321123@qq.com','593122689@qq.com','13036779152@163.com','1017014363@qq.com')");
		sb.append(" order by MU.username");
		List<Object[]> cardList =SshMysqlYiaiwang.queryBySQL(sb.toString());
		List<String> lineList = new ArrayList<String>();
		System.out.println(sb.toString());
		for (Object[] objects : cardList) {
			if (objects != null && objects.length > 0) {
				String loginId = objects[0] == null ? "" : objects[0].toString();
				String createTime = objects[1] == null ? "" : objects[1].toString();
				String certNum = objects[2] == null ? "" : objects[2].toString();
				
				// 过滤掉非CME的
				if (StringUtils.isEmpty(loginId) || loginId.startsWith("chaorg")) {
					continue;
				}
				
				if(loginId.startsWith("kq") && loginId.contains("yiaiwang.com")){
					continue;
				}
				
				// 判断是哪个category库
				int startIdx = 3;
				String year = "2017";
//				String year = getYearByDate(createTime);
//				if (year.equals("2017")) {
//					startIdx = 3;
//				} else if (year.equals("2016")) {
//					startIdx = 12;
//				} else if (year.equals("2015")) {
//					startIdx = 9;
//				} else  {
//					startIdx = 6;
//				}
				
				// 查询学分信息
				String categoryName = objects[startIdx] == null ? "" : objects[startIdx].toString();
				String type = objects[startIdx + 1] == null ? "" : objects[startIdx + 1].toString();
				String credit = objects[startIdx + 2] == null ? "" : objects[startIdx + 2].toString();
				
				if (categoryName == "") {
					System.out.println("课程包为空，不申请：" + year + " " + createTime + " " + loginId);
					continue;
				}
				
//				if (categoryName.equals("口腔种植新技术") && !"2016".equals(year)) {
//					System.out.println(categoryName + " " + year);
//					continue;
//				} 
				
				List<String> list = new ArrayList<String>();
				list.add(loginId);
				list.add(categoryName);
				list.add(type);
				list.add(credit);
				list.add(createTime);
				list.add(certNum);
				list.add(year);
				result.add(list);
				lineList.add(MyUtils.list2Str(list, gap));
			}
		}
		System.out.println("共查询到" + result.size() + "个申请记录");
		MyUtils.outputList(lineList, path);
		return result;
	}
	
	/**
	 * 根据时间字符串，返回所在学期的年份
	 * @param dateStr
	 * @return 2014~2017
	 */
	public static String getYearByDate(String dateStr){
		String result = "2017";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = formatter.parse(dateStr);
			if (date != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal2 = Calendar.getInstance();
				cal2.setTime(formatter1.parse("2015-02-25"));
				Calendar cal3 = Calendar.getInstance();
				cal3.setTime(formatter1.parse("2016-02-15"));
				Calendar cal4 = Calendar.getInstance();
				cal4.setTime(formatter1.parse("2017-02-05"));
				if (cal.after(cal4)) {
					result = "2017";
				} else  if (cal.after(cal3)) {
					result = "2016";
				} else if (cal.after(cal2)) {
					result = "2015";
				} else {
					result = "2014";
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("解析日期出错：" + dateStr);
		}
		return result;
	}
	
	//*生成并输出sql
	public void generalApplySql(){
		System.out.println("查询申请信息");
		List<List<String>> queryApplyInfo = queryApplyInfo();
		
		// 读取重复申请的查询sql
		isDuplicateApplyList = MyUtils.readFile(isDuplicateApplyPath);
		if (isDuplicateApplyList == null) {
			isDuplicateApplyList = new ArrayList<String>();
		}
		
		int batch = 100; // 分批处理
		for (int j = 0; j < 1000; j++) {
			int start = j * batch;
			int end = (j + 1) * batch;
			end = Math.min(end, queryApplyInfo.size());

			YiaiAddCreditApplyGeneralSqlThread t = new YiaiAddCreditApplyGeneralSqlThread(start,end,queryApplyInfo);
			t.start();
			
			if (end == queryApplyInfo.size()) {
				break;
			}
		}
	}
	
	public static void main(String[] args) {
		YiaiAddCreditApplyGeneralSql yiaiAddCreditApplyGeneralSql = new YiaiAddCreditApplyGeneralSql();
		yiaiAddCreditApplyGeneralSql.generalApplySql();
	}
}
