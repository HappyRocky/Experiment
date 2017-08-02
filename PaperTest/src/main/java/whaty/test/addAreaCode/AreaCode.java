package whaty.test.addAreaCode;
/** 
 * @className:AreaCode.java
 * @classDescription:
 * @author:yourname
 * @createTime:2017年7月27日
 */
public class AreaCode {
	private String id; // pe_area.id
	private String name; // pe_area.name
	private int level; // pe_area.level
	private String levelCode; // pe_area.levelCode
	private String parentId; // pe_area.fk_parent_id
	
	public AreaCode(String id, String name, int level, String levelCode, String parentId) {
		super();
		this.id = id;
		this.name = name;
		this.level = level;
		this.levelCode = levelCode;
		this.parentId = parentId;
	}

	/**
	 * 生成更新levelCode的sql
	 * @return
	 */
	public String getUpdateLevelCodeSql(){
		return "update pe_area set level_code='" + levelCode + "' where id='" + id + "';";
	}
	
	@Override
	public String toString() {
		return "AreaCode [id=" + id + ", name=" + name + ", level=" + level + ", levelCode=" + levelCode
				+ ", parentId=" + parentId + "]";
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getLevelCode() {
		return levelCode;
	}
	public void setLevelCode(String levelCode) {
		this.levelCode = levelCode;
	}
	
}

