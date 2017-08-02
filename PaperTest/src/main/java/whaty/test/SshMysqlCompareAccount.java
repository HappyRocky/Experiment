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
public class SshMysqlCompareAccount {
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
		int maxSize = 10000;
		System.out.println("*****************************BEGIN*************************");
		Statement statement = null;
		ResultSet resultSet = null;
		int num = 0;
		String sql = "";
		String result = "";
		long begin = System.currentTimeMillis();
		try {
			statement = conn.createStatement();
			sql = "select count(*) from (SELECT\n" +
					"	u.username,\n" +
					"	c.category,\n" +
					"	c.active,\n" +
					"	COUNT(c.id)\n" +
					"FROM\n" +
					"	mdl_below_card c\n" +
					"JOIN mdl_user u\n" +
					"WHERE\n" +
					"	c.user_id = u.id\n" +
					"AND c.active != 99\n" +
					"and c.active_time < '" + endDate + "'  \n" +
					"and c.active_time > '" + startDate + "'  \n" +
					"AND c.category BETWEEN 1\n" +
					"AND 10\n" +
					"GROUP BY u.username,c.category) t";
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				num = resultSet.getInt(1);
			}
			List<String> outputDiffList = new ArrayList<>();
			if (num >= maxSize) {
			} else {
				// 总学分
				sql = "SELECT\n" +
						"	u.username,\n" +
						"	c.category,\n" +
						"	c.active,\n" +
						"	COUNT(c.id)\n" +
						"FROM\n" +
						"	mdl_below_card c\n" +
						"JOIN mdl_user u\n" +
						"WHERE\n" +
						"	c.user_id = u.id\n" +
//						"AND c.active != 99\n" +
						"and c.active_time < '" + endDate + "'  \n" +
						"and c.active_time > '" + startDate + "'  \n" +
						"AND c.category BETWEEN 1\n" +
						"AND 10\n" +
						"GROUP BY u.username,c.category,c.active ";
				statement = conn.createStatement();
				resultSet = statement.executeQuery(sql);
				String conditions = "";
				
				Map<String, Integer[]> accountMap = new HashMap<String, Integer[]>();
				Map<String, Integer[]> accountMap2 = new HashMap<String, Integer[]>();
				Map<String, Integer[]> accountWebtrnMap = new HashMap<String, Integer[]>();
				Map<String, Integer[]> accountWebtrnMap2 = new HashMap<String, Integer[]>();
				
				while (resultSet.next()) {
					String username = resultSet.getString(1).trim();
					if (username.startsWith("chaorg")) {
						continue;
					}
					
					String category = resultSet.getString(2);
					String active = resultSet.getString(3);
					int count = resultSet.getString(4) == null ? 0 : Integer.valueOf(resultSet.getString(4)) ;

					// 计算学分
					int score1 = 0;
					int score2 = 0;
					int scoreCommon = 0;
					if (!"99".equals(active)) { // 只计算有效的卡片，无效的就是0
						if (category.equals("1")) {
							score1 = count * 10;
							score2 = count * 5;
						} else if (category.equals("2")) {
							score1 = count * 5;
						} else if (category.equals("3")) {
							score2 = count * 5;
						} else if (category.equals("4")) {
							score2 = count * 10;
						} else if (category.equals("5")) {
							score1 = count * 10;
						} else if (category.equals("6")) {
							score1 = count * 10;
							score2 = count * 10;
						} else if (category.equals("7")) {
							score1 = count * 5;
							score2 = count * 5;
						} else if (category.equals("8")) {
							score2 = count * 15;
						} else if (category.equals("9")) {
							score1 = count * 15;
						} else if (category.equals("10")) {
							scoreCommon = count * 20;
						} 
					}
					
					if (accountMap.containsKey(username)) {
						Integer[] accounts = accountMap.get(username);
						accounts[0] += score1;
						accounts[1] += score2;
						accounts[2] += scoreCommon;
					} else {
						Integer[] accounts = {score1,score2,scoreCommon};
						accountMap.put(username, accounts);
						conditions += "'" + username + "',";
					}
					if (!accountMap2.containsKey(username)) {
						Integer[] accounts = {0,0,0};
						accountMap2.put(username, accounts);
					}
					if (!accountWebtrnMap.containsKey(username)) {
						Integer[] accounts = {0,0,0};
						accountWebtrnMap.put(username, accounts);
					}
					if (!accountWebtrnMap2.containsKey(username)) {
						Integer[] accounts = {0,0,0};
						accountWebtrnMap2.put(username, accounts);
					}
					
				}
				
				if (conditions.endsWith(",")) {
					conditions = conditions.substring(0, conditions.length() - 1);
				}
				
				// 已申请学分
				System.out.println("计算已申请学分");
				StringBuffer sb = new StringBuffer();
				sb.append(" SELECT   ");
				sb.append(" 	MU.username AS loginId,  ");
				
				sb.append(" 	MCC. NAME AS category_name,     "); // 1
				sb.append(" 	MCC.cme_credit_type_id AS type, ");
				sb.append(" 	floor(MCC.credit) AS credit,     ");
				
				sb.append(" 	from_unixtime(MJO.timecreated) ");
				sb.append(" FROM   ");
				sb.append(" 	mdl_jscme_order MJO  ");
				sb.append(" LEFT JOIN mdl_user MU ON MJO.userid = MU.id  ");
				sb.append(" LEFT JOIN mdl_user_info_data MUID ON MUID.fieldid = 11 ");
				sb.append(" AND MU.id = MUID.userid  ");
				sb.append(" LEFT JOIN mdl_course_categories MCC ON MCC.id = MJO.category ");
				sb.append(" AND MCC.`is_cme` = 1                ");
				sb.append(" AND MCC.`credit` IS NOT NULL        ");
				sb.append(" WHERE ");
				sb.append(" 	MJO. STATUS = 2 ");
				sb.append(" AND MJO.category > 0 and MJO.timecreated < UNIX_TIMESTAMP('" + endDate + "')");
				sb.append(" and MJO.timecreated > UNIX_TIMESTAMP('" + startDate + "')");
				sb.append(" and MU.username in ("+conditions+")");
				statement = conn.createStatement();
				resultSet = statement.executeQuery(sb.toString());
				while (resultSet.next()) {
					String username = resultSet.getString(1).trim();

					// 查询学分信息
					String categoryName = resultSet.getString(2);
					String type = resultSet.getString(3);
					String credit = resultSet.getString(4);

					if (categoryName == "") {
						System.out.println("课程包为空，不算作已申请学分：" + username);
						continue;
					}

					if (categoryName.equals("口腔种植新技术")) {
						System.out.println(categoryName);
						continue;
					}

					int applyScore1 = 0;
					int applyScore2 = 0;
					try {
						if (type.equals("1")) {
							applyScore1 = Integer.valueOf(credit);
						} else if (type.equals("2")) {
							applyScore2 = Integer.valueOf(credit);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (accountMap2.containsKey(username)) {
						Integer[] accounts = accountMap2.get(username);
						accounts[0] += applyScore1;
						accounts[1] += applyScore2;
					} else {
						Integer[] accounts = { applyScore1, applyScore2, 0 };
						accountMap2.put(username, accounts);
					}
				}
			
				System.out.println("查询医爱网数据完毕");
				
				statement = conn2.createStatement();
				sql = "SELECT\n" +
						"	pt.LOGIN_ID,\n" +
						"	IFNULL(SUM(n.CMESCORE1),0),\n" +
						"	IFNULL(SUM(n.CMESCORE2),0),\n" +
						"	IFNULL(SUM(n.common_score),0),\n" +
						"	IFNULL(SUM(n.CMESCORE1_USABLE),0),\n" +
						"	IFNULL(SUM(n.CMESCORE2_USABLE),0),\n" +
						"	IFNULL(SUM(n.common_score_usable),0)\n" +
						"FROM\n" +
						"	pe_trainee pt\n" +
						"join pe_card_number n ON n.FK_SSO_USER_ID=pt.ID\n" +
						"and n.FLAG_ISACTIVATION='190959545f7f11e69b44848f69e05bf0'\n" +
						"and n.FLAG_ISUSED='1902b34c5f7f11e69b44848f69e05bf0'\n" +
						"and n.FLAG_ISVALID='40288a962e9d9ac5012e9dd6b0aa0004'\n" +
						"and n.FK_CARD_TYPE_ID='ff808081576b2bfc01576b7442360003'\n" +
						"and n.VALIDDATE > NOW()\n" +
						"and n.ACTIVATIONDATE < '" + endDate + "'\n" +
						"and n.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'\n" +
						" where pt.LOGIN_ID in ("+conditions+")\n" +
						"GROUP BY pt.ID ";
				resultSet = statement.executeQuery(sql);

				while (resultSet.next()) {
					String username = resultSet.getString(1).trim();
					int score1 = Integer.valueOf(resultSet.getString(2));
					int score2 = Integer.valueOf(resultSet.getString(3));
					int scoreCommon = Integer.valueOf(resultSet.getString(4));
					int score1_usable = Integer.valueOf(resultSet.getString(5));
					int score2_usable = Integer.valueOf(resultSet.getString(6));
					int scoreCommon_usable = Integer.valueOf(resultSet.getString(7));
					Integer[] a = {score1,score2,scoreCommon};
					accountWebtrnMap.put(username, a);
					Integer[] b = {score1_usable,score2_usable,scoreCommon_usable};
					accountWebtrnMap2.put(username, b);
				}
				System.out.println("查询webtrn数据完毕");
				
				// 输出
				List<String> outputList = new ArrayList<>();
				List<String> updateSqlList = new ArrayList<>();
				
				System.out.println("开始比对");
				for (Map.Entry<String, Integer[]> entry : accountMap.entrySet()) {
					String username = entry.getKey();
					Integer[] accounts = entry.getValue();
					Integer[] accounts2 = accountMap2.get(username);
					Integer[] accountsWebtrn = accountWebtrnMap.get(username);
					Integer[] accountsWebtrn2 = accountWebtrnMap2.get(username);
					
					// 原平台剩余的三种余额，新平台按照这个余额来校准
					int yiai_score1 = accounts[0] - accounts2[0]; // 一类剩余
					int yiai_score2 = accounts[1] - accounts2[1]; // 二类剩余
					int yiai_scoreCommon = accounts[2] - accounts2[2]; // 通用剩余
					
					// 新平台剩余
					int scoreLeftWebtrn1 = accountsWebtrn2[0];
					int scoreLeftWebtrn2 = accountsWebtrn2[1];
					int scoreLeftWebtrnCommon = accountsWebtrn2[2];
					
					// 处理通用学分
					if (yiai_score1 < 0) { // 一类为负的，说明额外扣除了通用学分
						yiai_scoreCommon = yiai_scoreCommon + yiai_score1;
						yiai_score1 = 0;
					}
					if (yiai_score2 < 0) { // 二类为负的，说明额外扣除了通用学分
						yiai_scoreCommon = yiai_scoreCommon + yiai_score2;
						yiai_score2 = 0;
					}
					
					List<String> list = new ArrayList<>();
					list.add(username);
					list.add(yiai_score1+"");
					list.add(scoreLeftWebtrn1+"");
					list.add(yiai_score2+"");
					list.add(scoreLeftWebtrn2+"");
					list.add(yiai_scoreCommon+"");
					list.add(scoreLeftWebtrnCommon+"");
					String log = MyUtils.list2Str(list, " ");
					
					// 判断余额是否一致
					if (yiai_score1 != scoreLeftWebtrn1 || yiai_score2 != scoreLeftWebtrn2 || yiai_scoreCommon != scoreLeftWebtrnCommon) {
						outputDiffList.add(log); 
					}
					
					// 修补学分
					if (yiai_score1 < 0 || yiai_score2 < 0 || yiai_scoreCommon < 0) {
						System.out.println("剩余余额为负,置为0：" + username + " " + yiai_score1 + " " + yiai_score2 + " " + yiai_scoreCommon);
						yiai_score1 = Math.max(yiai_score1, 0);
						yiai_score2 = Math.max(yiai_score1, 0);
						yiai_scoreCommon = Math.max(yiai_score1, 0);
					}
					if (yiai_score1 != scoreLeftWebtrn1 || yiai_score2 != scoreLeftWebtrn2 || yiai_scoreCommon != scoreLeftWebtrnCommon) {
						List<String> sqList = repairCardAccount(username, yiai_score1, yiai_score2, yiai_scoreCommon, conn2, endDate);
						if (CollectionUtils.isNotEmpty(sqList)) {
							updateSqlList.addAll(sqList);
						}
					}
					if (yiai_score1 + yiai_score2 + yiai_scoreCommon > 0) { // 剩余学分>0，放在输出列表的前面
						outputList.add(0,log);
					} else {
						outputList.add(log);
					}
				}
				String path = "E:/myJava/yiaiSql/20170703/compareAccountInfo_startFrom_0.txt";
				outputList.add(0, "username 原平台1类余额 新平台1类余额 原平台2类余额 新平台2类余额 原平台通用余额 新平台通用余额");
				MyUtils.outputList(outputList, path);
				String path2 = "E:/myJava/yiaiSql/20170703/compareAccountInfo_diff_all.txt";
				outputDiffList.add(0, "username 原平台1类余额 新平台1类余额 原平台2类余额 新平台2类余额 原平台通用余额 新平台通用余额");
				MyUtils.outputList(outputDiffList, path2);
				String path3 = "E:/myJava/yiaiSql/20170703/updateAccountSql.sql";
				MyUtils.outputList(updateSqlList, path3);
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
	 * 根据原平台数据，修复某个学员在新平台学分卡的余额，返回sql
	 * @param loginId
	 * @param yiai_score1 正确的一类余额
	 * @param yiai_score2 正确的二类余额
	 * @param yiai_scoreCommon 正确的通用余额
	 * @param conn
	 * @return
	 */
	public static List<String> repairCardAccount(String loginId, int yiai_score1, int yiai_score2, int yiai_scoreCommon, Connection conn, String endDate){
		List<String> resultList = new ArrayList<>();
		try {
			// 查询当前学员的有效卡片
			Statement statement = conn.createStatement();
			String sql = "SELECT\n" +
					"	n.id as cardNumberId,\n" +
					"	IFNULL(n.CMESCORE1, 0),	\n" +
					"	IFNULL(n.CMESCORE2, 0),\n" +
					"	IFNULL(n.common_score, 0)\n" +
					" FROM\n" +
					"	pe_trainee pt\n" +
					" JOIN pe_card_number n ON n.FK_SSO_USER_ID = pt.ID\n" +
					" AND n.FLAG_CARD_SOURCE = '3552e55194ff11e6be7b001e679d0a23'\n" +
					" AND n.FLAG_ISACTIVATION = '190959545f7f11e69b44848f69e05bf0'\n" +
					" AND n.FLAG_ISUSED = '1902b34c5f7f11e69b44848f69e05bf0'\n" +
					" AND n.FLAG_ISVALID = '40288a962e9d9ac5012e9dd6b0aa0004'\n" +
					" AND n.FK_CARD_TYPE_ID = 'ff808081576b2bfc01576b7442360003'\n" +
					" AND n.VALIDDATE > NOW()\n" +
					" and n.ACTIVATIONDATE < '" + endDate + "'\n" +
					" AND n.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'\n" +
					" WHERE\n" +
					"	pt.LOGIN_ID ='"+loginId+"' ORDER BY n.VALIDDATE desc";
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String cardNumberId = resultSet.getString(1);
				int max_score1 = Integer.valueOf(resultSet.getString(2));
				int max_score2 = Integer.valueOf(resultSet.getString(3));
				int max_scoreCommon = Integer.valueOf(resultSet.getString(4));
				
				// 这张卡片最终的学分值
				int new_score1 = 0;
				int new_score2 = 0;
				int new_scoreCommon = 0;

				// 开始补卡，把当前卡片的余额能补满就补满，如果还有剩余，则把任务交给下一张卡
				if (max_scoreCommon > 0) { // 通用卡
					if (yiai_scoreCommon > 0) { // 需要补通用学分
						new_scoreCommon = Math.min(yiai_scoreCommon, max_scoreCommon); // 取最小值：或者补满额，或者把要补的补完 
						yiai_scoreCommon -= new_scoreCommon;
					}
					String result = "update pe_card_number n set n.common_score_usable = " + new_scoreCommon
							+ "  where n.id='" + cardNumberId + "';";
					resultList.add(result);
				} else { // 一二类学分卡
					if (yiai_score1 > 0 && max_score1 > 0) { // 需要补一类学分，而且卡片一类总额大于0
						new_score1 = Math.min(yiai_score1, max_score1); // 取最小值，或者补满额，或者把要补的补完
						yiai_score1 -= new_score1; // 一类学分已补了new_score1
					}
					if (yiai_score2 > 0 && max_score2 > 0) { // 需要补2类学分，而且卡片2类总额大于0
						new_score2 = Math.min(yiai_score2, max_score2); // 取最小值：或者补满额，或者把要补的补完
						yiai_score2 -= new_score2; // 2类学分已补了new_score2
					}
					String result = "update pe_card_number n set n.CMESCORE1_USABLE=" + new_score1 + ",n.CMESCORE2_USABLE="
							+ new_score2 + " where n.id='" + cardNumberId + "'; ";
					resultList.add(result);
				}
			}
			if (yiai_score1 > 0 || yiai_score2 > 0 || yiai_scoreCommon > 0) { // 所有卡片补了一遍，还有剩余的需要补的学分
				System.out.println("[Error]：学员" + loginId + "所绑定的卡所允许的总额<需要补的学分（" + yiai_score1 + "," + yiai_score2
						+ "," + yiai_scoreCommon + "）");
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultList;
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
