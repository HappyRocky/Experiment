package whaty.test.addEEDSCourse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlSpace;

/** 
 * @className:addStuSco.java
 * @classDescription:鄂尔多斯选课，补充课程空间的scorm_stu_sco
 * @author:yourname
 * @createTime:2017年9月27日
 */
public class AddStuSco {
	public void outputAddStuScoSql(String startTime, String endTime){
		List<String> addStuScoSqlList = new ArrayList<>();
		System.out.println("查询课程空间学习完成记录");
		String sql = "SELECT\n" +
				"	ele.id as eleId,\n" +
				"	su.ID as userId,\n" +
				"	ele.FK_COURSE_ID as courseId,\n" +
				"	item.ID as itemId,\n" +
				"	ele.ELECTIVE_DATE as completeDate,\n" +
				"	item.TYPE,\n" +
				"	sss.`STATUS`\n" +
				"FROM\n" +
				"	pr_tch_stu_elective ele\n" +
				"JOIN pe_student ps ON ps.id = ele.FK_STU_ID\n" +
				"AND ps.SITE_CODE = 'yiai'\n" +
				"JOIN sso_user su ON su.LOGIN_ID = ps.LOGIN_ID\n" +
				"AND su.SITE_CODE = 'yiai'\n" +
				"JOIN scorm_course_info info ON info.FK_COURSE_ID = ele.FK_COURSE_ID\n" +
				"JOIN scorm_course_item item ON item.FK_SCORM_COURSE_ID = info.id\n" +
				"AND (	item.TYPE = 'video'	OR item.type = 'text' OR item.type = 'exam')\n" +
				"LEFT JOIN scorm_stu_sco sss on sss.SYSTEM_ID=item.ID and sss.STUDENT_ID=su.ID\n" +
				"WHERE\n" +
				"	(	ps.LOGIN_ID LIKE 'erds@%'	OR ps.LOGIN_ID LIKE 'dltq@%')\n" +
				"AND ele.FLAG_COURSE_FINISH = '1' AND ele.score1 is not null " + 
				"AND ele.ELECTIVE_DATE >= '" + startTime + "' " + 
				(StringUtils.isNotBlank(endTime) ? " AND ele.ELECTIVE_DATE <= '" + endTime + "'" : "");
		List<Object[]> list = SshMysqlSpace.queryBySQL(sql);
		System.out.println("共查出" + list.size() + "个结果，开始生成sql");
		for (Object[] objs : list) {
			String eleId = MyUtils.valueOf(objs[0]);
			String userId = MyUtils.valueOf(objs[1]);
			String courseId = MyUtils.valueOf(objs[2]);
			String itemId = MyUtils.valueOf(objs[3]);
			String completeDate = MyUtils.valueOf(objs[4]);
			String itemType = MyUtils.valueOf(objs[5]);
			String itemStatus = MyUtils.valueOf(objs[6]);
			if ("completed".equals(itemStatus)) {
				continue;
			}
			String newId = MyUtils.uuid();
			String addSql = "INSERT INTO `scorm_stu_sco` ( " +
				     " `ID`, " +
				     " `SYSTEM_ID`, " +
				     " `FK_COURSE_ID`, " +
				     " `STUDENT_ID`, " +
				     " `FIRST_ACCESSDATE`, " +
				     " `LAST_ACCESSDATE`, " +
				     " `STATUS`, " +
				     " `TOTAL_TIME`, " +
				     " `COMPLETE_PERCENT`, " +
				     " `studyTime`, " +
				     " `SITE_CODE` " +
				     ") VALUES( " +
				     "  '" + newId + "', " +
				     "  '" + itemId + "', " +
				     "  '" + courseId + "', " +
				     "  '" + userId + "', " +
				     "  '" + completeDate + "', " +
				     "  '" + completeDate + "', " +
				     "  'completed', " +
				     "  '00:00:01', " +
				     "  '100', " +
				     "  '1', " +
				     "  'yiai' " +
				     " ) ON DUPLICATE KEY UPDATE `STATUS` = VALUES(`STATUS`),COMPLETE_PERCENT =VALUES(COMPLETE_PERCENT);";
			addStuScoSqlList.add(addSql);
		}
		String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/addStuSco.sql";
		MyUtils.outputList(addStuScoSqlList, path1);
	}
	
	public static void main(String[] args) {
		AddStuSco addStuSco = new AddStuSco();
		addStuSco.outputAddStuScoSql("2017-07-01",null);
		System.exit(0);
	}
}

