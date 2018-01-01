package whaty.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/** 
 * @className:ImportYiaiKouqiangUser.java
 * @classDescription:导入医爱口腔一期二期的学员
 * @author:yourname
 * @createTime:2017年7月17日
 */
public class ImportYiaiKouqiangUser {
	/**
	 * 输出学员excel
	 * @return
	 */
	public static List<String> generalInsertSqlList(String path, String classId, String payId, String eleId){
		
		List<String> lineList = MyUtils.readFile(path);
		List<Object[]> excelListList = new ArrayList<Object[]>();
		List<String> resultList = new ArrayList<String>();
		List<String> updateSqlList = new ArrayList<String>();
		System.out.println("共读取到" + (lineList.size() - 1) + "个学员");
		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
		int count = 0;
		for (int j = 1; j < lineList.size(); j++) {
			String line = lineList.get(j);
			String[] strs = line.split("\\|");
			Object[] objects = new Object[13];
			objects[0] = strs[3].trim(); // loginId
			objects[1] = strs[4].trim(); // 密码
			objects[2] = fixNull(strs[5]); // 姓名
			objects[3] = fixNull(strs[6]).trim(); // 性别
			objects[4] = fixNull(strs[7]).trim(); // 身份证
			objects[5] = fixNull(strs[17]).trim(); // 手机号
			objects[6] = fixNull(strs[9]).trim(); // 省
			objects[7] =  fixNull(strs[10]).trim(); // 市
			objects[8] =  ""; // 县
			objects[9] = ""; // IC卡号
			objects[10] = ""; // 科室
			objects[11] = fixNull(strs[14]).trim().substring(0, fixNull(strs[14]).length() - fixNull(strs[13]).length()); // 工作单位
			objects[12] = fixNull(strs[15]).trim(); // 详细地址
			
			if (StringUtils.isNotBlank(objects[0].toString())) {
				String loginId = objects[0].toString();
				
				// 手动特殊处理
				switch (objects[0].toString()) {
				case "sh01陆玮":
					loginId = "luwei1";
					break;
				case "sh01朱懿佳":
					loginId = "zhuyijia";
					break;
				case "课程辅导教师":
					loginId = "wuwenqian";
					break;
				case "test":
					loginId = "testbeiyi";
					break;
				case "sd01machunhui":
					loginId = "sd01machunhui2";
					break;
				case "wh01gongjun":
					loginId = "wh01gongjun2";
					break;
				case "ly01沈文健":
					loginId = "shenwenjian";
					break;
				case "sh01金红霞":
					loginId = "jinhongxia";
					break;
				default:
					break;
				}
				
				// 将汉字loginId改为手机号
				if(pattern.matcher(loginId).find()){
					if (loginId.startsWith("口腔护士0") || loginId.startsWith("口腔护士1")) { // 以口腔护士开头的为测试账号
						loginId = loginId.replace("口腔护士", "kouqianghushi");
					} else if (StringUtils.isNotBlank(fixNull(strs[17]))) { // 手机号不为空
						loginId = fixNull(strs[17]);
					} else {
						System.out.println(loginId + "含有汉字，且手机号为空");
						continue;
					}
				}
				
				if (!objects[0].toString().equals(loginId)) {
					System.out.println(objects[0] + " 改为 " + loginId);
					objects[0] = loginId;
				}
				
				// 将空的姓名改为用户名
				if (StringUtils.isBlank(fixNull(strs[5]))) {
					objects[2] = loginId;
				}
				
				excelListList.add(objects);
				resultList.add(objects[0].toString()); // 存放loginId
				
				// 更新学员信息，插入班级，先导入学员再开放这个函数
//				updateSqlList.addAll(generalUpdateList(objects, strs, classId));
				// 开启缴费流程
				updateSqlList.addAll(completePay(loginId, classId, payId, eleId));
				count ++;
			}
			
			if (updateSqlList.size() > 0 && j % 100 == 0) {
				System.out.println("已处理完" + j + "个学员");
			}
		}
		String excelPath = "F:/myJava/yiaiSql/20170719/traineeExcel.xls";
		String[] titles = {"*用户名","密码","*姓名","性别","身份证","手机号","*省","*市","县","IC卡号","科室","工作单位","详细地址"};
		MyUtils.outputExcel(excelPath, titles, excelListList);
		String loginIdPath = "F:/myJava/yiaiSql/20170719/loginId.txt";
		MyUtils.outputList(resultList, loginIdPath);
		String updatePath = "F:/myJava/yiaiSql/20170719/updateRegisterTime.sql";
		MyUtils.outputList(updateSqlList, updatePath);
		System.out.println("共处理了" + count + "个学员");
		return resultList;
	}
	
