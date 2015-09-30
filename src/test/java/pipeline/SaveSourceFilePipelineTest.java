package pipeline;

import org.junit.Test;
import web.cano.spider.*;
import web.cano.spider.pipeline.SaveSourceFilePipeline;
import web.cano.spider.pipeline.TestCallabckPipeline;
import web.cano.spider.processor.DefaultPageProcessor;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com <br>
 */
public class SaveSourceFilePipelineTest extends DefaultPageProcessor implements TestableProcessor {
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
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Test
    public void testSaveSourceFile() throws Exception{
        PageProcessor processor = new SaveSourceFilePipelineTest();
        Spider.create(processor)
                .addPipeline(new SaveSourceFilePipeline("f:/test/"))
                .addPipeline(new TestCallabckPipeline())
                .addStartPage(new Page("http://www.baidu.com"))
                .run();


        //做测试
        File file = new File("f:/test/httpwwwbaiducom");
        assertThat(file.exists()).isEqualTo(true);
        assertThat(file.getUsableSpace()).isGreaterThan(100);

        file.delete();
        File folder= new File("f:/test/");
        folder.delete();
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
