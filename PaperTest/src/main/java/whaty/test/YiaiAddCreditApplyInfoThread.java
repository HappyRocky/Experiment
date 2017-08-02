package whaty.test;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class YiaiAddCreditApplyInfoThread extends Thread {
	private int current;
	public static String fk_site_id = "ff80808155da5b850155dddbec9404c9";
	public static String sCClassId = "ff8080815a27e1a4015a353a4767005f"; //四川班级ID
	public static String qGClassId = "ff8080815a27e1a4015a353967cf0037";  //全国班级ID
	public static String projrctId = "ff8080815a27e1a4015a353864350032"; // 项目ID
	public YiaiAddCreditApplyInfoThread(int current) {
		this.current = current;
	}
	public void run(){
		int start = current*2000;
		List<Object[]> list = getlocalInfo(start);
		for(int m =0; m < list.size();m++ ){
			System.out.println(current+"========  "+m+"   start============"+list.size());
			Object[] obj = list.get(m);
			String applyId  = String.valueOf(obj[0]);
			String fk_trainee_id = String.valueOf(obj[1]);
			String type  = String.valueOf(obj[2]);
			String credit  = String.valueOf(obj[3]==null?"0":obj[3]);
			//根据学员ID去查询学员的绑卡信息
			int result = Integer.parseInt(credit);
			String  sql = " select id,CMESCORE1,CMESCORE2,common_score from pe_card_number  where FK_SSO_USER_ID ='"+fk_trainee_id+"'";
			List<Object[]> cardList =SshMysqlWebtrn.queryBySQL(sql);
			for(int i=0;i<cardList.size();i++){
				Object[] objCard = cardList.get(i);
				String id = String.valueOf(objCard[0]);
				String CMESCORE1 = String.valueOf(objCard[1]==null?"0":objCard[1]);
				String CMESCORE2 = String.valueOf(objCard[2]==null?"0":objCard[2]);
				String commonScore = String.valueOf(objCard[3]==null?"0":objCard[3]);
				
				int score =0;
				String creditType ="0"; //存在通用学分优先扣除通用学分
				if(StringUtils.isBlank(commonScore)||"null".equals(commonScore.toLowerCase())){
					if("XF1".equals(type)){
						score = Integer.parseInt(CMESCORE1);
						creditType ="0";
					}else if("XF2".equals(type)){
						score = Integer.parseInt(CMESCORE2);
						creditType ="1";
					}
				}else{
					score = Integer.parseInt(commonScore);
					creditType ="2";
				}
				int creditTypeScore = Math.abs(result);
				if(result>0){
					result = score - result;	
				}else{
					result = score + result;
				}
				int creditTypeScoreLeft = Math.abs(result);
				String insertSql ="";
				String updateSql ="";
				if("0".equals(creditType)){
					insertSql =" INSERT INTO pe_credit_apply_card (`id`, `fk_credit_apply_id`, `fk_card_number_id`, `deducted_score1`, `deducted_score2`, `deducted_common_score`, `fk_site_id`) VALUES ('"+getUUID()+"', '"+applyId+"', '"+id+"', '"+creditTypeScore+"', '0', '0', 'ff80808155da5b850155dddbec9404c9');";
					updateSql =" UPDATE pe_card_number set  CMESCORE1_USABLE ='"+creditTypeScoreLeft+"' where id='"+id+"';" ;
				}else if("1".equals(creditType)){
					insertSql =" INSERT INTO pe_credit_apply_card (`id`, `fk_credit_apply_id`, `fk_card_number_id`, `deducted_score1`, `deducted_score2`, `deducted_common_score`, `fk_site_id`) VALUES ('"+getUUID()+"', '"+applyId+"', '"+id+"', '0', '"+creditTypeScore+"', '0', 'ff80808155da5b850155dddbec9404c9');";
					updateSql =" UPDATE pe_card_number set  CMESCORE2_USABLE ='"+creditTypeScoreLeft+"' where id='"+id+"';" ;
				}else if("2".equals(creditType)){
					insertSql =" INSERT INTO pe_credit_apply_card (`id`, `fk_credit_apply_id`, `fk_card_number_id`, `deducted_score1`, `deducted_score2`, `deducted_common_score`, `fk_site_id`) VALUES ('"+getUUID()+"', '"+applyId+"', '"+id+"', '0', '0', '"+creditTypeScore+"', 'ff80808155da5b850155dddbec9404c9');";
					updateSql =" UPDATE pe_card_number set  common_score_usable ='"+creditTypeScoreLeft+"' where id='"+id+"';" ;
				}
				SshMysqlWebtrn.writeTxt(insertSql, "sql", "pe_credit_apply_card",current);
				SshMysqlWebtrn.writeTxt(updateSql, "sql", "pe_card_number",current);
				if(result > 0){
					break;
				}
			}
		}
		System.out.println(current+"========执行完毕");
	}
	public  List<Object[]> getlocalInfo(int start){
		String sql ="select id,fk_trainee_id,type,credit from pe_credit_apply where credit is not null and fk_trainee_id not in (select id from pe_trainee where LOGIN_ID like 'erds2017@%'  or LOGIN_ID LIKE 'dltq@%' or LOGIN_ID LIKE 'YAYC17%') limit "+start+",2000  ";
		System.out.println(current+"========sql:"+sql);
		List<Object[]> list =SshMysqlWebtrn.queryBySQL(sql);
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