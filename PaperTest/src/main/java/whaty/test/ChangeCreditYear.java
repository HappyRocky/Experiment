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
public class ChangeCreditYear {
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
		List<String> updatePca = new ArrayList<>();
		int updateCount = 0; // 需要更新的记录数 
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
			for (int j = 0; j < second; j++) {
				System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
				
				// 查询出当前申请的信息
				sb = new StringBuffer();
				sb.append(" select pca.id,pca.fk_class_id,pca.type,packageOld.`name`,pt.province,pca.credit ");
				sb.append(" from  ");
				sb.append("  pe_credit_apply pca  ");
				sb.append(" JOIN pr_class_coursepackage pccOld ON pccOld.ID = pca.fk_coursePac_id   ");
				sb.append(" JOIN pe_course_package packageOld ON packageOld.id = pccOld.fk_coursepackage   ");
				sb.append(" JOIN pe_trainee pt ON pt.id = pca.fk_trainee_id    ");
				sb.append(" WHERE   ");
				sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'     ");
				sb.append(" AND pca.create_date BETWEEN '" + periodList.get(i) + "'        ");
				sb.append(" AND '" + periodList.get(i + 1) + "' limit " + (j * maxSize) + "," + maxSize);
				list = SshMysqlWebtrn.queryBySQL(sb.toString());
				for (Object[] objects : list) {
					String pcaId = (String) objects[0];
					String classId = (String) objects[1];
					String creditType = (String) objects[2];
					String pacName = (String) objects[3];
					String province = (String) objects[4];
					String credit = (String) objects[5];
					if (classId.equals(qgClass) || classId.equals(scClass)) { // 班级正确，不作处理
						continue;
					}
					
					// 计算出正确班级
					String correctClassId = qgClass;
					if (province != null && province.contains("四川")) {
						correctClassId = scClass;
					}
					
					// 计算出正确课程包
					String sql = "SELECT\n" +
							"	pcc.id,pac.`code`\n" +
							"FROM\n" +
							"	pr_class_coursepackage pcc\n" +
							"JOIN pe_course_package pac ON pac.id = pcc.fk_coursepackage\n" +
							"join pe_credit pc on pc.id=pcc.fk_credit_id " +
							"join enum_const ec on ec.id = pcc.flag_credit " +
							"WHERE\n" +
							"	pcc.fk_class = '" + correctClassId + "'\n" +
							"AND pac.`name` = '" + pacName + "' "+
							"and pc.`CODE`='" + creditType + "' "+
							"and pac.`year`='" + year + "' "+
							"and ec.`code`='" + credit + "'";
					list = SshMysqlWebtrn.queryBySQL(sql);
					if (CollectionUtils.isEmpty(list)) {
						System.out.println("查询不到正确的课程包：pcaId=" + pcaId + ",sql=" + sql);
						continue;
					}
					if (list.size() >= 2) {
						String code = (String)((Object[])list.get(0))[1];
						boolean allTheSame = true;
						if (StringUtils.isNotBlank(code)) {
							for (int k = 1; k < list.size(); k++) {
								String code2 = (String)((Object[])list.get(k))[1];
								if (!code.equals(code2)) {
									allTheSame = false;
									break;
								}
							}
						}
						if (!allTheSame) {
							System.out.println("查到多个课程包且code不同：pcaId=" + pcaId + ",sql=" + sql);
							continue;
						}
					}
					Object[] objects2 = list.get(0);
					String correctClassPacId = (String) objects2[0];
					
					// update
					String updateSql1 = "update pe_credit_apply pca set pca.fk_class_id='" + correctClassId
							+ "', pca.fk_coursePac_id ='" + correctClassPacId + "' where pca.id='"
							+ pcaId + "';";
					updatePca.add(updateSql1);
					updateCount++;
				}
			}
		}
		String path1 = "E:/myJava/yiaiSql/20170912/updatePca_" + year + ".sql";
		MyUtils.outputList(updatePca, path1);
		System.out.println(periodStart + "-" + periodEnd + "的数据处理完毕，需要更新" + updateCount + "条记录");
	}
	
	public static void main(String[] args) {
		AddTraineeClassPacBefore2017 changeCreditYear = new AddTraineeClassPacBefore2017();
		changeCreditYear.outputChangeCreditYear("2016", "ae8a903ee1ca47929a3d1011d6cb31d0",
				"bc6ed0cb76744b18ba90c6d09662275f", "2016-02-15", "2017-02-04");
//		changeCreditYear.outputChangeCreditYear("2015", "80b93beec1914de7831e5f71b9da4d1c",
//				"71a546591765432e9755866eb49431d6", "2015-02-25", "2016-02-14 23:59:59");
//		changeCreditYear.outputChangeCreditYear("2014", "dfdfc4026ffe43199d52929b95643989",
//				"de76d90235814c419699ef475c8b3346", "2014-02-15", "2015-02-24");
	}
}
