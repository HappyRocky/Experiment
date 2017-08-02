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
import java.util.List;
import java.util.Map;

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
public class SshMysqlCompareApply {
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
			String time = "2017-07-03 12:00:00";
			compareData(conn, conn2, time);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 比较两个数据源的内容
	 */
	public static void compareData(Connection conn, Connection conn2, String time) {
		int maxSize = 1000;
		System.out.println("*****************************BEGIN*************************");
		Statement statement = null;
		ResultSet resultSet = null;
		int num = 0;
		String sql = "";
		String result = "";
		long begin = System.currentTimeMillis();
		try {
			statement = conn.createStatement();
			sql = "select count(*) from ("
					+ "SELECT 	count(*) as counts FROM 	"
					+ "mdl_jscme_order o JOIN mdl_user mu ON mu.id = o.userid WHERE 	"
					+ "o.timecreated < UNIX_TIMESTAMP('"+time+"') AND o.category > 0 AND o. STATUS = 2 GROUP BY 	mu.id"
					+ ") t ";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				num = resultSet.getInt(1);
			}
			List<String> outputDiffList = new ArrayList<>();
			int second = num / maxSize + 1;
			System.out.println("总记录为" + num + "条，将分" + second + "次执行");
			
			
			// 查询用app申请的记录
			List<List<String>> appList = MyUtils.queryInfoByWebtrn("E:/myJava/yiaiSql/20170703/yiaiBatchApply.txt", "__", "");
			String noPcaIdCondition = "";
			for (List<String> list : appList) {
				noPcaIdCondition += "'" + list.get(0) + "',";
			}
			if (noPcaIdCondition.endsWith(",")) {
				noPcaIdCondition = noPcaIdCondition.substring(0, noPcaIdCondition.length() - 1);
			}
			
			for (int i = 0; i < second; i++) {
				System.out.println("正在执行第" + i + "次批量计算");
				statement = conn.createStatement();
				StringBuffer sb2 = new StringBuffer();
				sb2 = new StringBuffer();
				sb2.append("  SELECT                                                    ");
				sb2.append("  	mu.username,                                           ");
				sb2.append("  	count(o.id) as applyCount                                       ");
				sb2.append("  FROM                                                      ");
				sb2.append("  	mdl_jscme_order o                                      ");
				sb2.append("  JOIN mdl_user mu ON mu.id = o.userid                      ");
				sb2.append("  WHERE                                                     ");
				sb2.append("  	o.timecreated < UNIX_TIMESTAMP('" + time + "')  ");
				sb2.append("  AND o.category > 0                                        ");
				sb2.append("  AND o. STATUS = 2                                         ");
				sb2.append("  GROUP BY                                                  ");
				sb2.append("  	mu.id                                                  ");
				sb2.append("  ORDER BY                                                  ");
				sb2.append("  	applyCount    DESC  limit " + (i * maxSize) + "," + maxSize);
				sql = sb2.toString();
				resultSet = statement.executeQuery(sql);
				String conditions = "";
				Map<String, String> map = new LinkedMap();
				Map<String, String> map2 = new LinkedMap();
				List<String> tempList = new ArrayList<String>();
				while (resultSet.next()) {
					String username = resultSet.getString(1).trim();
					String count = resultSet.getString(2);
					if (username.startsWith("chaorg")) {
						continue;
					}
					conditions += "'" + username + "',";
					map.put(username, count);
				}
				if (conditions.endsWith(",")) {
					conditions = conditions.substring(0, conditions.length() - 1);
				}

				System.out.println("查询医爱网数据完毕");

				statement = conn2.createStatement();
				StringBuffer sb = new StringBuffer();
				sb.append(" SELECT                                        ");
				sb.append(" 	pt.LOGIN_ID as loginId,                   ");
				sb.append(" 	COUNT(pca.id) as applyCount               ");
				sb.append(" FROM                                          ");
				sb.append(" 	pe_credit_apply pca                       ");
				sb.append(" JOIN pe_trainee pt ON pt.ID=pca.fk_trainee_id ");
				sb.append(" WHERE                                         ");
				sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'         ");
				sb.append(" AND pca.flag_isvalid = '40288a962e9d9ac5012e9dd6b0aa0004' ");
				sb.append(" AND pca.create_date < '" + time + "'          ");
				sb.append(" AND pt.LOGIN_ID in (" + conditions + ")          ");
				if (noPcaIdCondition != "") {
					sb.append(" AND pca.id not in (" + noPcaIdCondition + ") ");
				}
				sb.append(" GROUP BY                                      ");
				sb.append(" 	pca.fk_trainee_id                         ");
				sb.append(" ORDER BY applyCount DESC  ");
				sql = sb.toString();
				resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					String username = resultSet.getString(1).trim();
					String count = resultSet.getString(2);
					map2.put(username, count);
				}
				System.out.println("查询webtrn数据完毕");

				// 输出
				List<String> outputList = new ArrayList<>();

				for (Map.Entry<String, String> entry : map.entrySet()) {
					String username = entry.getKey();
					String count = StringUtils.defaultIfBlank(entry.getValue(), "0");
					String count2 = StringUtils.defaultIfBlank(map2.get(username), "0");
					int diff = -9999;
					try {
						diff = Integer.valueOf(count) - Integer.valueOf(count2);
					} catch (Exception e) {
						System.out.println("字符串转整数错误：" + count + " " + count2);
						e.printStackTrace();
					}
					if (diff != 0) {
						outputDiffList.add(username + " " + count + " " + count2 + " " + diff);
					}
					outputList.add(username + " " + count + " " + count2 + " " + diff);
				}
				String path = "E:/myJava/yiaiSql/20170703/compareApplyInfo_startFrom_" + (i * maxSize) + ".txt";
				MyUtils.outputList(outputList, path);
			}
			String path = "E:/myJava/yiaiSql/20170703/compareApplyInfo_diff_all.txt";
			MyUtils.outputList(outputDiffList, path);
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
