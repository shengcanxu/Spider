package web.cano.spider.utils;

import java.io.*;
import java.util.*;

/**
 * Created by cano on 2015/5/31.tring
 * 从文件获取连接列表等操作
 */
public class FileHelper {

    /**
     * 从文件中读取内容到list
     * @return
     */
    public static List<String> readUrlsFromFile(File file){
        List<String> urls = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                urls.add(line);
            }
            br.close();
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return urls;
    }

    /**
     * 将list内容写入到file
     * @param urls
     */
    public static void writeUrlsToFile(List<String> urls, File file){
        try{
            FileOutputStream fos = new FileOutputStream(file);
            for(String url : urls){
                url = url + "\n";
                fos.write(url.getBytes());
            }
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 去除列表中的重复项
     */
    public static void removeDuplicate(File file){
        System.out.println("read content from file");
        List<String> urls = readUrlsFromFile(file);

        System.out.println("\nremoving duplicate\n");
        Set<String> set  = new HashSet<String>();
        set.addAll(urls);
        List<String> newUrlsList = new ArrayList<String>();
        newUrlsList.addAll(set);

        System.out.println("\nwrite content to file\n");
        writeUrlsToFile(newUrlsList, file);
    }

    /**
     * 比较两个文件的差异， LIst[0]存储from有to没有的， List[1]存储from没有to有的
     * @return
     */
    public static List<String>[] diffFile(File fromFile, File toFile){
        List<String>[] results = new List[2];
        List<String> fromHasList = new ArrayList<String>();
        List<String> toHasList = new ArrayList<String>();

        System.out.println("reading from files\n");
        List<String> from = readUrlsFromFile(fromFile);
        List<String> to = readUrlsFromFile(toFile);
        List<String> toLinkedList = new LinkedList<String>();
        toLinkedList.addAll(to);

        System.out.println("sorting...\n");
        from.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        toLinkedList.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        System.out.println("\ndoing diff...\n");
        for(int i=0; i<from.size(); i++){
            String str = from.get(i);
            int j;
            int len = toLinkedList.size();
            for(j=0; j<len; j++){
                if(str.equals(toLinkedList.get(j))){
                    toLinkedList.remove(j);
                    System.out.println("equal");
                    break;
                }
            }
            if(j>=len){
                System.out.println("not equal");
                fromHasList.add(str);
            }
        }
        toHasList.addAll(toLinkedList);

        results[0] = fromHasList;
        results[1] = toHasList;
        System.out.println("\ndiff finished\n");
        return results;
    }

    /**
     * 将文件里面的内容排序
     */
    public static void sortFile(File file){
        System.out.println("reading from files\n");
        List<String> list = readUrlsFromFile(file);

        System.out.println("sorting\n");
        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        System.out.println("writing from files\n");
        writeUrlsToFile(list,file);
    }

}
