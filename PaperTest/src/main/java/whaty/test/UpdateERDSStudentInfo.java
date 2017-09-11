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

import whaty.test.addAreaCode.AreaCode;

/** 
 * @className:UpdateERDSStudentInfo.java
 * @classDescription:
 * @author:yourname
 * @createTime:2017年8月29日
 */
public class UpdateERDSStudentInfo {
	
	private List<AreaCode> eedsAreaList;
	private Map<String, List<AreaCode>> hospitalMap;
	private Map<String, String> loginIdMap;
	private Map<String, String> deptMap;
	
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
		sql = "select id,LOGIN_ID from pe_trainee where (LOGIN_ID like 'dltq@%' or LOGIN_ID like 'erds@%' ) and FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
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
	}
	
	public void generalUpdateSqlList(){
		getEEDSAreaList();
		System.out.println("查询基础信息完毕");
		List<String> result = new ArrayList<>();
		List<String> addDepartList = new ArrayList<>();
		List<String> addHospitalList = new ArrayList<>();
		String path = "F:/whaty/eedsStudent2.xls";
		List<String[]> lineList = MyUtils.readExcel(path);
		for (int i = 0; i < lineList.size(); i++) {
			String[] strs = lineList.get(i);
			String county = MyUtils.valueOf(strs[0]).trim().replaceAll(" ", "").replaceAll("　", "");
			String workPlace = MyUtils.valueOf(strs[1]).trim().replaceAll(" ", "").replaceAll("　", "");
			String depart = MyUtils.valueOf(strs[2]).trim().replaceAll(" ", "").replaceAll("　", "");
			String name = MyUtils.valueOf(strs[3]).trim().replaceAll(" ", "").replaceAll("　", "");
			String loginId = MyUtils.valueOf(strs[4]).trim().replaceAll(" ", "").replaceAll("　", "");
			String mobile = MyUtils.valueOf(strs[5]).trim();
			
			// 判断学员是否存在
			String ptId = "";
			if (loginIdMap.containsKey(loginId)) {
				ptId = loginIdMap.get(loginId);
			} else {
				System.out.println("不存在此学员：" + loginId);
				continue;
			}
			
			// 查询区域id
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
			if (StringUtils.isEmpty(deptId) && !"无".equals(depart)) {
				System.out.println("自动添加科室：" + depart);
				deptId = MyUtils.uuid();
				addDepartList.addAll(addDepartment(depart, deptId));
				deptMap.put(depart, deptId);
			}
			
			
			// 处理手机号
			if (StringUtils.isNotBlank(mobile) && mobile.contains(" ")) {
				mobile = mobile.substring(0, mobile.indexOf(" "));
			}
			
			// 产生sql语句
			String sql = "update pe_trainee pt set pt.TRUE_NAME='" + name + "',pt.province='内蒙古自治区',pt.city='鄂尔多斯市',"
					+ "pt.county='" + county + "',pt.fk_area_id='" + areaId + "',pt.companyGroup='" + workPlaceId 
					+ "',pt.MOBILE='" + mobile + "',pt.department='" + deptId + "' where pt.ID='" + ptId + "';";
			result.add(sql);
		}
		String path0 = "E:/myJava/yiaiSql/20170830/";
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
