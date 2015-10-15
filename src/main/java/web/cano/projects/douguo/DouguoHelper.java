package web.cano.projects.douguo;

import web.cano.spider.utils.RedisUtils;

/**
 * Created by cano on 2015/10/9.
 */
public class DouguoHelper {
    public DouguoHelper(){

    }

    public static void main(String[] args) {
        //RedisUtils.redisSetToFile("D:\\software\\redis\\data\\douguourlset.txt","set_DouguocaidanUrls");

//        List<String>[] diff = FileHelper.diffFile(new File("D:\\software\\redis\\data\\douguourls.txt"),
//                new File("D:\\software\\redis\\data\\douguo20151009\\douguourls.txt"));
//        FileHelper.writeUrlsToFile(diff[0],new File("D:\\software\\redis\\data\\diff1.txt"));
//        FileHelper.writeUrlsToFile(diff[1],new File("D:\\software\\redis\\data\\diff2.txt"));

        RedisUtils.fileToRedisList("D:\\software\\redis\\data\\douguourlsredis.txt","queue_douguo");

    }
}
