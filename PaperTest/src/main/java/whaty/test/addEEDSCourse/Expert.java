package whaty.test.addEEDSCourse;
/** 
 * @className:Expert.java
 * @classDescription:专家
 * @author:yourname
 * @createTime:2017年9月11日
 */
public class Expert {
	private String id;
	private String name;
	private String workPlace;
	private YiaiSubject zhicheng;
	
	public Expert(String id, String name, String workPlace, YiaiSubject zhicheng) {
		super();
		this.id = id;
		this.name = name;
		this.workPlace = workPlace;
		this.zhicheng = zhicheng;
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
	public String getWorkPlace() {
		return workPlace;
	}
	public void setWorkPlace(String workPlace) {
		this.workPlace = workPlace;
	}
	public YiaiSubject getZhicheng() {
		return zhicheng;
	}
	public void setZhicheng(YiaiSubject zhicheng) {
		this.zhicheng = zhicheng;
	}
}

