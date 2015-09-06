package web.cano.spider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 存储页面上的爬取模型和爬取出来的结果
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 * @see Page
 * @see web.cano.spider.pipeline.Pipeline
 */
public class PageItems {

    public static enum Type {INT,STRING,TEXT,DATETIME}

    private Map<String, Object> items = new LinkedHashMap<String, Object>();

    private Map<String, Type> fields = new LinkedHashMap<String, Type>();

    private boolean skip = false;

    private Page page;

    private PageItems(){}

    public PageItems(Page page){
        this.page = page;
    }

    public PageItems clone(){
        PageItems pageItems = new PageItems();
        pageItems.setSkip(skip);
        pageItems.setPage(page);
        pageItems.getAllItems().putAll(this.items);
        pageItems.getAllFields().putAll(this.fields);
        return pageItems;
    }

    public Object getItem(String itemName) {
        Object o = items.get(itemName);
        if (o == null) {
            return null;
        }
        return o;
    }

    public Map<String, Object> getAllItems() {
        return items;
    }

    public PageItems putItem(String itemName, Object value) {
        items.put(itemName, value);
        return this;
    }

    public Type getField(String fieldName){
        Type type = fields.get(fieldName);
        if(type == null){
            return Type.STRING;
        }else{
            return type;
        }
    }

    public Map<String, Type> getAllFields(){
        return fields;
    }

    public PageItems putField(String fieldName, Type type){
        fields.put(fieldName, type);
        return this;
    }

    /**
     * Whether to skip the result.<br>
     * Result which is skipped will not be processed by Pipeline.
     *
     * @return whether to skip the result
     */
    public boolean isSkip() {
        return skip;
    }


    /**
     * Set whether to skip the result.<br>
     * Result which is skipped will not be processed by Pipeline.
     *
     * @param skip whether to skip the result
     * @return this
     */
    public PageItems setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "ResultItems{" +
                "items=" + items +
                ", skip=" + skip +
                '}';
    }
}
