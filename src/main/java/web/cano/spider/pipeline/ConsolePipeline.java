package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.*;

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
        if (page.isSkip()) return;
        Spider spider = (Spider) task;
        if(spider.getSite().isShouldSplitToMultipleValues()){
            printAsMultipleRecords(page);
        }else {
            String separator = spider.getSite().getMultiValueSeparator();
            printAsSingleRecord(page,separator);
        }
    }

    private void printAsSingleRecord(Page page, String separator){
        PageItems pageItems = page.getPageItems();
        StringBuilder sb = new StringBuilder();
        sb.append("get page: " + pageItems.getPage().getRequest().getUrl() + "\n");
        for (PageItem item : pageItems.getItems()) {
            if(item.isMultiple()){
                List<String> list = (List<String>) item.getItemValue();
                sb.append(item.getItemName() + ":\t"  + list.get(0));
                for(int i=1; i<list.size(); i++) {
                    sb.append(separator + list.get(i));
                }
                sb.append("\n");
            }else {
                sb.append(item.getItemName() + ":\t" + item.getItemValue().toString() + "\n");
            }
        }
        logger.info(sb.toString());
    }

    private void printAsMultipleRecords(Page page){
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
