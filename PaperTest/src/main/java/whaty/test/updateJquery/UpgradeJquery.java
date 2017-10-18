package whaty.test.updateJquery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * 升级jquery，将低版本替换为高版本
 * @author Administrator
 *
 */
public class UpgradeJquery {
	
	public static String REGEXP = "((jquery)|(jquery\\d)|(jquery-\\d\\.\\d\\.\\d))\\.((js)|(min\\.js))"; // jquery.js 或 jquery.min.js 或 jquery-1.1.1.js 或 jquery-1.1.1.min.js
	public static String[] FILE_TYPES = {".jsp",".html",".htm",".ftl"}; // 待检查的文件类型
	
	// 要替换成的 script 引用句
	public static String SCRIPT_JS =     "/js/jquery.js"; 
	public static String SCRIPT_MIN_JS = "/js/jquery.min.js";
	
	/**
	 * 寻找目录下的所有包含jquery语句的文件
	 * @param path
	 * @return
	 */
	public static List<JqueryInfo> getJqueryList(File file){
		if (!file.exists()) {
			return null;
		}
		
		List<JqueryInfo> result = new ArrayList<JqueryInfo>();
		if (file.isFile()) { // 文件类型
			List<JqueryInfo> infoList = getJqueryInfoListByOneFile(file); // 得到此文件的符合条件的行
			if (infoList != null && infoList.size() > 0) {
				result.addAll(infoList);
			}
		} else if (file.isDirectory()) { // 文件夹类型
			File[] files = file.listFiles();
			for(File curFile : files){
				result.addAll(getJqueryList(curFile)); // 递归遍历所有子文件
			}
		}
		
		return result;
	}
	
