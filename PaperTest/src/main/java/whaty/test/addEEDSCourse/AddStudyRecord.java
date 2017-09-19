package whaty.test.addEEDSCourse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xml.internal.security.Init;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;

/** 
 * @className:MatchUser.java
 * @classDescription:迁移鄂尔多斯学习记录
 * @author:yourname
 * @createTime:2017年9月14日
 */
public class AddStudyRecord {
	
	public void outputStudyRecord(){
		int maxSize = 3000;
		System.out.println("开始查询");
		String sql = "SELECT\n" +
				"	count(*)\n" +
				"FROM\n" +
				"	pe_trainee pt\n" +
				"WHERE\n" +
				"	pt.LOGIN_ID LIKE 'erds2017@%' and pt.var0 != ''\n" +
				"AND pt.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'";
		List<Object[]> countList = SshMysqlWebtrn.queryBySQL(sql);
		int num = Integer.valueOf(countList.get(0)[0].toString());
		int second = num / maxSize + 1;
		
//		second = 1;
		
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		String date = DateUtils.getToday();
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
			List<String> updateSqlList = new ArrayList<String>();
			
			sql = "SELECT\n" +
					"	pt.ID,	\n" +
					"	pt.var0	\n" +
					"WHERE\n" +
					"	pt.LOGIN_ID LIKE 'erds2017@%'  and pt.var0 != ''\n" +
					"AND pt.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' LIMIT " + (j * maxSize) + "," + maxSize;
			List<Object[]> ptList = SshMysqlWebtrn.queryBySQL(sql);
			String conditions = "";
			for (Object[] objects : ptList) {
				String ptId = MyUtils.valueOf(objects[0]);
				String userId = MyUtils.valueOf(objects[1]);
				conditions += ",'" + userId + "'";
			}
			
			// 查询原平台的记录
			Map<String, String[]> userCourseMap; // 原数据库学员选课和学分
			
			String path1 = "E:/myJava/yiaiSql/" + date + "/updateUserId_from_" +  (j * maxSize) + ".sql";
			MyUtils.outputList(updateSqlList, path1);
		}
		System.out.println("数据处理完毕");
	}
	
	
	public static void main(String[] args) {
		AddStudyRecord addStudyRecord = new AddStudyRecord();
		addStudyRecord.outputStudyRecord();
		System.exit(0);
	}
}