	/**
	 * 更新学员的注册时间、学历，插入到班级
	 * @param objects
	 * @param strs
	 * @param classId
	 * @return
	 */
	public static List<String> generalUpdateList(Object[] objects, String[] strs, String classId){
		List<String> updateSqlList = new ArrayList<String>();
		String ptId = "";
		// 查询学员id
		String querySql = "select pt.id from pe_trainee pt where pt.FK_SITE_ID='ff80808155da5b850155dddbec9404c9' and pt.LOGIN_ID='" + objects[0] + "'";
		List<Object[]> objs = SshMysqlWebtrn.queryBySQL(querySql);
		if (CollectionUtils.isNotEmpty(objs)) {
			ptId = objs.get(0)[0].toString();
			if (StringUtils.isNotBlank(ptId)) {
				// 更新注册时间
				if (StringUtils.isNotBlank(fixNull(strs[20]))) {
					String updateSql = "update sso_user su set su.registerDate='" + fixNull(strs[20]) + "' where su.LOGIN_ID='" + objects[0] + "' and su.FK_SITE_ID='ff80808155da5b850155dddbec9404c9';";
					updateSqlList.add(updateSql);
				}
				// 更新学历
				if (StringUtils.isNotBlank(fixNull(strs[19]))) {
					String oldEduction = fixNull(strs[19]);
					String education = oldEduction;
					if (oldEduction.equals("本科") || oldEduction.equals("专科")) {
						education = "专科/本科";
					} else if (oldEduction.equals("硕士研究生")) {
						education = "硕士";
					}else if (oldEduction.equals("博士研究生")) {
						education = "博士";
					}
					String updateSql = "update pe_trainee pt set pt.EDUCATION='" + education + "' where pt.id='" + ptId + "';";
					updateSqlList.add(updateSql);
				}
				// 插入到班级
				String insertId = MyUtils.uuid();
				String insertSql = "INSERT INTO `pr_class_trainee` ( " +
						"	`ID`, " +
						"	`FK_TRAINEE_ID`, " +
						"	`FK_TRAINING_CLASS_ID`, " +
						"	`FK_SITE_ID`, " +
						"	`START_SERVICE_DATE`, " +
						"	`END_SERVICE_DATE`, " +
						"	`modifyDate`, " +
						"	`learnTime`, " +
						"	`learnScore` " +
						") VALUES ( " +
						"		'" + insertId + "', " +
						"		'" + ptId + "', " +
						"		'" + classId + "', " +
						"		'ff80808155da5b850155dddbec9404c9', " +
						"		NOW(), " +
						"		'2017-12-31 23:59:59', " +
						"		NOW(), " +
						"		'0', " +
						"		'0' " +
						"	);";
				updateSqlList.add(insertSql);
			}
		}
		if (updateSqlList.size() != 3) {
			System.out.println("平台没有此学员：" + strs[0] + " " + strs[1] + " " + strs[2] + " " + strs[3]);
		}
		return updateSqlList;
	}
	
