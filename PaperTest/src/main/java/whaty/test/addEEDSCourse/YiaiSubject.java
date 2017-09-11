package whaty.test.addEEDSCourse;

import java.util.Map;

/** 
 * @className:YiaiSubject.java
 * @classDescription:
 * @author:yourname
 * @createTime:2017年9月5日
 */
public class YiaiSubject {
	private String id;
	private String name;
	private int level;
	private String code;
	private String parentId;
	private Map<String, YiaiSubject> childrenMap;
	
	public YiaiSubject(String id, String name, int level, String code, String parentId, Map<String, YiaiSubject> childrenMap) {
		super();
		this.id = id;
		this.name = name;
		this.level = level;
		this.code = code;
		this.parentId = parentId;
		this.childrenMap = childrenMap;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public Map<String, YiaiSubject> getChildrenMap() {
		return childrenMap;
	}
	public void setChildrenMap(Map<String, YiaiSubject> childrenMap) {
		this.childrenMap = childrenMap;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
}

