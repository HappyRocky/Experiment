package whaty.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YiaiAddStudentModuleThread extends Thread {
	private int current;
	public static String fk_site_id = "ff80808155da5b850155dddbec9404c9";
	public static String sCClassId = "ff8080815a27e1a4015a353a4767005f"; //四川班级ID
	public static String qGClassId ="ff8080815a27e1a4015a353967cf0037";  //全国班级ID
	public static String projrctId = "ff8080815a27e1a4015a353864350032"; // 项目ID
	public YiaiAddStudentModuleThread(int current) {
		this.current = current;
	}
	public void run(){
		int start = current*5000;
		System.out.println(current+"========执行开始                    +++++++++++++++ isSichuan:");
		List<Object[]> list = getlocalInfo(start);
		for(int m =0; m < list.size();m++ ){
			System.out.println(current+"========  "+m+"   start============"+list.size());
			Object[] obj = list.get(m);
			String fk_trainee_id = String.valueOf(obj[0]);
			String classId = String.valueOf(obj[1]);
			//添加学习流程
			List<String> moduleList = getClassModule(classId);
			for(int j=0;j<moduleList.size();j++){
				String moduleId = String.valueOf(moduleList.get(j));
				String pr_moduleId = getUUID();
				String pr_training_module = " INSERT INTO pr_training_module (`id`, `create_time`, `fk_trainee_id`, `fk_module_id`, `fk_site_id`, `status`) VALUES ('"+pr_moduleId+"', now(), '"+fk_trainee_id+"', '"+moduleId+"', '"+fk_site_id+"', '9dfe07fb9c1511e6975b00251113d11d'); " ;
				SshMysqlWebtrn.writeTxt(pr_training_module, "sql", "training_module",current);
			}
			moduleList.clear();
		}
		System.out.println(current+"========执行完毕");
	}
	public  List<String> getClassModule(String classId){
		List <String> moduleList = new ArrayList<String>();
		if(sCClassId.equals(classId)){
			moduleList.add("ff8080815a27e1a4015a353b3d640065");
			moduleList.add("ff8080815a27e1a4015a353b3d6f0066");
			moduleList.add("ff8080815a27e1a4015a353b3d8a0068");
			moduleList.add("ff8080815a27e1a4015a353b3d930069");
		}else if(qGClassId.equals(classId)){
			moduleList.add("ff8080815a27e1a4015a353969d8005b");
			moduleList.add("ff8080815a27e1a4015a353969d8005c");
			moduleList.add("ff8080815a27e1a4015a353969d9005e");
		}
		return moduleList;
	}
	
	public  List<Object[]> getlocalInfo(int start){
		String sql ="SELECT DISTINCT fk_trainee_id, fk_training_class_id FROM yiai_certificate_info WHERE fk_trainee_id IS NOT NULL AND fk_training_class_id IS NOT NULL limit "+start+",5000  ";
		List<Object[]> list =SshMysqlLocal.queryBySQL(sql);
		return list;
	}
	
	public String getUUID() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replace("-", "");
		return uuid;
	}
	
	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}
}
