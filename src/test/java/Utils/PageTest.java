package Utils;

import org.junit.Test;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.PageItems;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by canoxu on 2015/11/4.
 */
public class PageTest {

    @Test
    public void testPagetoJson(){
        Page page = new Page("http://www.baidu.com");
        PageItems pageItems = new PageItems(page);
        List<PageItem> list = new ArrayList<PageItem>();
        PageItem item1 = new PageItem("test1", PageItem.PageItemType.STRING,false,false);
        item1.setItemValue("abc");
        list.add(item1);
        PageItem item2 = new PageItem("test2", PageItem.PageItemType.INT,false,false);
        item2.setItemValue(3);
        list.add(item2);
        PageItem item3 = new PageItem("test3", PageItem.PageItemType.STRING,false,false);
        item3.setItemValue(false);
        list.add(item3);
        pageItems.setItems(list);
        page.setPageItems(pageItems);

        String json = page.toJson();
        System.out.println(json);

        Page newPage = Page.fromJson(json);
        assertThat(newPage.getUrl()).isEqualTo("http://www.baidu.com");
        assertThat(newPage.isTest()).isEqualTo(false);
        assertThat(newPage.getPageItems().getItems().size()).isEqualTo(3);
        List<PageItem>  items = newPage.getPageItems().getItems();
        assertThat(items.get(0).getItemName()).isEqualTo("test1");
        assertThat(items.get(0).getItemValue()).isEqualTo("abc");
    }
}
