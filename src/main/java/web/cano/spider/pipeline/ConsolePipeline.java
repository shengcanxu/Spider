package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItems;
import web.cano.spider.Task;

import java.util.Map;

/**
 * Write results in console.<br>
 * Usually used in test.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class ConsolePipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Page page, Task task) {
        if(page.isSkip()) return;
        PageItems pageItems = page.getPageItems();

        StringBuilder sb = new StringBuilder();
        sb.append("get page: " + pageItems.getPage().getRequest().getUrl() + "\n");
        for (Map.Entry<String, Object> entry : pageItems.getAllItems().entrySet()) {
            sb.append(entry.getKey() + ":\t" + entry.getValue() + "\n");
        }
        logger.info(sb.toString());
    }
}
