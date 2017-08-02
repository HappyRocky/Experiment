package whaty.test;

public class YiaiAddCreditApplyInfo {
	public static void main(String [] args){
		for(int i=0;i< 10 ;i++){
			YiaiAddCreditApplyInfoThread t = new YiaiAddCreditApplyInfoThread(i);
			t.start();
		}
	}
}
