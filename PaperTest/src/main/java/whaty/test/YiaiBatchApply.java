/*
 * 文件名：YiaiBatchApply.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月6日下午8:14:35
 */
package whaty.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 已知学分申请记录，重新扣除学分
 * @author Administrator
 *
 */
public class YiaiBatchApply {
	
	public static String siteId = "ff80808155da5b850155dddbec9404c9";
	public static String gap = "__";
	
	public List<List<String>> getBatchApplyInfo(){
		String path = "E:/myJava/yiaiSql/20170703/yiaiBatchApply.txt";
		String gap = "__";
		
		// 得到申请记录
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT                                                   ");
		sb.append(" 	pca.id,                                              ");
		sb.append(" 	pca.type,                                            ");
		sb.append(" 	pca.credit,                                          ");
		sb.append(" 	pca.fk_trainee_id                                    ");
		sb.append(" FROM                                                     ");
		sb.append(" 	pe_credit_apply pca                                  ");
		sb.append(" JOIN pe_trainee pt ON pt.ID=pca.fk_trainee_id ");
		sb.append(" WHERE                                                    ");
		sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'  ");
		sb.append(" AND pca.flag_isvalid = '40288a962e9d9ac5012e9dd6b0aa0004'");
//		sb.append(" and pt.id in ('271e60e25d0240cdbf24a4572e90ab94','0d95b1c1d3c0453495da5eed1c0af518','ff8080815c7cbb7d015c80d59201000f','a9148161953f41a7af91064038b4d323','08c568bee79d425b94c69b2dc99ef936','b32cc76b5f624f6686ecdf91206102d2','4ea5e5b216984d038d6fe826da9263ba','0dba873cb62a412aa2d8b120cdfc783f','0b9eff3211434e4884fe49b24b79d5cb','bbd1ca33504f44eb8355971036d1d8d7','9e8948aca13c4f3a8eac02db2d1ce14e','8863a88cc8f349b88dbd4eb1ad44a28e','ff8080815c7cbb7d015c80cf3e9a000a','3ab55e4d65fd48cda874bad06c0b565e','8e552342782b454184b72e9f87f6611b','b84eafdac0874a9da4b09c9e1bbe9aac','5c0a6cf3c8a34c8aa68f941527004f29','29d7d06d76f945f8888f797686d3eb94','31ba9ae6316743b8af0f7bf3191dc3d9','52210b7ee7bb465ea43b8201d32a8c55','a24b837e671142418a3406abcfc1bf65','d197d5430cce40a79cc55dbce24e30fc','09031aba58ec4e40b4aec4dfb9c17257','db0a0e30e9c042efb19f049b97b7ff17','0fc2d483e52741c4bb6b1b47363dc340','ae96ebfcb3604375b104d9939dc0c422','46b47db914fe418080ed6582da0a1823','527b302ef33b4e1497270e7b3346ed41','a2952a1a97ba4ea2a0fcd5b828370db5','561d152ce00a4941872c70bb01a91ee9','e8265ae333864bca9ed2db90ca8cc58a','30b8ff0ec59f409886d175445d24303d','66c735d39a89497e812d14367b2d5881','fddd8c224d724d1cb0ad8c4a90730a28','e480c63bf93043778e28b9d5cfc5b12b','84122e4d7fc54bacba23dea132bc74e5','67ac5f36aab847b4b67aee140c8ab51c','782f5eabaa3f4aecbdc3a6de7dac06c3','ff8080815c7cbb7d015c80cdf5870008','2e57eeafc1b448e99824c3ceed9bbd1a','e1fea55a1f5c4d4e8a8a1d404a75f8e6','5695c503ba1f4eaa8f0305662045c7eb','e92953880a12448a932fbd00a4fc1d58','350ffe420c984806869f9dbbba1b0f46','93dcc3c2781947a382ba146dea090872','bfaf43929acf4398bb8f767b28e5708b','7cb41b2dc86a4ff7a14b8f26efc212b7','c5389f5e4f174264abccb21cc0babca9','97bd01fbcc18465cbb040d16d7ac34f0','cf7f9a0c0d444751a21275121cb2adaf','0062b1aaad574463b1e5986844fed520','fd36b363e341433cb997e32d1f777945','336cb183b4064677a79622a22caf55b2','35ff24e923774fb793b62c12a2dfedb1','ff8080815c7cbb7d015c80d359e6000d','6c472957d0f84787bd3a161970a3fc10','2578496e374b44e7b1874ac594ab6465','0dfc5c96a374450e8007e18435af5303','ff8080815c7cbb7d015c80ccba320006')");
//		sb.append(" and pca.create_date > '2017-02-05'");
		sb.append(" and pca.create_date > '2017-06-14 12:00:00'");
//		List<String> newLoginList = new ArrayList<>();
//		newLoginList.add("810191389@qq.com");
//		newLoginList.add("1375791987@qq.com");
//		newLoginList.add("2239291449@qq.com");
//		newLoginList.add("2239291449@qq.com");
//		newLoginList.add("513773269@qq.com");
//		newLoginList.add("776715453@qq.com");
//		newLoginList.add("872066491@qq.com");
//		newLoginList.add("469894662@qq.com");
//		String append = "";
//		for (String string : newLoginList) {
//			append += ",'"+string+"'";
//		}
//		append = append.substring(1);
//		sb.append(" and pt.LOGIN_ID in ("+append+")");
		String sql = sb.toString();
		return MyUtils.queryInfoByWebtrn(path, gap, sql);
	}
	
