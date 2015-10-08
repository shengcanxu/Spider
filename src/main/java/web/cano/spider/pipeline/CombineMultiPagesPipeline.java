package web.cano.spider.pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.PageItems;
import web.cano.spider.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于爬取分页内容，合并多个分页到一个总页上
 */
public class CombineMultiPagesPipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String,Page[]> deferalMap = new HashMap<String, Page[]>();

    @Override
    public void process(Page page, Task task) {
        if (!page.hasMultiplePages() && !page.isSubMultiplePage()) return;
        page.setSkip(true);

        if(page.isSubMultiplePage()){
            String key = page.getMultiplePageKey();
            Page[] pages = deferalMap.get(key);
            if(pages == null){
                return;
            }else{
                pages[page.getMultiplePageIndex()] = page;
                boolean done = true;
                for(Page p : pages){
                    if(p == null){
                        done = false;
                        break;
                    }
                }
                if(done){  //所有multiplepages都爬取完了
                    combineMultiplepages(pages, page);
                    page.setSkip(false);
                }
            }

        }else if(page.hasMultiplePages()){
            int multiplePageNumber = page.getMultiplePageNumber();
            Page[] pages = new Page[multiplePageNumber+1];
            pages[0] = page;

            String key = DigestUtils.md5Hex(page.getUrl());
            deferalMap.put(key,pages);
        }
    }

    private void combineMultiplepages(Page[] pages,Page pageInPipeline){
        PageItems pageItems = pages[0].getPageItems();
        String name = pageInPipeline.getMultiplePageItemName();
        PageItem item = pageItems.getPageItemByName(name);
        if(item.getItemValue() instanceof List){
            List<String> list = (List<String>) item.getItemValue();
            for(int i=1; i<pages.length;i++){
                List<String> l = (List<String>) pages[i].getPageItems().getPageItemByName(name).getItemValue();
                list.addAll(l);
            }
            pageItems.setPageItemValue(name, list);
        }else{
            String content = item.getItemValue().toString();
            for(int i=1; i<pages.length; i++){
                content = content + pages[i].getPageItems().getPageItemByName(name).getItemValue();
            }
            pageItems.setPageItemValue(name, content);
        }

        pageInPipeline.setPageItems(pageItems);
    }
}
