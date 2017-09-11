package whaty.test.addEEDSCourse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.DateUtils;
import utils.FileUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;
import whaty.test.SshMysqlYiaiwang;

/** 
 * @className:AddEEDSCourse.java
 * @classDescription:引入鄂尔多斯文献类课程
 * @author:GongYanshang
 * @createTime:2017年9月4日
 */
public class AddEEDSCourse {
	
	private Map<String, YiaiSubject> subjectMap; // “临床医学”下的子学科
	private String[] columnType = {"section", "video", "doc", "text", "resource", "courseware", "link", "homework", "topic", "test", "exam"};
	private String[] columnName = {"章节", "视频", "文档", "图文", "下载资料", "电子课件", "链接", "作业", "主题讨论", "测试", "考试"};
	public final static String IMG_PATH = "http://yiai.learn.webtrn.cn:80/learnspace/incoming/editor/yiai/upload/image/";
	public final static String IMG_LOCAL_PATH = "F:/whaty/eedsImg/";

	public void outputInsertSql(){
		int maxSize = 100;
		System.out.println("获取学科列表");
		init();
		System.out.println("开始查询");
		String sql = "SELECT DISTINCT\n" +
				"	mc.id\n" +
				"FROM\n" +
				"	mdl_course MC\n" +
				"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
				"LEFT JOIN mdl_course_categories MCC2 ON MCC.parent = MCC2.id\n" +
				"LEFT JOIN mdl_course_categories MCC3 ON MCC2.parent = MCC3.id\n" +
				"LEFT JOIN mdl_course_categories MCC4 ON MCC3.parent = MCC4.id\n" +
				"JOIN mdl_lesson l ON l.course = MC.id\n" +
				"JOIN mdl_lesson_pages p ON p.lessonid = l.id\n" +
				"WHERE\n" +
				"	MCC.is_cme <> 1\n" +
				"AND MCC4.id = 16";
		List<Object[]> courseIdlist = SshMysqlYiaiwang.queryBySQL(sql);
		int num = courseIdlist.size();
		int second = num / maxSize + 1;
		
//		second = 1;
		
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		List<String> addSubjectSqlList = new ArrayList<String>();
		for (int j = 0; j < second; j++) {
			System.out.println("正在执行第" + j + "次批量计算,共" + second + "次");
			List<String> addWebtrnCourse = new ArrayList<String>();
			List<String> addSpaceCourse = new ArrayList<String>();
			StringBuffer sb = new StringBuffer();
			int begin = j * maxSize;
			int end = Math.min(j * maxSize + maxSize, num);
			for (int i = begin; i < end; i++) {
				sb.append(",'" + courseIdlist.get(i)[0].toString() + "'");
			}
			String conditions = sb.toString();
			if (conditions.startsWith(",")) {
				conditions = conditions.substring(1);
			}
			// 查询当前课程的来源、主题、能力
			// 查询出当前课程
			sql = "SELECT \n" +
					"	MCC3. NAME AS p3name,\n" +
					"	MCC3.id AS p3id,\n" +
					"	MCC2. NAME AS p2name,\n" +
					"	MCC2.id AS p2id,\n" +
					"	MCC. NAME,\n" +
					"	MCC.id,\n" +
					"	MC.id,\n" +
					"	mc.sortorder,\n" +
					"	MC.fullname,\n" +
					"	p.contents\n" +
					"FROM\n" +
					"	mdl_course MC\n" +
					"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
					"LEFT JOIN mdl_course_categories MCC2 ON MCC.parent = MCC2.id\n" +
					"LEFT JOIN mdl_course_categories MCC3 ON MCC2.parent = MCC3.id\n" +
					"LEFT JOIN mdl_course_categories MCC4 ON MCC3.parent = MCC4.id\n" +
					"JOIN mdl_lesson l ON l.course = MC.id\n" +
					"JOIN mdl_lesson_pages p ON p.lessonid = l.id\n" +
					"WHERE\n" +
					"	MCC.is_cme <> 1\n" +
					"AND MCC4.id = 16\n" +
					"AND mc.id in (" + conditions + ")";
			List<Object[]> list = SshMysqlYiaiwang.queryBySQL(sql);
			for (Object[] objects : list) {
				String name2 = MyUtils.valueOf(objects[0]);
				String id2 = MyUtils.valueOf(objects[1]);
				String name3 = MyUtils.valueOf(objects[2]);
				String id3 = MyUtils.valueOf(objects[3]);
				String name5 = MyUtils.valueOf(objects[4]);
				String id5 = MyUtils.valueOf(objects[5]);
				String name4 = name3;
				String id4 = id3;
				String courseId = MyUtils.valueOf(objects[6]);
				String sortOrder = MyUtils.valueOf(objects[7]);
				String fullName = MyUtils.valueOf(objects[8]);
				String content = MyUtils.valueOf(objects[9]);
				
				// 检验学科是否存在，插入新学科
				addSubjectSqlList.addAll(generateAddSubjectSql(name2, id2, name3, id3, name4, id4, name5, id5));
				
				// 重复验证
				sql = "select c.ID from pe_tch_course c where c.`CODE`='" + courseId + "-" + sortOrder + "' and c.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
				List<Object[]> repeatList = SshMysqlWebtrn.getBySQL(sql);
				if (CollectionUtils.isNotEmpty(repeatList)) {
					continue;
				}
				
				// 插入至webtrn
				String newId = MyUtils.uuid();
				YiaiSubject sub5 = subjectMap.get(name2).getChildrenMap().get(name3).getChildrenMap().get(name4).getChildrenMap().get(name5);
				String subjectId = sub5.getId();
				String subCode = sub5.getCode();
				
				sql = "INSERT INTO `pe_tch_course` ( " +
						"	`ID`, " +
						"	`NAME`, " +
						"	`CODE`, " +
						"	`FLAG_ISVALID`, " +
						"	`FK_SITE_ID`, " +
						"	`SITE_CODE`, " +
						"	`Flag_exam`, " +
						"	`fee_course`, " +
						"	`fk_courseType_id`, " +
						"	`addTime`, " +
						"	`course_time`, " +
						"	`sort`, " +
						"	`xuefen`, " +
						"	`isLearnSpace`, " +
						"	`fk_subject_id`, " +
						"	`FLAG_ISCMECOURSE` " +
						") VALUES ( " +
						"		'" + newId + "', " +
						"		'" + fullName.replaceAll("'", "\\\\'") + "', " +
						"		'" + courseId + "-" + sortOrder + "', " +
						"		'40288a962e9d9ac5012e9dd6b0aa0004', " +
						"		'ff80808155da5b850155dddbec9404c9', " +
						"		'" + courseId + "', " +
						"		'40288a1c2f0acd2d012f0ace87040001', " +
						"		'0', " +
						"		'ff8080815e55d981015e55f4946d0005', " +
						"		now(), " +
						"		'0', " +
						"		'0', " +
						"		'0', " +
						"		'1', " +
						"		'" + subjectId + "', " +
						"		'bb3c4d5290f911e69b44848f69e05bf0' " +
						"	) ON DUPLICATE KEY UPDATE FLAG_ISVALID='40288a962e9d9ac5012e9dd6b0aa0004';";
				addWebtrnCourse.add(sql);
				
				// 插入至课程空间
				String note = parseContent(content);
				if (StringUtils.isBlank(note)) {
					System.out.println("内容为空：courseId=" + courseId + "，fullName=" + fullName);
				}
				addSpaceCourse.addAll(generateSpaceSql(newId, fullName.replaceAll("'", "\\\\'"), courseId + "-" + sortOrder, subCode, note));
				
			}
			String date = "20170907";
			String path1 = "E:/myJava/yiaiSql/" + date + "/addWebtrnCourse_from_" + begin + ".sql";
			MyUtils.outputList(addWebtrnCourse, path1);
			String path2 = "E:/myJava/yiaiSql/" + date + "/addSpaceCourse_from_" + begin + ".sql";
			MyUtils.outputList(addSpaceCourse, path2);
		}
		String path1 = "E:/myJava/yiaiSql/20170905/addSubject.sql";
		MyUtils.outputList(addSubjectSqlList, path1);
		System.out.println("数据处理完毕");
	}
	
