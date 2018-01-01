/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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
		List<String> result = new ArrayList<String>();
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
	
	/**
	 * 根据链接下载文件到本地
	 * @param urlString
	 * @param filename
	 * @param savePath
	 * @throws Exception
	 */
	public static void download(String urlString, String filename, String savePath) throws Exception  {
		// 构造URL
		URL url = new URL(urlString);
		// 打开连接
		URLConnection con = url.openConnection();
		// 设置请求超时为5s
		con.setConnectTimeout(5 * 1000);
		// 输入流
		InputStream is = con.getInputStream();

		// 1K的数据缓冲
		byte[] bs = new byte[1024];
		// 读取到的数据长度
		int len;
		// 输出的文件流
		File sf = new File(savePath);
		mkDir(sf);
		OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
		// 开始读取
		while ((len = is.read(bs)) != -1) {
			os.write(bs, 0, len);
		}
		// 完毕，关闭所有链接
		os.close();
		is.close();
	}   

}
