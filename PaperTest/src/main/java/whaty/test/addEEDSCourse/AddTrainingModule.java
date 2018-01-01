package whaty.test.addEEDSCourse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlSpace;
import whaty.test.SshMysqlWebtrn;

/** 
 * @className:AddTrainingModule.java
 * @classDescription:补充开启班级流程
 * @author:yourname
 * @createTime:2017年9月27日
 */
public class AddTrainingModule {
	public void outputAddSql(String classId, String eleModuleId, String studyModuleId){
		List<String> addTrainingModuleSqlList = new ArrayList<String>();
		Set<String> openedLoginIdSet = new HashSet<String>();
		System.out.println("查询信息");
		String sql = "SELECT\n" +
				"	e.ID,\n" +
				"	pt.login_id,\n" +
				"	pt.ID,\n" +
				"	e.ELECTIVE_DATE\n" +
				"FROM\n" +
				"	pe_tch_elective e\n" +
				"JOIN pe_open_course poc ON poc.id = e.FK_OPENCOURSE_ID\n" +
				"JOIN pe_trainee pt ON pt.id = e.FK_TRAINEE_ID\n" +
				"LEFT JOIN pr_training_module m ON m.fk_module_id = '" + eleModuleId + "'\n" +
				"AND m.fk_trainee_id = e.FK_TRAINEE_ID\n" +
				"WHERE\n" +
				"	poc.FK_TRAIN_CLASS = '" + classId + "'\n" +
				"AND m.id IS NULL\n" +
				"GROUP BY\n" +
				"	e.FK_TRAINEE_ID";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		System.out.println("共查出" + list.size() + "个结果，开始生成sql");
		for (Object[] objs : list) {
			String eleId = MyUtils.valueOf(objs[0]);
			String username = MyUtils.valueOf(objs[1]);
			String ptId = MyUtils.valueOf(objs[2]);
			String createTime = MyUtils.valueOf(objs[3]);
			
			// 开启流程
			if (!openedLoginIdSet.contains(username)) { // 没有开启过
				openedLoginIdSet.add(username);
				// 选课，进行中
				String moduleSql = "insert into pr_training_module (id,create_time,fk_trainee_id,fk_module_id,fk_site_id,status) "
						+ "VALUE ('" + MyUtils.uuid() + "','" + createTime + "','" + ptId + "','" + eleModuleId + "','ff80808155da5b850155dddbec9404c9','bc2dec18d8cc11e6bb8900251113d11d') ON DUPLICATE KEY UPDATE status=values(status);";
				addTrainingModuleSqlList.add(moduleSql);
				// 学习，进行中
				moduleSql = "insert into pr_training_module (id,create_time,fk_trainee_id,fk_module_id,fk_site_id,status) "
						+ "VALUE ('" + MyUtils.uuid() + "','" + createTime + "','" + ptId + "','" + studyModuleId + "','ff80808155da5b850155dddbec9404c9','bc2dec18d8cc11e6bb8900251113d11d') ON DUPLICATE KEY UPDATE status=values(status);";
				addTrainingModuleSqlList.add(moduleSql);
			}
		}
		String path1 = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/addTrainingModule.sql";
		MyUtils.outputList(addTrainingModuleSqlList, path1);
	}
	
	public static void main(String[] args) {
		AddTrainingModule addStuSco = new AddTrainingModule();
		String classId2017 = "ff8080815e7696c1015e7a78d7f00313";
		String eleModuleId2017 = "ff8080815e7696c1015e7a78d8340315";
		String studyModuleId2017 = "ff8080815e7696c1015e7a78d8340316";
		
		String classId2016 = "ff8080815e86ebf2015e92c4faae070f";
		String eleModuleId2016 = "ff8080815e86ebf2015e92c4faed0710";
		String studyModuleId2016 = "ff8080815e86ebf2015e92c4faed0711";
		
		addStuSco.outputAddSql(classId2017, eleModuleId2017, studyModuleId2017);
		System.exit(0);
	}
}

