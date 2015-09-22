package web.cano.spider.pipeline;

import web.cano.spider.Page;
import web.cano.spider.Task;

/**
 * Pipeline is the persistent and offline process part of crawler.<br>
 * The interface Pipeline can be implemented to customize ways of persistent.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 * @see ConsolePipeline
 */
public interface Pipeline {

    /**
     * Process extracted results.
     *
     * @param page
     * @param task
     */
    public void process(Page page, Task task);
}
