package web.cano.spider.core;

import org.junit.Test;
import web.cano.spider.*;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class LocalSiteExtractTest extends DefaultPageProcessor implements TestableProcessor {
    private Page page;
    private Spider spider;

    private Site site = Site
            .me()
            .setDomain("douguo.com")
            .setSleepTime(3000)
            .setMaxDeep(1)
            .setLocalSiteCopyLocation(new File(this.getClass().getClassLoader().getResource("httpwwwdouguocomcookbook1257340html").getFile()).getParent() + "\\")
                    .addUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        PageItem pageUrl = new PageItem("pageUrl", PageItem.PageItemType.STRING,true,false);
        pageUrl = extractByUrl(page, ".*", pageUrl);
        putItem(page,pageUrl);

        PageItem title = new PageItem("title", PageItem.PageItemType.STRING,true,false);
        title = extractBy(page, "//*[@id=\"page_cm_id\"]/text()", PageProcessType.XPath, title);
        putItem(page,title);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new LocalSiteExtractTest();
        Spider.create(processor)
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("http://www.douguo.com/cookbook/1257340.html")) //网上url: http://www.douguo.com/cookbook/1257340.html
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        Page page = testableProcessor.getPage();
        List<PageItem> pageItems = page.getPageItems();

        assertThat(pageItems.size()).isEqualTo(2);
        assertThat(page.getPageItemByName("title").getItemValue().toString()).isEqualToIgnoringCase("宫保鸡丁");
        assertThat(page.getPageItemByName("pageUrl").getItemValue().toString()).isEqualToIgnoringCase("http://www.douguo.com/cookbook/1257340.html");
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
