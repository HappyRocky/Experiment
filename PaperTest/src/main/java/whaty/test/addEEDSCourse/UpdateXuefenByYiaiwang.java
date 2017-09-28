package whaty.test.addEEDSCourse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
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
		List<String> updateXuefenList = new ArrayList<>();
		loginIdCreditYiaiMap = new HashMap<String, Integer[]>();
		String sql = "SELECT\n" +
				"	sum(xueshi) AS xueshi,\n" +
				"	MAX(finishDate),\n" +
				"	username\n" +
				"FROM\n" +
				"	(\n" +
				"		SELECT DISTINCT\n" +
				"			MUCCS.user_id AS userid,\n" +
				"			MU.username,\n" +
				"			MC.id AS courseid,\n" +
				"			MUID2. DATA  AS xueshi,\n" +
				"			FROM_UNIXTIME(MUCCS.timemodified) AS finishDate\n" +
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
				"		AND (MU.username like 'erds%' or MU.username like 'dltq@%')\n" +
//				("2017".equals(year) ? " AND MC.id not in (" + AddStudyRecordOld.noNeedAddMCIdConditions + ")  " : "") +
				"	) A\n" +
				"GROUP BY\n" +
				"	username";
		List<Object[]> list = SshMysqlYiaiwang.queryBySQL(sql);
		for (Object[] objects : list) {
			String xueshiStr = MyUtils.valueOf(objects[0]);
			String createDate = MyUtils.valueOf(objects[1]);
			String username = MyUtils.valueOf(objects[2]);
			
			// 判断在新平台有没有此学员
			if (!loginIdCertXuefenMap.containsKey(username)) {
				System.out.println("新平台" + year + "班级没有此学员：" + username);
				continue;
			}
			
			// 计算正确学分
			int xueshi = (int)((double)Double.valueOf(xueshiStr));
			int xuefen = (int)(xueshi / 3);
			xuefen = Math.min(xuefen, 25);
			
			// 读取以前的学分
			String[] strs = loginIdCertXuefenMap.get(username);
			String ptId = strs[0];
			int oldXuefen = Integer.valueOf(strs[1]);
			String certId = strs[2];
			int oldCertXuefen = Integer.valueOf(strs[3]);
			
			// 更新学分
			if (xuefen > oldXuefen) {
				String updateSql = "UPDATE pr_class_trainee set learnScore = " + xuefen + " where FK_TRAINEE_ID = '" + ptId + "' and FK_TRAINING_CLASS_ID = '" + classId + "' and learnScore < " + xuefen + ";";
				updateXuefenList.add(updateSql);
				oldXuefen = xuefen;
			}
			
			// 插入证书
			if (oldXuefen > 0 && ("2016".equals(year) || ("2017".equals(year) && oldXuefen >= 25))) {
				if (StringUtils.isBlank(certId)) { // 没有证书
					String insertSql =  generalAddEEDSCertificate(ptId, classId, oldXuefen, xueshi, createDate, year);
					if (StringUtils.isBlank(insertSql)) {
						System.out.println("生成证书编号失败：ptId=" + ptId);
					} else {
//						SshMysqlWebtrn.executeBySQL(insertSql); // 直接执行，后续生成证书编号还需要查询
						updateXuefenList.add(insertSql);
					}
				} else if (oldCertXuefen != oldXuefen) { // 证书学分不一致
					String insertSql = "update pr_student_certificate set LEARNSCORE='" + oldXuefen + "',LEARNTIME='" + oldXuefen * 3 + "' where id='" + certId + "';";
					updateXuefenList.add(insertSql);
				} 
			}
			
		}
		String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/updateXuefenList.sql";
		MyUtils.outputList(updateXuefenList, path1);
	}
	/**
	 * 生成插入鄂尔多斯证书的sql
	 * @param traineeId
	 * @param classId
	 * @return
	 */
	public String generalAddEEDSCertificate(String traineeId, String classId, int credit, int xueshi, String createDate, String year){
		
		// 查找站点、证书模版
		String modelId = "ff8080815df4f28a015df525a7040005";
		String siteId = "ff80808155da5b850155dddbec9404c9";
		
		// 生成证书编号
		String newCertNo = createCertificateNo(siteId, "EEDS", year);
		if (StringUtils.isBlank(newCertNo)) {
			return null;
		}
		
		// 最大证书编号更新
		maxCertCode = newCertNo;
		
		// 生成sql
		String newId = MyUtils.uuid();
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO `pr_student_certificate` ( ");
		sb.append(" `id`, ");
		sb.append(" `Fk_student_id`, ");
		sb.append(" `key`, ");
		sb.append(" `createDate`, ");
		sb.append(" `Fk_site_id`, ");
		sb.append(" `CertificateNo`, ");
		sb.append(" `FK_CERTIFICATE_MODEL_ID`, ");
		sb.append(" `PROJECTNO`, ");
		sb.append(" `LEARNTIME`, ");
		sb.append(" `LEARNSCORE` ");
		sb.append(" ) VALUES( ");
		sb.append(" '" + newId + "', ");
		sb.append(" '" + traineeId + "', ");
		sb.append(" '" + classId + "', ");
		sb.append(" '" + createDate + "', ");
		sb.append(" '" + siteId + "', ");
		sb.append(" '" + newCertNo + "', ");
		sb.append(" '" + modelId + "', ");
		sb.append(" 'EEDS', ");
		sb.append(" '" + credit * 3 + "', ");
		sb.append(" '" + credit + "' ");
		sb.append(" ) ON DUPLICATE KEY UPDATE LEARNSCORE=values(LEARNSCORE),LEARNTIME=values(LEARNTIME);");
		return sb.toString();
	}
	
	/**
	 * 生成证书编号
	 * @param siteId
	 * @param prefix 证书编号的前缀
	 * @return
	 */
	public String createCertificateNo(String siteId, String prefix, String currentYear) {
		String code = "";
		// 查询数据库中最大的证书编号
//		String sql = " select IFNULL(MAX(`CertificateNo`),0) as `CODE` from pr_student_certificate  where FK_SITE_ID ='" + siteId
//				+ "' and CertificateNo like '" + prefix + "%' ";
//		try {
//			List<Object[]> list = SshMysqlWebtrn.getBySQL(sql);
//			code = String.valueOf(list.get(0)[0]);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		code = maxCertCode;
		// 计算证书编号
		String newCode = "";
		if ("0".equals(code)) {
			newCode = currentYear + "1000001";
		} else {
			if (code.startsWith(prefix)) {
				code = code.substring(prefix.length());
			}
			String year = code.substring(0, 4);
			if (year.equals(currentYear)) {
				BigInteger serialCode = new BigInteger(code.substring(4, code.length()));
				BigInteger temp = new BigInteger("1");
				serialCode = serialCode.add(temp);
				newCode = year + String.valueOf(serialCode);
			} else {
				newCode = currentYear + "1000001";
			}
		}
		return prefix + newCode;
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
		// 查询数据库中最大的证书编号
		sql = " select IFNULL(MAX(`CertificateNo`),0) as `CODE` from pr_student_certificate  where FK_SITE_ID ='ff80808155da5b850155dddbec9404c9' and CertificateNo like 'EEDS" + year + "%' ";
		list = SshMysqlWebtrn.getBySQL(sql);
		maxCertCode = String.valueOf(list.get(0)[0]);
	}
	
	public static void main(String[] args) {
		String classId2017 = "ff8080815e7696c1015e7a78d7f00313";
		String classId2016 = "ff8080815e86ebf2015e92c4faae070f";
		UpdateXuefenByYiaiwang updateXuefen = new UpdateXuefenByYiaiwang();
		updateXuefen.outputUpdateXuefenList(classId2017, "2017", "2017-07-01", "2017-09-27");
//		updateXuefen.outputUpdateXuefenList(classId2016, "2016", "2016-07-01", "2017-07-01");
		System.exit(0);
	}
}

