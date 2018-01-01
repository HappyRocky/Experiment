package whaty.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;


public class YiaiAddCreditApplyGeneralSqlThread extends Thread {
	private int start;
	private int end;
	private List<List<String>> queryApplyInfo;
	private List<String> applyIdList;

	
	public YiaiAddCreditApplyGeneralSqlThread(int start, int end, List<List<String>> queryApplyInfo) {
		this.start = start;
		this.end = end;
		this.queryApplyInfo = queryApplyInfo;

	}
	public void run(){
		System.out.println("线程开启：" + start + "~" + end);
		String outPath = "E:/myJava/yiaiSql/20170726/insertApply_" + start + "_" + end + ".sql";
//		File file = new File(outPath);
//		if (file.exists()) {
//			System.out.println("申请记录已经生成sql：" + outPath);
//			return;
//		}
		List<String> result = new ArrayList<String>();
		int success = 0;
		int fail = 0;
		applyIdList = new ArrayList<String>();
		for (int i = start; i < end; i++) {
			List<String> applyInfo = queryApplyInfo.get(i);
//			System.out.println(i + ".开始申请：" + applyInfo.get(0) + " " + applyInfo.get(1));
			List<String> sigleAppleSqlList = this.generalSingleApplySql(applyInfo);
			System.out.println("线程" + start + "~" + end + "：已完成了" + (i - start + 1) + "个");
			if (sigleAppleSqlList != null) {
				success++;
				result.addAll(sigleAppleSqlList);
			} else {
				fail++;
			}
			if (i % 500 == 0) {
				System.out.println("线程" + start + "~" + end + "：已完成了" + (i - start) + "个");
			}
		}
		System.out.println(start + "~" + end + "：总共成功申请" + success + "个，失败" + fail + "个)");
		MyUtils.outputList(result, outPath);
		
		// 输出申请的id，供小明使用
		String creditIdPath = "E:/myJava/yiaiSql/20170726/applyId_" + start + "_" + end + ".txt";
		MyUtils.outputList(this.applyIdList, creditIdPath);
		
		// 存放重复申请的sql
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		String fileName = df.format(calendar.getTime());
		String dupPath = YiaiAddCreditApplyGeneralSql.isDuplicateApplyPath + "." + fileName + ".txt";
		MyUtils.outputList(YiaiAddCreditApplyGeneralSql.isDuplicateApplyList, dupPath);
	}
	
