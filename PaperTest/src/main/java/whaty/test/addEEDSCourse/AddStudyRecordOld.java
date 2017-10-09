package whaty.test.addEEDSCourse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlSpace;
import whaty.test.SshMysqlWebtrn;
import whaty.test.SshMysqlYiaiwang;

/** 
 * @className:MatchUser.java
 * @classDescription:迁移鄂尔多斯学习记录，eeds和dltq开头的，同时会输出新平台没有的课程，然后经过selectNewCourse处理，进行迁移新课程。然后需要运行AddStuSco，课程空间添加学习完成记录
 * @author:yourname
 * @createTime:2017年9月14日
 */
public class AddStudyRecordOld {
	
	private Map<String, String> codePocId; // <课程code,开课Id>
	private Map<String, String> loginIdPtIdMap; // <loginId,ptId> webtrn
	private Map<String, String> loginIdStuIdMap; // <loginId,studentId> space
	private Set<String> loginIdCodeSet; // 存放已经存在的 loginId-courseCode
	
	// 原平台不需要迁移的课程id（114门七牛文件包   MP3+ppt 或  三分屏课程）
	public static String noNeedAddMCIdConditions = "'1255','1257','1260','1263','1268','1269','1272','1273','1274','1275','1278','1277','1279','1280','1702','1708','1714','1720','1745','1749','1753','1758','1764','1770','4682','4683','4684','4685','4686','4687','4688','4689','4690','4691','4694','4697','4700','4701','4702','4699','4703','4704','4698','4696','4695','4693','4692','5709','9329','9330','9331','9335','9337','10376','10377','10611','10612','10613','10614','10615','10616','10617','10618','10619','10620','10643','10644','10650','10657','10665','10669','10676','10677','10678','10679','10680','10681','10682','10683','10684','10685','10686','10687','10688','10689','10690','10691','10692','10693','10694','10695','10696','10697','10698','10699','10700','10701','10702','10703','10704','10705','10706','10707','10708','10709','10710','10711','10712','10713','10714','12020','12028','12029','4581'";
	
