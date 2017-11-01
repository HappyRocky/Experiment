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

/** 
 * @className:UpdateXuefen.java
 * @classDescription:更新鄂尔多斯学员的学分
 * @author:yourname
 * @createTime:2017年9月16日
 */
public class UpdateXuefen {
	
	private Map<String, String[]> ptIdCertXuefenMap; // <ptId,[获得学分，证书Id，证书学分]>
	private String maxCertCode; // 当前最大证书编号

	public void outputUpdateXuefenSql(String classId, String year){
		System.out.println("初始化");
		init(classId, year);
		System.out.println("统计学员学分");
		List<String> updateXuefenList = new ArrayList<>();
		String sql = "SELECT\n" +
				"	pt.id,\n" +
//				"	sum(ele.result0),\n" +
				"	count(ele.id),\n" +
				"	max(ele.ELECTIVE_DATE)\n" +
				"FROM\n" +
				"	pe_tch_elective ele\n" +
				"JOIN pe_open_course poc ON poc.id = ele.FK_OPENCOURSE_ID\n" +
				"AND poc.FK_TRAIN_CLASS = '" + classId + "'\n" +
				"JOIN pe_trainee pt ON pt.id = ele.FK_TRAINEE_ID\n" +
				"WHERE\n" +
//				"	(\n" +
//				"		pt.LOGIN_ID LIKE 'erds@%'\n" +
//				"		OR pt.LOGIN_ID LIKE 'dltq@%'\n" +
//				"	)\n" +
				" pt.LOGIN_ID in ('erds@1836.com','erds@5592.com','erds@3220.com','erds@2069.com','erds@2070.com','erds@2699.com','erds@2667.com') "+
				"AND ele.SCORE = '100.0'\n" +
				"AND ele.result0 IS NOT NULL\n" +
				"GROUP BY\n" +
				"	pt.id";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		System.out.println("共有" + list.size() + "名学员有学分记录");
		for (int i = 0; i < list.size(); i++) {
			Object[] objects = list.get(i);
			String ptId = MyUtils.valueOf(objects[0]);
			String xueshiStr = MyUtils.valueOf(objects[1]);
			String createDate = MyUtils.valueOf(objects[2]);
			int xueshi = (int)((double)Double.valueOf(xueshiStr));
			int xuefen = (int)(xueshi / 3);
			xuefen = Math.min(xuefen, 25);
			
			// 以前的学分
			int oldXuefen = 0;
			int oldCertXuefen = 0;
			String certId = "";
			if (ptIdCertXuefenMap.containsKey(ptId)) {
				String[] strs = ptIdCertXuefenMap.get(ptId);
				oldXuefen = Integer.valueOf(strs[0]);
				certId = strs[1];
				oldCertXuefen = Integer.valueOf(strs[2]);
			}
			
			// 更新学分
			if (xuefen > oldXuefen) {
				String updateSql = "UPDATE pr_class_trainee set learnScore = " + xuefen + " where FK_TRAINEE_ID = '" + ptId + "' and FK_TRAINING_CLASS_ID = '" + classId + "' and learnScore < " + xuefen + ";";
				updateXuefenList.add(updateSql);
				oldXuefen = xuefen;
			}
			
			// 插入证书
			if (oldXuefen >= 25) {
				if (StringUtils.isBlank(certId)) { // 没有证书
					String insertSql =  generalAddEEDSCertificate(ptId, classId, xuefen, xueshi, createDate,year);
					if (StringUtils.isBlank(insertSql)) {
						System.out.println("生成证书编号失败：ptId=" + ptId);
					} else {
						SshMysqlWebtrn.executeBySQL(insertSql); // 直接执行，后续生成证书编号还需要查询
//						updateXuefenList.add(insertSql);
					}
				} else if (oldCertXuefen != oldXuefen) { // 证书学分不一致
					String insertSql = "update pr_student_certificate set LEARNSCORE='" + oldXuefen + "',LEARNTIME='" + oldXuefen * 3 + "' where id='" + certId + "';";
					updateXuefenList.add(insertSql);
				} 
			}
			
			
			if (i % 100 == 0) {
				System.out.println("已完成第" + i + "个");
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
		if (year.equals("2016")) {
			modelId = "";
		}
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
		String sql = " select IFNULL(MAX(`CertificateNo`),0) as `CODE` from pr_student_certificate  where FK_SITE_ID ='" + siteId
				+ "' and CertificateNo like '" + prefix + "%' ";
		try {
			List<Object[]> list = SshMysqlWebtrn.getBySQL(sql);
			code = String.valueOf(list.get(0)[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		code = maxCertCode;
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
		ptIdCertXuefenMap = new HashMap<String, String[]>();
		String sql = "SELECT\n" +
				"	t.FK_TRAINEE_ID,\n" +
				"	t.learnScore,\n" +
				"	c.id,\n" +
				"	c.LEARNSCORE\n" +
				"FROM\n" +
				"	pr_class_trainee t\n" +
				"LEFT JOIN pr_student_certificate c ON c.Fk_student_id = t.FK_TRAINEE_ID\n" +
				"AND c.`key` = t.FK_TRAINING_CLASS_ID\n" +
				"WHERE\n" +
				"	t.FK_TRAINING_CLASS_ID = '" + classId + "'";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String ptId = MyUtils.valueOf(objects[0]);
			String credit = MyUtils.valueOf(objects[1]);
			String certId = MyUtils.valueOf(objects[2]);
			String certCredit = MyUtils.valueOf(objects[3]);
			if (StringUtils.isBlank(credit)) {
				credit = "0";
			}
			if (StringUtils.isBlank(certCredit)) {
				certCredit = "0";
			}
			String[] strs = {credit, certId, certCredit};
			if (ptIdCertXuefenMap.containsKey(ptId)) {
				System.out.println("一个学员有多个证书：ptId=" + ptId);
			}
			ptIdCertXuefenMap.put(ptId, strs);
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
		UpdateXuefen updateXuefen = new UpdateXuefen();
		updateXuefen.outputUpdateXuefenSql(classId2017, "2017");
		System.exit(0);
	}
}

