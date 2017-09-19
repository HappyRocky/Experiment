package whaty.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;

/** 
 * @className:MatchUser.java
 * @classDescription:webtrn中导入的医爱鄂尔多斯学员，新建的loginId，通过姓名、身份证等匹配到旧数据库的用户，将userId迁移到新数据库
 * @author:yourname
 * @createTime:2017年9月14日
 */
public class MatchUser {
	private List<List<String>> yiaiUserInfoList; 
	
	public void matchUser(){
		System.out.println("查询原平台信息");
		init();
		int maxSize = 3000;
		System.out.println("查询新平台信息");
		String sql = "SELECT\n" +
				"	count(*)\n" +
				"FROM\n" +
				"	pe_trainee pt\n" +
				"WHERE\n" +
				"	pt.LOGIN_ID LIKE 'erds2017@%'  and pt.var0 is null\n" +
				"AND pt.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'";
		List<Object[]> countList = SshMysqlWebtrn.queryBySQL(sql);
		int num = Integer.valueOf(countList.get(0)[0].toString());
		int second = num / maxSize + 1;
		
//		second = 1;
		
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		String date = DateUtils.getToday();
		List<String> userIdList = new ArrayList<String>(); // 已经使用过的userId
		List<String> ptIdList = new ArrayList<String>(); // 已经使用过的ptId
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
			List<String> updateSqlList = new ArrayList<String>();
			Map<String, String> matchedMap = new HashMap<String, String>(); // <ptId,userId> 已经正确匹配上的信息
			
			sql = "SELECT\n" +
					"	pt.ID,	\n" +
					"	pt.LOGIN_ID,\n" +
					"	pt.TRUE_NAME,\n" +
					"	pt.CARD_NO,\n" +
					"	pt.MOBILE,\n" +
					"	a.`name`,\n" +
					"	dept.`name`,\n" +
					"	pt.presentMajor,\n" +
					"	pt.education\n" +
					"FROM\n" +
					"	pe_trainee pt\n" +
					"LEFT JOIN pe_area a on a.id=pt.companyGroup\n" +
					"LEFT JOIN pe_department dept on dept.id=pt.department\n" +
					"WHERE\n" +
					"	pt.LOGIN_ID LIKE 'erds2017@%' and pt.var0 is null\n" +
					"AND pt.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' LIMIT " + (j * maxSize) + "," + maxSize;
			List<Object[]> ptList = SshMysqlWebtrn.queryBySQL(sql);
			
			// 原平台信息提取关键词
			Map<String, String> yiaiInfoMap = new HashMap<String, String>(); // <key, userid>
			Set<String> repeatKeySet = new HashSet<String>();
			for (List<String> infoList : yiaiUserInfoList) {
				String userId = infoList.get(0);
				String name = infoList.get(1);
				String cardNo = infoList.get(2);
				String workPlace = infoList.get(3);
				String dept = infoList.get(4);
				String major = infoList.get(5);
				String mobile = infoList.get(6);
				String education = infoList.get(7);
				if (cardNo.contains("E+")) {
					cardNo = "";
				}
				String key = generateKey(name, cardNo, mobile, workPlace, dept, major, education);
				if (StringUtils.isNotBlank(key)) {
					if (yiaiInfoMap.containsKey(key)) {
						repeatKeySet.add(key);
						System.out.println("原平台提取关键词时有重复：" + key + ":" + userId + "," + yiaiInfoMap.get(key));
					} else {
						yiaiInfoMap.put(key, userId);
					}
				}
			}
			for (String repeatKey : repeatKeySet) {
				yiaiInfoMap.remove(repeatKey);
			}
			
			// 新平台匹配
			Map<String, String> keyPtIdMap = new HashMap<String, String>(); // <key， ptId>
			repeatKeySet = new HashSet<String>();
			for (Object[] objects : ptList) {
				String ptId = MyUtils.valueOf(objects[0]);
				String name = MyUtils.valueOf(objects[2]);
				String cardNo = MyUtils.valueOf(objects[3]);
				String mobile = MyUtils.valueOf(objects[4]);
				String workPlace = MyUtils.valueOf(objects[5]);
				String dept = MyUtils.valueOf(objects[6]);
				String major = MyUtils.valueOf(objects[7]);
				String education = MyUtils.valueOf(objects[8]);
				if (cardNo.contains("E+")) {
					cardNo = "";
				}
				String key = generateKey(name, cardNo, mobile, workPlace, dept, major, education);
				if (StringUtils.isNotBlank(key)) {
					if (keyPtIdMap.containsKey(key)) {
						repeatKeySet.add(key);
						System.out.println("新平台提取关键词时有重复信息：" + key + ":" + ptId + "," + keyPtIdMap.get(key));
					} else {
						keyPtIdMap.put(key, ptId);
					}
				}
			}
			for (String repeatKey : repeatKeySet) {
				keyPtIdMap.remove(repeatKey);
			}
			
			// 两个平台匹配
			for (Entry<String, String> entry : yiaiInfoMap.entrySet()) {
				String key = entry.getKey();
				String userId = entry.getValue();
				if (userIdList.contains(userId)) { // 之前用过userId
					System.out.println("userId重复：" + userId);
					// 去掉之前匹配上的userid
					for (Entry<String, String> entry2 : matchedMap.entrySet()){
						if (entry2.getValue().equals(userId)) {
							matchedMap.remove(entry2.getKey());
							break;
						}
					}
				} else {
					userIdList.add(userId);
					if (keyPtIdMap.containsKey(key)) { // 匹配上了
						String ptId = keyPtIdMap.get(key);
						if (ptIdList.contains(ptId)) { // 之前用过userId
							System.out.println("ptId重复：" + ptId);
							// 去掉之前匹配上的ptId
							matchedMap.remove(ptId);
						} else {
							ptIdList.add(ptId);
							matchedMap.put(ptId, userId);
						}
					}
				}

			}
			
			updateSqlList = toUpdateSql(matchedMap);
			
			String path1 = "E:/myJava/yiaiSql/" + date + "/updateUserId_from_" +  (j * maxSize) + ".sql";
			MyUtils.outputList(updateSqlList, path1);
		}
		System.out.println("数据处理完毕");
	}
	
	public String generateKey(String name ,String cardNo ,String mobile ,String workPlace ,String dept ,String major ,String education){
		return name;
	}
	
	public void init(){
		// 获取原平台的user信息
		yiaiUserInfoList = new ArrayList<>();
		String sql = "SELECT\n" +
				"	u.id,\n" + // 0：userid
				"	nameData.`data` as name,\n" + // 1：姓名
				"	u.id_card_number,\n" + // 2：身份证
				"	u.org_name,\n" + // 3：单位地址
				"	u.department_name,\n" + // 4：科室
				"	u.specialty,\n" + // 5：专业
				"	u.phone2,\n" + // 6：手机号
				"	educData.`data` as educ\n" + // 7：学历
				"FROM\n" +
				"	mdl_user u\n" +
				"left join mdl_user_info_data educData on educData.userid=u.id and educData.fieldid='10'\n" +
				"LEFT JOIN mdl_user_info_data nameData on nameData.userid=u.id and nameData.fieldid='11'\n" +
				"WHERE\n" +
				"	(u.username like 'erds@%' or u.username like 'dltq@%')";
		List<Object[]> list = SshMysqlYiaiwang.queryBySQL(sql);
		for (Object[] objects : list) {
			List<String> infoList = new ArrayList<>();
			for (int i = 0; i < objects.length; i++) {
				infoList.add(MyUtils.valueOf(objects[i]));
			}
			// 姓名去掉空格
			infoList.set(2, infoList.get(2).replaceAll("\\s+", ""));
			yiaiUserInfoList.add(infoList);
		}
	}
	
	public List<String> toUpdateSql(Map<String, String> matchedMap){
		List<String> updateSqlList = new ArrayList<String>();
		for (Entry<String, String> entry : matchedMap.entrySet()) {
			updateSqlList.add("update pe_trainee set var0='" + entry.getValue() + "' where id='" + entry.getKey() + "';");
		}
		return updateSqlList;
	}
	
	public static void main(String[] args) {
		MatchUser mathMatchUser = new MatchUser();
		mathMatchUser.matchUser();
		System.exit(0);
	}
}

