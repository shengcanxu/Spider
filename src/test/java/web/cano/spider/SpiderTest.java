package web.cano.spider;

import org.junit.Ignore;
import org.junit.Test;
import web.cano.spider.downloader.Downloader;
import web.cano.spider.pipeline.Pipeline;
import web.cano.spider.processor.PageProcessor;
import web.cano.spider.processor.SimplePageProcessor;
import web.cano.spider.scheduler.Scheduler;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author code4crafter@gmail.com
 */
public class SpiderTest {

    @Ignore("long time")
    @Test
    public void testStartAndStop() throws InterruptedException {
        Spider spider = Spider.create(new SimplePageProcessor("http://www.oschina.net/", "http://www.oschina.net/*")).addPipeline(new Pipeline() {
            @Override
            public void process(Page page, Task task) {
                System.out.println(1);
            }
        }).addPageToScheduler(new Page("http://www.oschina.net/")).thread(1);
        spider.start();
        Thread.sleep(1000);
        spider.stop();
        Thread.sleep(1000);
        spider.start();
        Thread.sleep(1000);
    }

    @Ignore("long time")
    @Test
    public void testWaitAndNotify() throws InterruptedException {
        for (int i = 0; i < 10000; i++) {
            System.out.println("round " + i);
            testRound();
        }
    }

    private void testRound() {
        Spider spider = Spider.create(new PageProcessor() {

            private AtomicInteger count = new AtomicInteger();

            @Override
            public void process(Page page) {
                page.setSkip(true);
            }

            @Override
            public Site getSite() {
                return Site.me().setSleepTime(0);
            }
        }).setDownloader(new Downloader() {
            @Override
            public Page download(Request request, Task task) {
                return new Page("http://www.baidu.com").setRawText("");
            }

            @Override
            public void setThread(int threadNum) {

            }
        }).setScheduler(new Scheduler() {

            private AtomicInteger count = new AtomicInteger();

            private Random random = new Random();

            @Override
            public void push(Page page, Task task) {

            }

            @Override
            public synchronized Page poll(Task task) {
                if (count.incrementAndGet() > 1000) {
                    return null;
                }
                if (random.nextInt(100)>90){
                    return null;
                }
                return new Page("test");
            }

            @Override
            public void completeParse(Request request, Task task) {

            }

            @Override
            public List checkIfCompleteParse(Task task) {
                return null;
            }

            @Override
            public void saveQueue(Task task) {

            }

            @Override
            public void recoverQueue(Task task) {

            }
        }).thread(10);
        spider.run();
    }
}
