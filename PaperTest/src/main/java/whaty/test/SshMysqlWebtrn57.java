package whaty.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * 连接57webtrn的Mysql，2号库
 */
@SuppressWarnings("all")
public class SshMysqlWebtrn57 {
	static String url2 = "jdbc:mysql://192.168.20.49:3306/webtrn?useUnicode=true&characterEncoding=UTF-8";
	static String username2 = "webtrn";
	static String password2 = "whaty@webtrn"; 
	
	public static Connection conn = null;
	
	public static void main(String[] args) {
		SshMysqlWebtrn57 ssh = new SshMysqlWebtrn57();
		String type = "XF1";
		String siteId = "ff80808155de17270155de1ecca20448";
		String classId = "0bdd912e5c4e469abccefb73e82259a9";
		String stuId = "4028ab44574a689001574a8222bb0002";
		System.out.println("modelId=" + ssh.getCertificateModelId(type, siteId, classId,stuId));
		System.exit(0);
	} 
	
	public String getCertificateModelId(String type, String siteId, String classId, String stuId) {
		String id = "";
		// 特殊处理：湖南省的使用四川模板
		String province = "";
		String queryPtSql = "select pt.id,pt.province from pe_trainee pt where pt.ID='" + stuId
				+ "' and pt.FK_SITE_ID='" + siteId + "'";
		List ptList = getBySQL(queryPtSql.toString());
		if (CollectionUtils.isNotEmpty(ptList)) {
			Object[] objs = (Object[]) ptList.get(0);
			province = objs[1] == null ? "" : objs[1].toString();
			if (province.equals("湖南省")) {
				String modelCode = ("XF2".equals(type) ? "2" : "1");
				String sql = "select m.Id,m.`code` from pe_certificate_model m where m.`code`='CMESCType" + modelCode
						+ "' and m.FK_PESITE='" + siteId + "'";
				List list = getBySQL(sql);
				if (CollectionUtils.isNotEmpty(list)) {
					Object[] objs2 = (Object[]) list.get(0);
					id = (objs2[0] == null ? "" : objs2[0].toString());
				}
				return id;
			}
		}

		StringBuffer querySql = new StringBuffer();
		querySql.append(" SELECT ");
		querySql.append(" 	pcm.Id, ");
		querySql.append(" 	pcm.`code` ");
		querySql.append(" FROM ");
		querySql.append(" 	pr_class_certificate pcc ");
		querySql.append(" JOIN pe_certificate_model pcm ON pcm.id = pcc.FK_CERTIFICATE_MODEL_ID ");
		querySql.append(" WHERE ");
		querySql.append(" 	pcc.FK_TRAINING_CLASS_ID = '" + classId + "' ");
		querySql.append(" AND pcc.FK_SITE_ID = '" + siteId + "' ");
		try {
			List list = getBySQL(querySql.toString());
			if (CollectionUtils.isNotEmpty(list)) {
				if (list.size() == 1) { // 只有一行记录
					Object[] objs = (Object[]) list.get(0);
					id = (objs[0] == null ? "" : objs[0].toString());
				} else { // 有多行记录
					for (int i = 0; i < list.size(); i++) {
						Object[] objs = (Object[]) list.get(i);
						String curId = objs[0] == null ? "" : objs[0].toString();
						String code = objs[1] == null ? "" : objs[1].toString();
						if ((type.equals("XF1") && code.endsWith("Type1"))
								|| (type.equals("XF2") && code.endsWith("Type2"))) {
							id = curId;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
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
	
	public static List<Object[]> getBySQL(String  sql) {
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
}
