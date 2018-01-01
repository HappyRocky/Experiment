package whaty.test.addEEDSCourse;

import java.io.FileNotFoundException;
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
import utils.HanyuPinyinUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;
import whaty.test.SshMysqlYiaiwang;
import whaty.test.SshMysqlYiaiwangResource;

/** 
 * @className:AddExtraCourse.java
 * @classDescription:迁移非CME且非鄂尔多斯的课程
 * @author:yourname
 * @createTime:2017年9月21日
 */
public class AddExtraCourse {
	
	private Map<String, YiaiSubject> subjectMap; // “临床医学”下的子学科
	private Map<String, YiaiSubject> nengliMap; // 能力
	private Map<String, YiaiSubject> zhutiMap; // 主题
	private Map<String, YiaiSubject> laiyuanMap; // 来源
	private Map<String, YiaiSubject> zhichengMap; // 职称
	private Map<String, Expert> expertMap; // 专家
	private List<String> courseCodeList; // 存放已经insert的courseCode，防止重复
	private Map<String, String> exitCodeSubMap; // 存放已经存在的<courseCode,fk_subject_id>，防止重复
	private Map<String, String> subjectCodeIdMap; // 已经存在的5级学科的code和id
	private String[] columnType = {"section", "video", "doc", "text", "resource", "courseware", "link", "homework", "topic", "test", "exam"};
	private String[] columnName = {"章节", "视频", "文档", "图文", "下载资料", "电子课件", "链接", "作业", "主题讨论", "测试", "考试"};
	public final static String IMG_PATH = "http://yiai.learn.webtrn.cn:80/learnspace/incoming/editor/yiai/upload/image/";
	public final static String IMG_LOCAL_PATH = "F:/whaty/eedsImg/";

