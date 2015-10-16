package web.cano.spider;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cano on 2015/9/14.
 */
public class PageItem {
    public static enum PageItemType {INT,STRING,TEXT, DATE}

    private String itemName;

    private PageItemType itemType;

    private boolean isNull;

    private boolean isMultiple;

    private Object itemValue;

    private PageItem(){}

    public PageItem(String itemName, PageItemType itemType, boolean isNull, boolean isMultiple){
        this.itemName = itemName;
        this.itemType = itemType;
        this.isNull = isNull;
        this.isMultiple = isMultiple;
    }

    public String getItemName() {
        return itemName;
    }

    public PageItemType getItemType() {
        return itemType;
    }

    public boolean isNull() {
        return isNull;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public Object getItemValue() {
        return itemValue;
    }

    public void setItemValue(Object itemValue) {
        this.itemValue = itemValue;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemType(PageItemType itemType) {
        this.itemType = itemType;
    }

    public void setIsNull(boolean isNull) {
        this.isNull = isNull;
    }

    public void setIsMultiple(boolean isMultiple) {
        this.isMultiple = isMultiple;
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("<$PageItem>");
        json.append("<$itemName>" + itemName + "<$/itemName>");
        json.append("<$itemType>" + itemType.toString() + "<$/itemType>");
        json.append("<$isNull>" + isNull + "<$/isNull>");
        json.append("<$isMultiple>" + isMultiple + "<$/isMultiple>") ;
        if(itemValue == null) {
            json.append("<$itemValue><$/itemValue>");
        }else if(isMultiple){
            List<String> list = (List<String>) itemValue;
            String value = list.get(0);
            for(int i=1; i<list.size(); i++){
                value = "@#$" + list.get(i);
            }
            json.append("<$itemValue>" + value + "<$/itemValue>");
        }else{
            json.append("<$itemValue>" + (String)itemValue + "<$/itemValue>");
        }
        json.append("<$/PageItem>");

        return json.toString();
    }

    public static PageItem fromJson(String json){
        String itemName = json.substring(json.indexOf("<$itemName>") + 11, json.lastIndexOf("<$/itemName>"));
        String typeString = json.substring(json.indexOf("<$itemType>") + 11, json.lastIndexOf("<$/itemType>"));
        PageItemType itemType = PageItemType.STRING;
        if(typeString.equals("INT")){
            itemType = PageItemType.INT;
        }else if (typeString.equals("STRING")){
            itemType = PageItemType.STRING;
        }else if (typeString.equals("TEXT")){
            itemType = PageItemType.TEXT;
        }else if(typeString.equals("DATE")){
            itemType = PageItemType.DATE;
        }

        boolean isNull = Boolean.parseBoolean(json.substring(json.indexOf("<$isNull>") + 9, json.lastIndexOf("<$/isNull>")));
        boolean isMultiple = Boolean.parseBoolean(json.substring(json.indexOf("<$isMultiple>") + 13, json.lastIndexOf("<$/isMultiple>")));
        String itemValue = json.substring(json.indexOf("<$itemValue>") + 12, json.lastIndexOf("<$/itemValue>"));

        PageItem pageItem = new PageItem();
        pageItem.setItemName(itemName);
        pageItem.setItemType(itemType);
        pageItem.setIsNull(isNull);
        pageItem.setIsMultiple(isMultiple);
        if(itemValue.trim().length() == 0) {
            pageItem.setItemValue(null);
        }else if(isMultiple){
            String[] values = itemValue.split("@#$");
            List<String> list = Arrays.asList(values);
            pageItem.setItemValue(list);
        }else{
            pageItem.setItemValue(itemValue);
        }

        return pageItem;
    }
}
