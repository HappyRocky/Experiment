/*
 * 文件名：MyUtils.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年6月5日上午8:50:29
 */
package whaty.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author Administrator
 *
 */
public class MyUtils {
	
	/**
	 * 生成随机uuid
	 * @return
	 */
	public static String uuid(){
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString().replaceAll("-", "");
		return id;
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
	 * 读取文件至list
	 * @param path
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public static List<String> readFile(String filePath) {
		List<String> result = new ArrayList<>();
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("文件不存在，不能读取：" + filePath);
			return result;
		}
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
	 * 将list转为字符串，用gap连接
	 * @param list
	 * @param gap
	 * @return
	 */
	public static String list2Str(List<String> list, String gap){
		if (list == null) {
			return null;
		} 
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(gap);
			}
			sb.append(StringUtils.defaultString(list.get(i)));
		}
		return sb.toString();
	}
	
	/**
	 * 数组转为list
	 * @param a
	 * @return
	 */
	public static List<String> array2List(String[] a){
		if (a == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		for (String s : a) {
			result.add(s);
		}
		return result;
	}
	
	/**
	 * 将list输出至path
	 * @param path
	 * @param titles 每一列的标题
	 * @param contentList 内容
	 */
	public static void outputExcel(String path, String[] titles, List<Object[]> contentList) {
		File file = new File(path);
		createFile(path);
		try {
			OutputStream os = new FileOutputStream(file);
			// 创建工作薄
			WritableWorkbook wwb = Workbook.createWorkbook(os);
			int maxSize = 10000; // 每一页的最大行数
			for (int j = 0; j < 100; j++) { // 最大100页
				int start = j * maxSize;
				int end = (j + 1) * maxSize;
				end = Math.min(end, contentList.size());
				// 创建新的一页
				WritableSheet sheet = wwb.createSheet("sheet" + (j + 1), j);
				// 创建要显示的内容，第一个参数为列，第二个参数为行，第三个参数为内容
				int row = 0;
				if (titles != null && titles.length > 0) {
					for (int i = 0; i < titles.length; i++) {
						Label label = new Label(i, 0, titles[i]);
						sheet.addCell(label);
					}
					row++;
				}
				
				// 输出内容
				for (int i = start; i < end; i++) {
					Object[] objects = contentList.get(i);
					for (int k = 0; k < objects.length; k++) {
						String curContent = objects[k] == null ? "" : objects[k].toString();
						Label label = new Label(k, row, curContent);
						sheet.addCell(label);
					}
					row++;
				}
				if (end == contentList.size()) {
					break;
				}
			}
			
			// 把创建的内容写入输出流，并关闭
			wwb.write();
			wwb.close();
			os.close();
			System.out.println("内容输出至" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取excel内容
	 * @param path
	 * @return
	 */
	public static List<String[]> readExcel(String path) {
		return readExcel(path, 1);
	}
	
	/**
	 * 读取excel内容
	 * @param path
	 * @return
	 */
	public static List<String[]> readExcel(String path, int sheetIdx) {
		List<String[]> resultList = new ArrayList<>();
		try {
			// 创建输入流
			InputStream stream = new FileInputStream(path);
			// 获取Excel文件对象
			Workbook rwb = Workbook.getWorkbook(stream);
			// 获取文件的指定工作表 默认的第一个
			Sheet sheet = rwb.getSheet(sheetIdx - 1);
			// 行数(表头的目录不需要，从1开始)
			for (int i = 1; i < sheet.getRows(); i++) {
				// 创建一个数组 用来存储每一列的值
				String[] str = new String[sheet.getColumns()];
				Cell cell = null;
				// 列数
				for (int j = 0; j < sheet.getColumns(); j++) {
					// 获取第i行，第j列的值
					cell = sheet.getCell(j, i);
					str[j] = cell.getContents();
				}
				// 把刚获取的列存入list
				resultList.add(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**
	 * 从webtrn中查询申请信息
	 * @return
	 */
	public static List<List<String>> queryInfoByWebtrn(String path, String gap, String sql){
		List<List<String>> result = new ArrayList<>();
		// 先查询本地是否存在数据
		File file = new File(path);
		if (file.exists()) {
			List<String> lineList = MyUtils.readFile(path);
			if (CollectionUtils.isNotEmpty(lineList)) {
				for (String line : lineList) {
					String[] strs = line.split(gap);
					List<String> list = MyUtils.array2List(strs);
					if (line.endsWith(gap)) {
						list.add("");
					}
					result.add(list);
				}
			}
			System.out.println("共查询到" + result.size() + "个申请记录，来自" + path);
			return result;
		}
		
		List<String> lineList = new ArrayList<>();
		List<Object[]> cardList =SshMysqlWebtrn.queryBySQL(sql);
		for (Object[] objects : cardList) {
			if (objects != null && objects.length > 0) {
				List<String> list = new ArrayList<>();
				for (Object object : objects) {
					String str = object == null ? "" : object.toString();
					list.add(str);
				}
				result.add(list);
				lineList.add(MyUtils.list2Str(list, gap));
			}
		}
		System.out.println("共查询到" + result.size() + "个申请记录");
		MyUtils.outputList(lineList, path);
		return result;
	}
	
	/**
	 * 从旧医爱库中查询申请信息
	 * @return
	 */
	public static List<List<String>> queryInfoByYiaiwang(String path, String gap, String sql){
		List<List<String>> result = new ArrayList<>();
		// 先查询本地是否存在数据
		File file = new File(path);
		if (file.exists()) {
			List<String> lineList = MyUtils.readFile(path);
			if (CollectionUtils.isNotEmpty(lineList)) {
				for (String line : lineList) {
					String[] strs = line.split(gap);
					List<String> list = MyUtils.array2List(strs);
					if (line.endsWith(gap)) {
						list.add("");
					}
					result.add(list);
				}
			}
			System.out.println("共查询到" + result.size() + "个申请记录，来自" + path);
			return result;
		}
		
		List<String> lineList = new ArrayList<>();
		List<Object[]> cardList =SshMysqlYiaiwang.queryBySQL(sql);
		for (Object[] objects : cardList) {
			if (objects != null && objects.length > 0) {
				List<String> list = new ArrayList<>();
				for (Object object : objects) {
					String str = object == null ? "" : object.toString();
					list.add(str);
				}
				result.add(list);
				lineList.add(MyUtils.list2Str(list, gap));
			}
		}
		System.out.println("共查询到" + result.size() + "个申请记录");
		MyUtils.outputList(lineList, path);
		return result;
	}
	
	/**
	 * 判断两个数组是否完全相同
	 * @param strs
	 * @param strs2
	 * @return
	 */
	public static boolean isSame(String[] strs, String[] strs2){
		if (strs == null || strs2 == null || strs.length != strs2.length) {
			return false;
		}
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] != null || strs2[i] != null) { // 至少有一个不是null
				String str1 = strs[i] == null ? strs2[i] : strs[i]; // str1 肯定不是null
				String str2 = strs[i] == null ? strs[i] : strs2[i];
				if (!str1.equals(str2)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 返回一个对象的toString()
	 * @param obj
	 * @return 如果对象为空，则返回""
	 */
	public static String valueOf(Object obj){
		return (obj == null ? "" : obj.toString());
	}
}
