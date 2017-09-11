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
		conditions.put("loginId", "zd7969@phve.cn");
		conditions.put("siteCode", "yiai");
		conditions.put("parentId", "c10793526b5c11e683b100251113d11d");
		conditions.put("areaName", "测试医院6");
		
		System.out.println(ssh.addArea(conditions));
		
		System.exit(0);
	} 
	
	public Map<String, String> addArea(Map conditions) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("info", "添加失败");
		map.put("status", "false");
		String parentId = (String) conditions.get("parentId");
		String areaName = (String) conditions.get("areaName");
		String loginId = (String) conditions.get("loginId");
		String siteCode = (String) conditions.get("siteCode");

		// 校验参数
		if (StringUtils.isBlank(parentId) || StringUtils.isBlank(areaName)) {
			map.put("info", "参数缺失");
			return map;
		}
		
		// 查询 siteid
		StringBuffer siteSqlSb = new StringBuffer();
		siteSqlSb.append(" SELECT ");
		siteSqlSb.append(" 	ps.ID, ");
		siteSqlSb.append(" 	a.manager_name ");
		siteSqlSb.append(" FROM ");
		siteSqlSb.append(" 	pe_site ps ");
		siteSqlSb.append(" JOIN pe_trainee pt ON pt.FK_SITE_ID = ps.id ");
		siteSqlSb.append(" LEFT JOIN pe_area a ON a.manager_name = pt.LOGIN_ID and a.fk_site_id=ps.id ");
		siteSqlSb.append(" WHERE ");
		siteSqlSb.append(" 	ps.`CODE` = '" + siteCode + "' ");
		siteSqlSb.append(" AND pt.LOGIN_ID = '" + loginId + "' ");
		List<Object[]> siteList = new ArrayList();
		try {
			siteList = this.getGeneralService().getBySQL(siteSqlSb.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (CollectionUtils.isEmpty(siteList)) {
			map.put("info", "查询站点学员失败");
			return map;
		}
		String siteId = siteList.get(0)[0].toString();
		if (siteList.get(0)[1] != null && StringUtils.isNotBlank(siteList.get(0)[1].toString())) {
			map.put("info", "您之前手动添加过工作单位，每位用户只允许添加一次");
			return map;
		}
		
		// 查询出所有子节点
		StringBuffer queryChildSb = new StringBuffer();
		List<Object[]> childList = new ArrayList<Object[]>();
		queryChildSb.append(" SELECT ");
		queryChildSb.append(" 	parent.`level` as parentLevel, ");
		queryChildSb.append(" 	parent.level_code as parentLavelCode, ");
		queryChildSb.append(" 	a.`name` as childName, ");
		queryChildSb.append(" 	a.`level` as childLevel, ");
		queryChildSb.append(" 	a.level_code as childLevelCode");
		queryChildSb.append(" FROM ");
		queryChildSb.append(" 	pe_area parent ");
		queryChildSb.append(" LEFT JOIN pe_area a ON a.fk_parent_id = parent.id ");
		queryChildSb.append(" WHERE ");
		queryChildSb.append(" 	parent.id = '" + parentId + "' ");
		queryChildSb.append(" AND parent.fk_site_id = '" + siteId + "' ");
		try {
			childList = this.getGeneralService().getBySQL(queryChildSb.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
			map.put("info", "查询区域信息失败");
			return map;
		}
		if (CollectionUtils.isEmpty(childList)) {
			map.put("info", "区域不存在");
			return map;
		}
		if (childList.get(0)[0] == null || childList.get(0)[1] == null) {
			map.put("info", "父节点信息缺失");
			return map;
		}

		int levelCodeMax = 0; // levelCode的最后层级的最大值
		for (Object[] objects : childList) {
			String curName = String.valueOf(objects[2]);
			if (areaName.equals(curName)) { // 名称已存在
				map.put("info", "添加失败，" + areaName + "已存在");
				return map;
			}
			String curLevelCode = String.valueOf(objects[4]);
			if (StringUtils.isNotBlank(curLevelCode)) { // 更新 levelCodeMax
				if (curLevelCode.endsWith("/")) { // 去掉最后一个斜线
					curLevelCode = curLevelCode.substring(0, curLevelCode.length() - 1);
				}
				int idx = curLevelCode.lastIndexOf("/");
				if (idx >= 0) {
					int lastCode = Integer.parseInt(String.valueOf(curLevelCode.substring(idx + 1)));
					levelCodeMax = Math.max(levelCodeMax, lastCode);
				}
			}
		}
		int newLevel = Integer.valueOf(childList.get(0)[0].toString()) + 1;
		if (newLevel != 4) {
			map.put("info", "只允许在县级下添加医院");
			return map;
		}
		String parentLevelCode = childList.get(0)[1].toString();
		if (!parentLevelCode.endsWith("/")) {
			parentLevelCode = parentLevelCode + "/";
		}
		String newLevelCode = parentLevelCode + (levelCodeMax + 1) + "/";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		int rand = (int) (Math.random() * 9000) + 1000;// 产生1000-9999的随机数
		String newCode = sdf.format(new Date()) + rand;
		String newId = UUID.randomUUID().toString().replace("-", "");
		StringBuffer insertSb = new StringBuffer();
		insertSb.append(" INSERT INTO `pe_area` ( ");
		insertSb.append(" 	`id`, ");
		insertSb.append(" 	`name`, ");
		insertSb.append(" 	`code`, ");
		insertSb.append(" 	`createDate`, ");
		insertSb.append(" 	`level`, ");
		insertSb.append(" 	`level_code`, ");
		insertSb.append(" 	`fk_parent_id`, ");
		insertSb.append(" 	`fk_site_id`, ");
		insertSb.append(" 	`manager_name` ");
		insertSb.append(" ) VALUES ( ");
		insertSb.append(" 		'" + newId + "', ");
		insertSb.append(" 		'" + areaName + "', ");
		insertSb.append(" 		'" + newCode + "', ");
		insertSb.append(" 		NOW(), ");
		insertSb.append(" 		'" + newLevel + "', ");
		insertSb.append(" 		'" + newLevelCode + "', ");
		insertSb.append(" 		'" + parentId + "', ");
		insertSb.append(" 		'" + siteId + "', ");
		insertSb.append(" 		'" + loginId + "' )");
		try {
			int result = this.getGeneralService().executeBySQL(insertSb.toString());
			if (result < 1) {
				map.put("info", "插入区域信息失败，请稍后再试");
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("info", "插入区域信息失败，请稍后再试");
			return map;
		}
		map.put("info", "添加成功");
		map.put("status", "true");
		map.put("areaId", newId);
		map.put("areaName", areaName);
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
