package web.cano.projects.douguo;

import web.cano.spider.utils.RedisUtils;

/**
 * Created by cano on 2015/10/9.
 */
public class DouguoHelper {
    public DouguoHelper(){

    }

    public static void main(String[] args) {
        RedisUtils.redisSetToFile("D:\\software\\redis\\data\\douguourlset.txt","set_DouguocaidanUrls");
    }
}
