package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/** 
 * @className:DateUtils.java
 * @classDescription:日期工具类
 * @author:GongYanshang
 * @createTime:2017年9月7日
 */
public class DateUtils {
	/**
	 * 得到当天日期，格式为yyyyMMdd
	 * @return
	 */
	public static String getToday() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dateNowStr = sdf.format(d);
		return dateNowStr;
	}
}

