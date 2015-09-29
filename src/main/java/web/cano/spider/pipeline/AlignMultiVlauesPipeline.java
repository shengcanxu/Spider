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
public class AlignMultiVlauesPipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Page page, Task task) {
        if(page.isSkip()) return;
        Spider spider = (Spider) task;
        if(!task.getSite().isShouldSplitToMultipleValues()) return;
        PageItems pageItems = page.getPageItems();

        boolean isMultiple = false;
        int multiNumber = 1;
        for(PageItem item : pageItems.getItems()){
            if(item.isMultiple()){
                isMultiple = true;
                List<String> list = (List<String>) item.getItemValue();
                if(list.size() > multiNumber) multiNumber = list.size();
            }
        }
        if(!isMultiple) return;

        for (PageItem item : pageItems.getItems()) {
            if(item.isMultiple()){
                List<String> list = (List<String>) item.getItemValue();
                for(int i= list.size(); i<multiNumber; i++){
                    list.add("");
                }
                if(list.size() < multiNumber){
                    item.setItemValue(list);
                }
            }
        }
    }
}
