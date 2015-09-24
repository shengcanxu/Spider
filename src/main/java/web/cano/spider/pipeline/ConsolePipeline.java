package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
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

        String json = page.toJson();
        logger.info(json);

        Page page2 = Page.fromJson(json);

        StringBuilder sb = new StringBuilder();
        sb.append("get page: " + pageItems.getPage().getRequest().getUrl() + "\n");
        for (PageItem item : page.getPageItems().getItems()) {
            sb.append(item.getItemName() + ":\t" + item.getItemValue() + "\n");
        }
        logger.info(sb.toString());
    }
}
