package life.hunterGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 定义循环中的当时的某种场景，包括两岸的人员、已经尝试过的人员、此次行动的人员、行动方向
 * @author Gong
 *
 */
public class OneState {
	
	private List<Boolean> thisSide = new ArrayList<Boolean>(); // 出发岸边，包含的人
    private List<Boolean> otherSide = new ArrayList<Boolean>(); // 对岸，包含的人
    private int person1 = 0; // 行动的两个人
    private int person2 = 0;
    private int direction = 0; //0为去对岸，1为从对岸回来
    
    public OneState(){
    	super();
    }
	public OneState(List<Boolean> thisSide, List<Boolean> otherSide, int person1, int person2, int direction) {
		super();
		setThisSide(thisSide);
		setOtherSide(otherSide);
		this.person1 = person1;
		this.person2 = person2;
		this.direction = direction;	
	}
	
	public OneState clone(){
		return new OneState(thisSide, otherSide, person1, person2,direction);
	}
	
	/**
	 * 执行移动命令，得到两个人划船过去之后的新状态
	 * @return
	 */
	public OneState execute(){
		OneState reState = this.clone();
        // 规定好划船方向
        List<Boolean> startPosition = direction==0?reState.getThisSide():reState.getOtherSide();
        List<Boolean> targetPosition = direction==0?reState.getOtherSide():reState.getThisSide();
        // 移动两个人
        startPosition.set(person1,false);        
        targetPosition.set(person1,true);
		if (direction == 0) {
			startPosition.set(person2, false);
			targetPosition.set(person2, true);
		}
        
        reState.setDirection(1-direction);
        reState.setPerson1(reState.getDirection()==0?0:-1);
        reState.setPerson2(0);
        
        return reState;
	}
	
	/**
	 * 判断所有人员是否已经平安度过
	 * @return
	 */
	public boolean isFinished(){
		for(boolean b : thisSide){
			if(b){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 得到对此状态的描述字符串
	 */
	@Override
	public String toString(){
		String reString = "";

		reString += "此岸的人：";
		for(int i=0;i<thisSide.size();i++){
			if(thisSide.get(i)){
				reString += (People.getName(i)+" ");
			}
		}
		reString += "，对岸的人：";
		for(int i=0;i<otherSide.size();i++){
			if(otherSide.get(i)){
				reString += (People.getName(i)+" ");
			}
		}
		reString += "\r\n       ";
		if(direction==0){
			reString += (People.getName(person1)+"和"+People.getName(person2)+"乘船去对岸");
		}else{
			reString += (People.getName(person1)+"乘船回来");
		}
		return reString;
		
	}
	
	public List<Boolean> getThisSide() {
		return thisSide;
	}
	public void setThisSide(List<Boolean> thisSide) {
		this.thisSide.clear();
		for(boolean person : thisSide){
			this.thisSide.add(person);
		}
	}
	public List<Boolean> getOtherSide() {
		return otherSide;
	}
	public void setOtherSide(List<Boolean> otherSide) {
		this.otherSide.clear();
		for(boolean person : otherSide){
			this.otherSide.add(person);
		}
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public int getPerson1() {
		return person1;
	}
	public int getPerson2() {
		return person2;
	}
	public void setPerson1(int person1) {
		this.person1 = person1;
	}
	public void setPerson2(int person2) {
		this.person2 = person2;
	}
    
    

}
