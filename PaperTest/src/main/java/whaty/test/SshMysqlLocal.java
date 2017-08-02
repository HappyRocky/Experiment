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

import org.apache.commons.collections.CollectionUtils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * 使用SSH连接Mysql
 */
@SuppressWarnings("all")
public class SshMysqlLocal {
    static String driver="com.mysql.jdbc.Driver";
    static String url="jdbc:mysql://192.168.43.102:3306/chen_test";
    static String user="root";
    static String pwd="root";
	
	static String filePath = "E:\\delsql.txt";
	static String logPath = "E:\\log.txt";
	
	public static void main(String[] args) {
		
	} 
	
	public static Connection getConn() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Connection con = null;
		try {
			con = DriverManager.getConnection(url, user, pwd);
			System.out.println("Database connection is successful");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
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
