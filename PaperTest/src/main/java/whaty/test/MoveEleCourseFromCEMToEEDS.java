package whaty.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;

/** 
 * @className:MoveEleCourseFromCEMToEEDS.java
 * @classDescription:将鄂尔多斯的学员选择的cme班级的课转移到鄂尔多斯班级下，同时改变学分和证书
 * @author:yourname
 * @createTime:2017年10月23日
 */
public class MoveEleCourseFromCEMToEEDS {
	
	private Map<String, String> courseNamePocIdMap; // 2017鄂尔多斯班级的课程名和开课id的map
	private Map<String, String[]> loginIdScoreMap; // <loginId,{pr_class_trainee.id,score,courseCount}>
	private Map<String, String> loginIdStuIdMap; // 课程空间鄂尔多斯学员<loginId,stuId>
	private Set<String> loginIdCourseNameSet; // 鄂尔多斯学员<loginId-courseName>
	
	public void outputSql(String sourceClassId, String targetClassId){
		String eleModuleId = "ff8080815e7696c1015e7a78d8340315";
		String studyModuleId = "ff8080815e7696c1015e7a78d8340316";
		int maxSize = 1000;
		// 得到申请记录
		String sql = "SELECT\n" +
				"	pt.LOGIN_ID\n" +
				"FROM\n" +
				"	pe_tch_elective e\n" +
				"JOIN pe_open_course poc ON poc.id = e.FK_OPENCOURSE_ID\n" +
				"JOIN pe_tch_course ptc ON ptc.id = poc.FK_TCH_COURSE\n" +
				"JOIN pe_trainee pt ON pt.id = e.FK_TRAINEE_ID\n" +
//				"JOIN pr_class_trainee pct ON pct.FK_TRAINEE_ID = pt.ID\n" +
//				"AND pct.FK_TRAINING_CLASS_ID = poc.FK_TRAIN_CLASS\n" +
				"WHERE\n" +
				"	(\n" +
				"		pt.LOGIN_ID LIKE 'erds@%'\n" +
				"		OR pt.LOGIN_ID LIKE 'erds2017@%'\n" +
				"		OR pt.LOGIN_ID LIKE 'dltq@%'\n" +
				"	)\n" +
				" AND e.SCORE < 100 " + 
				" AND poc.FK_TRAIN_CLASS = '" + sourceClassId + "' ";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		
		// 初始化
		String conditions = "";
		Set<String> loginIdSet = new HashSet<String>();
		for (Object[] objects : list) {
			String loginId = MyUtils.valueOf(objects[0]);
			if (!loginIdSet.contains(loginId)) {
				conditions += ",'" + loginId + "'";
				loginIdSet.add(loginId);
			}
		}
		if (conditions.startsWith(",")) {
			conditions = conditions.substring(1);
		}
		System.out.println("需要处理" + loginIdSet.size() + "个学员，开始初始化");
		init(conditions);
		
		
		int num = list.size();
		int second = num / maxSize + 1;
		List<String> insertWebtrnSqlList = new ArrayList<String>();
		List<String> insertSpaceSqlList = new ArrayList<String>();
		Set<String> openedLoginIdSet = new HashSet<String>(); // 存放开启过班级流程的loginId
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");

			// 查询出当前申请的信息
			sql = "SELECT\n" +
					"	e.id,\n" +
					"	pt.ID,\n" +
					"	pt.LOGIN_ID,\n" +
					"	ptc.`NAME`,\n" +
					"	e.ELECTIVE_DATE\n" +
					"FROM\n" +
					"	pe_tch_elective e\n" +
					"JOIN pe_open_course poc ON poc.id = e.FK_OPENCOURSE_ID\n" +
					"JOIN pe_tch_course ptc ON ptc.id = poc.FK_TCH_COURSE\n" +
					"JOIN pe_trainee pt ON pt.id = e.FK_TRAINEE_ID\n" +
//					"JOIN pr_class_trainee pct ON pct.FK_TRAINEE_ID = pt.ID\n" +
//					"AND pct.FK_TRAINING_CLASS_ID = poc.FK_TRAIN_CLASS\n" +
					"WHERE\n" +
					"	(\n" +
					"		pt.LOGIN_ID LIKE 'erds@%'\n" +
					"		OR pt.LOGIN_ID LIKE 'erds2017@%'\n" +
					"		OR pt.LOGIN_ID LIKE 'dltq@%'\n" +
					"	)\n" +
					" AND e.SCORE < 100 " + 
					"AND poc.FK_TRAIN_CLASS = '" + sourceClassId + "' limit " + (j * maxSize) + "," + maxSize;
			list = SshMysqlWebtrn.queryBySQL(sql);
			for (Object[] objects : list) {
				String oldEleId = (String) objects[0];
				String ptId = (String) objects[1];
				String loginId = (String) objects[2];
				String courseName = (String) objects[3];
				String createTime = MyUtils.valueOf(objects[4]);
				
				String pocId = courseNamePocIdMap.get(courseName);
				if (StringUtils.isBlank(pocId)) {
					System.out.println("开课id为空：courseName=" + courseName);
					continue;
				}
				String stuId = loginIdStuIdMap.get(loginId);
				if (StringUtils.isBlank(stuId)) {
					System.out.println("课程空间无此学员,loginId=" + loginId);
					continue;
				}
				
				// 删除当前选课
				String deleteSql = "delete from pe_tch_elective where id='" + oldEleId + "';";
				insertWebtrnSqlList.add(deleteSql);
				deleteSql = "delete from pr_tch_stu_elective where id='" + oldEleId + "';";
				insertSpaceSqlList.add(deleteSql);
				
//				if (loginIdCourseNameSet.contains(loginId + "-" + courseName)) { // 鄂尔多斯班级中已完成这门课，不必再选
//					continue;
//				}
//				
//				// 根据coursename选择鄂尔多斯班级的课
//				String eleId = MyUtils.uuid();
//				String insertSql = "insert into pe_tch_elective (id,fk_trainee_id,fk_opencourse_id,elective_date,status,flag_fee_check,fk_site_id,start_service_date,finishDate,result0,SCORE) "
//						+ "values ('" + eleId + "','" + ptId + "','" + pocId + "','" + createTime + "','40288a962f15bc98012f15c3cd970002','40288a962f15bc98012f15c3cd970002','ff80808155da5b850155dddbec9404c9','" + createTime + "','" + createTime + "','1','100.0')  ON DUPLICATE KEY UPDATE SCORE=values(SCORE);";
//				insertWebtrnSqlList.add(insertSql);
//
//				
//				// 开启流程
//				if (!openedLoginIdSet.contains(loginId)) { // 没有开启过
//					openedLoginIdSet.add(loginId);
//					// 选课，进行中
//					String moduleSql = "insert into pr_training_module (id,create_time,fk_trainee_id,fk_module_id,fk_site_id,status) "
//							+ "VALUE ('" + MyUtils.uuid() + "','" + createTime + "','" + ptId + "','" + eleModuleId + "','ff80808155da5b850155dddbec9404c9','bc2dec18d8cc11e6bb8900251113d11d') ON DUPLICATE KEY UPDATE status=values(status);";
//					insertWebtrnSqlList.add(moduleSql);
//					// 学习，进行中
//					moduleSql = "insert into pr_training_module (id,create_time,fk_trainee_id,fk_module_id,fk_site_id,status) "
//							+ "VALUE ('" + MyUtils.uuid() + "','" + createTime + "','" + ptId + "','" + studyModuleId + "','ff80808155da5b850155dddbec9404c9','bc2dec18d8cc11e6bb8900251113d11d') ON DUPLICATE KEY UPDATE status=values(status);";
//					insertWebtrnSqlList.add(moduleSql);
//				}
//				
//				// 课程空间选课
//				insertSql = "INSERT INTO `pr_tch_stu_elective` ( " +
//						"	`ID`, " +
//						"	`FK_STU_ID`, " +
//						"	`FK_COURSE_ID`, " +
//						"	`ELECTIVE_DATE`, " +
//						"	`FLAG_COURSE_FINISH`, " +
//						"	`score1`, " +
//						"	`LEAR_TIME`, " +
//						"	`modifyDate`, " +
//						"	`FLAG_ELECTIVE_STATUS` " +
//						") VALUES ( " +
//						"		'" + eleId + "', " +
//						"		'" + stuId + "', " +
//						"		'" + pocId + "', " +
//						"		'" + createTime + "', " +
//						"		'1', " +
//						"		'1', " +
//						"		'1', " +
//						"		'" + createTime + "', " +
//						"		'0' " +
//						"	) ON DUPLICATE KEY UPDATE score1=values(score1);";
//				insertSpaceSqlList.add(insertSql);
			}
		}
		String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/insertSpaceSqlList.sql";
		MyUtils.outputList(insertSpaceSqlList, path1);
		path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/insertWebtrn.sql";
		MyUtils.outputList(insertWebtrnSqlList, path1);
		path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/loginIdconditions.txt";
		List<String> conditionList = new ArrayList<String>();
		conditionList.add(conditions);
		MyUtils.outputList(conditionList, path1);
		System.out.println("输出完毕");
	}
	
	public void init(String loginIdConditions){
		// 2017鄂尔多斯班级的课程名和开课id的map
		courseNamePocIdMap = new HashMap<String, String>();
		String sql = "SELECT\n" +
				"	poc.id,\n" +
				"	c.`NAME`\n" +
				"FROM\n" +
				"	pe_open_course poc\n" +
				"JOIN pe_tch_course c ON c.id = poc.FK_TCH_COURSE\n" +
				"WHERE\n" +
				"	poc.FK_TRAIN_CLASS = 'ff8080815e7696c1015e7a78d7f00313'";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String pocId = (String) objects[0];
			String courseName = (String) objects[1];
			courseNamePocIdMap.put(courseName, pocId);
		}
		
		// 2017鄂尔多斯班级的loginid和课程名
		loginIdCourseNameSet = new HashSet<String>();
		sql = "SELECT\n" +
				"	pt.LOGIN_ID,\n" +
				"	ptc.`NAME`\n" +
				"FROM\n" +
				"	pe_tch_elective e\n" +
				"JOIN pe_open_course poc ON poc.id = e.FK_OPENCOURSE_ID\n" +
				"JOIN pe_tch_course ptc ON ptc.id = poc.FK_TCH_COURSE\n" +
				"JOIN pe_trainee pt ON pt.id = e.FK_TRAINEE_ID\n" +
				"WHERE pt.LOGIN_ID in (" + loginIdConditions + ")\n" +
				"AND poc.FK_TRAIN_CLASS = 'ff8080815e7696c1015e7a78d7f00313' AND e.SCORE = '100' ";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginid = (String) objects[0];
			String courseName = (String) objects[1];
			loginIdCourseNameSet.add(loginid + "-" + courseName);
		}
		
		// 鄂尔多斯班级学员的学分和完成课程数
		loginIdScoreMap = new HashMap<String, String[]>();
		sql = "SELECT\n" +
				"	pt.LOGIN_ID,\n" +
				"	pct.ID,\n" +
				"	pct.learnScore,\n" +
				"	count(e.id)\n" +
				"FROM\n" +
				"	pr_class_trainee pct\n" +
				"JOIN pe_trainee pt ON pt.id = pct.FK_TRAINEE_ID\n" +
				"LEFT JOIN pe_tch_elective e ON e.FK_TRAINEE_ID = pt.id\n" +
				"AND e.SCORE = '100'\n" +
				"LEFT JOIN pe_open_course poc ON poc.ID = e.FK_OPENCOURSE_ID\n" +
				"AND poc.FK_TRAIN_CLASS = 'ff8080815e7696c1015e7a78d7f00313'\n" +
				"WHERE\n" +
				"		pt.LOGIN_ID in (" + loginIdConditions + ") \n" +
				"AND pct.FK_TRAINING_CLASS_ID = 'ff8080815e7696c1015e7a78d7f00313'\n" +
				"GROUP BY\n" +
				"	pct.id";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginId = MyUtils.valueOf(objects[0]);
			String pctId = MyUtils.valueOf(objects[1]);
			String score = MyUtils.valueOf(objects[2]);
			String courseCount = MyUtils.valueOf(objects[3]);
			String[] strings = {pctId, score, courseCount};
			loginIdScoreMap.put(loginId, strings);
		}
		
		// 课程空间查询loginId对应的studentId
		loginIdStuIdMap = new HashMap<String, String>();
		sql = "select t.LOGIN_ID,t.ID from pe_student t where t.LOGIN_ID in (" + loginIdConditions + ") and t.SITE_CODE='yiai'";
		list = SshMysqlSpace.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginid = MyUtils.valueOf(objects[0]);
			String ptid = MyUtils.valueOf(objects[1]);
			loginIdStuIdMap.put(loginid, ptid);
		}
	}
	
	public static void main(String[] args) {
		String sourceClassId = "ff8080815a27e1a4015a353967cf0037"; // CME
//		String sourceClassId = "ff8080815cd034e3015cd4250372023c"; // 传染病
//		String sourceClassId = "ff8080815d562af7015d59a467ab2223"; // 口腔四手操作系列课程（非学分项目）
		String targetClassId = "ff8080815e7696c1015e7a78d7f00313"; // 2017鄂尔多斯
		MoveEleCourseFromCEMToEEDS moveEleCourseFromCEMToEEDS = new MoveEleCourseFromCEMToEEDS();
		moveEleCourseFromCEMToEEDS.outputSql(sourceClassId, targetClassId);
		System.exit(0);
	}
}

