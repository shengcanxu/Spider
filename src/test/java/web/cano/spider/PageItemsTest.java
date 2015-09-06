package web.cano.spider;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com
 */
public class PageItemsTest {

    @Test
    public void testOrderOfEntries() throws Exception {
        PageItems pageItems = new PageItems(new Page("http://www.baidu.com"));
        pageItems.putItem("a", "a");
        pageItems.putItem("b", "b");
        pageItems.putItem("c", "c");
        assertThat(pageItems.getAllItems().keySet()).containsExactly("a","b","c");

    }
}
