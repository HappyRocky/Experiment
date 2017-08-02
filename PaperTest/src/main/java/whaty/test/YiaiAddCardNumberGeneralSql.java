/*
 * 文件名：YiaiAddCardNumberGeneralSql.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月3日下午6:10:08
 */
package whaty.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class YiaiAddCardNumberGeneralSql {
	
	private List<List<String>> cardTypeList; // 学分卡类型，共8种 
	private List<List<String>> cardAreaList; // 不同编码段的学分卡的类型
	
	private void init(){
		cardTypeList = new ArrayList<>();
		cardTypeList.add(Arrays.asList("1","国家一类10分 + 远程二类5分","10","5"));
		cardTypeList.add(Arrays.asList("2","国家一类5分","5","0"));
		cardTypeList.add(Arrays.asList("3","远程二类5分","0","5"));
		cardTypeList.add(Arrays.asList("4","远程二类10分","0","10"));
		cardTypeList.add(Arrays.asList("5","国家一类10分","10","0"));
		cardTypeList.add(Arrays.asList("6","国家一类10分 + 远程二类10分","10","10"));
		cardTypeList.add(Arrays.asList("7","国家一类5分 + 远程二类5分","5","5"));
		cardTypeList.add(Arrays.asList("8","远程二类15分","0","15"));
		cardTypeList.add(Arrays.asList("9","国家一类15分","15","0"));
		cardTypeList.add(Arrays.asList("10","通用学分卡20分","0","0"));
		
		cardAreaList = new ArrayList<>();
		// 江苏
		cardAreaList.add(Arrays.asList("A2201611410","A2201613984","ff8080815bd94514015be5e8f0a7068c","2017-12-31"));
		cardAreaList.add(Arrays.asList("A3201616166","A3201616836","ff8080815bd94514015be5e8f0a7068c","2017-12-31"));
		cardAreaList.add(Arrays.asList("B1201616215","B1201616984","ff8080815bd94514015be5e8f0a7068c","2017-12-31"));
		// 四川
		cardAreaList.add(Arrays.asList("Z201703064","Z201706063","ff808081596ef8bd015972cab8eb0204","2017-12-31"));
		cardAreaList.add(Arrays.asList("Z201706064","Z201708463","ff808081596ef8bd015972cab8eb0204","2017-12-31"));
		cardAreaList.add(Arrays.asList("A2201613985","A2201620984","ff808081596ef8bd015972cab8eb0204","2017-12-31"));
		cardAreaList.add(Arrays.asList("B2201606401","B2201612000","ff808081596ef8bd015972cab8eb0204","2017-12-31"));
		cardAreaList.add(Arrays.asList("A2201620985","A2201625055","ff808081596ef8bd015972cab8eb0204","2017-12-31"));
		cardAreaList.add(Arrays.asList("B2201616985","B2201619737","ff808081596ef8bd015972cab8eb0204","2017-12-31"));
		// 湖南
		cardAreaList.add(Arrays.asList("A1201600001","A1201605300","ff8080815bd93dea015be5e9d64a0576","2017-12-31"));
		cardAreaList.add(Arrays.asList("A3201616837","A3201617836","ff8080815bd93dea015be5e9d64a0576","2017-12-31"));
		// 湖北
		cardAreaList.add(Arrays.asList("A1201612801","A1201614400","ff8080815c455396015c48ad1ceb01fd","2017-12-31"));
		// 重庆
		cardAreaList.add(Arrays.asList("A1201611201","A1201611600","ff8080815bd93dea015be5eaf786057f","2018-12-31"));
		cardAreaList.add(Arrays.asList("A2201607451","A2201607550","ff8080815bd93dea015be5eaf786057f","2018-12-31"));
		cardAreaList.add(Arrays.asList("A2201607301","A2201607400","ff8080815bd93dea015be5eaf786057f","2018-12-31"));
		cardAreaList.add(Arrays.asList("A2201608001","A2201609600","ff8080815bd93dea015be5eaf786057f","2018-12-31"));
		cardAreaList.add(Arrays.asList("A2201610610","A2201611409","ff8080815bd93dea015be5eaf786057f","2018-12-31"));
		// 意林网
		cardAreaList.add(Arrays.asList("Z201700012","Z201703011","ff8080815bd93dea015be5ec88670584","2017-12-31"));
		// 甘肃
		cardAreaList.add(Arrays.asList("A2201511001","A2201511500","ff8080815bd94219015be5eb6c5805a9","2017-12-31"));
		cardAreaList.add(Arrays.asList("A2201512501","A2201513000","ff8080815bd94219015be5eb6c5805a9","2017-12-31"));
	}
	
	/**
	 * 从旧数据库中拼接insert卡片的语句
	 * @return
	 */
	public List<String> generalSelectSqlList(){
		init();
		List<String> result = new ArrayList<>();
		for (List<String> list : cardTypeList) {
			UUID uuid = UUID.randomUUID();
			String id = uuid.toString().replaceAll("-", "");
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT CONCAT( ");
			sb.append(" 		\"INSERT INTO pe_card_number (`ID`, `NAME`, `NUMBER`, `SERIAL_CODE`, `CREATE_YEAR`, `CODE`, `PASSWORD`, "
					+ "`CREATEDATE`, `VALIDDATE`, `BINDDATE`, `ACTIVATIONDATE`, `UPDATEDATE`, `MANAGERNAME`, `SUM`, `PRICE`, `CMESCORE1`, `CMESCORE2`, `CMESCORE1_USABLE`, `CMESCORE2_USABLE`, `DESCRIPTION`, `FK_SSO_USER_ID`, `FK_SITE_ID`, `FK_CARD_TYPE_ID`, `FLAG_ISVALID`, `FLAG_ISACTIVATION`, `FLAG_ISUSED`, `FK_TRAINING_PROJECT_ID`, `FK_TRAINING_CLASS_ID`, `FLAG_CARD_SOURCE`, `FK_TRAINEE_ID`, `FK_MANAGER_ID`) VALUES('"+id+"', ");
			sb.append(" 		'" + list.get(1) + "', '\",  ");
			sb.append(" 		a.NUMBER, ");
			sb.append(" 		\"', '\",  ");
			sb.append(" 		a.SERIAL_CODE, ");
			sb.append(" 		\"', '\",  ");
			sb.append(" 		a.CREATE_YEAR, ");
			sb.append(" 		\"', '\",  ");
			sb.append(" 		a. CODE,  ");
			sb.append(" 		\"', '\",  ");
			sb.append(" 		a.pwd,  ");
			sb.append(" 		\"', NOW(), '2017-12-21', null, '\",a.activeTime,\"', null, null, '', 0, '" + list.get(2) + "', '" + list.get(3) + "', '" + list.get(2) + "', '" + list.get(3) + "', '', '\",IFNULL(a.binduser,''),\"', 'ff80808155da5b850155dddbec9404c9', 'ff808081576b2bfc01576b7442360003', '40288a962e9d9ac5012e9dd6b0aa0004', '\",IF(a.active=99,'190d29945f7f11e69b44848f69e05bf0','190959545f7f11e69b44848f69e05bf0'),\"', '19060f605f7f11e69b44848f69e05bf0', NULL, NULL, '3552e55194ff11e6be7b001e679d0a23', null, null) ON DUPLICATE KEY UPDATE ACTIVATIONDATE='\",a.activeTime,\"',FK_SSO_USER_ID='\",a.binduser,\"';\" ");
			sb.append(" 	) FROM (  ");
			sb.append(" 		SELECT  ");
			sb.append(" 			c.card_number AS CODE,  ");
			sb.append(" 			IF(LENGTH(c.card_number) = 10,SUBSTR(c.card_number, 1, 1),SUBSTR(c.card_number, 1, 2)) AS NUMBER,  ");
			sb.append(" 			IF(LENGTH(c.card_number) = 10,SUBSTR(c.card_number, 6, 5),SUBSTR(c.card_number, 7, 5))  AS SERIAL_CODE,  ");
			sb.append(" 			IF(LENGTH(c.card_number) = 10,SUBSTR(c.card_number, 2, 4),SUBSTR(c.card_number, 3, 4))  AS CREATE_YEAR,  ");
			sb.append(" 			c.card_password AS pwd,  ");
			sb.append(" 			u.username AS binduser,  ");
			sb.append(" 			c.active AS active,  ");
			sb.append(" 			c.active_time AS activeTime  ");
			sb.append(" 		FROM  ");
			sb.append(" 			mdl_below_card c  ");
			sb.append(" 		JOIN mdl_user u ON u.id=c.user_id ");
			sb.append(" 		WHERE  ");
			sb.append(" 			c.category = '" + list.get(0) + "' and c.active_time >= '2017-07-12 19:00:00' "); // and c.active_time < '2017-07-03 10:15:00' ");
			sb.append(" 		and u.username in ('19216026@qq.com','805998570@qq.com','1825520817@qq.com','4609785@qq.com','2670628890@qq.com','215774301@qq.com','958553857@qq.com','849153603@qq.com','13383776@qq.com','752064756@qq.com','3449579530@qq.com','412060264@qq.com','522917859@qq.com','1036738968@qq.com','123321123@qq.com','593122689@qq.com','13036779152@163.com','1017014363@qq.com')  ");
			sb.append(" 	) a  ");
			String str = sb.toString();
			System.out.println("生成了一条select语句：" + str);
			result.add(str);
		}
		return result;
	}
	
	/**
	 * 得到insert语句
	 * @return
	 */
	public List<String> generalInsertSqlList(){
		int batch = 5000;
		System.out.println("开始生成select语句");
		List<String> selectSqlList = generalSelectSqlList();
		List<String> resultList = new ArrayList<>();
		System.out.println("开始执行select语句");
		for (int j = 0; j < selectSqlList.size(); j++) {
			List<String> eachSelectResult = new ArrayList<>(); 
			String selectSql = selectSqlList.get(j);
			// 分批select
			for (int i = 0; i < 1000 ; i++) {
				String selectSqlLimit = selectSql + " limit " + i * batch + "," + batch; 
				List<Object[]> cardList =SshMysqlYiaiwang.queryBySQL(selectSqlLimit);
				for (Object[] objects : cardList) {
					// 替换id
					String str = (String)objects[0];
					int idx = str.indexOf("VALUES('");
					if (idx >= 0) {
						String id2 = MyUtils.uuid();
						str = str.substring(0, idx + 8) + id2 + "'," + str.substring(idx + 43);
					}
					eachSelectResult.add(str);			
					resultList.add(str);
				}
				System.out.println("执行完第"+(j+1)+"条语句（limit " + i * batch + "," + batch + "），生成了" + cardList.size() + "条insert语句");
				if (cardList.size() < batch) {
					break;
				}
			}
			// 输出至文件
			String path = "E:/myJava/yiaiSql/20170726/insertCardNumber_category"+(j+1)+".sql";
			System.out.println("开始输出至文件：" + path);
			MyUtils.outputList(eachSelectResult, path);
		}
		System.out.println("共生成了" + resultList.size() + "条insert语句");
		String path = "E:/myJava/yiaiSql/20170726/insertCardNumber_all.sql";
		MyUtils.outputList(resultList, path);
		return resultList;
	}
	
	/**
	 * 增加分销商的sql
	 * @return
	 */
	public List<String> generalUpdateSql(){
		List<String> result = new ArrayList<>();
		for (List<String> cardArea : cardAreaList) {
			StringBuffer sqlSb = new StringBuffer();
			sqlSb.append(" update pe_card_number n set ");
			sqlSb.append(" n.FK_MANAGER_ID='" + cardArea.get(2) + "', n.VALIDDATE='" + cardArea.get(3) + "' ");
			if (cardArea.get(3).equals("2018-12-31")) { // 重庆
				sqlSb.append(",n.CMESCORE1=null,n.CMESCORE2=null,n.common_score='20',n.CMESCORE1_USABLE=null,n.CMESCORE2_USABLE=null,n.common_score_usable='20',n.`NAME`='通用学分卡20分' ");
			}
			sqlSb.append(" where n.`CODE` >= '" + cardArea.get(0) + "' and n.`CODE` <= '" + cardArea.get(1) + "' ");
			sqlSb.append(" and FK_SITE_ID='ff80808155da5b850155dddbec9404c9';");
			result.add(sqlSb.toString());
		}
		return result;
	}
	
	public static void main(String[] args) {
		List<String> executeList = new ArrayList<>();
		String outPath = "E:/myJava/yiaiSql/20170726/insertCardNumber_with_update.sql";
		String updateSql = "UPDATE pe_card_number n,  pe_trainee pt SET n.FK_SSO_USER_ID = pt.ID,  n.FK_TRAINEE_ID = pt.ID,  n.FLAG_ISUSED='1902b34c5f7f11e69b44848f69e05bf0' WHERE 	n.FK_SSO_USER_ID = pt.LOGIN_ID AND n.FK_SSO_USER_ID != '' AND n.FK_SITE_ID = 'ff80808155da5b850155dddbec9404c9' ;";
		YiaiAddCardNumberGeneralSql yiaiAddCardNumberGeneralSql = new YiaiAddCardNumberGeneralSql();
		yiaiAddCardNumberGeneralSql.init();
		List<String> insertSqlList = yiaiAddCardNumberGeneralSql.generalInsertSqlList();
		executeList.addAll(insertSqlList);
		System.out.println("追加学员对卡的绑定");
		executeList.add(updateSql); // 追加学员对卡的绑定
		
		// 绑定分销商
		System.out.println("追加分销商对卡的绑定");
		List<String> updateFXSSql = yiaiAddCardNumberGeneralSql.generalUpdateSql();
		executeList.addAll(updateFXSSql);
		
		MyUtils.outputList(executeList, outPath);
		System.exit(0);
	}
}
