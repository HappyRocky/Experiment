/*
 * 文件名：ChangeCreditYear.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月8日下午8:19:12
 */
package whaty.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author Administrator
 *
 */
public class ChangeCertificateClassByCredit {
	/**
	 * 按照申请记录，修改证书的班级、项目编号
	 */
	public void outputChangeCertificateClass() {

		int maxSize = 5000;
		// 得到申请记录
		String sql = "SELECT\n" + "	count(*)\n" + "FROM\n" + "	pr_student_certificate c\n" + "JOIN pe_credit_apply a ON a.id = c.Fk_certificate_id\n"
				+ "JOIN pr_class_coursepackage pcc ON pcc.id = a.fk_coursePac_id\n" + "JOIN pe_course_package pcp ON pcp.id = pcc.fk_coursepackage\n"
				+ "WHERE\n" + "	pcp.`code` != c.PROJECTNO\n" + "AND c.Fk_site_id = 'ff80808155da5b850155dddbec9404c9'\n"
				+ "AND c.createDate < '2017-02-05'";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		int num = Integer.valueOf(list.get(0)[0] == null ? "0" : list.get(0)[0].toString());
		int second = num / maxSize + 1;
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		for (int j = 0; j < second; j++) {
			List<String> updatePca = new ArrayList<String>();
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");

			// 查询出当前申请的信息
			sql = "SELECT\n" +
					"	c.id AS certId,\n" +
					"	pcc.fk_class AS classId,\n" +
					"	pcp.`code` AS correctCode,\n" +
					"	c.PROJECTNO AS errorCode\n" +
					"FROM\n" +
					"	pr_student_certificate c\n" +
					"JOIN pe_credit_apply a ON a.id = c.Fk_certificate_id\n" +
					"JOIN pr_class_coursepackage pcc ON pcc.id = a.fk_coursePac_id\n" +
					"JOIN pe_course_package pcp ON pcp.id = pcc.fk_coursepackage\n" +
					"WHERE\n" +
					"	pcp.`code` != c.PROJECTNO\n" +
					"AND c.Fk_site_id = 'ff80808155da5b850155dddbec9404c9'\n" +
					"AND c.createDate < '2017-02-05' limit " + (j * maxSize) + "," + maxSize;
			list = SshMysqlWebtrn.queryBySQL(sql);
			for (Object[] objects : list) {
				String certId = (String) objects[0];
				String classId = (String) objects[1];
				String correctCode = (String) objects[2];
				if (StringUtils.isBlank(classId)) { 
					continue;
				}
				// update
				String updateSql1 = "update pr_student_certificate c set c.key='" + classId + "',c.PROJECTNO='" + correctCode
						+ "' where c.id='" + certId + "';";
				updatePca.add(updateSql1);
			}
			String path1 = "E:/myJava/yiaiSql/20170912/updateCert_from_" + (j * maxSize) + ".sql";
			MyUtils.outputList(updatePca, path1);
		}
		System.out.println("输出完毕");
	}

	public static void main(String[] args) {
		ChangeCertificateClassByCredit changeCreditYear = new ChangeCertificateClassByCredit();
		changeCreditYear.outputChangeCertificateClass();
		System.exit(0);
	}
}
