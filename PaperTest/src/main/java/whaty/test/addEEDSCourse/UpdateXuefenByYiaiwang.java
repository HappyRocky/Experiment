package whaty.test.addEEDSCourse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;
import whaty.test.SshMysqlYiaiwang;

/** 
 * @className:UpdateXuefenByYiaiwang.java
 * @classDescription:直接从旧平台把学分迁移过来
 * @author:yourname
 * @createTime:2017年9月20日
 */
public class UpdateXuefenByYiaiwang {
	
	private Map<String, String[]> loginIdCertXuefenMap; // <loginId,[ptId，获得学分，证书Id，证书学分]>
	private String maxCertCode; // 当前最大证书编号
	Map<String, Integer[]> loginIdCreditYiaiMap; // 原平台学分

	public void outputUpdateXuefenList(String classId, String year, String startTime, String endTime){
		init(classId, year);
		// 查询原平台学分
		System.out.println("查询原平台学分");
		loginIdCreditYiaiMap = new HashMap<String, Integer[]>();
		String sql = "SELECT\n" +
				"	sum(xueshi) AS xueshi,\n" +
				"	username\n" +
				"FROM\n" +
				"	(\n" +
				"		SELECT DISTINCT\n" +
				"			MUCCS.user_id AS userid,\n" +
				"			MU.username,\n" +
				"			MC.id AS courseid,\n" +
				"			MUID2. DATA  AS xueshi,\n" +
				"			MCCO. STATUS\n" +
				"		FROM\n" +
				"			mdl_user_course_cm_status MUCCS\n" +
				"		LEFT JOIN mdl_user MU ON MUCCS.user_id = MU.id\n" +
				"		LEFT JOIN mdl_course MC ON MUCCS.course_id = MC.id\n" +
				"		LEFT JOIN mdl_user_info_data MUID ON MUID.userid = MU.id\n" +
				"		AND MUID.fieldid = 11\n" +
				"		LEFT JOIN mdl_course_info_data MUID2 ON MUID2.courseid = MC.id\n" +
				"		AND MUID2.fieldid = 2\n" +
				"		LEFT JOIN mdl_course_completions MCCO ON MCCO.`course` = MUCCS.`course_id`\n" +
				"		AND MCCO.`userid` = MUCCS.`user_id`\n" +
				"		WHERE\n" +
				"			MUCCS.`module` = 15\n" +
				"		AND MUCCS.timecreated >= UNIX_TIMESTAMP('" + startTime + "')\n" +
				(StringUtils.isNotBlank(endTime) ? " AND MUCCS.timecreated <= UNIX_TIMESTAMP('" + endTime + "')\n" : "") +
				"		AND MCCO. STATUS = 50\n" +
				"		AND (MU.username like 'erds@%' or MU.username like 'dltq@%')\n" +
				"		ORDER BY\n" +
				"			MU.username DESC\n" +
				"	) A\n" +
				"GROUP BY\n" +
				"	username";
		List<Object[]> list = SshMysqlYiaiwang.queryBySQL(sql);
		
	}
	
	public void init(String classId, String year){
		// 获取学员的学分
		loginIdCertXuefenMap = new HashMap<String, String[]>();
		String sql = "SELECT\n" +
				"	pt.LOGIN_ID,\n" +
				"	t.FK_TRAINEE_ID,\n" +
				"	t.learnScore,\n" +
				"	c.id,\n" +
				"	c.LEARNSCORE\n" +
				"FROM\n" +
				"	pr_class_trainee t\n" +
				"JOIN pe_trainee pt ON pt.id = t.FK_TRAINEE_ID\n" +
				"LEFT JOIN pr_student_certificate c ON c.Fk_student_id = t.FK_TRAINEE_ID\n" +
				"AND c.`key` = t.FK_TRAINING_CLASS_ID\n" +
				"WHERE\n" +
				"	t.FK_TRAINING_CLASS_ID = '" + classId + "'";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String loginId = MyUtils.valueOf(objects[0]);
			String ptId = MyUtils.valueOf(objects[1]);
			String credit = MyUtils.valueOf(objects[2]);
			String certId = MyUtils.valueOf(objects[3]);
			String certCredit = MyUtils.valueOf(objects[4]);
			if (StringUtils.isBlank(credit)) {
				credit = "0";
			}
			if (StringUtils.isBlank(certCredit)) {
				certCredit = "0";
			}
			String[] strs = {ptId, credit, certId, certCredit};
			if (loginIdCertXuefenMap.containsKey(loginId)) {
				System.out.println("一个学员有多个证书：ptId=" + ptId);
			}
			loginIdCertXuefenMap.put(loginId, strs);
		}
		// 当前最大证书编号
		// 查询数据库中最大的证书编号
		sql = " select IFNULL(MAX(`CertificateNo`),0) as `CODE` from pr_student_certificate  where FK_SITE_ID ='ff80808155da5b850155dddbec9404c9' and CertificateNo like 'EEDS" + year + "%' ";
		list = SshMysqlWebtrn.getBySQL(sql);
		maxCertCode = String.valueOf(list.get(0)[0]);
	}
	
	public static void main(String[] args) {
		String classId2017 = "ff8080815e7696c1015e7a78d7f00313";
		String classId2016 = "ff8080815e86ebf2015e92c4faae070f";
		UpdateXuefenByYiaiwang updateXuefen = new UpdateXuefenByYiaiwang();
		updateXuefen.outputUpdateXuefenList(classId2017, "2017", "2017-07-01", null);
		System.exit(0);
	}
}

