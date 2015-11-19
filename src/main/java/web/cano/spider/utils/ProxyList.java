package web.cano.spider.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by canoxu on 2015/11/19.
 */
public class ProxyList {

    /**
     * String[0]:ip, String[1]:port
     * get from http://www.xicidaili.com/nn
     */
    public static List<String[]> getProxyList(){
        List<String[]> proxyList = new ArrayList<String[]>();

        proxyList.add(toArray("220.185.103.47","3128"));
        proxyList.add(toArray("112.25.41.136","80"));
        proxyList.add(toArray("120.198.236.10","80"));
        proxyList.add(toArray("218.200.66.196","8080"));
        proxyList.add(toArray("36.235.45.249","8080"));
        proxyList.add(toArray("182.90.15.147","80"));
        proxyList.add(toArray("182.90.35.96","80"));
        proxyList.add(toArray("117.24.91.65","80"));
        proxyList.add(toArray("113.110.227.222","8888"));
        proxyList.add(toArray("171.39.233.36","80"));

        return proxyList;
    }

    private static String[] toArray(String ip, String port){
        String[] str = new String[2];
        str[0] = ip;
        str[1] = port;
        return str;
    }
}
