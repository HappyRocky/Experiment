package whaty.test;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;



/**
 * 连接57webtrn的Mysql，2号库
 */
@SuppressWarnings("all")
public class SshMysqlWebtrn57 {
	static String url2 = "jdbc:mysql://192.168.20.49:3306/webtrn?useUnicode=true&characterEncoding=UTF-8";
	static String username2 = "webtrn";
	static String password2 = "whaty@webtrn"; 
	
	static String YIAI_SITE_ID = "ff80808155de17270155de1ecca20448"; // 本地yiai
	
	public static Connection conn = null;
	
	public static void main(String[] args) {
		SshMysqlWebtrn57 ssh = new SshMysqlWebtrn57();
		Map conditions = new HashMap<>();
		conditions.put("traineeId", "a8a7e7b8db5046d58e713520a8b02939");
		conditions.put("classId", "4028aba95cf6acd4015cf6d88cd3002f");
		conditions.put("siteId", "ff80808155de17270155de1ecca20448");
		conditions.put("currentId", "4028aba95cf6acd4015cf6d957720057");
		
		System.out.println(ssh.closeNextAllModule(conditions));
		
		System.exit(0);
	} 
	
	public Map<String, String> closeNextAllModule(Map conditions) {
		String traineeId = String.valueOf(conditions.get("traineeId"));
		String classId = String.valueOf(conditions.get("classId"));
		String siteId = String.valueOf(conditions.get("siteId"));
		String currentId = String.valueOf(conditions.get("currentId"));
		Map<String, String> map = new HashMap<String, String>();
		map.put("info", "关闭后续流程失败");
		map.put("success", "false");
		String sql = " SELECT petm.id AS moduleId FROM pe_training_module petm, pe_training_setting pts WHERE petm.pe_project_base_id = pts.ID  AND petm.flag_active='1' AND petm.pe_training_class_id = '"
				+ classId + "'  ORDER BY petm.code ASC ";
		try {
			List list = this.getGeneralService().getBySQL(sql);
			String nextModuleIdCondition = "";
			int size = list.size();
			boolean hasFindCurrentId = false; // 是否遍历到了当前id，如果是，则开始累加后续所有id
			for (int i = 0; i < size; i++) {
				String moduleId = String.valueOf(list.get(i));
				if (moduleId.equals(currentId)) { // 遍历到了当前id
					hasFindCurrentId = true;
					continue;
				}
				if (hasFindCurrentId) { // 属于后续id
					nextModuleIdCondition += ",'" + moduleId + "'";
				}
			}
			if (nextModuleIdCondition.startsWith(",")) {
				nextModuleIdCondition = nextModuleIdCondition.substring(1);
				String deleteSql = " delete from pr_training_module where fk_module_id in (" + nextModuleIdCondition + ") and fk_trainee_id='"
						+ traineeId + "'";
				this.getGeneralService().executeBySQL(deleteSql);
				map.put("info", "添加/修改下一个流程成功");
				map.put("success", "true");
			} else {
				map.put("info", "已经是最后一个流程无需开启下一个流程。");
				map.put("success", "true");
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("info", "添加/修改下一个流程失败");
			map.put("success", "false");
		}
		return map;
	}
	
	
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
	
	public static int executeBySQL(String sql) {
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
	
	public static List getBySQL(String  sql) {
	    Connection conn = getConn();
	    List list = new ArrayList<Object[]>(); 
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
	    		if (obj.length > 1) {
	    			list.add(obj);
				} else {
					list.add(obj[0]);
				}
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
	
	public SshMysqlWebtrn57 getGeneralService(){
		return new SshMysqlWebtrn57();
	}
}
