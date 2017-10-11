package utils.sensitiveWordFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * 敏感词过滤
 * 
 * @author Gong Yanshang
 *
 */
@SuppressWarnings("rawtypes")
public class SensitivewordUtil {
	public static int minMatchTYpe = 1; // 最小匹配规则
	public static int maxMatchType = 2; // 最大匹配规则
	public static final String SENSITIVE_WORD_MAP = "sensitiveWordMap"; // 保存在application中的敏感词库的key

	/**
	 * 判断文字是否包含敏感字符
	 * 
	 * @param txt
	 *            待检测语句
	 * @param matchType
	 *            1：最小匹配规则，2：最大匹配规则
	 * @param sensitiveWordMap
	 *            敏感词库，若为空则读取配置文件
	 * @return 若包含返回true，否则返回false
	 */
	public static boolean isContaintSensitiveWord(String txt, int matchType, Map sensitiveWordMap) {
		boolean flag = false;
		for (int i = 0; i < txt.length(); i++) {
			int matchFlag = Integer.valueOf(checkSensitiveWord(txt, i, matchType, null, sensitiveWordMap).get("matchSize").toString()); // 判断是否包含敏感字符
			if (matchFlag > 0) { // 大于0为存在，返回true
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * 获取文字中的敏感词
	 * 
	 * @param txt
	 *            文字
	 * @param matchType
	 *            1：最小匹配规则，2：最大匹配规则
	 * @param sensitiveWordMap
	 *            敏感词库，若为null则读取配置文件
	 * @return
	 */
	public static Set<String> getSensitiveWord(String txt, int matchType, Map sensitiveWordMap) {
		Set<String> sensitiveWordList = new HashSet<String>();
		for (int i = 0; i < txt.length(); i++) {
			int length = Integer.valueOf(checkSensitiveWord(txt, i, matchType, null, sensitiveWordMap).get("matchSize").toString()); // 判断是否包含敏感字符
			if (length > 0) { // 存在,加入list中
				sensitiveWordList.add(txt.substring(i, i + length));
				i = i + length - 1; // 减1的原因，是因为for会自增
			}
		}
		return sensitiveWordList;
	}

	/**
	 * 替换敏感字字符
	 * 
	 * @param txt
	 *            待检测语句
	 * @param matchType
	 *            1：最小匹配规则，2：最大匹配规则
	 * @param replaceChar
	 *            替换字符
	 * @param sensitiveWordMap
	 *            敏感词库，若为null则读取配置文件
	 */
	public static String replaceSensitiveWord(String txt, int matchType, String replaceChar, Map sensitiveWordMap) {
		String resultTxt = txt;
		for (int i = 0; i < resultTxt.length(); i++) {
			Map matchResult = checkSensitiveWord(resultTxt, i, matchType, replaceChar, sensitiveWordMap);
			int length = Integer.valueOf(matchResult.get("matchSize").toString()); // 判断是否包含敏感字符
			if (length > 0) { // 存在,加入list中
				String afterStr = (String) matchResult.get("afterStr");
				i = i + length - 1 + afterStr.length() - resultTxt.length(); // 减1是因为for会自增
				// 如果替换前后字符串长度不一致，说明替换字符串长度与被替换长度不一致，需加上差异
				resultTxt = afterStr;
			}
		}
		return resultTxt;
	}

	/**
	 * 获取替换字符串
	 * 
	 * @param replaceChar
	 *            替换成什么字符
	 * @param length
	 *            被替换的长度
	 * @return
	 */
	private static String getReplaceChars(String replaceChar, int length) {
		if (replaceChar.length() != 1) { // 如果字符>1，则不再重复
			return replaceChar;
		}
		String resultReplace = replaceChar;
		for (int i = 1; i < length; i++) {
			resultReplace += replaceChar;
		}
		return resultReplace;
	}

	/**
	 * 检查文字中从某个index开始，是否为敏感词
	 * 
	 * @param txt
	 *            带检测语句
	 * @param beginIndex
	 *            开始检测的index
	 * @param matchType
	 *            1：最小匹配规则；2：最大匹配规则
	 * @param replaceChar
	 *            要替换为的字符
	 * @param sensitiveWordMap
	 *            敏感词库，若为空则读取配置文件
	 * @return matchSize=敏感词长度，afterStr=替换之后的语句
	 */
	@SuppressWarnings("unchecked")
	public static Map checkSensitiveWord(String txt, int beginIndex, int matchType, String replaceChar, Map sensitiveWordMap) {
		if (replaceChar == null) {
			replaceChar = "";
		}
		boolean flag = false; // 敏感词结束标识位
		int matchFlag = 0; // 匹配标识数默认为0
		char word = 0;
		if (sensitiveWordMap == null) {
			sensitiveWordMap = getSensitiveWordMap(null);
		}
		Map nowMap = sensitiveWordMap;
		for (int i = beginIndex; i < txt.length(); i++) {
			word = txt.charAt(i);
			nowMap = (Map) nowMap.get(word); // 获取指定key
			if (nowMap != null) { // 存在，则判断是否为最后一个
				matchFlag++; // 找到相应key，匹配标识+1
				if ("1".equals(nowMap.get("isEnd"))) { // 如果为最后一个匹配规则,结束循环，返回匹配标识数
					flag = true; // 结束标志位为true
					if (SensitivewordUtil.minMatchTYpe == matchType) { // 最小规则，直接返回,最大规则还需继续查找
						break;
					}
				}
			} else { // 不存在，直接返回
				break;
			}
		}
		if (!flag) { // 必须有isEnd=1，排除到了末尾还没有匹配完的情况
			matchFlag = 0;
		}

		String afterStr = txt;
		if (matchFlag > 0) {
			afterStr = txt.substring(0, beginIndex) + getReplaceChars(replaceChar, matchFlag) + txt.substring(beginIndex + matchFlag);
		}

		Map result = new HashMap();
		result.put("matchSize", matchFlag);
		result.put("afterStr", afterStr);
		return result;
	}

	/**
	 * 得到敏感词库
	 * 
	 * @param request
	 *            若为null，则直接读取配置文件；否则，先尝试从application中获取
	 * @return
	 */
	public static Map getSensitiveWordMap(HttpServletRequest request) {
		ServletContext application = null;
		if (request != null) {
			application = request.getSession().getServletContext();
		}
		Map sensitiveWordMap = null;
		if (application != null) { // 先从application中读取
			sensitiveWordMap = (HashMap) application.getAttribute(SENSITIVE_WORD_MAP);
		}
		if (sensitiveWordMap == null) {
			sensitiveWordMap = new SensitiveWordInit().initKeyWord(); // 从配置文件中读取
			if (application != null) {
				application.setAttribute(SENSITIVE_WORD_MAP, sensitiveWordMap); // 存入application中
			}
		}
		return sensitiveWordMap;
	}

	public static void main(String[] args) {
		System.out.println("敏感词的数量：" + SensitivewordUtil.getSensitiveWordMap(null).size());
		String string = "法轮功big啊老师的教法流口水的风景一杯红酒埃里克双方就爱上了的看法bigs法轮功big啊老师的教法流口水的风景一杯红酒埃里克双方就爱上了的看法bigs法轮功big啊老师的教法流口水的风景一杯红酒埃里克双方就爱上了的看法bigs法轮功big啊老师的教法流口水的风景一杯红酒埃里克双方就爱上了的看法bigs法轮功big啊老师的教法流口水的风景一杯红酒埃里克双方就爱上了的看法bigs";
		System.out.println("待检测语句字数：" + string.length());
		int type = 1;
		String replaceWord = "*";
		System.out.println("敏感词过滤规则：" + (type == 1 ? "最小" : "最大") + "匹配规则，敏感词替换为：" + replaceWord);
		long beginTime = System.currentTimeMillis();
		// Set<String> set = SensitivewordFilter.getSensitiveWord(string, type);
		String afterStr = SensitivewordUtil.replaceSensitiveWord(string, type, replaceWord, null);
		long endTime = System.currentTimeMillis();
		// System.out.println("语句中包含敏感词的个数为：" + set.size() + "。包含：" + set);
		System.out.println("替换之后的语句为：" + afterStr);
		System.out.println("总共消耗时间为：" + (endTime - beginTime) + " ms");
		System.exit(0);
	}
}
