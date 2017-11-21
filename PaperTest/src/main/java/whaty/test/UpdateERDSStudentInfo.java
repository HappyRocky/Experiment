package whaty.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.addAreaCode.AreaCode;

/** 
 * @className:UpdateERDSStudentInfo.java
 * @classDescription:根据表格，对科室、手机号、身份证、专业等信息进行更新
 * @author:yourname
 * @createTime:2017年8月29日
 */
public class UpdateERDSStudentInfo {
	
	private List<AreaCode> eedsAreaList;
	private Map<String, List<AreaCode>> hospitalMap;
	private Map<String, String> loginIdMap;
	private Map<String, String> deptMap;
	private Map<String, String> titleMap;
	private Map<String, String> educationMap;
	private Map<String, String> ssoLoginIdMap;
	
	public static void main(String[] args) {
		UpdateERDSStudentInfo updateERDSStudentInfo = new UpdateERDSStudentInfo();
		updateERDSStudentInfo.generalUpdateSqlList();
		System.exit(0);
	}
	
	/**
	 * 计算鄂尔多斯下的所有子节点
	 */
	public void getEEDSAreaList(){
		// 计算鄂尔多斯下的所有子节点
		hospitalMap = new HashMap<String, List<AreaCode>>();
		String sql = "select id,name,level_code from pe_area where fk_parent_id='c311a57e6b5c11e683b100251113d11d' ";
		List<Object[]> result = SshMysqlWebtrn.getBySQL(sql);
		eedsAreaList = new ArrayList<>();
		for (Object[] objects : result) {
			AreaCode areaCode = new AreaCode(objects[0].toString(), objects[1].toString(), 0, objects[2].toString(), "c311a57e6b5c11e683b100251113d11d");
			eedsAreaList.add(areaCode);
			// 得到旗下的医院
			sql = "select id,name,level_code from pe_area where fk_parent_id='" + areaCode.getId() + "'";
			List<Object[]> hospitalList = SshMysqlWebtrn.getBySQL(sql);
			List<AreaCode> hospitalList2 = new ArrayList<>();
			for (Object[] hosps : hospitalList) {
				AreaCode hosp = new AreaCode(hosps[0].toString(), hosps[1].toString(), 0, hosps[2].toString(), areaCode.getId());
				hospitalList2.add(hosp);
			}
			hospitalMap.put(areaCode.getId(), hospitalList2);
		}
		// 得到所有鄂尔多斯学员的用户名
		loginIdMap = new HashMap<String, String>();
		sql = "select id,LOGIN_ID from pe_trainee where (LOGIN_ID like 'erds@%' ) and FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		List<Object[]> loginList = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : loginList) {
			loginIdMap.put(objects[1].toString(), objects[0].toString());
		}
		// 得到二级科室
		deptMap = new HashMap<String, String>();
		sql = "select id,`name` as num from pe_department where fk_site_id='ff80808155da5b850155dddbec9404c9' and `level`='2'";
		List<Object[]> deptList = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : deptList) {
			deptMap.put(objects[1].toString(), objects[0].toString());
		}
		// 得到职称
		titleMap = new HashMap<String, String>();
		sql = "select id,`NAME` from enum_const where NAMESPACE='FlagCareer' and FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		List<Object[]> titleList = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : titleList) {
			titleMap.put(objects[1].toString(), objects[0].toString());
		}
		// 得到学历
		educationMap = new HashMap<String, String>();
		sql = "select id,`NAME` from enum_const where NAMESPACE='educationType'";
		titleList = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : titleList) {
			educationMap.put(objects[1].toString(), objects[0].toString());
		}
		// 得到用户中心的loginID和id
