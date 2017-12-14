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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 */
@SuppressWarnings("all")
public class SshMysqlSpace {
	static String url2 = "jdbc:mysql://192.168.20.51:33306/learning_space_2_zhw?useUnicode=true&characterEncoding=UTF-8";
	static String username2 = "tylearning_user";
	static String password2 = "S4XwPbzWQDfq"; 
	
	public static Connection conn = null;
	
	public static synchronized Connection getConn(){
		try {
			if(conn != null ){
				return conn;
			}
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url2, username2, password2);
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
	
	public static List<Object[]> getBySQL(String sql) {
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
