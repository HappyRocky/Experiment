package whaty.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 *
 */
public class DeleteERDS2017Student {
	
	public void outputWebtrnDeletesql(){
		
		int maxSize = 2500;
		System.out.println("输出webtrn的delete语句");
		// 得到申请记录
		String sql = "SELECT\n" +
				"	count(*)\n" +
				"FROM\n" +
				"	pe_trainee u\n" +
				"WHERE\n" +
				"	u.LOGIN_ID LIKE 'erds2017@%'\n" +
				"AND u.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		int num = Integer.valueOf(list.get(0)[0] == null ? "0" : list.get(0)[0].toString());
		int second = num / maxSize + 1;
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
			List<String> sqlList = new ArrayList<>();
			// 查询出当前申请的信息
			sql = "SELECT\n" +
					"	u.ID,\n" +
					"	u.FK_SSO_USER_ID\n" +
					"FROM\n" +
					"	pe_trainee u\n" +
					"WHERE\n" +
					"	u.LOGIN_ID LIKE 'erds2017@%'\n" +
					"AND u.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' limit " + (j * maxSize) + "," + maxSize;
			list = SshMysqlWebtrn.queryBySQL(sql);
			for (Object[] objects : list) {
				String traineeId = (String) objects[0];
				String ssoUserId = (String) objects[1];
				sqlList.add("delete from pr_trainee_class_course_package where fk_trainee_id='" + traineeId + "';");
				sqlList.add("delete from pe_tch_elective where FK_TRAINEE_ID='" + traineeId + "';");
				sqlList.add("delete from pr_class_trainee where FK_TRAINEE_ID='" + traineeId + "';");
				sqlList.add("delete from pe_credit_apply where fk_trainee_id='" + traineeId + "';");
				sqlList.add("delete from pr_student_certificate where Fk_student_id='" + traineeId + "';");
				sqlList.add("delete from pe_card_number where FK_SSO_USER_ID='" + traineeId + "';");
				sqlList.add("delete from pr_training_module where fk_trainee_id='" + traineeId + "';");
				sqlList.add("delete from pr_project_trainee where fk_traineeId='" + traineeId + "';");
				sqlList.add("delete from pr_site_trainee where FK_TRAINEE_ID='" + traineeId + "';");
				sqlList.add("delete from pe_trainee where id='" + traineeId + "';");
				sqlList.add("delete from sso_user where id='" + ssoUserId + "';");
			}
			String path1 = "E:/myJava/yiaiSql/20170829/deleteWebtrnERDS_from_" + (j * maxSize) + ".sql";
			MyUtils.outputList(sqlList, path1);
		}
		System.out.println("数据处理完毕");
	}
	
	public void outputSpaceDeletesql(){
		
		int maxSize = 2500;
		System.out.println("输出课程空间的delete语句");
		// 得到申请记录
		String sql = "select count(*) from pe_student s where s.LOGIN_ID like 'erds2017@%' and s.SITE_CODE='yiai'";
		List<Object[]> list = SshMysqlSpace.queryBySQL(sql);
		int num = Integer.valueOf(list.get(0)[0] == null ? "0" : list.get(0)[0].toString());
		int second = num / maxSize + 1;
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
			List<String> sqlList = new ArrayList<>();
			// 查询出当前申请的信息
			sql = "select s.ID,s.FK_SSO_USER_ID from pe_student s where s.LOGIN_ID like 'erds2017@%' and s.SITE_CODE='yiai' limit " + (j * maxSize) + "," + maxSize;
			list = SshMysqlSpace.queryBySQL(sql);
			for (Object[] objects : list) {
				String traineeId = (String) objects[0];
				String ssoUserId = (String) objects[1];
				sqlList.add("delete from pr_tch_stu_elective where FK_STU_ID='" + traineeId + "';");
				sqlList.add("delete from scorm_stu_course where STUDENT_ID='" + traineeId + "';");
				sqlList.add("delete from scorm_stu_sco where STUDENT_ID='" + traineeId + "';");
				sqlList.add("delete from learn_learning_record where FK_STUDENT_ID='" + traineeId + "';");
				sqlList.add("delete from pe_student where id='" + traineeId + "';");
				sqlList.add("delete from sso_user where id='" + ssoUserId + "';");
			}
			String path1 = "E:/myJava/yiaiSql/20170829/deleteSpaceERDS_from_" + (j * maxSize) + ".sql";
			MyUtils.outputList(sqlList, path1);
		}
		System.out.println("数据处理完毕");
	}
	
	public static void main(String[] args) {
		DeleteERDS2017Student deleteERDS2017Student = new DeleteERDS2017Student();
		deleteERDS2017Student.outputWebtrnDeletesql();
		deleteERDS2017Student.outputSpaceDeletesql();
		System.exit(0);
	}
}

