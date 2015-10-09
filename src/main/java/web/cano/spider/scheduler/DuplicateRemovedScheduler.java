package web.cano.spider.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.Task;
import web.cano.spider.scheduler.component.DuplicateRemover;
import web.cano.spider.scheduler.component.HashSetDuplicateRemover;

/**
 * Remove duplicate urls and only push urls which are not duplicate.<br></br>
 *
 * @author code4crafer@gmail.com
 * @since 0.5.0
 */
public abstract class DuplicateRemovedScheduler implements Scheduler {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private DuplicateRemover duplicatedRemover = new HashSetDuplicateRemover();

    public DuplicateRemover getDuplicateRemover() {
        return duplicatedRemover;
    }

    public DuplicateRemovedScheduler setDuplicateRemover(DuplicateRemover duplicatedRemover) {
        this.duplicatedRemover = duplicatedRemover;
        return this;
    }

    @Override
    public void push(Page page, Task task) {
        logger.trace("get a candidate url {}", page.getUrl());
        if (!duplicatedRemover.isDuplicate(page, task) ) {
            logger.debug("push to queue {}", page.getUrl());
            pushWhenNoDuplicate(page, task);
        }
    }

    @Override
    public void forcePush(Page page, Task task){
        logger.info("force to add to queue");
        pushWhenNoDuplicate(page,task);
    }

    @Override
    public void pushToHead(Page page, Task task){
        logger.trace("get a candidate url {}", page.getUrl());
        if (!duplicatedRemover.isDuplicate(page, task) ) {
            logger.debug("push to queue {}", page.getUrl());
            pushToHeadWhenNoDuplicate(page, task);
        }
    }

    protected void pushWhenNoDuplicate(Page page, Task task) {

    }

    protected void pushToHeadWhenNoDuplicate(Page page, Task task){

    }
}
