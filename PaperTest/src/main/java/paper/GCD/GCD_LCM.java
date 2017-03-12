package paper.GCD;

/**
 * Created by admin on 2015/11/10.
 * GCD: Greatest Common Divisor
 * LCM: Least Common Multiple
 */
public class GCD_LCM {

    /*
    compute the GCD of integer m and n
    input: two integers m and n
    output: the GCD of m and n, if m and n are positive integers
            -1, otherwise
     */
    static int GCD(int m,int n){
        if(m<1 || n<1) // m and n are not all positive integers
            return -1;

        int max = m>n?m:n;
        int min = m>n?n:m;
        while(min!=0){
            int temp = min;
            min = max%min;
            max = temp;
        }
        return max;
    }

    /*
    compute the LCM of integer m and n
    input: two integers m and n
    output: the LCM of m and n, if m and n are positive integers
            -1, otherwise
     */
    static int LCM(int m,int n){
        if(m<1 || n<1) // m and n are not all positive integers
            return -1;

        return m*n/GCD(m,n);
    }

    public static void main(String[] args){
        int[] a = {21, 0};
        int myGCD = GCD(a[0], a[1]);
        if(myGCD == -1)
            System.out.println("( "+a[0]+", "+a[1]+" ) have no GCD or LCM");
        else {
            System.out.println("GCD of ( " + a[0] + ", " + a[1] + " ) is " + myGCD);
            System.out.println("LCM of ( " + a[0] + ", " + a[1] + " ) is " + LCM(a[0], a[1]));
        }

    }

}
