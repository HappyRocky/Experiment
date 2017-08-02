package whaty.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;

public class AddQiniuVideo {
	/**
	 * 去本地库获取需要添加的视频信息
	 * @return
	 */
	public List<Map<String, String>> getAddVideoInfo() {
		//String sql = "select id,shortname,url,category from course_qiniu ";
		String sql = "select id,courseId,courseName,url from yiai_course_url_qiniu where courseName  in ("+

"'PPT语音工具-操作指南',"+
"'PPT语音工具的原理',"+
"'PPT语音工具-实现零噪微课件的方法',"+
"'Camtisia Studio特效-画中画',"+
"'CamtasiaStudio音轨合成',"+
"'科大讯飞语音包v1.0安装与调试',"+
"'甲的解剖及生物学基础',"+
"'色甲',"+
"'半导体激光在口腔种植修复和美容齿科的应用',"+
"'常见皮肤病及系统性疾病的甲损害',"+
"'微生物检验前的质量控制',"+
"'体液检验实验室质量管理的基本要求',"+
"'血液检验实验室质量管理的基本要求',"+
"'从微生物标本规范化采集看质量管理',"+
"'临床化学检验质量管理（1） ',"+
"'临床化学检验质量管理（2）',"+
"'定性免疫学检验的临床应用',"+
"'定量免疫学检验的质量保证',"+
"'血细胞分析显微镜复检规则的建立与验证',"+
"'临床检验质量管理体系',"+
"'人员培训、考核与能力评估',"+
"'仪器设备管理',"+
"'临床基因扩增检验实验室污染防护',"+
"'肺功能检查',"+
"'食品安全事故现场调查中样品的采集',"+
"'食品安全事故流行病学调查工作规范与技术指南',"+
"'食品安全事故流行病学调查报告撰写',"+
"'食源性疾病暴发现场流行病学调查步骤简介',"+
"'食品安全事故现场调查中的流行病学资料分析方法',"+
"'食品安全事故卫生学调查')";
		List<Object[]> list = SshMysqlLocal.queryBySQL(sql);
		List<Map<String, String>> courselist = new ArrayList<Map<String, String>>();
		String url ="http://linchuang.yiaiwang.com.cn/my/elsevierproject/whaty/course_view.php?courseid=";
		for (int i = 0; i < list.size(); i++) {
			Object[] obj = list.get(i);
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", String.valueOf(obj[0]));
			map.put("courseId", String.valueOf(obj[1]));
			map.put("url", url+String.valueOf(obj[1]));
			map.put("shortname", String.valueOf(obj[2]));
			//map.put("category", String.valueOf(obj[3]));
			courselist.add(map);
		}
		return courselist;
	}
	
