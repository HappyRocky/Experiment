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
		List<String> updateXuefenList = new ArrayList<String>();
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
				" pt.LOGIN_ID in ('erds@3683.com','erds@3684.com','erds@3642.com','erds@4472.com','erds@3174.com','erds@3690.com','erds@3673.com','erds@3633.com','erds@3640.com','erds@3629.com','erds@3711.com','erds@3703.com','erds@3637.com','erds@3647.com','erds@3624.com','erds@3407.com','erds@3634.com','erds@3695.com','erds@3368.com','erds@3682.com','erds@3681.com','erds@4479.com','erds@3631.com','erds@3716.com','erds@3658.com','erds@3717.com','erds@3649.com','erds@3621.com','erds@4489.com','erds@3664.com','erds@4487.com','erds@3676.com','erds@3688.com','erds@2587.com','erds@3678.com','erds@3700.com','erds@3672.com','erds@3671.com','erds@3630.com','erds@4488.com','erds@3489.com','erds@3651.com','erds@3661.com','erds@3692.com','erds@3689.com','erds@3693.com','erds@3710.com','erds@3705.com','erds@3652.com','erds@3657.com','erds@3715.com','erds@3408.com','erds@4478.com','erds@4475.com','erds@3659.com','erds@3625.com','erds@3677.com','erds@3639.com','erds@3249.com','erds@3679.com','erds@3655.com','erds@3680.com','erds@4473.com','erds@4470.com','erds@2533.com','erds@3635.com','erds@3712.com','erds@3686.com','erds@3666.com','erds@3696.com','erds@4481.com','erds@3656.com','erds@3707.com','erds@3665.com','erds@3706.com','erds@3675.com','erds@3713.com','erds@3698.com','erds@3653.com','erds@3668.com','erds@3701.com','erds@3648.com','erds@3650.com','erds@3632.com','erds@2695.com','erds@3573.com','erds@3670.com','erds@3709.com','erds@3339.com','erds@2584.com','erds@3645.com','erds@2350.com','erds@3685.com','erds@3623.com','erds@3694.com','erds@3644.com','erds@3704.com','erds@3718.com','erds@3702.com','erds@3667.com','erds@4474.com','erds@4480.com','erds@4485.com','erds@3708.com','erds@2740.com','erds@2743.com','erds@3626.com','erds@3691.com','erds@3191.com','erds@3146.com','erds@4471.com','erds@4486.com','erds@3252.com','erds@3714.com','erds@3385.com','erds@3646.com','erds@3660.com','erds@4476.com','erds@3638.com','erds@3674.com','erds@3620.com','erds@3687.com','erds@3628.com','erds@3697.com','erds@3641.com','erds@3669.com','erds@4477.com','erds@3622.com','erds@3248.com','erds@3654.com','erds@3643.com','erds@3619.com','erds@3662.com','erds@3699.com','erds@3627.com') "+
				"AND ele.SCORE = '100.0'\n" +
//				"AND ele.result0 IS NOT NULL\n" +
				"AND ele.ELECTIVE_DATE >= '2017-07-01'\n" +
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
			
			// 有证书，则直接将学分置为25
			if (StringUtils.isNotBlank(certId)) {
				xuefen = 25;
			}
			
			// 更新学分
			if (xuefen > oldXuefen) {
				String updateSql = "UPDATE pr_class_trainee set learnScore = " + xuefen + " where FK_TRAINEE_ID = '" + ptId + "' and FK_TRAINING_CLASS_ID = '" + classId + "' and learnScore < " + xuefen + " limit 1;";
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
//						SshMysqlWebtrn.executeBySQL(insertSql); // 直接执行，后续生成证书编号还需要查询
						updateXuefenList.add(insertSql);
					}
				} else if (oldCertXuefen != oldXuefen) { // 证书学分不一致
					String insertSql = "update pr_student_certificate set LEARNSCORE='" + oldXuefen + "',LEARNTIME='" + oldXuefen * 3 + "' where id='" + certId + "' limit 1;";
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