	/**
	 * 导出sql语句
	 * @param flag 0：文本；1：七牛
	 */
	public void outputInsertSql(int flag){
		int maxSize = 100;
		System.out.println("获取学科列表");
		init();
		System.out.println("开始查询");
		String sql = "SELECT DISTINCT \n" +
				"	MC.id as mcId \n" +
				"FROM\n" +
				"    mdl_course MC\n" +
				"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
				"LEFT JOIN mdl_course_categories MCC2 ON MCC.parent = MCC2.id\n" +
				"LEFT JOIN mdl_course_categories MCC3 ON MCC2.parent = MCC3.id\n" +
				"LEFT JOIN mdl_course_categories MCC4 ON MCC3.parent = MCC4.id\n" +
				"LEFT JOIN `mdl_view_course_scids` MVCS ON MVCS.course = MC.id\n" +
				"LEFT JOIN mdl_view_materials_source MCMS ON MVCS.`scids` = MCMS.`ID`\n" +
				"LEFT JOIN mdl_course_info_data expertName on expertName.courseid=MC.id and expertName.fieldid='4'\n" +
				"LEFT JOIN mdl_course_info_data zhicheng on zhicheng.courseid=MC.id and zhicheng.fieldid='5'\n" +
				"LEFT JOIN mdl_course_info_data danwei on danwei.courseid=MC.id and danwei.fieldid='6'\n" +
				"LEFT JOIN mdl_lessonpage_quiz q on q.course_id=MC.id\n" +
				"WHERE\n" +
				"    (MCC3.id = '3' or MCC4.id = '347') ";
//				"	MCC.is_cme <> 1\n" +
//				"  and MCC4.id = 3\n";
		if (flag == 0) { // 文本
			sql += " AND MVCS.scids IS NULL ";
		} else if (flag == 1) { // 七牛
			sql += " AND MVCS.scids IS NOT NULL\n" +
					"AND (\n" +
					"	MCMS.`STORAGE_PATH` LIKE 'http://v.yiaiwang.com.cn%'\n" +
					"	OR MCMS.`STORAGE_PATH` LIKE 'http://ppt.yiaiwang.com.cn%'\n" +
					"	OR MCMS.`STORAGE_PATH` LIKE 'http://www.ablesky.com%'\n" +
					")";
		}
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
			int begin = j * maxSize;
			// 查询出当前课程
			sql = "SELECT DISTINCT \n" +
					"	MCC3.name as p3name , MCC3.id as p3id ,\n" +
					"	MCC2.name as p2name , MCC2.id as p2id ,\n" +
					"	MCC.name,  MCC.id, \n" +
					"	MC.id as mcId, \n" +
					"	mc.sortorder,\n" +
					"	MC.fullname,\n" +
					"	MVCS.scids,\n" +
					"	expertName.`data`,\n" +
					"	zhicheng.`data`,\n" +
					"	danwei.`data`,\n" +
					"	p.contents,\n" +
					"	q.`subject`\n" + // 知识点，能力，来源
					"FROM\n" +
					"    mdl_course MC\n" +
					"LEFT JOIN mdl_course_categories MCC ON MC.category = MCC.id\n" +
					"LEFT JOIN mdl_course_categories MCC2 ON MCC.parent = MCC2.id\n" +
					"LEFT JOIN mdl_course_categories MCC3 ON MCC2.parent = MCC3.id\n" +
					"LEFT JOIN mdl_course_categories MCC4 ON MCC3.parent = MCC4.id\n" +
					"LEFT JOIN `mdl_view_course_scids` MVCS ON MVCS.course = MC.id\n" +
					"LEFT JOIN mdl_view_materials_source MCMS ON MVCS.`scids` = MCMS.`ID`\n" +
					"LEFT JOIN mdl_course_info_data expertName on expertName.courseid=MC.id and expertName.fieldid='4'\n" +
					"LEFT JOIN mdl_course_info_data zhicheng on zhicheng.courseid=MC.id and zhicheng.fieldid='5'\n" +
					"LEFT JOIN mdl_course_info_data danwei on danwei.courseid=MC.id and danwei.fieldid='6'\n" +
					"LEFT JOIN mdl_lessonpage_quiz q on q.course_id=MC.id\n" +
					"LEFT JOIN mdl_lesson l ON l.course = MC.id\n" +
					"LEFT JOIN mdl_lesson_pages p ON p.lessonid = l.id\n" +
					"WHERE\n" + 
					"    (MCC3.id = '3' ) ";
//					"	MCC.is_cme <> 1\n" +
//					"  and MCC4.id = 3\n";
			if (flag == 0) { // 文本
				sql += " AND MVCS.scids IS NULL ";
			} else if (flag == 1) { // 七牛
				sql += " AND MVCS.scids IS NOT NULL\n" +
						"AND (\n" +
						"	MCMS.`STORAGE_PATH` LIKE 'http://v.yiaiwang.com.cn%'\n" +
						"	OR MCMS.`STORAGE_PATH` LIKE 'http://ppt.yiaiwang.com.cn%'\n" +
						"	OR MCMS.`STORAGE_PATH` LIKE 'http://www.ablesky.com%'\n" +
						")";
			}
			sql += " limit " + (j * maxSize) + "," + maxSize;
			List<Object[]> list = SshMysqlYiaiwang.queryBySQL(sql);
			for (Object[] objects : list) {
				String p2name = MyUtils.valueOf(objects[2]);
				String p2id = MyUtils.valueOf(objects[3]);
				String p1name = MyUtils.valueOf(objects[4]);
				String p1id = MyUtils.valueOf(objects[5]);
				String courseId = MyUtils.valueOf(objects[6]);
				String sortOrder = MyUtils.valueOf(objects[7]);
				String fullName = MyUtils.valueOf(objects[8]).trim();
				String scids = MyUtils.valueOf(objects[9]);
				String expertName = MyUtils.valueOf(objects[10]).trim();
				String expertTitle = MyUtils.valueOf(objects[11]).trim();
				String expertPlace = MyUtils.valueOf(objects[12]).trim();
				String content = MyUtils.valueOf(objects[13]);
				String scidss = MyUtils.valueOf(objects[14]);
				
				// 查询结果去重
				String newCode = "EX-" + courseId + "-" + sortOrder;
				if (courseCodeList.contains(newCode)) {
					continue;
				} else {
					courseCodeList.add(newCode);
				}
				
				// 判断是否存在于新平台
				String courseCode = courseId + "-" + sortOrder;
				String[] courseCodes = {courseCode, "CME-" + courseCode, "EX-" + courseCode};
				boolean isExist = false;
				for (String string : courseCodes) {
					if (exitCodeSubMap.containsKey(string)) {
						courseCode = string;
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					System.out.println("没有在新平台查找到课程：" + courseId + "," + fullName);
					continue;
				}
				newCode = courseCode;
				
				
				// 重复验证
//				if (exitCodeSubMap.containsKey(courseId)) {
//					System.out.println("课程在新平台已存在：" + courseId + "," + fullName);
//					continue;
//				}
				
				// 检验学科是否存在，插入新学科
				String name2 = p1name.replaceAll("、", "");
				String name3 = "其他";
				String name4 = "其他";
				String name5 = "其他";
				if (subjectMap.containsKey(p2name)) {
					name2 = p2name;
					name3 = p1name;
				} else if (!subjectMap.containsKey(name2)) {
					name2 = "其他";
				}
				List<Object> obs = generateAddSubjectSql(name2, name3, name4, name5);
				addSubjectSqlList.addAll((List<String>)obs.get(0));
				YiaiSubject sub5 = (YiaiSubject)obs.get(1);
				
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
				String subjectCode = "";
				if (StringUtils.isNotBlank(scids)) {
					if (scids.contains(",")) {
						System.out.println("素材id有多个：" + scids);
						scids = scids.substring(0, scids.indexOf(","));
					}
					sql = "SELECT\n" +
							"	nengli.`NAME` AS nengliName,\n" +
							"	nengli.`CODE` AS nengliCode,\n" +
							"	laiyuan.`NAME` AS laiyuanName,\n" +
							"	laiyuan.`CODE` AS laiyuanCode,\n" +
							"	zhuti.`NAME` AS zhutiName,\n" +
							"	zhuti.`CODE` AS zhutiCode,\n" +
							"	zhicheng.`NAME` AS zhichengName,\n" +
							"	zhicheng.`CODE` AS zhichengCODE,\n" +
							"	subject.`NAME` AS subjectName,\n" +
							"	subject.`CODE` AS subjectCODE\n" +
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
							"LEFT JOIN study_materials_point point ON point.MATERIALS_ID = t.id\n" +
							"LEFT JOIN exam_prop_val subject ON subject.id = point.PROP_VAL_ID\n" +
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
						subjectCode = MyUtils.valueOf(sourceList.get(0)[9]);
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
					if (expertName.contains("；")) {
						expertName = expertName.substring(0, expertName.indexOf("；"));
					}
					if (expertName.contains(";")) {
						expertName = expertName.substring(0, expertName.indexOf(";"));
					}
					if (expertName.contains(",")) {
						expertName = expertName.substring(0, expertName.indexOf(","));
					}
					if (expertTitle.contains("、")) {
						expertTitle = expertTitle.substring(0, expertTitle.indexOf("、"));
					}
					if (expertPlace.contains("；")) {
						expertPlace = expertPlace.substring(0, expertPlace.indexOf("；"));
					}
					expertName = expertName.replaceAll("'", "").replaceAll("é", "e").replaceAll("í", "i").trim();
					if (expertName.length() > 20) {
						expertName = expertName.substring(0, 20).trim();
					}
					generateExpertSql(expertName, expertTitle, expertPlace, addSubjectSqlList, expertExcelList);
					expertId = expertMap.get(expertName + expertPlace).getId();
				}
				
				// 查询素材
				String subjectId = sub5.getId();
				String subCode = sub5.getCode();
				if (StringUtils.isNotBlank(scidss)) {
					scidss = scidss.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "");
					if (scidss.endsWith(",")) {
						scidss = scidss.substring(0, scidss.length() - 1);
					}
					scidss = "'" + scidss.replaceAll(",", "','") + "'";
					sql = "SELECT id,type,`NAME`,`CODE` FROM `exam_prop_val` WHERE id in (" + scidss + ")";
					List<Object[]>  sourceList = SshMysqlYiaiwangResource.queryBySQL(sql);
					if (CollectionUtils.isEmpty(sourceList)) {
						System.out.println("courseId=" + courseId + "没有查询到素材：" + sql);
					} else {
						for (Object[] objs : sourceList) {
							String scType = MyUtils.valueOf(objs[1]);
							String scName = MyUtils.valueOf(objs[2]);
							String scCode = MyUtils.valueOf(objs[3]);
							if (scType.equals("5")) { // 知识点
								if (subjectCodeIdMap.containsKey(scCode)) {
									subCode = scCode;
									subjectId = subjectCodeIdMap.get(scCode);
								}
							} else if (scType.equals("8") && StringUtils.isNotBlank(nengliName)) { // 能力
								nengliName = scName.replaceAll("\\\\", "");
								nengliCode = scCode;
								sql = generateNengliSql(nengliName, nengliCode);
								if (StringUtils.isNotBlank(sql)) {
									addSubjectSqlList.add(sql);
								}
								nengliId = nengliMap.get(nengliName).getId();
							} else if (scType.equals("14") && StringUtils.isNotBlank(laiyuanName)) { // 来源
								laiyuanName = scName.replaceAll("\\\\", "");
								laiyuanCode = scCode;
								sql = generateLaiyuanSql(laiyuanName, laiyuanCode);
								if (StringUtils.isNotBlank(sql)) {
									addSubjectSqlList.add(sql);
								}
								laiyuanId = laiyuanMap.get(laiyuanName).getId();
							}
						}
					}
				}
				if (subCode.startsWith("lv5-")) {
					if (StringUtils.isNotBlank(subjectCode)) {
						if (subjectCodeIdMap.containsKey(subjectCode)) {
							subCode = subjectCode;
							subjectId = subjectCodeIdMap.get(subjectCode);
						} else {
							System.out.println("新平台缺失学科：" + subjectCode);
						}
					}
				}
				
				// 文本类提取出中英文内容
				String[] notes = {"", ""};
				if (flag == 0) {
					notes = parseContent(content, courseId);
					if (StringUtils.isBlank(notes[0])) {
						System.out.println("内容为空：courseId=" + courseId + "，fullName=" + fullName);
						continue;
					}
				}
				
				// 插入至webtrn
				String newId = exitCodeSubMap.get(newCode);
//				String newId = MyUtils.uuid();
				String courseTypeId = (flag == 0 ? "ff80808155da5b850155dddc04d704d5" : "ff80808157084dfb0157085339f60008");
				String label = (flag == 0 ? "" : "鄂尔多斯七牛视频");
				String flagExam = (flag == 0 ? "40288a1c2f0acd2d012f0acf2a680002" : "40288a1c2f0acd2d012f0ace87040001");
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
						"		'" + flagExam + "', " +
						"		'0', " +
						"		'" + courseTypeId + "', " +
						"		now(), " +
						"		'0', " +
						"		'0', " +
						"		'0', " +
						"		'1', " +
						"		'" + subjectId + "', " +
						"		'" + label + "', " +
						(StringUtils.isBlank(zhutiId) ? " null," : " '" + zhutiId + "', ") +
						(StringUtils.isBlank(laiyuanId) ? " null," : " '" + laiyuanId + "', ") +
						(StringUtils.isBlank(nengliId) ? " null," : " '" + nengliId + "', ") +
						(StringUtils.isBlank(zhichengId) ? " null," : " '" + zhichengId + "', ") +
						(StringUtils.isBlank(expertId) ? " null," : " '" + expertId + "', ") +
						"		'bb3c4d5290f911e69b44848f69e05bf0' " +
//						"	) ON DUPLICATE KEY UPDATE fk_subject_id=values(fk_subject_id);";
						"	) ON DUPLICATE KEY UPDATE flag_ability=values(flag_ability),fk_origin_id=values(fk_origin_id);";
				addWebtrnCourse.add(sql);
				
				// 插入至课程空间
				addSpaceCourse.addAll(generateSpaceSql(flag, newId, toSql(fullName), courseId, newCode, subCode, laiyuanCode, nengliCode, zhichengName, zhutiName, notes));
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
	public String[] parseContent(String content, String courseId) {
		String[] results = {"", ""};
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
				} catch (FileNotFoundException e) {
					System.out.println("图片不存在：src=" + src);
					retry = 0;
				} catch (Exception e1){
					if (retry == 1) {
						System.out.println("第3次下载图片失败：src=" + src);
					}
					retry --;
					e1.printStackTrace();
				}
			}
		}
		try {
			// 提取中文
			Element chineseElement = doc.getElementsByClass("tabContentCurrent").first();
			addMenu(chineseElement, courseId);
			Element element = chineseElement.getElementById("course_1");
			if (element == null) {
				System.out.println("解析中文出错，courseId=" + courseId);
				return results;
			}
			results[0] = toSql(element.html());
			
			// 提取英文
			String tagsStyle = doc.getElementById("tags").attr("style"); // 判断是否是双版
			if (StringUtils.isBlank(tagsStyle) || !tagsStyle.contains("none")) { // 隐藏tab说明是单版
				Element engElement = doc.getElementsByClass("tabContenthidden").first();
				addMenu(engElement, courseId);
				element = engElement.getElementById("course_2");
				if (element == null) {
					System.out.println("解析英文出错，courseId=" + courseId);
					return results;
				}
				results[1] = toSql(element.html());
			}
		} catch (Exception e) {
			System.out.println("courseId=" + courseId + "解析中英文出错");
			e.printStackTrace();
		}
		