	/**
	 * 解析页面，提取出文献类课程内容
	 * @param content
	 * @return
	 */
	public String parseContent(String content) {
		String result = "";
		Document doc = Jsoup.parse(content);
		// 处理图片链接
		Elements imgTags = doc.select("img[src]");
		for (Element element : imgTags) {
			String src = element.attr("src");// 获取src的绝对路径
			src = src.replaceAll("@@PLUGINFILE@@", "http://www.yiaiwang.com.cn/images/upload/wx");
			// 下载图片
			String filename = MyUtils.uuid() + ".png";
			String dateStr = DateUtils.getToday();
			int retry = 3; // 最多重复3次，避免网络波动
			while (retry > 0) {
				try {
					FileUtils.download(src, filename, IMG_LOCAL_PATH + dateStr);
					element.attr("src", IMG_PATH + dateStr + "/" + filename);
					element.attr("title", filename);
					break;
				} catch (Exception e) {
					System.out.println("第" + (4 - retry) + "次下载图片失败：src=" + src);
					retry --;
					e.printStackTrace();
				}
			}
		}
		Element element = doc.getElementById("content1_0");
		result = element.html().replaceAll("'", "\\\\'").replaceAll("#", "\\\\#").replaceAll("-", "\\\\-");
		return result;
	}
	
