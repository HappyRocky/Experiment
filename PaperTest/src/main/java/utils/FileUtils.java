/**
 * 
 */
package utils;

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

/**
 * 包含常见的文件操作
 * 
 * @author GongYanshang
 *
 */
public class FileUtils {

	/**
	 * 按行读取文件
	 * @param filePath
	 * @return list，包含所有行的内容
	 */
	public static List<String> readFile(String filePath) {
		List<String> result = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			String str;
			while ((str = br.readLine()) != null) {
				result.add(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 把list输出至指定路径（覆盖，非追加）
	 * 
	 * @param list
	 * @param path
	 */
	public static void outputList(List<String> list, String path) {
		// 创建文件
		createFile(path);
		// 写入文件
		try {
			// 写入中文字符时解决中文乱码问题
			FileOutputStream fos = new FileOutputStream(new File(path));
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			for (String arr : list) {
				bw.write(arr + "\r\n");
			}
			// 注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
			bw.close();
			osw.close();
			fos.close();
			System.out.println("list输出完毕：" + path);
		} catch (Exception e) {
			System.out.println("写入文件出错");
			e.printStackTrace();
		}
	}
	
	/**
	 * 递归创建目录
	 * @param file
	 */
	public static void mkDir(File file) {
		if (file.exists()) {
			return;
		}
		if (!file.getParentFile().exists()) {
			mkDir(file.getParentFile());
		}
		file.mkdir();
	}
	
	public static void createFile(String path){
		File file = new File(path);
		if (file.exists()) {
			return;
		}
		mkDir(file.getParentFile());
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
