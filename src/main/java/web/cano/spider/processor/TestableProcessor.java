package web.cano.spider.processor;

import web.cano.spider.Page;
import web.cano.spider.Spider;

import javax.annotation.processing.Processor;

/**
 * Created by canoxu on 2015/9/23.
 */
public interface TestableProcessor {

    /**
     * 在pipeline里面回调，用于做测试对比
     * @param spider
     * @param page
     */
    public void testCallback(Spider spider, Page page);

    public Page getPage();

    public Spider getSpider();
}
