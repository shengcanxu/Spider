package web.cano.projects.douguo;

import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.SaveSourceFilePipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.scheduler.RedisScheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cano on 2015/5/28.
 * 获得豆果里面的菜单列表里面的菜单链接
 */

public class DouguocaidanUrls extends DefaultPageProcessor {
    private FileOutputStream fos;
    private FileOutputStream redisFos;

    public DouguocaidanUrls() {
        try {
            fos = new FileOutputStream(new File("D:\\software\\redis\\data\\douguourls.txt"),true);
            redisFos = new FileOutputStream(new File("D:\\software\\redis\\data\\douguourlsredis.txt"),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Site site = Site
            .me()
            .setDomain("douguo.com")
            .addHeader("Referer", "http://www.douguo.com/")
            .setDeepFirst(true)
            .setMaxDeep(2)
            .setSleepTime(3000)
            //.setLocalSiteCopyLocation("D:\\software\\redis\\data\\candanurlssourcefile\\")
            .addUserAgent("Mozilla/3.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31")
            .addUserAgent("Mozilla/4.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31")
            .addUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        switch(page.getDepth()){
            case 0:
                parseUrls(page,"//div[@class=\"sortf\"]//ul/li/a/text()",PageProcessType.XPath);
                break;
            case 1:
                PageItem pageItem = new PageItem("fenlei", PageItem.PageItemType.STRING,true,false);
                pageItem = extractBy(page,"//h1/text()",PageProcessType.XPath,pageItem);
                putItem(page,pageItem);

                parseUrls(page,"//*[@id=\"main\"]//h3/a/@href",PageProcessType.XPath);
                parseNextUrls(page,"//div[@class=\"pagination\"]/span/a/@href",PageProcessType.XPath);
                break;
            case 2:
                try {
                    String c = page.getUrl() + "\n";
                    fos.write(c.getBytes());
                    String d = page.toJson() + "\n";
                    redisFos.write(d.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
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
        if(args.length > 0 && args[0].length() > 0){
            threadNum = Integer.parseInt(args[0]);
        }
        System.out.println("thread NO.: " + threadNum);

        PageProcessor processor = new DouguocaidanUrls();
        Spider spider = Spider.create(processor);

        if(args.length > 1 && args[1].length() > 0){
            spider.setUUID(args[1]);
            System.out.println("spider Name: " + args[1]);
        }

        spider.setScheduler(new RedisScheduler("127.0.0.1",processor.getSite(),true))
                .addPipeline(new SaveSourceFilePipeline("D:/software/redis/data/candanurlssourcefile/"))
                .addStartPage(new Page("http://www.douguo.com/caipu/fenlei"))
                .thread(threadNum).run();
    }
}