	/**
	 * 生成一次申请的sql
	 * @param applyInfo
	 * @return
	 */
	public List<String> generalSingleApplySql(List<String> applyInfo){
		List<String> allSqlList = new ArrayList<String>(); // 存放扣除学分、插入申请记录、插入卡片扣除学分详细记录的sql
		
		String loginId = applyInfo.get(0);
		String packageName = applyInfo.get(1);
		String type = "XF" + applyInfo.get(2);
		String credit = applyInfo.get(3);
		String applyDate = applyInfo.get(4);
		String certNo = applyInfo.get(5);
		String year = StringUtils.defaultString(applyInfo.get(6));
		
		String log = " loginId=" + loginId + ",pacName=" + packageName;
		
		// 得到学生Id和班级Id
		String sql = "select pt.id,pt.province from pe_trainee pt where pt.LOGIN_ID='"+loginId+"' and pt.fk_site_id='"+YiaiAddCreditApplyGeneralSql.siteId+"'";
		List<Object[]> objects = SshMysqlWebtrn.queryBySQL(sql);
		if (CollectionUtils.isEmpty(objects)) {
			System.out.println("单次申请失败：不存在学员:" + loginId);
			return null;
		}
		String traineeId = (String)(objects.get(0))[0];
		String province = StringUtils.defaultString((String)(objects.get(0))[1]);
		String classId = "";
		if (year.equals("2017")) {
			classId = province.equals("四川省") ? "ff8080815a27e1a4015a353a4767005f" : "ff8080815a27e1a4015a353967cf0037";
		} else if (year.equals("2016")) {
			classId = province.equals("四川省") ? "ae8a903ee1ca47929a3d1011d6cb31d0" : "bc6ed0cb76744b18ba90c6d09662275f";
		} else if (year.equals("2015")) {
			classId = province.equals("四川省") ? "80b93beec1914de7831e5f71b9da4d1c" : "71a546591765432e9755866eb49431d6";
		} else {
			classId = province.equals("四川省") ? "dfdfc4026ffe43199d52929b95643989" : "de76d90235814c419699ef475c8b3346";
		}
		
		
		// 获取课程包信息
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT                                                    ");
		sb.append(" 	pcc.id AS pccId,                                      ");
		sb.append(" 	pcp.`code` AS pcpCode,                                 ");
		sb.append(" 	pcp.id AS pcpId                                 ");
		sb.append(" FROM                                                      ");
		sb.append(" 	pr_class_coursepackage pcc                            ");
		sb.append(" JOIN pe_course_package pcp ON pcp.id=pcc.fk_coursepackage ");
		sb.append(" WHERE                                                     ");
		sb.append(" 	pcc.fk_site_id = '" + YiaiAddCreditApplyGeneralSql.siteId + "'   ");
		sb.append(" AND pcc.fk_class = '" + classId + "'     ");
		sb.append(" AND (pcp.`name`='" + packageName + "'               ");
		sb.append(" or pcp.`name`='【包含无效课程】" + packageName + "'               ");
		sb.append(" or pcp.`name`='" + packageName + "【无课程】')               ");
		sb.append(" and pcp.fk_credit_id='"+ (type.equals("XF1") ? "ff80808156d3dcd70156d5593e4f0012" : "ff80808156d3dcd70156d55976680013") +"'              ");
		sb.append(" and pcp.year='" + year + "' ");
		
		
		objects = SshMysqlWebtrn.queryBySQL(sb.toString());
		if (CollectionUtils.isEmpty(objects)) {
			System.out.println("单次申请失败：获取课程包:" + sb.toString());
			return null;
		}
		String courseId = (String)(objects.get(0))[0];
		String packageCode = (String)(objects.get(0))[1];
		String packageId = (String)(objects.get(0))[2];
		
		// 判断是否已经重复申请
		sql = " select pac.id,UNIX_TIMESTAMP(NOW())-UNIX_TIMESTAMP(pac.create_date) from pe_credit_apply pac ,enum_const ec "
				+ "WHERE pac.flag_isvalid = ec.ID  and pac.fk_site_id='" + YiaiAddCreditApplyGeneralSql.siteId + "' AND ec.`CODE`='1' "
				+ "and ((pac.fk_trainee_id='" + traineeId	+ "' and fk_coursePac_id='" + courseId + "' ) "
				+ "or pac.create_date='"+applyDate+"') ";
		
		if (YiaiAddCreditApplyGeneralSql.isDuplicateApplyList.contains(sql)) {
			System.out.println("单次申请失败：重复申请:" + sql);
			return null;
		}
		
		List<Object[]> listCheckApply = SshMysqlWebtrn.queryBySQL(sql);
		// 验证学员是否重复申请
		if (CollectionUtils.isNotEmpty(listCheckApply)) {
			YiaiAddCreditApplyGeneralSql.isDuplicateApplyList.add(sql);
			System.out.println("单次申请失败：重复申请:" + sql);
			return null;
		}
		
//		System.out.println("生成学员和课程包的对应关系");
		sb = new StringBuffer();
		sb.append("INSERT INTO `pr_trainee_class_course_package` ( ");
		sb.append(" 	`id`,                                       ");
		sb.append(" 	`fk_trainee_id`,                            ");
		sb.append(" 	`fk_class_course_package_id`,               ");
		sb.append(" 	`fk_course_package_id`,                     ");
		sb.append(" 	`study_percent`,                     ");
		sb.append(" 	`complete_date`,                     ");
		sb.append(" 	`create_date`,                              ");
		sb.append(" 	`modify_date`,                              ");
		sb.append(" 	`flag_apply_status`,                       ");
		sb.append(" 	`fk_site_id`                                ");
		sb.append(" ) VALUES (                                      ");
		sb.append(" 		'" + MyUtils.uuid() + "', ");
		sb.append(" 		'" + traineeId + "',     ");
		sb.append(" 		'" + courseId + "',     ");
		sb.append(" 		'" + packageId + "',     ");
		sb.append(" 		100,     ");
		sb.append(" 		'" + applyDate + "',     ");
		sb.append(" 		NOW(),   ");
		sb.append(" 		NOW(),  ");
		sb.append(" 		'" + YiaiAddCreditApplyGeneralSql.flagIsValid + "',  ");
		sb.append(" 		'" + YiaiAddCreditApplyGeneralSql.siteId + "'      ");
		sb.append(" 	) ON DUPLICATE KEY UPDATE study_percent=values(study_percent),complete_date=values(complete_date),modify_date=values(modify_date),flag_apply_status=values(flag_apply_status);  ");
		allSqlList.add(sb.toString());


		
		String id = UUID.randomUUID().toString().replace("-", ""); // 申请id
		// 生成证书
		if (StringUtils.isNotBlank(certNo)) {
			Map<String, String> conditionsMap = new HashMap<String, String>();
			conditionsMap.put("creditType", type);
			conditionsMap.put("siteId", YiaiAddCreditApplyGeneralSql.siteId);
			conditionsMap.put("classId", classId);
			conditionsMap.put("stuId", traineeId);
			conditionsMap.put("packageCode", packageCode);
			conditionsMap.put("packageName", packageName);
			conditionsMap.put("credit", credit);
			conditionsMap.put("applyId", id); 
			conditionsMap.put("province", province); 
			conditionsMap.put("code", certNo); // 证书编号 
			conditionsMap.put("applyDate", applyDate);
			String insertCertSql = createCertificateNo(conditionsMap);
			allSqlList.add(insertCertSql);
//			System.out.println("生成证书完毕");
		}

		// 开始申请学分
		String applySql = "INSERT INTO `pe_credit_apply` (`id`, `fk_course_id`, `fk_class_id`, `type`, `credit`, `create_date`, `flag_isvalid`, `fk_trainee_id`, `fk_check_user_id`, `check_date`, `fk_site_id`,`fk_coursePac_id`) "
				+ "	VALUES ('"
				+ id
				+ "', null, '"
				+ classId + "', '"
				+ type + "', '"
				+ credit + "', '"+applyDate+"', '"
				+ YiaiAddCreditApplyGeneralSql.flagIsValid + "', '"
				+ traineeId + "', NULL, NULL, '"
				+ YiaiAddCreditApplyGeneralSql.siteId
				+ "','" + courseId + "');";
		allSqlList.add(applySql);
		this.applyIdList.add(id);
		// 卡号表中扣除可用学分
		Map<String, Object> deductMap = this.deductCredit(Integer.valueOf(credit), type, traineeId, YiaiAddCreditApplyGeneralSql.siteId, id);
		if ("1".equals(deductMap.get("code"))) { // 扣除成功
			allSqlList.addAll((List<String>) deductMap.get("sqlList"));
		} else {
			System.out.println("单次申请成功，但扣除学分失败:" + deductMap.get("info") + log);
		}
		
		for (String str : allSqlList) {
			SshMysqlWebtrn.executeBySQL(str);
		}
		
//		SshMysqlWebtrn.executeBatchBySQL(allSqlList); // 执行
		System.out.println("单次申请完毕:" + loginId + " " + packageName);
		return allSqlList;
	}
	
