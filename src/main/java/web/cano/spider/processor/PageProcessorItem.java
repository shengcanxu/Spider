package web.cano.spider.processor;

import web.cano.spider.PageItems;

/**
 * Created by cano on 2015/9/14.
 */
public class PageProcessorItem {

    private String itemName;

    private PageItems.PageItemsType itemType;

    private boolean isNull;

    private boolean isMultiple;

    private String itemValue;

    private PageProcessorItem(){}

    public PageProcessorItem(String itemName, PageItems.PageItemsType itemType, boolean isNull, boolean isMultiple){
        this.itemName = itemName;
        this.itemType = itemType;
        this.isNull = isNull;
        this.isMultiple = isMultiple;
    }

    public String getItemName() {
        return itemName;
    }

    public PageItems.PageItemsType getItemType() {
        return itemType;
    }

    public boolean isNull() {
        return isNull;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }
}