	/**
	 * 插入到课程空间的sql生成
	 * @param newId
	 * @param fullName
	 * @param newCode
	 * @param subCode
	 * @param content
	 * @return
	 */
	public List<String> generateSpaceSql(String newId, String fullName, String newCode, String subCode, String content){
		List<String> resultList = new ArrayList<>();
		// 插入课程
		String sql = "INSERT INTO `pe_tch_course` (" +
				"	`ID`," +
				"	`NAME`," +
				"	`CODE`," +
				"	`Flag_learn`," +
				"	`SITE_CODE`," +
				"	`DESIGN_PUBLISH_STATE`," +
				"	`template_type`," +
				"	`template_style_type`," +
				"	`FLAG_UP_MARKET`," +
				"	`SUBJECT`," +
				"	`ISCMECOURSE`" +
				")VALUES	( " +
				"		'" + newId + "', " +
				"		'" + fullName + "', " +
				"		'" + newCode + "', " +
				"		'402880911da8c3b2011da8d5f4100002', " +
				"		'yiai', " +
				"		'1', " +
				"		'2', " +
				"		'3', " +
				"		'1', " +
				"		'" + subCode + "', " +
				"		'0' " +
				"	);";
		resultList.add(sql);
		// 插入11个column
		String sectionId = "";
		String textId = "";
		for (int i = 0; i < columnType.length; i++) {
			String id = MyUtils.uuid();
			if (columnType[i].equals("section")) {
				sectionId = id;
			} else if (columnType[i].equals("text")) {
				textId = id;
			} 
			sql = "INSERT INTO `pe_workroom_column` (`ID`, `course`, `name`, `typt`, `serial`, `category`) " 
					+ "VALUES ('" + id + "', '" + newId + "', '" + columnName[i] + "', '" + columnType[i] + "', '" + (i + 1) + "', '1');";
			resultList.add(sql);
		}
		// 插入info
		String infoId = MyUtils.uuid();
		sql = "INSERT INTO `scorm_course_info` (`ID`, `TITLE`, `CONTROL_TYPE`, `NAVIGATE`, `FK_COURSE_ID`)" 
				+ "VALUES ('" + infoId + "', '" + fullName + "', 'choice', 'platform_nav, platform_nav', '" + newId + "');";
		resultList.add(sql);
		// 插入item
		String[] newItemIds = {MyUtils.uuid(), MyUtils.uuid(), MyUtils.uuid(), MyUtils.uuid()};
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
				+ "VALUES ( '" + newItemIds[0] + "', '" + infoId + "', 'section', '" + fullName + "', '1', '1', '" + infoId + "', 'section', '1', '" + sectionId + "', NULL, '0' ); ";
		resultList.add(sql);
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
				+ "VALUES ( '" + newItemIds[1] + "', '" + infoId + "', 'section', '" + fullName + "', '2', '2', '" + newItemIds[0] + "', 'section', '1', '" + sectionId + "', '" + newItemIds[0] + "', '1' ); ";
		resultList.add(sql);
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
				+ "VALUES ( '" + newItemIds[2] + "', '" + infoId + "', 'section', '" + fullName + "', '3', '3', '" + newItemIds[1] + "', 'section', '1', '" + sectionId + "', '" + newItemIds[1] + "', '1-1' ); ";
		resultList.add(sql);
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION`,note ) " 
				+ "VALUES ( '" + newItemIds[3] + "', '" + infoId + "', 'text', '" + fullName + "', '4', '4', '" + newItemIds[2] + "', 'text', '1', '" + textId + "', '" + newItemIds[2] + "', null, '" + content + "' ); ";
		resultList.add(sql);
		return resultList;
	}
	
	/**
	 * 补充缺失的二三四五级学科，生成sql语句
	 * @param name2
	 * @param id2
	 * @param name3
	 * @param id3
	 * @param name4
	 * @param id4
	 * @param name5
	 * @param id5
	 */
	public List<String> generateAddSubjectSql(String name2, String id2, String name3, String id3, String name4, String id4, String name5, String id5){
		List<String> addSubjectSqlList = new ArrayList<>();
		// 添加二级
		if (!subjectMap.containsKey(name2)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name2, 2, "lv2-" + id2, "5cb212da69c311e6a147001e679d6af4", new HashMap<String, YiaiSubject>());
			subjectMap.put(name2, subject);
			addSubjectSqlList.add(addSubject(subject));
		}
		// 添加三级
		String parentId = subjectMap.get(name2).getId();
		Map<String, YiaiSubject> map = subjectMap.get(name2).getChildrenMap();
		if (!map.containsKey(name3)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name3, 3, "lv3-" + id3, parentId, new HashMap<String, YiaiSubject>());
			map.put(name3, subject);
			addSubjectSqlList.add(addSubject(subject));
		}
		// 添加四级
		parentId = map.get(name3).getId();
		map = map.get(name3).getChildrenMap();
		if (!map.containsKey(name4)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name4, 4, "lv4-" + id4, parentId, new HashMap<String, YiaiSubject>());
			map.put(name4, subject);
			addSubjectSqlList.add(addSubject(subject));
		}
		// 添加五级
		parentId = map.get(name4).getId();
		map = map.get(name4).getChildrenMap();
		if (!map.containsKey(name5)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name5, 5, "lv5-" + id5, parentId, new HashMap<String, YiaiSubject>());
			map.put(name5, subject);
			addSubjectSqlList.add(addSubject(subject));
		}
		return addSubjectSqlList;
	}
	
	
	public String addSubject(YiaiSubject subject){
		String name = subject.getName();
		name = name.replaceAll("\'", "");
		String sql = "INSERT INTO `pe_subject` ( `id`, `name`, `code`, `createDate`, `level`, `fk_parent_id`, `fk_site_id` ) VALUES (" +
				"'" + subject.getId() + "'," +
				"'" + name + "'," +
				"'" + subject.getCode() + "'," +
				"now()," +
				"'" + subject.getLevel() + "'," +
				"'" + subject.getParentId() + "'," +
				"'ff80808155da5b850155dddbec9404c9'" +
				");";
		return sql;
	}
	
	public void init(){
		// 获取学科
		subjectMap = new HashMap<String, YiaiSubject>();
		String sql = "SELECT\n" +
				"	s2.id,\n" +
				"	s2.`name`,\n" +
				"	s3.id,\n" +
				"	s3.`name`,\n" +
				"	s4.id,\n" +
				"	s4.`name`,\n" +
				"	s5.id,\n" +
				"	s5.`name`,\n" +
				"	s2.code,\n" +
				"	s3.code,\n" +
				"	s4.code,\n" +
				"	s5.code\n" +
				"FROM\n" +
				"	pe_subject s2\n" +
				"JOIN pe_subject s3 ON s3.fk_parent_id = s2.id\n" +
				"JOIN pe_subject s4 ON s4.fk_parent_id = s3.id\n" +
				"JOIN pe_subject s5 ON s5.fk_parent_id = s4.id\n" +
				"WHERE\n" +
				"	s2.fk_parent_id = '5cb212da69c311e6a147001e679d6af4'";
		List<Object[]> list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id2 = MyUtils.valueOf(objects[0]);
			String name2 = MyUtils.valueOf(objects[1]);
			String id3 = MyUtils.valueOf(objects[2]);
			String name3 = MyUtils.valueOf(objects[3]);
			String id4 = MyUtils.valueOf(objects[4]);
			String name4 = MyUtils.valueOf(objects[5]);
			String id5 = MyUtils.valueOf(objects[6]);
			String name5 = MyUtils.valueOf(objects[7]);
			String code2 = MyUtils.valueOf(objects[8]);
			String code3 = MyUtils.valueOf(objects[9]);
			String code4 = MyUtils.valueOf(objects[10]);
			String code5 = MyUtils.valueOf(objects[11]);
			// 添加二级
			if (!subjectMap.containsKey(name2)) {
				YiaiSubject subject = new YiaiSubject(id2, name2, 2, code2, "5cb212da69c311e6a147001e679d6af4", new HashMap<String, YiaiSubject>());
				subjectMap.put(name2, subject);
			}
			// 添加三级
			Map<String, YiaiSubject> map = subjectMap.get(name2).getChildrenMap();
			if (!map.containsKey(name3)) {
				YiaiSubject subject = new YiaiSubject(id3, name3, 3, code3, id2, new HashMap<String, YiaiSubject>());
				map.put(name3, subject);
			}
			// 添加四级
			map = map.get(name3).getChildrenMap();
			if (!map.containsKey(name4)) {
				YiaiSubject subject = new YiaiSubject(id4, name4, 4, code4, id3, new HashMap<String, YiaiSubject>());
				map.put(name4, subject);
			}
			// 添加五级
			map = map.get(name4).getChildrenMap();
			if (!map.containsKey(name5)) {
				YiaiSubject subject = new YiaiSubject(id5, name5, 5, code5, id4, new HashMap<String, YiaiSubject>());
				map.put(name5, subject);
			}
		}
	}
	
	public static void main(String[] args) {
		AddEEDSCourse addEEDSCourse = new AddEEDSCourse();
		addEEDSCourse.outputInsertSql();
		System.exit(0);
	}
}

