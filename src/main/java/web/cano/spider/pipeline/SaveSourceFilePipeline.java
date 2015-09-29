package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.*;

import java.util.List;

/**
 * Write results in console.<br>
 * Usually used in test.
 *
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class SaveSourceFilePipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Page page, Task task) {
        if (page.isSkip()) return;

        Spider spider = (Spider) task;
        PageItems pageItems = page.getPageItems();

        boolean isMultiple = false;
        int multiNumber = 1;
        for(PageItem item : pageItems.getItems()){
            if(item.isMultiple()){
                isMultiple = true;
                List<String> list = (List<String>) item.getItemValue();
                multiNumber = list.size();
                break;
            }
        }s
    }
}
