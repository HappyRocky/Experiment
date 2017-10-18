package whaty.test.updateJquery;

/**
 * 包含特定字符串的文件信息
 * @author Administrator
 *
 */
public class JqueryInfo {
	private String fileName; // 全路径
	private int lineNum; // 符合条件的行号
	private String lineContent; //符合条件的行的内容
	
	public JqueryInfo(){
	}
	
	public JqueryInfo(String fileName) {
		super();
		this.fileName = fileName;
	}
	
	public JqueryInfo(String fileName, String lineContent, int lineNum) {
		super();
		this.fileName = fileName;
		this.lineContent = lineContent;
		this.lineNum = lineNum;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public String getLineContent() {
		return lineContent;
	}

	public void setLineContent(String lineContent) {
		this.lineContent = lineContent;
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	
}
