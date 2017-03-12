package life.hunterGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Gong on 2016/3/6.
 * 游戏描述：：
 * 人员：父亲，母亲，儿子，女儿，猎人，狗 共6人
 * 场景：6个人在一个河岸上，都想要渡河，但是只有一只船，而且最多载两个人。
 * 限制：
 * 1、会划船的人：父亲，母亲，猎人
 * 2、母亲不在，父亲会杀了女儿；父亲不在，母亲会杀了儿子；猎人不在，狗会杀了所有人
 * 问：采用什么方法，能让所有人和狗安全到达对岸
 */

public class HunterGame {

    private final static int TOTALCOUNT = People.NAME.length;
    private final static int[] RowerList = {People.FATHER,People.MOTHER,People.HUNTER};
    private List<Boolean> thisSide = new ArrayList<Boolean>(); // 出发岸边，包含的人
    private List<Boolean> otherSide = new ArrayList<Boolean>(); // 对岸，包含的人
    private List<Boolean> inBoat = new ArrayList<Boolean>(); // 船上的人 
    private Stack<OneState> stateStack = new Stack<OneState>(); // 按次序存放每一步的行动情况

    public HunterGame() {
		super();
		initialization();
	}

	/**
     * 初始化
     */
    private void initialization(){
        for(int i = 0;i<TOTALCOUNT;i++) {
            thisSide.add(true);
            otherSide.add(false);
            inBoat.add(false);
        }
        stateStack.clear();
    }

    /**
     * 判断一个set里面，是否会出现杀害
     * @param set
     * @return
     */
    private boolean haveDeath(List<Boolean> set){
        if(set.get(People.FATHER) && set.get(People.GIRL) && !set.get(People.MOTHER)) return true;
        if(set.get(People.MOTHER) && set.get(People.SON) && !set.get(People.FATHER)) return true;
        if(set.get(People.DOG) && !set.get(People.HUNTER)){
        	for(int i=0;i<set.size();i++){
        		if(set.get(i) && i!=People.DOG){
        			return true;
        		}
        	} 	
        }
        return false;
    }

    /**
     * 判断船上是否有划船手
     */
    private boolean haveRower(int person1, int person2){
        return isRower(person1)||isRower(person2);
    }
    private static boolean isRower(int person1){
        for(int rower : RowerList){
            if(person1==rower){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断将一/两个人坐船是否可行，如果可行，则这样做
     * @param side1,side2,dierction,person1,person2 : dierction=0，则方向为去对岸，否则方向为回来
     * @return
     */
    private boolean canMove(List<Boolean> side1, List<Boolean> side2, int direction, int person1, int person2){

		if (direction == 0) { //往对岸走，两个人一起
			// 判断两个人人是否会划船
			if (!haveRower(person1, person2)) {
				return false;
			}


			// 两个人是否可以一起划船
			for (int i = 0; i < inBoat.size(); i++) {
				if (i == person1 || i == person2) {
					inBoat.set(i, true);
				} else {
					inBoat.set(i, false);
				}
			}
			if (haveDeath(inBoat)) {
				return false;
			}

			// 规定好划船方向
			List<Boolean> startPosition = direction == 0 ? side1 : side2;
			List<Boolean> targetPosition = direction == 0 ? side2 : side1;

			// 判断出发点是否含有这两个人
			if (!startPosition.get(person1) || !startPosition.get(person2)) {
				return false;
			}

			// 移动两个人
			startPosition.set(person1, false);
			startPosition.set(person2, false);
			targetPosition.set(person1, true);
			targetPosition.set(person2, true);

			// 判断移动之后是否满足条件限制
			boolean can = true;
			if (haveDeath(side1) || haveDeath(side2)) {
				can = false;
			}

			// 撤消移动
			targetPosition.set(person1, false);
			targetPosition.set(person2, false);
			startPosition.set(person1, true);
			startPosition.set(person2, true);
			return can;
		} else { //从对岸回来，一个人划船
			// 判断此人人是否会划船
			if (!isRower(person1)) {
				return false;
			}

			// 判断出发点是否含有这个人
			if (!side2.get(person1)) {
				return false;
			}

			// 移动
			side2.set(person1, false);
			side1.set(person1, true);

			// 判断移动之后是否满足条件限制
			boolean can = true;
			if (haveDeath(side1) || haveDeath(side2)) {
				can = false;
			}

			// 撤消移动
			side1.set(person1, false);
			side2.set(person1, true);
			return can;
		}
	}
    
    /**
     *  得到符合条件的下一种走法
     * @param state
     * @return
     */
    private OneState getNextState(OneState state){
    	OneState stateClone = state.clone();
    	int person1 = stateClone.getPerson1();
    	int person2 = stateClone.getPerson2();
		if (state.getDirection() == 0) {
			for (int i = person1; i < TOTALCOUNT - 1; i++) {
				for (int j = i + 1; j < TOTALCOUNT; j++) {
					if (i == person1) {
						j = j > person2 ? j : (person2 + 1);
						if (j >= TOTALCOUNT) {
							break;
						}
					}

					if (canMove(stateClone.getThisSide(), stateClone.getOtherSide(), stateClone.getDirection(), i, j)) {
						return new OneState(stateClone.getThisSide(), stateClone.getOtherSide(), i, j, stateClone.getDirection());
					}
				}
			}
    	}else{
    		for (int i = person1+1; i < TOTALCOUNT; i++) {
    			if (canMove(stateClone.getThisSide(), stateClone.getOtherSide(), stateClone.getDirection(), i, 0)) {
					return new OneState(stateClone.getThisSide(), stateClone.getOtherSide(), i, 0, stateClone.getDirection());
				}
    		}
    	}
    	return null;
    }
    
    /**
     * 开始解题
     */
    public void start(){
    	int solutionCount = 0;
    	OneState currentState = new OneState(thisSide, otherSide, 0, 0, 0);
    	while(true){
    			OneState nextState = getNextState(currentState);
    			while(nextState==null && !stateStack.isEmpty()){
    				currentState = stateStack.pop();
    				nextState = getNextState(currentState);
    			}
    			if(stateStack.isEmpty() && nextState==null){
    				break;
    			}
    			stateStack.push(nextState);
    			currentState = nextState.execute();
    			if(currentState.isFinished()){
    				System.out.println("第"+(++solutionCount)+"种方案：");
    				outputSolution(stateStack);
    				currentState = stateStack.pop();
    			}
    	}		
    }
    
    /**
     * 输出一个栈里保存的解决方案
     * @param solutions
     */
    public void outputSolution(Stack<OneState> solutions){
    	OneState currentState = new OneState();
    	List<String> resultList = new ArrayList<String>();
    	Stack<OneState> solutions2 = (Stack<OneState>) solutions.clone();
    	while(!solutions2.isEmpty()){
    		currentState = solutions2.pop();
    		resultList.add(currentState.toString());
    	}
    	
    	for(int i=resultList.size()-1;i>=0;i--){
    		System.out.println(resultList.get(i));
    	}
    }
    
    public static void main(String[] args) {
		HunterGame game = new HunterGame();
		game.start();
	}

}