	/**
	 * 生成证书编号入库
	 */
	public String createCertificateNo(Map conditions) {
		String result = null;
		String creditType = String.valueOf(conditions.get("creditType"));
		String siteId = String.valueOf(conditions.get("siteId"));
		String classId = String.valueOf(conditions.get("classId"));
		String stuId = String.valueOf(conditions.get("stuId"));
		String packageCode = StringUtils.defaultString((String)(conditions.get("packageCode")));
		String packageName = String.valueOf(conditions.get("packageName"));
		String credit = String.valueOf(conditions.get("credit"));
		String applyId = (String) conditions.get("applyId");
		String province = StringUtils.defaultString((String) conditions.get("province"));
		String code = (String) conditions.get("code");
		String applyDate = (String) conditions.get("applyDate");
		String modelId = getCertificateModelId(province, creditType);
		if (StringUtils.isNotBlank(modelId)) {
			String id = UUID.randomUUID().toString().replace("-", "");
			String curFlagCMEType = "";
			int learnTime = 0;
			if (creditType.equals("XF1")) {
				learnTime = Integer.parseInt(credit) * 3; //国家一类 学时 = 学分 * 3
				curFlagCMEType = YiaiAddCreditApplyGeneralSql.flagCMEType1;
			} else {
				learnTime = Integer.parseInt(credit) * 6; //远程二类 学时 = 学分 * 6
				curFlagCMEType = YiaiAddCreditApplyGeneralSql.flagCMEType2;
			}
			result = "INSERT INTO pr_student_certificate ( id, certificateNo, applydate, createdate, fk_student_id, `key`, fk_certificate_model_id, fk_site_id, flag_apply_state, 	projectno, 	projectname, flag_cme_type, learntime, learnscore, Fk_certificate_id ) "
					+ "VALUES ( '" + id + "', '" + code + "', '"+applyDate+"', 	'"+applyDate+"', '" + stuId + "', '" + classId + "', '" + modelId + "', '" + siteId + "', '" + YiaiAddCreditApplyGeneralSql.flagIsValid + "', '" + packageCode + "', '" + packageName + "', '" + curFlagCMEType + "', '" + learnTime + "', '" + credit + "', '" + applyId + "' );";
		} 
		return result;
	}
	
