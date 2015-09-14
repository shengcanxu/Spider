package web.cano.spider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    Logger logger = LoggerFactory.getLogger(getClass());

    public static enum PageItemsType {INT,STRING,TEXT,DATETIME}

    private Map<String, String> items = new LinkedHashMap<String, String>();

    private Map<String, PageItemsType> fields = new LinkedHashMap<String, PageItemsType>();

    private Page page;

    private PageItems(){}

    public PageItems(Page page){
        this.page = page;
    }

    public PageItems clone(){
        PageItems pageItems = new PageItems();
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

    public Map<String, String> getAllItems() {
        return items;
    }

    public PageItems putItem(String itemName, String value) {
        items.put(itemName, value);
        return this;
    }

    public PageItemsType getField(String fieldName){
        PageItemsType pageItemsType = fields.get(fieldName);
        if(pageItemsType == null){
            return PageItemsType.STRING;
        }else{
            return pageItemsType;
        }
    }

    public Map<String, PageItemsType> getAllFields(){
        return fields;
    }

    public PageItems putField(String fieldName, PageItemsType pageItemsType){
        fields.put(fieldName, pageItemsType);
        return this;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("<$PageItems> <$items>");
        for(Map.Entry<String,String> entry : items.entrySet()){
            json.append("<$" + entry.getKey() + ">" + entry.getValue() + "<$/" + entry.getKey() + ">");
        }
        json.append("<$/items><$fields>");

        for(Map.Entry<String, PageItemsType> entry : fields.entrySet()){
            json.append("<$" + entry.getKey() + ">" + entry.getValue() + "<$/" + entry.getKey() + ">");
        }
        json.append("<$/fields><$/PageItems>");

        return json.toString();
    }

    public static PageItems fromJson(String json, Page page){
        String items = json.substring(json.indexOf("<$items>")+8, json.lastIndexOf("<$/items>"));
        String fields = json.substring(json.indexOf("<$fields>")+8, json.lastIndexOf("<$/fields>"));

        PageItems pageItems = new PageItems(page);
        String[] itemList = items.split("<\\$/");
        for(int i=0; i<itemList.length-1; i++){
            String[] list = itemList[i].split("<\\$")[1].split(">");
            pageItems.putItem(list[0], list[1]);
        }
        String[] fieldList = fields.split("<\\$/");
        for(int i=0; i<fieldList.length-1; i++){
            String[] list = fieldList[i].split("<\\$")[1].split(">");
            //TODO: change the type here
            pageItems.putField(list[0], PageItemsType.STRING);
        }
        return pageItems;
    }
}
