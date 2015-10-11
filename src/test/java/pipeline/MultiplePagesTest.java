package pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import web.cano.spider.*;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class MultiplePagesTest extends DefaultPageProcessor implements TestableProcessor {
    private Page page;
    private Spider spider;



    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setMaxDeep(2)
            .setUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        if(!page.isSubMultiplePage()) {
            Page subPage1 = new Page("163news_1.html", page).setTest(true);
            subPage1.setMultiplePageKey(DigestUtils.md5Hex(page.getUrl())).setMultiplePageIndex(1);
            Page subPage2 = new Page("163news_2.html", page).setTest(true);
            subPage2.setMultiplePageKey(DigestUtils.md5Hex(page.getUrl())).setMultiplePageIndex(2);
            page.setMultiplePageNumber(2);
            page.addTargetPage(subPage1);
            page.addTargetPage(subPage2);
            page.setMultiplePageItemName("content");

            PageItem content = new PageItem("content", PageItem.PageItemType.STRING, true, false);
            content = extractBy(page, "//div[@id=\"endText\"]", PageProcessType.XPath, content);
            putItem(page,content);
        }else{
            PageItem content = new PageItem("content", PageItem.PageItemType.STRING, true, false);
            content = extractBy(page, "//div[@id=\"endText\"]", PageProcessType.XPath, content);
            page.setMultiplePageItemName("content");
            putItem(page,content);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new MultiplePagesTest();
        Spider.create(processor)
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("163news_0.html").setTest(true)) //网上url: http://news.163.com/13/0802/05/958I1E330001124J.html
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        PageItems pageItems = testableProcessor.getPage().getPageItems();

        assertThat(pageItems.getItems().size()).isEqualTo(1);
        assertThat(pageItems.getPageItemByName("content").getItemValue().toString().length()).isGreaterThan(7000);
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
