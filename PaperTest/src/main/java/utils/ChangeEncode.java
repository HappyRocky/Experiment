package utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 *  
 * 
 * @className:ChangeEncode.java
 * @classDescription:检验编码、将一个文件的编码改为另一种编码
 * @author:gongyanshang
 * @createTime:2016年12月8日
 */
public class ChangeEncode {
	
	private static int GBKCount = 0;
	
	private static String[] FILE_TYPES = {".jsp",".html",".htm",".ftl"}; // 待检查的文件类型
	
	private static String[] CONTENT_TYPES = {"charset=gb2312","charset=gbk"}; // 有的文件明确写出是什么编码类型，需要改成utf-8
	/**
	 * 将一个文件从一种编码改为另一种编码
	 * 
	 * @param fileName
	 *            文件全路径
	 * @param fromCode
	 *            原编码
	 * @param toCode
	 *            要转换成的编码
	 */
	public static void changeEncode(String fileName, String fromCode, String toCode) {
		String fileTo = fileName;
		try {
			// 读取
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName(fromCode)));
			StringBuilder sb = new StringBuilder();
			String str;
			while ((str = br.readLine()) != null) {
				str = changeContentType(str);
				sb.append(str + "\r\n");
			}
			br.close();

			// 回写
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileTo), Charset.forName(toCode)));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
			System.out.println(fileName + " 由 " + fromCode + " 转换成 " + toCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将明确标识出charset的行改为utf-8
	 * @param str
	 * @return
	 */
	public static String changeContentType(String str){
		String lowcaseStr = str.toLowerCase();
		String result = str;
		for(String contentType : CONTENT_TYPES){
			if (lowcaseStr.contains(contentType)) {
				result = str.replaceAll("(?i)" + contentType, "charset=utf-8");
				System.out.println("由 " + str + " 转为 " + result);
				return result;
			}
		}
		if (lowcaseStr.contains("pageencoding=\"gbk\"")) {
			result = str.replaceAll("(?i)" + "pageencoding=\"gbk\"", "pageEncoding=\"utf-8\"");
			System.out.println("由 " + str + " 转为 " + result);
			return result;
		}
		return result;
	}

	/**
	 * 判断文件是否为UTF-8编码（包含有BOM和无BOM的UTF8）
	 * 说明：绝大多数情况可以判断正确，但是不是完全无误，可能会将无BOM头的UTF8误判为非UTF8，错误大概为1/69。
	 * @param file
	 * @return 若是有/无BOM头的UTF8，则返回true；否则返回false
	 */
	public static boolean isUTF8(File file){
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			StreamBuffer sbuf = new StreamBuffer(in, 10);
			
			// UTF8如果有BOM，则前三位都是固定的
			// UTF8的BOM：0xEF,0xBB,0xBF
			// UTF16BE的BOM：0xFE,0xFF
			// UTF16LE的BOM：0xFF,0xFE
			// UTF32BE的BOM：0x00,0x00,0xFE,0xFF
			// UTF32LE的BOM：0xFF,0xFE,0x00,0x00
			if (sbuf.next() == 0xEF && sbuf.next() == 0xBB && sbuf.next() == 0xBF) {
				return true;
			}
			
			// 开始检验是否是无BOM的UTF8
			sbuf.redo();
			// 1. U-00000000 - U-0000007F: 0xxxxxxx
			// 2. U-00000080 - U-000007FF: 110xxxxx 10xxxxxx
			// 3. U-00000800 - U-0000FFFF: 1110xxxx 10xxxxxx 10xxxxxx
			// 4. U-00010000 - U-001FFFFF: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
			// 5. U-00200000 - U-03FFFFFF: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
			// 6. U-04000000 - U-7FFFFFFF: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
			for (int ch = 0; (ch = sbuf.next()) != -1;) {
				int n = 0;
				if (ch <= 0x7F) {
					n = 1;
				} else if (ch <= 0xBF) {
					return false;
				} else if (ch <= 0xDF) {
					n = 2;
				} else if (ch <= 0xEF) {
					n = 3;
				} else if (ch <= 0xF7) {
					n = 4;
				} else if (ch <= 0xFB) {
					n = 5;
				} else if (ch <= 0xFD) {
					n = 6;
				} else {
					return false;
				}
				while (--n > 0) {
					ch = sbuf.next();
					if (ch != -1 && ((ch & 0x80) != 0x80)) {
						return false;
					}
				}
			}
			return true;
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			if (in != null) {
				try{
					in.close();
				}catch(Exception e){}
			}
		}
		return true;
	}

	/**
	 * 遍历一个文件夹下的所有文件，符合后缀名的文件进行编码转换
	 * @param folderPath 文件夹路径
	 * @param fromCode 原编码
	 * @param toCode 要转换成的编码
	 * @param extension 后缀名
	 * @throws IOException 
	 */
	public static void changeEncodeByFolder(String folderPath, String fromCode, String toCode) throws IOException{
		GBKCount = 0;
		changeEncodeByFolder(new File(folderPath),fromCode,toCode);
		System.out.println("非UTF-8文件的个数：" + GBKCount);
	}
	
	/**
	 * 递归遍历一个文件夹下的所有文件，符合后缀名的文件进行编码转换
	 * @param file 文件
	 * @param fromCode 原编码
	 * @param toCode 要转换成的编码
	 * @param extension 后缀名，要包含"."
	 * @throws IOException 
	 */
	public static void changeEncodeByFolder(File file, String fromCode, String toCode) throws IOException{
		if (file.exists()) {
			if (file.isDirectory()) { // 是文件夹
				File[] files = file.listFiles();
				for (File curFile : files) {
					changeEncodeByFolder(curFile, fromCode, toCode);
				}
			} else { // 是文件，进行编码转换
				String fileName = file.getAbsolutePath();
				if (isFileType(fileName) && !isUTF8(file)) { // 符合后缀名，且不是UTF8（默认为GBK）
					changeEncode(fileName, fromCode, toCode);
//					System.out.println(fileName + " 不是 UTF8");
					GBKCount ++;
				}
			}
		}
	}
	
	/**
	 * 判断文件名的后缀是否符合要求
	 * @param fileName
	 * @return
	 */
	public static boolean isFileType(String fileName){
		for(String type : FILE_TYPES){
			if (fileName.endsWith(type)) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		String fileName = "E:\\User.java";
		String folderName = "E:\\myJava\\release_webtrn\\src\\main\\webapp";
		// ChangeEncode.changeEncode(fileName, "GBK", "UTF-8");
		// System.out.println("转换完成");
		
		changeEncodeByFolder(folderName,"GBK","UTF-8");

	}

	static class StreamBuffer {
		final InputStream in;
		final byte[] buf;
		int pos = -1;// 初始值为-1,表示指针尚未移动.
		int len;

		public StreamBuffer(InputStream in, int size) {
			this.in = in;
			if (size < 3) {
				size = 3;
			}
			this.buf = new byte[size];
		}

		public void redo() {
			this.pos = 0;
		}

		public int next() throws IOException {
			if (len > 0 || pos < 0) {
				if (++pos == len) {
					if ((len = in.read(buf)) == 0) {
						return -1;
					}
					pos = 0;
				}
				return this.buf[this.pos] & 0xFF;
			} else {
				return -1;
			}
		}
	}
}
