package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.Spider;
import web.cano.spider.Task;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.TestableProcessor;

/**
 * 用户回调test processor，使得可以完成test cases
 */
public class TestCallabckPipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Page page, Task task) {
        Spider spider = (Spider) task;
        PageProcessor processor = spider.getPageProcessor();
        if(processor instanceof TestableProcessor){
            TestableProcessor testableProcessor = (TestableProcessor) processor;
            testableProcessor.testCallback(spider,page);
        }
    }
}
