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

import com.sun.org.apache.xpath.internal.functions.FuncBoolean;

import utils.DateUtils;
import utils.FileUtils;
import utils.HanyuPinyinUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;
import whaty.test.SshMysqlYiaiwang;
import whaty.test.SshMysqlYiaiwangResource;

/** 
 * @className:AddEEDSCourse.java
 * @classDescription:引入鄂尔多斯文献类课程
 * @author:GongYanshang
 * @createTime:2017年9月4日
 */
public class AddEEDSCourseQiniu {
	
	private Map<String, YiaiSubject> subjectMap; // “临床医学”下的子学科
	private Map<String, YiaiSubject> nengliMap; // 能力
	private Map<String, YiaiSubject> zhutiMap; // 主题
	private Map<String, YiaiSubject> laiyuanMap; // 来源
	private Map<String, YiaiSubject> zhichengMap; // 职称
	private Map<String, Expert> expertMap; // 专家
	private List<String> courseCodeList; // 存放已经insert的courseCode，防止重复
	private String[] columnType = {"section", "video", "doc", "text", "resource", "courseware", "link", "homework", "topic", "test", "exam"};
	private String[] columnName = {"章节", "视频", "文档", "图文", "下载资料", "电子课件", "链接", "作业", "主题讨论", "测试", "考试"};
	public final static String IMG_PATH = "http://yiai.learn.webtrn.cn:80/learnspace/incoming/editor/yiai/upload/image/";
	public final static String IMG_LOCAL_PATH = "F:/whaty/eedsImg/";

