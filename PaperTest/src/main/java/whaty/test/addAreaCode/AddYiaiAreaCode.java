package whaty.test.addAreaCode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import utils.DateUtils;
import whaty.test.MyUtils;
import whaty.test.SshMysqlWebtrn;
import whaty.test.SshMysqlWebtrn57;
import whaty.test.SshMysqlWebtrnTest;

/** 
 * @className:AddYiaiAreaCode.java
 * @classDescription:补充线上医爱所有区域的编码
 * @author:yourname
 * @createTime:2017年7月27日
 */
public class AddYiaiAreaCode {
	
//	private static final String[] LEVEL_CODES = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
//	private static final String SITE_ID = "ff80808155de17270155de1ecca20448"; // 49
//	private static final String SITE_ID = "ff80808155da5b850155dddbec9404c9"; // 85
	private static final String SITE_ID = "ff80808155d8b4ea0155dd0b669d0277"; // 93
	List<AreaCode> allNodeList = null;
	
	/**
	 * 递归生成所有的子孙节点的层级编码
	 * @param areaCode
	 * @return
	 */
	public List<AreaCode> generalAllChildCode(AreaCode areaCode){
		if (areaCode.getLevel() == 1) { // 输出省级日志
			System.out.println("开始处理" + areaCode.toString() + "的所有子节点");
		}
		List<AreaCode> result = new ArrayList<>();
//		String lastCode = LEVEL_CODES[areaCode.getLevel() + 1]; // 儿子节点的层级编码的最后一个字母
		
		// 查询出所有儿子节点
		List<AreaCode> allChildCodeList = new ArrayList<>(); // 所有子节点
		List<AreaCode> childCodeNoCodeList = new ArrayList<>(); // 没有层级编码的子节点
		int maxSerial = 0; // 有层级编码的子节点中的最后一个编码数字的最大值
		for (AreaCode curAreaCode : allNodeList) {
			if (curAreaCode.getParentId().equals(areaCode.getId())) {
				allChildCodeList.add(curAreaCode);
				String levelCode = curAreaCode.getLevelCode();
				if (StringUtils.isBlank(levelCode)) {
					childCodeNoCodeList.add(curAreaCode);
				} else {
					// 选出最后一个编码数字的最大值
					int lastSerial = 0;
					if (levelCode.endsWith("/")) {
						levelCode = levelCode.substring(0, levelCode.length() - 1);
					}
					int idx = levelCode.lastIndexOf("/");
					if (idx >= 0) {
						lastSerial = Integer.valueOf(levelCode.substring(idx + 1));
					} else { // 只有数字，说明是顶层
						lastSerial = Integer.valueOf(levelCode);
					}
					maxSerial = Math.max(maxSerial, lastSerial);
				}
			}
		}
		
		// 处理没有编码的子节点
		if (CollectionUtils.isNotEmpty(allChildCodeList)) {
			int startSerial = maxSerial + 1; // 新节点的起始序列
			// 遍历没有层级编码的子节点，生成层级编码
			for (int i = 0; i < childCodeNoCodeList.size(); i++) {
				AreaCode curAreaCode = childCodeNoCodeList.get(i);
				String a = areaCode.getLevelCode().endsWith("/") ? "" : "/";
				curAreaCode.setLevelCode(areaCode.getLevelCode() + a + (startSerial + i) + "/");
			}
			// 将刚刚生成层级编码的节点加入到输出队列
			result.addAll(childCodeNoCodeList);
			// 递归将子节点的子节点加入到输出队列
			for (AreaCode areaCode2 : allChildCodeList) {
				result.addAll(generalAllChildCode(areaCode2));
			}
		}
		
		// 去掉当前节点，因为接下来的遍历都是它的子孙节点，与它无关，节省遍历时间
		allNodeList.remove(areaCode);
		
		return result;
	}
	
	/**
	 * 将AreaCode转为update语句
	 * @param areaCodeList
	 * @return
	 */
	public List<String> getUpdateList(List<AreaCode> areaCodeList){
		List<String> result = new ArrayList<>();
		for (AreaCode areaCode : areaCodeList) {
			result.add(areaCode.getUpdateLevelCodeSql());
		}
		System.out.println("将" + result.size() + "个节点转换为update语句");
		return result;
	}
	
	/**
	 * 查询医爱网下的所有层级未编码的节点，生成更新语句
	 */
	public void outputUpdateSql(String path){
		// 存储医爱网下所有的区域节点
		System.out.println("查询医爱网所有的区域节点...");
		String sql = "select a.id,a.`name`,a.`level`,a.level_code,a.fk_parent_id from pe_area a where a.fk_site_id='" + SITE_ID + "'";
		List<Object[]> list = SshMysqlWebtrnTest.getBySQL(sql);
		if (CollectionUtils.isNotEmpty(list)) {
			allNodeList = new LinkedList<>(); // 所有的区域节点
			List<AreaCode> needRepairList = new ArrayList<>(); // 需要修复的区域节点
			AreaCode topAreaCode = null; // 顶级区域节点，level=0
			for (Object[] objs : list) {
				String id = MyUtils.valueOf(objs[0]);
				String name = MyUtils.valueOf(objs[1]);
				int level = Integer.valueOf(MyUtils.valueOf(objs[2]));
				String levelCode = MyUtils.valueOf(objs[3]);
				String parentId = MyUtils.valueOf(objs[4]);
				AreaCode areaCode = new AreaCode(id, name, level, levelCode, parentId);
				allNodeList.add(areaCode);
				
				// 处理顶级节点
				if (level == 0) {
					if (StringUtils.isBlank(levelCode)) { // 顶级节点也需要修复levelCode
						areaCode.setLevelCode("0/");
						needRepairList.add(areaCode);
					}
					topAreaCode = areaCode;
				}
			}
			if (topAreaCode != null) {
				needRepairList.addAll(generalAllChildCode(topAreaCode));
				List<String> updateSqlList = getUpdateList(needRepairList);
				MyUtils.outputList(updateSqlList, path);
			} else {
				System.out.println("没有查询到顶级节点");
			}
		} else {
			System.out.println("没有查询到节点，sql=" + sql);
		}
	}
	
	public static void main(String[] args) {
		String path = "E:/myJava/yiaiSql/" + DateUtils.getToday() + "/updateAreaCode.sql";
		AddYiaiAreaCode addYiaiAreaCode = new AddYiaiAreaCode();
		addYiaiAreaCode.outputUpdateSql(path);
		System.exit(0);
	}
}

