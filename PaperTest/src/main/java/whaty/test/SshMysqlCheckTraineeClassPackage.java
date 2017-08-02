package whaty.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 * 
 * @author 董得超
 */
public class SshMysqlCheckTraineeClassPackage {
	static String sshhost = "210.14.140.85"; // 远程服务器地址
	static String sshusername = "webtrn"; // 服务器用户名
	static String sshPassword = "0DgFa1AVbB"; // 服务器密码
	static int sshport = 22; // SSH默认端口号

	static int lport = 3307; // 本地端口
	static String host = "119.254.4.60"; // 远程MySQL服务器
	static int rport = 3306; // 远程MySQL服务端口
	static String dbName = "starbuckyiaiwang"; // 远程MySQL数据库名称
	static String dbUser = "whatyU@123"; // 远程MySQL数据库用户名
	static String dbPwd = "whatyp@132"; // 远程MySQL数据库密码

	static int lport2 = 3308; // 本地2号端口
	static String host2 = "210.14.140.92"; // 远程MySQL2号服务器
	static int rport2 = 3306; // 远程MySQL2号服务端口
	static String dbName2 = "webtrn";
	static String dbUser2 = "webtrn";
	static String dbPwd2 = "X55lhVAc";

	static String FlagIsActivation_no = "190d29945f7f11e69b44848f69e05bf0"; // 不可用
	static String FlagIsActivation_yes = "190959545f7f11e69b44848f69e05bf0"; // 可用
	