	/**
	 * 完成缴费流程，开启选课流程
	 * @param loginId
	 * @param classId
	 * @param payId
	 * @param eleId
	 * @return
	 */
	public static List<String> completePay(String loginId, String classId, String payId, String eleId){
		List<String> updateSqlList = new ArrayList<String>();
		String ptId = "";
		// 查询学员id、学员班级id
		String querySql = "select pt.id from pe_trainee pt where pt.FK_SITE_ID='ff80808155da5b850155dddbec9404c9' and pt.LOGIN_ID='" + loginId + "'";
		List<Object[]> objs = SshMysqlWebtrn.queryBySQL(querySql);
		if (CollectionUtils.isNotEmpty(objs)) {
			ptId = objs.get(0)[0].toString();
			if (StringUtils.isNotBlank(ptId)) {
				// 插入到班级
				String insertId = MyUtils.uuid();
				String insertSql = "INSERT INTO `pr_class_trainee` ( " +
						"	`ID`, " +
						"	`FK_TRAINEE_ID`, " +
						"	`FK_TRAINING_CLASS_ID`, " +
						"	`FK_SITE_ID`, " +
						"	`START_SERVICE_DATE`, " +
						"	`END_SERVICE_DATE`, " +
						"	`modifyDate`, " +
						"	`learnTime`, " +
						"	`assessResult`, " +
						"	`learnScore` " +
						") VALUES ( " +
						"		'" + insertId + "', " +
						"		'" + ptId + "', " +
						"		'" + classId + "', " +
						"		'ff80808155da5b850155dddbec9404c9', " +
						"		NOW(), " +
						"		'2017-12-31 23:59:59', " +
						"		NOW(), " +
						"		'0', " +
						"		'1', " +
						"		'0' " +
						"	) ON DUPLICATE KEY UPDATE assessResult=VALUES(assessResult);";
				updateSqlList.add(insertSql);
				// 完成缴费流程
				insertId = MyUtils.uuid();
				insertSql = "INSERT INTO `pr_training_module` ( " +
						"	`id`, " +
						"	`create_time`, " +
						"	`fk_trainee_id`, " +
						"	`fk_module_id`, " +
						"	`fk_site_id`, " +
						"	`status` " +
						")VALUES	( " +
						"		'" + insertId + "', " +
						"		NOW(), " +
						"		'" + ptId + "', " +
						"		'" + payId + "', " +
						"		'ff80808155da5b850155dddbec9404c9', " +
						"		'9dfe07fb9c1511e6975b00251113d11d' " +
						"	);";
				updateSqlList.add(insertSql);
				// 开启选课流程
				insertId = MyUtils.uuid();
				insertSql = "INSERT INTO `pr_training_module` ( " +
						"	`id`, " +
						"	`create_time`, " +
						"	`fk_trainee_id`, " +
						"	`fk_module_id`, " +
						"	`fk_site_id`, " +
						"	`status` " +
						")VALUES	( " +
						"		'" + insertId + "', " +
						"		NOW(), " +
						"		'" + ptId + "', " +
						"		'" + eleId + "', " +
						"		'ff80808155da5b850155dddbec9404c9', " +
						"		'bc2dec18d8cc11e6bb8900251113d11d' " +
						"	);";
				updateSqlList.add(insertSql);
			}
		}
		if (updateSqlList.size() == 0) {
			System.out.println("平台没有此学员：" + loginId);
		}
		return updateSqlList;
	}
	
	public static String fixNull(String str){
		 return (str == null || str.trim().equals("NULL") || str.trim().equals("null")) ? "" : str.trim();
	}
	
	public static void main(String[] args) {
		// 口腔护士一期
		String path1 = "F:/myJava/yiaiSql/mols_user_yiqi.txt";
		String classId1 = "ff8080815cd034e3015cd42042520237"; // 口腔培训1班
		String payId1 = "ff8080815cf238a5015cf2510aa80022"; // 1班的缴费流程id，pe_training_module
		String eleId1 = "ff8080815d0e0622015d120787fe2b3d"; // 1班的选课流程id，pe_training_module
		// 口腔护士二期
		String path2 = "F:/myJava/yiaiSql/mols_user_erqi.txt";
		String classId2 = "ff8080815cd034e3015cd420b9c90238"; // 口腔培训2班
		String payId2 = "ff8080815cf238a5015cf25159a70024";
		String eleId2 = "ff8080815d0e07c0015d1208d03f50b8";
//		generalInsertSqlList(path1, classId1, payId1, eleId1);
		generalInsertSqlList(path2, classId2, payId2, eleId2);
		System.exit(0);
	}
}
