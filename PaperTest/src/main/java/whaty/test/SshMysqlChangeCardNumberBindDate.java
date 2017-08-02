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
public class SshMysqlChangeCardNumberBindDate {
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
	


	/**
	 * 比较两个数据源的内容
	 */
	public static void compareData(Connection conn, Connection conn2) {
		int maxSize = 1000;
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
					"	count(*)\n" +
					"FROM\n" +
					"	pe_card_number n\n" +
					"WHERE\n" +
					"	n.ACTIVATIONDATE IS NULL\n" +
					"AND FK_SSO_USER_ID IS NOT NULL\n" +
					"AND FK_SSO_USER_ID != ''\n" +
					"AND n.CREATEDATE > '2017-06-14'\n" +
					"AND n.NUMBER != 'ERDS'\n" +
					"AND n.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				num = resultSet.getInt(1);
			}
			List<String> updateSqlList = new ArrayList<String>();
			int second = num / maxSize + 1;
			System.out.println("总记录为" + num + "条，将分" + second + "次执行");
			for (int i = 0; i < second; i++) {
				System.out.println("正在执行第" + i + "次批量计算");
				sql = "SELECT\n" +
					"	n.ID,\n" +
					"	n.`CODE`\n" +
					"FROM\n" +
					"	pe_card_number n\n" +
					"WHERE\n" +
					"	n.ACTIVATIONDATE IS NULL\n" +
					"AND FK_SSO_USER_ID IS NOT NULL\n" +
					"AND FK_SSO_USER_ID != ''\n" +
					"AND n.CREATEDATE > '2017-06-01'\n" +
					"AND n.NUMBER != 'ERDS'\n" +
					"AND n.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' limit " + (i * maxSize) + "," + maxSize;
				statement = conn2.createStatement();
				resultSet = statement.executeQuery(sql);
				Map<String, String> codeAndIdMap = new HashMap<String, String>(); // 存放新平台的code和id的对应关系
				List<String> codeList = new ArrayList<String>();
				while (resultSet.next()) {
					String cardId = resultSet.getString(1);
					String cardCode = resultSet.getString(2);
					codeAndIdMap.put(cardCode, cardId);
					codeList.add(cardCode);
				}
				System.out.println("新平台查询完毕");
				
				String conditions = MyUtils.list2Str(codeList, "','");
				sql = "SELECT\n" +
						"	c.card_number,\n" +
						"	c.active_time\n" +
						"FROM\n" +
						"	mdl_below_card c\n" +
						"WHERE\n" +
						"	c.card_number IN ('"+conditions+"')";
				statement = conn.createStatement();
				resultSet = statement.executeQuery(sql);
				Map<String, String> codeAndTime = new HashMap<String, String>(); // 存放旧平台的code和activetime的对应关系
				while (resultSet.next()) {
					String cardCode = resultSet.getString(1);
					String activeTime = resultSet.getString(2);
					codeAndTime.put(cardCode, activeTime);
				}
				System.out.println("原平台查询完毕");
				
				
				for (Entry<String, String> entry : codeAndIdMap.entrySet()) {
					String cardCode = entry.getKey();
					String cardId = entry.getValue();
					String activetime = codeAndTime.get(cardCode);
					if (activetime != null) {
						updateSqlList.add("update pe_card_number n set n.ACTIVATIONDATE='"+activetime+"' where n.ID='"+cardId+"';");
					}
				}
			}
			String path = "E:/myJava/yiaiSql/20170614/updateCardBindDate.sql";
			MyUtils.outputList(updateSqlList, path);
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
