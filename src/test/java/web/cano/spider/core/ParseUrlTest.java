package web.cano.spider.core;

import org.junit.Test;
import web.cano.spider.Page;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class ParseUrlTest extends DefaultPageProcessor implements TestableProcessor {
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
        parseUrls(page, "//div[@class=\"articleList\"]/div//a/@href", PageProcessType.XPath);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testParseUrl() throws Exception{
        PageProcessor processor = new ParseUrlTest();
        Spider.create(processor)
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("blog_sina.html", true)) //网上url：http://blog.sina.com.cn/s/articlelist_1487828712_0_1.html
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        List<Page> pages = testableProcessor.getPage().getTargetPages();
        assertThat(pages.size()).isEqualTo(50);
        assertThat(pages.get(0).getUrl()).isEqualToIgnoringCase("http://blog.sina.com.cn/s/blog_58ae76e80100to5q.html");
        assertThat(pages.get(9).getUrl()).isEqualToIgnoringCase("http://blog.sina.com.cn/s/blog_58ae76e80100msy6.html");
        assertThat(pages.get(49).getUrl()).isEqualToIgnoringCase("http://blog.sina.com.cn/s/blog_58ae76e80100g8cy.html");

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
