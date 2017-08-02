package whaty.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 * 
 * @author 董得超
 */
public class SshMysqlChangeCoursePackageValid {
	static String sshhost = "210.14.140.85"; // 远程服务器地址
	static String sshusername = "readonly"; // 服务器用户名
	static String sshPassword = "56Q8XNhu3x"; // 服务器密码
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
			// 读取正确的课程包，并输出
			String path = "E:/myJava/yiaiSql/20170616/correctClassPackageInfo.txt";
			List<String> correctClassPackageInfoList = new ArrayList<>();
			statement = conn2.createStatement();
			sql = "select a1 from t1";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String packagename = resultSet.getString(1).trim();
				if (!correctClassPackageInfoList.contains(packagename)) {
					correctClassPackageInfoList.add(packagename);
				}
			}
			MyUtils.outputList(correctClassPackageInfoList, path);
			System.out.println("正确的课程包数量为" + correctClassPackageInfoList.size());
			
			// 读取某个班级下的课程包，并输出
			String classId = "ff8080815a27e1a4015a353967cf0037";
			List<String> classPackageInfoList = new ArrayList<>();
			statement = conn2.createStatement();
			sql = "SELECT \n" +
					"	c.`id`,\n" +
					"	p.`name`\n" +
					"FROM\n" +
					"	pr_class_coursepackage c\n" +
					"JOIN pe_course_package p ON p.id = c.fk_coursepackage\n" +
					"WHERE\n" +
					"	c.fk_class = '" + classId + "' and p.`year` = '2017'\n" +
					"AND c.fk_site_id = 'ff80808155da5b850155dddbec9404c9'";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String pccId = resultSet.getString(1);
				String packagename = resultSet.getString(2).trim();
				if (!classPackageInfoList.contains(packagename) && !correctClassPackageInfoList.contains(packagename)) {
					classPackageInfoList.add(pccId);
				}
			}
			path = "E:/myJava/yiaiSql/20170616/classPackageInfo.txt";
			MyUtils.outputList(classPackageInfoList, path);
			System.out.println("新平台班级" + classId + "下应该删除的的课程包数量为" + classPackageInfoList.size());
			
			// 按照课程包名称进行处理
			List<String> eletiveIdList = new ArrayList<>(); // 应该删除的选课id
			List<String> deleteSqlList = new ArrayList<>(); // sql
			int hasOpenCourse = 0; // 有学员选课的开课数
			int openCourseCount = 0; // 应该删除的开课数
			num = classPackageInfoList.size();
			int second = num / maxSize + 1;
			System.out.println("总记录为" + num + "条，将分" + second + "次执行");
			for (int i = 0; i < second; i++) {
				System.out.println("正在执行第" + i + "次批量计算");
				int start = i * maxSize;
				int end = (i + 1) * maxSize;
				end = Math.min(end, num);
				for (int j = start; j < end; j++) {
					String pccId = classPackageInfoList.get(j);
					// 获取开课Id
					System.out.println("获取开课Id");
					sql = "SELECT DISTINCT\n" +
							"	poc.ID\n" +
							"FROM\n" +
							"	pr_class_coursepackage pcc\n" +
							"JOIN pr_coursepackage_course pcc2 ON pcc2.fk_coursepackage = pcc.fk_coursepackage\n" +
							"JOIN pe_open_course poc ON poc.FK_TRAIN_CLASS = pcc.fk_class\n" +
							"AND poc.FK_TCH_COURSE = pcc2.fk_tchcourse\n" +
							"WHERE\n" +
							"	pcc.id = '" + pccId + "'";
					statement = conn2.createStatement();
					resultSet = statement.executeQuery(sql);
					List<String> pocIdList = new ArrayList<String>();
					while (resultSet.next()) {
						String pocId = resultSet.getString(1);
						pocIdList.add(pocId);
					}
					openCourseCount += pocIdList.size();
					
					// 查询是否有学生选了这个开课
					String conditions = MyUtils.list2Str(pocIdList, "','");
					sql = "SELECT\n" +
							"	pte.ID,\n" +
							"	pte.FK_OPENCOURSE_ID,\n" +
							"	pte.FK_TRAINEE_ID\n" +
							"FROM\n" +
							"	pe_tch_elective pte\n" +
							"WHERE\n" +
							"	pte.FK_OPENCOURSE_ID in ('" + conditions + "')";
					statement = conn2.createStatement();
					resultSet = statement.executeQuery(sql);
					List<String> openCourseIdList = new ArrayList<>();
					while (resultSet.next()) {
						String pteId = resultSet.getString(1);
						String openCourseId = resultSet.getString(2);
						String traineeId = resultSet.getString(3);
						if (!openCourseIdList.contains(openCourseId)) {
							openCourseIdList.add(openCourseId);
						}
						System.out.println("有学生选了不该选的开课：pteId=" + pteId);
						eletiveIdList.add(pteId);
					}
					hasOpenCourse += openCourseIdList.size();
					
					// 生成删除语句
					for (String pocId : pocIdList) {
						deleteSqlList.add("delete from pe_open_course where id='" + pocId + "';"); // 删除开课
					}
					deleteSqlList.add("delete from pr_class_coursepackage where id='" + pccId + "';"); // 删除班级和课程包的对应关系
				}
			}
			System.out.println("应该删除" + num + "个课程包，包含" + openCourseCount + "个开课，有" + hasOpenCourse + "个开课被学员选课了");
			String sqlPath = "E:/myJava/yiaiSql/20170616/deleteClassPackage.sql";
			MyUtils.outputList(deleteSqlList, sqlPath);
			
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

}
