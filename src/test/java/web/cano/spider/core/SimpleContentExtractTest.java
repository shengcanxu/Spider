package web.cano.spider.core;

import org.junit.Test;
import web.cano.spider.Page;
import web.cano.spider.PageItems;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.PageItem;
import web.cano.spider.processor.TestableProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class SimpleContentExtractTest extends DefaultPageProcessor implements TestableProcessor {
    private Page page;
    private Spider spider;



    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setMaxDeep(1)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        PageItem title = new PageItem("title", PageItem.PageItemType.STRING,true,false);
        title = extractBy(page, "//div[@class='articalTitle']/h2/text()", PageProcessType.XPath, title);
        putItem(page, title);

        PageItem tag = new PageItem("tag",PageItem.PageItemType.STRING,true,false);
        tag = extractBy(page, "//h3/a/text()", PageProcessType.XPath, tag);
        putItem(page, tag);

        PageItem date = new PageItem("date", PageItem.PageItemType.STRING,true, false);
        date = extractBy(page, "//div[@id='articlebody']//span[@class='time SG_txtc']/text()", PageProcessType.XPath, date);
        date = formatValue(date,"\\((.*)\\)");
        putItem(page, date);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new SimpleContentExtractTest();
        Spider.create(processor)
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("blog_sina_detail.html").setTest(true)) //网上url: http://blog.sina.com.cn/s/blog_58ae76e80100pjln.html
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        PageItems pageItems = testableProcessor.getPage().getPageItems();

        assertThat(pageItems.getItems().size()).isEqualTo(3);
        assertThat(pageItems.getPageItemByName("title").getItemValue().toString()).isEqualToIgnoringCase("编程为什么有趣？ 太有共鸣了");
        assertThat(pageItems.getPageItemByName("tag").getItemValue().toString()).isEqualToIgnoringCase("it");
        assertThat(pageItems.getPageItemByName("date").getItemValue().toString()).isEqualToIgnoringCase("2011-03-24 16:04:08");
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