	public List<List<String>> getBatchApplyInfoBefore2017(){
		String path = "E:/myJava/yiaiSql/20170607/yiaiBatchApplyBefore2017.txt";
		String gap = "__";
		
		// 得到申请记录
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT                                                   ");
		sb.append(" 	pca.id,                                              ");
		sb.append(" 	pca.type,                                            ");
		sb.append(" 	pca.credit,                                          ");
		sb.append(" 	pca.fk_trainee_id                                    ");
		sb.append(" FROM                                                     ");
		sb.append(" 	pe_credit_apply pca                                  ");
		sb.append(" JOIN pe_trainee pt ON pt.ID=pca.fk_trainee_id ");
		sb.append(" WHERE                                                    ");
		sb.append(" 	pca.fk_site_id = 'ff80808155da5b850155dddbec9404c9'  ");
		sb.append(" AND pca.flag_isvalid = '40288a962e9d9ac5012e9dd6b0aa0004'");
		sb.append(" and pca.create_date < '2017-02-05'");
		String sql = sb.toString();
		return MyUtils.queryInfoByWebtrn(path, gap, sql);
	}
	
	
	// 扣除延期的卡的学分
	public void batchApplyBefore2017(){
		List<List<String>> applyList = getBatchApplyInfo();
		
		// 扣除学分
		for (int i = 0; i < applyList.size(); i++) {
			List<String> objects = applyList.get(i);
			String creditApplyid = objects.get(0);
			String type = objects.get(1);
			String credit = objects.get(2);
			String traineeId = objects.get(3);
			this.executeDeductCredit(credit, type, traineeId, creditApplyid);
			System.out.println("已处理完了第" + i + "个申请");
		}
		System.out.println("处理完毕，共" + applyList.size() + "个记录");
	}
	
	
	// 批量申请
	public void batchApply(){
		List<List<String>> applyList = getBatchApplyInfo();
		
		// 扣除学分
		for (int i = 0; i < applyList.size(); i++) {
			List<String> objects = applyList.get(i);
			String creditApplyid = objects.get(0);
			String type = objects.get(1);
			String credit = objects.get(2);
			String traineeId = objects.get(3);
			this.executeDeductCredit(credit, type, traineeId, creditApplyid);
			System.out.println("已处理完了第" + i + "个申请");
		}
		System.out.println("处理完毕，共" + applyList.size() + "个记录");
	}

	/**
	 * 扣除一次申请的学分（执行sql）
	 * @param credit
	 * @param type
	 * @param traineeId
	 * @param creditApplyid
	 */
	public void executeDeductCredit(String credit, String type, String traineeId, String creditApplyid){
		Map<String, Object> deductMap = this.deductCredit(Integer.valueOf(credit), type, traineeId, siteId, creditApplyid);
		if ("1".equals(deductMap.get("code"))) { // 扣除成功
			List<String> sqlList = (List<String>) deductMap.get("sqlList");
			if (CollectionUtils.isNotEmpty(sqlList)) {
				for (String string : sqlList) {
					SshMysqlWebtrn.executeBySQL(string);
				}
//				SshMysqlWebtrn.executeBatchBySQL(sqlList);
			}
		} else {
			System.out.println("单次申请成功，但扣除学分失败:" + deductMap.get("info") + " traineeId=" + traineeId + ",creditApplyId=" + creditApplyid);
		}
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
//		cardSqlSb.append(" AND pcn.ACTIVATIONDATE < '2017-02-05'");
//		cardSqlSb.append(" ORDER BY pcn.VALIDDATE "); // 按截止日期排序
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
	
	public static void main(String[] args) {
		YiaiBatchApply yiaiBatchApply = new YiaiBatchApply();
		yiaiBatchApply.batchApply();
	}
}
