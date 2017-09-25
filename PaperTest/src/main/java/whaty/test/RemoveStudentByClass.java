package whaty.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import utils.DateUtils;

/** 
 * @className:RemoveStudentByClass.java
 * @classDescription:将某些学员从某个班级中移除
 * @author:yourname
 * @createTime:2017年9月22日
 */
public class RemoveStudentByClass {

		public void removeRepeatedDltqStudent(){
			// 读取应该留下的loginId
			List<String> correctLoginIdList = new ArrayList<>();
			List<String> nameList = new ArrayList<>();
			List<String> nameMobileList = new ArrayList<>();
			String filePath = "F:/whaty/医爱数据库迁移/";
			String[] fileNames = {"1-达拉特旗2017年继续医学教育专业课统一培训考核报名汇总表2-2_用户名.xls", "1-达拉特旗人民医院北大医学网花名册2-1_用户名.xls"};
			for (String fileName : fileNames) {
				List<String[]> lineList = MyUtils.readExcel(filePath + fileName);
				for (int i = 0; i < lineList.size(); i++) {
					String[] strs = lineList.get(i);
					String loginId = MyUtils.valueOf(strs[0]).trim();
					String name = MyUtils.valueOf(strs[2]).trim().replace(" ", "");
					String mobile = MyUtils.valueOf(strs[9]).trim();
					if (StringUtils.isBlank(loginId) || StringUtils.isBlank(name)) {
						continue;
					}
					if (correctLoginIdList.contains(loginId)) {
						System.out.println("表格中重复loginId：" + loginId);
					} else {
						correctLoginIdList.add(loginId);
					}
					if (nameList.contains(name)) {
						System.out.println("表格中重复姓名：" + name);
					} else {
						nameList.add(name);
					}
					String nameMobile = name + "-" + mobile;
					if (nameMobileList.contains(nameMobile)) {
						System.out.println("表格中重复姓名单位：" + nameMobile);
					} else {
						nameMobileList.add(nameMobile);
					}
				}
			}
			
			// 去新平台读取姓名
			List<String> toBeRemovedLoginList = new ArrayList<>();
			String conditions = "";
			for (String name : nameList) {
				conditions += ",'" + name + "'";
			}
			if (conditions.startsWith(",")) {
				conditions = conditions.substring(1);
			}
			String sql = "SELECT\n" +
					"	pt.id,\n" +
					"	pt.LOGIN_ID,\n" +
					"	pt.`TRUE_NAME`,\n" +
					"	pt.CARD_NO,\n" +
					"	pt.qq,\n" +
					"	pt.MOBILE\n" +
					"FROM\n" +
					"	pe_trainee pt\n" +
					"JOIN pr_class_trainee pct ON pct.FK_TRAINEE_ID = pt.ID\n" +
					"WHERE\n" +
					"	pt.county = '达拉特旗' and pct.FK_TRAINING_CLASS_ID = 'ff8080815e7696c1015e7a78d7f00313'\n" +
					"AND pt.TRUE_NAME IN (\n" +
					"	SELECT\n" +
					"		a.TRUE_NAME\n" +
					"	FROM\n" +
					"		(\n" +
					"			SELECT\n" +
					"				pt.TRUE_NAME,\n" +
					"				count(pt.id) AS num\n" +
					"			FROM\n" +
					"				pe_trainee pt\n" +
					"			JOIN pr_class_trainee pct ON pct.FK_TRAINEE_ID = pt.ID\n" +
					"			WHERE\n" +
					"				pct.FK_TRAINING_CLASS_ID = 'ff8080815e7696c1015e7a78d7f00313'\n" +
					"			AND pt.county = '达拉特旗'\n" +
					"			AND pt.TRUE_NAME in (" + conditions + ")\n" +
					"			GROUP BY\n" +
					"				pt.TRUE_NAME\n" +
					"		) a\n" +
					"	WHERE\n" +
					"		a.num > 1\n" +
					") ";
			List<Object[]> list = SshMysqlWebtrn.getBySQL(sql);
			Map<String, List<Object[]>> nameLoginIdMobleMap = new HashMap<String, List<Object[]>>();
			for (Object[] objects : list) {
				String ptId = MyUtils.valueOf(objects[0]);
				String loginId = MyUtils.valueOf(objects[1]);
				String name = MyUtils.valueOf(objects[2]);
				String cardNo = MyUtils.valueOf(objects[3]);
				String qq = MyUtils.valueOf(objects[4]);
				String mobile = MyUtils.valueOf(objects[5]);
				String nameMobile = name + "-" + mobile;
				if (!correctLoginIdList.contains(loginId)) {
					if (loginId.startsWith("erds2017") && nameMobileList.contains(nameMobile)) {
						toBeRemovedLoginList.add(loginId);
					} else {
						System.out.println("此用户不在表格中，但不是新添用户：" + loginId + "-" + nameMobile);
					}
				}
			}
			
			outputRemoveStudentByClassSql(toBeRemovedLoginList, "ff8080815e7696c1015e7a78d7f00313");
		}
	
		public void outputRemoveStudentByClassSql(List<String> loginIdList, String classId){
			List<String> result = new ArrayList<>();
			
			// 查询项目id
			String projectId = "";
			String sql = "select FK_PROJECT_ID from pe_training_class where id='" + classId + "'";
			List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
			if (CollectionUtils.isNotEmpty(list)) {
				projectId = MyUtils.valueOf(list.get(0)[0]);
			}
			
			// 查询学员id
			String conditions = "";
			for (String string : loginIdList) {
				conditions += ",'" + string + "'";
			}
			if (conditions.startsWith(",")) {
				conditions = conditions.substring(1);
			}
			sql = "SELECT\n" +
					"	pt.ID\n" +
					"FROM\n" +
					"	pe_trainee pt\n" +
					"WHERE\n" +
					"	pt.LOGIN_ID IN (" + conditions + ")\n" +
					"AND FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9';";
			list = SshMysqlWebtrn.queryBySQL(sql);
			for (Object[] objects : list) {
				String traineeId = MyUtils.valueOf(objects[0]);
				// 删除班级关联
				result.add("delete from pr_class_trainee where FK_TRAINEE_ID='" + traineeId + "' and FK_TRAINING_CLASS_ID='" + classId + "';");
				// 删除项目关联
				result.add("delete from pr_project_trainee where fk_traineeId='" + traineeId + "' and fk_projectId='" + projectId + "';");
			}
			String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/deleteWebtrnDltq.sql";
			MyUtils.outputList(result, path1);
			
		}
		
		public static void main(String[] args) {
			RemoveStudentByClass removeStudentByClass = new RemoveStudentByClass();
			removeStudentByClass.removeRepeatedDltqStudent();
			System.exit(0);
		}
}

