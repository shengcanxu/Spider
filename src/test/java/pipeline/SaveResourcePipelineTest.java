package pipeline;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import web.cano.spider.Page;
import web.cano.spider.Site;
import web.cano.spider.Spider;
import web.cano.spider.pipeline.SaveResourcePipeline;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class SaveResourcePipelineTest extends DefaultPageProcessor implements TestableProcessor {
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
        if(page.isResource()){
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testSaveSourceFile() throws Exception{
        PageProcessor processor = new SaveResourcePipelineTest();
        Spider.create(processor)
                .addPipeline(new SaveResourcePipeline("f:/test/"))
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("sina_cn.jpg").setTest(true).setIsResource(true))
                .run();


        //做测试
        File file = new File("f:/test/25e52d8af2b3f27cb5829ea0da0c027b.jpg");
        assertThat(file.exists()).isEqualTo(true);
        assertThat(file.getUsableSpace()).isGreaterThan(100);

        FileUtils.forceDelete(new File("f:/test/"));
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