	public void outputInsertSql(){
		int maxSize = 100;
		System.out.println("获取学科列表");
		init();
		System.out.println("开始查询");
		String sql = "SELECT\n" +
				"	MC.id\n" +
				"FROM\n" +
				"	mdl_course MC\n" +
				"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
				"LEFT JOIN mdl_course_categories MCC2 ON MCC.parent = MCC2.id\n" +
				"LEFT JOIN mdl_course_categories MCC3 ON MCC2.parent = MCC3.id\n" +
				"LEFT JOIN mdl_course_categories MCC4 ON MCC3.parent = MCC4.id\n" +
				"JOIN `mdl_view_course_scids` MVCS ON MVCS.course = MC.id\n" +
				"LEFT JOIN mdl_view_materials_source MCMS ON MVCS.`scids` = MCMS.`ID`\n" +
				"LEFT JOIN mdl_course_info_data expertName on expertName.courseid=MC.id and expertName.fieldid='4'\n" +
				"LEFT JOIN mdl_course_info_data zhicheng on zhicheng.courseid=MC.id and zhicheng.fieldid='5'\n" +
				"LEFT JOIN mdl_course_info_data danwei on danwei.courseid=MC.id and danwei.fieldid='6'\n" +
				"WHERE\n" +
				"	MCC.is_cme <> 1\n" +
				"AND MCC4.id = 3\n" +
				"AND MVCS.scids IS NOT NULL\n" +
				"AND (\n" +
				"	MCMS.`STORAGE_PATH` LIKE 'http://v.yiaiwang.com.cn%'\n" +
				"	OR MCMS.`STORAGE_PATH` LIKE 'http://ppt.yiaiwang.com.cn%'\n" +
				"	OR MCMS.`STORAGE_PATH` LIKE 'http://www.ablesky.com%'\n" +
				")";
		List<Object[]> courseIdlist = SshMysqlYiaiwang.queryBySQL(sql);
		int num = courseIdlist.size();
		int second = num / maxSize + 1;
		
//		second = 1;
		
		System.out.println("总记录共" + num + "条，大于" + maxSize + "条，将分" + second + "次执行");
		List<String> addSubjectSqlList = new ArrayList<String>();
		List<String> expertExcelList = new ArrayList<String>();
		String date = DateUtils.getToday();
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
			sql = "SELECT\n" +
					"MCC3.name as p3name , MCC3.id as p3id ,\n" +
					"MCC2.name as p2name , MCC2.id as p2id ,\n" +
					"MCC.name,  MCC.id, \n" +
					"MC.id, mc.sortorder,MC.fullname,\n" +
					"	MVCS.scids,\n" +
					"	expertName.`data`,\n" +
					"	zhicheng.`data`,\n" +
					"	danwei.`data`\n" +
					"FROM\n" +
					"	mdl_course MC\n" +
					"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
					"LEFT JOIN mdl_course_categories MCC2 ON MCC.parent = MCC2.id\n" +
					"LEFT JOIN mdl_course_categories MCC3 ON MCC2.parent = MCC3.id\n" +
					"LEFT JOIN mdl_course_categories MCC4 ON MCC3.parent = MCC4.id\n" +
					"JOIN `mdl_view_course_scids` MVCS ON MVCS.course = MC.id\n" +
					"LEFT JOIN mdl_view_materials_source MCMS ON MVCS.`scids` = MCMS.`ID`\n" +
					"LEFT JOIN mdl_course_info_data expertName on expertName.courseid=MC.id and expertName.fieldid='4'\n" +
					"LEFT JOIN mdl_course_info_data zhicheng on zhicheng.courseid=MC.id and zhicheng.fieldid='5'\n" +
					"LEFT JOIN mdl_course_info_data danwei on danwei.courseid=MC.id and danwei.fieldid='6'\n" +
					"WHERE\n" +
					"	MCC.is_cme <> 1\n" +
					"AND MCC4.id = 3\n" +
					"AND MVCS.scids IS NOT NULL\n" +
					"AND (\n" +
					"	MCMS.`STORAGE_PATH` LIKE 'http://v.yiaiwang.com.cn%'\n" +
					"	OR MCMS.`STORAGE_PATH` LIKE 'http://ppt.yiaiwang.com.cn%'\n" +
					"	OR MCMS.`STORAGE_PATH` LIKE 'http://www.ablesky.com%'\n" +
					") limit " + (j * maxSize) + "," + maxSize;
			List<Object[]> list = SshMysqlYiaiwang.queryBySQL(sql);
			for (Object[] objects : list) {
				String name2 = MyUtils.valueOf(objects[0]);
				String id2 = MyUtils.valueOf(objects[1]);
				String name3 = MyUtils.valueOf(objects[2]);
				String id3 = MyUtils.valueOf(objects[3]);
				String name5 = MyUtils.valueOf(objects[4]);
				String id5 = MyUtils.valueOf(objects[5]);
				String name4 = name5;
				String id4 = id5;
				String courseId = MyUtils.valueOf(objects[6]);
				String sortOrder = MyUtils.valueOf(objects[7]);
				String fullName = MyUtils.valueOf(objects[8]).trim();
				String scids = MyUtils.valueOf(objects[9]);
				String expertName = MyUtils.valueOf(objects[10]).trim();
				String expertTitle = MyUtils.valueOf(objects[11]).trim();
				String expertPlace = MyUtils.valueOf(objects[12]).trim();
				
				String newCode = courseId + "-" + sortOrder;
				if (courseCodeList.contains(newCode)) {
					continue;
				} else {
					courseCodeList.add(newCode);
				}
				
				// 检验学科是否存在，插入新学科
				if (!subjectMap.containsKey(name2)) {
					name2 = "其他";
					id2 = subjectMap.get(name2).getId();
				}
				addSubjectSqlList.addAll(generateAddSubjectSql(name2, id2, name3, id3, name4, id4, name5, id5));
				
				// 查询能力来源
				String nengliId = "";
				String laiyuanId = "";
				String zhutiId = "";
				String zhichengId = "";
				String nengliName = "";
				String nengliCode = "";
				String laiyuanName = "";
				String laiyuanCode = "";
				String zhutiName = "";
				String zhutiCode = "";
				String zhichengName = "";
				String zhichengCode = "";
				if (StringUtils.isNotBlank(scids)) {
					sql = "SELECT\n" +
							"	nengli.`NAME` AS nengliName,\n" +
							"	nengli.`CODE` AS nengliCode,\n" +
							"	laiyuan.`NAME` AS laiyuanName,\n" +
							"	laiyuan.`CODE` AS laiyuanCode,\n" +
							"	zhuti.`NAME` AS zhutiName,\n" +
							"	zhuti.`CODE` AS zhutiCode,\n" +
							"	zhicheng.`NAME` AS zhichengName,\n" +
							"	zhicheng.`CODE` AS zhichengCODE\n" +
							"FROM\n" +
							"	study_materials t\n" +
							"LEFT JOIN study_materials_cognize c ON c.MATERIALS_ID = t.id\n" +
							"LEFT JOIN exam_prop_val nengli ON nengli.id = c.PROP_VAL_ID\n" +
							"LEFT JOIN study_materials_source s ON s.MATERIALS_ID = t.id\n" +
							"LEFT JOIN exam_prop_val laiyuan ON laiyuan.id = s.PROP_VAL_ID\n" +
							"LEFT JOIN study_materials_theme theme ON theme.MATERIALS_ID = t.id\n" +
							"LEFT JOIN exam_prop_val zhuti ON zhuti.id = theme.PROP_VAL_ID\n" +
							"LEFT JOIN study_materials_title title ON title.MATERIALS_ID = t.id\n" +
							"LEFT JOIN exam_prop_val zhicheng ON zhicheng.id = title.PROP_VAL_ID\n" +
							"WHERE\n" +
							"	t.id = '" + scids + "'";
					List<Object[]>  sourceList = SshMysqlYiaiwangResource.queryBySQL(sql);
					if (CollectionUtils.isEmpty(sourceList)) {
						System.out.println("courseId=" + courseId + "没有查询到素材：" + sql);
					} else {
						nengliName = MyUtils.valueOf(sourceList.get(0)[0]).replaceAll("\\\\", "");
						nengliCode = MyUtils.valueOf(sourceList.get(0)[1]);
						laiyuanName = MyUtils.valueOf(sourceList.get(0)[2]).replaceAll("\\\\", "");
						laiyuanCode = MyUtils.valueOf(sourceList.get(0)[3]);
						zhutiName = MyUtils.valueOf(sourceList.get(0)[4]).replaceAll("\\\\", "");
						zhutiCode = MyUtils.valueOf(sourceList.get(0)[5]);
						zhichengName = MyUtils.valueOf(sourceList.get(0)[6]).replaceAll("\\\\", "");
						zhichengCode = MyUtils.valueOf(sourceList.get(0)[7]);
						sql = generateNengliSql(nengliName, nengliCode);
						if (StringUtils.isNotBlank(sql)) {
							addSubjectSqlList.add(sql);
						}
						sql = generateLaiyuanSql(laiyuanName, laiyuanCode);
						if (StringUtils.isNotBlank(sql)) {
							addSubjectSqlList.add(sql);
						}
						sql = generateZhutiSql(zhutiName, zhutiCode);
						if (StringUtils.isNotBlank(sql)) {
							addSubjectSqlList.add(sql);
						}
						sql = generateZhichengSql(zhichengName, zhichengCode);
						if (StringUtils.isNotBlank(sql)) {
							addSubjectSqlList.add(sql);
						}
						nengliId = nengliMap.get(nengliName).getId();
						laiyuanId = laiyuanMap.get(laiyuanName).getId();
						zhutiId = zhutiMap.get(zhutiName).getId();
						zhichengId = zhichengMap.get(zhichengName).getId();
					}
				}
				
				// 查询专家
				String expertId = "";
				if (StringUtils.isNotEmpty(expertName)) {
					if (expertName.contains("、")) {
						expertName = expertName.substring(0, expertName.indexOf("、"));
					}
					if (expertTitle.contains("、")) {
						expertTitle = expertTitle.substring(0, expertTitle.indexOf("、"));
					}
					if (expertPlace.contains("；")) {
						expertPlace = expertPlace.substring(0, expertPlace.indexOf("；"));
					}
					generateExpertSql(expertName, expertTitle, expertPlace, addSubjectSqlList, expertExcelList);
					expertId = expertMap.get(expertName + expertPlace).getId();
				}
				
				
				// 重复验证
				sql = "select c.ID from pe_tch_course c where c.`CODE`='" + newCode + "' and c.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
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
						"	`lable`, " +
						"	`flag_theme`, " +
						"	`fk_origin_id`, " +
						"	`flag_ability`, " +
						"	`flag_career`, " +
						"	`teacherName`, " +
						"	`FLAG_ISCMECOURSE` " +
						") VALUES ( " +
						"		'" + newId + "', " +
						"		'" + toSql(fullName) + "', " +
						"		'" + newCode + "', " +
						"		'40288a962e9d9ac5012e9dd6b0aa0004', " +
						"		'ff80808155da5b850155dddbec9404c9', " +
						"		'" + courseId + "', " +
						"		'40288a1c2f0acd2d012f0ace87040001', " +
						"		'0', " +
						"		'ff80808157084dfb0157085339f60008', " +
						"		now(), " +
						"		'0', " +
						"		'0', " +
						"		'0', " +
						"		'1', " +
						"		'" + subjectId + "', " +
						"		'鄂尔多斯七牛视频', " +
						"		'" + zhutiId + "', " +
						"		'" + laiyuanId + "', " +
						"		'" + nengliId + "', " +
						"		'" + zhichengId + "', " + 
						(StringUtils.isBlank(expertId) ? " null," : " '" + expertId + "', ") +
						"		'bb3c4d5290f911e69b44848f69e05bf0' " +
						"	) ON DUPLICATE KEY UPDATE FLAG_ISVALID='40288a962e9d9ac5012e9dd6b0aa0004';";
				addWebtrnCourse.add(sql);
				
				// 插入至课程空间
				addSpaceCourse.addAll(generateSpaceSql(newId, toSql(fullName), courseId, newCode, subCode, laiyuanCode, nengliName, zhichengName, zhutiName));
			}
			
			String path1 = "E:/myJava/yiaiSql/" + date + "/addWebtrnCourse_from_" + begin + ".sql";
			MyUtils.outputList(addWebtrnCourse, path1);
			String path2 = "E:/myJava/yiaiSql/" + date + "/addSpaceCourse_from_" + begin + ".sql";
			MyUtils.outputList(addSpaceCourse, path2);
		}
		String path1 = "E:/myJava/yiaiSql/" + date + "/addSubject.sql";
		MyUtils.outputList(addSubjectSqlList, path1);
		String path2 = "E:/myJava/yiaiSql/" + date + "/expertExcel.txt";
		MyUtils.outputList(expertExcelList, path2);
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
	public List<String> generateSpaceSql(String newId, String fullName, String courseId, String newCode, String subCode, String originCode, String abilityName, String careerName, String themeName){
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
				"	`note`," +
				"	`ORIGIN`," +
				"	`ABILITY`," +
				"	`CAREER`," +
				"	`THEME`," +
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
				"		'鄂尔多斯七牛视频', " +
				"		'" + originCode + "', " +
				"		'" + abilityName + "', " +
				"		'" + careerName + "', " +
				"		'" + themeName + "', " +
				"		'0' " +
				"	) ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		// 插入11个column
		String sectionId = "";
		String textId = "";
		String videoId = "";
		for (int i = 0; i < columnType.length; i++) {
			String id = MyUtils.uuid();
			if (columnType[i].equals("section")) {
				sectionId = id;
			} else if (columnType[i].equals("text")) {
				textId = id;
			} else if (columnType[i].equals("video")) {
				videoId = id;
			} 
			sql = "INSERT INTO `pe_workroom_column` (`ID`, `course`, `name`, `typt`, `serial`, `category`) " 
					+ "VALUES ('" + id + "', '" + newId + "', '" + columnName[i] + "', '" + columnType[i] + "', '" + (i + 1) + "', '1') ON DUPLICATE KEY UPDATE id=id;";
			resultList.add(sql);
		}
		// 插入info
		String infoId = MyUtils.uuid();
		sql = "INSERT INTO `scorm_course_info` (`ID`, `TITLE`, `CONTROL_TYPE`, `NAVIGATE`, `FK_COURSE_ID`)" 
				+ "VALUES ('" + infoId + "', '" + fullName + "', 'choice', 'platform_nav, platform_nav', '" + newId + "') ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		// 插入item
		String[] newItemIds = {MyUtils.uuid(), MyUtils.uuid(), MyUtils.uuid(), MyUtils.uuid()};
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
				+ "VALUES ( '" + newItemIds[0] + "', '" + infoId + "', 'section', '" + fullName + "', '1', '1', '" + infoId + "', 'section', '1', '" + sectionId + "', NULL, '0' ) ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
				+ "VALUES ( '" + newItemIds[1] + "', '" + infoId + "', 'section', '" + fullName + "', '2', '2', '" + newItemIds[0] + "', 'section', '1', '" + sectionId + "', '" + newItemIds[0] + "', '1' ) ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
				+ "VALUES ( '" + newItemIds[2] + "', '" + infoId + "', 'section', '" + fullName + "', '3', '3', '" + newItemIds[1] + "', 'section', '1', '" + sectionId + "', '" + newItemIds[1] + "', '1-1' ) ON DUPLICATE KEY UPDATE id=id;";
		resultList.add(sql);
		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `SCO_TYPE`, `LAUNCH` ) " 
				+ "VALUES ( '" + newItemIds[3] + "', '" + infoId + "', 'video', '" + fullName + "', '4', '4', '" + newItemIds[2] + "', 'video', '1', '" + videoId + "', '" + newItemIds[2] + "', 'QINIU', 'http://linchuang.yiaiwang.com.cn/my/elsevierproject/whaty/course_view.php?courseid=" + courseId + "' ) ON DUPLICATE KEY UPDATE id=id;";
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
			name2 = "其他";
		}
		// 添加三级
		String parentId = subjectMap.get(name2).getId();
		Map<String, YiaiSubject> map = subjectMap.get(name2).getChildrenMap();
		if (!map.containsKey(name3)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name3, 3, "lv3-" + id3 + "-" + id3, parentId, new HashMap<String, YiaiSubject>());
			map.put(name3, subject);
			addSubjectSqlList.add(addSubject(subject));
		}
		// 添加四级
		parentId = map.get(name3).getId();
		map = map.get(name3).getChildrenMap();
		if (!map.containsKey(name4)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name4, 4, "lv4-" + id4 + "-" + id4, parentId, new HashMap<String, YiaiSubject>());
			map.put(name4, subject);
			addSubjectSqlList.add(addSubject(subject));
		}
		// 添加五级
		parentId = map.get(name4).getId();
		map = map.get(name4).getChildrenMap();
		if (!map.containsKey(name5)) {
			String id = MyUtils.uuid();
			YiaiSubject subject = new YiaiSubject(id, name5, 5, "lv5-" + id5 + "-" + id5, parentId, new HashMap<String, YiaiSubject>());
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
				") ON DUPLICATE KEY UPDATE id=id;";
		return sql;
	}
	
	public String generateNengliSql(String name, String code){
		if (nengliMap.containsKey(name)) {
			return null;
		}
		String id = MyUtils.uuid();
		YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
		nengliMap.put(name, yiaiSubject);
		String sql = "INSERT INTO `enum_const` (`ID`, `NAME`, `CODE`, `NAMESPACE`, `IS_DEFAULT`, `CREATE_DATE`, `NOTE`, `FK_SITE_ID`) VALUES ('" + id + "', '" + name + "', '" + code + "', 'FlagAbility', '', now(), '医爱能力管理', 'ff80808155da5b850155dddbec9404c9') ON DUPLICATE KEY UPDATE id=id;";
		return sql;
	}
	
	public String generateZhutiSql(String name, String code){
		if (zhutiMap.containsKey(name)) {
			return null;
		}
		String id = MyUtils.uuid();
		YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
		zhutiMap.put(name, yiaiSubject);
		String sql = "INSERT INTO `enum_const` (`ID`, `NAME`, `CODE`, `NAMESPACE`, `IS_DEFAULT`, `CREATE_DATE`, `NOTE`, `FK_SITE_ID`) VALUES ('" + id + "', '" + name + "', '" + code + "', 'FlagTheme', '', now(), '医爱主题管理', 'ff80808155da5b850155dddbec9404c9') ON DUPLICATE KEY UPDATE id=id;";
		return sql;
	}
	
	public String generateZhichengSql(String name, String code){
		if (zhichengMap.containsKey(name)) {
			return null;
		}
		String id = MyUtils.uuid();
		YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
		zhichengMap.put(name, yiaiSubject);
		String sql = "INSERT INTO `enum_const` (`ID`, `NAME`, `CODE`, `NAMESPACE`, `IS_DEFAULT`, `CREATE_DATE`, `NOTE`, `FK_SITE_ID`) VALUES ('" + id + "', '" + name + "', '" + code + "', 'FlagCareer', '', now(), '医爱职称管理', 'ff80808155da5b850155dddbec9404c9') ON DUPLICATE KEY UPDATE id=id;";
		return sql;
	}
	
	public String generateLaiyuanSql(String name, String code){
		if (laiyuanMap.containsKey(name)) {
			return null;
		}
		String id = MyUtils.uuid();
		YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
		laiyuanMap.put(name, yiaiSubject);
		String sql = "INSERT INTO `pe_origin` (`ID`, `CODE`, `NAME`, `CREATE_DATE`, `NOTE`, `FK_SITE_ID`) VALUES ('" + id + "', '" + code + "', '" + name + "', now(), '医爱来源管理', 'ff80808155da5b850155dddbec9404c9') ON DUPLICATE KEY UPDATE id=id;";
		return sql;
	}
	
	public void generateExpertSql(String expertName, String expertTitle, String expertPlace, List<String> webtrnList, List<String> excelList){
		if (StringUtils.isBlank(expertName) || expertMap.containsKey(expertName + expertPlace)) {
			return;
		}
		// 添加职称
		if (StringUtils.isNotBlank(expertTitle)) {
			String sql = generateZhichengSql(expertTitle, MyUtils.uuid());
			if (StringUtils.isNotBlank(sql)) {
				webtrnList.add(sql);
			}
		}

		// webtrn添加专家
		expertMap.put(expertName + expertPlace, new Expert(MyUtils.uuid(), expertName, expertPlace, null));
		String gap = "\t";
		String excel = HanyuPinyinUtils.toHanyuPinyin(expertName) + "4" + gap + expertName + gap + expertName + gap + gap + gap + gap + gap + expertPlace + gap + expertTitle;
		excelList.add(excel);
	}
	
	public void init(){
		courseCodeList = new ArrayList<>();
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
		
		// 获取能力
		nengliMap = new HashMap<>();
		sql = "select ec.id,ec.name,ec.code from enum_const ec where ec.NAMESPACE='FlagAbility' and ec.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id = MyUtils.valueOf(objects[0]);
			String name = MyUtils.valueOf(objects[1]);
			String code = MyUtils.valueOf(objects[2]);
			YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
			nengliMap.put(name, yiaiSubject);
		}
		// 获取主题
		zhutiMap = new HashMap<>();
		sql = "select ec.id,ec.name,ec.code from enum_const ec where ec.NAMESPACE='FlagTheme' and ec.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id = MyUtils.valueOf(objects[0]);
			String name = MyUtils.valueOf(objects[1]);
			String code = MyUtils.valueOf(objects[2]);
			YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
			zhutiMap.put(name, yiaiSubject);
		}
		// 获取职称
		zhichengMap = new HashMap<>();
		sql = "select ec.id,ec.name,ec.code from enum_const ec where ec.NAMESPACE='FlagCareer' and ec.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id = MyUtils.valueOf(objects[0]);
			String name = MyUtils.valueOf(objects[1]);
			String code = MyUtils.valueOf(objects[2]);
			YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
			zhichengMap.put(name, yiaiSubject);
		}
		// 获取来源
		laiyuanMap = new HashMap<>();
		sql = "select o.id,o.`NAME`,o.`CODE` from pe_origin o where o.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id = MyUtils.valueOf(objects[0]);
			String name = MyUtils.valueOf(objects[1]);
			String code = MyUtils.valueOf(objects[2]);
			YiaiSubject yiaiSubject = new YiaiSubject(id, name, 0, code, null, null);
			laiyuanMap.put(name, yiaiSubject);
		}
		// 获取专家
		expertMap = new HashMap<>();
		sql = "SELECT e.ID,e.`NAME`,e.WORKPLACE FROM 	pe_expert e WHERE 	e.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' and e.`NAME` != ''";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id = MyUtils.valueOf(objects[0]);
			String name = MyUtils.valueOf(objects[1]);
			String workPlace = MyUtils.valueOf(objects[2]);
			expertMap.put(name + workPlace, new Expert(id, name, workPlace, null));
		}
	}
	
	/**
	 * 将str中的特殊字符前面加上斜线，兼容sql
	 * @param str
	 * @return
	 */
	public static String toSql(String str){
		if (StringUtils.isBlank(str)) {
			return str;
		}
		return str.replaceAll("'", "\\\\'").replaceAll("#", "\\\\#").replaceAll("-", "\\\\-");
	}
	
	public static void main(String[] args) {
		AddEEDSCourseQiniu addEEDSCourse = new AddEEDSCourseQiniu();
		addEEDSCourse.outputInsertSql();
		System.exit(0);
	}
}

