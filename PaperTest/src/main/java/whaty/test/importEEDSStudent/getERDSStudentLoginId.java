package whaty.test.importEEDSStudent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;

/** 
 * @className:UpdateERDSStudentInfo.java
 * @classDescription:根据表格的学员信息，与数据库中的学员进行比对，输出loginId
 * @author:yourname
 * @createTime:2017年8月29日
 */
public class getERDSStudentLoginId {
	
	private Map<String, String> keyLoginIdMap;
	
	public String generateKey(String name ,String cardNo ,String mobile ,String workPlace ,String dept ,String major ,String education){
		return name + mobile;
	}
	
	public void outputLoginIdList(){
		init();
		List<String> result = new ArrayList<String>();
//		String fileName = "1-达拉特旗2017年继续医学教育专业课统一培训考核报名汇总表2-2_用户名";
		String fileName = "all";
		String path = "F:/whaty/医爱数据库迁移/2017--2018年度/" + fileName + ".xls";
		List<String[]> lineList = MyUtils.readExcel(path, 1);
		Set<String> repeatLoginIdSet = new HashSet<String>(); // 已经使用过的loginId
		List<String> newMatchedLoginIdList = new ArrayList<String>(); // 新匹配到的loginId
		for (int i = 0; i < lineList.size(); i++) {
			String[] strs = lineList.get(i);
			String oldLoginId = MyUtils.valueOf(strs[0]).trim().replaceAll(" ", "").replaceAll("　", "");
			String name = MyUtils.valueOf(strs[2]).trim().replaceAll(" ", "").replaceAll("　", "");
			String workPlace = MyUtils.valueOf(strs[3]).trim().replaceAll(" ", "").replaceAll("　", "");
			String cardNo = MyUtils.valueOf(strs[4]).trim().replaceAll(" ", "").replaceAll("　", "");
			String depart = MyUtils.valueOf(strs[5]).trim().replaceAll(" ", "").replaceAll("　", "");
			String title = MyUtils.valueOf(strs[6]).trim().replaceAll(" ", "").replaceAll("　", "");
			String education = MyUtils.valueOf(strs[7]).trim().replaceAll(" ", "").replaceAll("　", "");
			String major = MyUtils.valueOf(strs[8]).trim().replaceAll(" ", "").replaceAll("　", "");
			String mobile = MyUtils.valueOf(strs[9]).trim().replaceAll(" ", "").replaceAll("　", "");
			
			if (depart.equals("产科") || depart.equals("妇科")) {
				depart = "妇产科";
			}
			
			String loginId = "";
			if (StringUtils.isNotBlank(oldLoginId)) {
				loginId = oldLoginId;
				repeatLoginIdSet.add(loginId);
			} else {
				String key = generateKey(name, cardNo, mobile, workPlace, depart, major, education);
				if (keyLoginIdMap.containsKey(key)) {
					loginId = keyLoginIdMap.get(key);
					if (loginId.equals("r")) {
						System.out.println("关键词重复，不能匹配：" + key);
						loginId = "";
					} else if (repeatLoginIdSet.contains(loginId)) {
						System.out.println("用户名已经使用过：" + loginId + "," + key);
						loginId = "";
					} else {
						repeatLoginIdSet.add(loginId);
						newMatchedLoginIdList.add(loginId);
					}
				} else {
					if (StringUtils.isNotBlank(key)) {
						System.out.println("没有匹配到关键词：" + key);
					}
				}
			}
			result.add(loginId);
		}
		String path0 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/";
		String path3 = path0 + "erdsLogin-" + fileName + ".txt";
		MyUtils.outputList(result, path3);
		System.out.println("新匹配到" + newMatchedLoginIdList.size() + "人：" + newMatchedLoginIdList.toString());
	}
	
	public void init(){
		keyLoginIdMap = new HashMap<String, String>();
		String sql = "SELECT\n" +
				"	pt.LOGIN_ID,\n" +
				"	pt.CARD_NO,\n" +
				"	pt.MOBILE,\n" +
				"	pt.TRUE_NAME,\n" +
				"	a.`name`,\n" +
				"	d.`name`\n" +
				"FROM\n" +
				"	pe_trainee pt\n" +
				"LEFT JOIN pe_area a on a.id=pt.companyGroup\n" +
				"LEFT JOIN pe_department d on d.id=pt.department\n" +
				"WHERE\n" +
				"	pt.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9'\n" +
				"AND (\n" +
				"	pt.LOGIN_ID LIKE 'erds@%'\n" +
				"	OR pt.LOGIN_ID LIKE 'erds2017@%'\n" +
				") ";
		List<Object[]> list = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : list) {
			String loginId = MyUtils.valueOf(objects[0]);
			String cardNo = MyUtils.valueOf(objects[1]);
			String mobile = MyUtils.valueOf(objects[2]);
			String name = MyUtils.valueOf(objects[3]);
			String workPlace = MyUtils.valueOf(objects[4]);
			String depart = MyUtils.valueOf(objects[5]);
			String key = generateKey(name, cardNo, mobile, workPlace, depart, "", "");
			if (keyLoginIdMap.containsKey(key)) {
//				System.out.println("关键词重复：" + key + "，loginId=" + loginId + "," + keyLoginIdMap.get(key));
				keyLoginIdMap.put(key, "r");
			} else {
				keyLoginIdMap.put(key, loginId);
			}
		}
	}
	
	public static void main(String[] args) {
		getERDSStudentLoginId updateERDSStudentInfo = new getERDSStudentLoginId();
		updateERDSStudentInfo.outputLoginIdList();
		System.exit(0);
	}
	
	
	
}

