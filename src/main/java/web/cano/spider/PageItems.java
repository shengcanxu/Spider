package web.cano.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储页面上的爬取模型和爬取出来的结果
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 * @see Page
 * @see web.cano.spider.pipeline.Pipeline
 */
public class PageItems {
    Logger logger = LoggerFactory.getLogger(getClass());

    private List<PageItem> items = new ArrayList<PageItem>();

    private Page page;

    private PageItems(){}

    public PageItems(Page page){
        this.page = page;
    }

    public List<PageItem> getItems() {
        return items;
    }

    public void setItems(List<PageItem> items) {
        this.items = items;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public PageItem getPageItemByName(String name){
        for(PageItem item : items){
            if(item.getItemName().equals(name)){
                return item;
            }
        }
        return null;
    }

    public PageItems setPageItemValue(String name, Object value){
        for(PageItem item : items){
            if(item.getItemName().equals(name)){
                item.setItemValue(value);
            }
        }
        return this;
    }

}
