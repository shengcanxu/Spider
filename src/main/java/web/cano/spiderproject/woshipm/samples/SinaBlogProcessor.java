package web.cano.spiderproject.woshipm.samples;

import web.cano.spider.Page;
import web.cano.spider.PageItems;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.MysqlPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.PageProcessorItem;
import web.cano.spider.scheduler.RedisScheduler;

/**
 * @author code4crafter@gmail.com <br>
 */
public class SinaBlogProcessor extends DefaultPageProcessor {

    public static final String URL_LIST = "http://blog\\.sina\\.com\\.cn/s/articlelist_1487828712_0_\\d+\\.html";

    public static final String URL_POST = "http://blog\\.sina\\.com\\.cn/s/blog_\\w+\\.html";

    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        //列表页
        if (isPageMatchByUrl(page,URL_LIST)) {
            parseUrls(page,"//div[@class=\"articleList\"]/div//a/@href", PageProcessType.XPath);
            page.setSkip(true);
            //文章页
        } else {
            PageProcessorItem title = new PageProcessorItem("title",PageItems.PageItemsType.STRING,true,false);
            title = extratBy(page,"//div[@class='articalTitle']/h2/text()",PageProcessType.XPath,title);
            putItem(page, title);

            PageProcessorItem tag = new PageProcessorItem("tag",PageItems.PageItemsType.STRING,true,false);
            tag = extratBy(page,"//h3/a/text()",PageProcessType.XPath,tag);
            putItem(page, tag);

            PageProcessorItem date = new PageProcessorItem("date", PageItems.PageItemsType.STRING,true, false);
            date = extratBy(page, "//div[@id='articlebody']//span[@class='time SG_txtc']/text()", PageProcessType.XPath,date);
            date = formatValue(date,"\\((.*)\\)");
            putItem(page,date);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        PageProcessor processor = new SinaBlogProcessor();
        Spider.create(processor)
                .setScheduler(new RedisScheduler("127.0.0.1",processor.getSite(),true))
                .setRecoverUrlSet(true)
                .addPipeline(new MysqlPipeline(true))
                .addStartPage(new Page("http://blog.sina.com.cn/s/articlelist_1487828712_0_1.html"))
                .run();
    }
}
