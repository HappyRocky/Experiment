/*
 * 文件名：JSONtest.java 
 * 描述：〈描述〉
 * 创建人：Administrator
 * 创建时间：2017年5月17日下午3:11:19
 */
package utils.JSON;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Administrator
 *
 */
public class JSONtest {
	
	public static void main(String[] args) {
		// 初始化一个实体对象并赋值
		Student student = new Student();
		List<String> hobbyList = new ArrayList<String>();
		hobbyList.add("Baseball");
		hobbyList.add("Sing");
		student.setHobbyList(hobbyList);
		student.setAge(10);
		student.setName("Mark");
		String[] relatives  = {"Mother", "Father"};
		student.setRelatives(relatives);
		System.out.println("初始对象：" + student.toString());
		
		// 实体对象转为json对象
		JSONArray jsonArray = JSONArray.fromObject(student);
		
		// json对象转为json字符串
		String jsonString = jsonArray.toString();
		System.out.println("json字符串：" + jsonString);
		
		// json字符串可能是包围在[]中，需要去掉
		if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
			jsonString = jsonString.substring(1, jsonString.length() - 1);
		}
		
		// json字符串转为 json对象
		JSONObject JSONObject2 = JSONObject.fromObject(jsonString);
		
		// json对象转为实体对象
		Student student2 = (Student)JSONObject.toBean(JSONObject2, Student.class);
		System.out.println("转化为的对象：" + student2.toString());
	}
}
