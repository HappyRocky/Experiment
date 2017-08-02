package whaty.test;

import java.util.List;
import java.util.UUID;


public class AddWebtrnUser {
	public static void main(String args[]){
		String sql =" select LOGIN_ID,TRUE_NAME from pe_trainee where LOGIN_ID like 'YAYC17%' ";
		List<Object[]> list =SshMysqlWebtrn.queryBySQL(sql);
		AddWebtrnUser user =new AddWebtrnUser();
		for(int i =0;i<list.size();i++){
			Object[] obj = list.get(i);
			String loginId= String.valueOf(obj[0]);
			String name= String.valueOf(obj[1]);
			user.simulationLogin(loginId, name);
		}
		System.out.println("==========  完毕");
	}
	
	public  void simulationLogin(String loginId,String name){
		
		String id1 = getUUID();
		String id2 = getUUID();
		String sql_1 =" INSERT INTO `whatysns`.`sso_user` (`ID`, `LOGIN_ID`, `NICK_NAME`, `PASSWORD`, `FK_ROLE_ID`, `FLAG_ISVALID`, `FLAG_BAK`, `LOGIN_NUM`, `LAST_LOGIN_DATE`, `LAST_LOGIN_IP`, `cas_id`, `fk_site_id`, `create_date`, `update_date`, `tags`, `avatar_id`, `EMAIL`, `MALE`, `TURN_OFF_NOTIFY`, `HAVE_FOLLOW_SIZE`, `FOLLOW_ME_SIZE`, `TRENDS_SIZE`, `ONLINE_TIME`, `TOTAL_TIME`, `course_study_isfirst`, `true_name`, `ORGANCODE`, `modifyDate`) VALUES ('"+id1+"', '"+loginId+"', '"+name+"', '"+loginId+"', '4028809127b83e450127b841cf580003', NULL, '0', NULL, NULL, NULL, NULL, 'ff80808155da68f00155dde956ca01d9', '2017-05-17', NULL, NULL, '1', NULL, '1', NULL, '0', '0', '0', '0', '0', NULL, '压测', NULL, '2017-05-17 14:33:18');";
		String sql_2 =" INSERT INTO `whatysns`.`pe_student` (`ID`, `LOGIN_ID`, `NAME`, `TRUE_NAME`, `FK_SSO_USER_ID`, `CARD_NO`, `GENDER`, `FOLK`, `STATUS`, `ADDRESS`, `ZIP`, `MOBILE`, `EMAIL`, `photo_link`, `NOTE`, `BIRTH_DATE`, `PHONE`, `FLAG_ISVALID`, `fk_site_id`, `EDUCATION`, `CAREER`, `WORK_PLACE`, `areaProvince`, `areaCity`, `PHONE_HOME`, `QUIZ_PRE`, `QUIZ_SUF`, `department`, `professionalTitle`, `position`, `graduateTime`, `presentMajor`, `workTime`, `companyGroup`, `companySort`, `fax`, `memo`, `faxamount`, `doctorOperationNo`, `professionalTitleNo`, `certificatePhoto`, `photoRootPath`, `invocie_citye`, `invocie_province`, `last_point`, `doctor_operation_num`, `qq`, `areaCounty`) VALUES ('"+id2+"', '"+loginId+"', NULL, '"+name+"', '"+id1+"', NULL, '男', NULL, NULL, NULL, NULL, '13900000000', NULL, NULL, NULL, NULL, NULL, NULL, 'ff80808155da68f00155dde956ca01d9', NULL, NULL, NULL, NULL, NULL, NULL, '-1', '-1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);";
		SshMysqlWebtrn.writeTxt(sql_1, "sql", "1");
		SshMysqlWebtrn.writeTxt(sql_2, "sql", "1");
	}
	
	
	public String getUUID() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replace("-", "");
		return uuid;
	}
}
