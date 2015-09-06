package web.cano.spider.scheduler.component;

import web.cano.spider.Page;
import web.cano.spider.Task;

/**
 * Remove duplicate requests.
 * @author code4crafer@gmail.com
 * @since 0.5.1
 */
public interface DuplicateRemover {
    /**
     *
     * Check whether the request is duplicate.
     *
     * @param page
     * @param task
     * @return
     */
    public boolean isDuplicate(Page page, Task task);

    /**
     * Get TotalRequestsCount for monitor.
     * @param task
     * @return
     */
    public int getTotalRequestsCount(Task task);

}