//		ssoLoginIdMap = new HashMap<String, String>();
//		sql = "select username,id from ucenter_user where username like 'erds@%'";
//		titleList = SshMysqlSso.getBySQL(sql);
//		for (Object[] objects : titleList) {
//			ssoLoginIdMap.put(objects[0].toString(), objects[1].toString());
//		}
	}
	
	public void generalUpdateSqlList(){
		getEEDSAreaList();
		System.out.println("查询基础信息完毕");
		String[] fileNames = {"杭锦旗专业技术人员信息汇总表-2017年"};
		List<String> result = new ArrayList<>();
		List<String> addDepartList = new ArrayList<>();
		List<String> addHospitalList = new ArrayList<>();
		List<String> updateSsoMobile = new ArrayList<>();
//		String path = "F:/whaty/医爱数据库迁移/1-达拉特旗2017年继续医学教育专业课统一培训考核报名汇总表2-2.xls";
//		String path = "F:/whaty/医爱数据库迁移/1-达拉特旗人民医院北大医学网花名册2-1.xls";
		for (String fileName : fileNames) {
			String path = "F:/whaty/医爱数据库迁移/" + fileName + ".xls";
			for (int j = 1; j <= 3; j++) {
				if (j != 1) {
					continue;
				}
				List<String[]> lineList = MyUtils.readExcel(path, j);
				for (int i = 0; i < lineList.size(); i++) {
					String[] strs = lineList.get(i);
					String loginId = MyUtils.valueOf(strs[0]).trim().replaceAll(" ", "").replaceAll("　", "");
					String name = MyUtils.valueOf(strs[2]).trim().replaceAll(" ", "").replaceAll("　", ""); 
					String workPlace = MyUtils.valueOf(strs[3]).trim().replaceAll(" ", "").replaceAll("　", "");
					String idcard = MyUtils.valueOf(strs[4]).trim().replaceAll(" ", "").replaceAll("　", "");
					String depart = MyUtils.valueOf(strs[5]).trim().replaceAll(" ", "").replaceAll("　", "");
					String title = MyUtils.valueOf(strs[6]).trim().replaceAll(" ", "").replaceAll("　", "");
					String education = MyUtils.valueOf(strs[7]).trim().replaceAll(" ", "").replaceAll("　", "");
					String major = MyUtils.valueOf(strs[8]).trim().replaceAll(" ", "").replaceAll("　", "");
					String mobile = MyUtils.valueOf(strs[9]).trim().replaceAll(" ", "").replaceAll("　", "");
					String county = "杭锦旗";
					
					if (StringUtils.isBlank(loginId)) {
						continue;
					}
					
					// 判断学员是否存在
					String ptId = "";
					if (loginIdMap.containsKey(loginId)) {
						ptId = loginIdMap.get(loginId);
					} else {
						System.out.println("不存在此学员：" + loginId);
						continue;
					}
					
					// 查询区域id
					if (workPlace.contains("准旗")) {
						workPlace = workPlace.replace("准旗", "准格尔旗");
					}
					String areaId = "";
					AreaCode parentArea = null;
					String workPlaceId = "";
					if (StringUtils.isNotBlank(county)) {
						for (AreaCode areaCode : eedsAreaList) {
							if (areaCode.getName().equals(county)) {
								areaId = areaCode.getId();
								parentArea = areaCode;
								break;
							}
						}
						if (StringUtils.isEmpty(areaId)) {
							System.out.println("区域不存在：" + county);
							continue;
						}
						
						// 查询单位id
						List<AreaCode> hospitalList = hospitalMap.get(areaId);
						for (AreaCode areaCode : hospitalList) {
							if (areaCode.getName().equals(workPlace)) {
								workPlaceId = areaCode.getId();
								break;
							}
						}
						if (StringUtils.isEmpty(workPlaceId)) {
							for (AreaCode areaCode : hospitalList) {
								if (areaCode.getName().contains(workPlace) || workPlace.contains(areaCode.getName())) {
									workPlaceId = areaCode.getId();
									break;
								}
							}
						}
						if (StringUtils.isEmpty(workPlaceId)) {
							System.out.println("自动添加单位：" + parentArea.getName() + " 下的 " + workPlace);
							// 添加新单位
							Map map = addNewHospital(hospitalList, parentArea, workPlace);
							addHospitalList.add(map.get("sql").toString());
							AreaCode newAreaCode = (AreaCode)map.get("areaCode");
							hospitalList.add(newAreaCode);
							workPlaceId = newAreaCode.getId();
						}
					}
					
					// 查询科室id
					if (depart.contains("、")) {
						depart = depart.substring(0, depart.indexOf("、"));
					}
					if (depart.contains("-->")) {
						depart = depart.substring(depart.lastIndexOf("-->") + 3);
					}
					if (depart.contains("医保")) {
						depart = "医保科";
					}
					if (depart.contains("B超")) {
						depart = "B超室";
					}
					if (depart.contains("心电图")) {
						depart = "心电图室";
					}
					if (depart.contains("CT")) {
						depart = "CT室";
					}
					if (depart.contains("总务后勤科")) {
						depart = "后勤科";
					}
					if (depart.contains("血液透析科")) {
						depart = "血液透析室";
					}
					if (depart.contains("妇科")) {
						depart = "妇科";
					}
					if (depart.contains("挂号")) {
						depart = "挂号";
					}
					if (depart.contains("放射")) {
						depart = "放射科";
					}
					if (depart.contains("接种预防")) {
						depart = "预防接种";
					}
					String deptId = "";
					if (deptMap.containsKey(depart)) {
						deptId = deptMap.get(depart);
					} else {
						for (Entry<String, String> entry : deptMap.entrySet()) {
							String deptName = entry.getKey();
							if (deptName.contains(depart)) {
								deptId = entry.getValue();
								break;
							}
						}
					}
					
					// 如果没有匹配到，则把修改最后一个字再匹配一次
					if (StringUtils.isEmpty(deptId)) {
						String secondDept = "";
						if (depart.endsWith("科")) {
							secondDept = depart.substring(0, depart.length() - 1) + "室";
						} else if (depart.endsWith("室")) {
							secondDept = depart.substring(0, depart.length() - 1) + "科";
						}
						if (StringUtils.isNotBlank(secondDept)) {
							if (deptMap.containsKey(secondDept)) {
								deptId = deptMap.get(secondDept);
							} else {
								for (Entry<String, String> entry : deptMap.entrySet()) {
									String deptName = entry.getKey();
									if (deptName.contains(secondDept)) {
										deptId = entry.getValue();
										break;
									}
								}
							}
						}
					}
					
					if (StringUtils.isEmpty(deptId) && !"无".equals(depart)) {
						System.out.println("自动添加科室：" + depart);
						deptId = MyUtils.uuid();
						addDepartList.addAll(addDepartment(depart, deptId));
						deptMap.put(depart, deptId);
					}
					
					// 查询职称id
					String titleId = "";
					if (StringUtils.isNotBlank(title) && !title.equals("樊圆圆")) {
						if (title.contains("（")) {
							title = title.substring(0, title.indexOf("（"));
						}
						if (title.contains("、")) {
							title = title.substring(0, title.indexOf("、"));
						}
						if (title.contains("，")) {
							title = title.substring(0, title.indexOf("，"));
						}
						if (title.contains("执业医")) {
							title = "执业医师";
						}
						if (title.contains("助理医")) {
							title = "助理医师";
						}
						if (title.contains("副主任医")) {
							title = "副主任医师";
						} else if (title.contains("主任医")) {
							title = "主任医师";
						}
						if (title.contains("主治医")) {
							title = "主治医师";
						}
						if (title.equals("正高")) {
							title = "正高级";
						}
						if (title.equals("副高")) {
							title = "副高级";
						}
						if (title.equals("高级工")) {
							title = "高级工程师";
						} 
						if (title.equals("初级医士")) {
							title = "初级医师";
						}
						if (title.equals("无证")) {
							title = "无职称";
						}
						if (title.equals("员级")) {
							title = "研究员";
						}
						if (title.equals("执业中西医医师")) {
							title = "中西医结合医师";
						}
						if (title.equals("中医执业药师")) {
							title = "中医药师";
						}
						if (title.equals("执业蒙医医师")) {
							title = "执业医师";
						}
						if (title.equals("检验技士")) {
							title = "检验师";
						}
						if (title.contains("放射医学技术")) {
							title = "技术员";
						}
						if (title.contains("\n")) {
							title = title.replaceAll("\n", "");
						}
						if (titleMap.containsKey(title)) {
							titleId = titleMap.get(title);
						} 
						if (StringUtils.isEmpty(titleId) && !"无".equals(title)) {
							System.out.println("自动添加职称：" + title);
							titleId = MyUtils.uuid();
							addDepartList.addAll(addTitle(title, titleId));
							titleMap.put(title, titleId);
						}
					}
					
					// 查询学历id
					String educationId = "";
					if (StringUtils.isNotBlank(education)) {
						if (education.contains("（")) {
							education = education.substring(0, education.indexOf("（"));
						}
						if (education.contains("大学本科") || education.contains("大学专科")) {
							education = education.replace("大学", "");
						}
						if (education.equals("大学")) {
							education = "本科";
						}
						if (education.equals("大学大专")) {
							education = "专科";
						}
						if (education.contains("中专技校")) {
							education = "专科";
						}
						if (education.contains("等专科")) {
							education = "专科";
						}
						if (education.contains("硕士研究生")) {
							education = "硕士";
						}
						if (educationMap.containsKey(education)) {
							educationId = educationMap.get(education);
						} 
						if (StringUtils.isEmpty(educationId) && !"无".equals(education)) {
							System.out.println("自动添加学历：" + education);
							educationId = MyUtils.uuid();
							addDepartList.addAll(addEducation(education, educationId));
							educationMap.put(education, educationId);
						}
					}

					// 产生sql语句
					String sql = "update pe_trainee pt set pt.TRUE_NAME='" + name + "',pt.CARD_NO='" + idcard + "',pt.MOBILE='" + mobile + "',pt.county='" + county + "',pt.fk_area_id='" + areaId + "',pt.companyGroup='" + workPlaceId 
							+ "',pt.department='" + deptId + "',pt.career='" + titleId + "',pt.EDUCATION='" + education + "',pt.presentMajor='" + major + "' where pt.ID='" + ptId + "';";
					result.add(sql);
					// 去掉其他学员的重复身份证
					sql = "update pe_trainee pt set pt.qqNumber=pt.CARD_NO,pt.CARD_NO = null where pt.ID != '" + ptId + "' and pt.CARD_NO='" + idcard + "' and pt.FK_SITE_ID='ff80808155da5b850155dddbec9404c9';";
					result.add(sql);
				}
			}
		}
		
		String path0 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/";
		String path1 = path0 + "addDepart.sql";
		String path2 = path0 + "addHospital.sql";
		String path3 = path0 + "updateEEDSStudentInfo.sql";
		MyUtils.outputList(addDepartList, path1);
		MyUtils.outputList(addHospitalList, path2);
		MyUtils.outputList(result, path3);
		System.out.println("需要添加的单位个数：" + addHospitalList.size() + "，需要添加的科室个数：" + addDepartList.size());
	}
	
	
	public List<String> addDepartment(String name, String id2){
		List<String> resultList = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		int rand = (int) (Math.random() * 9000) + 1000;
		String code2 = "yiai" + sdf.format(new Date()) + rand;
		String id1 = "ff8080815e2d4f47015e2d5676ed0004";
		resultList.add("INSERT INTO `pe_department` (`id`, `name`, `code`, `createDate`, `level`, `fk_parent_id`, `fk_site_id`) VALUES ('" + id2 + "', '" + name + "', '" + code2 + "', now(), '2', '" + id1 + "', 'ff80808155da5b850155dddbec9404c9');");
		return resultList;
	}
	
	public List<String> addTitle(String name, String id2){
		List<String> resultList = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		int rand = (int) (Math.random() * 9000) + 1000;
		String code2 = "yiai" + sdf.format(new Date()) + rand;
		String sql = "INSERT INTO `enum_const` (`ID`, `NAME`, `CODE`, `NAMESPACE`, `IS_DEFAULT`, `CREATE_DATE`, `NOTE`, `FK_SITE_ID`) VALUES ('" + id2 + "', '" + name + "', '" + code2 + "', 'FlagCareer', '', now(), '医爱职称管理', 'ff80808155da5b850155dddbec9404c9') ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		return resultList;
	}
	
	public List<String> addEducation(String name, String id2){
		List<String> resultList = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		int rand = (int) (Math.random() * 9000) + 1000;
		String code2 = sdf.format(new Date()) + rand;
		String sql = "INSERT INTO `enum_const` (`ID`, `NAME`, `CODE`, `NAMESPACE`, `IS_DEFAULT`, `CREATE_DATE`, `NOTE`, `FK_SITE_ID`) VALUES ('" + id2 + "', '" + name + "', '" + code2 + "', 'educationType', '0', now(), '学历等级', null) ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		return resultList;
	}
	
	public Map addNewHospital(List<AreaCode> hospitalList, AreaCode parent, String newName){
		int levelCodeMax = 0; // levelCode的最后层级的最大值
		for (AreaCode areaCode : hospitalList) {
			String curName = MyUtils.valueOf(areaCode.getName());
			String curLevelCode = MyUtils.valueOf(areaCode.getLevelCode());
			if (StringUtils.isNotBlank(curLevelCode)) { // 更新 levelCodeMax
				if (curLevelCode.endsWith("/")) { // 去掉最后一个斜线
					curLevelCode = curLevelCode.substring(0, curLevelCode.length() - 1);
				}
				int idx = curLevelCode.lastIndexOf("/");
				if (idx >= 0) {
					int lastCode = Integer.parseInt(String.valueOf(curLevelCode.substring(idx + 1)));
					levelCodeMax = Math.max(levelCodeMax, lastCode);
				}
			}
		}
		int newLevel = 4;
		String parentLevelCode = parent.getLevelCode();
		if (!parentLevelCode.endsWith("/")) {
			parentLevelCode = parentLevelCode + "/";
		}
		String newLevelCode = parentLevelCode + (levelCodeMax + 1) + "/";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		int rand = (int) (Math.random() * 9000) + 1000;// 产生1000-9999的随机数
		String newCode = sdf.format(new Date()) + rand;
		String newId = UUID.randomUUID().toString().replace("-", "");
		StringBuffer insertSb = new StringBuffer();
		insertSb.append(" INSERT INTO `pe_area` ( ");
		insertSb.append(" 	`id`, ");
		insertSb.append(" 	`name`, ");
		insertSb.append(" 	`code`, ");
		insertSb.append(" 	`createDate`, ");
		insertSb.append(" 	`level`, ");
		insertSb.append(" 	`level_code`, ");
		insertSb.append(" 	`fk_parent_id`, ");
		insertSb.append(" 	`fk_site_id` ");
		insertSb.append(" ) VALUES ( ");
		insertSb.append(" 		'" + newId + "', ");
		insertSb.append(" 		'" + newName + "', ");
		insertSb.append(" 		'" + newCode + "', ");
		insertSb.append(" 		NOW(), ");
		insertSb.append(" 		'" + newLevel + "', ");
		insertSb.append(" 		'" + newLevelCode + "', ");
		insertSb.append(" 		'" + parent.getId() + "', ");
		insertSb.append(" 		'ff80808155da5b850155dddbec9404c9'); ");
		Map result = new HashMap<String, Object>();
		result.put("sql", insertSb.toString());
		result.put("areaCode", new AreaCode(newId, newName, 0, newLevelCode, parent.getId()));
		return result;
	}
}

