package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/** 
 * @className:WordXml.java
 * @classDescription: 根据wordxml模板，输出word文档
 * @author:gongyanshang
 * @createTime:2016年9月27日
 */
public class WordXml {
	
	/**
	 * 根据wordxml模板、动态数据，输出word文档
	 * @param templatePath:模板的路径
	 * @param templateName:模板的文件名
	 * @param data:存放模板中动态变量值的Map
	 * @param out：文档的输出流，可以是response.getWriter()，也可以是文件输出流
	 */
	public static void exportWordByXml(String templatePath, String templateName, Map data, Writer out){
		try {
			// 装载模板
			Configuration cfg = new Configuration();
			cfg.setDefaultEncoding("UTF-8");
			cfg.setObjectWrapper(new DefaultObjectWrapper());
			cfg.setClassicCompatible(true);
			cfg.setDirectoryForTemplateLoading(new File(templatePath));
			Template t = cfg.getTemplate(templateName); 
			t.setEncoding("utf-8");
			
			// 输出word
			t.process(data, out); 
			
			// 关闭输出流
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Map data = new HashMap(); // 存放动态变量的值
        data .put( "regNo", "201321099333333"); // 给动态变量赋值
        
        Writer out = null;
        try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("E:/whaty/wordXmlOut.doc"), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
        String templatePath = "E:/whaty";
        String templateName = "test output xml.xml";

		exportWordByXml(templatePath,templateName,data,out);
		System.out.println("完成！");
	}
}