		return results;
	}
	
	/**
	 * 为中英文element的内容添加目录
	 * @param element
	 */
	public void addMenu(Element element, String courseId){
		Elements hrefs = element.getElementsByClass("muluA").select("a[href]"); // 提取目录
		for (Element hrefElement : hrefs) {
			String href = hrefElement.attr("href"); // 链接
			String key = hrefElement.html().trim();
			if (StringUtils.isNotBlank(href) && href.startsWith("#") && StringUtils.isNotBlank(key)) {
				String menuId = href.substring(1);
				Elements menuElements = element.getElementsByAttributeValue("name", menuId);
				if (menuElements == null || menuElements.isEmpty()) {
					System.out.println("courseId=" + courseId + "的标签不存在:" + menuId);
				} else {
					Element menuElement = menuElements.first();
					if (menuElement != null) {
						menuElement.before("<a class=\"anchor_my\" name=\"" + key + "\"></a>");
					} else {
						System.out.println("courseId=" + courseId + "的标签不存在:" + menuId);
					}
				}
			}
		}
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
	public List<String> generateSpaceSql(int flag, String newId, String fullName, String courseId, String newCode, String subCode, String originCode, String abilityName, String careerName, String themeName, String[] contents){
		List<String> resultList = new ArrayList<String>();
		String lan = "3";
		String ch = contents[0];
		String en = contents[1];
		if (StringUtils.isBlank(ch)) {
			lan = "2";
		}
		if (StringUtils.isBlank(en)) {
			lan = "1";
		}
		// 插入课程
		String label = (flag == 0 ? "" : "鄂尔多斯七牛视频EX");
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
				"		'" + label + "', " +
				"		'" + originCode + "', " +
				"		'" + abilityName + "', " +
				"		'" + careerName + "', " +
				"		'" + themeName + "', " +
				"		'0' " +
//				"	) ON DUPLICATE KEY UPDATE SUBJECT=values(SUBJECT);";
				"	) ON DUPLICATE KEY UPDATE ORIGIN=values(ORIGIN),ABILITY=values(ABILITY);";
		resultList.add(sql);
		// 插入11个column
//		String sectionId = "";
//		String textId = "";
//		String videoId = "";
//		for (int i = 0; i < columnType.length; i++) {
//			String id = MyUtils.uuid();
//			if (columnType[i].equals("section")) {
//				sectionId = id;
//			} else if (columnType[i].equals("text")) {
//				textId = id;
//			} else if (columnType[i].equals("video")) {
//				videoId = id;
//			} 
//			sql = "INSERT INTO `pe_workroom_column` (`ID`, `course`, `name`, `typt`, `serial`, `category`) " 
//					+ "VALUES ('" + id + "', '" + newId + "', '" + columnName[i] + "', '" + columnType[i] + "', '" + (i + 1) + "', '1') ON DUPLICATE KEY UPDATE id=id;";
//			resultList.add(sql);
//		}
//		// 插入info
//		String infoId = MyUtils.uuid();
//		sql = "INSERT INTO `scorm_course_info` (`ID`, `TITLE`, `CONTROL_TYPE`, `NAVIGATE`, `FK_COURSE_ID`)" 
//				+ "VALUES ('" + infoId + "', '" + fullName + "', 'choice', 'platform_nav, platform_nav', '" + newId + "') ON DUPLICATE KEY UPDATE id=id;";
//		resultList.add(sql);
//		// 插入item
//		String[] newItemIds = {MyUtils.uuid(), MyUtils.uuid(), MyUtils.uuid(), MyUtils.uuid()};
//		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
//				+ "VALUES ( '" + newItemIds[0] + "', '" + infoId + "', 'section', '" + fullName + "', '1', '1', '" + infoId + "', 'section', '1', '" + sectionId + "', NULL, '0' ) ON DUPLICATE KEY UPDATE id=id;";
//		resultList.add(sql);
//		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
//				+ "VALUES ( '" + newItemIds[1] + "', '" + infoId + "', 'section', '" + fullName + "', '2', '2', '" + newItemIds[0] + "', 'section', '1', '" + sectionId + "', '" + newItemIds[0] + "', '1' ) ON DUPLICATE KEY UPDATE id=id;";
//		resultList.add(sql);
//		sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `LOCATION` ) " 
//				+ "VALUES ( '" + newItemIds[2] + "', '" + infoId + "', 'section', '" + fullName + "', '3', '3', '" + newItemIds[1] + "', 'section', '1', '" + sectionId + "', '" + newItemIds[1] + "', '1-1' ) ON DUPLICATE KEY UPDATE id=id;";
//		resultList.add(sql);
//		if (flag == 0) {
//			sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `note_en`,note,`language` ) " 
//					+ "VALUES ( '" + newItemIds[3] + "', '" + infoId + "', 'text', '" + fullName + "', '4', '4', '" + newItemIds[2] + "', 'text', '1', '" + textId + "', '" + newItemIds[2] + "', '" + en + "', '" + ch + "', '" + lan + "' ) ON DUPLICATE KEY UPDATE id=id;";
//		} else {
//			sql = "INSERT INTO `scorm_course_item` ( `ID`, `FK_SCORM_COURSE_ID`, `TYPE`, `TITLE`, `SEQUENCE`, `THELEVEL`, `ITEM_ID`, `wareType`, `flagActive`, `column_Id`, `FK_PARENT_ID`, `SCO_TYPE`, `LAUNCH` ) " 
//					+ "VALUES ( '" + newItemIds[3] + "', '" + infoId + "', 'video', '" + fullName + "', '4', '4', '" + newItemIds[2] + "', 'video', '1', '" + videoId + "', '" + newItemIds[2] + "', 'QINIU', 'http://linchuang.yiaiwang.com.cn/my/elsevierproject/whaty/course_view.php?courseid=" + courseId + "' ) ON DUPLICATE KEY UPDATE id=id;";
//		}
//		resultList.add(sql);
		return resultList;
	}
	
	/**
	 * 补充缺失的二三四五级学科，生成sql语句
	 * @param name2
	 * @param name3
	 * @param name4
	 * @param name5
	 * @return 0:List<String> sql语句 ；1：YiaiSubject，五级学科
	 */
	public List<Object> generateAddSubjectSql(String name2, String name3, String name4, String name5){
		List<Object> result = new ArrayList<Object>();
		List<String> addSubjectSqlList = new ArrayList<String>();
		// 添加二级
		if (!subjectMap.containsKey(name2)) {
			name2 = "其他";
		}
		// 添加三级
		String parentId = subjectMap.get(name2).getId();
		Map<String, YiaiSubject> map = subjectMap.get(name2).getChildrenMap();
		if (!map.containsKey(name3)) {
			for (String key : map.keySet()) {
				if (key.contains(name3)) {
					name3 = key;
					break;
				}
			}
			if (!map.containsKey(name3)) {
				String id = MyUtils.uuid();
				YiaiSubject subject = new YiaiSubject(id, name3, 3, "lv3-" + id, parentId, new HashMap<String, YiaiSubject>());
				map.put(name3, subject);
				addSubjectSqlList.add(addSubject(subject));
			}
		}
		// 添加四级
		parentId = map.get(name3).getId();
		map = map.get(name3).getChildrenMap();
		if (!map.containsKey(name4)) {
			for (String key : map.keySet()) {
				if (key.contains(name4)) {
					name4 = key;
					break;
				}
			}
			if (!map.containsKey(name4)) {
				String id = MyUtils.uuid();
				YiaiSubject subject = new YiaiSubject(id, name4, 4, "lv4-" + id, parentId, new HashMap<String, YiaiSubject>());
				map.put(name4, subject);
				addSubjectSqlList.add(addSubject(subject));
			}
		}
		// 添加五级
		parentId = map.get(name4).getId();
		map = map.get(name4).getChildrenMap();
		if (!map.containsKey(name5)) {
			for (String key : map.keySet()) {
				if (key.contains(name5)) {
					name5 = key;
					break;
				}
			}
			if (!map.containsKey(name5)) {
				String id = MyUtils.uuid();
				YiaiSubject subject = new YiaiSubject(id, name5, 5, "lv5-" + id, parentId, new HashMap<String, YiaiSubject>());
				map.put(name5, subject);
				addSubjectSqlList.add(addSubject(subject));
			}
		}
		YiaiSubject subject5 = subjectMap.get(name2).getChildrenMap().get(name3).getChildrenMap().get(name4).getChildrenMap().get(name5);
		result.add(addSubjectSqlList);
		result.add(subject5);
		return result;
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
		courseCodeList = new ArrayList<String>();
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
				"left JOIN pe_subject s3 ON s3.fk_parent_id = s2.id\n" +
				"left JOIN pe_subject s4 ON s4.fk_parent_id = s3.id\n" +
				"left JOIN pe_subject s5 ON s5.fk_parent_id = s4.id\n" +
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
			if (StringUtils.isBlank(name2)) {
				continue;
			}
			if (!subjectMap.containsKey(name2)) {
				YiaiSubject subject = new YiaiSubject(id2, name2, 2, code2, "5cb212da69c311e6a147001e679d6af4", new HashMap<String, YiaiSubject>());
				subjectMap.put(name2, subject);
			}
			// 添加三级
			if (StringUtils.isBlank(name3)) {
				continue;
			}
			Map<String, YiaiSubject> map = subjectMap.get(name2).getChildrenMap();
			if (!map.containsKey(name3)) {
				YiaiSubject subject = new YiaiSubject(id3, name3, 3, code3, id2, new HashMap<String, YiaiSubject>());
				map.put(name3, subject);
			}
			// 添加四级
			if (StringUtils.isBlank(name4)) {
				continue;
			}
			map = map.get(name3).getChildrenMap();
			if (!map.containsKey(name4)) {
				YiaiSubject subject = new YiaiSubject(id4, name4, 4, code4, id3, new HashMap<String, YiaiSubject>());
				map.put(name4, subject);
			}
			// 添加五级
			if (StringUtils.isBlank(name5)) {
				continue;
			}
			map = map.get(name4).getChildrenMap();
			if (!map.containsKey(name5)) {
				YiaiSubject subject = new YiaiSubject(id5, name5, 5, code5, id4, new HashMap<String, YiaiSubject>());
				map.put(name5, subject);
			}
		}
		
		// 获取能力
		nengliMap = new HashMap<String, YiaiSubject>();
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
		zhutiMap = new HashMap<String, YiaiSubject>();
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
		zhichengMap = new HashMap<String, YiaiSubject>();
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
		laiyuanMap = new HashMap<String, YiaiSubject>();
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
		expertMap = new HashMap<String, Expert>();
		sql = "SELECT e.ID,e.`NAME`,e.WORKPLACE FROM 	pe_expert e WHERE 	e.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' and e.`NAME` != ''";
		list = SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : list) {
			String id = MyUtils.valueOf(objects[0]);
			String name = MyUtils.valueOf(objects[1]);
			String workPlace = MyUtils.valueOf(objects[2]);
			expertMap.put(name + workPlace, new Expert(id, name, workPlace, null));
		}
		// 获取已存在的课程
		exitCodeSubMap = new HashMap<String, String>();
		sql = "select c.`code`,c.fk_subject_id,c.id from pe_tch_course c where c.FK_SITE_ID='ff80808155da5b850155dddbec9404c9'";
		list = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : list) {
			String code = MyUtils.valueOf(objects[0]);
			String id = MyUtils.valueOf(objects[2]);
			if (StringUtils.isNotBlank(code)) {
				exitCodeSubMap.put(code, id);
			}
		}
		// 获取新平台的5级学科
		subjectCodeIdMap = new HashMap<String, String>();
		sql = "select s.`code`,s.id from pe_subject s where s.`level`='5' and s.fk_site_id='ff80808155da5b850155dddbec9404c9';";
		list = SshMysqlWebtrn.getBySQL(sql);
		for (Object[] objects : list) {
			String code = MyUtils.valueOf(objects[0]);
			String id = MyUtils.valueOf(objects[1]);
			subjectCodeIdMap.put(code, id);
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
		AddExtraCourse addExtraCourse = new AddExtraCourse();
		addExtraCourse.outputInsertSql(1);
		System.exit(0);
	}
}

