package web.cano.spider.pipeline;

import web.cano.spider.Page;
import web.cano.spider.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author code4crafter@gmail.com
 * @since 0.4.0
 */
public class ResultItemsCollectorPipeline implements CollectorPipeline<Page> {

    private List<Page> collector = new ArrayList<Page>();

    @Override
    public synchronized void process(Page page, Task task) {
        collector.add(page);
    }

    @Override
    public List<Page> getCollected() {
        return collector;
    }
}
