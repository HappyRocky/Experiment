package whaty.test.addEEDSCourse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlSpace;
import whaty.test.SshMysqlWebtrn;

/** 
 * @className:AddCourseByCredit.java
 * @classDescription:根据学分，补充缺失的选课并置为完成，需要补充的课程数=学分*3-已完成课程数
 * @author:GongYanshang
 * @createTime:2017年10月31日
 */
public class AddCourseByCredit {
	
	private List<String> openCourseIdList; // 候选课程的开课id
	private Set<String> ptIdOpencourseIdSet; // 已经通过的<loginId-openCourseId>
	private Map<String, String> loginIdStuIdMap; // <loginId,studentId> space
	
	public void outputSqlList(){
		
		int maxSize = 1000;
		String sql = "SELECT\n" +
				"	count(1)\n" +
				"FROM\n" +
				"	pr_class_trainee pct\n" +
				"JOIN pe_trainee pt ON pt.id = pct.FK_TRAINEE_ID\n" +
				"LEFT JOIN (\n" +
				"	SELECT\n" +
				"		ele.FK_TRAINEE_ID,\n" +
				"		count(ele.ID) AS eleCount,\n" +
				"		sum(IF(ele.SCORE = 100, 1, 0)) AS completeCount\n" +
				"	FROM\n" +
				"		pe_tch_elective ele\n" +
				"	JOIN pe_open_course poc ON poc.id = ele.FK_OPENCOURSE_ID\n" +
				"	WHERE\n" +
				"		poc.FK_TRAIN_CLASS = 'ff8080815e7696c1015e7a78d7f00313'\n" +
				"	GROUP BY\n" +
				"		ele.FK_TRAINEE_ID\n" +
				") t ON t.FK_TRAINEE_ID = pct.FK_TRAINEE_ID\n" +
				"WHERE\n" +
				"	pct.FK_TRAINING_CLASS_ID = 'ff8080815e7696c1015e7a78d7f00313'\n" +
				"AND (\n" +
				"	FLOOR(t.eleCount / 3) < pct.learnScore\n" +
				"	OR t.FK_TRAINEE_ID IS NULL\n" +
				")\n" +
				"AND pct.learnScore <= 25\n" +
				"AND pct.learnScore > 0";
		List<Object[]> countList = SshMysqlWebtrn.queryBySQL(sql);
		int num = Integer.valueOf(countList.get(0)[0].toString());
		int second = num / maxSize + 1;
		List<String> insertWebtrnSqlList = new ArrayList<String>();
		List<String> insertSpaceSqlList = new ArrayList<String>();
		List<String> logList = new ArrayList<String>();
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");

			// 查询出当前申请的信息
			sql = "SELECT\n" +
					"	pt.id,\n" +
					"	pt.LOGIN_ID,\n" +
					"	pct.learnScore,\n" +
					"	t.completeCount\n" +
					"FROM\n" +
					"	pr_class_trainee pct\n" +
					"JOIN pe_trainee pt ON pt.id = pct.FK_TRAINEE_ID\n" +
					"LEFT JOIN (\n" +
					"	SELECT\n" +
					"		ele.FK_TRAINEE_ID,\n" +
					"		count(ele.ID) AS eleCount,\n" +
					"		sum(IF(ele.SCORE = 100, 1, 0)) AS completeCount\n" +
					"	FROM\n" +
					"		pe_tch_elective ele\n" +
					"	JOIN pe_open_course poc ON poc.id = ele.FK_OPENCOURSE_ID\n" +
					"	WHERE\n" +
					"		poc.FK_TRAIN_CLASS = 'ff8080815e7696c1015e7a78d7f00313'\n" +
					"	GROUP BY\n" +
					"		ele.FK_TRAINEE_ID\n" +
					") t ON t.FK_TRAINEE_ID = pct.FK_TRAINEE_ID\n" +
					"WHERE\n" +
					"	pct.FK_TRAINING_CLASS_ID = 'ff8080815e7696c1015e7a78d7f00313'\n" +
					"AND (\n" +
					"	FLOOR(t.eleCount / 3) < pct.learnScore\n" +
					"	OR t.FK_TRAINEE_ID IS NULL\n" +
					")\n" +
					"AND pct.learnScore <= 25\n" +
					"AND pct.learnScore > 0 limit " + (j * maxSize) + "," + maxSize;
			List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
			
			// 学员已完成课程的开课id
			String loginIdConditions = "";
			for (Object[] objects : list) {
				String loginId = MyUtils.valueOf(objects[1]);
				if (StringUtils.isNotBlank(loginId)) {
					loginIdConditions += ",'" + loginId + "'";
				}
			}
			if (loginIdConditions.startsWith(",")) {
				loginIdConditions = loginIdConditions.substring(1);
			}
			init(loginIdConditions);
			
			// 开始补课
			for (Object[] objects : list) {
				String ptId = MyUtils.valueOf(objects[0]);
				String loginId = MyUtils.valueOf(objects[1]);
				int learnScore = Integer.valueOf(MyUtils.valueOf(objects[2]));
				int completeCount = Integer.valueOf(MyUtils.valueOf(objects[3]));
				int toAddCount = learnScore * 3 - completeCount; // 需要补课的数量
				if (toAddCount <= 0) {
					continue;
				}
				
				// 课程空间有无此学员
				String stuId = loginIdStuIdMap.get(loginId);
				if (StringUtils.isBlank(stuId)) {
					System.out.println("课程空间无此学员,loginId=" + loginId);
					continue;
				}
				
				// 从候选课程中进行选课
				int addCount = 0; // 已修复的课程数
				for (String pocId : openCourseIdList) {
					// 是否已完成此课
					if (ptIdOpencourseIdSet.contains(ptId + "-" + pocId)) {
						continue;
					}
					// 选课并完成
					String eleId = MyUtils.uuid();
					String insertSql = "insert into pe_tch_elective (id,fk_trainee_id,fk_opencourse_id,elective_date,status,flag_fee_check,fk_site_id,start_service_date,finishDate,SCORE) "
							+ "values ('" + eleId + "','" + ptId + "','" + pocId + "',now(),'40288a962f15bc98012f15c3cd970002','40288a962f15bc98012f15c3cd970002','ff80808155da5b850155dddbec9404c9',now(),now(),'100.0')  ON DUPLICATE KEY UPDATE SCORE=values(SCORE);";
					insertWebtrnSqlList.add(insertSql);
					// 课程空间选课
					insertSql = "INSERT INTO `pr_tch_stu_elective` ( " +
							"	`ID`, " +
							"	`FK_STU_ID`, " +
							"	`FK_COURSE_ID`, " +
							"	`ELECTIVE_DATE`, " +
							"	`FLAG_COURSE_FINISH`, " +
							"	`LEAR_TIME`, " +
							"	`modifyDate`, " +
							"	`FLAG_ELECTIVE_STATUS` " +
							") VALUES ( " +
							"		'" + eleId + "', " +
							"		'" + stuId + "', " +
							"		'" + pocId + "', " +
							"		now(), " +
							"		'1', " +
							"		'1', " +
							"		now(), " +
							"		'0' " +
							"	) ON DUPLICATE KEY UPDATE FLAG_COURSE_FINISH=values(FLAG_COURSE_FINISH);";
					insertSpaceSqlList.add(insertSql);
					// 是否已经补完
					addCount++;
					if (addCount >= toAddCount) {
						break;
					}
				}
				// 日志
				if (addCount != toAddCount) {
					System.out.println("应补课程数：" + toAddCount + "，实际补课数：" + addCount);
				}
				logList.add(loginId + "\t" + learnScore + "\t" + completeCount + "\t" + toAddCount + "\t" + addCount);
			}
		}
		String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/insertSpaceSqlList.sql";
		MyUtils.outputList(insertSpaceSqlList, path1);
		path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/insertWebtrn.sql";
		MyUtils.outputList(insertWebtrnSqlList, path1);
		path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/log.txt";
		MyUtils.outputList(logList, path1);
		System.out.println("输出完毕");
	}
	
	public void init(String loginIdConditions){
		// 读取课程库
		if (openCourseIdList == null) {
			String excelPath = "F:/whaty/医爱数据库迁移/鄂尔多斯学员补课.xls";
			List<String[]> list = MyUtils.readExcel(excelPath);
			String codeConditions = "";
			for (String[] strings : list) {
				String courseCode = strings[1];
				if (StringUtils.isNotBlank(courseCode)) {
					codeConditions += ",'" + courseCode + "'";
				}
			}
			if (codeConditions.startsWith(",")) {
				codeConditions = codeConditions.substring(1);
			}
			// 查询课程信息及开课id
			openCourseIdList = new ArrayList<String>();
			String sql = "SELECT\n" +
					"	c.ID,\n" +
					"	ptc.`CODE`\n" +
					"FROM\n" +
					"	pe_open_course c\n" +
					"JOIN pe_tch_course ptc ON ptc.id = c.FK_TCH_COURSE\n" +
					"WHERE\n" +
					"	ptc.`CODE` IN (" + codeConditions + ")\n" +
					"AND c.FK_TRAIN_CLASS='ff8080815e7696c1015e7a78d7f00313'";
			List<Object[]> objList = SshMysqlWebtrn.getBySQL(sql);
			for (Object[] objects : objList) {
				String openCourseId = MyUtils.valueOf(objects[0]);
				if (StringUtils.isNotBlank(openCourseId) && !openCourseIdList.contains(openCourseId)) {
					openCourseIdList.add(openCourseId);
				}
			}
		}
		System.out.println("候选课程库共有" + openCourseIdList.size() + "门");
		
		// 查询已经完成的选课
		ptIdOpencourseIdSet = new HashSet<String>();
		String sql = "SELECT\n" +
				"	ele.FK_TRAINEE_ID,\n" +
				"	ele.FK_OPENCOURSE_ID\n" +
				"FROM\n" +
				"	pe_tch_elective ele\n" +
				"JOIN pe_open_course poc ON poc.id = ele.FK_OPENCOURSE_ID\n" +
				"JOIN pe_trainee pt ON pt.id = ele.FK_TRAINEE_ID\n" +
				"WHERE\n" +
				"	pt.LOGIN_ID IN (" + loginIdConditions + ")\n" +
				"AND ele.SCORE = 100\n" +
				"AND poc.FK_TRAIN_CLASS = 'ff8080815e7696c1015e7a78d7f00313'";
		List<Object[]> list = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : list) {
			String ptId = MyUtils.valueOf(objects[0]);
			String openCourseId = MyUtils.valueOf(objects[1]);
			ptIdOpencourseIdSet.add(ptId + "-" + openCourseId);
		}
		
		// 课程空间查询loginId对应的studentId
		loginIdStuIdMap = new HashMap<String, String>();
		sql = "select t.LOGIN_ID,t.ID from pe_student t where t.LOGIN_ID in (" + loginIdConditions + ")  and t.SITE_CODE='yiai'";
		list = SshMysqlSpace.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginid = MyUtils.valueOf(objects[0]);
			String ptid = MyUtils.valueOf(objects[1]);
			loginIdStuIdMap.put(loginid, ptid);
		}
	}
	
	public static void main(String[] args) {
		AddCourseByCredit addCourseByCredit = new AddCourseByCredit();
		addCourseByCredit.outputSqlList();
		System.exit(0);
	}
}

