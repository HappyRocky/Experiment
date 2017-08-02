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
 * 更新没有绑定过的卡片的可用/不可用
 * 
 * @author 董得超
 */
public class SshMysqlChangeCardValidWhereNoBind {
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

	static String filePath = "F:\\sql.sql"; // 生成待执行的SQL文件
	static List tempList = new ArrayList();
	
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
			String startDate = "2017-02-05";
			String endDate = "2017-07-03 12:00:00";
			compareData(conn, conn2, startDate, endDate);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 比较两个数据源的内容
	 */
	public static void compareData(Connection conn, Connection conn2, String startDate, String endDate) {
		int maxSize = 1000;
		System.out.println("*****************************BEGIN*************************");
		Statement statement = null;
		ResultSet resultSet = null;
		int num = 0;
		String sql = "";
		try {
			statement = conn2.createStatement();
			sql = "SELECT\n" +
					"	count(*)\n" +
					"FROM\n" +
					"	pe_card_number n\n" +
					"WHERE\n" +
					"	(\n" +
					"		n.FK_SSO_USER_ID IS NULL\n" +
					"		OR n.FK_SSO_USER_ID = ''\n" +
					"	)\n" +
					"AND n.FLAG_ISACTIVATION = '190d29945f7f11e69b44848f69e05bf0'";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				num = resultSet.getInt(1);
			}
			int second = num / maxSize + 1;
			System.out.println("总记录为" + num + "条，将分" + second + "次执行");
			for (int i = 0; i < second; i++) {
				int start = i * maxSize;
				int end = (i + 1) * maxSize;
				SshMysqlChangeCardValidWhereNoBindThread t = new SshMysqlChangeCardValidWhereNoBindThread(start,end,conn,conn2);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
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
