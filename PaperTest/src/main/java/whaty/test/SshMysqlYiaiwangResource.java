package whaty.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 */
@SuppressWarnings("all")
public class SshMysqlYiaiwangResource {
	static int lport = 3306;                  //本地端口  映射到本地的端口
	static String host = "119.254.4.60";    //远程MySQL服务器  
	static int rport = 3306;                 //远程MySQL服务端口 
	
	static String sshhost = "210.14.140.85";  //远程服务器地址
	static String sshusername = "readonly";       //服务器用户名
	static String sshPassword = "Y5TK4GScIv"; //服务器密码
	static int sshport = 22;
	public static Connection conn = null;
	
	static String filePath = "E:\\delsql.txt";
	static String logPath = "E:\\log.txt";
	static List delList;
	
	public static void main(String[] args) {
		String sql = "select * from study_materials limit 1";
		System.out.println(queryBySQL(sql).get(0)[0]);
	}
	
	public static synchronized Connection getConn(){
		try {
			if(conn != null ){
				return conn;
			}
			//1、加载驱动  
			Class.forName("com.mysql.jdbc.Driver");
			//2、创建连接  
			go();
			//映射到本地的服务
			conn = DriverManager.getConnection("jdbc:mysql://localhost:"+lport+"/resource", "whatyU@123", "whatyp@132");
		} catch (Exception e) {
		}
		return conn;
	}
	
	public static int executeBySQL(String  sql) {
	    Connection conn = getConn();
	    int i = 0;
	    PreparedStatement pstmt = null;
	    try {
	        pstmt = (PreparedStatement) conn.prepareStatement(sql);
	        i = pstmt.executeUpdate();
	        pstmt.close();
	        conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally{
	    	try {
				pstmt.close();
		        conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
	    }
	    return i;
	}
	
	public static List<Object[]> queryBySQL(String  sql) {
	    Connection conn = getConn();
	    List<Object[]> list = new ArrayList<Object[]>(); 
	    PreparedStatement pstmt =null;
	    ResultSet resultSet = null;
	    try {
	    	pstmt = (PreparedStatement)conn.prepareStatement(sql);;
	    	resultSet = pstmt.executeQuery();
	    	int col = resultSet.getMetaData().getColumnCount();
	    	while (resultSet.next()) {
	    		Object[] obj = new Object[col]; 
	    		for(int i = 1; i <= col; i++){
	    			obj[i-1]=resultSet.getObject(i);
	    		}
	    		list.add(obj);
			}
	    } catch (SQLException e) {
	    	System.out.println("error sql:" + sql);
	        e.printStackTrace();
	    }finally{
	    	try {
				resultSet.close();
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
	    }
	    return list;
	}
	
	public static void executeBatchBySQL(List<String> list) {
		Connection conn = getConn();
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			for (int i = 0; i < list.size(); i++) {
				pstmt =  conn.prepareStatement(list.get(i));
				pstmt.addBatch(list.get(i));
			}
			// 执行批量执行
			pstmt.executeBatch();
			pstmt.close();
			conn.commit();
			conn.setAutoCommit(true);
			conn.close();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
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
			//System.out.println(session.getServerVersion());	//这里打印SSH服务器版本信息  
			int assinged_port = session.setPortForwardingL(lport, host, rport);//端口映射 转发
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
}
