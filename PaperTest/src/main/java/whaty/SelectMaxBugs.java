package whaty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.sun.java_cup.internal.runtime.virtual_parse_stack;

import utils.SortMethods;
import whaty.test.MyUtils;

/** 
 * @className:SelectMaxBugs.java
 * @classDescription:查询出excel中出问题次数最多的url
 * @author:yourname
 * @createTime:2017年11月14日
 */
public class SelectMaxBugs {

	public void outputUrl() throws Exception{
		String path = "F:/whaty/响应慢的站点详细信息.xls";
		List<String> outputList = new ArrayList<String>();
		
		// 扫描所有标签页
		for (int i = 1; i < 100; i++) {
			List<String[]> list = MyUtils.readExcel(path, i);
			if (CollectionUtils.isNotEmpty(list)) {
				// 标签页名称
				InputStream stream = new FileInputStream(path);
				Workbook rwb = Workbook.getWorkbook(stream);
				Sheet sheet = rwb.getSheet(i - 1);
				String sheetName = sheet.getName();
				outputList.add(sheetName);
				
				// url计数
				Map<String, Integer> urlCountMap = new HashMap<String, Integer>();
				for (String[] strings : list) {
					String url = strings[0];
					int idx = url.indexOf(";");
					if (idx > 0) {
						url = url.substring(0, idx);
					}
					if (urlCountMap.containsKey(url)) {
						urlCountMap.put(url, urlCountMap.get(url) + 1);
					} else {
						urlCountMap.put(url, 1);
					}
				}
				
				// 排序
				double[] counts = new double[urlCountMap.size()];
				int idx = 0;
				for (Entry<String, Integer> entry : urlCountMap.entrySet()) {
					int count = entry.getValue();
					counts[idx++] = count;
				}
				SortMethods sortMethods = new SortMethods(counts);
				double[] results = sortMethods.QSort();
				
				// 提取出最大的五个url
				int max = 5;
				for (int j = results.length - 1; j >= 0; j--) {
					double count = results[j];
					for (Entry<String, Integer> entry : urlCountMap.entrySet()) {
						if (entry.getValue() == count && max > 0) {
							outputList.add(entry.getKey() + "\t" + (int)count);
							max--;
						};
					}
					if (max <= 0) {
						break;
					}
				}
			}
			outputList.add("");
		}
		String outputPath = "F:/whaty/urlCount.txt";
		MyUtils.outputList(outputList, outputPath);
	}
	
	public static void main(String[] args) throws Exception {
		SelectMaxBugs selectMaxBugs = new SelectMaxBugs();
		selectMaxBugs.outputUrl();
		System.exit(0);
	}
}

