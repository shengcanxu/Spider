package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.PageItems;
import web.cano.spider.Task;

import java.util.List;
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

        boolean isMultiple = false;
        int multiNumber = 1;
        for(PageItem item : pageItems.getItems()){
            if(item.isMultiple()){
                isMultiple = true;
                List<String> list = (List<String>) item.getItemValue();
                multiNumber = list.size();
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("get page: " + pageItems.getPage().getRequest().getUrl() + "\n");
        for(int i=0; i<multiNumber; i++) {
            for (PageItem item : pageItems.getItems()) {
                if(item.isMultiple()){
                    List<String> list = (List<String>) item.getItemValue();
                    sb.append(item.getItemName() + ":\t" + list.get(i) + "\n");
                }else {
                    sb.append(item.getItemName() + ":\t" + item.getItemValue().toString() + "\n");
                }
            }
            sb.append("\n");
        }
        logger.info(sb.toString());
    }
}
