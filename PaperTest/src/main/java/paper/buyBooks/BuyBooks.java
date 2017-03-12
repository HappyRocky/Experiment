package paper.buyBooks;

/**
 * 《编程之美》P29买书问题
 * 问题描述：
 * 1、有5种书，哈利波特卷1~卷5，每种书价格相同，8元/本
 * 2、促销活动，一次性购买多卷，享受折扣：
 * 卷数   折扣
 *  2     5%
 *  3     10%
 *  4     20%
 *  5     25%
 * 3、一本书只能享受一种折扣，不能叠加。如购买了2本卷一，1本卷二，则1本卷一1本卷二可以享受5%折扣，第2本卷一要按照原价。
 * 4、已知读者要买一批书，卷1~卷5分别要买X1~X5本，请问分几次购买、每次怎么搭配不同卷，价格可以最低？
 *
 * 问题分析：
 *     不能用贪心算法（即优先最大不同卷数的搭配），这样可以证明不一定是最低的价格。
 *     用动态规划。
 * Created by Gong on 2016/11/12.
 */
public class BuyBooks {

    private int[] bookNums = new int[5]; // 每种书的数量

    public BuyBooks(int[] nums){
        bookNums = nums;
    }

    /**
     * 开始寻找最优搭配
     */
    public void run(){
        
    }

    /**
     * 是否还有书没有进行搭配
     * @return
     */
    private boolean hasMore(){
        for (int num : bookNums){
            if (num > 0){
                return false;
            }
        }
        return true;
    }

}
