package whaty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.DateUtils;
import whaty.test.MyUtils;

public class CompileTXT {
	private String commitFilePath; // 线上比对文件先列表
	private String preCommitFilePath; // 本地要上线的文件列表

	public CompileTXT() {
		super();
	}

	public CompileTXT(String commitFilePath, String preCommitFilePath) {
		super();
		this.commitFilePath = commitFilePath;
		this.preCommitFilePath = preCommitFilePath;
	}

	@SuppressWarnings("resource")
	public void compileTXT() {
		List<String> listCompile = new ArrayList<String>();
		List<String> listCommit = new ArrayList<String>();
		List<String> listPreCommit = new ArrayList<String>();
		List<String> listDeleting = new ArrayList<String>();
		try {
			if(this.commitFilePath==null||"".equals(this.commitFilePath)||this.preCommitFilePath==null||"".equals(this.preCommitFilePath)){
				System.out.println("文件路径不能为空");
				return;
			}
			BufferedReader readerCommit = new BufferedReader(new FileReader(this.commitFilePath));
			String line = null;
			
			while ((line = readerCommit.readLine()) != null) {
				if(line.indexOf("adding ")==-1){
					if(line.indexOf("deleting ")!=-1){
						line=line.replace("deleting", "").trim();
						listDeleting.add(line);
					}
					listCommit.add(line);
				}
				
			}
			BufferedReader readerPreCommit = new BufferedReader(new FileReader(this.preCommitFilePath));
			line = null;
			while ((line = readerPreCommit.readLine()) != null) {
				if(line.indexOf("webtrn/webapp")!=-1){
					line = line.replace("webtrn/webapp", "webtrn").trim();
				}
				if(line.indexOf("learning/webapp")!=-1){
					line = line.replace("learning/webapp", "learning").trim();
				}
				listPreCommit.add(line);
			}
			for (int j = listCommit.size(), k = 0; k < j; k++) {
				String commitLine = listCommit.get(k).trim();
				for (int m = listPreCommit.size(), n = 0; n < m; n++) {
					String linePreCommit = listPreCommit.get(n).trim();
					if (linePreCommit.equals(commitLine)) {
						listCompile.add(linePreCommit);
						break;
					} 
				}
			}
			System.out.println("需回退的代码有：");
			for (int j = listCommit.size(), k = 0; k < j; k++) {
				String commitLine = listCommit.get(k).trim();
				boolean result=false;
				//文件比对
				for (int m = listCompile.size(), n = 0; n < m; n++) {
					String lineCommit = listCompile.get(n).trim();
					if (lineCommit.equals(commitLine)) {
						result=true;
						break;
					} 
				}
				//和新加的jar文件比对
				for (int m = listDeleting.size(), n = 0; n < m; n++) {
					String lineCommit = listDeleting.get(n).trim();
					if (lineCommit.equals(commitLine)) {
						result=true;
						break;
					} 
				}
				if(!result){
					System.out.println(listCommit.get(k));
				}
			}
			
			System.out.println("");
			System.out.println("需上线但是没有比对出的代码有：");
			for (int j = listPreCommit.size(), k = 0; k < j; k++) {
				String commitLine = listPreCommit.get(k).trim();
				boolean result=false;
				for (int m = listCompile.size(), n = 0; n < m; n++) {
					String lineCommit = listCompile.get(n).trim();
					if (lineCommit.equals(commitLine)) {
						result=true;
						break;
					} 
				}
				if(!result){
					System.out.println(listPreCommit.get(k));
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getCommitFilePath() {
		return commitFilePath;
	}

	public void setCommitFilePath(String commitFilePath) {
		this.commitFilePath = commitFilePath;
	}

	public String getPreCommitFilePath() {
		return preCommitFilePath;
	}

	public void setPreCommitFilePath(String preCommitFilePath) {
		this.preCommitFilePath = preCommitFilePath;
	}
	
	public static void main(String[] args) {
		String commitFilePath = "E:\\compare\\commit.txt";
		commitFilePath = "E:\\a.xls";
		String preCommitFilePath = "E:\\compare\\precommit.txt";
		CompileTXT compile = new CompileTXT(commitFilePath, preCommitFilePath);
		compile.compileTXT();
	}
}
