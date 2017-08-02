/*
 * 文件名：FreemarkerAddDefault.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月27日上午10:38:44
 */
package whaty;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import utils.FileUtils;

/**
 * @author Administrator
 *
 */
public class FreemarkerAddDefault {
	
	public static void freemarkerAddDefault(File file) {
		try {
			// 读取
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			String str = br.readLine();
			while (str != null) {
				str = handleLine(str);
				sb.append(str);
				str = br.readLine();
				if (str != null) {
					sb.append("\r\n");
				}
			}
			br.close();

			// 回写
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
			System.out.println("转化完毕：" + file.getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 处理一行，将freemarker的表达式加上默认值
	 * @param line
	 * @return
	 */
	public static String handleLine(String line){
		String pattern1 = "${i18nMap.";
		String defaultString = "!''";
		int startIdx = 0;
		while (true) {
			int idx = line.indexOf(pattern1, startIdx);
			if (idx < 0) { // 不再包含pattern
				break;
			}
			int endIdx = line.indexOf("}", idx);
			if (line.substring(endIdx - defaultString.length(), endIdx).equals(defaultString)) { // 包含pattern，但是已经加上了默认值
				startIdx = endIdx; // 继续查找后面的pattern
				continue;
			}
			line = line.substring(0, endIdx) + defaultString + line.substring(endIdx);
			startIdx = endIdx + defaultString.length();
		}
		return line;
	}
	
	/**
	 * 递归处理所有子文件夹下的文件
	 * @param file
	 */
	public static void handlePath(File file){
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File curFile : file.listFiles()) {
				handlePath(curFile);
			}
		} else {
			String name = file.getName();
			if (name.endsWith("html") || name.endsWith("htm") || name.endsWith("js") || name.endsWith("ftl")) {
				freemarkerAddDefault(file);
			}
		}
	}
	
	public static void main(String[] args) {
		
		String filePath = "E:/testEncode/fileName.txt"; //
		List<String> lineList = FileUtils.readFile(filePath);
		for (String line : lineList) {
			String fileName = "E:/myJava/whaty_space/trunk/src/main/webapp/" + line;
			try {
				File file = new File(fileName);
				if (!file.exists()) {
					System.out.println("不存在次路径:" + filePath);
				} else {
					handlePath(file);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("处理完毕");
		System.exit(0);
	}
	
}