	public void outputStudyRecord(String startTime, String endTime, String classId, String eleModuleId, String studyModuleId){
		init(classId);
		int maxSize = 3000;
		List<String> insertWebtrnSqlList = new ArrayList<>();
		List<String> insertSpaceSqlList = new ArrayList<>();
		Set<String> openedLoginIdSet = new HashSet<>(); // 存放开启过班级流程的loginId
		List<String> lostCourseList = new ArrayList<>(); // 缺失的课程
		System.out.println("开始查询");
		String sql = "SELECT DISTINCT\n" +
				"    MUCCS.user_id AS userid,\n" +
				"    MU.username,\n" +
				"    MC.sortorder AS courseCode,\n" +
				"    MUID2. DATA AS xueshi,\n" +
				"		FROM_UNIXTIME(MUCCS.timecreated),\n" +
				"		FROM_UNIXTIME(MUCCS.timemodified),\n" +
				"		MC.fullname,\n" +
				"		MC.id\n" +
				"FROM\n" +
				"    mdl_user_course_cm_status MUCCS\n" +
				"LEFT JOIN mdl_user MU ON MUCCS.user_id = MU.id\n" +
				"LEFT JOIN mdl_course MC ON MUCCS.course_id = MC.id\n" +
				"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
				"LEFT JOIN mdl_user_info_data MUID ON MUID.userid = MU.id\n" +
				"AND MUID.fieldid = 11\n" +
				"LEFT JOIN mdl_course_info_data MUID2 ON MUID2.courseid = MC.id\n" +
				"AND MUID2.fieldid = 2\n" +
				"LEFT JOIN mdl_course_completions MCCO ON MCCO.`course` = MUCCS.`course_id`\n" +
				"AND MCCO.`userid` = MUCCS.`user_id`\n" +
				"WHERE\n" +
				"    MUCCS.`module` = 15\n" +
				" AND MUCCS.timecreated >= UNIX_TIMESTAMP('" + startTime + "')\n" +
				" AND MUCCS.timecreated <= UNIX_TIMESTAMP('" + endTime + "')\n" +
				"AND MCCO. STATUS = 50\n" +
				"AND (MU.username like 'erds@%' or MU.username like 'dltq@%') " +
				"AND MC.id not in (" + noNeedAddMCIdConditions + ")";
		List<Object[]> selectList = SshMysqlYiaiwang.queryBySQL(sql);
//		List<Object[]> selectList = generateObjectList();
		for (Object[] objs : selectList) {
			String userId = MyUtils.valueOf(objs[0]);
			String username = MyUtils.valueOf(objs[1]);
			String preCourseCode = MyUtils.valueOf(objs[2]);
			String xueshi = MyUtils.valueOf(objs[3]);
			String createTime = MyUtils.valueOf(objs[4]);
			String modifyTime = MyUtils.valueOf(objs[5]);
			String courseName = MyUtils.valueOf(objs[6]);
			String courseId = MyUtils.valueOf(objs[7]);
			
			// 判断此课程属于鄂尔多斯课程
			String courseCode = courseId + "-" + preCourseCode;
			String[] courseCodes = {courseCode, "CME-" + courseCode, "EX-" + courseCode};
			boolean isExist = false;
			for (String string : courseCodes) {
				if (codePocId.containsKey(string)) {
					courseCode = string;
					isExist = true;
					break;
				}
			}
			if (!isExist) {
				String lostCourse =  courseId + "\t" + preCourseCode + "\t" + courseName;
				System.out.println("没有在新平台查找到课程：" + lostCourse);
				if (!lostCourseList.contains(lostCourse)) {
					lostCourseList.add(lostCourse);
				}
				continue;
			}
			String pocId = codePocId.get(courseCode); // 开课Id
			
			// 此学员在新平台中
			if (!loginIdPtIdMap.containsKey(username)) { 
				continue;
			}
			String ptId = loginIdPtIdMap.get(username);
			
			// 此学员在课程空间中
			if (!loginIdStuIdMap.containsKey(username)) { 
				System.out.println("webtrn有，space没有：" + username);
				continue;
			}
			String stuId = loginIdStuIdMap.get(username);
			
			// 判断是否已经选课
			String key = username + "_" + courseCode;
			if (loginIdCodeSet.contains(key)) {
				continue;
			}
			loginIdCodeSet.add(key); // 选课
			
			// webtrn选课
			String eleId = MyUtils.uuid();
			if (StringUtils.isBlank(xueshi)) {
				xueshi = "0";
			}
			if (xueshi.equals("３")) {
				xueshi = "3";
			}
			try {
				Double.parseDouble(xueshi);
			} catch (NumberFormatException e) {
				System.out.println("将学时转为double失败：学时=" + xueshi);
			}
			
			String insertSql = "insert into pe_tch_elective (id,fk_trainee_id,fk_opencourse_id,elective_date,status,flag_fee_check,fk_site_id,start_service_date,finishDate,result0,SCORE) "
					+ "values ('" + eleId + "','" + ptId + "','" + pocId + "','" + createTime + "','40288a962f15bc98012f15c3cd970002','40288a962f15bc98012f15c3cd970002','ff80808155da5b850155dddbec9404c9','" + createTime + "','" + modifyTime + "','" + xueshi + "','100.0')  ON DUPLICATE KEY UPDATE result0=values(result0);";
			insertWebtrnSqlList.add(insertSql);
			
			// 开启流程
			if (!openedLoginIdSet.contains(username)) { // 没有开启过
				openedLoginIdSet.add(username);
				// 选课，进行中
				String moduleSql = "insert into pr_training_module (id,create_time,fk_trainee_id,fk_module_id,fk_site_id,status) "
						+ "VALUE ('" + MyUtils.uuid() + "','" + createTime + "','" + ptId + "','" + eleModuleId + "','ff80808155da5b850155dddbec9404c9','bc2dec18d8cc11e6bb8900251113d11d') ON DUPLICATE KEY UPDATE status=values(status);";
				insertWebtrnSqlList.add(moduleSql);
				// 学习，进行中
				moduleSql = "insert into pr_training_module (id,create_time,fk_trainee_id,fk_module_id,fk_site_id,status) "
						+ "VALUE ('" + MyUtils.uuid() + "','" + createTime + "','" + ptId + "','" + studyModuleId + "','ff80808155da5b850155dddbec9404c9','bc2dec18d8cc11e6bb8900251113d11d') ON DUPLICATE KEY UPDATE status=values(status);";
				insertWebtrnSqlList.add(moduleSql);
			}
			
			
			// 课程空间选课
			insertSql = "INSERT INTO `pr_tch_stu_elective` ( " +
					"	`ID`, " +
					"	`FK_STU_ID`, " +
					"	`FK_COURSE_ID`, " +
					"	`ELECTIVE_DATE`, " +
					"	`FLAG_COURSE_FINISH`, " +
					"	`score1`, " +
					"	`LEAR_TIME`, " +
					"	`modifyDate`, " +
					"	`FLAG_ELECTIVE_STATUS` " +
					") VALUES ( " +
					"		'" + eleId + "', " +
					"		'" + stuId + "', " +
					"		'" + pocId + "', " +
					"		'" + createTime + "', " +
					"		'1', " +
					"		'" + xueshi + "', " +
					"		'1', " +
					"		'" + modifyTime + "', " +
					"		'0' " +
					"	) ON DUPLICATE KEY UPDATE score1=values(score1);";
			insertSpaceSqlList.add(insertSql);
			
		}
		String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/addWebtrnRecord.sql";
		MyUtils.outputList(insertWebtrnSqlList, path1);
		path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/addSpaceRecord.sql";
		MyUtils.outputList(insertSpaceSqlList, path1);
		path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/lostCourse.txt";
		MyUtils.outputList(lostCourseList, path1);
		System.out.println("数据处理完毕");
	}
	
