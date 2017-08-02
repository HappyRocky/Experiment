package whaty.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 */
@SuppressWarnings("all")
public class SshMysql {
	static int lport = 3308;                  //本地端口  映射到本地的端口
	static String host = "192.168.148.52";    //远程MySQL服务器  
	static int rport = 13332;                 //远程MySQL服务端口 
	
	static String sshhost = "210.14.140.85";  //远程服务器地址
	static String sshusername = "tyxl";       //服务器用户名
	static String sshPassword = "15eQ34Auib"; //服务器密码
	static int sshport = 22;
	
	
	static String filePath = "E:\\delsql.txt";
	static String logPath = "E:\\log.txt";
	static List delList;
	
	public static void main(String[] args) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String date = df.format(new Date());
		System.out.println(date);
		/*String sql = "select id from scorm_stu_course  limit 10 ";
		List<Object[]> list = queryBySQL(sql);
		System.out.println(list.size());
		for(int i=0;i<list.size();i++){
			Object [] obj =list.get(i);
			for(int j=0;j<obj.length;j++){
				System.out.print(String.valueOf(obj[j])+"     ");
			}
			System.out.println("");
		}*/
	} 

	public static Connection getConn(){
		try {
			//1、加载驱动  
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//2、创建连接  
		Connection conn = null;
		try {
			go();
			//映射到本地的服务
			conn = DriverManager.getConnection("jdbc:mysql://localhost:"+lport+"/learning_space_2_zhw", "tylearning_user", "S4XwPbzWQDfq");
			//System.out.println("获取链接成功！");
		} catch (SQLException e) {
			//e.printStackTrace();
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
	    	resultSet.close();
	    	pstmt.close();
	        conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally{
	    	try {
				resultSet.close();
				pstmt.close();
		        conn.close();
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
			System.out.println("localhost:" + assinged_port); //端口映射 转发
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
	
	
	
	
}
