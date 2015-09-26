package web.cano.spider.testsamples;

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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class MultiValueExtractTest extends DefaultPageProcessor implements TestableProcessor {
    private Page page;
    private Spider spider;



    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setShouldSplitToMultipleValues(true)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        PageItem title = new PageItem("title", PageItem.PageItemType.STRING,true,true);
        title = extratBy(page,"//span[@class=\"atc_title\"]/a/text()",PageProcessType.XPath,title);
        putItem(page, title);

        PageItem url = new PageItem("url",PageItem.PageItemType.STRING,true,true);
        url = extratBy(page, "//span[@class=\"atc_title\"]/a/@href", PageProcessType.XPath, url);
        putItem(page, url);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new MultiValueExtractTest();
        Spider.create(processor)
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("blog_sina.html", true)) //网上url：http://blog.sina.com.cn/s/articlelist_1487828712_0_1.html
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        PageItems pageItems = testableProcessor.getPage().getPageItems();

        assertThat(pageItems.getItems().size()).isEqualTo(2);
        List<String> titles = (List<String>) pageItems.getPageItemByName("title").getItemValue();
        List<String> urls = (List<String>) pageItems.getPageItemByName("url").getItemValue();

        assertThat(titles.size()).isEqualTo(50);
        assertThat(titles.get(0)).isEqualToIgnoringCase("zzSed学习笔记");
        assertThat(titles.get(10)).isEqualToIgnoringCase("一些IR相关概念");
        assertThat(titles.get(48)).isEqualToIgnoringCase("分治算法的一点思考--为什么大多使…");
        assertThat(urls.size()).isEqualTo(50);
        assertThat(urls.get(0)).isEqualToIgnoringCase("http://blog.sina.com.cn/s/blog_58ae76e80100to5q.html");
        assertThat(urls.get(10)).isEqualToIgnoringCase("http://blog.sina.com.cn/s/blog_58ae76e80100mfqf.html");
        assertThat(urls.get(49)).isEqualToIgnoringCase("http://blog.sina.com.cn/s/blog_58ae76e80100g8cy.html");
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