	public void init(String classId){
		// 查询课程code对应的开课id
		System.out.println("查询课程code对应的开课id");
		codePocId = new HashMap<String, String>();
		loginIdCodeSet = new HashSet<>();
		String sql = "SELECT\n" +
				"	ptc.`CODE`,\n" +
				"	poc.ID,\n" +
				"	pt.LOGIN_ID\n" +
				"FROM\n" +
				"	pe_open_course poc\n" +
				"JOIN pe_tch_course ptc ON ptc.id = poc.FK_TCH_COURSE\n" +
				"LEFT JOIN pe_tch_elective ele on ele.FK_OPENCOURSE_ID=poc.ID\n" +
				"LEFT JOIN pe_trainee pt on pt.id=ele.FK_TRAINEE_ID\n" +
				"where poc.FK_TRAIN_CLASS in ('" + classId + "') ";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String code = MyUtils.valueOf(objects[0]);
			String pocId = MyUtils.valueOf(objects[1]);
			String loginId = MyUtils.valueOf(objects[2]);
			if (StringUtils.isNotBlank(loginId)) {
				loginIdCodeSet.add(loginId + "_" + code);
			}
			codePocId.put(code, pocId);
		}
		// 查询loginId对应的ptId
		System.out.println("查询loginId对应的ptId");
		loginIdPtIdMap = new HashMap<>();
		sql = "SELECT\n" +
				"	pt.LOGIN_ID,\n" +
				"	pt.ID\n" +
				"FROM\n" +
				"	pe_trainee pt\n" +
				"WHERE\n" +
				"	(\n" +
				"		pt.LOGIN_ID LIKE 'erds@%'\n" +
				"		OR pt.LOGIN_ID LIKE 'dltq@%'\n" +
				"	)\n" +
				"AND pt.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginid = MyUtils.valueOf(objects[0]);
			String ptid = MyUtils.valueOf(objects[1]);
			loginIdPtIdMap.put(loginid, ptid);
		}
		// 课程空间查询loginId对应的studentId
		System.out.println("查询课程空间查询loginId对应的studentId");
		loginIdStuIdMap = new HashMap<>();
		sql = "select t.LOGIN_ID,t.ID from pe_student t where (t.LOGIN_ID LIKE 'erds@%' or t.LOGIN_ID LIKE 'dltq@%') and t.SITE_CODE='yiai'";
		list = SshMysqlSpace.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginid = MyUtils.valueOf(objects[0]);
			String ptid = MyUtils.valueOf(objects[1]);
			loginIdStuIdMap.put(loginid, ptid);
		}
	}
	
	// 读取AddStudyRecordOld生成的缺失课程，进一步检验是否在新平台有缺失
	public void selectNewCourse() {
		String filePath = "E:/myJava/yiaiSql/lostCourse.txt";
		List<String> lineList = MyUtils.readFile(filePath);

		// 生成courseId的conditions
		System.out.println("生成name的conditions");
		Map<String, String> courseIdSortOrderMap = new HashMap<String, String>(); // <id,sortOrder>
		String conditions = "";
		for (String line : lineList) {
			if (StringUtils.isNotBlank(line)) {
				String[] strs = line.split("\t");
				String courseId = strs[0];
				String sortOrder = strs[1];
				conditions += ",'" + courseId + "'";
				courseIdSortOrderMap.put(courseId, sortOrder);
			}
		}
		if (conditions.startsWith(",")) {
			conditions = conditions.substring(1);
		} else {
			System.out.println("lostCourse内容为空");
			return;
		}

		// 在新平台查找
		System.out.println("在新平台查找");
		List<String> updateCodeSqlList = new ArrayList<>();
		String sql = "select c.ID,c.`NAME`,c.site_code from pe_tch_course c "
				+ "where c.FK_SITE_ID='ff80808155da5b850155dddbec9404c9' and c.site_code in (" + conditions + ")";
		List<Object[]> list = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : list) {
			String courseId = MyUtils.valueOf(objects[0]);
			String courseName = MyUtils.valueOf(objects[1]);
			String site_code = MyUtils.valueOf(objects[2]);
			String sortOrder = courseIdSortOrderMap.get(site_code);
			String newCode = "EX-" + site_code + "-" + sortOrder;
			String updateSql = "update pe_tch_course set `CODE`='" + newCode + "' where id='" + courseId + "';";
			updateCodeSqlList.add(updateSql);
			System.out.println("新平台已经存在的课程：" + courseName);
			courseIdSortOrderMap.remove(site_code);
		}

		// 输出缺失课程id
		List<String> lostIdList = new ArrayList<String>();
		for (Entry<String, String> entry : courseIdSortOrderMap.entrySet()) {
			String key = entry.getKey();
			lostIdList.add(key);
		}
		String path = "E:/myJava/yiaiSql/newlostCourse.txt";
		MyUtils.outputList(lostIdList, path);
		String path1 = "E:/myJava/yiaiSql/updateCourseCode.txt";
		MyUtils.outputList(updateCodeSqlList, path1);
	}
	
	// 对特殊学员进行补课
	public List<Object[]> generateObjectList(){
		List<Object[]> result = new ArrayList<>();
		// 需要补课的学员
		String[] loginIdList = {"erds@4735.com", "erds@4700.com", "erds@4224.com", "erds@6406.com", "erds@4650.com", "erds@4643.com", "erds@4638.com"};
		
		// 需要补的课
		String path = "F:/whaty/医爱数据库迁移/补课名称.xls";
		List<String[]> courseList = MyUtils.readExcel(path);
		
		// 双重循环进行补课
		for (String[] courseLine : courseList) {
			String courseName = courseLine[0];
			if (StringUtils.isBlank(courseName)) {
				continue;
			}
			String courseCode = courseLine[1];
			String[] codes = courseCode.split("-");
			String userId = "";
			String preCourseCode = codes[1];
			String xueshi = "1";
			String createTime = courseLine[16];
			String modifyTime = createTime;
			String courseId = codes[0];
			for (String loginId : loginIdList) {
				Object[] obj = {userId, loginId, preCourseCode, xueshi, createTime, modifyTime, courseName, courseId};
				result.add(obj);
			}
		}
		return result;
	}
	
	public static void main(String[] args) {
		AddStudyRecordOld addStudyRecord = new AddStudyRecordOld();
		String classId2017 = "ff8080815e7696c1015e7a78d7f00313";
		String eleModuleId2017 = "ff8080815e7696c1015e7a78d8340315";
		String studyModuleId2017 = "ff8080815e7696c1015e7a78d8340316";
		
		String classId2016 = "ff8080815e86ebf2015e92c4faae070f";
		String eleModuleId2016 = "ff8080815e86ebf2015e92c4faed0710";
		String studyModuleId2016 = "ff8080815e86ebf2015e92c4faed0711";
		addStudyRecord.outputStudyRecord("2017-09-27", "2017-09-29", classId2017, eleModuleId2017, studyModuleId2017);
//		addStudyRecord.outputStudyRecord("2016-07-01", "2017-07-01", classId2016, eleModuleId2016, studyModuleId2016);
		
		System.exit(0);
	}
}

