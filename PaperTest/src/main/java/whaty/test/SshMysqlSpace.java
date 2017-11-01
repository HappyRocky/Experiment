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
public class SshMysqlSpace {
	static int lport = 3307;                  //本地端口  映射到本地的端口
	static String host = "192.168.148.52";    //远程MySQL服务器  
	static int rport = 13332;                 //远程MySQL服务端口 
	
	static String sshhost = "210.14.140.85";  //远程服务器地址
	static String sshusername = "tyxl";       //服务器用户名
	static String sshPassword = "5eqbFz7Q0t"; //服务器密码
	static int sshport = 22;
	public static Connection conn = null;
	
	static String filePath = "E:\\delsql.txt";
	static String logPath = "E:\\log.txt";
	static List delList;
	
	public static void main(String[] args) {
		String sql = "select count(*) from pe_student s where s.LOGIN_ID like 'erds2017@%' and s.SITE_CODE='yiai';";
		List<Object[]> result = getBySQL(sql);
		System.out.println("运行结果=" + result.get(0)[0] + "，sql=" + sql);
		
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
			conn = DriverManager.getConnection("jdbc:mysql://localhost:"+lport+"/learning_space_2_zhw", "tylearning_user", "S4XwPbzWQDfq");
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
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally{
	    	try {
				pstmt.close();
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
	    	resultSet.close();
	    	pstmt.close();
	    } catch (SQLException e) {
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
	
	public static List<Object[]> getBySQL(String  sql) {
		return queryBySQL(sql);
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
	
	

	/**
	 * 写入需要删除的数据
	 */
	public static void writeTxt(String txt,String type,String filename) {
		try {
			File f =null;
			if(type.equals("log")){
				f = new File("E:\\exam_log_"+filename+".txt");
			}else{
				f = new File("E:\\exam_insert_"+filename+".txt");
			}
			if (f.exists()) {
			} else {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f, true);  
	        fw.write(txt+"\n");  
	        fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 写入需要删除的数据
	 */
	public static void writeTxt(String txt,String type,String filename,int num) {
		try {
			File f =null;
			if(type.equals("log")){
				f = new File("E:\\log_"+filename+"_"+num+".sql");
			}else{
				f = new File("E:\\insert_"+filename+"_"+num+".sql");
			}
			if (f.exists()) {
			} else {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f, true);  
	        fw.write(txt+"\n");  
	        fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}