	/**
	 * 比较两个数据源的内容
	 */
	public static void compareData(Connection conn, Connection conn2) {
		int maxSize = 100;
		System.out.println("*****************************BEGIN*************************");
		Statement statement = null;
		ResultSet resultSet = null;
		int num = 0;
		String sql = "";
		String result = "";
		long begin = System.currentTimeMillis();
		try {
			statement = conn2.createStatement();
			sql = "SELECT\n" +
					"	count(a.traineeId)\n" +
					"FROM\n" +
					"	(\n" +
					"		SELECT DISTINCT\n" +
					"			pte.FK_TRAINEE_ID AS traineeId,\n" +
					"			pclc.id AS classPacId,\n" +
					"			pcp.id AS pacId\n" +
					"		FROM\n" +
					"			pe_tch_elective pte\n" +
					"		JOIN pe_open_course poc ON poc.ID = pte.FK_OPENCOURSE_ID\n" +
					"		JOIN pr_coursepackage_course pcc ON pcc.fk_tchcourse = poc.FK_TCH_COURSE\n" +
					"		JOIN pe_course_package pcp ON pcp.id = pcc.fk_coursepackage\n" +
					"		JOIN pr_class_coursepackage pclc ON pclc.fk_class = poc.FK_TRAIN_CLASS\n" +
					"		AND pclc.fk_coursepackage = pcp.id\n" +
					"		LEFT JOIN pr_trainee_class_course_package ptccp ON ptccp.fk_class_course_package_id = pclc.id\n" +
					"		AND ptccp.fk_trainee_id = pte.FK_TRAINEE_ID\n" +
					"		WHERE\n" +
					"			ptccp.id IS NULL\n" +
					"		AND pte.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'\n" +
					"	) a";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				num = resultSet.getInt(1);
			}
			List<String> updateSqlList = new ArrayList<String>();
			int second = num / maxSize + 1;
			System.out.println("总记录为" + num + "条，将分" + second + "次执行");
			List<String> sqlList = new ArrayList<>();
			for (int i = 0; i < second; i++) {
				System.out.println("正在执行第" + i + "次批量计算");
				sql = "SELECT DISTINCT\n" +
						"			pte.FK_TRAINEE_ID AS traineeId,\n" +
						"			pclc.id AS classPacId,\n" +
						"			pcp.id AS pacId\n" +
						"		FROM\n" +
						"			pe_tch_elective pte\n" +
						"		JOIN pe_open_course poc ON poc.ID = pte.FK_OPENCOURSE_ID\n" +
						"		JOIN pr_coursepackage_course pcc ON pcc.fk_tchcourse = poc.FK_TCH_COURSE\n" +
						"		JOIN pe_course_package pcp ON pcp.id = pcc.fk_coursepackage\n" +
						"		JOIN pr_class_coursepackage pclc ON pclc.fk_class = poc.FK_TRAIN_CLASS\n" +
						"		AND pclc.fk_coursepackage = pcp.id\n" +
						"		LEFT JOIN pr_trainee_class_course_package ptccp ON ptccp.fk_class_course_package_id = pclc.id\n" +
						"		AND ptccp.fk_trainee_id = pte.FK_TRAINEE_ID\n" +
						"		WHERE\n" +
						"			ptccp.id IS NULL\n" +
						"		AND pte.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' limit " + (i * maxSize) + "," + maxSize;
				statement = conn2.createStatement();
				resultSet = statement.executeQuery(sql);
				Map<String, String> codeAndIdMap = new HashMap<String, String>(); // 存放新平台的code和id的对应关系
				List<String> codeList = new ArrayList<String>();
				while (resultSet.next()) {
					String traineeId = resultSet.getString(1);
					String classCoursePacId = resultSet.getString(2);
					String coursePacId = resultSet.getString(3);
					StringBuffer sb = new StringBuffer();
					sb.append("INSERT INTO `pr_trainee_class_course_package` ( ");
					sb.append(" 	`id`,                                       ");
					sb.append(" 	`fk_trainee_id`,                            ");
					sb.append(" 	`fk_class_course_package_id`,               ");
					sb.append(" 	`fk_course_package_id`,                     ");
					sb.append(" 	`study_percent`,                     ");
					sb.append(" 	`complete_date`,                     ");
					sb.append(" 	`create_date`,                              ");
					sb.append(" 	`modify_date`,                              ");
					sb.append(" 	`flag_apply_status`,                       ");
					sb.append(" 	`fk_site_id`                                ");
					sb.append(" ) VALUES (                                      ");
					sb.append(" 		'" + MyUtils.uuid() + "', ");
					sb.append(" 		'" + traineeId + "',     ");
					sb.append(" 		'" + classCoursePacId + "',     ");
					sb.append(" 		'" + coursePacId + "',     ");
					sb.append(" 		null,     ");
					sb.append(" 		null,     ");
					sb.append(" 		NOW(),   ");
					sb.append(" 		NOW(),  ");
					sb.append(" 		'" + YiaiAddCreditApplyGeneralSql.flagIsNotValid + "',  ");
					sb.append(" 		'" + YiaiAddCreditApplyGeneralSql.siteId + "'      ");
					sb.append(" 	);");
					sqlList.add(sb.toString());
				}
				System.out.println("新平台查询完毕");

			}
			String path = "E:/myJava/yiaiSql/20170703/checkTraineePackage.sql";
			MyUtils.outputList(sqlList, path);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != statement) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (null != conn) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(result);
		System.out.println("*****************************END*************************");
		long end = System.currentTimeMillis();
		System.out.println("花费时间：" + (end-begin)/1000 + "秒");
		System.exit(0);
	}

	/**
	 * 获得ssh链接
	 */
	public static void go() {
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(sshusername, sshhost, sshport); // 端口账号
			session.setPassword(sshPassword);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			System.out.println(session.getServerVersion()); // 这里打印SSH服务器版本信息
			int assinged_port = session.setPortForwardingL(lport, host, rport);// 端口映射
																				// 转发
			System.out.println("localhost:" + assinged_port); // 端口映射 转发
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得ssh2链接
	 */
	public static void go2() {
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(sshusername, sshhost, sshport); // 端口账号
			session.setPassword(sshPassword);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			System.out.println(session.getServerVersion()); // 这里打印SSH服务器版本信息
			int assinged_port2 = session.setPortForwardingL(lport2, host2, rport2);// 端口映射
			System.out.println("localhost2:" + assinged_port2); // 端口映射 转发
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// 2、创建连接
		Connection conn = null;
		Connection conn2 = null;
		try {
			go();
			go2();
			// 映射到本地的服务
			conn = DriverManager.getConnection("jdbc:mysql://localhost:" + lport + "/" + dbName, dbUser, dbPwd);
			conn2 = DriverManager.getConnection("jdbc:mysql://localhost:" + lport2 + "/" + dbName2, dbUser2, dbPwd2);
			System.out.println("获取链接成功！");
			// getData(conn);
			compareData(conn, conn2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