	/**
	 * 得到一个文件中符合条件的行
	 * @param file
	 * @return 
	 */
	public static List<JqueryInfo> getJqueryInfoListByOneFile(File file){
		// 文件类型判断
		if (!file.exists() || !file.isFile() || !isNeededFile(file)) {
			return null;
		}
		// 读取文件
		String filePath = file.getAbsolutePath();
		List<JqueryInfo> jqueryInfoList = new ArrayList<JqueryInfo>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF-8"));
			String lineContent;
			int lineNum = 0;
			while ((lineContent = br.readLine()) != null) {
				lineNum ++;
				if (isJquery(lineContent)) { // 此行符合正则式
					jqueryInfoList.add(new JqueryInfo(filePath,lineContent.trim(),lineNum)); // 加入到返回结果中
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return jqueryInfoList;
	}
	
	/**
	 * 文件类型是否符合 FILE_TYPES 中定义的类型
	 * @param file
	 * @return
	 */
	public static boolean isNeededFile(File file){
		String fileName = file.getName();
		for(String type : FILE_TYPES){
			if (fileName.endsWith(type)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断一个字符串是否符合正则
	 * @param str
	 * @return
	 */
	public static boolean isJquery(String str){
		// 不改变注释
		if (str.trim().startsWith("<!--") || str.trim().startsWith("<%--") || str.trim().startsWith("<!#--")) {
			return false;
		}
		// 不改变本来就正确的句子
		if (str.contains("src=\"/js/jquery.js\"") || str.contains("src=\"/js/jquery.min.js\"")) {
			return false;
		}
		// 编译正则表达式
	    Pattern pattern = Pattern.compile(REGEXP);
	    Matcher matcher = pattern.matcher(str);
	    // 字符串是否与正则表达式相匹配
	    return matcher.find();
	}
	
	/**
	 * 将jqueryInfo的list进行输出
	 * @param infoList
	 * @return
	 */
	public static void outputList(List<JqueryInfo> infoList){
		if (infoList == null || infoList.size() == 0) {
			System.out.println("list为空！");
		}
		for (int i = 0; i < infoList.size(); i++) {
			JqueryInfo info = infoList.get(i);
			System.out.println(i + "：" + info.getLineContent() + "  文件名：" + info.getFileName() + "，第"
					+ info.getLineNum() + "行");
		}
	}
	
	/**
	 * 得到一个路径下包含所有子文件的符合正则的文件信息
	 * @param fileName
	 * @return
	 */
	public static List<JqueryInfo> getJqueryInfoList(String fileName){
		File file = new File(fileName);
		return getJqueryList(file);
	}
	
	/**
	 * 除去exclude中的行数，将list中其余所有的jquery引用句替换为正确的引用句
	 * @param list
	 * @param exclude 以空格分隔行号
	 */
	public static void replaceJqueryScript(List<JqueryInfo> list, String exclude){
		
		// 得到需要排除掉的jqueryInfo
		String[] excludes = exclude.split(" ");
		List<Integer> excludeLineList = new ArrayList<Integer>();
		for (String curExclude : excludes) {
			if (StringUtils.isNotBlank(curExclude)) {
				excludeLineList.add(Integer.valueOf(curExclude.trim()));
			}
		}
		
		// 遍历list，纠正jquery的引用句
		int correctCount = 0; // 已修改的个数
		for (int i = 0; i < list.size(); i++) {
			// 排除不需要的
			if (excludeLineList.contains(i)) {
				continue;
			}
			
			JqueryInfo info = list.get(i);
			String fileName = info.getFileName(); // 要读取的文件
			String fileTo = fileName; // 要输出至的文件
//			fileTo = "E:\\test1.txt";
			int targetNum = info.getLineNum(); // 要处理的行数
			File file = new File(fileName);
			
			try {
				// 读取文件
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
				String lineContent;
				int lineNum = 0;
				StringBuilder sb = new StringBuilder();
				while ((lineContent = br.readLine()) != null) {
					lineNum ++;
					if (lineNum == targetNum) { // 需要修改的行
						String correctStr = getCorrectScript(lineContent); // 正确的字符串
						sb.append(correctStr); // 替换为正确的字符串
						if (!correctStr.equals(lineContent)) { // 替换前后不同
							System.out.println(i + "：" + fileName + "第" + lineNum + "行，将" + lineContent.trim() + "改为" + correctStr.trim());
							correctCount ++;
						}
					} else {
						sb.append(lineContent); // 保持原句
					}
					sb.append("\r\n");
				}
				br.close();
				
				// 回写
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileTo), "UTF-8"));
				bw.write(sb.toString());
				bw.flush();
				bw.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("修改完毕，修改了 " + correctCount + "处");
	}
	
	/**
	 * 将一行中的内容替换成正确的jquery的引用句
	 * @param oriLine
	 * @return
	 */
	public static String getCorrectScript(String lineContent){
		
		int srcIdx = lineContent.indexOf("src=");
		
		// 如果此行不包含 src=，则原句返回，不修改此行，以免出现错误
		if (srcIdx < 0) {
			System.out.println("[warn]此行内容特殊，没有进行修复：" + lineContent);
			return lineContent;
		}
		
		// 获得src之前的子字符串
		int startIdx = srcIdx + 5;
		String startString = lineContent.substring(0,startIdx);
		
		// 获得src结束之后的子字符串
		String quote = lineContent.substring(srcIdx + 4, srcIdx + 5); // src= 后面的符号是单引号还是双引号
		String srcString = lineContent.substring(startIdx); 
		int endIdx = srcString.indexOf(quote);
		String endString = srcString.substring(endIdx);
		
		// 正确的引用句
		String correctString = lineContent.contains("min.js") ? SCRIPT_MIN_JS : SCRIPT_JS; 
		return startString + correctString + endString;
	}
	
	public static void main(String[] args) throws IOException {
		String filePath = "E:\\myJava\\release_webtrn\\src";
		List<JqueryInfo> list = getJqueryInfoList(filePath);
		outputList(list);
		
		if (list != null && list.size() > 0) {
			// 控制台输入需要排除的行号
			System.out.print("请输入不需要处理的编号，用空格隔开：");
			BufferedReader strin=new BufferedReader(new InputStreamReader(System.in));  
			String excludeString = strin.readLine(); 
			
			replaceJqueryScript(list,excludeString);
		}
		
		System.out.println("完成");
		
	}
}
