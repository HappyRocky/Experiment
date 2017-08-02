/*
 * 文件名：ExportApplyInfo.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月6日下午6:18:44
 */
package whaty.test;

import java.util.List;


/**
 * @author Administrator
 *
 */
public class ExportApplyInfo {
	public static String siteId = "ff80808155da5b850155dddbec9404c9";

	/**
	 * 输出截止日期之前的统计结果至excel
	 * @param time
	 */
	public static void outputApplyInfo(String time, String path1, String path2){
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT                                        ");
		sb.append(" 	pt.LOGIN_ID as loginId,                   ");
		sb.append(" 	COUNT(pca.id) as applyCount               ");
		sb.append(" FROM                                          ");
		sb.append(" 	pe_credit_apply pca                       ");
		sb.append(" JOIN pe_trainee pt ON pt.ID=pca.fk_trainee_id ");
		sb.append(" WHERE                                         ");
		sb.append(" 	pca.fk_site_id = '" + siteId + "'         ");
		sb.append(" AND pca.flag_isvalid = '" + YiaiAddCreditApplyGeneralSql.flagIsValid + "' ");
		sb.append(" AND pca.create_date < '" + time + "'          ");
		sb.append(" GROUP BY                                      ");
		sb.append(" 	pca.fk_trainee_id                         ");
		sb.append(" ORDER BY applyCount DESC  ");
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sb.toString());
		System.out.println("查询出了" + list.size() + "条统计记录");
		String[] titles = {"用户名","申请数量"};
		MyUtils.outputExcel(path1, titles, list);
		
		StringBuffer sb2 = new StringBuffer();
		sb2.append("  SELECT                                                    ");
		sb2.append("  	mu.username,                                           ");
		sb2.append("  	count(o.id) as applyCount                                       ");
		sb2.append("  FROM                                                      ");
		sb2.append("  	mdl_jscme_order o                                      ");
		sb2.append("  JOIN mdl_user mu ON mu.id = o.userid                      ");
		sb2.append("  WHERE                                                     ");
		sb2.append("  	o.timecreated < UNIX_TIMESTAMP('" + time + "')  ");
		sb2.append("  AND o.category > 0                                        ");
		sb2.append("  AND o. STATUS = 2                                         ");
		sb2.append("  GROUP BY                                                  ");
		sb2.append("  	mu.id                                                  ");
		sb2.append("  ORDER BY                                                  ");
		sb2.append("  	applyCount    DESC                                         ");
		list = SshMysqlYiaiwang.queryBySQL(sb2.toString());
		System.out.println("医爱网查询出了" + list.size() + "条统计记录");
		MyUtils.outputExcel(path2, titles, list);
		
	}
	
	public static void main(String[] args) {
		String path1 = "E:/myJava/yiaiSql/exportApplyInfo_before_2017-06-07_12_00_00.xls";
		String path2 = "E:/myJava/yiaiSql/exportApplyInfo_yiaiwang_before_2017-06-07_12_00_00.xls";
		outputApplyInfo("2017-06-07 12:00:00",path1,path2);
		
//		path = "E:/myJava/yiaiSql/exportApplyInfo_now.xls";
//		outputApplyInfo("2017-06-08 12:00:00",path);
	}
	
}
