/*
 * 文件名：ChangeCreditYear.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月8日下午8:19:12
 */
package whaty.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Administrator
 *
 */
public class ChangeCreditYearNew {
	/**
	 * 按照年份，修改申请记录所关联的班级课程包id、证书的项目编号
	 */
	public void outputChangeCreditYear(String year, String scClass, String qgClass, String periodStart, String periodEnd){
		
		List<String> periodList = new ArrayList<String>();
		periodList.add(periodStart);
		for (int i = 1; i <= 12; i++) {
			periodList.add(year + "-" + (i < 10 ? "0" : "") + i + "-01");
		}
		periodList.add(periodEnd);
		
		int maxSize = 100;
		List<String> updatePca = new ArrayList<String>();
		List<String> updatePtcc = new ArrayList<String>();
		for (int i = 0; i < periodList.size() - 1; i++) {
			// 得到申请记录
			StringBuffer sb = new StringBuffer();
			sb.append(" select count(*) ");
			sb.append(" from                                                                                                                                                       ");
			sb.append("  pe_credit_apply pca                                                                                                                                       ");
			sb.append(" JOIN pr_class_coursepackage pccOld ON pccOld.ID = pca.fk_coursePac_id                                                                                      ");
			sb.append(" JOIN pe_course_package packageOld ON packageOld.id = pccOld.fk_coursepackage                                                                               ");
			sb.append(" JOIN pe_course_package package ON package.`year` = '"+year+"'                                                                                                  ");
			sb.append(" AND (                                                                                                                                                      ");
			sb.append(" 	packageOld.`name` = package.`name`                                                                                                                     ");
			sb.append(" 	OR packageOld.`name` = CONCAT(                                                                                                                         ");
			sb.append(" 		package.`name`,                                                                                                                                    ");
			sb.append(" 		'【无课程】'                                                                                                                                       ");
			sb.append(" 	)                                                                                                                                                      ");
			sb.append(" 	OR packageOld.`name` = CONCAT(                                                                                                                         ");
			sb.append(" 		'【包含无效课程】',                                                                                                                                ");
			sb.append(" 		package.`name`                                                                                                                                     ");
			sb.append(" 	)                                                                                                                                                      ");
			sb.append(" )                                                                                                                                                          ");
			sb.append(" AND package.fk_credit_id = packageOld.fk_credit_id                                                                                                         ");
			sb.append(" JOIN pe_trainee pt ON pt.id = pca.fk_trainee_id                                                                                                            ");
			sb.append(" JOIN pr_class_coursepackage pcc ON pcc.fk_class =                                                                                                          ");
			sb.append(" IF (                                                                                                                                                       ");
			sb.append(" 	pt.province = '四川省',                                                                                                                                ");
			sb.append(" 	'"+scClass+"',                                                                                                                    ");
			sb.append(" 	'"+qgClass+"'                                                                                                                     ");
			sb.append(" )                                                                                                                                                          ");
			sb.append(" AND pcc.fk_coursepackage = package.id                                                                                                                      ");
			sb.append(" JOIN pr_trainee_class_course_package ptcc ON ptcc.fk_class_course_package_id = pccOld.id                                                                   ");
			sb.append(" AND ptcc.fk_trainee_id = pt.ID                                                                                                                             ");
			sb.append(" WHERE                                                                                                                                                      ");
			sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'                                                                                                    ");
			sb.append(" AND pca.create_date BETWEEN '"+periodList.get(i)+"'                                                                                                                   ");
			sb.append(" AND '"+periodList.get(i + 1)+"'                     ");
			List<Object[]> list = SshMysqlWebtrn.queryBySQL(sb.toString());
			if (!CollectionUtils.isNotEmpty(list)) {
				continue;
			}
			int num = Integer.valueOf(list.get(0)[0] == null ? "0" : list.get(0)[0].toString());
			if (num >= maxSize) {
				System.out.println(periodList.get(i) + "~" + periodList.get(i + 1) + " 共查询出" + num + "条数据");
				int second = num / maxSize + 1;
				System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + (second - 1)+ "次执行");
				for (int j = 0; j < second; j++) {
					System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
					sb = new StringBuffer();
					sb.append(" select pcc.fk_class,pcc.id,pca.id,pcc.id,ptcc.id ");
					sb.append(" from                                                                                                                                                       ");
					sb.append("  pe_credit_apply pca                                                                                                                                       ");
					sb.append(" JOIN pr_class_coursepackage pccOld ON pccOld.ID = pca.fk_coursePac_id                                                                                      ");
					sb.append(" JOIN pe_course_package packageOld ON packageOld.id = pccOld.fk_coursepackage                                                                               ");
					sb.append(" JOIN pe_course_package package ON package.`year` = '"+year+"'                                                                                                  ");
					sb.append(" AND (                                                                                                                                                      ");
					sb.append(" 	packageOld.`name` = package.`name`                                                                                                                     ");
					sb.append(" 	OR packageOld.`name` = CONCAT(                                                                                                                         ");
					sb.append(" 		package.`name`,                                                                                                                                    ");
					sb.append(" 		'【无课程】'                                                                                                                                       ");
					sb.append(" 	)                                                                                                                                                      ");
					sb.append(" 	OR packageOld.`name` = CONCAT(                                                                                                                         ");
					sb.append(" 		'【包含无效课程】',                                                                                                                                ");
					sb.append(" 		package.`name`                                                                                                                                     ");
					sb.append(" 	)                                                                                                                                                      ");
					sb.append(" )                                                                                                                                                          ");
					sb.append(" AND package.fk_credit_id = packageOld.fk_credit_id                                                                                                         ");
					sb.append(" JOIN pe_trainee pt ON pt.id = pca.fk_trainee_id                                                                                                            ");
					sb.append(" JOIN pr_class_coursepackage pcc ON pcc.fk_class =                                                                                                          ");
					sb.append(" IF (                                                                                                                                                       ");
					sb.append(" 	pt.province = '四川省',                                                                                                                                ");
					sb.append(" 	'"+scClass+"',                                                                                                                    ");
					sb.append(" 	'"+qgClass+"'                                                                                                                     ");
					sb.append(" )                                                                                                                                                          ");
					sb.append(" AND pcc.fk_coursepackage = package.id                                                                                                                      ");
					sb.append(" JOIN pr_trainee_class_course_package ptcc ON ptcc.fk_class_course_package_id = pccOld.id                                                                   ");
					sb.append(" AND ptcc.fk_trainee_id = pt.ID                                                                                                                             ");
					sb.append(" WHERE                                                                                                                                                      ");
					sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'                                                                                                    ");
					sb.append(" AND pca.create_date BETWEEN '"+periodList.get(i)+"'                                                                                                                   ");
					sb.append(" AND '"+periodList.get(i + 1)+"' limit " + (j * maxSize) + "," + maxSize);
					list = SshMysqlWebtrn.queryBySQL(sb.toString());
					for (Object[] objects : list) {
						String updateSql1 = "update pe_credit_apply pca set pca.fk_class_id='" + (String) objects[0]
								+ "', pca.fk_coursePac_id ='" + (String) objects[1] + "' where pca.id='"
								+ (String) objects[2] + "';";
						String updateSql2 = "update pr_trainee_class_course_package ptcc set ptcc.fk_class_course_package_id = '"
								+ (String) objects[3] + "' where ptcc.id='" + (String) objects[4] + "';";
						updatePca.add(updateSql1);
						updatePtcc.add(updateSql2);
					}
				}
			} else {
				sb = new StringBuffer();
				sb.append(" select pcc.fk_class,pcc.id,pca.id,pcc.id,ptcc.id ");
				sb.append(" from                                                                                                                                                       ");
				sb.append("  pe_credit_apply pca                                                                                                                                       ");
				sb.append(" JOIN pr_class_coursepackage pccOld ON pccOld.ID = pca.fk_coursePac_id                                                                                      ");
				sb.append(" JOIN pe_course_package packageOld ON packageOld.id = pccOld.fk_coursepackage                                                                               ");
				sb.append(" JOIN pe_course_package package ON package.`year` = '"+year+"'                                                                                                  ");
				sb.append(" AND (                                                                                                                                                      ");
				sb.append(" 	packageOld.`name` = package.`name`                                                                                                                     ");
				sb.append(" 	OR packageOld.`name` = CONCAT(                                                                                                                         ");
				sb.append(" 		package.`name`,                                                                                                                                    ");
				sb.append(" 		'【无课程】'                                                                                                                                       ");
				sb.append(" 	)                                                                                                                                                      ");
				sb.append(" 	OR packageOld.`name` = CONCAT(                                                                                                                         ");
				sb.append(" 		'【包含无效课程】',                                                                                                                                ");
				sb.append(" 		package.`name`                                                                                                                                     ");
				sb.append(" 	)                                                                                                                                                      ");
				sb.append(" )                                                                                                                                                          ");
				sb.append(" AND package.fk_credit_id = packageOld.fk_credit_id                                                                                                         ");
				sb.append(" JOIN pe_trainee pt ON pt.id = pca.fk_trainee_id                                                                                                            ");
				sb.append(" JOIN pr_class_coursepackage pcc ON pcc.fk_class =                                                                                                          ");
				sb.append(" IF (                                                                                                                                                       ");
				sb.append(" 	pt.province = '四川省',                                                                                                                                ");
				sb.append(" 	'"+scClass+"',                                                                                                                    ");
				sb.append(" 	'"+qgClass+"'                                                                                                                     ");
				sb.append(" )                                                                                                                                                          ");
				sb.append(" AND pcc.fk_coursepackage = package.id                                                                                                                      ");
				sb.append(" JOIN pr_trainee_class_course_package ptcc ON ptcc.fk_class_course_package_id = pccOld.id                                                                   ");
				sb.append(" AND ptcc.fk_trainee_id = pt.ID                                                                                                                             ");
				sb.append(" WHERE                                                                                                                                                      ");
				sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'                                                                                                    ");
				sb.append(" AND pca.create_date BETWEEN '"+periodList.get(i)+"'                                                                                                                   ");
				sb.append(" AND '"+periodList.get(i + 1)+"'");
				list = SshMysqlWebtrn.queryBySQL(sb.toString());
				System.out.println(periodList.get(i) + "~" + periodList.get(i + 1) + " 共查询出" + list.size() + "条数据");
				for (Object[] objects : list) {
					String updateSql1 = "update pe_credit_apply pca set pca.fk_class_id='" + (String) objects[0]
							+ "', pca.fk_coursePac_id ='" + (String) objects[1] + "' where pca.id='"
							+ (String) objects[2] + "';";
					String updateSql2 = "update pr_trainee_class_course_package ptcc set ptcc.fk_class_course_package_id = '"
							+ (String) objects[3] + "' where ptcc.id='" + (String) objects[4] + "';";
					updatePca.add(updateSql1);
					updatePtcc.add(updateSql2);
				}
			}
		}
		String path1 = "E:/myJava/yiaiSql/20170608/updatePca_" + year + ".sql";
		MyUtils.outputList(updatePca, path1);
		String path2 = "E:/myJava/yiaiSql/20170608/updatePtcc_" + year + ".sql";
		MyUtils.outputList(updatePtcc, path2);
		
	}
	
	public static void main(String[] args) {
		ChangeCreditYearNew changeCreditYear = new ChangeCreditYearNew();
//		changeCreditYear.outputChangeCreditYear("2016", "ae8a903ee1ca47929a3d1011d6cb31d0",
//				"bc6ed0cb76744b18ba90c6d09662275f", "2016-02-15", "2017-02-04");
		changeCreditYear.outputChangeCreditYear("2015", "80b93beec1914de7831e5f71b9da4d1c",
				"71a546591765432e9755866eb49431d6", "2015-02-25", "2016-02-14");
//		changeCreditYear.outputChangeCreditYear("2014", "dfdfc4026ffe43199d52929b95643989",
//				"de76d90235814c419699ef475c8b3346", "2014-02-15", "2015-02-24");
	}
}
