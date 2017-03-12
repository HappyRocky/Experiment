package utils;

import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.xml.transform.Templates;

//此类包含了7种排序算法，默认为从小到大排序
public class SortMethods {
    private int n;
    private double[] value;
    private double[] result;

    public SortMethods(double[] a)
    {
        n = a.length;
        value = new double[n];
        result = new double[n];
        System.arraycopy(a, 0, value, 0, n);
    }

    //
    //选择排序：不稳定，时间复杂度 O(n^2)
    //
    public double[] SelectionSort(){
        for(int i=0;i<n-1;i++){  //共length-1次循环，第i此循环之后，前i个记录的位置已经是正确的
            int idx = i;
            for(int j=idx+1;j<n;j++){ //寻找value[i+1..n]中的最小值
                if(value[j]<value[idx])
                    idx = j;
            }
            if(idx!=i){
                double temp = value[i];
                value[i] = value[idx];
                value[idx] = temp;
            }
        }
        return value;
    }

    //
    //插入排序：稳定，时间复杂度 O(n^2)
    //
    public double[] InsertSort(){
        for(int i=1;i<n;i++){
            for(int j=i-1;j>=0;j--){
                if(value[j]>value[j+1]){
                    double temp = value[j];
                    value[j] = value[j+1];
                    value[j+1] = temp;
                }
            }
        }
        return value;
    }

    //
    //冒泡排序：稳定，时间复杂度 O(n^2)
    //
    public double[] BubbleSort(){
        for(int i=1;i<n;i++){
            for(int j=0;j<n-i;j++){
                if(value[j]>value[j+1]){
                    double temp = value[j];
                    value[j] = value[j+1];
                    value[j+1] = temp;
                }
            }
        }
        return value;
    }

    //
    //堆排序：不稳定，时间复杂度 O(nlogn)
    //
    public double[] HeapSort(){
        CreateHeap(value,value.length);
        for(int i=0;i<value.length;i++){
            result[i] = value[0];
            value[0] = value[value.length-1-i];
            value[value.length-1-i] = result[i];
            CreateHeap(value, value.length-i-1);
        }
        return result;
    }
    public double[] HeapSort_Dec(){ //降序排列
        CreateHeap(value,value.length);
        for(int i=0;i<value.length;i++){
            double temp = value[0];
            value[0] = value[value.length-1-i];
            value[value.length-1-i] = temp;
            CreateHeap(value, value.length-i-1);
        }
        return value;
    }
    public void CreateHeap(double[] a,int len){  //对数组a的前len个元素创建最小堆
        if(len==1) return;
        int nodeNum = (int) (Math.pow(2,(int)(Math.log(len)/Math.log(2)))-1); //可能含有子节点的节点数
        for(int i=nodeNum-1;i>=0;i--){   //左右子节点的下标为i*2+1，i*2+2
            if(i*2+1<len && a[i*2+1]<a[i]){   //左节点小于父节点
                if(i*2+2<len){   //存在右节点
                    if(a[i*2+1]<a[i*2+2]){
                        double temp = a[i*2+1];
                        a[i*2+1] = a[i];
                        a[i] = temp;
                    }
                    else{
                        double temp = a[i*2+2];
                        a[i*2+2] = a[i];
                        a[i] = temp;
                    }
                }
                else{  //不存在右节点
                    double temp = a[i*2+1];
                    a[i*2+1] = a[i];
                    a[i] = temp;
                }
            }
            else{
                if(i*2+2<len && a[i*2+2]<a[i]){   //右节点小于父节点
                    double temp = a[i*2+2];
                    a[i*2+2] = a[i];
                    a[i] = temp;
                }
            }
        }
    }

    //
    //归并排序：稳定，时间复杂度 O(nlog n)
    //
    public double[] MergeSort(){
        MergeSort(0, value.length-1);
        return value;
    }
    public void MergeSort(int first,int last){  //递归进行归并排序
        if(first==last) return;
        if(first+1==last){
            if(value[first]>value[last]){
                double temp = value[first];
                value[first] = value[last];
                value[last] = temp;
            }
            return;
        }
        if(first+1<last){
            int mid = (first+last)/2;
            MergeSort(first,mid);
            MergeSort(mid+1,last);
            Merge(first,mid,last);
        }
    }
    public void Merge(int first,int mid,int last){ //将两个已经排好序的数组归并到一个数组
        if(first==mid || mid==last) return;
        int len = last-first+1;
        double[] temp = new double[len];
        int i=first,j=mid+1,k=0;
        while(true){
            if(value[i]<value[j]){
                temp[k++] = value[i];
                i++;
            }
            else {
                temp[k++] = value[j];
                j++;
            }
            if(i>mid || j>last)
                break;
        }
        if(i<=mid)
            for(int a=i;a<=mid;a++)
                temp[k++] = value[a];
        else {
            for(int a=j;a<=last;a++)
                temp[k++] = value[a];
        }
        for(int a=0;a<len;a++)
            value[first+a] = temp[a];
    }

    //
    //快速排序：不稳定，时间复杂度 最理想 O(nlogn) 最差时间O(n^2)
    //
    public double[] QSort(){
        QSort(0,value.length-1);
        return value;
    }
    public void QSort(int first,int last){
        if(first>=last) return;
        int i=first,j=last;
        double key = value[i];
        while(true){
            for(;j>=i;j--){
                if(value[j]<key){
                    double temp = value[i];
                    value[i] = value[j];
                    value[j] = temp;
                    break;
                }
            }
            if(i>=j){
                j=i;
                break;
            }
            for(;i<=j;i++){
                if(value[i]>key){
                    double temp = value[i];
                    value[i] = value[j];
                    value[j] = temp;
                    break;
                }
            }
            if(i>=j){
                i=j;
                break;
            }
        }
        QSort(first,i);
        QSort(i+1,last);
    }

    //
    //希尔排序：不稳定，时间复杂度 平均时间 O(nlogn) 最差时间O(n^s) 1<s<2
    //
    public double[] ShellSort(){
        int n = value.length;
        for(int gap=n/2;gap>0;gap/=2){
            for(int i=gap;i<n;i++){
                for(int j=i-gap;j>=0 && value[j]>value[j+gap];j-=gap){
                    double temp = value[j];
                    value[j] = value[j+gap];
                    value[j+gap] = temp;
                }
            }
        }
        return value;
    }

    //
    //记数排序：不稳定排序，Ο(n+k)（其中k是整数的范围），只适用于整数
    //
    public double[] CountingSort(){
        int maxInt = (int)GetMax(value);
        int minInt = (int)GetMin(value);
        double[] store = new double[maxInt-minInt+1];
        for(int i=0;i<value.length;i++){
            store[(int)value[i]-minInt]++;
        }
        int j=0;
        for(int i=0;i<store.length;i++){
            if(store[i]==0) continue;
            int postJ = j+(int)store[i];
            for(;j<postJ;j++)
                value[j] = i+minInt;
        }
        return value;
    }
    public double GetMax(double[] a){  //得到数组a的最大值
        int len = a.length;
        if(len==1) return(a[0]);
        double tempMax = a[0];
        for(int i=1;i<len;i++){
            if(a[i]>tempMax)
                tempMax = a[i];
        }
        return tempMax;
    }
    public double GetMin(double[] a){  //得到数组a的最小值
        int len = a.length;
        if(len==1) return(a[0]);
        double tempMin = a[0];
        for(int i=1;i<len;i++){
            if(a[i]<tempMin)
                tempMin = a[i];
        }
        return tempMin;
    }
}
