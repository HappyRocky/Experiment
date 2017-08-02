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
public class SshMysqlChangeCardValidation {
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
			compareData(conn, conn2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 比较两个数据源的内容
	 */
	public static void compareData(Connection conn, Connection conn2) {
		int maxSize = 10000;
		System.out.println("*****************************BEGIN*************************");
		Statement statement = null;
		ResultSet resultSet = null;
		int num = 0;
		String sql = "";
		String result = "";
		long begin = System.currentTimeMillis();
		try {
			// 读取需要修改的学员
			String path = "E:/myJava/yiaiSql/20170612/UsernameToUpdateAccount.sql";
			List<String> lineList = MyUtils.readFile(path);
			List<String> loginIdList = new ArrayList<>();
			for (int i = 0; i < lineList.size(); i++) {
				String line = lineList.get(i);
				loginIdList.add(line);
//				loginIdList.add(line.substring(0,line.indexOf(" ")).trim());
			}
			String loginIdCondition = MyUtils.list2Str(loginIdList, "','");
			
			statement = conn.createStatement();
			sql = "SELECT count(*)\n" +
					"FROM\n" +
					"	mdl_below_card c\n" +
					"JOIN mdl_user u\n" +
					"WHERE\n" +
					"	c.user_id = u.id\n" +
					"AND c.active_time < '2017-06-07 12:00:00'\n" +
					"AND c.active_time > '2016-01-01'\n" +
					"AND c.category BETWEEN 1\n" +
					"AND 10\n" +
					"and u.username in ('"+loginIdCondition+"')";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				num = resultSet.getInt(1);
			}
			List<String> outputDiffList = new ArrayList<>();
			if (num >= maxSize) {
				System.out.println("数量超过" + maxSize + "，需编码");
			} else {
				statement = conn.createStatement();

				// 总学分
				sql = "SELECT\n" +
						"	u.username,\n" +
						"	c.card_number,\n" +
						"	c.active\n" +
						"FROM\n" +
						"	mdl_below_card c\n" +
						"JOIN mdl_user u\n" +
						"WHERE\n" +
						"	c.user_id = u.id\n" +
						"AND c.active_time < '2017-06-07 12:00:00'\n" +
						"AND c.active_time > '2016-01-01'\n" +
						"AND c.category BETWEEN 1\n" +
						"AND 10\n" +
						"and u.username in ('"+loginIdCondition+"')";
				resultSet = statement.executeQuery(sql);
				List<String> updateSql = new ArrayList<>();
				
				while (resultSet.next()) {
					String username = resultSet.getString(1).trim();
					String code = resultSet.getString(2);
					String active = resultSet.getString(3);
					if (username.startsWith("chaorg")) {
						continue;
					}
					
					// 查询新平台的有效性
					sql = "SELECT\n" +
							"	n.ID,\n" +
							"	n.FLAG_ISACTIVATION\n" +
							"FROM\n" +
							"	pe_trainee pt\n" +
							"JOIN pe_card_number n ON n.FK_SSO_USER_ID = pt.ID\n" +
							"AND n.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'\n" +
							"WHERE\n" +
							"	pt.LOGIN_ID ='"+username+"' and n.`CODE`='"+code+"'";
					statement = conn2.createStatement();
					ResultSet resultSet1 = statement.executeQuery(sql);
					while (resultSet1.next()) {
						String cardId = resultSet1.getString(1);
						String isActivation = resultSet1.getString(2);
						
						// 判断有效性是否一致，并生成sql
						if ("99".equals(active) && FlagIsActivation_yes.equals(isActivation)) { // 原无效，新有效
							updateSql.add("update pe_card_number n set n.FLAG_ISACTIVATION='"+FlagIsActivation_no+"' where n.ID='"+cardId+"';");
						} else if (!"99".equals(active) && FlagIsActivation_no.equals(isActivation)) { // 原有效，新无效
							updateSql.add("update pe_card_number n set n.FLAG_ISACTIVATION='"+FlagIsActivation_yes+"' where n.ID='"+cardId+"';");
						}
					}
				}
				
				path = "E:/myJava/yiaiSql/20170612/updateCardValidation.sql";
				MyUtils.outputList(updateSql, path);
			}
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
	 * 获取数据信息
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	private static void getData(Connection conn) throws SQLException {
		System.out.println("*****************************BEGIN*************************");
		Statement statement = null;
		ResultSet resultSet = null;
		ResultSet resultSet2 = null;
		int num = 0;
		try {
			statement = conn.createStatement();
			resultSet = statement.executeQuery(" select count(1) from ucenter_user s where s.siteId = '10315' ");
			while (resultSet.next()) {
				num = resultSet.getInt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != statement) {
				statement.close();
			}
			if (null != conn) {
				conn.close();
			}
		}
		System.out.println("*****************************END*************************");
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

	/**
	 * 写入需要删除的数据
	 */
	private static void writeTxt() {
		try {
			File f = new File(filePath);
			if (f.exists()) {
				System.out.println("要写入的目标文件存在：" + filePath);
				f.createNewFile();
				System.out.println("重新创建文件成功");
			} else {
				System.out.println("要写入的目标文件不存在存在：" + filePath);
				f.createNewFile();
				System.out.println("创建文件成功");
			}
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < tempList.size(); i++) {
				buf.append(tempList.get(i) + "\n");
			}
			BufferedWriter output = new BufferedWriter(new FileWriter(f));
			output.write(buf.toString());
			output.close();
			System.out.println("--写入目标文件完成--");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
