package web.cano.spider.pipeline;

import org.junit.BeforeClass;
import org.junit.Test;
import web.cano.spider.*;

import java.util.UUID;

/**
 * Created by ywooer on 2014/5/6 0006.
 */
public class FilePipelineTest {

    private static PageItems pageItems;
    private static Task task;

    @BeforeClass
    public static void before() {
        pageItems = new PageItems(new Page("http://www.baidu.com"));
        pageItems.putItem("content", "webmagic 爬虫工具");
        Request request = new Request(new Page("http://www.baidu.com"));

        task = new Task() {
            @Override
            public String getUUID() {
                return UUID.randomUUID().toString();
            }

            @Override
            public Site getSite() {
                return null;
            }
        };
    }
    @Test
    public void testProcess() {
        //FilePipeline filePipeline = new FilePipeline();
        //TODO: fix the test case here
        //filePipeline.process(resultItems, task);

    }
}
