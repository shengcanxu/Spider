package web.cano.spider.scheduler;

import org.apache.http.annotation.ThreadSafe;
import web.cano.spider.Page;
import web.cano.spider.Task;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Basic Scheduler implementation.<br>
 * Store urls to fetch in LinkedBlockingQueue and remove duplicate urls by HashMap.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
@ThreadSafe
public class QueueScheduler extends DuplicateRemovedScheduler implements MonitorableScheduler {

    private BlockingQueue<Page> queue = new LinkedBlockingQueue<Page>();

    @Override
    public void pushWhenNoDuplicate(Page page, Task task) {
        queue.add(page);
    }

    @Override
    public synchronized Page poll(Task task) {
        return queue.poll();
    }

    @Override
    public void completeParse(Page page, Task task){

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

    @Override
    public int getLeftRequestsCount(Task task) {
        return queue.size();
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return getDuplicateRemover().getTotalRequestsCount(task);
    }
}
