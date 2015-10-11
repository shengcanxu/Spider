package web.cano.projects.douguo;

import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.SaveSourceFilePipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.scheduler.RedisScheduler;

/**
 * Created by cano on 2015/5/28.
 */

public class DouguocaidanContent extends DefaultPageProcessor {

    private Site site = Site
            .me()
            .setDomain("douguo.com")
            .addHeader("Referer", "http://www.douguo.com/")
            .setDeepFirst(false)
            .setSleepTime(3000)
            .setLocalSiteCopyLocation("D:\\software\\redis\\data\\contentsourcefile\\")
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        PageItem pageUrl = new PageItem("pageUrl", PageItem.PageItemType.STRING,true,false);
        pageUrl = extractByUrl(page,".*",pageUrl);
        putItem(page,pageUrl);

        PageItem title = new PageItem("title", PageItem.PageItemType.STRING,true,false);
        title = extratBy(page,"//*[@id=\"page_cm_id\"]/text()",PageProcessType.XPath,title);
        putItem(page,title);

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

        PageProcessor processor = new DouguocaidanContent();
        Spider.create(processor)
                .setScheduler(new RedisScheduler("127.0.0.1", processor.getSite(), true))
                .addPipeline(new SaveSourceFilePipeline("D:/software/redis/data/contentsourcefile/"))
                //.addPipeline(new MysqlPipeline(true))
                .addStartPage(new Page("http://www.douguo.com/cookbook/1257340.html"))
                .thread(threadNum).run();
    }

//    @ExtractByUrl(regrex = "")
//    private String pageUrl;
//
//    @ExtractBy(value = "//*[@id=\"page_cm_id\"]")
//    private String title;
//
//    @ExtractBy(value = "//*[@id=\"main\"]//div[@class=\"melef clearfix\"]/span[1]/text()")
//    @CustomFunction(name = "removeCreateText")
//    private String createDate;
//
//    @ExtractBy(value = "//*[@id=\"main\"]//div[@class=\"melef clearfix\"]/span[2]/text()")
//    @CustomFunction(name = "removeUpdateText")
//    private String updateDate;
//
//    @ExtractBy(value = "//*[@id=\"main\"]//div[@class=\"melef clearfix\"]/span[3]/text()")
//    @FieldType(type = FieldType.Type.INT)
//    @CustomFunction(name = "removeReadsText")
//    private int reads;
//
//    @ExtractBy(value = "//*[@id=\"main\"]//div[@class=\"melef clearfix\"]/span/font/text()")
//    @FieldType(type = FieldType.Type.INT)
//    private int souchang;
//
//    @ExtractBy(value = "//*[@id=\"main\"]/div[@class=\"meview\"]//p/text()")
//    @FieldType(type = FieldType.Type.TEXT)
//    private String description;
//
//    @ExtractBy(value = "//*[@id=\"main\"]/div[@class=\"mecai\"]/div/div/a/@href")
//    @FieldType(type = FieldType.Type.TEXT)
//    private List<String> caipus;

    public Object removeCreateText(Object value, Page page) {
        if (value instanceof String) {
            String str = (String) value;
            str = str.replace("创建时间：", "").trim();
            return str;
        }
        return value;
    }

    public Object removeUpdateText(Object value, Page page) {
        if (value instanceof String) {
            String str = (String) value;
            str = str.replace("最后更新：", "").trim();
            return str;
        }
        return value;
    }

    public Object removeReadsText(Object value, Page page) {
        if (value instanceof String) {
            String str = (String) value;
            str = str.replace("浏览：", "").trim();
            return str;
        }
        return value;
    }

}
