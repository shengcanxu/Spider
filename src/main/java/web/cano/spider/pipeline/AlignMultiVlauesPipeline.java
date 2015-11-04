package web.cano.spider.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.*;

import java.util.List;

/**
 * 如果是爬取多个内容的时候， 不同爬取内容所爬取到的个数不一样的话，
 * 这个pipeline就用于对少的结果加入更多的内容， 使它和多的结果一样多数量内容
 */
public class AlignMultiVlauesPipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void process(Page page, Task task) {
        if(page.isSkip() || page.isResource()) return;

        Spider spider = (Spider) task;
        if(!task.getSite().isShouldSplitToMultipleValues()) return;
        List<PageItem> pageItems = page.getPageItems();

        boolean isMultiple = false;
        int multiNumber = 1;
        for(PageItem item : pageItems){
            if(item.isMultiple()){
                isMultiple = true;
                List<String> list = (List<String>) item.getItemValue();
                if(list.size() > multiNumber) multiNumber = list.size();
            }
        }
        if(!isMultiple) return;

        for (PageItem item : pageItems) {
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
