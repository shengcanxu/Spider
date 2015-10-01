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

    public PageItems clone(){
        PageItems pageItems = new PageItems();
        pageItems.setPage(page);
        pageItems.getItems().addAll(items);
        return pageItems;
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

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("<$PageItems> <$items>");
        for(PageItem item : items){
            json.append(item.toJson());
            json.append("<$br>");
        }
        json.append("<$/items><$/PageItems>");

        return json.toString();
    }

    public static PageItems fromJson(String json, Page page){
        String items = json.substring(json.indexOf("<$items>")+8, json.lastIndexOf("<$/items>"));
        PageItems pageItems = new PageItems(page);
        String[] itemList = items.split("<$br>");
        for(int i=0; i<itemList.length-1; i++){
            PageItem item = PageItem.fromJson(itemList[i]);
        }
        return pageItems;
    }
}
