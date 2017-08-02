package whaty.test;


public class YiaiAddStudentCourse {
	public static void main(String [] args){
		for(int i=0;i< 50 ;i++){
			/*YiaiAddStudentCourseThread t =new YiaiAddStudentCourseThread(i);
			t.start();*/
			YiaiAddStudentModuleThread t = new YiaiAddStudentModuleThread(i);
			t.start();
		}
	}
}
