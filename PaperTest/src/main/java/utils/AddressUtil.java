package utils;

import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @className:AddressUtil.java
 * @classDescription: 得到本机ip地址
 * @author:gongyanshang
 * @createTime:2016年11月4日
 */
public class AddressUtil {

     /**
      * 获取本机ip
      * @return
      */
     public static Set<String> getIp(){
         Set<String> set = new HashSet<String>();
         try {
             for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                 NetworkInterface intf = en.nextElement();
                 for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                     InetAddress inetAddress = enumIpAddr.nextElement();
                     if (inetAddress instanceof Inet4Address  // 是 ipv4
                                && (inetAddress.isLinkLocalAddress() //本地连接地址
                                || inetAddress.isSiteLocalAddress() //地区本地地址
                                || inetAddress.isMulticastAddress() //广播地址
                                || inetAddress.isMCGlobal()) //全球范围的广播地址
                                ) {
                          set.add(inetAddress.getHostAddress().toString());
                           }
                 }
             }
         } catch (SocketException ex) {
          ex.printStackTrace();
         }
         return set;
     }
     
     /**
      * 得到ip地址的String，多个地址用/分割
      * @return
      */
     public static String getIpString(){
    	 String result = "";
    	 Set<String> ipSet = getIp();
         for (String curIp : ipSet) {
        	 result += ("/"+curIp);
         }
         return result;
     }

     public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, UnknownHostException{
           System.out.println(InetAddress.getLocalHost().getHostName() + AddressUtil.getIpString());
     }
}
