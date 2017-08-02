package utils.JSON;

import java.util.Arrays;
import java.util.List;

public class Student {
	
	public static String COMMON = "COMMON";
	
	private String name;
	private int age;
	private String[] relatives;
	private List<String> hobbyList;
	
	
//	public String getName() {
//		return name;
//	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String[] getRelatives() {
		return relatives;
	}
	public void setRelatives(String[] relatives) {
		this.relatives = relatives;
	}
	public List<String> getHobbyList() {
		return hobbyList;
	}
	public void setHobbyList(List<String> hobbyList) {
		this.hobbyList = hobbyList;
	}
	public static String getCOMMON() {
		return COMMON;
	}
	public static void setCOMMON(String cOMMON) {
		COMMON = cOMMON;
	}
	
	@Override
	public String toString() {
		return "Student [name=" + name + ", age=" + age + ", relatives=" + Arrays.toString(relatives) + ", hobbyList="
				+ hobbyList + "]";
	}

}
