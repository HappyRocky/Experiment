package whaty.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 * 更新没有绑定过的卡片的可用/不可用
 * 
 * @author 董得超
 */
public class SshMysqlChangeCardValidWhereNoBindThread extends Thread{
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
	
	private int start;
	private int end;
	private Connection conn;
	private Connection conn2;
	
	public SshMysqlChangeCardValidWhereNoBindThread(int start, int end, Connection conn, Connection conn2) {
		this.start = start;
		this.end = end;
		this.conn = conn;
		this.conn2 = conn2;
	}
	
	public void run(){
		System.out.println("线程开启：" + start + "~" + end);
		String outPath = "E:/myJava/yiaiSql/20170705/udpateCardValid_" + start + "_" + end + ".sql";
		List<String> sqlList = new ArrayList<>();
		int updateCount = 0;
		int notUpdateCount = 0;
		Statement statement = null;
		ResultSet resultSet = null;
		int batch = (end - start) / 100;
		try {
			String sql = "SELECT\n" +
					"	n.id,n.`CODE`\n" +
					"FROM\n" +
					"	pe_card_number n\n" +
					"WHERE\n" +
					"	(\n" +
					"		n.FK_SSO_USER_ID IS NULL\n" +
					"		OR n.FK_SSO_USER_ID = ''\n" +
					"	)\n" +
					"AND n.FLAG_ISACTIVATION = '190d29945f7f11e69b44848f69e05bf0'  limit " + start + "," + (end - start);
			statement = conn2.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String cardId = resultSet.getString(1);
				String cardCode = resultSet.getString(2);
				
				sql = "SELECT\n" +
						"	c.active\n" +
						"FROM\n" +
						"	mdl_below_card c\n" +
						"WHERE\n" +
						"	c.card_number = '" + cardCode + "'";
				statement = conn.createStatement();
				ResultSet resultSet2 = statement.executeQuery(sql);
				while (resultSet2.next()) {
					String active = resultSet2.getString(1);
					if (!"99".equals(active)) {
						sqlList.add("update pe_card_number n set n.FLAG_ISACTIVATION='190959545f7f11e69b44848f69e05bf0' where n.id = '" + cardId + "';");
						updateCount++;
					} else {
						notUpdateCount++;
					}
				}
				if ((updateCount + notUpdateCount) % batch == 0) {
					System.out.println(start + "_" + end + "已完成了" + (updateCount + notUpdateCount) + "条，共" + (end - start) + "条");
				}
			}
			MyUtils.outputList(sqlList, outPath);
			System.out.println(start + "_" + end + " 数据处理完毕，需要更新" + updateCount + "条记录，不需要更新" + notUpdateCount + "条记录");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
