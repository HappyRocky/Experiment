package life.hunterGame;

public class People {
	
	public static final String[] NAME = {"父亲","母亲","儿子","女儿","猎人","狗"};
	
    public static final int FATHER = 0;
    public static final int MOTHER = 1;
    public static final int SON = 2;
    public static final int GIRL = 3;
    public static final int HUNTER = 4;
    public static final int DOG = 5;
    
    public static String getName(int idx){
    	return NAME[idx];
    }
}
