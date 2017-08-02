/**
 * 
 */
package utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 将输入的文件的每一行去重，然后用空格拼接起来
 * @author GongYanshang
 *
 */
public class ParseFiles {
	
	/**
	 * 将输入的文件的每一行去重，然后用空格拼接起来
	 * @param filePath
	 * @return
	 */
	public static String parseFile(String filePath){
		// 读取文件
		List<String> fileList = FileUtils.readFile(filePath);
		// 去重并用空格连接
		StringBuilder resultSb = new StringBuilder();
		if (fileList != null && fileList.size() > 0) {
			for (String line : fileList) {
				line = line.trim();
				if (StringUtils.isNotBlank(line) && !resultSb.toString().contains(line)) {
					resultSb.append(line + " ");
				}
			}
		}
		return resultSb.toString().trim();
	}
	
	public static void main(String[] args) {
		String filePath = "E:\\whaty\\git\\commitedFiles.txt";
		System.out.println(ParseFiles.parseFile(filePath));
	}
}
