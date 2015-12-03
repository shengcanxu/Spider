package web.cano.projects.woshipm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.MysqlPipeline;
import web.cano.spider.pipeline.SaveResourcePipeline;
import web.cano.spider.pipeline.SaveSourceFilePipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.scheduler.RedisScheduler;
import web.cano.spider.utils.ProxyList;

import java.io.*;

/**
 * Created by cano on 2015/5/28.
 */

public class woshipmUrls extends DefaultPageProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Writer writer;
    private Writer redisWriter;

    private Site site = Site
            .me()
            .setDomain("woshipm.com")
            .addHeader("Referer", "http://www.woshipm.com/")
            .setDeepFirst(true)
            .setSleepTime(3000)
            .setMaxDeep(1)
            //.setLocalSiteCopyLocation("D:\\software\\redis\\data\\woshipmcontentsourcefile\\")
            .addUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31")
            .setHttpProxyPool(ProxyList.getProxyList());

    public woshipmUrls(){
        try {
            writer = new OutputStreamWriter(new FileOutputStream(new File("D:\\software\\redis\\data\\woshipmurls.txt"),true));
            redisWriter = new OutputStreamWriter(new FileOutputStream(new File("D:\\software\\redis\\data\\woshipmredisqueue.txt"),true));
        } catch (IOException e) {
            logger.error("get writer error", e);
        }
    }

    @Override
    public void process(Page page) {
        switch(page.getDepth()){
            case 0:
                parseUrls(page, "//*[@id=\"home-list\"]/div/dl/dd//h3/a/@href", PageProcessType.XPath);
                break;
            case 1:
                try {
                    String c = page.getUrl() + "\n";
                    writer.write(c);
                    writer.flush();
                    String d = page.toJson() + "\n";
                    redisWriter.write(d);
                    redisWriter.flush();
                } catch (IOException e) {
                    logger.error("write to file error", e);
                }
                break;
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        int threadNum = 1;
        if(args.length > 0){
            threadNum = Integer.parseInt(args[0]);
        }
        System.out.println("thread NO.: " + threadNum);

        PageProcessor processor = new woshipmUrls();
        Spider spider = Spider.create(processor);

        if(args.length > 1 && args[1].length() > 0){
            spider.setUUID(args[1]);
            System.out.println("spider Name: " + args[1]);
        }

        spider.setScheduler(new RedisScheduler("127.0.0.1", processor.getSite(), true))
                .addPipeline(new SaveSourceFilePipeline("D:/software/redis/data/woshipmurlssourcefile/"));

        //2145
        for(int i=1; i<10; i++){
            spider.addStartPage(new Page("http://www.woshipm.com/page/" + i + "?nocache"));
        }
        spider.thread(threadNum).run();
    }

}
