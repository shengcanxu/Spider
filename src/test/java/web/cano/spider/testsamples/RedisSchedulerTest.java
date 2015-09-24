package web.cano.spider.testsamples;

import org.junit.Test;
import web.cano.spider.Page;
import web.cano.spider.PageItems;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.PageProcessorItem;
import web.cano.spider.processor.TestableProcessor;
import web.cano.spider.scheduler.RedisScheduler;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class RedisSchedulerTest extends DefaultPageProcessor implements TestableProcessor {

    public static final String URL_LIST = "http://blog\\.sina\\.com\\.cn/s/articlelist_1487828712_0_\\d+\\.html";

    public static final String URL_POST = "http://blog\\.sina\\.com\\.cn/s/blog_\\w+\\.html";

    private Page page;
    private Spider spider;



    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        PageProcessorItem title = new PageProcessorItem("title", PageItems.PageItemsType.STRING,true,false);
        title = extratBy(page,"//div[@class='articalTitle']/h2/text()",PageProcessType.XPath,title);
        putItem(page, title);

        PageProcessorItem tag = new PageProcessorItem("tag",PageItems.PageItemsType.STRING,true,false);
        tag = extratBy(page,"//h3/a/text()",PageProcessType.XPath,tag);
        putItem(page, tag);

        PageProcessorItem date = new PageProcessorItem("date", PageItems.PageItemsType.STRING,true, false);
        date = extratBy(page, "//div[@id='articlebody']//span[@class='time SG_txtc']/text()", PageProcessType.XPath,date);
        date = formatValue(date,"\\((.*)\\)");
        putItem(page, date);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new RedisSchedulerTest();
        Spider.create(processor)
                .setScheduler(new RedisScheduler("127.0.0.1",processor.getSite(),true))
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("blog_sina_detail.html", true)) //网上url: http://blog.sina.com.cn/s/blog_58ae76e80100pjln.html
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        PageItems pageItems = testableProcessor.getPage().getPageItems();
        Map<String, String> items = pageItems.getAllItems();

        assertThat(items.size()).isEqualTo(3);
        assertThat(items.get("title")).isEqualToIgnoringCase("编程为什么有趣？ 太有共鸣了");
        assertThat(items.get("tag")).isEqualToIgnoringCase("it");
        assertThat(items.get("date")).isEqualToIgnoringCase("2011-03-24 16:04:08");
    }

    @Override
    public void testCallback(Spider spider, Page page) {
        this.page = page;
        this.spider = spider;
    }

    @Override
    public Page getPage() {
        return this.page;
    }

    @Override
    public Spider getSpider() {
        return this.spider;
    }
}