	/**
	 * 根据本地课程去线上查询需要修改的课程
	 */
	public static void main(String[]args){
		AddQiniuVideo video = new AddQiniuVideo();
		List<Map<String, String>> list = video.getAddVideoInfo();
		for(int i =0;i<list.size();i++){
			Map<String, String> map = list.get(i);
			System.out.println("=================:"+map.get("shortname")+"==================");
			video.getOnlineCourseInfo(map.get("shortname"),map.get("url"));
		}
		
	} 
	/**
	 * 根据课程名称查询课程信息并查看当前视频是否存在对应信息
	 */
	public void getOnlineCourseInfo(String courseName,String url){
		String sql =" select id,name from pe_tch_course where site_code='yiai' and name='"+courseName+"'";
		List<Object[]> list = SshMysql.queryBySQL(sql);
		for(int i=0;i<list.size();i++){
			Object[] obj = list.get(i);
			String id= String.valueOf(obj[0]);
			///// pe_workroom_column
			sql= " select id,typt,serial from pe_workroom_column where  course ='"+id+"' order by serial";
			List<Object[]> listVideo = SshMysql.queryBySQL(sql);
			Map<String,String> map =new HashMap<String,String>();
			for(int j=0;j<listVideo.size();j++ ){
				Object[] objList = listVideo.get(j); 
				map.put(String.valueOf(objList[1]), String.valueOf(objList[0]));
			}
			if(!map.containsKey("section")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '章节', 'section', '1', '1');";
				map.put("section", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("video")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '视频', 'video', '2', '1');";
				map.put("video", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("doc")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '文档', 'doc', '3', '1');";
				map.put("doc", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("text")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '图文', 'text', '4', '1');";
				map.put("text", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("resource")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '下载资料', 'resource', '5', '1');";
				map.put("resource", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("courseware")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '电子课件', 'courseware', '6', '1');";
				map.put("courseware", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("link")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '链接', 'link', '7', '1');";
				map.put("link", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("homework")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '作业', 'homework', '8', '1');";
				map.put("homework", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("topic")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '主题讨论', 'topic', '9', '1');";
				map.put("topic", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("test")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '测试', 'test', '10', '1');";
				map.put("test", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			if(!map.containsKey("exam")){
				String section_id = getUUID();
				String insertSql = "INSERT INTO pe_workroom_column (`ID`, `course`, `name`, `typt`, `serial`, `category`) VALUES ('"+section_id+"', '"+id+"', '考试', 'exam', '11', '1');";
				map.put("exam", section_id);
				SshMysql.executeBySQL(insertSql);
			}
			///// scorm_course_info
			sql =" select id from scorm_course_info where fk_course_id='"+id+"' ";
			List<Object[]> listsci = SshMysql.queryBySQL(sql);
			String sciId ="";
			if(CollectionUtils.isEmpty(listsci)){
				sciId = getUUID();
				String insertSciSQl= " INSERT INTO scorm_course_info (ID, TITLE, CONTROL_TYPE, VERSION, DESCRIPTION, NAVIGATE, FK_COURSE_ID) VALUES ('"+sciId+"', '"+courseName+"', 'choice', NULL, NULL, 'platform_nav, platform_nav', '"+id+"');";
				SshMysql.executeBySQL(insertSciSQl);
			}else{
				sciId = String.valueOf(listsci.get(0)[0]);
			}
			////scorm_course_item
			sql =" select id,thelevel from scorm_course_item  where FK_SCORM_COURSE_ID='"+sciId+"'  ";
			List<Object[]> listscii = SshMysql.queryBySQL(sql);
			if(CollectionUtils.isEmpty(listscii)){
				String level1Id = this.getUUID();
				String insertSql =" INSERT INTO scorm_course_item (id,FK_SCORM_COURSE_ID,type,title,launch,sequence,thelevel,item_id,wareType,flagActive,column_id,fk_parent_id,location) VALUES ('"+level1Id+"','"+sciId+"','section','"+courseName+"',null,1,1,'"+sciId+"','section','1','"+map.get("section")+"',null,'0');";
				SshMysql.executeBySQL(insertSql);
				String level2Id = this.getUUID();
				String insertSql2 =" INSERT INTO scorm_course_item (id,FK_SCORM_COURSE_ID,type,title,launch,sequence,thelevel,item_id,wareType,flagActive,column_id,fk_parent_id,location) VALUES ('"+level2Id+"','"+sciId+"','section','"+courseName+"',null,2,2,'"+level1Id+"','section','1','"+map.get("section")+"','"+level1Id+"','1');";
				SshMysql.executeBySQL(insertSql2);
				String level3Id = this.getUUID();
				String insertSql3 =" INSERT INTO scorm_course_item (id,FK_SCORM_COURSE_ID,type,title,launch,sequence,thelevel,item_id,wareType,flagActive,column_id,fk_parent_id,location) VALUES ('"+level3Id+"','"+sciId+"','section','"+courseName+"',null,3,3,'"+level2Id+"','section','1','"+map.get("section")+"','"+level2Id+"','1-1');";
				SshMysql.executeBySQL(insertSql3);
				String level4Id = this.getUUID();
				String insertSql4 =" INSERT INTO scorm_course_item (id,FK_SCORM_COURSE_ID,type,title,launch,sequence,thelevel,item_id,wareType,flagActive,column_id,fk_parent_id,location) VALUES ('"+level4Id+"','"+sciId+"','video','"+courseName+"','"+url+"',4,4,'"+level3Id+"','video','1','"+map.get("video")+"','"+level3Id+"',null);";
				SshMysql.executeBySQL(insertSql4);
			}else{
				if(listscii.size() < 4){
					sql =" select id,SEQUENCE from scorm_course_item  where FK_SCORM_COURSE_ID='"+sciId+"'  and THELEVEL='3' ORDER BY SEQUENCE  ";
					List<Object[]> listSciiInfo = SshMysql.queryBySQL(sql);
					String level2Id ="";
					for(int j=0;j<listscii.size();j++){
						Object [] obj1 = listscii.get(j);
						String itemid = String.valueOf(obj1[0]);
						String thelevel = String.valueOf(obj1[1]);
						if("2".equals(thelevel)){
							level2Id = itemid;
						}
					}
					String level3Id = this.getUUID();
					String itemId ="";
					int sequence = 4;
					if(listSciiInfo.size() < 1){
						String insertSql3 =" INSERT INTO scorm_course_item (id,FK_SCORM_COURSE_ID,type,title,launch,sequence,thelevel,item_id,wareType,flagActive,column_id,fk_parent_id,location) VALUES ('"+level3Id+"','"+sciId+"','section','"+courseName+"',null,3,3,'"+level2Id+"','section','1','"+map.get("section")+"','"+level2Id+"','1-1');";
						SshMysql.executeBySQL(insertSql3);
						itemId = level3Id;
					}else{
						itemId = String.valueOf(listSciiInfo.get(0)[0]);
						sequence = Integer.parseInt(String.valueOf(listSciiInfo.get(0)[1]))+1;
					}
					String updateSql = " update scorm_course_item set SEQUENCE = SEQUENCE+1 where SEQUENCE > "+sequence+" and FK_SCORM_COURSE_ID='"+sciId+"'";
					String insertSql =" INSERT INTO scorm_course_item (id,FK_SCORM_COURSE_ID,type,title,launch,sequence,thelevel,item_id,wareType,flagActive,column_id,fk_parent_id,location) VALUES ('"+getUUID()+"','"+sciId+"','video','"+courseName+"','"+url+"',"+sequence+",4,'"+itemId+"','video','1','"+map.get("video")+"','"+itemId+"',null);";
					SshMysql.executeBySQL(updateSql);
					SshMysql.executeBySQL(insertSql);
				}
			}
		}
	}
	
	public String getUUID() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replace("-", "");
		return uuid;
	}
}