	/**
	 * 根据证书类型获取证书模版（仅针对CME证书） type XF1(国家一类) XF2(远程二类)
	 */
	public String getCertificateModelId(String province, String type) {
		//判断是不是四川的班级  如果不是则用全国的模版打印  如果是则用四川自己的模版 
		String sql = " select id from pe_certificate_model  where fk_pesite='" + YiaiAddCreditApplyGeneralSql.siteId + "'";
		if(province.contains("四川")){
			if (type.equals("XF1")) {
				sql += " and  code='CMESCType1'  ";
			} else {
				sql += " and  code='CMESCType2'  ";
			}
		}else{
			if (type.equals("XF1")) {
				sql += " and  code='CMEType1'  ";
			} else {
				sql += " and  code='CMEType2'  ";
			}
		}
		List<Object[]> listCheckApply = SshMysqlWebtrn.queryBySQL(sql);
		if (CollectionUtils.isEmpty(listCheckApply)) {
			return null;
		}
		return (String)listCheckApply.get(0)[0];
	}
	
	/**
	 * 扣除学分，优先扣除即将过期的学分卡的学分
	 * @param credit 要扣除的学分
	 * @param creditType 学分类型：XF1、XF2
	 * @param traineeId 学员Id
	 * @param siteId 站点Id
	 * @param creditApplyid 学分申请记录id 
	 * @return code=1为成功，其他为失败，list为回退sql
	 */
	private Map<String, Object> deductCredit(int credit, String creditType, String traineeId, String siteId,
			String creditApplyid) {
		Map<String, Object> map = new HashMap<String, Object>();
		StringBuilder cardSqlSb = new StringBuilder();
		cardSqlSb.append(" SELECT ");
		cardSqlSb.append(" 	pcn.ID, "); // 0：卡号id
		cardSqlSb.append(" 	pcn.CMESCORE1_USABLE, "); // 1：可用一类学分
		cardSqlSb.append(" 	pcn.CMESCORE2_USABLE, "); // 2：可用二类学分
		cardSqlSb.append(" 	pcn.common_score_usable "); // 3：可用通用学分
		cardSqlSb.append(" FROM ");
		cardSqlSb.append(" 	pe_card_number pcn ");
		cardSqlSb.append(" JOIN enum_const enumIsValid ON enumIsValid.ID = pcn.FLAG_ISVALID "); // 卡片有效
		cardSqlSb.append(" AND enumIsValid.`CODE` = '1' ");
		cardSqlSb.append(" AND enumIsValid.NAMESPACE = 'FlagIsvalid' ");
		cardSqlSb.append(" JOIN enum_const enumIsActivation ON enumIsActivation.ID = pcn.FLAG_ISACTIVATION "); // 卡片已激活
		cardSqlSb.append(" AND enumIsActivation.`CODE` = '0' ");
		cardSqlSb.append(" AND enumIsActivation.NAMESPACE = 'FlagIsActivation' ");
		cardSqlSb.append(" JOIN enum_const enumIsUsed ON enumIsUsed.ID = pcn.FLAG_ISUSED "); // 卡片已绑定
		cardSqlSb.append(" AND enumIsUsed.`CODE` = '0' ");
		cardSqlSb.append(" AND enumIsUsed.NAMESPACE = 'FlagIsused' ");
		cardSqlSb.append(" JOIN pe_cardtype pc ON pc.ID=pcn.FK_CARD_TYPE_ID AND pc.`CODE`='CREDIT' "); // 学分卡
		cardSqlSb.append(" WHERE ");
		cardSqlSb.append(" 	pcn.VALIDDATE >= CURDATE() "); // 卡片没有过期
		cardSqlSb.append(" AND pcn.FK_SSO_USER_ID = '" + traineeId + "' ");
		cardSqlSb.append(" AND pcn.FK_SITE_ID = '" + siteId + "' ");
		cardSqlSb.append(" ORDER BY pcn.VALIDDATE "); // 按截止日期排序
		try {
			List<Object[]> cardList = SshMysqlWebtrn.queryBySQL(cardSqlSb.toString());
			if (CollectionUtils.isEmpty(cardList)) {
				map.put("code", "-1");
				map.put("info", "没有可用学分");
				return map;
			} else {
				int creditCount = 0;
				List<String> cardNumberIdList = new ArrayList<String>();
				List<String> isCommonScoreList = new ArrayList<String>();
				List<String> resultSqlList = new ArrayList<String>();
				boolean hasSuccess = false; // 是否可以成功扣除学分
				// 累加可用学分，直至达到需要扣除的学分
				for (int i = 0; i < cardList.size(); i++) {
					Object[] objCard = cardList.get(i);
					String cardId = objCard[0].toString(); // 卡片id
					int credit1 = Integer.valueOf(objCard[1] == null ? "0" : StringUtils.defaultIfEmpty(
							objCard[1].toString(), "0")); // 一类学分值
					int credit2 = Integer.valueOf(objCard[2] == null ? "0" : StringUtils.defaultIfEmpty(
							objCard[2].toString(), "0")); // 二类学分值
					int commonCredit = Integer.valueOf(objCard[3] == null ? "0" : StringUtils.defaultIfEmpty(
							objCard[3].toString(), "0")); // 通用学分值
					int curCredit = ("XF1".equals(creditType) ? credit1 : credit2) + commonCredit; // 适合的学分值，一二类与通用互斥，只有一个是大于零的，直接相加没问题
					if (curCredit == 0) { // 卡中没有可用学分
						continue;
					}
					// 存方卡片id，便于更新学分字段
					cardNumberIdList.add(cardId);
					isCommonScoreList.add(commonCredit > 0 ? "1" : "0"); // 标记cardId是否是通用学分
					// 累加学分
					creditCount += curCredit;
					if (creditCount >= credit) { // 达到需要扣除的学分值
						List<String> sqlList = updateCardNumberUsableCredit(cardNumberIdList, isCommonScoreList,
								creditCount - credit, creditType); // 更新字段
						if (CollectionUtils.isNotEmpty(sqlList)) {
							resultSqlList.addAll(sqlList);
							hasSuccess = true;
							map.put("code", "1");
							map.put("info", "生成sql成功");
						} else {
							map.put("code", "-3");
							map.put("info", "生成sql失败");
							return map;
						}
					}
					// 插入卡片扣除学分详细记录的sql
					int deductedCredit = curCredit - (creditCount - credit); // 最后一个卡片，原有的-剩下的=扣除的
					if (commonCredit > 0) { // 通用学分
						credit1 = 0;
						credit2 = 0;
						if (hasSuccess) { // 最后一张，不是全部扣除；否则全部扣除
							commonCredit = deductedCredit;
						}
					} else { // 非通用学分
						commonCredit = 0;
						if ("XF1".equals(creditType)) {
							if (hasSuccess) {
								credit1 = deductedCredit;
							}
							credit2 = 0; // 不扣除二类学分
						} else {
							if (hasSuccess) {
								credit2 = deductedCredit;
							}
							credit1 = 0; // 不扣除一类学分
						}
					}
					String insertId = UUID.randomUUID().toString();
					String insertSql = " INSERT INTO `pe_credit_apply_card` (`id`, `fk_credit_apply_id`, `fk_card_number_id`, `deducted_score1`, `deducted_score2`, `deducted_common_score`, `fk_site_id`) "
							+ "VALUES ('"
							+ insertId + "', '"
							+ creditApplyid + "', '"
							+ cardId + "', '"
							+ credit1 + "', '" 
							+ credit2 + "', '" 
							+ commonCredit + "', '" 
							+ siteId + "'); ";
					resultSqlList.add(insertSql);
					if (hasSuccess) {
						map.put("sqlList", resultSqlList);
						return map;
					}
				}
				// 可用学分不足
				map.put("code", "-2");
				map.put("info", "可用学分不足");
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("code", "-3");
			map.put("info", "生成sql失败");
			return map;
		}
	}
	
	/**
	 * 将list中的除最后一个的可用学分设置为0，最后一个可用学分设为restCredit
	 * @param cardNumberIdList 卡片id队列
	 * @param isCommonScoreList 卡片id是否是通用学分的队列
	 * @param restCredit 剩余的可用学分
	 * @param creditType 学分类型：XF1、XF2
	 * @return 要执行的sql队列
	 */
	private List<String> updateCardNumberUsableCredit(List<String> cardNumberIdList, List<String> isCommonScoreList, int restCredit, String creditType){
		List<String> sqlList = new ArrayList<String>(); // 要执行的sql队列
		if (CollectionUtils.isEmpty(cardNumberIdList) || cardNumberIdList.size() != isCommonScoreList.size()) {
			return sqlList;
		}
		int size = cardNumberIdList.size();
		String scoreCol = "XF1".equals(creditType) ? "CMESCORE1_USABLE" : "CMESCORE2_USABLE"; // 要更新的字段名
		
		// 前面 size-1个的可用学分设置为0
		if (size > 1) { 
			// 拼接更新可用学分字段的sql
			StringBuilder cardIdSb = new StringBuilder();
			cardIdSb.append(" UPDATE pe_card_number SET " + scoreCol + "=0,common_score_usable=0 WHERE ID IN ( ");
			for (int i = 0; i < size - 1; i++) { // 拼接id
				if (i > 0) {
					cardIdSb.append(",");
				}
				cardIdSb.append("'" + cardNumberIdList.get(i) + "'");
			}
			cardIdSb.append(") ");
			sqlList.add(cardIdSb.toString());
		}
		
		// 更新最后id的学分
		String isCommonScore = isCommonScoreList.get(size - 1);
		scoreCol = isCommonScore.equals("1") ? "common_score_usable" : scoreCol; // 判断更新哪个字段
		StringBuilder cardSb = new StringBuilder();
		cardSb.append(" UPDATE pe_card_number SET " + scoreCol + "=" + restCredit + " WHERE ID='" + cardNumberIdList.get(size - 1) + "';");
		sqlList.add(cardSb.toString());
		
		return sqlList;
	}
	
	/**
	 * 批量扣除学分，优先扣除即将过期的学分卡的学分
	 * @param credit 要扣除的学分
	 * @param creditType 学分类型：XF1、XF2
	 * @param traineeId 学员Id
	 * @param siteId 站点Id
	 * @param creditApplyidList 学分申请记录id的list，必须属于同一个学员 
	 * @return code=1为成功，其他为失败，list为回退sql
	 */
	private Map<String, Object> deductCreditByBatch(String traineeId, List<Integer> creditList, List<String> creditTypeList, List<String> creditApplyidList, String siteId) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (creditList.size() != creditTypeList.size() || creditList.size() != creditApplyidList.size()) {
			map.put("code", "-4");
			map.put("info", "参数错误，list长度不一致");
			return map;
		}
		
		StringBuilder cardSqlSb = new StringBuilder();
		cardSqlSb.append(" SELECT ");
		cardSqlSb.append(" 	pcn.ID, "); // 0：卡号id
		cardSqlSb.append(" 	pcn.CMESCORE1_USABLE, "); // 1：可用一类学分
		cardSqlSb.append(" 	pcn.CMESCORE2_USABLE, "); // 2：可用二类学分
		cardSqlSb.append(" 	pcn.common_score_usable "); // 3：可用通用学分
		cardSqlSb.append(" FROM ");
		cardSqlSb.append(" 	pe_card_number pcn ");
		cardSqlSb.append(" JOIN enum_const enumIsValid ON enumIsValid.ID = pcn.FLAG_ISVALID "); // 卡片有效
		cardSqlSb.append(" AND enumIsValid.`CODE` = '1' ");
		cardSqlSb.append(" AND enumIsValid.NAMESPACE = 'FlagIsvalid' ");
		cardSqlSb.append(" JOIN enum_const enumIsActivation ON enumIsActivation.ID = pcn.FLAG_ISACTIVATION "); // 卡片已激活
		cardSqlSb.append(" AND enumIsActivation.`CODE` = '0' ");
		cardSqlSb.append(" AND enumIsActivation.NAMESPACE = 'FlagIsActivation' ");
		cardSqlSb.append(" JOIN enum_const enumIsUsed ON enumIsUsed.ID = pcn.FLAG_ISUSED "); // 卡片已绑定
		cardSqlSb.append(" AND enumIsUsed.`CODE` = '0' ");
		cardSqlSb.append(" AND enumIsUsed.NAMESPACE = 'FlagIsused' ");
		cardSqlSb.append(" JOIN pe_cardtype pc ON pc.ID=pcn.FK_CARD_TYPE_ID AND pc.`CODE`='CREDIT' "); // 学分卡
		cardSqlSb.append(" WHERE ");
		cardSqlSb.append(" 	pcn.VALIDDATE >= CURDATE() "); // 卡片没有过期
		cardSqlSb.append(" AND pcn.FK_SSO_USER_ID = '" + traineeId + "' ");
		cardSqlSb.append(" AND pcn.FK_SITE_ID = '" + siteId + "' ");
//		cardSqlSb.append(" ORDER BY pcn.VALIDDATE "); // 按截止日期排序
		try {
			List<Object[]> cardList = SshMysqlWebtrn.queryBySQL(cardSqlSb.toString());
			if (CollectionUtils.isEmpty(cardList)) {
				map.put("code", "-1");
				map.put("info", "没有可用学分");
				return map;
			} else {
				for (int applyIdx = 0; applyIdx < creditList.size(); applyIdx++) {
					int credit = creditList.get(applyIdx);
					String creditType = creditTypeList.get(applyIdx);
					String creditApplyid = creditApplyidList.get(applyIdx);
					
					int creditCount = 0;
					List<String> cardNumberIdList = new ArrayList<String>();
					List<String> isCommonScoreList = new ArrayList<String>();
					List<String> resultSqlList = new ArrayList<String>();
					boolean hasSuccess = false; // 是否可以成功扣除学分
					// 累加可用学分，直至达到需要扣除的学分
					for (int i = 0; i < cardList.size(); i++) {
						Object[] objCard = cardList.get(i);
						String cardId = objCard[0].toString(); // 卡片id
						int credit1 = Integer.valueOf(objCard[1] == null ? "0" : StringUtils.defaultIfEmpty(
								objCard[1].toString(), "0")); // 一类学分值
						int credit2 = Integer.valueOf(objCard[2] == null ? "0" : StringUtils.defaultIfEmpty(
								objCard[2].toString(), "0")); // 二类学分值
						int commonCredit = Integer.valueOf(objCard[3] == null ? "0" : StringUtils.defaultIfEmpty(
								objCard[3].toString(), "0")); // 通用学分值
						int curCredit = ("XF1".equals(creditType) ? credit1 : credit2) + commonCredit; // 适合的学分值，一二类与通用互斥，只有一个是大于零的，直接相加没问题
						if (curCredit == 0) { // 卡中没有可用学分
							continue;
						}
						// 存方卡片id，便于更新学分字段
						cardNumberIdList.add(cardId);
						isCommonScoreList.add(commonCredit > 0 ? "1" : "0"); // 标记cardId是否是通用学分
						// 累加学分
						creditCount += curCredit;
						if (creditCount >= credit) { // 达到需要扣除的学分值
							List<String> sqlList = updateCardNumberUsableCredit(cardNumberIdList, isCommonScoreList,
									creditCount - credit, creditType); // 更新字段
							if (CollectionUtils.isNotEmpty(sqlList)) {
								resultSqlList.addAll(sqlList);
								hasSuccess = true;
								map.put("code", "1");
								map.put("info", "生成sql成功");
							} else {
								map.put("code", "-3");
								map.put("info", "生成sql失败");
								return map;
							}
						}
						// 插入卡片扣除学分详细记录的sql
						int deductedCredit = curCredit - (creditCount - credit); // 最后一个卡片，原有的-剩下的=扣除的
						if (commonCredit > 0) { // 通用学分
							credit1 = 0;
							credit2 = 0;
							if (hasSuccess) { // 最后一张，不是全部扣除；否则全部扣除
								commonCredit = deductedCredit;
							}
						} else { // 非通用学分
							commonCredit = 0;
							if ("XF1".equals(creditType)) {
								if (hasSuccess) {
									credit1 = deductedCredit;
								}
								credit2 = 0; // 不扣除二类学分
							} else {
								if (hasSuccess) {
									credit2 = deductedCredit;
								}
								credit1 = 0; // 不扣除一类学分
							}
						}
						String insertId = UUID.randomUUID().toString();
						String insertSql = " INSERT INTO `pe_credit_apply_card` (`id`, `fk_credit_apply_id`, `fk_card_number_id`, `deducted_score1`, `deducted_score2`, `deducted_common_score`, `fk_site_id`) "
								+ "VALUES ('"
								+ insertId + "', '"
								+ creditApplyid + "', '"
								+ cardId + "', '"
								+ credit1 + "', '" 
								+ credit2 + "', '" 
								+ commonCredit + "', '" 
								+ siteId + "'); ";
						resultSqlList.add(insertSql);
						if (hasSuccess) {
							map.put("sqlList", resultSqlList);
							return map;
						}
					}
					// 可用学分不足
					map.put("code", "-2");
					map.put("info", "可用学分不足");
					return map;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			map.put("code", "-3");
			map.put("info", "生成sql失败");
			return map;
		}
		return map;
	}
}
