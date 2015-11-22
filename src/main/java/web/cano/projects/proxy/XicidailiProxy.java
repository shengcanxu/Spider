package web.cano.projects.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.MysqlPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.scheduler.RedisScheduler;

/**
 * 从 http://www.xicidaili.com/nn 上面爬取最新的代理信息
 */
public class XicidailiProxy extends DefaultPageProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Site site = Site
            .me()
            .setDomain("xicidaili.com")
            .addHeader("Referer", "http://www.xicidaili.com/")
            .setDeepFirst(true)
            .setSleepTime(3000)
            .setShouldSplitToMultipleValues(true)
            .addUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        switch(page.getDepth()){
            case 0:
                PageItem countryItem = new PageItem("country", PageItem.PageItemType.STRING,true,true);
                countryItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[2]/img/@alt",PageProcessType.XPath,countryItem);
                putItem(page,countryItem);

                PageItem ipItem = new PageItem("ip", PageItem.PageItemType.STRING,true,true);
                ipItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[3]/text()",PageProcessType.XPath,ipItem);
                putItem(page,ipItem);

                PageItem portItem = new PageItem("port", PageItem.PageItemType.STRING,true,true);
                portItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[4]/text()",PageProcessType.XPath,portItem);
                putItem(page,portItem);

                //TODO： 不能爬取到最终的结果，只能爬取父节点，因为不能用统一的xpath来爬取
                PageItem placeItem = new PageItem("place", PageItem.PageItemType.STRING,true,true);
                placeItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[5]",PageProcessType.XPath,placeItem);
                putItem(page,placeItem);

                PageItem nimingItem = new PageItem("niming", PageItem.PageItemType.STRING,true,true);
                nimingItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[6]/text()",PageProcessType.XPath,nimingItem);
                putItem(page,nimingItem);

                PageItem typeItem = new PageItem("type", PageItem.PageItemType.STRING,true,true);
                typeItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[7]/text()",PageProcessType.XPath,typeItem);
                putItem(page,typeItem);

                PageItem speedItem = new PageItem("speed", PageItem.PageItemType.STRING,true,true);
                speedItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[8]/div/@title",PageProcessType.XPath,speedItem);
                putItem(page,speedItem);

                PageItem connectTimeItem = new PageItem("connecttime", PageItem.PageItemType.STRING,true,true);
                connectTimeItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[9]/div/@title",PageProcessType.XPath,connectTimeItem);
                putItem(page,connectTimeItem);

                PageItem updateDateItem = new PageItem("updatedate", PageItem.PageItemType.STRING,true,true);
                updateDateItem = extractBy(page,"//*[@id=\"ip_list\"]/tbody/tr/td[10]/text()",PageProcessType.XPath,updateDateItem);
                putItem(page,updateDateItem);

                parseNextUrls(page,"//*[@id=\"body\"]/div[@class=\"pagination\"]/a/@href",PageProcessType.XPath);
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

        PageProcessor processor = new XicidailiProxy();
        Spider spider = Spider.create(processor);

        if(args.length > 1 && args[1].length() > 0){
            spider.setUUID(args[1]);
            System.out.println("spider Name: " + args[1]);
        }

        Site site = processor.getSite();
        if(args.length > 2 && args[2].length() > 0){
            site.setDeepFirst(Boolean.parseBoolean(args[2]));
        }

        spider.setScheduler(new RedisScheduler("127.0.0.1", site, true))
                .addPipeline(new MysqlPipeline(true,true))
                .addStartPage(new Page("http://www.xicidaili.com/nn/1"))
                .thread(threadNum).run();
    }
}
