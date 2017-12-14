package whaty;
/** 
 * @className:ExportClass.java
 * @classDescription:
 * @author:gongyanshang
 * @createTime:2016年9月26日
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExportClass {
	private List<String> fileNamels = new ArrayList<String>();
	private String filesp = System.getProperty("file.separator");
	private String[] javaRoot = { "main"};// 要提取class文件的目录
	private String currentJavaRoot;
	private List<String> javaSourceDirectory = new ArrayList<String>();
	private List<String> javaRootLs = new ArrayList<String>();
	private boolean isExportAll = false;// 是否提取javaRoot目录下所有的文件，false:只提取java文件相对应的.class文件，true:提取java文件相对应的.class文件与其它文件

	public static void main(String[] args) {

		// 用svn导出更新的文件，存放到d:\svnupdate中
		// 执行程序后，把javaRoot目录下相对应的java文件提取项目中相对应的.class文件
		// 存放到D:\\svnupdate\\WebRoot\\WEB-INF\\classes
		// 下,WebRoot下的文件即为所要更新的所有文件
		
		
		/*2015-08-05
		 * author:梁灿
		 * 使用说明：
		 * 		maven项目和普通项目运行不同的类文件，普通项目运行ExportClass.java类;maven运行ExportClassMaven.java
		 * 		有java文件改动才需要运行该类
		 * 		exportClass.exportAll("svn检出路径","项目在工作空间路径",true)
		 * 		svn路径要求：src/com....从src开始，下面直接是从com路径开始，删除两者之间目录
		 * 				     其它非java类文件，检出放到WebRoot目录下
		 * 		项目在工作空间路径：如：E:\\workSpace\\maven_tyxl
		 * 		
		 * 		
		 * */
		ExportClass exportClass = new ExportClass();
		//exportClass.exportAll("D:\\abc", "E:\\workspace\\taqpx\\taqpx_training", true);// 提取在svn提取目录下，除WebRoot
		//exportClass.exportAll("D:\\20150610_tyut_learning", "E:\\workspace\\xm_tylearning", true);// 提取在svn提取目录下，除WebRoot
		exportClass.exportAll("F:\\whaty\\SVN diff export", "E:\\myJava\\thtm", true);// 提取在svn提取目录下，除WebRoot
		//exportClass.exportAll("D:\\20150330_gylearning", "E:\\workspace\\gy_learning", true);// 提取在svn提取目录下，除WebRoot
		//exportClass.exportAll("D:\\20150611_ajbz", "E:\\workspace\\taqpx_training", true);// 提取在svn提取目录下，除WebRoot
		//exportClass.exportAll("D:\\20150610_ajbz_cms", "E:\\workspace\\taqpx_cms", true);// 提取在svn提取目录下，除WebRoot
		//exportClass.exportAll("D:\\20150323_hbu", "E:\\workspace\\xm_hbu", true);// 提取在svn提取目录下，除WebRoot
		//exportClass.exportAll("D:\\20150327_wdcj", "E:\\workspace\\xm_wdcj", true);// 提取在svn提取目录下，除WebRoot
		System.out.println("完成");
	}

	/*
	 * 列出svn 更新时导出的文件中 src目录下所有文件的文件名（包括相对src的全路径，java文件名变为相对应的class文件名）
	 */
	public void listFile(File f) {

		if (!f.exists()) {
			System.err.println("文件或目录：" + f.getAbsolutePath() + "不存在");
			return;
		}
		String fileName = f.getAbsolutePath();
		if (f.isDirectory()) {
			// System.out.println("directory:"+f.getPath()+"--parent:"+f.getParent());
			// System.out.println("directory:"+f.getPath()+"--parent:"+f.getParent());
			String directoryName;
			if (fileName.indexOf(filesp + currentJavaRoot + filesp) != -1) {
				directoryName = fileName.substring(fileName.indexOf(this.filesp + this.currentJavaRoot + this.filesp) + this.currentJavaRoot.length() + 1);
				for (String javaroot : javaRoot) {
					int idx = directoryName.indexOf(javaroot);
					if (idx >= 0) {
						String path = directoryName.substring(idx + javaroot.length());
						if (path.startsWith("\\java")) {
							path = path.substring(5);
							javaSourceDirectory.add(path);
						} else if (path.startsWith("\\resources")) {
							path = path.substring(10);
							javaSourceDirectory.add(path);
						}
						
					}
				}
			}
			File[] t = f.listFiles();
			for (int i = 0; i < t.length; i++) {
				listFile(t[i]);
			}
		} else {
			// System.out.println("fileName:"+f.getAbsolutePath()+"--parent:"+f.getParent());

			if (fileName.indexOf(filesp + currentJavaRoot + filesp) != -1) {
				int index = fileName.indexOf(filesp + currentJavaRoot + filesp);
				if (fileName.lastIndexOf(".java") != -1) {
					String javaFile = fileName.substring(index + this.currentJavaRoot.length() + 1, fileName.lastIndexOf(".java")) + ".class";
					this.fileNamels.add(javaFile);
					System.out.println("fileName:" + javaFile);
				} else {
					if (this.isExportAll) {
						String otherFile = fileName.substring(index + this.currentJavaRoot.length() + 1);
						this.fileNamels.add(otherFile);
						System.out.println("fileName:" + otherFile);

					}
				}

			}
		}

	}

	/*
	 * svn 更新时导出的文件中，根据JAVA文件目录结构，提取项目中相应的.class文件； srcRoot ：用svn
	 * 提取更新文件所存放的目录，即提取文件后java源文件所在的父目录 projectRoot： 项目所在的路径 desRoot
	 * ：提取相对应文件后classes文件夹所在的目录，用于提取后文件保存的目录
	 */
	public void exportClass(String srcRoot, String projectRoot, String desRoot) {
		this.fileNamels.clear();
		this.initJavaRoot();
		if (this.javaRootLs.size() < 1) {
			System.out.println("不存在要提取class文件的目录！");
			return;
		}
//		this.listFile(new File(srcRoot));
		for (int i = 0; i < this.javaRootLs.size(); i++) {
			this.currentJavaRoot = this.javaRootLs.get(i);
			this.listFile(new File(srcRoot + this.filesp + this.currentJavaRoot));
		}
		getInnerClass(projectRoot);
		String src = null;
		String des = null;
		for (String fileName : fileNamels) {
			if (fileName.startsWith("\\main\\java")) {
				fileName = fileName.substring(10);
			} else if (fileName.startsWith("\\main\\resources")) {
				fileName = fileName.substring(15);
			}
			src = projectRoot + this.filesp + "src\\main\\webapp" + this.filesp + "WEB-INF" + this.filesp + "classes" + fileName;
			des = desRoot + this.filesp + "classes" + fileName;
			copyFile(src, des);
		}

	}

	/*
	 * 初始化要提取class文件的java源文件目录
	 */
	private void initJavaRoot() {
		if (this.javaRootLs.size() > 0) {// javaRootLs已经赋值，直接返回
			return;
		}
		for (int i = 0; i < javaRoot.length; i++) {
			if (!"webapp".equalsIgnoreCase(javaRoot[i])) {
				this.javaRootLs.add(javaRoot[i]);
			} else {
				System.out.println("javaRoot中不能包含webapp！");
			}
		}
	}

	/*
	 * 根据svn提取的文件目录，进行提取相对应的class文件 存到指定的desRoot/classes 目录下
	 * java文件目录为javaRoot中所指定的目录 isExportOtherFile 是否提取java文件所在目录的其它文件 如，java
	 * 配置文件 .xml
	 */
	public void exportAllByJavaRoot(String srcRoot, String projectRoot, String desRoot, boolean isExportOtherFile) {
		this.isExportAll = isExportOtherFile;
		exportClass(srcRoot, projectRoot, desRoot);
	}

	/*
	 * 根据svn提取的文件目录，进行提取相对应的class文件 存到srcRoot/WebRoot/WEB-INF/classes 目录下
	 * java文件目录为javaRoot中所指定的目录 isExportOtherFile 是否提取java文件所在目录的其它文件 如，java
	 * 配置文件 .xml
	 */
	public void exportAllByJavaRoot(String srcRoot, String projectRoot, boolean isExportOtherFile) {
		exportAllByJavaRoot(srcRoot, projectRoot, srcRoot + this.filesp + "WEB-INF", isExportOtherFile);
	}

	/*
	 * 根据svn提取的文件目录，进行提取相对应的class文件 java文件所在目录默认为除WebRoot目录 之外的目录
	 * isExportOtherFile 是否提取java文件所在目录的其它文件 如，配置文件 .xml
	 */
	public void exportAll(String srcRoot, String projectRoot, boolean isExportOtherFile) {

		File root = new File(srcRoot);
		if (!root.exists()) {
			System.out.println("文件或目录：" + root.getAbsolutePath() + "不存在");
			return;
		}
		this.javaRootLs.clear();
		File[] t = root.listFiles();

		for (int i = 0; i < t.length; i++) {
			if (!"webapp".equalsIgnoreCase(t[i].getName())) {
				this.javaRootLs.add(t[i].getName());
			}
		}
		exportAllByJavaRoot(srcRoot, projectRoot, isExportOtherFile);

	}

	/*
	 * 文件复制
	 */
	private void copyFile(String oldPath, String newPath) { // 复制文件
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);

			File newfile = new File(newPath);

			if (!newfile.getParentFile().exists()) {// 目录不存在时，创建目录
				newfile.getParentFile().mkdirs();
			}

			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);

				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				System.out.println("文件：" + oldfile.getName() + " 复制大小：" + (double) bytesum / 1024 + " KB");
			} else {
				System.err.println("文件：" + oldfile.toString() + "不存在！！！");
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();

		}

	}

	/*
	 * 查找提取文件中是否存在内部类，存在则提取出来
	 */
	private void getInnerClass(String projectRoot) {
		for (int i = 0; i < javaSourceDirectory.size(); i++) {
			String temp_javaSourceDirectory = javaSourceDirectory.get(i);
			String directory = projectRoot + this.filesp + "src" + this.filesp + "main" + this.filesp + "webapp" + this.filesp + "WEB-INF" + this.filesp + "classes" + temp_javaSourceDirectory;
			File temp_directory = new File(directory);
			File[] fs = temp_directory.listFiles();
			for (int j = 0; j < fs.length; j++) {
				File file = fs[j];
				if (!file.isDirectory()) {
					if (file.getName().indexOf("$") != -1 && file.getName().indexOf(".class") != -1) {
						String innerClassFather = javaSourceDirectory.get(i) + this.filesp + file.getName().substring(0, file.getName().indexOf("$")) + ".class";
						if (this.fileNamels.contains(innerClassFather)) {
							this.fileNamels.add(temp_javaSourceDirectory + this.filesp + file.getName());
							System.out.println("fileName:" + temp_javaSourceDirectory + this.filesp + file.getName());
						}
					}
				}
			}
		}

	}
}
