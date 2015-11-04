package pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import web.cano.spider.*;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class SubPagesTest extends DefaultPageProcessor implements TestableProcessor {
    private Page page;
    private Spider spider;



    private Site site = Site
            .me()
            .setDomain("blog.sina.com.cn")
            .setSleepTime(3000)
            .setMaxDeep(2)
            .addUserAgent(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

    @Override
    public void process(Page page) {
        if(!page.isSubPage()) {
            Page subPage = new Page("ximalaya339173subpage.html", page).setTest(true);
            subPage.setParentPageKey(DigestUtils.md5Hex(page.getUrl()));
            page.setSubPagesNumber(1);
            page.addTargetPage(subPage);

            PageItem title = new PageItem("title", PageItem.PageItemType.STRING, true, false);
            title = extractBy(page, "//*[@id=\"mainbox\"]//h1/text()", PageProcessType.XPath, title);
            putItem(page,title);
        }else{
            PageItem author = new PageItem("author", PageItem.PageItemType.STRING, true, false);
            author = extractBy(page, "//*[@id=\"timelinePage\"]//span[@class=\"user_name\"]/h1/text()", PageProcessType.XPath, author);
            putItem(page,author);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testExtractContentUrl() throws Exception{
        PageProcessor processor = new SubPagesTest();
        Spider.create(processor)
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("ximalaya339173.html").setTest(true)) //网上url: http://www.ximalaya.com/20115042/album/339173
                .run();


        //做测试
        TestableProcessor testableProcessor = (TestableProcessor) processor;
        Page page = testableProcessor.getPage();
        List<PageItem> pageItems = page.getPageItems();

        assertThat(pageItems.size()).isEqualTo(2);
        assertThat(page.getPageItemByName("title").getItemValue().toString()).isEqualToIgnoringCase("玛格丽特的秘密：大鹏讲故事");
        assertThat(page.getPageItemByName("author").getItemValue().toString()).isEqualToIgnoringCase("大鹏讲故事");
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
