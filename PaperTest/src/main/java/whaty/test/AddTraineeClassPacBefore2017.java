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
import org.apache.commons.lang.StringUtils;

/**
 * @author Administrator
 *
 */
public class AddTraineeClassPacBefore2017 {
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
		for (int i = 0; i < periodList.size() - 1; i++) {
			// 得到申请记录
			StringBuffer sb = new StringBuffer();
			sb.append(" select count(*) ");
			sb.append(" from                                                                                                                                                       ");
			sb.append("  pe_credit_apply pca                                                                                                                                       ");
			sb.append(" WHERE                                                                                                                                                      ");
			sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'                                                                                                    ");
			sb.append(" AND pca.create_date BETWEEN '"+periodList.get(i)+"'                                                                                                                   ");
			sb.append(" AND '"+periodList.get(i + 1)+"'                     ");
			List<Object[]> list = SshMysqlWebtrn.queryBySQL(sb.toString());
			if (!CollectionUtils.isNotEmpty(list)) {
				continue;
			}
			int num = Integer.valueOf(list.get(0)[0] == null ? "0" : list.get(0)[0].toString());
			int second = num / maxSize + 1;
			System.out.println(periodList.get(i) + "~" + periodList.get(i + 1) + " 共查询出" + num + "条数据");
			System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
			List<String> allSqlList = new ArrayList<String>();
			for (int j = 0; j < second; j++) {
				System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
				
				// 查询出当前申请的信息
				sb = new StringBuffer();
				sb.append(" select pca.fk_trainee_id,pca.fk_coursePac_id,pccOld.fk_coursepackage,pca.create_date");
				sb.append(" from  ");
				sb.append("  pe_credit_apply pca  ");
				sb.append(" JOIN pr_class_coursepackage pccOld ON pccOld.ID = pca.fk_coursePac_id   ");
				sb.append(" WHERE   ");
				sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'     ");
				sb.append(" AND pca.create_date BETWEEN '" + periodList.get(i) + "'        ");
				sb.append(" AND '" + periodList.get(i + 1) + "' limit " + (j * maxSize) + "," + maxSize);
				list = SshMysqlWebtrn.queryBySQL(sb.toString());
				for (Object[] objects : list) {
					String traineeId = (String) objects[0];
					String classPacId = (String) objects[1];
					String packageId = (String) objects[2];
					String applyDate = objects[3] == null ? "" : objects[3].toString();
					
					// 查询是否已存在
					String sql = "select * from pr_trainee_class_course_package p where p.fk_trainee_id='" + traineeId
							+ "' and p.fk_class_course_package_id='" + classPacId + "'";
					List<Object[]> list2 = SshMysqlWebtrn.queryBySQL(sql);
					if (CollectionUtils.isNotEmpty(list2)) { // 存在记录
						continue;
					}

					// 插入
					sb = new StringBuffer();
					sb.append("INSERT INTO `pr_trainee_class_course_package` ( ");
					sb.append(" `id`, ");
					sb.append(" `fk_trainee_id`, ");
					sb.append(" `fk_class_course_package_id`, ");
					sb.append(" `fk_course_package_id`, ");
					sb.append(" `study_percent`, ");
					sb.append(" `complete_date`, ");
					sb.append(" `create_date`, ");
					sb.append(" `modify_date`,  ");
					sb.append(" `flag_apply_status`, ");
					sb.append(" `fk_site_id` ");
					sb.append(" ) VALUES (  ");
					sb.append(" 	'" + MyUtils.uuid() + "', ");
					sb.append(" 	'" + traineeId + "',     ");
					sb.append(" 	'" + classPacId + "',  ");
					sb.append(" 	'" + packageId + "',   ");
					sb.append(" 	100,     ");
					sb.append(" 	'" + applyDate + "',  ");
					sb.append(" 	NOW(),   ");
					sb.append(" 	NOW(),  ");
					sb.append(" 	'" + YiaiAddCreditApplyGeneralSql.flagIsValid + "',  ");
					sb.append(" 	'" + YiaiAddCreditApplyGeneralSql.siteId + "'  ");
					sb.append(" );  ");
					allSqlList.add(sb.toString());
				}
			}
			String path1 = "E:/myJava/yiaiSql/20170705/updateTraineeClassPac_" + year + "_" + periodList.get(i) + "_" + periodList.get(i + 1) + ".sql";
			MyUtils.outputList(allSqlList, path1);
			System.out.println(periodList.get(i) + "-" + periodList.get(i + 1) + "的数据处理完毕，需要插入" + allSqlList.size() + "条记录");
		}
		System.out.println(periodStart + "~" + periodEnd + "全部处理完毕");
	}
	
	public static void main(String[] args) {
		AddTraineeClassPacBefore2017 changeCreditYear = new AddTraineeClassPacBefore2017();
		changeCreditYear.outputChangeCreditYear("2016", "ae8a903ee1ca47929a3d1011d6cb31d0",
				"bc6ed0cb76744b18ba90c6d09662275f", "2016-02-15", "2017-02-05");
//		changeCreditYear.outputChangeCreditYear("2015", "80b93beec1914de7831e5f71b9da4d1c",
//				"71a546591765432e9755866eb49431d6", "2015-02-25", "2016-02-15");
//		changeCreditYear.outputChangeCreditYear("2014", "dfdfc4026ffe43199d52929b95643989",
//				"de76d90235814c419699ef475c8b3346", "2014-02-15", "2015-02-25");
		System.exit(0);
	}
}
