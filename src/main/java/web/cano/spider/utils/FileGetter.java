package web.cano.spider.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cano on 2015/5/31.tring
 * 从文件获取连接列表等操作
 */
public class FileGetter {

    /**
     * 从文件中读取内容到list
     * @param filePath
     * @return
     */
    public static List<String> getUrlsFromFile(String filePath){
        List<String> urls = new ArrayList<String>();
        try {
            File fin = new File(filePath);
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                urls.add(line);
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return urls;
    }

}