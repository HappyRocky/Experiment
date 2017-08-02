package whaty.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YiaiAddStudentCourseThread extends Thread {
	private int current;
	public static String fk_site_id = "ff80808155da5b850155dddbec9404c9";
	public static String sCClassId = "ff8080815a27e1a4015a353a4767005f"; //四川班级ID
	public static String qGClassId ="ff8080815a27e1a4015a353967cf0037";  //全国班级ID
	public static String projrctId = "ff8080815a27e1a4015a353864350032"; // 项目ID
	public YiaiAddStudentCourseThread(int current) {
		this.current = current;
	}
	public void run(){
		int start = current*5000;
		System.out.println(current+"========执行开始                    +++++++++++++++ isSichuan:");
		List<Object[]> list = getlocalInfo(start);
		for(int m =0; m < list.size();m++ ){
			System.out.println(current+"========  "+m+"   start============"+list.size());
			Object[] obj = list.get(m);
			String fk_trainee_id = String.valueOf(obj[0]);
			String packageName = String.valueOf(obj[1]);
			String prj_item_no = String.valueOf(obj[2] == null?"":obj[2] ); 
			String cme_credit_type_id = String.valueOf(obj[3] == null?"":obj[3]);
			String credit = String.valueOf(obj[4] == null?"0":obj[4]);
			String certnumber = String.valueOf(obj[5] == null?"":obj[5]);
			String classId = String.valueOf(obj[6]);
			//根据班级去获取课程包信息及课程包下的课程信息
			String opencourse_sql= " select DISTINCT 	poc.id as openCourseId ,prc.id as classPackageId from pr_class_coursepackage prc, pe_open_course poc, pe_course_package pcp, pr_coursepackage_course pcc where prc.fk_class =poc.FK_TRAIN_CLASS and prc.fk_coursepackage = pcp.id and pcc.fk_coursepackage = pcp.id and poc.FK_TCH_COURSE = pcc.fk_tchcourse and fk_class ='"+classId+"' and pcp.`name` ='"+packageName+"'";
			List<Object[]> courseList = SshMysqlWebtrn.queryBySQL(opencourse_sql);
			if(courseList.size() > 0){
				String packageId = String.valueOf(courseList.get(0)[1]);;
				//添加选课 webtrn  课程空间
				for(int j=0;j<courseList.size();j++){
					String openCourseId = String.valueOf(courseList.get(j)[0]);
					String ele_webtrnId = getUUID();
					String insert_ele_webtrn="INSERT INTO pe_tch_elective ( `ID`, `FK_TRAINEE_ID`, `FK_OPENCOURSE_ID`, `ELECTIVE_DATE`, `STATUS`, `FLAG_FEE_CHECK`, `SCORE`, `FK_SITE_ID` ) VALUES ( '"+ele_webtrnId+"', '"+fk_trainee_id+"', '"+openCourseId+"', now(), '40288a1c2edc36bf012edc387fe70001', '40288a962f15bc98012f15c3cd970002', '100.0', 'ff80808155da5b850155dddbec9404c9' );";
					SshMysqlWebtrn.writeTxt(insert_ele_webtrn, "sql", "ele_webtrn",current);
					String insert_ele_learn =" INSERT INTO pr_tch_stu_elective ( `ID`, `FK_STU_ID`, `FK_COURSE_ID`, `ELECTIVE_DATE`, `SCORE_TOTAL`, `FLAG_COURSE_FINISH`, `score2`, `LEAR_TIME`, `modifyDate`, `FLAG_ELECTIVE_STATUS` ) VALUES ( '"+ele_webtrnId+"', '"+fk_trainee_id+"', '"+openCourseId+"', now(), '100.0', '1', '100.0', '0', now(), '0' );";
					SshMysqlWebtrn.writeTxt(insert_ele_learn, "sql", "ele_learn",current);
				}
				courseList.clear();
				//添加证书申请
				String applyId = getUUID();
				String type="";
				String learnTime = "0";
				String FK_CERTIFICATE_MODEL_ID ="";
				String FLAG_CME_TYPE ="";
				if(cme_credit_type_id.equals("1")){
					type ="XF1";
					learnTime = String.valueOf(Integer.parseInt(credit)*3);
					if(sCClassId.equals(classId)){
						FK_CERTIFICATE_MODEL_ID ="ff80808159832e9001598683e1a52478";
					}else if(qGClassId.equals(classId)){
						FK_CERTIFICATE_MODEL_ID ="ff8080815764d2120157654a06890176";
					}
					FLAG_CME_TYPE ="43531506cbdc11e6975b00251113d11d";
				}else{
					type ="XF2";
					learnTime = String.valueOf(Integer.parseInt(credit)*6);
					if(sCClassId.equals(classId)){
						FK_CERTIFICATE_MODEL_ID ="ff80808159832e90015986843ca52479";
					}else if(qGClassId.equals(classId)){
						FK_CERTIFICATE_MODEL_ID ="ff80808157084c0001570938577f02d0";
					}
					FLAG_CME_TYPE ="4377af1ccbdc11e6975b00251113d11d";
				}
				//学分申请
				String insert_apply_sql =" INSERT INTO pe_credit_apply ( `id`, `fk_class_id`, `type`, `credit`, `create_date`, `flag_isvalid`, `fk_trainee_id`, `fk_site_id`, `fk_coursePac_id` ) VALUES ( '"+applyId+"', '"+classId+"', '"+type+"', '"+credit+"', now() , '40288a962e9d9ac5012e9dd6b0aa0004', '"+fk_trainee_id+"', '"+fk_site_id+"', '"+packageId+"' );";
				SshMysqlWebtrn.writeTxt(insert_apply_sql, "sql", "pe_credit_apply",current);
				//证书信息
				String certificateId = getUUID();
				String insert_student_certificate = " INSERT INTO pr_student_certificate ( `id`, `Fk_student_id`, `key`, `createDate`, `Fk_site_id`, `CertificateNo`, `APPLYDATE`, `FK_CERTIFICATE_MODEL_ID`, `FLAG_APPLY_STATE`, `PROJECTNO`, `PROJECTNAME`, `FLAG_CME_TYPE`, `LEARNTIME`, `LEARNSCORE` ) VALUES ( '"+certificateId+"', '"+fk_trainee_id+"', '"+classId+"', now(), '"+fk_site_id+"', '"+certnumber+"',  now(), '"+FK_CERTIFICATE_MODEL_ID+"', '40288a962e9d9ac5012e9dd6b0aa0004', '"+prj_item_no+"', '"+packageName+"', '"+FLAG_CME_TYPE+"', '"+learnTime+"', '"+credit+"' );";
				SshMysqlWebtrn.writeTxt(insert_student_certificate, "sql", "pr_student_certificate",current);
				
			}
		}
		System.out.println(current+"========执行完毕");
	}
	

	//获取班级开课
	public  List<Object[]> getOpenCourseByClassId(String classId){
		String sql = " select id from pe_open_course where FK_TRAIN_CLASS ='"+classId+"' and FK_SITE_ID ='"+fk_site_id+"' ";
		List <Object[]> courseList = SshMysqlWebtrn.queryBySQL(sql);
		return courseList;
	}
	
	public  List<String> getClassModule(String classId){
		List <String> moduleList = new ArrayList<String>();
		if(sCClassId.equals(classId)){
			moduleList.add("ff8080815a27e1a4015a353b3d640065");
			moduleList.add("ff8080815a27e1a4015a353b3d6f0066");
			moduleList.add("ff8080815a27e1a4015a353b3d8a0068");
			moduleList.add("ff8080815a27e1a4015a353b3d930069");
		}else if(qGClassId.equals(classId)){
			moduleList.add("ff8080815a27e1a4015a353969d8005b");
			moduleList.add("ff8080815a27e1a4015a353969d8005c");
			moduleList.add("ff8080815a27e1a4015a353969d9005e");
		}
		return moduleList;
	}
	
	public  List<Object[]> getlocalInfo(int start){
		String sql ="select fk_trainee_id,`name` as packageName,prj_item_no,cme_credit_type_id,credit,certnumber,fk_training_class_id from yiai_certificate_info where fk_trainee_id is not null and fk_training_class_id is not null and status ='0' limit "+start+",5000  ";
		List<Object[]> list =SshMysqlLocal.queryBySQL(sql);
		return list;
	}
	
	public String getUUID() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replace("-", "");
		return uuid;
	}
	
	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}
}
