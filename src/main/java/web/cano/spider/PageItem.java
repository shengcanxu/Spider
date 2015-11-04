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
}
